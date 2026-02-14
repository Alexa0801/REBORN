package com.example.aidforyou;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button smartCamBtn;
    Button soundDetectBtn;
    Button mapBtn;
    ImageButton btnOpenSettings;

    private float currentSize = 18f;
    private boolean isBold = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnOpenSettings = findViewById(R.id.btnOpenSettings);
        smartCamBtn = findViewById(R.id.smartCamBtn);
        soundDetectBtn = findViewById(R.id.soundDetectBtn);
        mapBtn = findViewById(R.id.mapBtn);

        btnOpenSettings.setOnClickListener(v -> showSettingsDialog());

        mapBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MapActivity.class)));

        smartCamBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));

        soundDetectBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SoundActivity.class)));
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Text Settings");

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        // Find views inside the dialog
        SeekBar seekBarFontSize = dialogView.findViewById(R.id.seekBarFontSize);
        CheckBox checkBold = dialogView.findViewById(R.id.checkBold);

        // Pre-fill with current values
        seekBarFontSize.setProgress((int) currentSize);
        checkBold.setChecked(isBold);

        builder.setPositiveButton("Apply" ,(dialog, which) -> {
            // 1. Get Font Size
            currentSize = seekBarFontSize.getProgress();

            // 2. Get Bold status
            isBold = checkBold.isChecked();

            applySettingsToButtons();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applySettingsToButtons() {
        // Apply settings to all buttons (excluding ImageButton)
        Button[] buttons = {smartCamBtn, soundDetectBtn, mapBtn};
        int style = isBold ? Typeface.BOLD : Typeface.NORMAL;

        for (Button button : buttons) {
            button.setTextSize(currentSize);
            button.setTypeface(Typeface.create("sans-serif", style));

            // Adjust button height based on text size
            int newHeight = (int) (currentSize * 9); // Example multiplier for height
            ViewGroup.LayoutParams params = button.getLayoutParams();
            if (params != null) {
                params.height = newHeight;
                button.setLayoutParams(params);
            }
        }

        Toast.makeText(this, "Button Settings Updated", Toast.LENGTH_SHORT).show();
    }
}