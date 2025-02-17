package com.example.aidforyou;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SoundActivity extends AppCompatActivity {

    Button lanternButton;
    Button vibreteButton;
    Vibrator vibrator;
    boolean hasCameraFlash = false;
    boolean flashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sound);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lanternButton = findViewById(R.id.lanternButton);
        vibreteButton = findViewById(R.id.vibrateButton);

        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        handleLantern();
        handleVibration();

    }

    private void handleLantern(){
        lanternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasCameraFlash){
                    if (flashOn){
                        flashOn = false;
                        try {
                            flashlightOff();
                        } catch (CameraAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        flashOn = true;
                        try {
                            flashlightOn();
                        } catch (CameraAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                else {
                    Toast.makeText(SoundActivity.this, "No flashlight", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleVibration(){
        vibreteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 26){
                    vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                else {
                    vibrator.vibrate(500);
                }
            }
        });
    }

    private void flashlightOn() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = cameraManager.getCameraIdList()[0];
        cameraManager.setTorchMode(cameraId, true);
    }
    private void flashlightOff() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = cameraManager.getCameraIdList()[0];
        cameraManager.setTorchMode(cameraId, false);
    }
}