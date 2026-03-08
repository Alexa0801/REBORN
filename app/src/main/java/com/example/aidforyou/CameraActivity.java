package com.example.aidforyou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.aidforyou.helpers.MLImageHelperActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class CameraActivity extends MLImageHelperActivity {

    private ImageLabeler imageLabeler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build());imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build());
    }

    @Override
    protected void runDetection(Bitmap bitmap) {
        // aici vine logica ta MLKit sau orice procesare imagine
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        imageLabeler.process(inputImage).addOnSuccessListener(imageLabels -> {
            StringBuilder sb = new StringBuilder();
            for (ImageLabel label : imageLabels) {
                sb.append(label.getText()).append(": ").append(label.getConfidence()).append("\n");
            }
            if (imageLabels.isEmpty()) {
                getOutputTextView().setText("Could not classify!!");
            } else {
                getOutputTextView().setText(sb.toString());
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }

    @Override
    public void onPickImage(android.view.View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Document picker recomandat pentru compatibilitate
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        }

        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onTakeImage(android.view.View view) {
        // folosește implementarea din MLImageHelperActivity
        super.onTakeImage(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
                && resultCode == RESULT_OK) {

            Bitmap bitmap = null;

            if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    bitmap = loadFromUri(uri);
                }
            } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                bitmap = getCapturedImage();
                bitmap = rotateIfRequiredSafe(bitmap);
            }

            if (bitmap != null) {
                // Asigură-te că ImageView-ul este layout-uit
                Bitmap finalBitmap = bitmap;
                getInputImageView().post(() -> {
                    getInputImageView().setImageBitmap(finalBitmap);
                    runDetection(finalBitmap);
                });
            } else {
                Toast.makeText(this, "Nu s-a putut încărca imaginea!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap rotateIfRequiredSafe(Bitmap bitmap) {
        try {
            return super.rotateImage(bitmap, getOrientation(photoFile));
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private int getOrientation(java.io.File file) {
        try {
            android.media.ExifInterface exif = new android.media.ExifInterface(file.getAbsolutePath());
            return exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            return android.media.ExifInterface.ORIENTATION_NORMAL;
        }
    }
}