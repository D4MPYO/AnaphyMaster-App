package com.example.anaphymaster;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AboutRateApp extends AppCompatActivity {

    private ImageView[] stars = new ImageView[5];
    private int selectedRating = 0;
    private LinearLayout thankYouPopup;
    private SharedPreferences preferences;

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

        setContentView(R.layout.about_rate_app);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Init views
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);
        thankYouPopup = findViewById(R.id.thankYouPopup);

        // Load saved rating
        preferences = getSharedPreferences("RatingPrefs", Context.MODE_PRIVATE);
        selectedRating = preferences.getInt("user_rating", 0);
        if (selectedRating > 0) {
            updateStars(selectedRating);
        }

        // Back Button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> updateStars(rating));
        }

        // Rate button
        Button rateButton = findViewById(R.id.rateButton);
        rateButton.setOnClickListener(v -> {
            if (selectedRating > 0) {
                saveRating(selectedRating);
                showThankYouPopup();
            }
        });

    }

    private void updateStars(int rating) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
        }
    }

    private void saveRating(int rating) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("user_rating", rating);
        editor.apply();
    }

    private void showThankYouPopup() {
        thankYouPopup.setVisibility(View.VISIBLE);
    }
}
