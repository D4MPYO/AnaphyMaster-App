package com.example.anaphymaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class FragmentHome extends Fragment {

    private DatabaseHelper dbHelper;

    public FragmentHome() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(getActivity());
        AverageHelper averageHelper = new AverageHelper(getActivity());


        // Fetch username and avatar from SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        String userName = preferences.getString("USER_NAME", "User");
        int avatarResId = preferences.getInt("SELECTED_AVATAR", R.drawable.avatar_icon);

        // Set username and avatar in the UI
        TextView usernameText = view.findViewById(R.id.usernameText);
        usernameText.setText("Welcome, " + userName + "!");

        ImageView profileAvatar = view.findViewById(R.id.ProfileAvatar);
        profileAvatar.setImageResource(avatarResId);

        // Fetch average stats
        float[] results = averageHelper.getAveragePercentages();
        float correctPercentage = results[0];
        float incorrectPercentage = results[1];

// Set to TextViews
        TextView averageCorrect = view.findViewById(R.id.AverageCorrect);
        TextView averageIncorrect = view.findViewById(R.id.AverageInCorrect);

        averageCorrect.setText("Avg. Correct Answers: " + String.format("%.1f", correctPercentage) + "%");
        averageIncorrect.setText("Avg. Incorrect Answers: " + String.format("%.1f", incorrectPercentage) + "%");

        // Handle QuizMode1 click
        CardView quizMode1Card = view.findViewById(R.id.QuizMode1);
        quizMode1Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectCategoryPractice.class);
                startActivity(intent);
            }
        });

        // Handle QuizMode2 click
        CardView quizMode2Card = view.findViewById(R.id.QuizMode2);
        quizMode2Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectDifficultyChallenge.class);
                startActivity(intent);
            }
        });

        // Handle CATEGORY1 click
        CardView category1Card = view.findViewById(R.id.CATEGORY1);
        category1Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopocIntegumentary1.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // Set Practice and Challenge counts
        updateQuizCounts(view);

        // Handle CATEGORY2 click
        CardView category2Card = view.findViewById(R.id.CATEGORY2);
        category2Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopicCardiovascular2.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // Handle CATEGORY3 click
        CardView category3Card = view.findViewById(R.id.CATEGORY3);
        category3Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopicSkeletal3.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // Handle CATEGORY4 click
        CardView category4Card = view.findViewById(R.id.CATEGORY4);
        category4Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopicReproductive4.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // Handle CATEGORY5 click
        CardView category5Card = view.findViewById(R.id.CATEGORY5);
        category5Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopicRespiratiry5.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // Handle CATEGORY6 click
        CardView category6Card = view.findViewById(R.id.CATEGORY6);
        category6Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopicMuscular6.class); // Replace with your actual activity
                startActivity(intent);
            }
        });

        // ✅ Handle viewAll TextView click
        TextView viewAll = view.findViewById(R.id.viewAll);
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Show_All_Topics.class);
                startActivity(intent);
            }
        });

        return view;
    }

    // Method to update the quiz counts for Practice and Challenge modes
    private void updateQuizCounts(View view) {
        // Get the counts for both modes
        int practiceCount = dbHelper.getQuizCount("Practice");
        int challengeCount = dbHelper.getQuizCount("Challenge");

        // Find TextViews where the counts will be displayed
        TextView takePracticeCount = view.findViewById(R.id.takePracticeCount);
        TextView takeChallengeCount = view.findViewById(R.id.takeChallengeCount);

        // Set the quiz counts to the TextViews
        takePracticeCount.setText("Practice Mode: " + practiceCount);
        takeChallengeCount.setText("Challenge Mode: " + challengeCount);
    }
}
