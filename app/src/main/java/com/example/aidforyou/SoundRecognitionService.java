package com.example.aidforyou;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioRecord;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SoundRecognitionService extends Service {
    private static final String TAG = "AudioService";
    private static final String CHANNEL_ID = "AudioServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final float PROBABILITY_THRESHOLD = 0.3f;

    private AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Starting..."));

        try {
            classifier = AudioClassifier.createFromFile(this, "1.tflite");
            tensor = classifier.createInputTensorAudio();
            record = classifier.createAudioRecord();
            record.startRecording();

            startClassification();
        } catch (IOException e) {
            Log.e(TAG, "Error loading model", e);
            stopSelf();
        }
    }

    private void startClassification() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Load audio data from the microphone
                int numberOfSamples = tensor.load(record);
                List<Classifications> output = classifier.classify(tensor);

                // Filtering categories with score greater than threshold
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        if (category.getScore() > PROBABILITY_THRESHOLD) {
                            finalOutput.add(category);
                        }
                    }
                }

                // Sorting by score
                Collections.sort(finalOutput, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));

                // Prepare classification result string
                String resultStr = "No classification detected";
                if (!finalOutput.isEmpty()) {
                    Category topCategory = finalOutput.get(0); // Get the top category
                    resultStr = topCategory.getLabel() + ": " + topCategory.getScore();
                }

                Log.d(TAG, resultStr);

                // Update the notification with the classification result
                updateNotification(resultStr);
            }
        };

        timer.scheduleAtFixedRate(timerTask, 1000, 500); // Update every 500ms
    }

    private Notification createNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Classification Running")
                .setContentText(contentText) // This text will be updated dynamically
                .setSmallIcon(R.mipmap.ic_launcher)  // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String classificationResult) {
        // Update the content of the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Classification Running")
                .setContentText("Detected sound: " + classificationResult) // Show the detected sound
                .setSmallIcon(R.mipmap.ic_launcher)  // Replace with your app's icon
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // Send the updated notification to the NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Classification Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timerTask != null) timerTask.cancel();
        if (record != null) {
            record.stop();
            record.release();
        }
        Log.d(TAG, "Service Stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
