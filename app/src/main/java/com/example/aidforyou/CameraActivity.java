package com.example.aidforyou;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private TextView textViewResult;
    private ImageView imageView;

    private Uri photoUri;
    private File photoFile;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openCameraFullRes();
                } else {
                    textViewResult.setText("Camera permission denied.");
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoFile != null) {
                    Bitmap bitmap = decodeDownsampledBitmap(photoFile.getAbsolutePath(), 1280, 1280);
                    imageView.setImageBitmap(bitmap);
                    runObjectRecognition(bitmap);
                } else {
                    textViewResult.setText("No image captured.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        textViewResult = findViewById(R.id.textViewResult);
        imageView = findViewById(R.id.imageView);
        Button buttonCaptureImage = findViewById(R.id.buttonCaptureImage);

        buttonCaptureImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            } else {
                openCameraFullRes();
            }
        });
    }

    private void openCameraFullRes() {
        try {
            photoFile = createTempImageFile();
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            textViewResult.setText("Failed to create image file: " + e.getMessage());
        }
    }

    private File createTempImageFile() throws IOException {
        File cacheDir = new File(getCacheDir(), "images");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        return File.createTempFile("capture_", ".jpg", cacheDir);
    }

    private Bitmap decodeDownsampledBitmap(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int inSampleSize = 1;
        int height = options.outHeight;
        int width = options.outWidth;

        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    private void runObjectRecognition(Bitmap bitmap) {
        // Better for “detect objects” + “label them”
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();

        ObjectDetector detector = ObjectDetection.getClient(options);

        // If your bitmap might be rotated, you need the correct rotation degrees.
        // For this flow (TakePicture -> decode file), rotation may still come from EXIF.
        // We'll keep 0 for now; for perfect results, read EXIF and pass rotation.
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(this::processObjectRecognitionResult)
                .addOnFailureListener(e -> textViewResult.setText("Error: " + e.getMessage()));
    }

    private void processObjectRecognitionResult(List<DetectedObject> detectedObjects) {
        StringBuilder sb = new StringBuilder();

        for (DetectedObject obj : detectedObjects) {
            if (obj.getLabels().isEmpty()) {
                sb.append("Object detected (unclassified)\n");
            } else {
                for (DetectedObject.Label label : obj.getLabels()) {
                    sb.append(label.getText())
                            .append(" (")
                            .append(Math.round(label.getConfidence() * 100))
                            .append("%)\n");
                }
            }
        }

        if (sb.length() == 0) sb.append("No objects detected.");
        textViewResult.setText(sb.toString());
    }
}
