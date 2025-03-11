package com.example.aidforyou;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.Manifest;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aidforyou.helpers.AudioHelperActivity;


public class SoundActivity extends AudioHelperActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    Vibrator vibrator;
    boolean hasCameraFlash = false;
    boolean flashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        checkPermissions();

        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    private void handleLantern(){
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

    private void handleVibration()
    {
        if (Build.VERSION.SDK_INT >= 26){
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            vibrator.vibrate(500);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    public void onStartRecording(View view) {
        Intent serviceIntent = new Intent(this, SoundRecognitionService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void onStopRecording(View view) {
        Intent serviceIntent = new Intent(this, SoundRecognitionService.class);
        stopService(serviceIntent);
    }

}


