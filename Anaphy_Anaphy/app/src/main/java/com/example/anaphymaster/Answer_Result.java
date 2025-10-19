package com.example.anaphymaster;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Answer_Result extends AppCompatActivity {

    private TextView scoreTextView;
    private TextView Done_Exam; // Added for the "done" message
    private TextView Text_text;    // For the motivational message
    private Button backHome;

    private static final int PASSING_SCORE_PERCENT = 70; // Example passing score

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

        setContentView(R.layout.answer_result);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Initialize Views
        scoreTextView = findViewById(R.id.scoreTextView);
        Done_Exam = findViewById(R.id.Done_Exam); // Make sure you have this TextView in your XML
        Text_text = findViewById(R.id.Text_text);
        backHome = findViewById(R.id.backHome);

        // Get data from Intent
        int correctAnswers = getIntent().getIntExtra("correctAnswers", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        // Calculate the score percentage
        double scorePercentage = ((double) correctAnswers / totalQuestions) * 100;

        // Display the score in the format: "correctAnswers/totalQuestions"
        scoreTextView.setText(correctAnswers + "/" + totalQuestions);

        // Set Done_Exam TextView (celebratory main message)
        if (scorePercentage >= PASSING_SCORE_PERCENT) {
            Done_Exam.setText("Awesome Work! You Nailed It! 🏆");
            Done_Exam.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (scorePercentage >= 50) {
            Done_Exam.setText("You're Almost There! 🔥");
            Done_Exam.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            Done_Exam.setText("Don't Give Up! Try Again! 💡");
            Done_Exam.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Set Text_text TextView (motivational side message)
        if (scorePercentage >= PASSING_SCORE_PERCENT) {
            Text_text.setText("Great job! You're on your way to success! 🎯");
            Text_text.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (scorePercentage >= 50) {
            Text_text.setText("Not bad! Keep practicing to improve your score. 💪");
            Text_text.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            Text_text.setText("Don't give up! Try again and aim higher! 🚀");
            Text_text.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Animate backHome Button
        if (backHome != null) {
            Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
            backHome.startAnimation(buttonAnimation);
        }

        // Set onClickListener for backHome Button
        backHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Answer_Result.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        TextView difficultyLabel = findViewById(R.id.difficultyLabel);
        TextView categoryLabel = findViewById(R.id.categoryLabel);
        TextView modeTitle = findViewById(R.id.modeTitle);  // Added modeTitle
        TextView modeLabel = findViewById(R.id.modeLabel);  // Added modeLabel

// Get the Intent extras
        Intent intent = getIntent();
        String difficulty = intent.getStringExtra("difficulty");
        String category = intent.getStringExtra("category");
        String mode = intent.getStringExtra("mode");  // Assuming "mode" is passed in the Intent

// Set the TextViews
        difficultyLabel.setText(difficulty);
        categoryLabel.setText(category);
        modeLabel.setText(mode);  // Set modeLabel


    }

    @Override
    public void onBackPressed() {
        // Do nothing, so the user cannot go back
    }

}
