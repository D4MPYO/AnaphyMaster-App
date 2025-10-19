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

public class Show_All_Topics extends AppCompatActivity {

    private ImageView backButton;

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

        setContentView(R.layout.show_all_study_guides);

        // Handle CATEGORY1 click
        CardView category1Card = findViewById(R.id.CATEGORY1);
        category1Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopocIntegumentary1.class); // Corrected class name
                startActivity(intent);
            }
        });

        CardView category2Card = findViewById(R.id.CATEGORY2);
        category2Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicCardiovascular2.class);
                startActivity(intent);
            }
        });

        CardView category3Card = findViewById(R.id.CATEGORY3);
        category3Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicSkeletal3.class);
                startActivity(intent);
            }
        });

        CardView category4Card = findViewById(R.id.CATEGORY4);
        category4Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicReproductive4.class);
                startActivity(intent);
            }
        });

        CardView category5Card = findViewById(R.id.CATEGORY5);
        category5Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicRespiratiry5.class); // Corrected spelling
                startActivity(intent);
            }
        });

        CardView category6Card = findViewById(R.id.CATEGORY6);
        category6Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicMuscular6.class);
                startActivity(intent);
            }
        });

        CardView category7Card = findViewById(R.id.CATEGORY7);
        category7Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicNervous7.class);
                startActivity(intent);
            }
        });

        CardView category8Card = findViewById(R.id.CATEGORY8);
        category8Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicEndocrine8.class);
                startActivity(intent);
            }
        });

        CardView category9Card = findViewById(R.id.CATEGORY9);
        category9Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicLyhpmatic9.class);
                startActivity(intent);
            }
        });

        CardView category10Card = findViewById(R.id.CATEGORY10);
        category10Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicDigestive10.class);
                startActivity(intent);
            }
        });

        CardView category11Card = findViewById(R.id.CATEGORY11);
        category11Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_All_Topics.this, TopicUrinary11.class);
                startActivity(intent);
            }
        });

        // Initialize back button
        backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish()); // Close the activity when clicked
        }
    }
}
