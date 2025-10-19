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

public class ChallengeMode9 extends AppCompatActivity {

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

        setContentView(R.layout.challenge_mode9);

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
                Toast.makeText(ChallengeMode9.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode9.this)
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
            new AlertDialog.Builder(ChallengeMode9.this)
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
                new AlertDialog.Builder(ChallengeMode9.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode9.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Lymphatic System", correctAnswersCount, totalQuestions);


                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Lymphatic System");
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
                        Intent intent = new Intent(ChallengeMode9.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode9.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode9.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Lymphatic System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Lymphatic System");
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
        new AlertDialog.Builder(ChallengeMode9.this)
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

        questions.add("What is the main function of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Remove excess fluids from tissues", // Correct answer
                "Circulate blood",
                "Produce red blood cells",
                "Produce hormones"
        )));
        correctAnswers.add("Remove excess fluids from tissues");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Remove excess fluids from tissues (Correct answer)\n" +
                        "The lymphatic system drains excess fluid (lymph) from tissues and returns it to the blood circulatory system.\n\n" +
                        "Circulate blood (Incorrect)\n" +
                        "Blood circulation is the role of the cardiovascular system, not the lymphatic system.\n\n" +
                        "Produce red blood cells (Incorrect)\n" +
                        "Red blood cells are produced in the bone marrow, not by the lymphatic system.\n\n" +
                        "Produce hormones (Incorrect)\n" +
                        "Hormone production is primarily the role of the endocrine system."
        );

        questions.add("Which of the following is NOT a component of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Heart", // Correct answer
                "Lymph nodes",
                "Lymph vessels",
                "Spleen"
        )));
        correctAnswers.add("Heart");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Heart (Correct answer)\n" +
                        "The heart is part of the cardiovascular system, not the lymphatic system.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes are part of the lymphatic system and play a key role in filtering lymph.\n\n" +
                        "Lymph vessels (Incorrect)\n" +
                        "Lymph vessels are essential to the lymphatic system, transporting lymph throughout the body.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is an important organ in the lymphatic system, filtering blood and producing lymphocytes."
        );

        questions.add("What is the fluid transported by the lymphatic vessels called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph", // Correct answer
                "Plasma",
                "Blood",
                "Cerebrospinal fluid"
        )));
        correctAnswers.add("Lymph");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Lymph (Correct answer)\n" +
                        "Lymph is the fluid transported by the lymphatic vessels, derived from interstitial fluid.\n\n" +
                        "Plasma (Incorrect)\n" +
                        "Plasma is the liquid component of blood, not lymph.\n\n" +
                        "Blood (Incorrect)\n" +
                        "Blood is transported by blood vessels, not lymphatic vessels.\n\n" +
                        "Cerebrospinal fluid (Incorrect)\n" +
                        "This is the fluid that surrounds and cushions the brain and spinal cord, not part of the lymphatic system."
        );

        questions.add("Where do the lymphatic vessels drain their contents?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Venous system (near the jugular vein)", // Correct answer
                "Lungs",
                "Kidneys",
                "Heart"
        )));
        correctAnswers.add("Venous system (near the jugular vein)");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Venous system (near the jugular vein) (Correct answer)\n" +
                        "The lymphatic vessels empty their contents into the venous system near the jugular vein, particularly at the junction of the subclavian vein.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "Lymphatic vessels do not drain into the lungs.\n\n" +
                        "Kidneys (Incorrect)\n" +
                        "Lymph does not drain into the kidneys; instead, it returns to the venous system.\n\n" +
                        "Heart (Incorrect)\n" +
                        "Lymphatic vessels drain into the venous system near the jugular vein, not directly into the heart."
        );

        questions.add("What is the primary function of the spleen in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filter blood and store red blood cells", // Correct answer
                "Produce lymphocytes",
                "Detoxify harmful substances",
                "Produce hormones"
        )));
        correctAnswers.add("Filter blood and store red blood cells");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Filter blood and store red blood cells (Correct answer)\n" +
                        "The spleen filters blood to remove damaged red blood cells and stores red blood cells and platelets.\n\n" +
                        "Produce lymphocytes (Incorrect)\n" +
                        "The spleen does produce lymphocytes, but its primary function is blood filtration.\n\n" +
                        "Detoxify harmful substances (Incorrect)\n" +
                        "This is more the role of the liver, not the spleen.\n\n" +
                        "Produce hormones (Incorrect)\n" +
                        "The spleen does not produce hormones."
        );

        questions.add("Which of the following is responsible for producing lymphocytes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bone marrow", // Correct answer
                "Lymph nodes",
                "Spleen",
                "Thymus"
        )));
        correctAnswers.add("Bone marrow");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Bone marrow (Correct answer)\n" +
                        "The bone marrow produces lymphocytes (a type of white blood cell), which are crucial for immune responses.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes store and filter lymphocytes but do not produce them.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen stores lymphocytes but does not produce them.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus matures T-cells but does not produce lymphocytes."
        );

        questions.add("Which lymphatic organ is responsible for maturing T-cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thymus", // Correct answer
                "Spleen",
                "Bone marrow",
                "Lymph nodes"
        )));
        correctAnswers.add("Thymus");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Thymus (Correct answer)\n" +
                        "The thymus is where T-cells mature and become functional.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is involved in filtering blood and storing lymphocytes, but it does not mature T-cells.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "Bone marrow produces lymphocytes but does not mature T-cells.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes filter lymph but do not mature T-cells."
        );

        questions.add("What is the role of the lymph nodes in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filter lymph and trap foreign particles", // Correct answer
                "Produce red blood cells",
                "Store white blood cells",
                "Secrete antibodies"
        )));
        correctAnswers.add("Filter lymph and trap foreign particles");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Filter lymph and trap foreign particles (Correct answer)\n" +
                        "Lymph nodes filter lymph, trapping foreign particles (like bacteria or viruses) to aid in immune defense.\n\n" +
                        "Produce red blood cells (Incorrect)\n" +
                        "Red blood cell production occurs in the bone marrow, not the lymph nodes.\n\n" +
                        "Store white blood cells (Incorrect)\n" +
                        "Lymph nodes house white blood cells but are not primarily for storage.\n\n" +
                        "Secrete antibodies (Incorrect)\n" +
                        "Lymph nodes help in immune responses, but antibodies are secreted by plasma cells, not directly by lymph nodes."
        );

        questions.add("What is the largest lymphatic vessel in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thoracic duct", // Correct answer
                "Jugular vein",
                "Right lymphatic duct",
                "Brachial vein"
        )));
        correctAnswers.add("Thoracic duct");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Thoracic duct (Correct answer)\n" +
                        "The thoracic duct is the largest lymphatic vessel, draining lymph from the left side of the body into the venous system.\n\n" +
                        "Jugular vein (Incorrect)\n" +
                        "The jugular vein is a blood vessel, not a lymphatic vessel.\n\n" +
                        "Right lymphatic duct (Incorrect)\n" +
                        "The right lymphatic duct drains the right side of the upper body, but it is smaller than the thoracic duct.\n\n" +
                        "Brachial vein (Incorrect)\n" +
                        "The brachial vein is a blood vessel, not a lymphatic vessel."
        );

        questions.add("Which part of the lymphatic system helps in the defense against infections?");
        choices.add(new ArrayList<>(Arrays.asList(
                "All of the above", // Correct answer
                "Lymph nodes",
                "Spleen",
                "Thymus"
        )));
        correctAnswers.add("All of the above");
        rationales.put(9,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Lymph nodes, spleen, and the thymus all play important roles in defending the body against infections.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes filter lymph and trap foreign particles, playing a key role in immune defense.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen helps filter blood and removes damaged cells, supporting immune function.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus helps in T-cell maturation, critical for immune defense."
        );

        questions.add("Where does the right lymphatic duct drain lymph from?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left side of the head",
                "Right side of the body", // Correct answer
                "Left side of the body",
                "Both sides of the body"
        )));
        correctAnswers.add("Right side of the body");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Right side of the body (Correct answer)\n" +
                        "The right lymphatic duct drains lymph from the right side of the head, right arm, and right side of the thorax into the venous system.\n\n" +
                        "Left side of the head (Incorrect)\n" +
                        "The left side of the head is drained by the left lymphatic duct, also called the thoracic duct.\n\n" +
                        "Left side of the body (Incorrect)\n" +
                        "The left side of the body is drained by the thoracic duct, not the right lymphatic duct.\n\n" +
                        "Both sides of the body (Incorrect)\n" +
                        "The right lymphatic duct drains only the right side of the body, while the thoracic duct drains the rest."
        );

        questions.add("Which cells in the lymphatic system are involved in fighting infections?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Red blood cells",
                "Lymphocytes", // Correct answer
                "Platelets",
                "Neurons"
        )));
        correctAnswers.add("Lymphocytes");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Lymphocytes (Correct answer)\n" +
                        "Lymphocytes (including T-cells and B-cells) are the key cells of the immune system, responsible for responding to infections.\n\n" +
                        "Red blood cells (Incorrect)\n" +
                        "Red blood cells carry oxygen and do not play a direct role in fighting infections.\n\n" +
                        "Platelets (Incorrect)\n" +
                        "Platelets are involved in blood clotting, not directly in immune defense.\n\n" +
                        "Neurons (Incorrect)\n" +
                        "Neurons are part of the nervous system and do not have a role in fighting infections."
        );

        questions.add("Which of the following is a type of lymphocyte?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eosinophil",
                "Neutrophil",
                "T-cell", // Correct answer
                "Platelet"
        )));
        correctAnswers.add("T-cell");
        rationales.put(12,
                "RATIONALE:\n" +
                        "T-cell (Correct answer)\n" +
                        "T-cells are a type of lymphocyte responsible for cell-mediated immunity, helping to identify and destroy infected cells.\n\n" +
                        "Eosinophil (Incorrect)\n" +
                        "Eosinophils are a type of white blood cell involved in allergic reactions and parasitic infections, but not lymphocytes.\n\n" +
                        "Neutrophil (Incorrect)\n" +
                        "Neutrophils are another type of white blood cell, but they are not classified as lymphocytes.\n\n" +
                        "Platelet (Incorrect)\n" +
                        "Platelets are cell fragments involved in clotting, not lymphocytes."
        );

        questions.add("What is the term for swelling caused by a blockage in the lymphatic vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphadenopathy",
                "Lymphedema", // Correct answer
                "Leukemia",
                "Lymphoma"
        )));
        correctAnswers.add("Lymphedema");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Lymphedema (Correct answer)\n" +
                        "Lymphedema is the abnormal accumulation of lymph fluid due to a blockage or damage to the lymphatic vessels.\n\n" +
                        "Lymphadenopathy (Incorrect)\n" +
                        "Lymphadenopathy refers to swelling of the lymph nodes, often due to infection or inflammation, not blockages in the lymphatic vessels.\n\n" +
                        "Leukemia (Incorrect)\n" +
                        "Leukemia is a cancer of the blood cells, not a lymphatic issue.\n\n" +
                        "Lymphoma (Incorrect)\n" +
                        "Lymphoma is a type of cancer that affects the lymphatic system."
        );

        questions.add("What is the purpose of the tonsils in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce blood cells",
                "Protect the respiratory system from infections", // Correct answer
                "Store lymph",
                "Filter urine"
        )));
        correctAnswers.add("Protect the respiratory system from infections");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Protect the respiratory system from infections (Correct answer)\n" +
                        "The tonsils are part of the lymphatic system and help protect against respiratory infections by trapping pathogens that enter through the mouth and nose.\n\n" +
                        "Produce blood cells (Incorrect)\n" +
                        "The tonsils are not involved in blood cell production; this is the role of the bone marrow.\n\n" +
                        "Store lymph (Incorrect)\n" +
                        "The tonsils do not store lymph; they are involved in immune defense.\n\n" +
                        "Filter urine (Incorrect)\n" +
                        "The tonsils are not involved in urinary filtration; this is the role of the kidneys."
        );

        questions.add("The thymus is most active during which stage of life?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Infancy",
                "Childhood", // Correct answer
                "Adolescence",
                "Adulthood"
        )));
        correctAnswers.add("Childhood");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Childhood (Correct answer)\n" +
                        "The thymus is most active during childhood, when it helps in the maturation of T-cells, which are crucial for immune defense.\n\n" +
                        "Infancy (Incorrect)\n" +
                        "The thymus is somewhat active during infancy, but it becomes most active during childhood.\n\n" +
                        "Adolescence (Incorrect)\n" +
                        "The thymus begins to shrink after childhood, becoming less active during adolescence and adulthood.\n\n" +
                        "Adulthood (Incorrect)\n" +
                        "The thymus atrophies and is less active in adulthood, with its role in T-cell maturation diminishing."
        );

        questions.add("What is the term for enlarged lymph nodes, often a sign of infection?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphedema",
                "Lymphadenopathy", // Correct answer
                "Lymphoma",
                "Leukopenia"
        )));
        correctAnswers.add("Lymphadenopathy");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Lymphadenopathy (Correct answer)\n" +
                        "Lymphadenopathy refers to enlarged lymph nodes, which can occur due to infections, cancer, or other diseases.\n\n" +
                        "Lymphedema (Incorrect)\n" +
                        "Lymphedema refers to swelling due to lymph fluid accumulation, not enlargement of the lymph nodes.\n\n" +
                        "Lymphoma (Incorrect)\n" +
                        "Lymphoma is a type of lymphatic cancer, not a term for swollen lymph nodes.\n\n" +
                        "Leukopenia (Incorrect)\n" +
                        "Leukopenia is a decrease in white blood cells, not related to lymph node enlargement."
        );

        questions.add("Which of the following is NOT a function of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorption of fats from the digestive tract",
                "Protection against infection",
                "Circulation of blood", // Correct answer
                "Maintenance of fluid balance"
        )));
        correctAnswers.add("Circulation of blood");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Circulation of blood (Correct answer)\n" +
                        "Blood circulation is the role of the cardiovascular system, not the lymphatic system.\n\n" +
                        "Absorption of fats from the digestive tract (Incorrect)\n" +
                        "The lymphatic system plays a role in absorbing fats from the digestive system and transporting them through lacteals in the small intestine.\n\n" +
                        "Protection against infection (Incorrect)\n" +
                        "The lymphatic system helps protect the body by filtering pathogens and producing immune cells.\n\n" +
                        "Maintenance of fluid balance (Incorrect)\n" +
                        "The lymphatic system helps maintain fluid balance by returning excess fluid from tissues to the bloodstream."
        );

        questions.add("Which type of blood cells are produced in the bone marrow and are involved in the immune response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Red blood cells",
                "Lymphocytes", // Correct answer
                "Platelets",
                "Neutrophils"
        )));
        correctAnswers.add("Lymphocytes");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Lymphocytes (Correct answer)\n" +
                        "Lymphocytes are a type of white blood cell produced in the bone marrow and are crucial for the immune response.\n\n" +
                        "Red blood cells (Incorrect)\n" +
                        "Red blood cells are produced in the bone marrow, but they are not involved in the immune response.\n\n" +
                        "Platelets (Incorrect)\n" +
                        "Platelets are involved in blood clotting, not immune response.\n\n" +
                        "Neutrophils (Incorrect)\n" +
                        "Neutrophils are white blood cells involved in phagocytosis, not directly in immune response like lymphocytes."
        );

        questions.add("What does the term “lymphedema” refer to?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Inflammation of the spleen",
                "Abnormal accumulation of lymph fluid", // Correct answer
                "Enlargement of lymph nodes",
                "A type of lymphatic cancer"
        )));
        correctAnswers.add("Abnormal accumulation of lymph fluid");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Abnormal accumulation of lymph fluid (Correct answer)\n" +
                        "Lymphedema is the abnormal accumulation of lymph fluid, often due to damage or blockage of lymphatic vessels.\n\n" +
                        "Inflammation of the spleen (Incorrect)\n" +
                        "This refers to splenomegaly, not lymphedema.\n\n" +
                        "Enlargement of lymph nodes (Incorrect)\n" +
                        "Enlargement of lymph nodes is called lymphadenopathy, not lymphedema.\n\n" +
                        "A type of lymphatic cancer (Incorrect)\n" +
                        "Lymphedema is not a form of cancer; it is a condition related to the lymphatic fluid buildup."
        );

        questions.add("Which of the following lymphatic organs filters out pathogens and old blood cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thymus",
                "Bone marrow",
                "Spleen", // Correct answer
                "Tonsils"
        )));
        correctAnswers.add("Spleen");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Spleen (Correct answer)\n" +
                        "The spleen filters blood, removing old blood cells and pathogens, playing an important role in immune function.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is involved in the maturation of T-cells but does not filter pathogens or old blood cells.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "The bone marrow produces blood cells but does not filter pathogens or old blood cells.\n\n" +
                        "Tonsils (Incorrect)\n" +
                        "The tonsils help protect the respiratory system but do not filter blood or old blood cells."
        );

        questions.add("Which part of the lymphatic system is primarily responsible for filtering lymph fluid?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph nodes", // Correct answer
                "Thymus",
                "Spleen",
                "Bone marrow"
        )));
        correctAnswers.add("Lymph nodes");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Lymph nodes (Correct answer)\n" +
                        "Lymph nodes are responsible for filtering lymph fluid, trapping pathogens, and facilitating immune responses by housing immune cells like lymphocytes.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is involved in the maturation of T-cells, not in filtering lymph.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen filters blood, not lymph fluid.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "The bone marrow produces blood cells, including immune cells, but does not filter lymph fluid."
        );

        questions.add("What is the role of B-cells in the immune response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Directly attack infected cells",
                "Produce antibodies", // Correct answer
                "Present antigens to T-cells",
                "Phagocytose bacteria"
        )));
        correctAnswers.add("Produce antibodies");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Produce antibodies (Correct answer)\n" +
                        "B-cells are responsible for producing antibodies that bind to pathogens and neutralize them or mark them for destruction.\n\n" +
                        "Directly attack infected cells (Incorrect)\n" +
                        "B-cells do not directly attack infected cells; this is the role of T-cells.\n\n" +
                        "Present antigens to T-cells (Incorrect)\n" +
                        "Antigen-presenting cells like dendritic cells and macrophages present antigens to T-cells, not B-cells.\n\n" +
                        "Phagocytose bacteria (Incorrect)\n" +
                        "Phagocytosis is performed by other immune cells like neutrophils and macrophages, not B-cells."
        );

        questions.add("Which of the following describes the lymphatic pathway?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph → Heart → Blood",
                "Lymph → Lymph nodes → Blood", // Correct answer
                "Blood → Lymph nodes → Lymph",
                "Blood → Spleen → Lymph"
        )));
        correctAnswers.add("Lymph → Lymph nodes → Blood");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Lymph → Lymph nodes → Blood (Correct answer)\n" +
                        "Lymph flows through lymphatic vessels to the lymph nodes where it is filtered and then drains into the bloodstream.\n\n" +
                        "Lymph → Heart → Blood (Incorrect)\n" +
                        "The lymphatic system drains into the venous system, not directly into the heart.\n\n" +
                        "Blood → Lymph nodes → Lymph (Incorrect)\n" +
                        "Blood does not flow through the lymph nodes; it enters lymphatic vessels to form lymph, which is then filtered by the lymph nodes.\n\n" +
                        "Blood → Spleen → Lymph (Incorrect)\n" +
                        "While the spleen filters blood, it does not contribute directly to the lymphatic pathway in the sequence described here."
        );

        questions.add("What causes the condition known as \"elephantiasis\"?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Parasite infection that blocks lymphatic vessels", // Correct answer
                "Genetic mutation",
                "Autoimmune disorder",
                "Overactive spleen"
        )));
        correctAnswers.add("Parasite infection that blocks lymphatic vessels");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Parasite infection that blocks lymphatic vessels (Correct answer)\n" +
                        "Elephantiasis is caused by parasite infections, such as filariasis, which block the lymphatic vessels, leading to severe swelling and lymphedema.\n\n" +
                        "Genetic mutation (Incorrect)\n" +
                        "Elephantiasis is an infection-related condition, not typically caused by genetic mutations.\n\n" +
                        "Autoimmune disorder (Incorrect)\n" +
                        "While autoimmune disorders can affect the lymphatic system, elephantiasis is specifically caused by parasitic infection.\n\n" +
                        "Overactive spleen (Incorrect)\n" +
                        "An overactive spleen does not cause elephantiasis; the condition is linked to parasitic infections blocking lymphatic flow."
        );

        questions.add("What is the main function of the lymphatic capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Collect lymph fluid from tissues", // Correct answer
                "Transport oxygen",
                "Filter waste products",
                "Transport nutrients"
        )));
        correctAnswers.add("Collect lymph fluid from tissues");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Collect lymph fluid from tissues (Correct answer)\n" +
                        "Lymphatic capillaries are responsible for collecting excess interstitial fluid from tissues and transporting it into the larger lymphatic vessels to form lymph.\n\n" +
                        "Transport oxygen (Incorrect)\n" +
                        "Oxygen is transported by red blood cells via the circulatory system, not by lymphatic capillaries.\n\n" +
                        "Filter waste products (Incorrect)\n" +
                        "The lymphatic system helps filter lymph through lymph nodes, not through the lymphatic capillaries.\n\n" +
                        "Transport nutrients (Incorrect)\n" +
                        "Nutrients are primarily transported via the blood vessels, not the lymphatic system."
        );

        questions.add("The lymphatic system works closely with which other body system to maintain fluid balance?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nervous system",
                "Cardiovascular system", // Correct answer
                "Digestive system",
                "Endocrine system"
        )));
        correctAnswers.add("Cardiovascular system");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Cardiovascular system (Correct answer)\n" +
                        "The cardiovascular system and the lymphatic system work together to regulate fluid levels, with the lymphatic system returning excess fluid to the venous system.\n\n" +
                        "Nervous system (Incorrect)\n" +
                        "While the nervous system and lymphatic system are both vital for overall health, they do not directly work together to maintain fluid balance.\n\n" +
                        "Digestive system (Incorrect)\n" +
                        "While the digestive system absorbs nutrients and fats, the cardiovascular system works more closely with the lymphatic system to maintain fluid balance.\n\n" +
                        "Endocrine system (Incorrect)\n" +
                        "The endocrine system regulates hormones, not fluid balance in conjunction with the lymphatic system."
        );

        questions.add("Which of the following is a major symptom of lymphatic obstruction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fever",
                "Swelling in limbs", // Correct answer
                "Coughing",
                "Excessive thirst"
        )));
        correctAnswers.add("Swelling in limbs");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Swelling in limbs (Correct answer)\n" +
                        "Swelling (often referred to as lymphedema) is a common symptom of lymphatic obstruction, especially in the arms and legs.\n\n" +
                        "Fever (Incorrect)\n" +
                        "Fever can occur with infection but is not the primary symptom of lymphatic obstruction.\n\n" +
                        "Coughing (Incorrect)\n" +
                        "Coughing is more commonly associated with respiratory issues, not lymphatic obstruction.\n\n" +
                        "Excessive thirst (Incorrect)\n" +
                        "Excessive thirst is generally associated with dehydration or conditions like diabetes, not with lymphatic obstruction."
        );

        questions.add("Which of the following is a primary lymphoid organ?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph node",
                "Bone marrow", // Correct answer
                "Spleen",
                "Tonsils"
        )));
        correctAnswers.add("Bone marrow");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Bone marrow (Correct answer)\n" +
                        "The bone marrow is a primary lymphoid organ, where lymphocytes (such as B-cells and T-cells) are produced.\n\n" +
                        "Lymph node (Incorrect)\n" +
                        "Lymph nodes are secondary lymphoid organs involved in filtering lymph and housing immune cells, but not the site of lymphocyte production.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is a secondary lymphoid organ involved in filtering blood.\n\n" +
                        "Tonsils (Incorrect)\n" +
                        "Tonsils are secondary lymphoid organs that help protect against infections in the respiratory system."
        );

        questions.add("What is the major immune function of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorb nutrients from the digestive system",
                "Detoxify harmful substances",
                "Defend against pathogens", // Correct answer
                "Regulate blood pressure"
        )));
        correctAnswers.add("Defend against pathogens");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Defend against pathogens (Correct answer)\n" +
                        "The primary role of the lymphatic system is to defend the body against pathogens by producing and transporting immune cells, such as lymphocytes, and filtering lymph.\n\n" +
                        "Absorb nutrients from the digestive system (Incorrect)\n" +
                        "The lymphatic system helps absorb fats but is primarily involved in immune defense, not nutrient absorption.\n\n" +
                        "Detoxify harmful substances (Incorrect)\n" +
                        "While the lymphatic system filters lymph and blood, detoxification is not its major immune function.\n\n" +
                        "Regulate blood pressure (Incorrect)\n" +
                        "The lymphatic system does not regulate blood pressure; this is the function of the cardiovascular system."
        );

        questions.add("What lymphatic organ is located near the junction of the small and large intestines?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spleen",
                "Tonsils",
                "Peyer’s patches", // Correct answer
                "Thymus"
        )));
        correctAnswers.add("Peyer’s patches");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Peyer’s patches (Correct answer)\n" +
                        "Peyer’s patches are clusters of lymphoid tissue located in the ileum (part of the small intestine) and play a role in immune surveillance.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is located in the upper left part of the abdomen and is not near the junction of the small and large intestines.\n\n" +
                        "Tonsils (Incorrect)\n" +
                        "The tonsils are located in the throat and are not near the small and large intestines.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is located in the chest and is responsible for T-cell maturation, not near the small and large intestines."
        );

        questions.add("What term refers to the removal of the spleen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Splenectomy", // Correct answer
                "Lymphadenectomy",
                "Tonsillectomy",
                "Lymphoma"
        )));
        correctAnswers.add("Splenectomy");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Splenectomy (Correct answer)\n" +
                        "Splenectomy refers to the surgical removal of the spleen.\n\n" +
                        "Lymphadenectomy (Incorrect)\n" +
                        "Lymphadenectomy is the removal of lymph nodes, not the spleen.\n\n" +
                        "Tonsillectomy (Incorrect)\n" +
                        "Tonsillectomy is the removal of the tonsils, not the spleen.\n\n" +
                        "Lymphoma (Incorrect)\n" +
                        "Lymphoma is a type of cancer affecting the lymphatic system, not a surgical procedure."
        );

        questions.add("Which type of lymphocyte is responsible for recognizing infected cells and destroying them?");
        choices.add(new ArrayList<>(Arrays.asList(
                "B-cells",
                "T-cells", // Correct answer
                "Neutrophils",
                "Eosinophils"
        )));
        correctAnswers.add("T-cells");
        rationales.put(32,
                "RATIONALE:\n" +
                        "T-cells (Correct answer)\n" +
                        "T-cells, specifically cytotoxic T-cells, recognize and directly destroy infected cells.\n\n" +
                        "B-cells (Incorrect)\n" +
                        "B-cells are responsible for producing antibodies but do not directly destroy infected cells.\n\n" +
                        "Neutrophils (Incorrect)\n" +
                        "Neutrophils are phagocytes that engulf pathogens but are not responsible for recognizing and destroying infected cells in the way T-cells do.\n\n" +
                        "Eosinophils (Incorrect)\n" +
                        "Eosinophils are involved in combating parasitic infections and allergic reactions but do not destroy infected cells directly like T-cells."
        );

        questions.add("What is the primary function of the lymphatic vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Circulate blood",
                "Absorb nutrients",
                "Transport lymph", // Correct answer
                "Produce antibodies"
        )));
        correctAnswers.add("Transport lymph");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Transport lymph (Correct answer)\n" +
                        "Lymphatic vessels transport lymph, which contains immune cells and helps maintain fluid balance in the body.\n\n" +
                        "Circulate blood (Incorrect)\n" +
                        "Lymphatic vessels do not circulate blood; they transport lymph. Blood circulation is handled by the cardiovascular system.\n\n" +
                        "Absorb nutrients (Incorrect)\n" +
                        "Lymphatic vessels do not directly absorb nutrients; this function is performed by the digestive system.\n\n" +
                        "Produce antibodies (Incorrect)\n" +
                        "Antibodies are produced by B-cells, not by the lymphatic vessels."
        );

        questions.add("Where do the majority of the body’s lymphatic vessels drain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lungs",
                "Lymph nodes",
                "Venous system near the subclavian veins", // Correct answer
                "Heart"
        )));
        correctAnswers.add("Venous system near the subclavian veins");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Venous system near the subclavian veins (Correct answer)\n" +
                        "The lymphatic vessels drain into the venous system near the subclavian veins, where the lymph re-enters the bloodstream.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "Lymphatic vessels do not drain into the lungs.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes filter lymph but do not serve as the final drainage point.\n\n" +
                        "Heart (Incorrect)\n" +
                        "Lymphatic vessels do not drain directly into the heart."
        );

        questions.add("What is the name of the lymphatic fluid that transports fats from the digestive tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphocytes",
                "Chyle", // Correct answer
                "Plasma",
                "Platelets"
        )));
        correctAnswers.add("Chyle");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Chyle (Correct answer)\n" +
                        "Chyle is the milky fluid that contains fats and is transported by the lymphatic system from the digestive tract.\n\n" +
                        "Lymphocytes (Incorrect)\n" +
                        "Lymphocytes are immune cells, not the fluid that transports fats.\n\n" +
                        "Plasma (Incorrect)\n" +
                        "Plasma is the liquid portion of blood, not lymphatic fluid.\n\n" +
                        "Platelets (Incorrect)\n" +
                        "Platelets are components of blood involved in clotting, not in transporting fats."
        );

        questions.add("What part of the lymphatic system is responsible for immune surveillance and response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph nodes", // Correct answer
                "Thymus",
                "Bone marrow",
                "Spleen"
        )));
        correctAnswers.add("Lymph nodes");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Lymph nodes (Correct answer)\n" +
                        "Lymph nodes are critical for immune surveillance and immune responses, as they filter lymph and house immune cells like T-cells and B-cells.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is responsible for the maturation of T-cells, not immune surveillance.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "The bone marrow produces blood cells, including immune cells, but does not perform immune surveillance.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen filters blood and plays a role in immune function, but the lymph nodes are the primary site of immune surveillance and response."
        );

        questions.add("Which of the following is true about the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is part of the digestive system",
                "It only transports water",
                "It helps in immune response and fluid balance", // Correct answer
                "It circulates oxygen"
        )));
        correctAnswers.add("It helps in immune response and fluid balance");
        rationales.put(37,
                "RATIONALE:\n" +
                        "It helps in immune response and fluid balance (Correct answer)\n" +
                        "The lymphatic system plays a key role in maintaining fluid balance and is central to the immune response by transporting immune cells and filtering lymph.\n\n" +
                        "It is part of the digestive system (Incorrect)\n" +
                        "The lymphatic system is not part of the digestive system, though it helps absorb fats from the digestive system.\n\n" +
                        "It only transports water (Incorrect)\n" +
                        "The lymphatic system transports more than just water; it also carries immune cells, fats, and waste products.\n\n" +
                        "It circulates oxygen (Incorrect)\n" +
                        "The lymphatic system does not circulate oxygen; this function is performed by the cardiovascular system."
        );

        questions.add("Which condition is caused by cancer of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Leukemia",
                "Lymphoma", // Correct answer
                "Anemia",
                "Hemophilia"
        )));
        correctAnswers.add("Lymphoma");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Lymphoma (Correct answer)\n" +
                        "Lymphoma is a type of cancer that originates in the lymphatic system, affecting lymphoid tissues like lymph nodes and the spleen.\n\n" +
                        "Leukemia (Incorrect)\n" +
                        "Leukemia is a cancer of the blood cells, not the lymphatic system.\n\n" +
                        "Anemia (Incorrect)\n" +
                        "Anemia is a condition related to low red blood cell count, not cancer of the lymphatic system.\n\n" +
                        "Hemophilia (Incorrect)\n" +
                        "Hemophilia is a genetic disorder related to blood clotting, not a cancer of the lymphatic system."
        );

        questions.add("What is the primary function of the lymphatic capillaries in the small intestine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorb oxygen",
                "Absorb fats", // Correct answer
                "Transport lymph",
                "Filter blood"
        )));
        correctAnswers.add("Absorb fats");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Absorb fats (Correct answer)\n" +
                        "The lymphatic capillaries in the small intestine absorb fats from digested food, forming a fluid called chyle.\n\n" +
                        "Absorb oxygen (Incorrect)\n" +
                        "Lymphatic capillaries in the small intestine (called lacteals) absorb fats, not oxygen.\n\n" +
                        "Transport lymph (Incorrect)\n" +
                        "The primary function of lymphatic capillaries in the small intestine is to absorb fats, not to transport lymph.\n\n" +
                        "Filter blood (Incorrect)\n" +
                        "Lymphatic capillaries in the small intestine do not filter blood; their main role is to absorb fats."
        );

        questions.add("Where is the thymus located?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Above the kidneys",
                "In the chest, near the heart", // Correct answer
                "Behind the liver",
                "Near the spleen"
        )));
        correctAnswers.add("In the chest, near the heart");
        rationales.put(40,
                "RATIONALE:\n" +
                        "In the chest, near the heart (Correct answer)\n" +
                        "The thymus is located in the upper chest, just behind the sternum and near the heart, and is crucial for the maturation of T-cells.\n\n" +
                        "Above the kidneys (Incorrect)\n" +
                        "The thymus is not located above the kidneys. The kidneys are in the lower back, while the thymus is in the chest.\n\n" +
                        "Behind the liver (Incorrect)\n" +
                        "The liver is located in the upper right abdomen, and the thymus is not near it.\n\n" +
                        "Near the spleen (Incorrect)\n" +
                        "The thymus is located in the chest, while the spleen is in the upper left abdomen."
        );

        questions.add("What is the role of T-helper cells in the immune response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Recognize and destroy pathogens",
                "Stimulate B-cells to produce antibodies", // Correct answer
                "Neutralize toxins",
                "Produce enzymes"
        )));
        correctAnswers.add("Stimulate B-cells to produce antibodies");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Stimulate B-cells to produce antibodies (Correct answer)\n" +
                        "T-helper cells play a central role in activating B-cells, which then produce antibodies to fight infections.\n\n" +
                        "Recognize and destroy pathogens (Incorrect)\n" +
                        "While T-cells are involved in recognizing infected cells, T-helper cells primarily stimulate other cells like B-cells to produce antibodies.\n\n" +
                        "Neutralize toxins (Incorrect)\n" +
                        "T-helper cells do not directly neutralize toxins; they assist other immune cells in the immune response.\n\n" +
                        "Produce enzymes (Incorrect)\n" +
                        "T-helper cells do not produce enzymes; their role is to stimulate other immune responses."
        );

        questions.add("Which of the following is a function of the tonsils?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce red blood cells",
                "Produce hormones",
                "Filter bacteria and viruses from the air and food", // Correct answer
                "Store lymphocytes"
        )));
        correctAnswers.add("Filter bacteria and viruses from the air and food");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Filter bacteria and viruses from the air and food (Correct answer)\n" +
                        "The tonsils act as a defense mechanism, filtering out bacteria and viruses that enter the body through the mouth and nose.\n\n" +
                        "Produce red blood cells (Incorrect)\n" +
                        "The tonsils are not involved in producing red blood cells; this is done by the bone marrow.\n\n" +
                        "Produce hormones (Incorrect)\n" +
                        "The tonsils do not produce hormones; they help filter pathogens.\n\n" +
                        "Store lymphocytes (Incorrect)\n" +
                        "While the tonsils contain lymphocytes, their primary function is to filter pathogens rather than store immune cells."
        );

        questions.add("The presence of which type of cells in the lymph nodes is indicative of a viral infection?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eosinophils",
                "T-cells", // Correct answer
                "B-cells",
                "Plasma cells"
        )));
        correctAnswers.add("T-cells");
        rationales.put(43,
                "RATIONALE:\n" +
                        "T-cells (Correct answer)\n" +
                        "T-cells, particularly helper T-cells, are important for the immune response to viral infections and are often present in lymph nodes during a viral infection.\n\n" +
                        "Eosinophils (Incorrect)\n" +
                        "Eosinophils are typically involved in responses to parasites and allergic reactions, not viral infections.\n\n" +
                        "B-cells (Incorrect)\n" +
                        "B-cells produce antibodies, but T-cells are more directly involved in responding to viral infections.\n\n" +
                        "Plasma cells (Incorrect)\n" +
                        "Plasma cells are derived from B-cells and secrete antibodies, but they are not specifically indicative of a viral infection in the lymph nodes."
        );

        questions.add("What part of the lymphatic system filters blood and removes old red blood cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph nodes",
                "Spleen", // Correct answer
                "Bone marrow",
                "Thymus"
        )));
        correctAnswers.add("Spleen");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Spleen (Correct answer)\n" +
                        "The spleen filters blood, removing old or damaged red blood cells and pathogens.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes filter lymph and trap pathogens, but they do not filter blood.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "Bone marrow is responsible for producing blood cells but does not filter blood.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is involved in T-cell maturation, not in filtering blood or removing red blood cells."
        );

        questions.add("What happens when the lymphatic system is blocked or damaged?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased risk of infection",
                "Increased blood pressure",
                "Swelling in the limbs",
                "All of the above" // Correct answer
        )));
        correctAnswers.add("All of the above");
        rationales.put(45,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Damage to the lymphatic system can lead to an increased risk of infection, swelling (lymphedema), and may affect fluid balance.\n\n" +
                        "Increased risk of infection (Incorrect)\n" +
                        "A blocked or damaged lymphatic system leads to impaired immune response, which increases the risk of infection.\n\n" +
                        "Increased blood pressure (Incorrect)\n" +
                        "While the lymphatic system maintains fluid balance, its damage does not directly lead to increased blood pressure.\n\n" +
                        "Swelling in the limbs (Incorrect)\n" +
                        "Blocked lymphatic flow can result in lymphedema, causing swelling in the limbs."
        );

        questions.add("What is the name of the specialized lymphatic vessels in the small intestine that absorb dietary fats?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lacteals", // Correct answer
                "Lymphatic ducts",
                "Venous sinuses",
                "Capillaries"
        )));
        correctAnswers.add("Lacteals");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Lacteals (Correct answer)\n" +
                        "Lacteals are specialized lymphatic vessels in the villi of the small intestine that absorb dietary fats from digested food.\n\n" +
                        "Lymphatic ducts (Incorrect)\n" +
                        "Lymphatic ducts are larger vessels that collect lymph from smaller lymphatic vessels, but they are not specialized for fat absorption.\n\n" +
                        "Venous sinuses (Incorrect)\n" +
                        "Venous sinuses are structures related to blood circulation, not the lymphatic system.\n\n" +
                        "Capillaries (Incorrect)\n" +
                        "While capillaries are small blood vessels involved in nutrient exchange, lacteals are the specific vessels in the small intestine that absorb fats."
        );

        questions.add("Which of the following is a common symptom of lymphedema?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Shortness of breath",
                "Painful, swollen limbs", // Correct answer
                "Frequent headaches",
                "Unexplained weight loss"
        )));
        correctAnswers.add("Painful, swollen limbs");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Painful, swollen limbs (Correct answer)\n" +
                        "Lymphedema is characterized by painful swelling of the limbs due to impaired lymphatic drainage.\n\n" +
                        "Shortness of breath (Incorrect)\n" +
                        "Shortness of breath is not typically associated with lymphedema; it may be a symptom of other conditions.\n\n" +
                        "Frequent headaches (Incorrect)\n" +
                        "Headaches are not a primary symptom of lymphedema.\n\n" +
                        "Unexplained weight loss (Incorrect)\n" +
                        "Unexplained weight loss is not typical of lymphedema but may indicate other conditions."
        );

        questions.add("What is the function of the Peyer’s patches in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce lymphocytes",
                "Filter lymph",
                "Protect against intestinal infections", // Correct answer
                "Detoxify harmful substances"
        )));
        correctAnswers.add("Protect against intestinal infections");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Protect against intestinal infections (Correct answer)\n" +
                        "Peyer’s patches are found in the small intestine and help protect the body by monitoring and responding to potential infections.\n\n" +
                        "Produce lymphocytes (Incorrect)\n" +
                        "While Peyer’s patches contain lymphoid tissue and are involved in immune responses, their main function is not producing lymphocytes.\n\n" +
                        "Filter lymph (Incorrect)\n" +
                        "Peyer’s patches do not primarily filter lymph; they protect against intestinal infections by detecting pathogens.\n\n" +
                        "Detoxify harmful substances (Incorrect)\n" +
                        "Peyer’s patches are not involved in detoxification but are part of the immune defense system."
        );

        questions.add("In which part of the body are the largest concentrations of lymph nodes found?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Arms and legs",
                "Neck, armpits, and groin", // Correct answer
                "Abdomen",
                "Back"
        )));
        correctAnswers.add("Neck, armpits, and groin");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Neck, armpits, and groin (Correct answer)\n" +
                        "The largest concentrations of lymph nodes are found in these areas, where they help filter lymph and detect infections.\n\n" +
                        "Arms and legs (Incorrect)\n" +
                        "While there are lymph nodes in the arms and legs, the largest concentrations are in the neck, armpits, and groin.\n\n" +
                        "Abdomen (Incorrect)\n" +
                        "There are lymph nodes in the abdomen, but the largest concentrations are in the neck, armpits, and groin.\n\n" +
                        "Back (Incorrect)\n" +
                        "The back does not have large concentrations of lymph nodes compared to other areas like the neck and groin."
        );

        questions.add("Which type of lymphocyte is produced in the bone marrow but matures in the thymus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "B-cells",
                "T-cells", // Correct answer
                "Natural killer cells",
                "Dendritic cells"
        )));
        correctAnswers.add("T-cells");
        rationales.put(50,
                "RATIONALE:\n" +
                        "T-cells (Correct answer)\n" +
                        "T-cells are produced in the bone marrow and then migrate to the thymus for maturation before becoming involved in the immune response.\n\n" +
                        "B-cells (Incorrect)\n" +
                        "B-cells are produced and mature in the bone marrow. They are not involved with the thymus for maturation.\n\n" +
                        "Natural killer cells (Incorrect)\n" +
                        "Natural killer cells are part of the innate immune system and are produced in the bone marrow, but they do not mature in the thymus.\n\n" +
                        "Dendritic cells (Incorrect)\n" +
                        "Dendritic cells are immune cells that present antigens to other cells but are not the same as T-cells, nor do they mature in the thymus."
        );

        questions.add("Which lymphatic structure is responsible for draining lymph from the right side of the body’s head, neck, and chest?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thoracic duct",
                "Right lymphatic duct", // Correct answer
                "Spleen",
                "Lymph nodes"
        )));
        correctAnswers.add("Right lymphatic duct");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Right lymphatic duct (Correct answer)\n" +
                        "The right lymphatic duct drains lymph from the right side of the head, neck, and chest, as well as the right arm.\n\n" +
                        "Thoracic duct (Incorrect)\n" +
                        "The thoracic duct drains lymph from the left side of the body and parts of the abdomen, not from the right side of the head, neck, and chest.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is an organ that filters blood but does not drain lymph.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes are filtering stations for lymph but are not responsible for draining lymph from specific regions."
        );

        questions.add("What type of cells are responsible for the production of antibodies?");
        choices.add(new ArrayList<>(Arrays.asList(
                "T-cells",
                "Plasma cells", // Correct answer
                "Dendritic cells",
                "Natural killer cells"
        )));
        correctAnswers.add("Plasma cells");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Plasma cells (Correct answer)\n" +
                        "Plasma cells are B-cells that have differentiated and are responsible for the production of antibodies to fight infections.\n\n" +
                        "T-cells (Incorrect)\n" +
                        "T-cells help in immune responses, especially in attacking infected cells, but they do not produce antibodies.\n\n" +
                        "Dendritic cells (Incorrect)\n" +
                        "Dendritic cells are antigen-presenting cells that help activate T-cells, but they do not produce antibodies.\n\n" +
                        "Natural killer cells (Incorrect)\n" +
                        "Natural killer cells are involved in the innate immune response and target infected or cancerous cells, but they do not produce antibodies."
        );

        questions.add("Which of the following is a characteristic of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is an open circulatory system",
                "It does not have a central pump", // Correct answer
                "It only transports oxygen",
                "It circulates blood through veins and arteries"
        )));
        correctAnswers.add("It does not have a central pump");
        rationales.put(53,
                "RATIONALE:\n" +
                        "It does not have a central pump (Correct answer)\n" +
                        "Unlike the cardiovascular system, the lymphatic system does not have a central pump. Lymph is transported by muscular contractions and valves.\n\n" +
                        "It is an open circulatory system (Incorrect)\n" +
                        "The lymphatic system is a closed system, although it is not directly connected to the circulatory system like veins and arteries.\n\n" +
                        "It only transports oxygen (Incorrect)\n" +
                        "The lymphatic system transports lymph, which contains immune cells, waste, and fats, not oxygen.\n\n" +
                        "It circulates blood through veins and arteries (Incorrect)\n" +
                        "The lymphatic system does not circulate blood; it circulates lymph, a fluid containing immune cells and waste products."
        );

        questions.add("What is the most common cause of lymphatic obstruction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Infections",
                "Tumors",
                "Surgery or radiation",
                "All of the above" // Correct answer
        )));
        correctAnswers.add("All of the above");
        rationales.put(54,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Infections, tumors, and surgery/radiation can all lead to lymphatic obstruction and related complications like swelling and immune dysfunction.\n\n" +
                        "Infections (Incorrect)\n" +
                        "Infections, particularly parasitic infections (e.g., filariasis), can block lymphatic vessels, leading to conditions like lymphedema.\n\n" +
                        "Tumors (Incorrect)\n" +
                        "Tumors can obstruct lymphatic vessels and disrupt normal flow, causing swelling.\n\n" +
                        "Surgery or radiation (Incorrect)\n" +
                        "Surgery or radiation treatments for cancer can lead to damage or blockage of lymphatic vessels, causing lymphedema."
        );

        questions.add("What type of fluid does the lymphatic system transport?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood plasma",
                "Interstitial fluid", // Correct answer
                "Oxygen",
                "Red blood cells"
        )));
        correctAnswers.add("Interstitial fluid");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Interstitial fluid (Correct answer)\n" +
                        "The lymphatic system transports interstitial fluid, which is fluid that surrounds cells in tissues and becomes lymph as it enters lymphatic vessels.\n\n" +
                        "Blood plasma (Incorrect)\n" +
                        "Blood plasma is carried by the cardiovascular system and is not directly transported by the lymphatic system.\n\n" +
                        "Oxygen (Incorrect)\n" +
                        "Oxygen is carried by red blood cells in the blood, not the lymphatic system.\n\n" +
                        "Red blood cells (Incorrect)\n" +
                        "The lymphatic system does not transport red blood cells, as they are part of the blood circulatory system."
        );

        questions.add("Which organ is involved in immune response and filters blood for pathogens, damaged cells, and old red blood cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spleen", // Correct answer
                "Bone marrow",
                "Thymus",
                "Lymph nodes"
        )));
        correctAnswers.add("Spleen");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Spleen (Correct answer)\n" +
                        "The spleen filters blood, removing old red blood cells, pathogens, and damaged cells, while also playing a role in immune responses.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "The bone marrow is where blood cells are produced, but it does not filter blood.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is involved in the maturation of T-cells, not in filtering blood.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes filter lymph, not blood, and are not directly involved in blood filtration."
        );

        questions.add("What is the term for the process by which the lymphatic system absorbs fats from the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph filtration",
                "Lipid absorption",
                "Chyle absorption", // Correct answer
                "Fat synthesis"
        )));
        correctAnswers.add("Chyle absorption");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Chyle absorption (Correct answer)\n" +
                        "Chyle is a milky fluid formed in the lacteals (specialized lymphatic vessels) of the small intestine, which absorbs dietary fats.\n\n" +
                        "Lymph filtration (Incorrect)\n" +
                        "Lymph filtration occurs in lymph nodes, not related to fat absorption from the digestive system.\n\n" +
                        "Lipid absorption (Incorrect)\n" +
                        "Lipid absorption is a general term but does not specifically refer to the lymphatic process of absorbing dietary fats.\n\n" +
                        "Fat synthesis (Incorrect)\n" +
                        "Fat synthesis refers to the production of fats by the body, not absorption by the lymphatic system."
        );

        questions.add("Which of the following best describes the function of the thymus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Storage of lymph",
                "Maturation of T-cells", // Correct answer
                "Production of red blood cells",
                "Filtering of lymphatic fluid"
        )));
        correctAnswers.add("Maturation of T-cells");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Maturation of T-cells (Correct answer)\n" +
                        "The thymus is essential for the maturation of T-cells, which are key components of the adaptive immune response.\n\n" +
                        "Storage of lymph (Incorrect)\n" +
                        "The thymus is not responsible for storing lymph; that is the role of lymph nodes and other lymphatic structures.\n\n" +
                        "Production of red blood cells (Incorrect)\n" +
                        "The thymus is not involved in the production of red blood cells; this is the role of the bone marrow.\n\n" +
                        "Filtering of lymphatic fluid (Incorrect)\n" +
                        "While the thymus is important for immune development, it does not filter lymph; that function is performed by lymph nodes."
        );

        questions.add("Which of the following is true about the spleen’s role in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It stores white blood cells",
                "It produces lymphocytes",
                "It filters blood and recycles iron", // Correct answer
                "It produces red blood cells in adults"
        )));
        correctAnswers.add("It filters blood and recycles iron");
        rationales.put(59,
                "RATIONALE:\n" +
                        "It filters blood and recycles iron (Correct answer)\n" +
                        "The spleen filters blood, removing old red blood cells, and recycles iron from hemoglobin.\n\n" +
                        "It stores white blood cells (Incorrect)\n" +
                        "The spleen stores white blood cells, but this is not its primary function. It is more important in filtering blood.\n\n" +
                        "It produces lymphocytes (Incorrect)\n" +
                        "The spleen contains lymphoid tissue but does not produce lymphocytes; this occurs in the bone marrow.\n\n" +
                        "It produces red blood cells in adults (Incorrect)\n" +
                        "The spleen does not produce red blood cells in adults; this occurs in the bone marrow."
        );

        questions.add("Which of the following cells is the most involved in responding to bacterial infections?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Neutrophils", // Correct answer
                "B-cells",
                "T-helper cells",
                "T-cytotoxic cells"
        )));
        correctAnswers.add("Neutrophils");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Neutrophils (Correct answer)\n" +
                        "Neutrophils are the primary phagocytes and are the first line of defense in the body’s response to bacterial infections. They actively engulf and destroy bacteria.\n\n" +
                        "B-cells (Incorrect)\n" +
                        "B-cells are primarily involved in the production of antibodies and are more involved in responding to viruses and antigens, not directly to bacterial infections.\n\n" +
                        "T-helper cells (Incorrect)\n" +
                        "T-helper cells help activate B-cells and T-cytotoxic cells, but they are not directly involved in responding to bacterial infections.\n\n" +
                        "T-cytotoxic cells (Incorrect)\n" +
                        "T-cytotoxic cells are involved in attacking virus-infected cells and cancer cells, not specifically bacterial infections."
        );

        questions.add("Where in the body are lymph nodes NOT typically found?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Brain", // Correct answer
                "Neck",
                "Groin",
                "Abdomen"
        )));
        correctAnswers.add("Brain");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Brain (Correct answer)\n" +
                        "Lymph nodes are not typically found in the brain. The brain has a specialized system, the glymphatic system, for waste clearance instead of lymph nodes.\n\n" +
                        "Neck (Incorrect)\n" +
                        "Lymph nodes are found in the neck and play an important role in immune surveillance.\n\n" +
                        "Groin (Incorrect)\n" +
                        "Lymph nodes are found in the groin and are part of the lymphatic system.\n\n" +
                        "Abdomen (Incorrect)\n" +
                        "Lymph nodes are found in the abdomen and play a role in filtering lymph from the digestive system."
        );

        questions.add("Which of the following is true about the right lymphatic duct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It drains lymph from the right side of the body above the diaphragm", // Correct answer
                "It drains lymph from the entire body",
                "It is responsible for filtering lymph",
                "It drains lymph from the legs"
        )));
        correctAnswers.add("It drains lymph from the right side of the body above the diaphragm");
        rationales.put(62,
                "RATIONALE:\n" +
                        "It drains lymph from the right side of the body above the diaphragm (Correct answer)\n" +
                        "The right lymphatic duct drains lymph from the right side of the head, neck, chest, and right arm, as well as part of the right lung.\n\n" +
                        "It drains lymph from the entire body (Incorrect)\n" +
                        "The right lymphatic duct only drains lymph from the right side of the body, not the entire body.\n\n" +
                        "It is responsible for filtering lymph (Incorrect)\n" +
                        "Lymph nodes and spleen are responsible for filtering lymph, not the right lymphatic duct.\n\n" +
                        "It drains lymph from the legs (Incorrect)\n" +
                        "The right lymphatic duct does not drain lymph from the legs; this is the role of the thoracic duct."
        );

        questions.add("What is the role of the lymphatic valves?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Maintain the flow of lymph fluid in one direction", // Correct answer
                "Help filter out bacteria",
                "Absorb dietary fats",
                "Transport red blood cells"
        )));
        correctAnswers.add("Maintain the flow of lymph fluid in one direction");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Maintain the flow of lymph fluid in one direction (Correct answer)\n" +
                        "Lymphatic valves prevent the backflow of lymph and ensure it flows in one direction toward the thoracic duct or right lymphatic duct.\n\n" +
                        "Help filter out bacteria (Incorrect)\n" +
                        "Lymphatic valves do not filter bacteria. Lymph nodes perform the filtering of bacteria and other pathogens.\n\n" +
                        "Absorb dietary fats (Incorrect)\n" +
                        "The absorption of dietary fats occurs in the lacteals (specialized lymphatic vessels in the small intestine), not by the lymphatic valves.\n\n" +
                        "Transport red blood cells (Incorrect)\n" +
                        "The lymphatic system does not transport red blood cells; this is the role of the cardiovascular system."
        );

        questions.add("Which of the following describes the primary immune response of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Activation of T-cells and production of antibodies by B-cells", // Correct answer
                "Inactivation of the spleen",
                "Blood clotting and platelet activation",
                "Rapid response to immediate threats"
        )));
        correctAnswers.add("Activation of T-cells and production of antibodies by B-cells");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Activation of T-cells and production of antibodies by B-cells (Correct answer)\n" +
                        "The primary immune response involves the activation of T-cells and the production of antibodies by B-cells.\n\n" +
                        "Inactivation of the spleen (Incorrect)\n" +
                        "The spleen is an important organ in immune surveillance but is not inactivated during the primary immune response.\n\n" +
                        "Blood clotting and platelet activation (Incorrect)\n" +
                        "Blood clotting and platelet activation are part of the hemostatic response, not the immune response.\n\n" +
                        "Rapid response to immediate threats (Incorrect)\n" +
                        "The primary immune response is slower as the body is exposed to a new pathogen. The secondary immune response is the faster, more immediate response after previous exposure."
        );

        questions.add("What is the purpose of macrophages in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Act as antigen-presenting cells", // Correct answer
                "Produce antibodies",
                "Filter excess water from the lymph",
                "Secrete hormones to control lymphatic flow"
        )));
        correctAnswers.add("Act as antigen-presenting cells");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Act as antigen-presenting cells (Correct answer)\n" +
                        "Macrophages are key antigen-presenting cells that engulf pathogens and present their antigens to activate T-cells and initiate the immune response.\n\n" +
                        "Produce antibodies (Incorrect)\n" +
                        "Macrophages do not produce antibodies. B-cells are responsible for antibody production.\n\n" +
                        "Filter excess water from the lymph (Incorrect)\n" +
                        "The primary function of macrophages is to act as phagocytes and present antigens, not to filter water from lymph.\n\n" +
                        "Secrete hormones to control lymphatic flow (Incorrect)\n" +
                        "Macrophages do not secrete hormones to control lymphatic flow."
        );

        questions.add("What is the role of the lymphatic system in maintaining fluid balance in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Removes excess interstitial fluid and returns it to the bloodstream", // Correct answer
                "Transports red blood cells",
                "Distributes oxygen throughout the body",
                "Regulates blood sugar levels"
        )));
        correctAnswers.add("Removes excess interstitial fluid and returns it to the bloodstream");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Removes excess interstitial fluid and returns it to the bloodstream (Correct answer)\n" +
                        "The lymphatic system helps maintain fluid balance by collecting excess interstitial fluid from tissues and returning it to the bloodstream as lymph.\n\n" +
                        "Transports red blood cells (Incorrect)\n" +
                        "The lymphatic system does not transport red blood cells; this is the function of the cardiovascular system.\n\n" +
                        "Distributes oxygen throughout the body (Incorrect)\n" +
                        "The lymphatic system does not distribute oxygen; this function is carried out by the cardiovascular system.\n\n" +
                        "Regulates blood sugar levels (Incorrect)\n" +
                        "The lymphatic system does not regulate blood sugar levels. Insulin regulation is the responsibility of the endocrine system."
        );

        questions.add("What is the result of a blocked thoracic duct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased blood pressure",
                "Swelling and lymphedema, especially in the lower body", // Correct answer
                "Severe headache",
                "Reduced production of lymphocytes"
        )));
        correctAnswers.add("Swelling and lymphedema, especially in the lower body");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Increased blood pressure (Incorrect)\n" +
                        "While lymphatic blockage can cause swelling, it does not directly cause increased blood pressure.\n\n" +
                        "Swelling and lymphedema, especially in the lower body (Correct answer)\n" +
                        "A blocked thoracic duct can lead to lymphedema, especially in the lower body, as it impairs the return of lymph from the lower extremities.\n\n" +
                        "Severe headache (Incorrect)\n" +
                        "A blocked thoracic duct is more likely to cause swelling rather than a headache.\n\n" +
                        "Reduced production of lymphocytes (Incorrect)\n" +
                        "A blocked thoracic duct affects lymph flow, but it does not directly reduce the production of lymphocytes."
        );

        questions.add("Which condition is most commonly associated with lymphatic cancer?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Leukemia",
                "Lymphoma", // Correct answer
                "Anemia",
                "Tuberculosis"
        )));
        correctAnswers.add("Lymphoma");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Lymphoma (Correct answer)\n" +
                        "Lymphoma is a cancer of the lymphatic system, specifically the lymph nodes and lymphatic tissues.\n\n" +
                        "Leukemia (Incorrect)\n" +
                        "Leukemia is cancer of the blood and bone marrow, not of the lymphatic system.\n\n" +
                        "Anemia (Incorrect)\n" +
                        "Anemia is a condition involving a deficiency of red blood cells or hemoglobin, not a type of lymphatic cancer.\n\n" +
                        "Tuberculosis (Incorrect)\n" +
                        "Tuberculosis is a bacterial infection, not a type of cancer."
        );

        questions.add("Which type of immune response is carried out by the B-cells in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cell-mediated immunity",
                "Humoral immunity", // Correct answer
                "Phagocytosis",
                "Antigen presentation"
        )));
        correctAnswers.add("Humoral immunity");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Humoral immunity (Correct answer)\n" +
                        "Humoral immunity is mediated by B-cells, which produce antibodies that target pathogens in body fluids.\n\n" +
                        "Cell-mediated immunity (Incorrect)\n" +
                        "Cell-mediated immunity is primarily carried out by T-cells, not B-cells.\n\n" +
                        "Phagocytosis (Incorrect)\n" +
                        "Phagocytosis is carried out by macrophages and other phagocytes, not B-cells.\n\n" +
                        "Antigen presentation (Incorrect)\n" +
                        "Antigen presentation is primarily performed by macrophages and dendritic cells, not B-cells."
        );

        questions.add("Which organ is NOT involved in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thymus",
                "Kidney", // Correct answer
                "Lymph nodes",
                "Spleen"
        )));
        correctAnswers.add("Kidney");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Kidney (Correct answer)\n" +
                        "The kidney is not involved in the lymphatic system. It plays a role in filtering blood and regulating fluid balance, but not in the immune function provided by the lymphatic system.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is an essential lymphatic organ that is involved in the maturation of T-cells, a crucial component of the immune system.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes are key structures in the lymphatic system that filter lymph and act as sites for immune cell activation.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is involved in filtering blood, recycling iron, and supporting the immune system, making it an important organ in the lymphatic system."
        );

        questions.add("What is the primary function of the lymphatic capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorb fats",
                "Transport red blood cells",
                "Collect interstitial fluid and return it to the bloodstream", // Correct answer
                "Produce white blood cells"
        )));
        correctAnswers.add("Collect interstitial fluid and return it to the bloodstream");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Collect interstitial fluid and return it to the bloodstream (Correct answer)\n" +
                        "Lymphatic capillaries absorb interstitial fluid, which is then returned to the bloodstream as lymph, helping maintain fluid balance in the body.\n\n" +
                        "Absorb fats (Incorrect)\n" +
                        "While lymphatic capillaries (specifically lacteals) absorb fats from the digestive system, the primary function of lymphatic capillaries is to collect interstitial fluid.\n\n" +
                        "Transport red blood cells (Incorrect)\n" +
                        "Lymphatic capillaries do not transport red blood cells. This is the role of the cardiovascular system.\n\n" +
                        "Produce white blood cells (Incorrect)\n" +
                        "White blood cells are produced in the bone marrow, not in the lymphatic capillaries."
        );

        questions.add("What condition can occur due to the blockage or removal of lymph nodes, resulting in the accumulation of lymph fluid?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myocardial infarction",
                "Lymphangitis",
                "Lymphedema", // Correct answer
                "Cirrhosis"
        )));
        correctAnswers.add("Lymphedema");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Lymphedema (Correct answer)\n" +
                        "Lymphedema occurs when lymphatic fluid builds up in the tissues, usually due to the blockage or removal of lymph nodes, leading to swelling in the affected area.\n\n" +
                        "Myocardial infarction (Incorrect)\n" +
                        "A myocardial infarction is a heart attack, caused by a blockage of blood flow to the heart, not related to the lymphatic system.\n\n" +
                        "Lymphangitis (Incorrect)\n" +
                        "Lymphangitis is the inflammation of lymphatic vessels, typically due to an infection, but it is not specifically caused by lymph node blockage.\n\n" +
                        "Cirrhosis (Incorrect)\n" +
                        "Cirrhosis is a condition affecting the liver, not the lymphatic system. It involves the scarring and dysfunction of liver tissue."
        );

        questions.add("The lymphatic system is a major player in which of the following processes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digestion",
                "Immune defense", // Correct answer
                "Metabolism",
                "Bone formation"
        )));
        correctAnswers.add("Immune defense");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Immune defense (Correct answer)\n" +
                        "The lymphatic system plays a central role in immune defense by producing lymphocytes, filtering pathogens, and supporting immune responses.\n\n" +
                        "Digestion (Incorrect)\n" +
                        "While the lymphatic system helps absorb fats in the digestive system (via lacteals), it is not primarily responsible for digestion itself.\n\n" +
                        "Metabolism (Incorrect)\n" +
                        "Metabolism refers to the biochemical processes in cells for converting food into energy, and this is managed by the endocrine system, not the lymphatic system.\n\n" +
                        "Bone formation (Incorrect)\n" +
                        "Bone formation is primarily managed by the skeletal system and osteoblasts, not the lymphatic system."
        );

        questions.add("What happens when the immune system mistakes normal tissue for pathogens in autoimmune lymphatic diseases?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The body overproduces lymphocytes",
                "Lymph fluid accumulates in tissues",
                "The immune system attacks the body's own tissues", // Correct answer
                "There is a decrease in antibody production"
        )));
        correctAnswers.add("The immune system attacks the body's own tissues");
        rationales.put(74,
                "RATIONALE:\n" +
                        "The immune system attacks the body's own tissues (Correct answer)\n" +
                        "In autoimmune lymphatic diseases, the immune system mistakenly identifies the body’s own tissues as foreign and begins to attack them, leading to inflammation and damage.\n\n" +
                        "The body overproduces lymphocytes (Incorrect)\n" +
                        "Lymphocyte overproduction can occur in certain conditions, but it is not the direct result of autoimmune disease where normal tissue is attacked.\n\n" +
                        "Lymph fluid accumulates in tissues (Incorrect)\n" +
                        "Lymphedema occurs due to lymphatic obstruction rather than being directly caused by autoimmune diseases.\n\n" +
                        "There is a decrease in antibody production (Incorrect)\n" +
                        "In many autoimmune conditions, antibody production can increase, as the immune system targets normal tissues."
        );

        questions.add("Which part of the lymphatic system is responsible for draining lymph from the left side of the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thoracic duct", // Correct answer
                "Right lymphatic duct",
                "Lymph nodes",
                "Spleen"
        )));
        correctAnswers.add("Thoracic duct");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Thoracic duct (Correct answer)\n" +
                        "The thoracic duct is responsible for draining lymph from the left side of the body, including the left arm, left side of the chest, abdomen, and lower body.\n\n" +
                        "Right lymphatic duct (Incorrect)\n" +
                        "The right lymphatic duct drains lymph from the right side of the head, neck, chest, and right arm, but not the left side.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes are involved in filtering lymph, but they do not drain lymph from different regions of the body.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen filters blood but does not drain lymph from specific regions of the body."
        );

        questions.add("The process of lymphocyte activation involves:");
        choices.add(new ArrayList<>(Arrays.asList(
                "B-cells producing antibodies",
                "T-cells killing infected cells",
                "Both A and B", // Correct answer
                "Phagocytosis of bacteria"
        )));
        correctAnswers.add("Both A and B");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Both A and B (Correct answer)\n" +
                        "Lymphocyte activation involves both B-cells, which produce antibodies, and T-cells, which are involved in killing infected cells and assisting with immune responses.\n\n" +
                        "B-cells producing antibodies (Incorrect)\n" +
                        "B-cells produce antibodies as part of the humoral immune response, but the activation process also involves T-cells.\n\n" +
                        "T-cells killing infected cells (Incorrect)\n" +
                        "T-cells are activated and involved in killing infected cells, but both B-cells and T-cells are part of the overall lymphocyte activation process.\n\n" +
                        "Phagocytosis of bacteria (Incorrect)\n" +
                        "Phagocytosis is carried out by macrophages and other phagocytes, not directly by lymphocytes."
        );

        questions.add("What is the term used for the inflammation of lymphatic vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphadenopathy",
                "Lymphangitis", // Correct answer
                "Lymphoma",
                "Lymphedema"
        )));
        correctAnswers.add("Lymphangitis");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Lymphangitis (Correct answer)\n" +
                        "Lymphangitis is the inflammation of lymphatic vessels, often due to infection or injury.\n\n" +
                        "Lymphadenopathy (Incorrect)\n" +
                        "Lymphadenopathy refers to enlarged lymph nodes, not the inflammation of lymphatic vessels.\n\n" +
                        "Lymphoma (Incorrect)\n" +
                        "Lymphoma is a type of cancer affecting lymphatic tissues, not an inflammatory condition of the vessels.\n\n" +
                        "Lymphedema (Incorrect)\n" +
                        "Lymphedema is the condition of fluid accumulation in tissues, not the inflammation of lymphatic vessels."
        );

        questions.add("Which of the following is a characteristic of lymph fluid?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Clear and watery", // Correct answer
                "Red and viscous",
                "Thick and yellow",
                "Black and tar-like"
        )));
        correctAnswers.add("Clear and watery");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Clear and watery (Correct answer)\n" +
                        "Lymph fluid is typically clear and watery in appearance, and it contains water, proteins, and white blood cells.\n\n" +
                        "Red and viscous (Incorrect)\n" +
                        "Lymph fluid is not red or viscous like blood.\n\n" +
                        "Thick and yellow (Incorrect)\n" +
                        "Lymph fluid can appear milky when it contains absorbed fats but is not typically thick and yellow.\n\n" +
                        "Black and tar-like (Incorrect)\n" +
                        "Lymph fluid is not black or tar-like; it is generally clear."
        );

        questions.add("Where do T-cells mature after being produced in the bone marrow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thymus", // Correct answer
                "Lymph nodes",
                "Bone marrow",
                "Spleen"
        )));
        correctAnswers.add("Thymus");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Thymus (Correct answer)\n" +
                        "After being produced in the bone marrow, T-cells mature in the thymus where they are trained to distinguish between self and non-self.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "While lymph nodes are sites for immune activation, T-cells mature in the thymus, not in the lymph nodes.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "T-cells are produced in the bone marrow but do not mature there.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen helps filter blood and supports immune functions but is not the maturation site for T-cells."
        );

        questions.add("Which of the following is a function of the spleen in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stores bile",
                "Filters blood and removes old red blood cells", // Correct answer
                "Transports lymph fluid",
                "Produces red blood cells"
        )));
        correctAnswers.add("Filters blood and removes old red blood cells");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Filters blood and removes old red blood cells (Correct answer)\n" +
                        "The spleen filters blood, removing old red blood cells, and plays a role in the immune system by storing white blood cells.\n\n" +
                        "Stores bile (Incorrect)\n" +
                        "The liver stores bile, not the spleen.\n\n" +
                        "Transports lymph fluid (Incorrect)\n" +
                        "The spleen does not transport lymph fluid. Lymph is transported through the lymphatic vessels.\n\n" +
                        "Produces red blood cells (Incorrect)\n" +
                        "While the spleen produces red blood cells during fetal development, it does not do so in adults."
        );

        questions.add("What is the primary cause of elephantiasis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bacterial infection of the lymph nodes",
                "Chronic inflammation of lymphatic vessels due to parasitic infection", // Correct answer
                "Genetic mutations in lymphatic cells",
                "Overproduction of lymph fluid in the body"
        )));
        correctAnswers.add("Chronic inflammation of lymphatic vessels due to parasitic infection");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Chronic inflammation of lymphatic vessels due to parasitic infection (Correct answer)\n" +
                        "Elephantiasis is caused by filariasis, a parasitic infection where worms block lymphatic vessels, leading to severe swelling and lymphatic obstruction.\n\n" +
                        "Bacterial infection of the lymph nodes (Incorrect)\n" +
                        "Elephantiasis is not caused by bacterial infection but by parasitic infections that cause chronic inflammation of lymphatic vessels.\n\n" +
                        "Genetic mutations in lymphatic cells (Incorrect)\n" +
                        "While genetic mutations can affect the lymphatic system, elephantiasis is caused by parasitic infection, not genetic mutations.\n\n" +
                        "Overproduction of lymph fluid in the body (Incorrect)\n" +
                        "Overproduction of lymph fluid is not the cause of elephantiasis, which is linked to blockage and infection in lymphatic vessels."
        );

        questions.add("What happens when a lymph node becomes swollen and tender?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The lymph node is in a resting state",
                "The body is fighting off an infection or inflammation", // Correct answer
                "The immune system has stopped working",
                "Lymph nodes are no longer functioning"
        )));
        correctAnswers.add("The body is fighting off an infection or inflammation");
        rationales.put(82,
                "RATIONALE:\n" +
                        "The body is fighting off an infection or inflammation (Correct answer)\n" +
                        "Swollen and tender lymph nodes are often a sign that the immune system is responding to an infection or inflammation in the body.\n\n" +
                        "The lymph node is in a resting state (Incorrect)\n" +
                        "A swollen lymph node is actively responding to an infection or inflammation, not in a resting state.\n\n" +
                        "The immune system has stopped working (Incorrect)\n" +
                        "Swelling and tenderness of lymph nodes indicate an active immune response, not that the immune system has stopped working.\n\n" +
                        "Lymph nodes are no longer functioning (Incorrect)\n" +
                        "Swollen lymph nodes are functioning by filtering lymph and activating immune cells in response to infection."
        );

        questions.add("Which of the following best describes the lymphatic capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They are larger than regular capillaries and have no valves",
                "They have walls that allow the entry of large molecules like proteins and fats", // Correct answer
                "They are composed of smooth muscle cells",
                "They only transport red blood cells"
        )));
        correctAnswers.add("They have walls that allow the entry of large molecules like proteins and fats");
        rationales.put(83,
                "RATIONALE:\n" +
                        "They have walls that allow the entry of large molecules like proteins and fats (Correct answer)\n" +
                        "Lymphatic capillaries have thin walls that allow large molecules, such as proteins and fats, to enter and form lymph.\n\n" +
                        "They are larger than regular capillaries and have no valves (Incorrect)\n" +
                        "Lymphatic capillaries are actually smaller than regular capillaries and have valves to ensure one-way flow.\n\n" +
                        "They are composed of smooth muscle cells (Incorrect)\n" +
                        "Lymphatic capillaries are not composed of smooth muscle; they are made of a single layer of endothelial cells.\n\n" +
                        "They only transport red blood cells (Incorrect)\n" +
                        "Lymphatic capillaries do not transport red blood cells; they transport lymph, which includes proteins, fats, and white blood cells."
        );

        questions.add("What is the medical term for the removal of lymph nodes during cancer treatment?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphadenopathy",
                "Lymphangiography",
                "Lymphadenectomy", // Correct answer
                "Lymphorrhea"
        )));
        correctAnswers.add("Lymphadenectomy");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Lymphadenectomy (Correct answer)\n" +
                        "Lymphadenectomy is the surgical removal of lymph nodes, often done in cancer treatment to prevent the spread of cancer cells.\n\n" +
                        "Lymphadenopathy (Incorrect)\n" +
                        "Lymphadenopathy refers to the enlargement or disease of lymph nodes, not their removal.\n\n" +
                        "Lymphangiography (Incorrect)\n" +
                        "Lymphangiography is a diagnostic imaging procedure used to visualize the lymphatic system, not a surgical procedure.\n\n" +
                        "Lymphorrhea (Incorrect)\n" +
                        "Lymphorrhea refers to the abnormal leakage of lymph from the lymphatic vessels, not the removal of lymph nodes."
        );

        questions.add("Which type of immunity is provided by the lymphatic system when it responds to a pathogen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Innate immunity",
                "Adaptive immunity", // Correct answer
                "Passive immunity",
                "Acquired immunity"
        )));
        correctAnswers.add("Adaptive immunity");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Adaptive immunity (Correct answer)\n" +
                        "The lymphatic system is central to adaptive immunity, which involves specific responses to pathogens, including the activation of T-cells and B-cells.\n\n" +
                        "Innate immunity (Incorrect)\n" +
                        "Innate immunity is the body’s initial, nonspecific response to pathogens and is not directly provided by the lymphatic system.\n\n" +
                        "Passive immunity (Incorrect)\n" +
                        "Passive immunity is acquired through the transfer of antibodies from another individual (e.g., breast milk), not through the lymphatic system's response to pathogens.\n\n" +
                        "Acquired immunity (Incorrect)\n" +
                        "While acquired immunity is a result of the body's adaptive response to infections, it is more specifically referred to as adaptive immunity in this context."
        );

        questions.add("Which of the following is true about the role of the lymphatic system in cancer?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph nodes often become a site for cancer cells to spread", // Correct answer
                "The lymphatic system completely prevents cancer cell growth",
                "Lymphatic vessels never carry cancer cells",
                "The lymphatic system causes cancerous growths"
        )));
        correctAnswers.add("Lymph nodes often become a site for cancer cells to spread");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Lymph nodes often become a site for cancer cells to spread (Correct answer)\n" +
                        "Lymph nodes are common sites for cancer cells to spread, especially in cancers such as breast cancer and melanoma.\n\n" +
                        "The lymphatic system completely prevents cancer cell growth (Incorrect)\n" +
                        "While the lymphatic system plays a role in immune defense, it does not prevent cancer cell growth.\n\n" +
                        "Lymphatic vessels never carry cancer cells (Incorrect)\n" +
                        "Lymphatic vessels can carry cancer cells from one site to another, leading to metastasis.\n\n" +
                        "The lymphatic system causes cancerous growths (Incorrect)\n" +
                        "The lymphatic system does not cause cancerous growths; however, cancer can spread through the lymphatic system."
        );

        questions.add("The process by which lymphocytes respond to a foreign antigen involves:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The release of digestive enzymes",
                "The activation of phagocytic cells",
                "The production of antibodies", // Correct answer
                "The secretion of hormones"
        )));
        correctAnswers.add("The production of antibodies");
        rationales.put(87,
                "RATIONALE:\n" +
                        "The production of antibodies (Correct answer)\n" +
                        "B-cells of the lymphatic system produce antibodies to neutralize and mark foreign antigens for destruction.\n\n" +
                        "The release of digestive enzymes (Incorrect)\n" +
                        "Lymphocytes do not release digestive enzymes to respond to foreign antigens.\n\n" +
                        "The activation of phagocytic cells (Incorrect)\n" +
                        "Phagocytic cells (like macrophages) assist in immune defense but do not involve lymphocytes in antigen response.\n\n" +
                        "The secretion of hormones (Incorrect)\n" +
                        "Lymphocytes do not secrete hormones to respond to antigens."
        );

        questions.add("What is the function of the tonsils in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Production of red blood cells",
                "Filter bacteria from the air and food entering the body", // Correct answer
                "Absorption of dietary fats",
                "Production of insulin"
        )));
        correctAnswers.add("Filter bacteria from the air and food entering the body");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Filter bacteria from the air and food entering the body (Correct answer)\n" +
                        "Tonsils help filter and trap bacteria and other pathogens from the air and food before they enter the body.\n\n" +
                        "Production of red blood cells (Incorrect)\n" +
                        "The tonsils do not produce red blood cells; that function is carried out by the bone marrow.\n\n" +
                        "Absorption of dietary fats (Incorrect)\n" +
                        "The tonsils do not absorb dietary fats; this function is performed by the lymphatic vessels in the digestive system.\n\n" +
                        "Production of insulin (Incorrect)\n" +
                        "Insulin is produced by the pancreas, not the tonsils."
        );

        questions.add("The primary function of the lymphatic system is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate body temperature",
                "Transport oxygen to cells",
                "Drain excess interstitial fluid and help fight infection", // Correct answer
                "Supply nutrients to tissues"
        )));
        correctAnswers.add("Drain excess interstitial fluid and help fight infection");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Drain excess interstitial fluid and help fight infection (Correct answer)\n" +
                        "The primary function of the lymphatic system is to remove excess fluid from tissues and aid in immune responses to fight infections.\n\n" +
                        "Regulate body temperature (Incorrect)\n" +
                        "While the lymphatic system contributes to overall homeostasis, it is not primarily responsible for regulating body temperature.\n\n" +
                        "Transport oxygen to cells (Incorrect)\n" +
                        "Oxygen transport is handled by the circulatory system, not the lymphatic system.\n\n" +
                        "Supply nutrients to tissues (Incorrect)\n" +
                        "The circulatory system is responsible for supplying nutrients to tissues, not the lymphatic system."
        );

        questions.add("Which of the following is NOT a major component of the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymph",
                "Lymph nodes",
                "Spleen",
                "Pituitary gland" // Correct answer
        )));
        correctAnswers.add("Pituitary gland");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Pituitary gland (Correct answer)\n" +
                        "The pituitary gland is part of the endocrine system, not the lymphatic system.\n\n" +
                        "Lymph (Incorrect)\n" +
                        "Lymph is a major component of the lymphatic system, as it transports immune cells and waste products.\n\n" +
                        "Lymph nodes (Incorrect)\n" +
                        "Lymph nodes are critical parts of the lymphatic system involved in filtering lymph and activating immune responses.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen is an important component of the lymphatic system responsible for filtering blood and storing white blood cells."
        );

        questions.add("What is the term for the swelling caused by a blockage in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lymphadenitis",
                "Lymphedema", // Correct answer
                "Lymphangitis",
                "Lymphoma"
        )));
        correctAnswers.add("Lymphedema");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Lymphedema (Correct answer)\n" +
                        "Lymphedema is the swelling caused by blockage or damage to the lymphatic system, often due to infection, injury, or surgical removal of lymph nodes.\n\n" +
                        "Lymphadenitis (Incorrect)\n" +
                        "Lymphadenitis refers to the inflammation of lymph nodes, not the swelling caused by blockage of lymphatic vessels.\n\n" +
                        "Lymphangitis (Incorrect)\n" +
                        "Lymphangitis is the inflammation of the lymphatic vessels, not a result of blockage.\n\n" +
                        "Lymphoma (Incorrect)\n" +
                        "Lymphoma is a cancer of the lymphatic system, not swelling due to blockage."
        );

        questions.add("Which condition is caused by a malfunction of the immune system, leading to an attack on the body’s own tissues?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Autoimmune disease", // Correct answer
                "Lymphangitis",
                "Lymphedema",
                "Leukemia"
        )));
        correctAnswers.add("Autoimmune disease");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Autoimmune disease (Correct answer)\n" +
                        "An autoimmune disease occurs when the immune system mistakenly attacks healthy tissues in the body, such as in conditions like rheumatoid arthritis or lupus.\n\n" +
                        "Lymphangitis (Incorrect)\n" +
                        "Lymphangitis is the inflammation of lymphatic vessels, often due to infection, not an immune system malfunction.\n\n" +
                        "Lymphedema (Incorrect)\n" +
                        "Lymphedema is caused by a blockage or disruption in the lymphatic system, not by an immune system malfunction.\n\n" +
                        "Leukemia (Incorrect)\n" +
                        "Leukemia is a type of cancer of the blood and bone marrow, not an autoimmune disorder."
        );

        questions.add("Which of the following is a key role of the lymphatic vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To distribute oxygen to tissues",
                "To carry absorbed fats from the digestive system to the bloodstream", // Correct answer
                "To produce red blood cells",
                "To pump blood throughout the body"
        )));
        correctAnswers.add("To carry absorbed fats from the digestive system to the bloodstream");
        rationales.put(93,
                "RATIONALE:\n" +
                        "To carry absorbed fats from the digestive system to the bloodstream (Correct answer)\n" +
                        "Lymphatic vessels transport lipids (fats) absorbed from the digestive system to the bloodstream, specifically through the lacteals in the small intestine.\n\n" +
                        "To distribute oxygen to tissues (Incorrect)\n" +
                        "Oxygen distribution is handled by the circulatory system, not the lymphatic vessels.\n\n" +
                        "To produce red blood cells (Incorrect)\n" +
                        "Red blood cells are produced in the bone marrow, not by the lymphatic vessels.\n\n" +
                        "To pump blood throughout the body (Incorrect)\n" +
                        "The circulatory system is responsible for pumping blood, not the lymphatic system."
        );

        questions.add("What is a common complication of lymphedema?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Poor blood circulation",
                "Increased risk of infections due to fluid buildup", // Correct answer
                "Dehydration",
                "Hyperactivity of the immune system"
        )));
        correctAnswers.add("Increased risk of infections due to fluid buildup");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Increased risk of infections due to fluid buildup (Correct answer)\n" +
                        "The swelling in lymphedema can create a favorable environment for bacterial infections as fluid buildup can compromise the body's ability to fight infections.\n\n" +
                        "Poor blood circulation (Incorrect)\n" +
                        "Lymphedema primarily affects the lymphatic system, leading to fluid buildup and swelling, but not necessarily poor blood circulation.\n\n" +
                        "Dehydration (Incorrect)\n" +
                        "Lymphedema does not directly cause dehydration; it is more about fluid retention.\n\n" +
                        "Hyperactivity of the immune system (Incorrect)\n" +
                        "Lymphedema is associated with impaired lymphatic function, not hyperactivity of the immune system."
        );

        questions.add("In the lymphatic system, what is the name of the clear fluid that transports immune cells, proteins, and waste products?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood plasma",
                "Interstitial fluid",
                "Lymph", // Correct answer
                "Cerebrospinal fluid"
        )));
        correctAnswers.add("Lymph");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Lymph (Correct answer)\n" +
                        "Lymph is a clear fluid that transports immune cells, proteins, and waste products within the lymphatic system.\n\n" +
                        "Blood plasma (Incorrect)\n" +
                        "Blood plasma is the fluid component of blood, not lymph.\n\n" +
                        "Interstitial fluid (Incorrect)\n" +
                        "Interstitial fluid is the fluid found between cells, which is eventually collected by the lymphatic system to form lymph.\n\n" +
                        "Cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid surrounds the brain and spinal cord, not the lymphatic system."
        );

        questions.add("Which lymphatic structure is responsible for filtering and removing foreign particles, bacteria, and viruses from the lymph?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thymus",
                "Lymph nodes", // Correct answer
                "Bone marrow",
                "Spleen"
        )));
        correctAnswers.add("Lymph nodes");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Lymph nodes (Correct answer)\n" +
                        "Lymph nodes filter lymph, removing foreign particles, bacteria, and viruses, and play a key role in immune responses.\n\n" +
                        "Thymus (Incorrect)\n" +
                        "The thymus is involved in the maturation of T-cells, but it does not filter lymph.\n\n" +
                        "Bone marrow (Incorrect)\n" +
                        "The bone marrow produces lymphocytes but does not filter lymph.\n\n" +
                        "Spleen (Incorrect)\n" +
                        "The spleen filters blood (not lymph) and removes old red blood cells."
        );

        questions.add("What is the role of the bone marrow in the lymphatic system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Maturation of T-cells",
                "Production of red blood cells",
                "Production of lymphocytes (B-cells and T-cells)", // Correct answer
                "Filtering harmful substances from the blood"
        )));
        correctAnswers.add("Production of lymphocytes (B-cells and T-cells)");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Production of lymphocytes (B-cells and T-cells) (Correct answer)\n" +
                        "The bone marrow produces lymphocytes, which include B-cells (responsible for producing antibodies) and T-cells (involved in cellular immunity).\n\n" +
                        "Maturation of T-cells (Incorrect)\n" +
                        "T-cells mature in the thymus, not in the bone marrow.\n\n" +
                        "Production of red blood cells (Incorrect)\n" +
                        "While bone marrow produces red blood cells, its role in the lymphatic system is the production of lymphocytes.\n\n" +
                        "Filtering harmful substances from the blood (Incorrect)\n" +
                        "The bone marrow does not filter the blood; this function is performed by organs like the spleen and liver."
        );

        questions.add("What is the primary function of the thoracic duct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To carry lymph from the right side of the body",
                "To drain lymph from the left side of the body and return it to the bloodstream", // Correct answer
                "To filter harmful substances from the lymph",
                "To produce antibodies in the blood"
        )));
        correctAnswers.add("To drain lymph from the left side of the body and return it to the bloodstream");
        rationales.put(98,
                "RATIONALE:\n" +
                        "To drain lymph from the left side of the body and return it to the bloodstream (Correct answer)\n" +
                        "The thoracic duct drains lymph from the left side of the body, returning it to the bloodstream at the left subclavian vein.\n\n" +
                        "To carry lymph from the right side of the body (Incorrect)\n" +
                        "The right lymphatic duct drains lymph from the right side of the body, not the thoracic duct.\n\n" +
                        "To filter harmful substances from the lymph (Incorrect)\n" +
                        "The thoracic duct transports lymph, but it does not filter harmful substances (this is done by lymph nodes).\n\n" +
                        "To produce antibodies in the blood (Incorrect)\n" +
                        "Antibody production occurs in B-cells of the lymphatic system, not in the thoracic duct."
        );

        questions.add("Which of the following is a potential consequence of untreated or chronic lymphatic obstruction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decreased heart rate",
                "Lymphatic cancer",
                "Progressive swelling and tissue damage", // Correct answer
                "Loss of muscle mass"
        )));
        correctAnswers.add("Progressive swelling and tissue damage");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Progressive swelling and tissue damage (Correct answer)\n" +
                        "Chronic lymphatic obstruction leads to progressive swelling (lymphedema) and tissue damage due to impaired fluid drainage.\n\n" +
                        "Decreased heart rate (Incorrect)\n" +
                        "Lymphatic obstruction does not directly affect the heart rate.\n\n" +
                        "Lymphatic cancer (Incorrect)\n" +
                        "Lymphatic obstruction does not directly lead to lymphatic cancer.\n\n" +
                        "Loss of muscle mass (Incorrect)\n" +
                        "While muscle mass can be affected by immobility, it is not a direct consequence of lymphatic obstruction."
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
        new AlertDialog.Builder(ChallengeMode9.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
