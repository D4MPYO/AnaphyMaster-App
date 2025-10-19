package com.example.anaphymaster;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChallengeMode8 extends AppCompatActivity {

    private int correctAnswersCount = 0;  // To track the number of correct answers
    private int totalQuestions = 20;       // Total number of questions


    private final int maxQuestions = 20;

    private List<String> questions;
    private List<List<String>> choices;
    private List<String> correctAnswers;

    private TextView questionText, counterText, rationaleTextView, timerTextView;
    private Button btnA, btnB, btnC, btnD, nextQuestionButton;
    private ImageView restartIcon, exitIcon, rationaleIcon;

    private int currentIndex = 0;
    private List<Integer> questionOrder;

    private View rationaleCard;

    private HashMap<Integer, String> rationales;

    private boolean hasAnswered = false;

    private CountDownTimer countDownTimer;
    private long totalTimeInMillis = 60000; // 1 minute per question
    private long timeLeftInMillis = totalTimeInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AverageHelper averageHelper = new AverageHelper(this);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.challenge_mode8);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Initialize views
        questionText = findViewById(R.id.text_question);
        counterText = findViewById(R.id.cpt_question);
        btnA = findViewById(R.id.LetterA);
        btnB = findViewById(R.id.LetterB);
        btnC = findViewById(R.id.LetterC);
        btnD = findViewById(R.id.LetterD);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);

        restartIcon = findViewById(R.id.restartIcon);
        exitIcon = findViewById(R.id.exitIcon);
        rationaleIcon = findViewById(R.id.bottomRightImage);

        rationaleCard = findViewById(R.id.rationaleCard);
        rationaleTextView = findViewById(R.id.rationaleContent);

        timerTextView = findViewById(R.id.timerTextView); // ⏱ Timer TextView

        // Set Text Color
        questionText.setTextColor(getResources().getColor(R.color.white));
        counterText.setTextColor(getResources().getColor(R.color.white));
        rationaleTextView.setTextColor(getResources().getColor(R.color.white));

        btnA.setTextColor(getResources().getColor(R.color.white));
        btnB.setTextColor(getResources().getColor(R.color.white));
        btnC.setTextColor(getResources().getColor(R.color.white));
        btnD.setTextColor(getResources().getColor(R.color.white));

        resetButtonStyles();

        setupQuestionsAndChoices();
        randomizeQuestions();
        displayQuestion(currentIndex);

        startTimer(); // Start the timer for the first question

        btnA.setOnClickListener(view -> checkAnswer(btnA));
        btnB.setOnClickListener(view -> checkAnswer(btnB));
        btnC.setOnClickListener(view -> checkAnswer(btnC));
        btnD.setOnClickListener(view -> checkAnswer(btnD));

        rationaleIcon.setOnClickListener(v -> {
            if (hasAnswered) {
                showRationale(questionOrder.get(currentIndex));
            } else {
                Toast.makeText(ChallengeMode8.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode8.this)
                    .setTitle("Restart Quiz")
                    .setMessage("Are you sure you want to restart? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        resetQuiz();
                        resetTimer();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        exitIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode8.this)
                    .setTitle("Exit Quiz")
                    .setMessage("Are you sure you want to exit? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        nextQuestionButton.setOnClickListener(v -> {
            if (currentIndex < maxQuestions - 1) {
                currentIndex++;
                displayQuestion(currentIndex);
                resetTimer();  // Reset the timer for the next question
                rationaleCard.setVisibility(View.GONE);  // Hide rationale card when proceeding to next question
            } else {
                // Show a dialog asking the user if they want to see the results
                new AlertDialog.Builder(ChallengeMode8.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode8.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Endocrine System", correctAnswersCount, totalQuestions);


                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Endocrine System");
                            intent.putExtra("mode", "Challenge Mode");

                            startActivity(intent);
                        })
                        .show();
            }
        });

    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int minutes = (int) (timeLeftInMillis / 1000) / 60;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timerTextView.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                setButtonsEnabled(false);
                hasAnswered = true;

                // Removed: showRationale(realIndex);

                new Handler().postDelayed(() -> {
                    if (currentIndex < maxQuestions - 1) {
                        currentIndex++;
                        displayQuestion(currentIndex);
                        resetTimer();
                    } else {
                        // Automatically go to results screen
                        Intent intent = new Intent(ChallengeMode8.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode8.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode8.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Endocrine System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Endocrine System");
                        intent.putExtra("mode", "Challenge Mode");

                        startActivity(intent);
                        finish(); // Prevent user from going back to quiz
                    }
                }, 500); // Optional short delay
            }
        }.start();
    }

    private void showTimeUpDialog() {
        // Disable the answer buttons to prevent further interaction
        setButtonsEnabled(false);

        // Check if the rationale is already being displayed
        if (rationaleCard.getVisibility() == View.VISIBLE) {
            return; // Skip showing the "Time's Up" dialog
        }

        // Get the rationale for the current question
        int realIndex = questionOrder.get(currentIndex);
        String rationale = rationales.get(realIndex);

        // Display rationale before the "Time's Up!" dialog
        if (rationale != null && !rationale.isEmpty()) {
            String[] parts = rationale.split("\\n");
            SpannableStringBuilder formattedRationale = new SpannableStringBuilder();

            for (String part : parts) {
                int start = formattedRationale.length();
                formattedRationale.append(part).append("\n");

                if (part.contains("(Correct answer)")) {
                    formattedRationale.setSpan(
                            new ForegroundColorSpan(getResources().getColor(R.color.green)),
                            start,
                            start + part.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (part.contains("(Incorrect)")) {
                    formattedRationale.setSpan(
                            new ForegroundColorSpan(getResources().getColor(R.color.red)),
                            start,
                            start + part.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }

            rationaleTextView.setText(formattedRationale);
            rationaleCard.setVisibility(View.VISIBLE);
        } else {
            rationaleCard.setVisibility(View.GONE);
        }

        // Show the Time's Up dialog without automatically proceeding
        new AlertDialog.Builder(ChallengeMode8.this)
                .setTitle("Time's Up!")
                .setMessage("Time has run out. Please review the rationale before moving to the next question.")
                .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss())
                .setCancelable(false) // Prevent closing by back button or outside tap
                .show();
    }



    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = totalTimeInMillis;
        startTimer();
    }

    private void setupQuestionsAndChoices() {
        questions = new ArrayList<>();
        choices = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        rationales = new HashMap<>();

        questions.add("Which of the following glands is known as the \"master gland\" of the endocrine system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pituitary", // Correct answer
                "Thyroid",
                "Adrenal",
                "Pancreas"
        )));
        correctAnswers.add("Pituitary");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Pituitary (Correct answer)\n" +
                        "The pituitary gland is known as the \"master gland\" because it secretes hormones that regulate other endocrine glands.\n\n" +
                        "Thyroid (Incorrect)\n" +
                        "While the thyroid regulates metabolism, it is controlled by the pituitary gland and is not considered the \"master gland.\"\n\n" +
                        "Adrenal (Incorrect)\n" +
                        "The adrenal glands are important for stress responses but are regulated by the pituitary gland.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "The pancreas controls blood sugar levels but does not regulate other glands."
        );

        questions.add("Which hormone is produced by the pancreas to lower blood glucose levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin", // Correct answer
                "Glucagon",
                "Cortisol",
                "Thyroxine"
        )));
        correctAnswers.add("Insulin");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Insulin (Correct answer)\n" +
                        "Insulin is produced by the beta cells of the pancreas and lowers blood glucose by promoting glucose uptake by cells.\n\n" +
                        "Glucagon (Incorrect)\n" +
                        "Glucagon increases blood glucose levels by promoting glycogen breakdown in the liver.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol is a stress hormone from the adrenal cortex and can raise blood sugar levels.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Thyroxine (T4) is a thyroid hormone that regulates metabolism, not blood glucose."
        );

        questions.add("What is the primary function of the thyroid gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate metabolism", // Correct answer
                "Regulate growth",
                "Regulate immune function",
                "Regulate water balance"
        )));
        correctAnswers.add("Regulate metabolism");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Regulate metabolism (Correct answer)\n" +
                        "The thyroid gland produces T3 and T4 hormones that play a major role in controlling the body's metabolic rate.\n\n" +
                        "Regulate growth (Incorrect)\n" +
                        "Growth is primarily regulated by growth hormone from the pituitary gland.\n\n" +
                        "Regulate immune function (Incorrect)\n" +
                        "Immune function is influenced more by the thymus and other immune system components.\n\n" +
                        "Regulate water balance (Incorrect)\n" +
                        "Water balance is regulated by ADH (antidiuretic hormone), secreted by the posterior pituitary."
        );

        questions.add("Which hormone is secreted by the adrenal glands and is responsible for the \"fight or flight\" response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epinephrine", // Correct answer
                "Insulin",
                "Estrogen",
                "Progesterone"
        )));
        correctAnswers.add("Epinephrine");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Epinephrine (Correct answer)\n" +
                        "Epinephrine (adrenaline) is secreted by the adrenal medulla during stress and triggers the \"fight or flight\" response.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is secreted by the pancreas and regulates blood sugar, not involved in \"fight or flight.\"\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen is a sex hormone produced by the ovaries and not involved in stress response.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone is another sex hormone involved in reproductive functions, not stress responses."
        );

        questions.add("The gonads are responsible for producing which of the following?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sex hormones", // Correct answer
                "Insulin",
                "Thyroid hormones",
                "Growth hormone"
        )));
        correctAnswers.add("Sex hormones");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Sex hormones (Correct answer)\n" +
                        "Gonads (testes and ovaries) produce sex hormones like testosterone, estrogen, and progesterone.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is produced by the pancreas.\n\n" +
                        "Thyroid hormones (Incorrect)\n" +
                        "These are produced by the thyroid gland, not the gonads.\n\n" +
                        "Growth hormone (Incorrect)\n" +
                        "Growth hormone is secreted by the anterior pituitary gland."
        );

        questions.add("Which gland regulates the body's metabolism through the release of T3 and T4 hormones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid gland", // Correct answer
                "Pineal gland",
                "Adrenal glands",
                "Pituitary gland"
        )));
        correctAnswers.add("Thyroid gland");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Thyroid gland (Correct answer)\n" +
                        "The thyroid gland releases triiodothyronine (T3) and thyroxine (T4), both of which regulate metabolism.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "The pineal gland secretes melatonin and regulates circadian rhythms, not metabolism.\n\n" +
                        "Adrenal glands (Incorrect)\n" +
                        "These produce stress hormones (cortisol, epinephrine) but do not regulate metabolism via T3 and T4.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary regulates the thyroid by secreting TSH, but does not produce T3 or T4."
        );

        questions.add("Which hormone is produced by the pituitary gland and stimulates growth?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Growth hormone (GH)", // Correct answer
                "Prolactin",
                "Thyroid-stimulating hormone (TSH)",
                "Luteinizing hormone (LH)"
        )));
        correctAnswers.add("Growth hormone (GH)");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Growth hormone (GH) (Correct answer)\n" +
                        "GH promotes growth of bones and tissues and is secreted by the anterior pituitary.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Prolactin stimulates milk production, not growth.\n\n" +
                        "Thyroid-stimulating hormone (TSH) (Incorrect)\n" +
                        "TSH stimulates the thyroid to release T3 and T4, but does not directly promote body growth.\n\n" +
                        "Luteinizing hormone (LH) (Incorrect)\n" +
                        "LH is involved in regulating the reproductive system, not physical growth."
        );

        questions.add("The hormone melatonin is secreted by which gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland", // Correct answer
                "Thyroid gland",
                "Pituitary gland",
                "Adrenal gland"
        )));
        correctAnswers.add("Pineal gland");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Pineal gland (Correct answer)\n" +
                        "The pineal gland secretes melatonin, which regulates sleep-wake cycles.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "The thyroid secretes T3 and T4, not melatonin.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary produces various hormones, but not melatonin.\n\n" +
                        "Adrenal gland (Incorrect)\n" +
                        "The adrenal glands produce hormones like cortisol and epinephrine, not melatonin."
        );

        questions.add("Which of the following is a major symptom of hyperthyroidism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Weight loss", // Correct answer
                "Weight gain",
                "Slow heart rate",
                "Fatigue"
        )));
        correctAnswers.add("Weight loss");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Weight loss (Correct answer)\n" +
                        "Due to increased metabolism, weight loss is a hallmark sign of hyperthyroidism.\n\n" +
                        "Weight gain (Incorrect)\n" +
                        "This is a common symptom of hypothyroidism, not hyperthyroidism.\n\n" +
                        "Slow heart rate (Incorrect)\n" +
                        "A slow heart rate (bradycardia) is also associated with hypothyroidism.\n\n" +
                        "Fatigue (Incorrect)\n" +
                        "While fatigue can occur, it is more characteristic of hypothyroidism."
        );

        questions.add("What is the main function of cortisol?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Help with stress response", // Correct answer
                "Regulate metabolism",
                "Control blood sugar levels",
                "Regulate calcium levels"
        )));
        correctAnswers.add("Help with stress response");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Help with stress response (Correct answer)\n" +
                        "Cortisol is a major stress hormone that helps the body respond to physical or emotional stress.\n\n" +
                        "Regulate metabolism (Incorrect)\n" +
                        "While cortisol affects metabolism, its primary role is stress response.\n\n" +
                        "Control blood sugar levels (Incorrect)\n" +
                        "Cortisol has an indirect effect on glucose metabolism but it’s not its main function.\n\n" +
                        "Regulate calcium levels (Incorrect)\n" +
                        "Calcium regulation is primarily controlled by parathyroid hormone and calcitonin."
        );

        questions.add("The adrenal glands are located above which organ?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Liver",
                "Kidneys", // Correct answer
                "Heart",
                "Stomach"
        )));
        correctAnswers.add("Kidneys");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Kidneys (Correct answer)\n" +
                        "The adrenal glands sit on top of each kidney and play a role in stress response and metabolism.\n\n" +
                        "Liver (Incorrect)\n" +
                        "The liver is located in the upper right quadrant of the abdomen and is not directly related to the adrenal glands.\n\n" +
                        "Heart (Incorrect)\n" +
                        "The heart is in the thoracic cavity and not related to adrenal gland location.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "The stomach is in the upper abdominal cavity, but adrenal glands are specifically above the kidneys."
        );

        questions.add("Which hormone stimulates the release of thyroid hormones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid-stimulating hormone (TSH)", // Correct answer
                "Adrenocorticotropic hormone (ACTH)",
                "Luteinizing hormone (LH)",
                "Follicle-stimulating hormone (FSH)"
        )));
        correctAnswers.add("Thyroid-stimulating hormone (TSH)");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Thyroid-stimulating hormone (TSH) (Correct answer)\n" +
                        "TSH is secreted by the anterior pituitary gland and stimulates the thyroid to produce T3 and T4.\n\n" +
                        "Adrenocorticotropic hormone (ACTH) (Incorrect)\n" +
                        "ACTH stimulates the adrenal cortex, not the thyroid.\n\n" +
                        "Luteinizing hormone (LH) (Incorrect)\n" +
                        "LH is involved in the reproductive system, not thyroid function.\n\n" +
                        "Follicle-stimulating hormone (FSH) (Incorrect)\n" +
                        "FSH regulates reproductive processes, not thyroid hormone release."
        );

        questions.add("Which of the following is produced by the ovaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Testosterone",
                "Progesterone", // Correct answer
                "Cortisol",
                "Insulin"
        )));
        correctAnswers.add("Progesterone");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Progesterone (Correct answer)\n" +
                        "Progesterone is produced by the ovaries and plays a key role in regulating the menstrual cycle and pregnancy.\n\n" +
                        "Testosterone (Incorrect)\n" +
                        "Testosterone is mainly produced in the testes, although small amounts are made in the ovaries.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol is produced by the adrenal cortex.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is produced by the pancreas, not the ovaries."
        );

        questions.add("What hormone is responsible for regulating calcium levels in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Parathyroid hormone (PTH)", // Correct answer
                "Thyroxine",
                "Cortisol"
        )));
        correctAnswers.add("Parathyroid hormone (PTH)");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Parathyroid hormone (PTH) (Correct answer)\n" +
                        "PTH increases blood calcium levels by stimulating bone resorption and increasing calcium reabsorption in kidneys.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin regulates blood glucose, not calcium.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Thyroxine (T4) regulates metabolism, not calcium.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol is a stress hormone, not involved in calcium homeostasis."
        );

        questions.add("The pancreas is located behind which organ?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stomach", // Correct answer
                "Liver",
                "Kidneys",
                "Lungs"
        )));
        correctAnswers.add("Stomach");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Stomach (Correct answer)\n" +
                        "The pancreas lies retroperitoneally, just behind the stomach.\n\n" +
                        "Liver (Incorrect)\n" +
                        "The liver is located above and to the right of the pancreas, not directly in front.\n\n" +
                        "Kidneys (Incorrect)\n" +
                        "The kidneys are posterior to the pancreas but not directly in front of it.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "The lungs are in the thoracic cavity, while the pancreas is in the abdominal cavity."
        );

        questions.add("Which condition is associated with the overproduction of growth hormone in adults?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diabetes",
                "Acromegaly", // Correct answer
                "Cushing's disease",
                "Hypothyroidism"
        )));
        correctAnswers.add("Acromegaly");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Acromegaly (Correct answer)\n" +
                        "Acromegaly results from excessive GH in adults, leading to enlarged hands, feet, and facial features.\n\n" +
                        "Diabetes (Incorrect)\n" +
                        "While growth hormone can affect glucose metabolism, diabetes is not directly caused by GH overproduction.\n\n" +
                        "Cushing's disease (Incorrect)\n" +
                        "Cushing’s disease is caused by excess ACTH or cortisol, not GH.\n\n" +
                        "Hypothyroidism (Incorrect)\n" +
                        "Hypothyroidism is caused by low thyroid hormone levels, not GH abnormalities."
        );

        questions.add("Which of the following is a function of the parathyroid glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Control metabolism",
                "Regulate blood calcium levels", // Correct answer
                "Regulate the menstrual cycle",
                "Control growth"
        )));
        correctAnswers.add("Regulate blood calcium levels");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Regulate blood calcium levels (Correct answer)\n" +
                        "The parathyroid glands secrete PTH, which increases blood calcium levels.\n\n" +
                        "Control metabolism (Incorrect)\n" +
                        "Metabolism is mainly regulated by the thyroid gland.\n\n" +
                        "Regulate the menstrual cycle (Incorrect)\n" +
                        "The menstrual cycle is regulated by sex hormones like estrogen and progesterone.\n\n" +
                        "Control growth (Incorrect)\n" +
                        "Growth is regulated by growth hormone from the pituitary gland."
        );

        questions.add("What is the main effect of insulin on the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increases blood glucose levels",
                "Decreases blood glucose levels", // Correct answer
                "Stimulates the production of red blood cells",
                "Increases metabolism"
        )));
        correctAnswers.add("Decreases blood glucose levels");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Decreases blood glucose levels (Correct answer)\n" +
                        "Insulin facilitates the uptake of glucose by cells, thereby lowering blood sugar levels.\n\n" +
                        "Increases blood glucose levels (Incorrect)\n" +
                        "Glucagon and cortisol can raise blood glucose, not insulin.\n\n" +
                        "Stimulates the production of red blood cells (Incorrect)\n" +
                        "Erythropoietin, produced by the kidneys, stimulates RBC production.\n\n" +
                        "Increases metabolism (Incorrect)\n" +
                        "While insulin has metabolic effects, it does not significantly increase the metabolic rate like thyroid hormones do."
        );

        questions.add("What is the name of the disorder caused by the underproduction of thyroid hormones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyperthyroidism",
                "Hypothyroidism", // Correct answer
                "Cushing’s disease",
                "Addison’s disease"
        )));
        correctAnswers.add("Hypothyroidism");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Hypothyroidism (Correct answer)\n" +
                        "Hypothyroidism results from low levels of T3 and T4, causing fatigue, weight gain, and cold intolerance.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "Hyperthyroidism is caused by overproduction of thyroid hormones.\n\n" +
                        "Cushing’s disease (Incorrect)\n" +
                        "Cushing’s disease involves high cortisol, not thyroid hormone issues.\n\n" +
                        "Addison’s disease (Incorrect)\n" +
                        "Addison’s is caused by adrenal insufficiency, not thyroid hormone deficiency."
        );

        questions.add("Which hormone is responsible for the development of secondary sexual characteristics in males?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Progesterone",
                "Testosterone", // Correct answer
                "Oxytocin"
        )));
        correctAnswers.add("Testosterone");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Testosterone (Correct answer)\n" +
                        "Testosterone promotes the development of male secondary sexual characteristics like facial hair and deeper voice.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen is mainly responsible for female secondary sexual characteristics.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone helps regulate the menstrual cycle and supports pregnancy, not male traits.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Oxytocin is involved in childbirth and lactation, not male sexual development."
        );

        questions.add("What is the primary symptom of Addison's disease?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased blood pressure",
                "Weight loss and fatigue", // Correct answer
                "Excessive thirst",
                "Increased metabolism"
        )));
        correctAnswers.add("Weight loss and fatigue");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Weight loss and fatigue (Correct answer)\n" +
                        "Addison’s disease, a condition of adrenal insufficiency, leads to symptoms like chronic fatigue, weight loss, and muscle weakness.\n\n" +
                        "Increased blood pressure (Incorrect)\n" +
                        "Addison’s disease typically causes low blood pressure, not increased.\n\n" +
                        "Excessive thirst (Incorrect)\n" +
                        "Excessive thirst is more commonly associated with diabetes mellitus or diabetes insipidus.\n\n" +
                        "Increased metabolism (Incorrect)\n" +
                        "Addison’s disease slows the metabolism due to cortisol deficiency."
        );

        questions.add("Which gland produces aldosterone, a hormone that regulates sodium and potassium levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Adrenal glands", // Correct answer
                "Thyroid gland",
                "Pineal gland",
                "Pituitary gland"
        )));
        correctAnswers.add("Adrenal glands");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Adrenal glands (Correct answer)\n" +
                        "The adrenal cortex produces aldosterone, which helps control blood pressure by balancing sodium and potassium.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "The thyroid gland regulates metabolism with T3 and T4, not electrolytes.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "The pineal gland secretes melatonin, which regulates circadian rhythms.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary regulates many hormones but does not produce aldosterone directly."
        );

        questions.add("Which of the following is associated with Cushing’s syndrome?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Weight loss",
                "Moon face and buffalo hump", // Correct answer
                "Hypoglycemia",
                "Increased energy levels"
        )));
        correctAnswers.add("Moon face and buffalo hump");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Moon face and buffalo hump (Correct answer)\n" +
                        "Cushing’s syndrome results from excessive cortisol and causes characteristic fat deposits in the face and upper back.\n\n" +
                        "Weight loss (Incorrect)\n" +
                        "Cushing’s is linked to weight gain, especially in the abdomen, face, and upper back.\n\n" +
                        "Hypoglycemia (Incorrect)\n" +
                        "It’s associated with hyperglycemia, not low blood sugar.\n\n" +
                        "Increased energy levels (Incorrect)\n" +
                        "Most patients experience muscle weakness and fatigue, not increased energy."
        );

        questions.add("What is the effect of progesterone during pregnancy?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulates milk production",
                "Maintains the uterine lining", // Correct answer
                "Increases metabolic rate",
                "Regulates blood pressure"
        )));
        correctAnswers.add("Maintains the uterine lining");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Maintains the uterine lining (Correct answer)\n" +
                        "Progesterone maintains the endometrium to support a fertilized egg during pregnancy.\n\n" +
                        "Stimulates milk production (Incorrect)\n" +
                        "Prolactin stimulates milk production, not progesterone.\n\n" +
                        "Increases metabolic rate (Incorrect)\n" +
                        "Thyroid hormones primarily influence metabolic rate.\n\n" +
                        "Regulates blood pressure (Incorrect)\n" +
                        "Aldosterone and ADH are more involved in blood pressure regulation."
        );

        questions.add("Which of the following is a sign of hypothyroidism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sweating and palpitations",
                "Weight gain and cold intolerance", // Correct answer
                "Tremors and irritability",
                "Heat intolerance"
        )));
        correctAnswers.add("Weight gain and cold intolerance");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Weight gain and cold intolerance (Correct answer)\n" +
                        "Hypothyroidism slows metabolism, leading to weight gain and sensitivity to cold.\n\n" +
                        "Sweating and palpitations (Incorrect)\n" +
                        "These are signs of hyperthyroidism, not hypothyroidism.\n\n" +
                        "Tremors and irritability (Incorrect)\n" +
                        "These are symptoms of hyperthyroidism due to increased metabolic activity.\n\n" +
                        "Heat intolerance (Incorrect)\n" +
                        "Heat intolerance is also typical of hyperthyroidism."
        );

        questions.add("What is the main function of oxytocin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulates metabolism",
                "Stimulates uterine contractions during labor", // Correct answer
                "Increases heart rate",
                "Stimulates milk production"
        )));
        correctAnswers.add("Stimulates uterine contractions during labor");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Stimulates uterine contractions during labor (Correct answer)\n" +
                        "Oxytocin causes uterine muscle contractions during labor and facilitates bonding.\n\n" +
                        "Regulates metabolism (Incorrect)\n" +
                        "Thyroid hormones regulate metabolism.\n\n" +
                        "Increases heart rate (Incorrect)\n" +
                        "Adrenaline and sympathetic stimulation increase heart rate, not oxytocin.\n\n" +
                        "Stimulates milk production (Incorrect)\n" +
                        "Prolactin stimulates milk production; oxytocin aids milk ejection."
        );

        questions.add("Which of the following glands secretes adrenaline?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid gland",
                "Adrenal glands", // Correct answer
                "Pineal gland",
                "Pituitary gland"
        )));
        correctAnswers.add("Adrenal glands");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Adrenal glands (Correct answer)\n" +
                        "The adrenal medulla secretes adrenaline (epinephrine), responsible for the fight-or-flight response.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "The thyroid secretes T3 and T4, not adrenaline.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "The pineal gland secretes melatonin.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary gland produces many hormones but not adrenaline."
        );

        questions.add("Which of the following hormones is responsible for increasing blood glucose levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Glucagon", // Correct answer
                "Thyroxine",
                "Parathyroid hormone"
        )));
        correctAnswers.add("Glucagon");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Glucagon (Correct answer)\n" +
                        "Glucagon, secreted by alpha cells in the pancreas, increases blood glucose by promoting glycogen breakdown in the liver.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin decreases blood glucose by promoting cellular uptake.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Thyroxine increases metabolism but has a minor effect on glucose levels.\n\n" +
                        "Parathyroid hormone (Incorrect)\n" +
                        "PTH regulates calcium, not glucose."
        );

        questions.add("Which of the following hormones is involved in regulating sleep-wake cycles?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cortisol",
                "Melatonin", // Correct answer
                "Growth hormone",
                "Estrogen"
        )));
        correctAnswers.add("Melatonin");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Melatonin (Correct answer)\n" +
                        "Melatonin, secreted by the pineal gland, promotes sleep and helps regulate circadian rhythms.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol has a diurnal rhythm but is not directly responsible for inducing sleep.\n\n" +
                        "Growth hormone (Incorrect)\n" +
                        "Growth hormone is secreted during deep sleep but does not regulate sleep cycles.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen is related to reproductive function, not sleep."
        );

        questions.add("Which of the following is NOT a function of the thyroid gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate metabolism",
                "Regulate calcium levels",
                "Stimulate growth",
                "Stimulate milk production" // Correct answer
        )));
        correctAnswers.add("Stimulate milk production");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Stimulate milk production (Correct answer)\n" +
                        "This is the correct answer because milk production is stimulated by prolactin, not the thyroid gland.\n\n" +
                        "Regulate metabolism (Incorrect)\n" +
                        "The thyroid gland plays a major role in regulating the body’s metabolic rate.\n\n" +
                        "Regulate calcium levels (Incorrect)\n" +
                        "The thyroid gland secretes calcitonin, which lowers blood calcium levels.\n\n" +
                        "Stimulate growth (Incorrect)\n" +
                        "Thyroid hormones are essential for normal growth and development, especially in children."
        );

        questions.add("Which of the following is NOT a symptom of hyperthyroidism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Weight loss",
                "Increased heart rate",
                "Increased appetite",
                "Cold intolerance" // Correct answer
        )));
        correctAnswers.add("Cold intolerance");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Cold intolerance (Correct answer)\n" +
                        "Cold intolerance is a hallmark symptom of hypothyroidism, not hyperthyroidism.\n\n" +
                        "Weight loss (Incorrect)\n" +
                        "Typical symptom. Increased metabolism causes unintentional weight loss.\n\n" +
                        "Increased heart rate (Incorrect)\n" +
                        "Typical symptom. Hyperthyroidism stimulates the cardiovascular system.\n\n" +
                        "Increased appetite (Incorrect)\n" +
                        "Typical symptom. Despite eating more, patients often lose weight due to high metabolism."
        );

        questions.add("What is the purpose of the hormone prolactin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulate uterine contractions",
                "Regulate blood glucose",
                "Stimulate milk production", // Correct answer
                "Stimulate thyroid hormone production"
        )));
        correctAnswers.add("Stimulate milk production");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Stimulate milk production (Correct answer)\n" +
                        "Prolactin, secreted by the anterior pituitary, triggers milk synthesis.\n\n" +
                        "Stimulate uterine contractions (Incorrect)\n" +
                        "That’s the function of oxytocin, not prolactin.\n\n" +
                        "Regulate blood glucose (Incorrect)\n" +
                        "Insulin and glucagon manage glucose levels.\n\n" +
                        "Stimulate thyroid hormone production (Incorrect)\n" +
                        "TSH (thyroid-stimulating hormone) handles this function."
        );

        questions.add("Which disorder is characterized by the overproduction of cortisol?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Addison's disease",
                "Cushing's syndrome", // Correct answer
                "Hyperthyroidism",
                "Acromegaly"
        )));
        correctAnswers.add("Cushing's syndrome");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Cushing's syndrome (Correct answer)\n" +
                        "Cushing’s involves excess cortisol, leading to symptoms like central obesity and moon face.\n\n" +
                        "Addison's disease (Incorrect)\n" +
                        "Addison’s is due to low cortisol levels.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "This is related to excess thyroid hormone, not cortisol.\n\n" +
                        "Acromegaly (Incorrect)\n" +
                        "Acromegaly results from excess growth hormone, not cortisol."
        );

        questions.add("Which hormone is primarily responsible for regulating the body’s sleep-wake cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Melatonin", // Correct answer
                "Cortisol",
                "Growth hormone"
        )));
        correctAnswers.add("Melatonin");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Melatonin (Correct answer)\n" +
                        "Secreted by the pineal gland, melatonin regulates circadian rhythms.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood glucose, not sleep.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol has a daily rhythm but does not induce sleep.\n\n" +
                        "Growth hormone (Incorrect)\n" +
                        "Secreted during sleep, but it does not regulate the sleep cycle."
        );

        questions.add("What hormone does the pancreas secrete to prevent hypoglycemia?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glucagon", // Correct answer
                "Insulin",
                "Cortisol",
                "Thyroxine"
        )));
        correctAnswers.add("Glucagon");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Glucagon (Correct answer)\n" +
                        "Glucagon increases blood sugar by promoting glycogen breakdown in the liver.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin lowers blood glucose—can actually cause hypoglycemia.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Helps with long-term glucose balance but not the primary response.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Thyroid hormone influences metabolism, not immediate glucose control."
        );

        questions.add("The function of aldosterone is to regulate:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glucose levels",
                "Blood pressure and sodium balance", // Correct answer
                "Growth and development",
                "Reproductive functions"
        )));
        correctAnswers.add("Blood pressure and sodium balance");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Blood pressure and sodium balance (Correct answer)\n" +
                        "Aldosterone promotes sodium retention and potassium excretion, affecting fluid balance and blood pressure.\n\n" +
                        "Glucose levels (Incorrect)\n" +
                        "Insulin and glucagon are responsible for this.\n\n" +
                        "Growth and development (Incorrect)\n" +
                        "Growth hormone handles this role.\n\n" +
                        "Reproductive functions (Incorrect)\n" +
                        "Estrogen and testosterone regulate reproductive functions."
        );

        questions.add("Which hormone stimulates milk production in the mammary glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prolactin", // Correct answer
                "Oxytocin",
                "Estrogen",
                "Progesterone"
        )));
        correctAnswers.add("Prolactin");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Prolactin (Correct answer)\n" +
                        "Directly stimulates milk production in mammary glands.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Oxytocin causes milk ejection, not production.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Prepares breasts for lactation but does not stimulate milk production.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Maintains pregnancy and inhibits lactation until after birth."
        );

        questions.add("Which of the following is a cause of hypothyroidism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Iodine deficiency", // Correct answer
                "Graves’ disease",
                "Tumors in the pituitary gland",
                "Excessive iodine intake"
        )));
        correctAnswers.add("Iodine deficiency");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Iodine deficiency (Correct answer)\n" +
                        "Iodine is essential for thyroid hormone production. A deficiency leads to hypothyroidism and goiter.\n\n" +
                        "Graves’ disease (Incorrect)\n" +
                        "This causes hyperthyroidism, not hypo.\n\n" +
                        "Tumors in the pituitary gland (Incorrect)\n" +
                        "May affect hormone levels but not the most common cause.\n\n" +
                        "Excessive iodine intake (Incorrect)\n" +
                        "Rarely, it can cause thyroid dysfunction but not commonly hypothyroidism."
        );

        questions.add("Which of the following hormones is associated with the body's response to stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Glucagon",
                "Epinephrine", // Correct answer
                "Thyroxine"
        )));
        correctAnswers.add("Epinephrine");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Epinephrine (Correct answer)\n" +
                        "Released by the adrenal medulla, it triggers the \"fight or flight\" response during acute stress.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates glucose, not part of the acute stress response.\n\n" +
                        "Glucagon (Incorrect)\n" +
                        "Helps raise blood sugar but not a key stress hormone.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Regulates metabolism, not directly tied to stress responses."
        );

        questions.add("What is the primary function of the hormone estrogen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulate male reproductive functions",
                "Regulate menstrual cycles and female secondary sex characteristics", // Correct answer
                "Regulate metabolism",
                "Increase blood glucose"
        )));
        correctAnswers.add("Regulate menstrual cycles and female secondary sex characteristics");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Regulate menstrual cycles and female secondary sex characteristics (Correct answer)\n" +
                        "Estrogen, secreted by the ovaries, plays a major role in the development of female reproductive features and menstrual regulation.\n\n" +
                        "Stimulate male reproductive functions (Incorrect)\n" +
                        "This is the role of testosterone.\n\n" +
                        "Regulate metabolism (Incorrect)\n" +
                        "Thyroid hormones regulate metabolism.\n\n" +
                        "Increase blood glucose (Incorrect)\n" +
                        "Glucagon increases blood sugar, not estrogen."
        );

        questions.add("What gland is responsible for secreting adrenaline (epinephrine) and norepinephrine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid gland",
                "Pituitary gland",
                "Adrenal glands", // Correct answer
                "Pancreas"
        )));
        correctAnswers.add("Adrenal glands");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Adrenal glands (Correct answer)\n" +
                        "Specifically, the adrenal medulla secretes adrenaline (epinephrine) and norepinephrine for the fight-or-flight response.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "Secretes thyroxine (T4) and triiodothyronine (T3), not adrenaline.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Secretes regulatory hormones like TSH, ACTH, not adrenaline.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "Secretes insulin and glucagon, not adrenaline."
        );

        questions.add("What is the name of the disorder caused by an excessive production of growth hormone in children?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cushing’s disease",
                "Gigantism", // Correct answer
                "Acromegaly",
                "Hyperthyroidism"
        )));
        correctAnswers.add("Gigantism");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Gigantism (Correct answer)\n" +
                        "Occurs when there is excess growth hormone before the epiphyseal plates close in children.\n\n" +
                        "Cushing’s disease (Incorrect)\n" +
                        "This is due to excess cortisol, not growth hormone.\n\n" +
                        "Acromegaly (Incorrect)\n" +
                        "Same cause (excess GH) but occurs after bone growth plates close, affecting adults.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "Due to excessive thyroid hormones, not growth hormone."
        );

        questions.add("Which of the following glands does NOT produce a hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland",
                "Parathyroid gland",
                "Thymus",
                "Sweat gland" // Correct answer
        )));
        correctAnswers.add("Sweat gland");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Sweat gland (Correct answer)\n" +
                        "Produces sweat, but it is not a hormone-producing gland.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "Produces melatonin.\n\n" +
                        "Parathyroid gland (Incorrect)\n" +
                        "Produces parathyroid hormone (PTH).\n\n" +
                        "Thymus (Incorrect)\n" +
                        "Secretes thymosin, important for immune function."
        );

        questions.add("Which of the following is the main function of thyroid hormones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Control calcium balance",
                "Stimulate adrenaline production",
                "Regulate metabolic rate", // Correct answer
                "Control glucose metabolism"
        )));
        correctAnswers.add("Regulate metabolic rate");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Regulate metabolic rate (Correct answer)\n" +
                        "Thyroid hormones like T3 and T4 regulate the body's metabolic rate.\n\n" +
                        "Control calcium balance (Incorrect)\n" +
                        "Done by parathyroid hormone (PTH).\n\n" +
                        "Stimulate adrenaline production (Incorrect)\n" +
                        "Done by the adrenal medulla.\n\n" +
                        "Control glucose metabolism (Incorrect)\n" +
                        "Handled by insulin and glucagon."
        );

        questions.add("The hormone insulin is produced in which part of the pancreas?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Alpha cells",
                "Beta cells", // Correct answer
                "Delta cells",
                "Acini cells"
        )));
        correctAnswers.add("Beta cells");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Beta cells (Correct answer)\n" +
                        "Located in the islets of Langerhans, they secrete insulin.\n\n" +
                        "Alpha cells (Incorrect)\n" +
                        "Produce glucagon, not insulin.\n\n" +
                        "Delta cells (Incorrect)\n" +
                        "Produce somatostatin.\n\n" +
                        "Acini cells (Incorrect)\n" +
                        "Involved in exocrine function, producing digestive enzymes."
        );

        questions.add("What is the result of an excess of insulin in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyperglycemia",
                "Hypoglycemia", // Correct answer
                "Increased metabolism",
                "Increased growth"
        )));
        correctAnswers.add("Hypoglycemia");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Hypoglycemia (Correct answer)\n" +
                        "Excess insulin leads to low blood glucose levels.\n\n" +
                        "Hyperglycemia (Incorrect)\n" +
                        "Caused by lack of insulin.\n\n" +
                        "Increased metabolism (Incorrect)\n" +
                        "Not directly related to insulin levels.\n\n" +
                        "Increased growth (Incorrect)\n" +
                        "Governed by growth hormone, not insulin."
        );

        questions.add("The parathyroid glands are located behind which gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pituitary gland",
                "Thyroid gland", // Correct answer
                "Adrenal gland",
                "Pineal gland"
        )));
        correctAnswers.add("Thyroid gland");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Thyroid gland (Correct answer)\n" +
                        "Parathyroids are embedded behind the thyroid gland.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Located in the brain.\n\n" +
                        "Adrenal gland (Incorrect)\n" +
                        "Sits atop the kidneys, unrelated in position.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "Located in the brain, not related."
        );

        questions.add("Which hormone is involved in the regulation of blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Aldosterone", // Correct answer
                "Estrogen",
                "Thyroxine"
        )));
        correctAnswers.add("Aldosterone");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Aldosterone (Correct answer)\n" +
                        "Promotes sodium and water retention, affecting blood pressure.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates glucose.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Regulates reproductive functions, not BP directly.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Regulates metabolism, not blood pressure directly."
        );

        questions.add("Which of the following hormones is released by the hypothalamus to stimulate the anterior pituitary?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cortisol",
                "Gonadotropin-releasing hormone (GnRH)", // Correct answer
                "Insulin",
                "Melatonin"
        )));
        correctAnswers.add("Gonadotropin-releasing hormone (GnRH)");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Gonadotropin-releasing hormone (GnRH) (Correct answer)\n" +
                        "Stimulates the anterior pituitary to release LH and FSH.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Secreted by the adrenal cortex, not hypothalamus.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Secreted by the pancreas.\n\n" +
                        "Melatonin (Incorrect)\n" +
                        "Secreted by the pineal gland, regulates circadian rhythm."
        );

        questions.add("Which of the following is NOT a function of the endocrine system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate metabolism",
                "Regulate mood and emotions",
                "Control body movement", // Correct answer
                "Maintain homeostasis"
        )));
        correctAnswers.add("Control body movement");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Control body movement (Correct answer)\n" +
                        "Movement is controlled by the nervous system and muscles, not the endocrine system.\n\n" +
                        "Regulate metabolism (Incorrect)\n" +
                        "Endocrine function. Done by thyroid hormones.\n\n" +
                        "Regulate mood and emotions (Incorrect)\n" +
                        "Endocrine function. Affected by hormones like cortisol and serotonin.\n\n" +
                        "Maintain homeostasis (Incorrect)\n" +
                        "Endocrine function. Hormones help regulate blood glucose, water balance, etc."
        );

        questions.add("Which hormone helps regulate the circadian rhythm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid hormone",
                "Melatonin",
                "Insulin",
                "Adrenaline"
        )));
        correctAnswers.add("Melatonin");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Melatonin (Correct answer)\n" +
                        "Secreted by the pineal gland, melatonin helps regulate sleep-wake cycles.\n\n" +
                        "Thyroid hormone (Incorrect)\n" +
                        "Regulates metabolism, not sleep cycles.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood sugar.\n\n" +
                        "Adrenaline (Incorrect)\n" +
                        "Involved in the fight-or-flight response, not circadian rhythm."
        );

        questions.add("The function of the thymus gland is primarily to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate the immune system",
                "Produce insulin",
                "Stimulate growth",
                "Control metabolism"
        )));
        correctAnswers.add("Regulate the immune system");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Regulate the immune system (Correct answer)\n" +
                        "The thymus produces thymosin, essential for T-cell maturation in the immune system.\n\n" +
                        "Produce insulin (Incorrect)\n" +
                        "That’s the function of the pancreas.\n\n" +
                        "Stimulate growth (Incorrect)\n" +
                        "Done by growth hormone.\n\n" +
                        "Control metabolism (Incorrect)\n" +
                        "Regulated by the thyroid gland."
        );

        questions.add("Which of the following is a function of the hormone estrogen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulates the development of male secondary sexual characteristics",
                "Helps maintain pregnancy",
                "Stimulates milk production",
                "Stimulates the development of female secondary sexual characteristics"
        )));
        correctAnswers.add("Stimulates the development of female secondary sexual characteristics");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Stimulates the development of female secondary sexual characteristics (Correct answer)\n" +
                        "Estrogen promotes breast development, fat distribution, and menstrual cycle regulation.\n\n" +
                        "Stimulates the development of male secondary sexual characteristics (Incorrect)\n" +
                        "This is the function of testosterone.\n\n" +
                        "Helps maintain pregnancy (Incorrect)\n" +
                        "That’s the role of progesterone.\n\n" +
                        "Stimulates milk production (Incorrect)\n" +
                        "Done by prolactin."
        );

        questions.add("The primary function of the hormone progesterone is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulate milk production",
                "Regulate blood sugar",
                "Prepare the uterus for pregnancy",
                "Stimulate the adrenal glands"
        )));
        correctAnswers.add("Prepare the uterus for pregnancy");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Prepare the uterus for pregnancy (Correct answer)\n" +
                        "Progesterone thickens the endometrial lining, making it suitable for implantation.\n\n" +
                        "Stimulate milk production (Incorrect)\n" +
                        "Done by prolactin.\n\n" +
                        "Regulate blood sugar (Incorrect)\n" +
                        "That’s the job of insulin and glucagon.\n\n" +
                        "Stimulate the adrenal glands (Incorrect)\n" +
                        "Not related to adrenal stimulation."
        );

        questions.add("Which of the following hormones is produced by the testes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Testosterone",
                "Progesterone",
                "Oxytocin"
        )));
        correctAnswers.add("Testosterone");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Testosterone (Correct answer)\n" +
                        "Secreted by Leydig cells in the testes; responsible for male traits and reproduction.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Produced in ovaries, and in small amounts in males.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Mainly produced in ovaries and placenta.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Produced in the hypothalamus and released by the posterior pituitary."
        );

        questions.add("What is the role of oxytocin in childbirth?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulates uterine contractions",
                "Increases heart rate",
                "Increases milk production",
                "Stimulates the release of adrenaline"
        )));
        correctAnswers.add("Stimulates uterine contractions");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Stimulates uterine contractions (Correct answer)\n" +
                        "Oxytocin causes the uterus to contract during labor and helps with postpartum uterine contraction.\n\n" +
                        "Increases heart rate (Incorrect)\n" +
                        "That’s a job for adrenaline.\n\n" +
                        "Increases milk production (Incorrect)\n" +
                        "Prolactin increases milk production; oxytocin assists with milk ejection.\n\n" +
                        "Stimulates the release of adrenaline (Incorrect)\n" +
                        "Not a function of oxytocin."
        );

        questions.add("The release of thyroid hormones is regulated by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid-stimulating hormone (TSH)",
                "Growth hormone (GH)",
                "Cortisol",
                "Insulin"
        )));
        correctAnswers.add("Thyroid-stimulating hormone (TSH)");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Thyroid-stimulating hormone (TSH) (Correct answer)\n" +
                        "TSH is released from the anterior pituitary and stimulates the thyroid gland to produce T3 and T4.\n\n" +
                        "Growth hormone (GH) (Incorrect)\n" +
                        "Stimulates body growth, not thyroid function.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "A glucocorticoid from adrenal cortex, unrelated to thyroid.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Manages blood sugar, not thyroid hormones."
        );

        questions.add("The primary effect of glucagon is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increase blood glucose levels",
                "Lower blood glucose levels",
                "Stimulate the production of red blood cells",
                "Increase metabolism"
        )));
        correctAnswers.add("Increase blood glucose levels");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Increase blood glucose levels (Correct answer)\n" +
                        "Glucagon stimulates the liver to convert glycogen to glucose, raising blood sugar.\n\n" +
                        "Lower blood glucose levels (Incorrect)\n" +
                        "Done by insulin.\n\n" +
                        "Stimulate the production of red blood cells (Incorrect)\n" +
                        "Done by erythropoietin, not glucagon.\n\n" +
                        "Increase metabolism (Incorrect)\n" +
                        "Primarily a function of thyroid hormones."
        );

        questions.add("The adrenal glands secrete which of the following hormones in response to stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Glucagon",
                "Adrenaline (epinephrine)",
                "Insulin"
        )));
        correctAnswers.add("Adrenaline (epinephrine)");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Adrenaline (epinephrine) (Correct answer)\n" +
                        "Released by the adrenal medulla during the fight-or-flight response.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Mainly produced in the ovaries.\n\n" +
                        "Glucagon (Incorrect)\n" +
                        "Secreted by the pancreas.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood glucose; not a stress hormone."
        );

        questions.add("What does the parathyroid hormone (PTH) do in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increases calcium levels in the blood",
                "Decreases calcium levels in the blood",
                "Stimulates insulin release",
                "Stimulates the production of red blood cells"
        )));
        correctAnswers.add("Increases calcium levels in the blood");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Increases calcium levels in the blood (Correct answer)\n" +
                        "PTH raises blood calcium by stimulating bone resorption and increasing calcium absorption in intestines and kidneys.\n\n" +
                        "Decreases calcium levels in the blood (Incorrect)\n" +
                        "That is done by calcitonin, not PTH.\n\n" +
                        "Stimulates insulin release (Incorrect)\n" +
                        "That’s the pancreas’ job.\n\n" +
                        "Stimulates the production of red blood cells (Incorrect)\n" +
                        "Done by erythropoietin from the kidneys."
        );

        questions.add("Which of the following is a symptom of hyperparathyroidism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Low calcium levels",
                "Weak bones", // Correct answer
                "Weight loss",
                "Decreased appetite"
        )));
        correctAnswers.add("Weak bones");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Weak bones (Correct answer)\n" +
                        "Excess PTH stimulates calcium release from bones, making them fragile and prone to fractures.\n\n" +
                        "Low calcium levels (Incorrect)\n" +
                        "Hyperparathyroidism causes high calcium levels.\n\n" +
                        "Weight loss (Incorrect)\n" +
                        "Not a primary symptom of hyperparathyroidism.\n\n" +
                        "Decreased appetite (Incorrect)\n" +
                        "Appetite is generally unaffected."
        );

        questions.add("Which gland secretes the hormone leptin, which helps regulate body weight?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid gland",
                "Pancreas",
                "Adipose tissue", // Correct answer
                "Pituitary gland"
        )));
        correctAnswers.add("Adipose tissue");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Adipose tissue (Correct answer)\n" +
                        "Fat cells release leptin, which helps regulate appetite and energy balance.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "It secretes thyroid hormones, not leptin.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "Produces insulin and glucagon.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Produces several hormones but not leptin."
        );

        questions.add("Which of the following is a function of the adrenal cortex?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secretes adrenaline",
                "Regulates metabolism",
                "Secretes corticosteroids", // Correct answer
                "Produces insulin"
        )));
        correctAnswers.add("Secretes corticosteroids");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Secretes corticosteroids (Correct answer)\n" +
                        "The adrenal cortex releases glucocorticoids (like cortisol) and mineralocorticoids (like aldosterone).\n\n" +
                        "Secretes adrenaline (Incorrect)\n" +
                        "That’s the adrenal medulla.\n\n" +
                        "Regulates metabolism (Incorrect)\n" +
                        "Partially correct, but not specific.\n\n" +
                        "Produces insulin (Incorrect)\n" +
                        "That’s the pancreas."
        );

        questions.add("Which of the following hormones is responsible for the regulation of the sleep-wake cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Serotonin",
                "Melatonin", // Correct answer
                "Insulin",
                "Epinephrine"
        )));
        correctAnswers.add("Melatonin");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Melatonin (Correct answer)\n" +
                        "Secreted by the pineal gland, it controls the circadian rhythm.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "It's a precursor to melatonin and involved in mood.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood sugar.\n\n" +
                        "Epinephrine (Incorrect)\n" +
                        "Involved in stress response, not sleep."
        );

        questions.add("Which of the following hormones increases water retention by the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Antidiuretic hormone (ADH)", // Correct answer
                "Cortisol",
                "Prolactin"
        )));
        correctAnswers.add("Antidiuretic hormone (ADH)");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Antidiuretic hormone (ADH) (Correct answer)\n" +
                        "Secreted by the posterior pituitary, ADH causes the kidneys to retain water, concentrating the urine.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood glucose.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Affects metabolism and stress but not water retention.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Stimulates milk production."
        );

        questions.add("Which condition is caused by an overproduction of cortisol?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cushing's syndrome", // Correct answer
                "Addison’s disease",
                "Hyperthyroidism",
                "Acromegaly"
        )));
        correctAnswers.add("Cushing's syndrome");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Cushing's syndrome (Correct answer)\n" +
                        "Caused by excess cortisol, leading to weight gain, moon face, and purple striae.\n\n" +
                        "Addison’s disease (Incorrect)\n" +
                        "This results from low cortisol.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "Involves excess thyroid hormones, not cortisol.\n\n" +
                        "Acromegaly (Incorrect)\n" +
                        "Caused by excess growth hormone in adults."
        );

        questions.add("Which gland produces a hormone that is involved in regulating calcium metabolism?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Adrenal glands",
                "Parathyroid glands", // Correct answer
                "Pancreas",
                "Pituitary gland"
        )));
        correctAnswers.add("Parathyroid glands");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Parathyroid glands (Correct answer)\n" +
                        "Produce parathyroid hormone (PTH), which increases blood calcium levels.\n\n" +
                        "Adrenal glands (Incorrect)\n" +
                        "Secrete cortisol, aldosterone, and adrenaline.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "Manages blood glucose, not calcium.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Controls many hormones but not directly involved in calcium metabolism."
        );

        questions.add("Which of the following is a function of the hormone aldosterone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decreases blood pressure",
                "Increases sodium retention", // Correct answer
                "Stimulates milk production",
                "Regulates blood sugar"
        )));
        correctAnswers.add("Increases sodium retention");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Increases sodium retention (Correct answer)\n" +
                        "It acts on the kidneys to retain sodium and water, increasing blood volume and pressure.\n\n" +
                        "Decreases blood pressure (Incorrect)\n" +
                        "Aldosterone actually increases blood pressure by retaining sodium.\n\n" +
                        "Stimulates milk production (Incorrect)\n" +
                        "That’s prolactin’s job.\n\n" +
                        "Regulates blood sugar (Incorrect)\n" +
                        "Managed by insulin and glucagon."
        );

        questions.add("Which hormone is responsible for regulating the body's response to stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cortisol", // Correct answer
                "Melatonin",
                "Estrogen",
                "Thyroxine"
        )));
        correctAnswers.add("Cortisol");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Cortisol (Correct answer)\n" +
                        "Released from the adrenal cortex, cortisol helps the body manage stress by increasing glucose and suppressing inflammation.\n\n" +
                        "Melatonin (Incorrect)\n" +
                        "Regulates sleep, not stress.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Regulates the female reproductive system.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Controls metabolism."
        );

        questions.add("Which of the following is a common symptom of diabetes mellitus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyperglycemia", // Correct answer
                "Increased calcium levels",
                "Increased energy levels",
                "Decreased thirst"
        )));
        correctAnswers.add("Hyperglycemia");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Hyperglycemia (Correct answer)\n" +
                        "A hallmark of diabetes due to lack of insulin or insulin resistance.\n\n" +
                        "Increased calcium levels (Incorrect)\n" +
                        "Not directly related to diabetes.\n\n" +
                        "Increased energy levels (Incorrect)\n" +
                        "Diabetics often feel fatigued.\n\n" +
                        "Decreased thirst (Incorrect)\n" +
                        "Diabetes causes polydipsia (increased thirst)."
        );

        questions.add("What does the hormone cortisol do during stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lowers blood glucose",
                "Increases glucose production", // Correct answer
                "Reduces inflammation",
                "Stimulates milk production"
        )));
        correctAnswers.add("Increases glucose production");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Increases glucose production (Correct answer)\n" +
                        "Cortisol stimulates gluconeogenesis, increasing glucose availability for energy during stress.\n\n" +
                        "Lowers blood glucose (Incorrect)\n" +
                        "Cortisol actually raises blood glucose to provide energy.\n\n" +
                        "Reduces inflammation (Incorrect)\n" +
                        "Partially correct, but not the main purpose in stress response.\n\n" +
                        "Stimulates milk production (Incorrect)\n" +
                        "That’s prolactin’s role."
        );

        questions.add("The hormone secreted by the pineal gland is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melatonin", // Correct answer
                "Prolactin",
                "Oxytocin",
                "Estrogen"
        )));
        correctAnswers.add("Melatonin");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Melatonin (Correct answer)\n" +
                        "The pineal gland releases melatonin, regulating sleep-wake cycles.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Produced by the anterior pituitary.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Comes from the posterior pituitary.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Produced in the ovaries."
        );

        questions.add("What gland is involved in the regulation of the body's circadian rhythms?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland", // Correct answer
                "Adrenal glands",
                "Pituitary gland",
                "Thyroid gland"
        )));
        correctAnswers.add("Pineal gland");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Pineal gland (Correct answer)\n" +
                        "Releases melatonin, crucial for the circadian rhythm.\n\n" +
                        "Adrenal glands (Incorrect)\n" +
                        "Secretes cortisol and adrenaline.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Regulates other glands, not circadian rhythms directly.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "Manages metabolism."
        );

        questions.add("The hormone secreted by the pituitary gland to stimulate the adrenal cortex is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "ACTH", // Correct answer
                "GH",
                "TSH",
                "FSH"
        )));
        correctAnswers.add("ACTH");
        rationales.put(73,
                "RATIONALE:\n" +
                        "ACTH (Correct answer)\n" +
                        "Stimulates the adrenal cortex to release cortisol.\n\n" +
                        "GH (Incorrect)\n" +
                        "Promotes body growth.\n\n" +
                        "TSH (Incorrect)\n" +
                        "Stimulates the thyroid gland.\n\n" +
                        "FSH (Incorrect)\n" +
                        "Regulates reproductive functions."
        );

        questions.add("Which of the following is produced in response to low blood calcium levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Calcitonin",
                "Parathyroid hormone (PTH)", // Correct answer
                "Insulin",
                "Prolactin"
        )));
        correctAnswers.add("Parathyroid hormone (PTH)");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Parathyroid hormone (PTH) (Correct answer)\n" +
                        "Increases blood calcium by releasing it from bones and increasing absorption.\n\n" +
                        "Calcitonin (Incorrect)\n" +
                        "Lowers blood calcium, secreted when calcium is high.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates glucose, not calcium.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Involved in milk production."
        );

        questions.add("Which of the following hormones is involved in the body’s response to dehydration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prolactin",
                "Antidiuretic hormone (ADH)", // Correct answer
                "Epinephrine",
                "Cortisol"
        )));
        correctAnswers.add("Antidiuretic hormone (ADH)");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Antidiuretic hormone (ADH) (Correct answer)\n" +
                        "Promotes water reabsorption in kidneys to conserve water.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Milk production, not fluid balance.\n\n" +
                        "Epinephrine (Incorrect)\n" +
                        "Manages fight-or-flight responses.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Manages stress, not hydration directly."
        );

        questions.add("What gland is responsible for secreting parathyroid hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroid gland",
                "Adrenal gland",
                "Parathyroid glands", // Correct answer
                "Pituitary gland"
        )));
        correctAnswers.add("Parathyroid glands");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Parathyroid glands (Correct answer)\n" +
                        "Located behind the thyroid, they release PTH.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "Produces thyroxine and calcitonin, not PTH.\n\n" +
                        "Adrenal gland (Incorrect)\n" +
                        "Produces cortisol, aldosterone.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Controls many glands, but not PTH."
        );

        questions.add("The hormone calcitonin is secreted by which gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pituitary gland",
                "Parathyroid gland",
                "Thyroid gland", // Correct answer
                "Pineal gland"
        )));
        correctAnswers.add("Thyroid gland");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Thyroid gland (Correct answer)\n" +
                        "Produces calcitonin, which lowers blood calcium.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "Does not produce calcitonin.\n\n" +
                        "Parathyroid gland (Incorrect)\n" +
                        "Secretes PTH, not calcitonin.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "Produces melatonin."
        );

        questions.add("Which hormone is primarily responsible for regulating blood glucose levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thyroxine",
                "Insulin", // Correct answer
                "Growth hormone",
                "Adrenaline"
        )));
        correctAnswers.add("Insulin");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Insulin (Correct answer)\n" +
                        "Secreted by beta cells in the pancreas to lower blood sugar.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Affects metabolism but not glucose directly.\n\n" +
                        "Growth hormone (Incorrect)\n" +
                        "Affects growth and may raise blood sugar, but not the main regulator.\n\n" +
                        "Adrenaline (Incorrect)\n" +
                        "Raises glucose in emergencies, not routine regulation."
        );

        questions.add("Which of the following hormones is secreted by the posterior pituitary?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prolactin",
                "Oxytocin", // Correct answer
                "Growth hormone",
                "TSH"
        )));
        correctAnswers.add("Oxytocin");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Oxytocin (Correct answer)\n" +
                        "Stored and secreted by the posterior pituitary, stimulates uterine contractions and milk ejection.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Anterior pituitary hormone.\n\n" +
                        "Growth hormone (Incorrect)\n" +
                        "From the anterior pituitary.\n\n" +
                        "TSH (Incorrect)\n" +
                        "Also from the anterior pituitary."
        );

        questions.add("Which of the following is NOT a function of the endocrine system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulation of metabolism",
                "Regulation of mood and emotions",
                "Control of voluntary muscle movements", // Correct answer
                "Regulation of reproductive functions"
        )));
        correctAnswers.add("Control of voluntary muscle movements");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Control of voluntary muscle movements (Correct answer)\n" +
                        "This is the job of the nervous system, not the endocrine system.\n\n" +
                        "Regulation of metabolism (Incorrect)\n" +
                        "The endocrine system regulates metabolism via hormones like thyroxine.\n\n" +
                        "Regulation of mood and emotions (Incorrect)\n" +
                        "Hormones such as serotonin, dopamine, and cortisol influence mood.\n\n" +
                        "Regulation of reproductive functions (Incorrect)\n" +
                        "Hormones like estrogen, progesterone, and testosterone regulate reproduction."
        );

        questions.add("Which hormone is responsible for the \"fight or flight\" response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Adrenaline (Epinephrine)", // Correct answer
                "Melatonin",
                "Prolactin"
        )));
        correctAnswers.add("Adrenaline (Epinephrine)");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Adrenaline (Epinephrine) (Correct answer)\n" +
                        "Secreted by the adrenal medulla, prepares the body for rapid action.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood sugar, not emergency responses.\n\n" +
                        "Melatonin (Incorrect)\n" +
                        "Regulates sleep, not stress.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Involved in milk production."
        );

        questions.add("Which of the following hormones stimulates the growth of follicles in the ovaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Follicle-stimulating hormone (FSH)", // Correct answer
                "Growth hormone (GH)",
                "Luteinizing hormone (LH)",
                "Progesterone"
        )));
        correctAnswers.add("Follicle-stimulating hormone (FSH)");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Follicle-stimulating hormone (FSH) (Correct answer)\n" +
                        "Stimulates ovarian follicle development.\n\n" +
                        "Growth hormone (GH) (Incorrect)\n" +
                        "Promotes overall growth, not specific to ovaries.\n\n" +
                        "Luteinizing hormone (LH) (Incorrect)\n" +
                        "Triggers ovulation, not follicle growth.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Prepares uterus for pregnancy."
        );

        questions.add("What is the name of the hormone that helps maintain pregnancy by preparing the uterine lining?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Progesterone", // Correct answer
                "Oxytocin",
                "Prolactin"
        )));
        correctAnswers.add("Progesterone");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Progesterone (Correct answer)\n" +
                        "Maintains uterine lining post-ovulation and during pregnancy.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Helps develop the endometrium, but progesterone sustains it.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Induces labor contractions, not maintenance.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Aids in lactation, not uterine maintenance."
        );

        questions.add("What is the main function of growth hormone (GH)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulate milk production",
                "Stimulate the growth of bones and tissues", // Correct answer
                "Increase blood sugar levels",
                "Regulate water balance"
        )));
        correctAnswers.add("Stimulate the growth of bones and tissues");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Stimulate the growth of bones and tissues (Correct answer)\n" +
                        "GH stimulates linear growth, especially during childhood.\n\n" +
                        "Stimulate milk production (Incorrect)\n" +
                        "This is prolactin’s role.\n\n" +
                        "Increase blood sugar levels (Incorrect)\n" +
                        "GH does have diabetogenic effects, but it’s not the main function.\n\n" +
                        "Regulate water balance (Incorrect)\n" +
                        "That’s ADH’s job."
        );

        questions.add("Which of the following is the main function of the pancreas in the endocrine system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Producing growth hormone",
                "Secreting insulin and glucagon to regulate blood sugar", // Correct answer
                "Regulating metabolism",
                "Secreting estrogen"
        )));
        correctAnswers.add("Secreting insulin and glucagon to regulate blood sugar");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Secreting insulin and glucagon to regulate blood sugar (Correct answer)\n" +
                        "These hormones are secreted by beta and alpha cells respectively.\n\n" +
                        "Producing growth hormone (Incorrect)\n" +
                        "GH is from the pituitary gland.\n\n" +
                        "Regulating metabolism (Incorrect)\n" +
                        "Mainly the thyroid’s role.\n\n" +
                        "Secreting estrogen (Incorrect)\n" +
                        "Estrogen is from ovaries."
        );

        questions.add("What hormone is released from the pancreas to increase blood sugar levels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Glucagon", // Correct answer
                "Cortisol",
                "Thyroxine"
        )));
        correctAnswers.add("Glucagon");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Glucagon (Correct answer)\n" +
                        "Released by alpha cells, it increases blood glucose by stimulating glycogenolysis.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Lowers blood sugar.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "It does raise glucose, but it’s from adrenal glands, not the pancreas.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Involved in metabolism, not directly in glucose control."
        );

        questions.add("The primary role of the endocrine system is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate the body’s immune response",
                "Produce enzymes for digestion",
                "Secrete hormones to regulate various body functions", // Correct answer
                "Transport nutrients"
        )));
        correctAnswers.add("Secrete hormones to regulate various body functions");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Secrete hormones to regulate various body functions (Correct answer)\n" +
                        "That’s the main function of the endocrine system.\n\n" +
                        "Regulate the body’s immune response (Incorrect)\n" +
                        "Mostly the role of the immune system.\n\n" +
                        "Produce enzymes for digestion (Incorrect)\n" +
                        "The digestive system handles this.\n\n" +
                        "Transport nutrients (Incorrect)\n" +
                        "Function of the circulatory system."
        );

        questions.add("Which hormone is primarily responsible for controlling the body's stress response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melatonin",
                "Cortisol", // Correct answer
                "Estrogen",
                "Insulin"
        )));
        correctAnswers.add("Cortisol");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Cortisol (Correct answer)\n" +
                        "Secreted by the adrenal cortex, helps the body respond to stress.\n\n" +
                        "Melatonin (Incorrect)\n" +
                        "Controls sleep cycle.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Involved in reproductive functions.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Controls blood sugar, not stress."
        );

        questions.add("Which gland is responsible for the secretion of follicle-stimulating hormone (FSH)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland",
                "Pituitary gland", // Correct answer
                "Thyroid gland",
                "Adrenal gland"
        )));
        correctAnswers.add("Pituitary gland");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Pituitary gland (Correct answer)\n" +
                        "The anterior pituitary secretes FSH.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "Produces melatonin.\n\n" +
                        "Thyroid gland (Incorrect)\n" +
                        "Produces thyroxine, not FSH.\n\n" +
                        "Adrenal gland (Incorrect)\n" +
                        "Secretes cortisol, aldosterone, etc."
        );

        questions.add("What is the main function of thyroid hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulate the immune system",
                "Regulate the body's metabolic rate", // Correct answer
                "Promote the release of adrenaline",
                "Control calcium levels in the blood"
        )));
        correctAnswers.add("Regulate the body's metabolic rate");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Regulate the body's metabolic rate (Correct answer)\n" +
                        "Thyroxine (T4) and triiodothyronine (T3) regulate metabolism.\n\n" +
                        "Stimulate the immune system (Incorrect)\n" +
                        "This is the function of immune system-related hormones, not thyroid hormones.\n\n" +
                        "Promote the release of adrenaline (Incorrect)\n" +
                        "This is related to adrenal glands, not thyroid hormones.\n\n" +
                        "Control calcium levels in the blood (Incorrect)\n" +
                        "This is a function of parathyroid hormone."
        );

        questions.add("Which of the following conditions is caused by insufficient production of thyroid hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyperthyroidism",
                "Hypothyroidism", // Correct answer
                "Cushing’s syndrome",
                "Addison’s disease"
        )));
        correctAnswers.add("Hypothyroidism");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Hypothyroidism (Correct answer)\n" +
                        "Insufficient thyroid hormone, leading to slow metabolism, fatigue, and other symptoms.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "Caused by excessive thyroid hormone production.\n\n" +
                        "Cushing’s syndrome (Incorrect)\n" +
                        "Caused by excessive cortisol production.\n\n" +
                        "Addison’s disease (Incorrect)\n" +
                        "Caused by insufficient cortisol and aldosterone production."
        );

        questions.add("What does the hormone prolactin primarily regulate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood pressure",
                "Milk production", // Correct answer
                "Growth of hair",
                "Blood glucose levels"
        )));
        correctAnswers.add("Milk production");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Milk production (Correct answer)\n" +
                        "Prolactin stimulates the production of milk in lactating women.\n\n" +
                        "Blood pressure (Incorrect)\n" +
                        "Blood pressure is regulated by aldosterone, adrenaline, and other factors.\n\n" +
                        "Growth of hair (Incorrect)\n" +
                        "Hair growth is influenced by androgens, not prolactin.\n\n" +
                        "Blood glucose levels (Incorrect)\n" +
                        "This is the role of insulin and glucagon."
        );

        questions.add("Which of the following glands is located at the base of the brain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland",
                "Pituitary gland", // Correct answer
                "Thymus",
                "Parathyroid glands"
        )));
        correctAnswers.add("Pituitary gland");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Pituitary gland (Correct answer)\n" +
                        "Located at the base of the brain, it secretes several critical hormones.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "Located in the brain, but not at the base, and primarily secretes melatonin.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "Located in the chest and regulates immune system function.\n\n" +
                        "Parathyroid glands (Incorrect)\n" +
                        "Located behind the thyroid, not at the base of the brain."
        );

        questions.add("Which hormone helps regulate the body's response to stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cortisol", // Correct answer
                "Thyroxine",
                "Oxytocin",
                "Insulin"
        )));
        correctAnswers.add("Cortisol");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Cortisol (Correct answer)\n" +
                        "Known as the \"stress hormone,\" cortisol helps the body cope with stress by elevating blood sugar and controlling inflammation.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Regulates metabolism, not directly related to stress.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Primarily involved in labor, bonding, and milk ejection.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Regulates blood sugar, not stress."
        );

        questions.add("What is the main purpose of antidiuretic hormone (ADH)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To regulate growth",
                "To decrease water retention in the kidneys",
                "To increase water retention in the kidneys", // Correct answer
                "To stimulate the release of insulin"
        )));
        correctAnswers.add("To increase water retention in the kidneys");
        rationales.put(95,
                "RATIONALE:\n" +
                        "To increase water retention in the kidneys (Correct answer)\n" +
                        "ADH (also called vasopressin) helps the kidneys retain water to conserve body fluids.\n\n" +
                        "To regulate growth (Incorrect)\n" +
                        "This is the role of growth hormone (GH).\n\n" +
                        "To decrease water retention in the kidneys (Incorrect)\n" +
                        "ADH increases water retention to prevent dehydration.\n\n" +
                        "To stimulate the release of insulin (Incorrect)\n" +
                        "Insulin is released by the pancreas."
        );

        questions.add("Which of the following is a function of the thyroid gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secretes growth hormone",
                "Regulates calcium balance",
                "Secretes thyroid hormones that control metabolism", // Correct answer
                "Secretes insulin"
        )));
        correctAnswers.add("Secretes thyroid hormones that control metabolism");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Secretes thyroid hormones that control metabolism (Correct answer)\n" +
                        "The thyroid produces T3 and T4, which regulate the body's metabolism.\n\n" +
                        "Secretes growth hormone (Incorrect)\n" +
                        "Growth hormone is secreted by the pituitary gland.\n\n" +
                        "Regulates calcium balance (Incorrect)\n" +
                        "This function is performed by the parathyroid glands.\n\n" +
                        "Secretes insulin (Incorrect)\n" +
                        "Insulin is secreted by the pancreas."
        );

        questions.add("The hormone insulin is essential for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stimulating growth",
                "Regulating blood sugar levels", // Correct answer
                "Regulating sleep-wake cycles",
                "Regulating metabolism"
        )));
        correctAnswers.add("Regulating blood sugar levels");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Regulating blood sugar levels (Correct answer)\n" +
                        "Insulin lowers blood glucose levels by facilitating glucose uptake into cells.\n\n" +
                        "Stimulating growth (Incorrect)\n" +
                        "Growth hormone (GH) stimulates growth.\n\n" +
                        "Regulating sleep-wake cycles (Incorrect)\n" +
                        "Melatonin is responsible for regulating sleep-wake cycles.\n\n" +
                        "Regulating metabolism (Incorrect)\n" +
                        "This is primarily the role of thyroid hormones."
        );

        questions.add("Which of the following is a condition that occurs due to insufficient production of insulin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyperthyroidism",
                "Diabetes mellitus", // Correct answer
                "Cushing’s syndrome",
                "Addison’s disease"
        )));
        correctAnswers.add("Diabetes mellitus");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Diabetes mellitus (Correct answer)\n" +
                        "Results from insulin deficiency or insulin resistance, leading to hyperglycemia.\n\n" +
                        "Hyperthyroidism (Incorrect)\n" +
                        "Caused by excessive thyroid hormones, not insulin.\n\n" +
                        "Cushing’s syndrome (Incorrect)\n" +
                        "Caused by excessive cortisol, not insulin deficiency.\n\n" +
                        "Addison’s disease (Incorrect)\n" +
                        "Caused by insufficient cortisol, not insulin."
        );

        questions.add("Which hormone is released during exercise and increases heart rate and blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Adrenaline", // Correct answer
                "Oxytocin",
                "Progesterone",
                "Prolactin"
        )));
        correctAnswers.add("Adrenaline");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Adrenaline (Correct answer)\n" +
                        "Released by the adrenal glands, it increases heart rate, blood pressure, and blood flow to muscles during exercise.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Released during labor and lactation, not during exercise.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Regulates reproductive functions, not exercise responses.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Involved in milk production, not exercise."
        );
    }


    private void randomizeQuestions() {
        questionOrder = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            questionOrder.add(i);
        }
        Collections.shuffle(questionOrder);
        if (questionOrder.size() > maxQuestions) {
            questionOrder = questionOrder.subList(0, maxQuestions);
        }
    }

    private void displayQuestion(int index) {
        int realIndex = questionOrder.get(index);
        questionText.setText(questions.get(realIndex));
        counterText.setText((index + 1) + " out of " + maxQuestions);

        List<String> choiceList = new ArrayList<>(choices.get(realIndex));
        Collections.shuffle(choiceList);

        btnA.setText(choiceList.get(0));
        btnB.setText(choiceList.get(1));
        btnC.setText(choiceList.get(2));
        btnD.setText(choiceList.get(3));

        resetButtonStyles();
        setButtonsEnabled(true);
        hasAnswered = false;
        rationaleCard.setVisibility(View.GONE);
    }

    private void checkAnswer(Button selectedButton) {
        int realIndex = questionOrder.get(currentIndex);
        String correct = correctAnswers.get(realIndex);
        String selectedText = selectedButton.getText().toString();

        setButtonsEnabled(false);
        hasAnswered = true;

        // Stop the timer since the user has answered
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Update score if the answer is correct
        if (selectedText.equals(correct)) {
            correctAnswersCount++;
            selectedButton.setBackgroundResource(R.drawable.correct_border);
        } else {
            selectedButton.setBackgroundResource(R.drawable.incorrect_border);
            highlightCorrectAnswer(correct);
        }

        // Delay the rationale pop-up to ensure the answer status is shown first
        new Handler().postDelayed(() -> showRationale(realIndex), 800); // Delay for 500ms

    }

    private void highlightCorrectAnswer(String correct) {
        if (btnA.getText().toString().equals(correct)) {
            btnA.setBackgroundResource(R.drawable.correct_border);
        } else if (btnB.getText().toString().equals(correct)) {
            btnB.setBackgroundResource(R.drawable.correct_border);
        } else if (btnC.getText().toString().equals(correct)) {
            btnC.setBackgroundResource(R.drawable.correct_border);
        } else if (btnD.getText().toString().equals(correct)) {
            btnD.setBackgroundResource(R.drawable.correct_border);
        }
    }

    private void resetButtonStyles() {
        btnA.setBackgroundResource(R.drawable.linear_gradient_bg);
        btnB.setBackgroundResource(R.drawable.linear_gradient_bg);
        btnC.setBackgroundResource(R.drawable.linear_gradient_bg);
        btnD.setBackgroundResource(R.drawable.linear_gradient_bg);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
        btnD.setEnabled(enabled);
    }

    private void resetQuiz() {
        currentIndex = 0;
        randomizeQuestions();
        displayQuestion(currentIndex);
    }

    private void showRationale(int questionIndex) {
        // Disable the answer buttons to prevent further interaction
        setButtonsEnabled(false);

        String question = questions.get(questionIndex);
        String rationale = rationales.get(questionIndex);

        if (rationale != null && !rationale.isEmpty()) {
            SpannableStringBuilder formattedRationale = new SpannableStringBuilder();

            // Add the question text at the top
            formattedRationale.append("QUESTION:\n")
                    .append(question)
                    .append("\n\n");

            String[] parts = rationale.split("\\n");

            for (String part : parts) {
                int start = formattedRationale.length();
                formattedRationale.append(part).append("\n");

                if (part.contains("(Correct answer)")) {
                    formattedRationale.setSpan(
                            new ForegroundColorSpan(getResources().getColor(R.color.green)),
                            start,
                            start + part.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                } else if (part.contains("(Incorrect)")) {
                    formattedRationale.setSpan(
                            new ForegroundColorSpan(getResources().getColor(R.color.red)),
                            start,
                            start + part.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }

            rationaleTextView.setText(formattedRationale);
            rationaleCard.setVisibility(View.VISIBLE);
        } else {
            rationaleCard.setVisibility(View.GONE);
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ChallengeMode8.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
