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

public class SelectCategoryPractice extends AppCompatActivity {

    private ImageView backButton;
    private CardView categoty1, CATEGORY2, CATEGORY3, CATEGORY4, CATEGORY5,
            CATEGORY6, CATEGORY7, CATEGORY8, CATEGORY9, CATEGORY10, CATEGORY11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request no title bar and set fullscreen portrait mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide ActionBar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Immersive sticky UI mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.select_category_practice);

        // Initialize back button
        backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish()); // Close the activity when clicked
        }

        // Initialize the CardViews for CATEGORY1 to CATEGORY11
        categoty1 = findViewById(R.id.categoty1);
        CATEGORY2 = findViewById(R.id.CATEGORY2);
        CATEGORY3 = findViewById(R.id.CATEGORY3);
        CATEGORY4 = findViewById(R.id.CATEGORY4);
        CATEGORY5 = findViewById(R.id.CATEGORY5);
        CATEGORY6 = findViewById(R.id.CATEGORY6);
        CATEGORY7 = findViewById(R.id.CATEGORY7);
        CATEGORY8 = findViewById(R.id.CATEGORY8);
        CATEGORY9 = findViewById(R.id.CATEGORY9);
        CATEGORY10 = findViewById(R.id.CATEGORY10);
        CATEGORY11 = findViewById(R.id.CATEGORY11);

        // Set OnClickListener for each category, directing to different activities
        categoty1.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category1.class)));
        CATEGORY2.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category2.class)));
        CATEGORY3.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category3.class)));
        CATEGORY4.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category4.class)));
        CATEGORY5.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category5.class)));
        CATEGORY6.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category6.class)));
        CATEGORY7.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category7.class)));
        CATEGORY8.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category8.class)));
        CATEGORY9.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category9.class)));
        CATEGORY10.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category10.class)));
        CATEGORY11.setOnClickListener(v -> startActivity(new Intent(SelectCategoryPractice.this, QuizIntro_Category11.class)));
    }
}
