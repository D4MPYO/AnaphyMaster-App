package com.example.anaphymaster;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SelectDifficultyChallenge extends AppCompatActivity {

    private ImageView backButton;
    // Declare CardViews for EasyMode and AdvancedMode
    private CardView EasyMode, AdvanceMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen + portrait lock
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.select_dificulty_challenge);

        // Initialize back button
        backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish()); // Close the activity when clicked
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Initialize CardViews by finding them from the layout
        EasyMode = findViewById(R.id.EasyMode);
        AdvanceMode = findViewById(R.id.AdvanceMode);

        // Set OnClickListener for EasyMode
        EasyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SelectCategoryPractice activity
                Intent intent = new Intent(SelectDifficultyChallenge.this, SelectEasyChallenge.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for AdvancedMode
        AdvanceMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SelectAdvanceChallenge activity
                Intent intent = new Intent(SelectDifficultyChallenge.this, SelectAdvanceChallenge.class);
                startActivity(intent);
            }
        });
    }
}
