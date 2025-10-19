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

public class ChallengeMode5 extends AppCompatActivity {

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

        setContentView(R.layout.challenge_mode5);

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
                Toast.makeText(ChallengeMode5.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode5.this)
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
            new AlertDialog.Builder(ChallengeMode5.this)
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
                new AlertDialog.Builder(ChallengeMode5.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode5.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Respiratory System", correctAnswersCount, totalQuestions);


                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Respiratory System");
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
                        Intent intent = new Intent(ChallengeMode5.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode5.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode5.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Respiratory System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Respiratory System");
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
        new AlertDialog.Builder(ChallengeMode5.this)
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


        questions.add("What is the primary function of the respiratory system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate hormones",
                "Circulate blood",
                "Facilitate gas exchange", // Correct answer
                "Digest nutrients"
        )));
        correctAnswers.add("Facilitate gas exchange");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Facilitate gas exchange (Correct answer)\n" +
                        "The respiratory system exchanges oxygen and carbon dioxide between air and blood.\n\n" +
                        "Regulate hormones (Incorrect)\n" +
                        "Endocrine system regulates hormones.\n\n" +
                        "Circulate blood (Incorrect)\n" +
                        "Circulatory system (heart, vessels) handles this.\n\n" +
                        "Digest nutrients (Incorrect)\n" +
                        "Digestive system performs nutrient breakdown."
        );

        questions.add("Which of the following structures is part of the upper respiratory tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Trachea",
                "Alveoli",
                "Nasal cavity", // Correct answer
                "Lungs"
        )));
        correctAnswers.add("Nasal cavity");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Nasal cavity (Correct answer)\n" +
                        "Upper respiratory tract includes nasal cavity, pharynx, and larynx.\n\n" +
                        "Trachea (Incorrect)\n" +
                        "Lower respiratory tract.\n\n" +
                        "Alveoli (Incorrect)\n" +
                        "Found in the lungs, part of lower tract.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "Lower respiratory tract."
        );

        questions.add("The nasal cavity is divided by the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Soft palate",
                "Conchae",
                "Nasal septum", // Correct answer
                "Epiglottis"
        )));
        correctAnswers.add("Nasal septum");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Nasal septum (Correct answer)\n" +
                        "A cartilage and bone partition dividing nasal cavity into left and right sides.\n\n" +
                        "Soft palate (Incorrect)\n" +
                        "Separates oral and nasal cavities but not the nasal cavity itself.\n\n" +
                        "Conchae (Incorrect)\n" +
                        "Increase surface area but don't divide cavity.\n\n" +
                        "Epiglottis (Incorrect)\n" +
                        "Guards trachea during swallowing."
        );

        questions.add("Which structure prevents food from entering the trachea?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Uvula",
                "Larynx",
                "Epiglottis", // Correct answer
                "Pharynx"
        )));
        correctAnswers.add("Epiglottis");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Epiglottis (Correct answer)\n" +
                        "A flap of tissue that covers the trachea when swallowing.\n\n" +
                        "Uvula (Incorrect)\n" +
                        "Prevents food from entering nasal cavity.\n\n" +
                        "Larynx (Incorrect)\n" +
                        "Contains vocal cords, not for guarding airway during swallowing.\n\n" +
                        "Pharynx (Incorrect)\n" +
                        "Passageway for air and food, not protective."
        );

        questions.add("What is the function of the pleural cavity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pump oxygen into blood",
                "Facilitate swallowing",
                "Create negative pressure to keep lungs expanded", // Correct answer
                "Filter air"
        )));
        correctAnswers.add("Create negative pressure to keep lungs expanded");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Create negative pressure to keep lungs expanded (Correct answer)\n" +
                        "The pleural cavity allows lung expansion during breathing.\n\n" +
                        "Pump oxygen into blood (Incorrect)\n" +
                        "Lungs and alveoli manage this, not pleural cavity.\n\n" +
                        "Facilitate swallowing (Incorrect)\n" +
                        "Involves mouth, pharynx, esophagus.\n\n" +
                        "Filter air (Incorrect)\n" +
                        "Nasal cavity and cilia handle filtration."
        );

        questions.add("Gas exchange occurs in the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bronchi",
                "Trachea",
                "Alveoli", // Correct answer
                "Larynx"
        )));
        correctAnswers.add("Alveoli");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Alveoli (Correct answer)\n" +
                        "Microscopic air sacs where oxygen and CO₂ exchange occurs.\n\n" +
                        "Bronchi (Incorrect)\n" +
                        "Conduct air, no gas exchange.\n\n" +
                        "Trachea (Incorrect)\n" +
                        "Air passage only.\n\n" +
                        "Larynx (Incorrect)\n" +
                        "Produces sound, no gas exchange."
        );

        questions.add("What muscle is primarily responsible for inspiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Intercostals",
                "Diaphragm", // Correct answer
                "Rectus abdominis",
                "Sternocleidomastoid"
        )));
        correctAnswers.add("Diaphragm");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Diaphragm (Correct answer)\n" +
                        "Main muscle causing lung expansion by creating negative pressure.\n\n" +
                        "Intercostals (Incorrect)\n" +
                        "Assist in breathing but not primary.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Aids forced expiration.\n\n" +
                        "Sternocleidomastoid (Incorrect)\n" +
                        "Accessory muscle for deep breathing."
        );

        questions.add("Which part of the brain controls breathing rate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cerebrum",
                "Thalamus",
                "Medulla oblongata", // Correct answer
                "Cerebellum"
        )));
        correctAnswers.add("Medulla oblongata");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Medulla oblongata (Correct answer)\n" +
                        "Regulates autonomic breathing control.\n\n" +
                        "Cerebrum (Incorrect)\n" +
                        "Controls voluntary actions, not breathing reflex.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "Relays sensory signals.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "Coordinates balance and movement."
        );

        questions.add("The voice box is also called the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Trachea",
                "Pharynx",
                "Larynx", // Correct answer
                "Bronchus"
        )));
        correctAnswers.add("Larynx");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Larynx (Correct answer)\n" +
                        "Contains vocal cords, produces sound.\n\n" +
                        "Trachea (Incorrect)\n" +
                        "Windpipe below larynx.\n\n" +
                        "Pharynx (Incorrect)\n" +
                        "Throat area.\n\n" +
                        "Bronchus (Incorrect)\n" +
                        "Lower airway branch."
        );

        questions.add("Which of the following helps trap dust and pathogens in the nasal cavity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cilia", // Correct answer
                "Blood vessels",
                "Cartilage",
                "Sinuses"
        )));
        correctAnswers.add("Cilia");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Cilia (Correct answer)\n" +
                        "Hair-like structures that sweep debris trapped in mucus.\n\n" +
                        "Blood vessels (Incorrect)\n" +
                        "Warm air but don't trap debris.\n\n" +
                        "Cartilage (Incorrect)\n" +
                        "Supports structure, no filtration role.\n\n" +
                        "Sinuses (Incorrect)\n" +
                        "Produce mucus, but cilia clear it."
        );

        questions.add("What is the correct pathway of air into the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pharynx → Larynx → Trachea → Bronchi → Alveoli", // Correct answer
                "Larynx → Pharynx → Bronchi → Alveoli",
                "Trachea → Larynx → Bronchioles → Alveoli",
                "Nasal cavity → Bronchi → Trachea → Larynx"
        )));
        correctAnswers.add("Pharynx → Larynx → Trachea → Bronchi → Alveoli");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Pharynx → Larynx → Trachea → Bronchi → Alveoli (Correct answer)\n" +
                        "Follows natural airway route.\n\n" +
                        "Larynx → Pharynx → Bronchi → Alveoli (Incorrect)\n" +
                        "Larynx precedes pharynx incorrectly.\n\n" +
                        "Trachea → Larynx → Bronchioles → Alveoli (Incorrect)\n" +
                        "Trachea should follow larynx, not before.\n\n" +
                        "Nasal cavity → Bronchi → Trachea → Larynx (Incorrect)\n" +
                        "Nasal cavity should precede pharynx."
        );

        questions.add("Which of the following is a function of the respiratory system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Excretion of solid waste",
                "Regulation of temperature",
                "Regulation of blood pH", // Correct answer
                "Production of insulin"
        )));
        correctAnswers.add("Regulation of blood pH");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Regulation of blood pH (Correct answer)\n" +
                        "By controlling CO₂ levels.\n\n" +
                        "Excretion of solid waste (Incorrect)\n" +
                        "Digestive system.\n\n" +
                        "Regulation of temperature (Incorrect)\n" +
                        "Primarily integumentary and circulatory systems.\n\n" +
                        "Production of insulin (Incorrect)\n" +
                        "Endocrine function."
        );

        questions.add("Which structure conducts air from the larynx to the bronchi?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Esophagus",
                "Pharynx",
                "Trachea", // Correct answer
                "Diaphragm"
        )));
        correctAnswers.add("Trachea");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Trachea (Correct answer)\n" +
                        "Main airway tube.\n\n" +
                        "Esophagus (Incorrect)\n" +
                        "Carries food.\n\n" +
                        "Pharynx (Incorrect)\n" +
                        "Above larynx.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Muscle for breathing, not air conduction."
        );

        questions.add("The alveoli are lined with:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cartilage",
                "Smooth muscle",
                "Simple squamous epithelium", // Correct answer
                "Ciliated columnar cells"
        )));
        correctAnswers.add("Simple squamous epithelium");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Simple squamous epithelium (Correct answer)\n" +
                        "Thin layer for efficient gas exchange.\n\n" +
                        "Cartilage (Incorrect)\n" +
                        "Found in trachea and bronchi.\n\n" +
                        "Smooth muscle (Incorrect)\n" +
                        "Surround bronchioles, not alveoli.\n\n" +
                        "Ciliated columnar cells (Incorrect)\n" +
                        "Line upper airways."
        );

        questions.add("Surfactant in the lungs functions to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increase gas exchange rate",
                "Prevent alveolar collapse", // Correct answer
                "Increase blood flow",
                "Remove mucus"
        )));
        correctAnswers.add("Prevent alveolar collapse");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Prevent alveolar collapse (Correct answer)\n" +
                        "Reduces surface tension.\n\n" +
                        "Increase gas exchange rate (Incorrect)\n" +
                        "Indirectly helps, but main function is preventing collapse.\n\n" +
                        "Increase blood flow (Incorrect)\n" +
                        "Circulatory task.\n\n" +
                        "Remove mucus (Incorrect)\n" +
                        "Cilia and mucus manage this."
        );

        questions.add("Which gas is primarily carried as bicarbonate in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Oxygen",
                "Nitrogen",
                "Carbon dioxide", // Correct answer
                "Carbon monoxide"
        )));
        correctAnswers.add("Carbon dioxide");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Carbon dioxide (Correct answer)\n" +
                        "Mostly transported as bicarbonate ion.\n\n" +
                        "Oxygen (Incorrect)\n" +
                        "Mostly via hemoglobin.\n\n" +
                        "Nitrogen (Incorrect)\n" +
                        "Inert, not transported.\n\n" +
                        "Carbon monoxide (Incorrect)\n" +
                        "Binds hemoglobin, not as bicarbonate."
        );

        questions.add("Which molecule carries most of the oxygen in blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myoglobin",
                "Hemoglobin", // Correct answer
                "Platelets",
                "Albumin"
        )));
        correctAnswers.add("Hemoglobin");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Hemoglobin (Correct answer)\n" +
                        "Oxygen transport in red blood cells.\n\n" +
                        "Myoglobin (Incorrect)\n" +
                        "Oxygen storage in muscle.\n\n" +
                        "Platelets (Incorrect)\n" +
                        "Clotting role.\n\n" +
                        "Albumin (Incorrect)\n" +
                        "Carries other substances, not oxygen."
        );

        questions.add("What happens to the diaphragm during expiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It contracts",
                "It flattens",
                "It relaxes and rises", // Correct answer
                "It enlarges the thoracic cavity"
        )));
        correctAnswers.add("It relaxes and rises");
        rationales.put(17,
                "RATIONALE:\n" +
                        "It relaxes and rises (Correct answer)\n" +
                        "Decreases thoracic volume during exhalation.\n\n" +
                        "It contracts (Incorrect)\n" +
                        "Happens during inhalation.\n\n" +
                        "It flattens (Incorrect)\n" +
                        "Inhalation action.\n\n" +
                        "It enlarges the thoracic cavity (Incorrect)\n" +
                        "Inhalation effect."
        );

        questions.add("Which of the following is not part of the lower respiratory tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lungs",
                "Bronchi",
                "Trachea",
                "Nasal cavity" // Correct answer
        )));
        correctAnswers.add("Nasal cavity");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Nasal cavity (Correct answer)\n" +
                        "Upper tract.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "Lower tract.\n\n" +
                        "Bronchi (Incorrect)\n" +
                        "Lower tract.\n\n" +
                        "Trachea (Incorrect)\n" +
                        "Lower tract."
        );

        questions.add("What lines the trachea to help remove debris?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Smooth muscle",
                "Cartilage rings",
                "Mucus and cilia", // Correct answer
                "Elastic fibers"
        )));
        correctAnswers.add("Mucus and cilia");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Mucus and cilia (Correct answer)\n" +
                        "Trap and move debris out.\n\n" +
                        "Smooth muscle (Incorrect)\n" +
                        "Controls airway diameter, not debris removal.\n\n" +
                        "Cartilage rings (Incorrect)\n" +
                        "Maintain structure.\n\n" +
                        "Elastic fibers (Incorrect)\n" +
                        "Provide stretch, no debris role."
        );

        questions.add("Which term refers to the volume of air inhaled or exhaled during normal breathing?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Residual volume",
                "Inspiratory reserve volume",
                "Tidal volume",
                "Vital capacity"
        )));
        correctAnswers.add("Tidal volume");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Tidal volume (Correct answer)\n" +
                        "It’s the amount of air moved in or out of the lungs during normal, quiet breathing.\n\n" +
                        "Residual volume (Incorrect)\n" +
                        "This is the amount of air remaining in the lungs after a forceful expiration.\n\n" +
                        "Inspiratory reserve volume (Incorrect)\n" +
                        "This is the extra volume of air that can be forcibly inhaled after normal inspiration.\n\n" +
                        "Vital capacity (Incorrect)\n" +
                        "This is the maximum amount of air a person can expel from the lungs after a maximum inhalation."
        );

        questions.add("Which gas is most important for regulating respiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Oxygen",
                "Nitrogen",
                "Carbon dioxide",
                "Hydrogen"
        )));
        correctAnswers.add("Carbon dioxide");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Carbon dioxide (Correct answer)\n" +
                        "CO₂ levels affect blood pH and are detected by chemoreceptors, triggering changes in ventilation.\n\n" +
                        "Oxygen (Incorrect)\n" +
                        "Although vital, changes in oxygen levels influence respiration less acutely than carbon dioxide.\n\n" +
                        "Nitrogen (Incorrect)\n" +
                        "This inert gas has no direct effect on breathing regulation.\n\n" +
                        "Hydrogen (Incorrect)\n" +
                        "Indirectly influences breathing via pH but primarily in relation to CO₂ levels."
        );

        questions.add("The pharynx serves as a common passageway for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Air and food",
                "Blood and lymph",
                "Hormones and enzymes",
                "Gas and liquid waste"
        )));
        correctAnswers.add("Air and food");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Air and food (Correct answer)\n" +
                        "The pharynx serves both the respiratory and digestive systems.\n\n" +
                        "Blood and lymph (Incorrect)\n" +
                        "These circulate in vessels, not through the pharynx.\n\n" +
                        "Hormones and enzymes (Incorrect)\n" +
                        "These are transported via the blood, not through the pharynx.\n\n" +
                        "Gas and liquid waste (Incorrect)\n" +
                        "Eliminated by different systems (respiratory and urinary/digestive)."
        );

        questions.add("Which is a major muscle involved in forced expiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphragm",
                "Pectoralis major",
                "Internal intercostals",
                "Deltoid"
        )));
        correctAnswers.add("Internal intercostals");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Internal intercostals (Correct answer)\n" +
                        "These help force air out by pulling ribs down during forced expiration.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Primarily contracts during inspiration.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Mainly involved in arm and chest movement.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Responsible for moving the shoulder, unrelated to breathing."
        );

        questions.add("What part of the larynx produces sound?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epiglottis",
                "Glottis",
                "Vocal cords",
                "Arytenoid cartilage"
        )));
        correctAnswers.add("Vocal cords");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Vocal cords (Correct answer)\n" +
                        "They vibrate as air passes through, producing sound.\n\n" +
                        "Epiglottis (Incorrect)\n" +
                        "Prevents food from entering the trachea, not sound production.\n\n" +
                        "Glottis (Incorrect)\n" +
                        "The opening between vocal cords; involved in sound modulation but not production.\n\n" +
                        "Arytenoid cartilage (Incorrect)\n" +
                        "Adjusts the tension of the vocal cords but doesn’t produce sound itself."
        );

        questions.add("Which type of pressure causes air to flow into the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Atmospheric pressure > alveolar pressure",
                "Alveolar pressure > atmospheric pressure",
                "Pleural pressure = alveolar pressure",
                "Capillary pressure > thoracic pressure"
        )));
        correctAnswers.add("Atmospheric pressure > alveolar pressure");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Atmospheric pressure > alveolar pressure (Correct answer)\n" +
                        "Air moves into the lungs when alveolar pressure drops below atmospheric pressure.\n\n" +
                        "Alveolar pressure > atmospheric pressure (Incorrect)\n" +
                        "Causes air to move out of the lungs.\n\n" +
                        "Pleural pressure = alveolar pressure (Incorrect)\n" +
                        "Would result in no airflow.\n\n" +
                        "Capillary pressure > thoracic pressure (Incorrect)\n" +
                        "Not relevant to air movement."
        );

        questions.add("Which respiratory gas has the highest affinity to hemoglobin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "O₂",
                "CO₂",
                "CO",
                "N₂"
        )));
        correctAnswers.add("CO");
        rationales.put(26,
                "RATIONALE:\n" +
                        "CO (Correct answer)\n" +
                        "Binds 200–250 times more strongly than oxygen, displacing it and causing toxicity.\n\n" +
                        "O₂ (Incorrect)\n" +
                        "Binds to hemoglobin under normal conditions but less strongly than CO.\n\n" +
                        "CO₂ (Incorrect)\n" +
                        "Binds to hemoglobin but with lower affinity than both O₂ and CO.\n\n" +
                        "N₂ (Incorrect)\n" +
                        "Inert and doesn’t bind hemoglobin under normal circumstances."
        );

        questions.add("Which structure allows gas exchange in the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bronchi",
                "Capillaries around alveoli",
                "Larynx",
                "Trachea"
        )));
        correctAnswers.add("Capillaries around alveoli");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Capillaries around alveoli (Correct answer)\n" +
                        "Site of O₂ and CO₂ exchange via diffusion.\n\n" +
                        "Bronchi (Incorrect)\n" +
                        "Conduct air but no gas exchange.\n\n" +
                        "Larynx (Incorrect)\n" +
                        "Voice production, not gas exchange.\n\n" +
                        "Trachea (Incorrect)\n" +
                        "Air passageway only."
        );

        questions.add("The process of breathing in is called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Expiration",
                "Inhalation",
                "Ventilation",
                "Exhalation"
        )));
        correctAnswers.add("Inhalation");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Inhalation (Correct answer)\n" +
                        "Breathing in.\n\n" +
                        "Expiration (Incorrect)\n" +
                        "Breathing out.\n\n" +
                        "Ventilation (Incorrect)\n" +
                        "General term for the movement of air in and out.\n\n" +
                        "Exhalation (Incorrect)\n" +
                        "Same as expiration."
        );

        questions.add("Which lung has three lobes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left lung",
                "Right lung",
                "Both",
                "Neither"
        )));
        correctAnswers.add("Right lung");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Right lung (Correct answer)\n" +
                        "It has three lobes (superior, middle, inferior).\n\n" +
                        "Left lung (Incorrect)\n" +
                        "Has only two lobes (superior and inferior) to accommodate the heart.\n\n" +
                        "Both (Incorrect)\n" +
                        "Only the right has three.\n\n" +
                        "Neither (Incorrect)\n" +
                        "Incorrect."
        );

        questions.add("Which of these is part of the conducting zone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Respiratory bronchioles",
                "Alveolar ducts",
                "Alveoli",
                "Terminal bronchioles" // Correct answer
        )));
        correctAnswers.add("Terminal bronchioles");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Terminal bronchioles (Correct answer)\n" +
                        "Last part of the conducting zone, which moves air but doesn’t exchange gases.\n\n" +
                        "Respiratory bronchioles (Incorrect)\n" +
                        "These are part of the respiratory zone, where gas exchange begins.\n\n" +
                        "Alveolar ducts (Incorrect)\n" +
                        "Also part of the respiratory zone.\n\n" +
                        "Alveoli (Incorrect)\n" +
                        "Primary site of gas exchange — not part of the conducting zone."
        );

        questions.add("Which cell type synthesizes pulmonary surfactant?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Type I pneumocytes",
                "Type II pneumocytes", // Correct answer
                "Alveolar macrophages",
                "Club (Clara) cells"
        )));
        correctAnswers.add("Type II pneumocytes");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Type II pneumocytes (Correct answer)\n" +
                        "They secrete surfactant to reduce surface tension and prevent alveolar collapse.\n\n" +
                        "Type I pneumocytes (Incorrect)\n" +
                        "Form the thin barrier for gas exchange, but do not produce surfactant.\n\n" +
                        "Alveolar macrophages (Incorrect)\n" +
                        "Immune cells that engulf pathogens.\n\n" +
                        "Club (Clara) cells (Incorrect)\n" +
                        "Secrete components of airway lining fluid, but not the main surfactant producers."
        );

        questions.add("Which law governs the amount of gas that dissolves in a liquid at a given partial pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dalton’s law",
                "Boyle’s law",
                "Henry’s law", // Correct answer
                "Charles’s law"
        )));
        correctAnswers.add("Henry’s law");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Henry’s law (Correct answer)\n" +
                        "Gas solubility in a liquid is proportional to its partial pressure.\n\n" +
                        "Dalton’s law (Incorrect)\n" +
                        "Describes partial pressures of gases in a mixture.\n\n" +
                        "Boyle’s law (Incorrect)\n" +
                        "Relates gas pressure and volume (P ∝ 1/V).\n\n" +
                        "Charles’s law (Incorrect)\n" +
                        "Relates gas volume and temperature."
        );

        questions.add("Which disease increases lung compliance by destroying alveolar walls?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary fibrosis",
                "Asthma",
                "Emphysema", // Correct answer
                "Acute respiratory distress syndrome"
        )));
        correctAnswers.add("Emphysema");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Emphysema (Correct answer)\n" +
                        "Destruction of alveolar walls leads to loss of elasticity and increased compliance.\n\n" +
                        "Pulmonary fibrosis (Incorrect)\n" +
                        "Decreases compliance due to stiffened lung tissue.\n\n" +
                        "Asthma (Incorrect)\n" +
                        "Affects airway constriction but doesn’t increase compliance.\n\n" +
                        "Acute respiratory distress syndrome (Incorrect)\n" +
                        "Decreases compliance due to fluid buildup and inflammation."
        );

        questions.add("What term describes the maximum volume of air that can be exhaled after a maximal inhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tidal volume",
                "Vital capacity", // Correct answer
                "Inspiratory reserve volume",
                "Functional residual capacity"
        )));
        correctAnswers.add("Vital capacity");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Vital capacity (Correct answer)\n" +
                        "Total volume expelled after deep inhalation.\n\n" +
                        "Tidal volume (Incorrect)\n" +
                        "Normal breath in/out, not maximum effort.\n\n" +
                        "Inspiratory reserve volume (Incorrect)\n" +
                        "Extra volume inhaled beyond normal inhalation.\n\n" +
                        "Functional residual capacity (Incorrect)\n" +
                        "Volume remaining after normal exhalation."
        );

        questions.add("Which pressure difference (ΔP) keeps the lungs from collapsing?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Intrapulmonary minus atmospheric",
                "Transpulmonary (alveolar minus pleural)", // Correct answer
                "Intrapleural minus intrapulmonary",
                "Atmospheric minus intrapleural"
        )));
        correctAnswers.add("Transpulmonary (alveolar minus pleural)");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Transpulmonary (alveolar minus pleural) (Correct answer)\n" +
                        "Difference between alveolar and pleural pressure maintains lung expansion.\n\n" +
                        "Intrapulmonary minus atmospheric (Incorrect)\n" +
                        "Drives air movement, not lung stability.\n\n" +
                        "Intrapleural minus intrapulmonary (Incorrect)\n" +
                        "Reverse of correct direction.\n\n" +
                        "Atmospheric minus intrapleural (Incorrect)\n" +
                        "Doesn’t directly relate to lung recoil."
        );

        questions.add("Which receptors respond primarily to changes in blood pH and PCO₂?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Baroreceptors in aortic arch",
                "Central chemoreceptors in medulla", // Correct answer
                "Peripheral stretch receptors in lung",
                "Irritant receptors in airway mucosa"
        )));
        correctAnswers.add("Central chemoreceptors in medulla");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Central chemoreceptors in medulla (Correct answer)\n" +
                        "Respond to changes in CO₂ via pH in cerebrospinal fluid.\n\n" +
                        "Baroreceptors in aortic arch (Incorrect)\n" +
                        "Monitor blood pressure, not gas levels.\n\n" +
                        "Peripheral stretch receptors in lung (Incorrect)\n" +
                        "Detect lung inflation, not blood chemistry.\n\n" +
                        "Irritant receptors in airway mucosa (Incorrect)\n" +
                        "Respond to airborne particles."
        );

        questions.add("Which nerve carries the motor signal to contract the diaphragm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Vagus",
                "Phrenic", // Correct answer
                "Intercostal",
                "Hypoglossal"
        )));
        correctAnswers.add("Phrenic");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Phrenic (Correct answer)\n" +
                        "Originates from C3–C5 and innervates the diaphragm.\n\n" +
                        "Vagus (Incorrect)\n" +
                        "Parasympathetic innervation of thoracic/abdominal organs.\n\n" +
                        "Intercostal (Incorrect)\n" +
                        "Innervate intercostal muscles, not diaphragm.\n\n" +
                        "Hypoglossal (Incorrect)\n" +
                        "Controls tongue movements."
        );

        questions.add("Which law explains how each gas in a mixture exerts its own pressure independently?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Henry’s law",
                "Boyle’s law",
                "Dalton’s law", // Correct answer
                "Graham’s law"
        )));
        correctAnswers.add("Dalton’s law");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Dalton’s law (Correct answer)\n" +
                        "Each gas in a mixture contributes to total pressure according to its partial pressure.\n\n" +
                        "Henry’s law (Incorrect)\n" +
                        "Dissolution of gases in liquids.\n\n" +
                        "Boyle’s law (Incorrect)\n" +
                        "Volume–pressure relationship.\n\n" +
                        "Graham’s law (Incorrect)\n" +
                        "Describes gas diffusion rate based on molecular weight."
        );

        questions.add("Which reflex prevents over‑inflation of the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cough reflex",
                "Hering–Breuer reflex", // Correct answer
                "Bös reflex",
                "Diving reflex"
        )));
        correctAnswers.add("Hering–Breuer reflex");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Hering–Breuer reflex (Correct answer)\n" +
                        "Triggered by stretch receptors to inhibit inspiration and prevent overinflation.\n\n" +
                        "Cough reflex (Incorrect)\n" +
                        "Clears airways, not related to lung inflation.\n\n" +
                        "Bös reflex (Incorrect)\n" +
                        "Not a recognized respiratory reflex.\n\n" +
                        "Diving reflex (Incorrect)\n" +
                        "Slows heart rate and conserves oxygen underwater."
        );

        questions.add("What is the primary phagocyte in the alveolar air spaces?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Neutrophil",
                "Type I pneumocyte",
                "Alveolar macrophage", // Correct answer
                "Eosinophil"
        )));
        correctAnswers.add("Alveolar macrophage");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Alveolar macrophage (Correct answer)\n" +
                        "Patrol alveolar surfaces, engulfing debris and pathogens.\n\n" +
                        "Neutrophil (Incorrect)\n" +
                        "Major phagocyte in blood, but not the primary lung phagocyte unless infection is severe.\n\n" +
                        "Type I pneumocyte (Incorrect)\n" +
                        "Structural cells for gas exchange, not phagocytic.\n\n" +
                        "Eosinophil (Incorrect)\n" +
                        "Involved in allergic responses and parasitic infections, not routine alveolar defense."
        );

        questions.add("An increase in temperature shifts the O₂–hemoglobin dissociation curve:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left, increasing affinity",
                "Right, decreasing affinity", // Correct answer
                "Upward, increasing P₅₀",
                "Downward, decreasing P₅₀"
        )));
        correctAnswers.add("Right, decreasing affinity");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Right, decreasing affinity (Correct answer)\n" +
                        "Promotes oxygen release to tissues at higher temperatures.\n\n" +
                        "Left, increasing affinity (Incorrect)\n" +
                        "Happens with decreased temperature, alkalosis, or low PCO₂.\n\n" +
                        "Upward, increasing P₅₀ (Incorrect)\n" +
                        "Not how the curve shifts — P₅₀ increases with rightward shift, but upward movement isn’t standard terminology.\n\n" +
                        "Downward, decreasing P₅₀ (Incorrect)\n" +
                        "Occurs with increased oxygen affinity, not during temperature rise."
        );

        questions.add("Which pneumocyte type forms the thin barrier for gas exchange?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Type I pneumocytes", // Correct answer
                "Type II pneumocytes",
                "Alveolar macrophages",
                "Club cells"
        )));
        correctAnswers.add("Type I pneumocytes");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Type I pneumocytes (Correct answer)\n" +
                        "Thin, flat cells making up most of the alveolar wall for diffusion.\n\n" +
                        "Type II pneumocytes (Incorrect)\n" +
                        "Produce surfactant, not for diffusion.\n\n" +
                        "Alveolar macrophages (Incorrect)\n" +
                        "Immune cells, not structural.\n\n" +
                        "Club cells (Incorrect)\n" +
                        "Found in bronchioles, with detox and secretion functions."
        );

        questions.add("“Anatomical dead space” is defined as the volume of air:");
        choices.add(new ArrayList<>(Arrays.asList(
                "In alveoli not participating in exchange",
                "In conducting passages", // Correct answer
                "Remaining after forced expiration",
                "That cannot be inhaled"
        )));
        correctAnswers.add("In conducting passages");
        rationales.put(43,
                "RATIONALE:\n" +
                        "In conducting passages (Correct answer)\n" +
                        "Air in trachea, bronchi, bronchioles where no gas exchange occurs.\n\n" +
                        "In alveoli not participating in exchange (Incorrect)\n" +
                        "Physiologic dead space.\n\n" +
                        "Remaining after forced expiration (Incorrect)\n" +
                        "Residual volume.\n\n" +
                        "That cannot be inhaled (Incorrect)\n" +
                        "Not a formal term for dead space."
        );

        questions.add("Which cartilage forms the “Adam’s apple” of the larynx?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cricoid",
                "Epiglottic",
                "Thyroid", // Correct answer
                "Arytenoid"
        )));
        correctAnswers.add("Thyroid");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Thyroid (Correct answer)\n" +
                        "Large, shield-shaped cartilage forming Adam’s apple.\n\n" +
                        "Cricoid (Incorrect)\n" +
                        "Below thyroid cartilage; complete ring structure.\n\n" +
                        "Epiglottic (Incorrect)\n" +
                        "Covers laryngeal inlet during swallowing.\n\n" +
                        "Arytenoid (Incorrect)\n" +
                        "Anchor vocal cords posteriorly."
        );

        questions.add("Which volume remains in the lungs after maximal expiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Residual volume", // Correct answer
                "Expiratory reserve volume",
                "Inspiratory reserve volume",
                "Functional residual capacity"
        )));
        correctAnswers.add("Residual volume");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Residual volume (Correct answer)\n" +
                        "Air remaining after full expiration, prevents lung collapse.\n\n" +
                        "Expiratory reserve volume (Incorrect)\n" +
                        "Extra air forcibly exhaled after normal expiration.\n\n" +
                        "Inspiratory reserve volume (Incorrect)\n" +
                        "Air inhaled beyond normal inspiration.\n\n" +
                        "Functional residual capacity (Incorrect)\n" +
                        "Volume remaining after normal expiration (includes residual and expiratory reserve)."
        );

        questions.add("Minute ventilation (VE) equals:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tidal volume ÷ respiratory rate",
                "(Tidal volume – dead space) × respiratory rate",
                "Tidal volume × respiratory rate", // Correct answer
                "Vital capacity × respiratory rate"
        )));
        correctAnswers.add("Tidal volume × respiratory rate");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Tidal volume × respiratory rate (Correct answer)\n" +
                        "Total air moved per minute.\n\n" +
                        "Tidal volume ÷ respiratory rate (Incorrect)\n" +
                        "Not a meaningful calculation.\n\n" +
                        "(Tidal volume – dead space) × respiratory rate (Incorrect)\n" +
                        "Formula for alveolar ventilation.\n\n" +
                        "Vital capacity × respiratory rate (Incorrect)\n" +
                        "Not typically used clinically."
        );

        questions.add("Alveolar ventilation (VA) is calculated as:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tidal volume × respiratory rate",
                "(Tidal volume – dead space) × respiratory rate", // Correct answer
                "Vital capacity – inspiratory reserve",
                "Total lung capacity – residual volume"
        )));
        correctAnswers.add("(Tidal volume – dead space) × respiratory rate");
        rationales.put(47,
                "RATIONALE:\n" +
                        "(Tidal volume – dead space) × respiratory rate (Correct answer)\n" +
                        "Accounts for non-exchanging airways.\n\n" +
                        "Tidal volume × respiratory rate (Incorrect)\n" +
                        "Minute ventilation.\n\n" +
                        "Vital capacity – inspiratory reserve (Incorrect)\n" +
                        "Not related to ventilation rate.\n\n" +
                        "Total lung capacity – residual volume (Incorrect)\n" +
                        "Gives vital capacity, not ventilation."
        );

        questions.add("Which muscles are most active during forced expiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphragm",
                "External intercostals",
                "Internal intercostals and abdominal muscles", // Correct answer
                "Sternocleidomastoids"
        )));
        correctAnswers.add("Internal intercostals and abdominal muscles");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Internal intercostals and abdominal muscles (Correct answer)\n" +
                        "Force air out by compressing thoracic cavity.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Relaxes during expiration.\n\n" +
                        "External intercostals (Incorrect)\n" +
                        "Active in inspiration.\n\n" +
                        "Sternocleidomastoids (Incorrect)\n" +
                        "Accessory muscle for deep inspiration."
        );

        questions.add("Which mediator causes bronchodilation in the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Acetylcholine",
                "Histamine",
                "Leukotriene D₄",
                "Epinephrine" // Correct answer
        )));
        correctAnswers.add("Epinephrine");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Epinephrine (Correct answer)\n" +
                        "Binds beta-2 receptors causing smooth muscle relaxation and bronchodilation.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Causes bronchoconstriction.\n\n" +
                        "Histamine (Incorrect)\n" +
                        "Bronchoconstrictor in allergic reactions.\n\n" +
                        "Leukotriene D₄ (Incorrect)\n" +
                        "Potent bronchoconstrictor."
        );

        questions.add("The trachea is lined by which epithelium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pseudostratified ciliated columnar", // Correct answer
                "Simple squamous",
                "Stratified squamous",
                "Transitional"
        )));
        correctAnswers.add("Pseudostratified ciliated columnar");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Pseudostratified ciliated columnar (Correct answer)\n" +
                        "Lines the trachea to trap and move particles via mucociliary action.\n\n" +
                        "Simple squamous (Incorrect)\n" +
                        "Found in alveoli for gas exchange, not the trachea.\n\n" +
                        "Stratified squamous (Incorrect)\n" +
                        "Found in areas exposed to friction (e.g., oropharynx), not the trachea.\n\n" +
                        "Transitional (Incorrect)\n" +
                        "Found in the urinary system, not the respiratory tract."
        );

        questions.add("In obstructive lung disease, the FEV₁/FVC ratio is typically:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decreased (< 0.7)", // Correct answer
                "Normal (> 0.8)",
                "Increased (> 0.9)",
                "Unchanged"
        )));
        correctAnswers.add("Decreased (< 0.7)");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Decreased (< 0.7) (Correct answer)\n" +
                        "FEV₁ is reduced more than FVC in obstructive diseases (e.g., asthma, COPD).\n\n" +
                        "Normal (> 0.8) (Incorrect)\n" +
                        "Normal in healthy lungs or restrictive disease, not obstructive.\n\n" +
                        "Increased (> 0.9) (Incorrect)\n" +
                        "Seen in restrictive lung diseases, not obstructive.\n\n" +
                        "Unchanged (Incorrect)\n" +
                        "Untrue in obstructive diseases."
        );

        questions.add("Which immunoglobulin predominates in mucosal secretions of the respiratory tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "IgA", // Correct answer
                "IgG",
                "IgM",
                "IgE"
        )));
        correctAnswers.add("IgA");
        rationales.put(52,
                "RATIONALE:\n" +
                        "IgA (Correct answer)\n" +
                        "The main antibody in mucosal immunity.\n\n" +
                        "IgG (Incorrect)\n" +
                        "More abundant in blood, not secretions.\n\n" +
                        "IgM (Incorrect)\n" +
                        "First antibody produced in infections, but not dominant in mucosa.\n\n" +
                        "IgE (Incorrect)\n" +
                        "Associated with allergies, not primary mucosal defense."
        );

        questions.add("Which enzyme in red blood cells catalyzes CO₂ hydration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Carbonic anhydrase", // Correct answer
                "Carboxypeptidase",
                "ATP synthase",
                "Lactate dehydrogenase"
        )));
        correctAnswers.add("Carbonic anhydrase");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Carbonic anhydrase (Correct answer)\n" +
                        "Converts CO₂ and water to carbonic acid, crucial in CO₂ transport.\n\n" +
                        "Carboxypeptidase (Incorrect)\n" +
                        "Involved in protein digestion, not gas exchange.\n\n" +
                        "ATP synthase (Incorrect)\n" +
                        "Produces ATP, found in mitochondria.\n\n" +
                        "Lactate dehydrogenase (Incorrect)\n" +
                        "Involved in anaerobic metabolism, not CO₂ hydration."
        );

        questions.add("The pneumotaxic center, which helps regulate the rate of breathing, is located in the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pons", // Correct answer
                "Medulla oblongata",
                "Midbrain",
                "Hypothalamus"
        )));
        correctAnswers.add("Pons");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Pons (Correct answer)\n" +
                        "Pneumotaxic center in the pons controls breathing rhythm and limits inspiration.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "Contains other respiratory centers, not the pneumotaxic center.\n\n" +
                        "Midbrain (Incorrect)\n" +
                        "Involved in vision and hearing, not respiration.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "Regulates hormones and temperature, not breathing rate."
        );

        questions.add("What force tends to collapse alveoli and must be overcome by surfactant?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Surface tension", // Correct answer
                "Elastic recoil",
                "Osmotic pressure",
                "Compliance"
        )));
        correctAnswers.add("Surface tension");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Surface tension (Correct answer)\n" +
                        "Surfactant reduces this to prevent alveolar collapse.\n\n" +
                        "Elastic recoil (Incorrect)\n" +
                        "Promotes lung deflation but not the primary issue in alveolar collapse.\n\n" +
                        "Osmotic pressure (Incorrect)\n" +
                        "Regulates fluid movement, not related to alveolar collapse.\n\n" +
                        "Compliance (Incorrect)\n" +
                        "Describes lung expandability, not a collapsing force."
        );

        questions.add("Surfactant is composed primarily of:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Phospholipids", // Correct answer
                "Proteoglycans",
                "Glycoproteins",
                "Cholesterol"
        )));
        correctAnswers.add("Phospholipids");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Phospholipids (Correct answer)\n" +
                        "Particularly dipalmitoylphosphatidylcholine, reduces surface tension.\n\n" +
                        "Proteoglycans (Incorrect)\n" +
                        "Found in connective tissue, not surfactant.\n\n" +
                        "Glycoproteins (Incorrect)\n" +
                        "Present in mucus but not the main surfactant component.\n\n" +
                        "Cholesterol (Incorrect)\n" +
                        "Minor component, not primary."
        );

        questions.add("Normal systemic arterial PO₂ is about:");
        choices.add(new ArrayList<>(Arrays.asList(
                "100 mmHg", // Correct answer
                "40 mmHg",
                "80 mmHg",
                "120 mmHg"
        )));
        correctAnswers.add("100 mmHg");
        rationales.put(57,
                "RATIONALE:\n" +
                        "100 mmHg (Correct answer)\n" +
                        "Normal arterial oxygen partial pressure.\n\n" +
                        "40 mmHg (Incorrect)\n" +
                        "Typical for venous blood, not arterial.\n\n" +
                        "80 mmHg (Incorrect)\n" +
                        "Lower end, could indicate mild hypoxemia.\n\n" +
                        "120 mmHg (Incorrect)\n" +
                        "Higher than normal for arterial PO₂."
        );

        questions.add("Which receptors in the airway mucosa trigger a cough when irritated?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Irritant receptors", // Correct answer
                "Stretch receptors",
                "Chemoreceptors",
                "Baroreceptors"
        )));
        correctAnswers.add("Irritant receptors");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Irritant receptors (Correct answer)\n" +
                        "Stimulate cough in response to particles.\n\n" +
                        "Stretch receptors (Incorrect)\n" +
                        "Respond to lung inflation, not irritation.\n\n" +
                        "Chemoreceptors (Incorrect)\n" +
                        "Detect blood gas changes, not airway irritation.\n\n" +
                        "Baroreceptors (Incorrect)\n" +
                        "Detect pressure changes, mainly in arteries."
        );

        questions.add("The bronchial circulation’s main role is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Supply oxygenated blood to lung tissue", // Correct answer
                "Exchange gases with alveoli",
                "Drain lymph from lungs",
                "Remove CO₂ from pulmonary veins"
        )));
        correctAnswers.add("Supply oxygenated blood to lung tissue");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Supply oxygenated blood to lung tissue (Correct answer)\n" +
                        "Nourishes lung structures.\n\n" +
                        "Exchange gases with alveoli (Incorrect)\n" +
                        "Done by pulmonary circulation, not bronchial.\n\n" +
                        "Drain lymph from lungs (Incorrect)\n" +
                        "Performed by lymphatic vessels, not bronchial circulation.\n\n" +
                        "Remove CO₂ from pulmonary veins (Incorrect)\n" +
                        "CO₂ removal happens in alveoli, not veins."
        );

        questions.add("During which embryonic stage of lung development do the primary bronchi form?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Embryonic", // Correct answer
                "Alveolar",
                "Canalicular",
                "Saccular"
        )));
        correctAnswers.add("Embryonic");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Embryonic (Correct answer)\n" +
                        "The primary bronchi develop from the lung bud during the embryonic stage (weeks 4–7).\n\n" +
                        "Alveolar (Incorrect)\n" +
                        "This is the final stage where mature alveoli form, starting around 36 weeks gestation.\n\n" +
                        "Canalicular (Incorrect)\n" +
                        "This stage (16–26 weeks) involves development of respiratory bronchioles.\n\n" +
                        "Saccular (Incorrect)\n" +
                        "Characterized by terminal sac formation (26–36 weeks)."
        );

        questions.add("In which stage do respiratory bronchioles and alveolar ducts first appear?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Canalicular", // Correct answer
                "Embryonic",
                "Pseudoglandular",
                "Alveolar"
        )));
        correctAnswers.add("Canalicular");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Canalicular (Correct answer)\n" +
                        "This stage (16–26 weeks) sees formation of respiratory bronchioles and alveolar ducts.\n\n" +
                        "Embryonic (Incorrect)\n" +
                        "Only the lung bud and primary bronchi develop here.\n\n" +
                        "Pseudoglandular (Incorrect)\n" +
                        "Terminal bronchioles form but no alveoli or ducts.\n\n" +
                        "Alveolar (Incorrect)\n" +
                        "Involves alveolar maturation and increase in number."
        );

        questions.add("Which structure marks the entry and exit point for bronchi, vessels, and nerves on the lung?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hilum", // Correct answer
                "Apex",
                "Base",
                "Costal surface"
        )));
        correctAnswers.add("Hilum");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Hilum (Correct answer)\n" +
                        "The mediastinal surface where bronchi, arteries, veins, and nerves enter and exit.\n\n" +
                        "Apex (Incorrect)\n" +
                        "It’s the uppermost lung region.\n\n" +
                        "Base (Incorrect)\n" +
                        "Refers to the inferior lung surface.\n\n" +
                        "Costal surface (Incorrect)\n" +
                        "It’s the lung surface adjacent to ribs."
        );

        questions.add("In West’s zone 1 of the lung (apex) under normal conditions, the relationship of pressures is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "PA > Pa > Pv", // Correct answer
                "Pa > PA > Pv",
                "Pa > Pv > PA",
                "Pv > Pa > PA"
        )));
        correctAnswers.add("PA > Pa > Pv");
        rationales.put(63,
                "RATIONALE:\n" +
                        "PA > Pa > Pv (Correct answer)\n" +
                        "At apex: alveolar pressure exceeds arterial and venous pressures.\n\n" +
                        "Pa > PA > Pv (Incorrect)\n" +
                        "This happens in lower lung zones.\n\n" +
                        "Pa > Pv > PA (Incorrect)\n" +
                        "Represents zone 3.\n\n" +
                        "Pv > Pa > PA (Incorrect)\n" +
                        "Not physiologically normal."
        );

        questions.add("What is the primary function of club (Clara) cells in the bronchioles?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Detoxify xenobiotics", // Correct answer
                "Produce surfactant",
                "Secrete mucus",
                "Phagocytose pathogens"
        )));
        correctAnswers.add("Detoxify xenobiotics");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Detoxify xenobiotics (Correct answer)\n" +
                        "Clara cells contain enzymes that detoxify harmful substances and protect the bronchioles.\n\n" +
                        "Produce surfactant (Incorrect)\n" +
                        "Surfactant is primarily produced by type II pneumocytes.\n\n" +
                        "Secrete mucus (Incorrect)\n" +
                        "While Clara cells do secrete a substance, it is not primarily mucus.\n\n" +
                        "Phagocytose pathogens (Incorrect)\n" +
                        "Clara cells are not primarily involved in phagocytosis."
        );

        questions.add("Which component gives airway mucus its viscoelastic properties?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mucin glycoproteins", // Correct answer
                "Water",
                "Electrolytes",
                "Antibodies"
        )));
        correctAnswers.add("Mucin glycoproteins");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Mucin glycoproteins (Correct answer)\n" +
                        "Mucins are large glycoproteins that provide mucus with its viscous, elastic properties.\n\n" +
                        "Water (Incorrect)\n" +
                        "While water is important for mucus consistency, it’s the mucins that give it its structure.\n\n" +
                        "Electrolytes (Incorrect)\n" +
                        "Electrolytes contribute to the overall composition but not the viscoelastic nature of mucus.\n\n" +
                        "Antibodies (Incorrect)\n" +
                        "Antibodies in mucus serve immune functions but not its viscoelasticity."
        );

        questions.add("The term “acinus” refers to the functional respiratory unit consisting of:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Respiratory bronchioles + alveolar ducts + alveolar sacs", // Correct answer
                "Terminal bronchioles + conducting bronchioles",
                "Alveolar ducts + terminal bronchioles",
                "Bronchopulmonary segment"
        )));
        correctAnswers.add("Respiratory bronchioles + alveolar ducts + alveolar sacs");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Respiratory bronchioles + alveolar ducts + alveolar sacs (Correct answer)\n" +
                        "This describes the acinus, where gas exchange occurs.\n\n" +
                        "Terminal bronchioles + conducting bronchioles (Incorrect)\n" +
                        "These are part of the conducting zone, not the respiratory unit.\n\n" +
                        "Alveolar ducts + terminal bronchioles (Incorrect)\n" +
                        "Alveolar ducts are part of the acinus but not with terminal bronchioles alone.\n\n" +
                        "Bronchopulmonary segment (Incorrect)\n" +
                        "This refers to an anatomical division of the lung, not a functional unit for gas exchange."
        );

        questions.add("Which cells are the main phagocytes in alveolar air spaces?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Alveolar macrophages", // Correct answer
                "Type I pneumocytes",
                "Type II pneumocytes",
                "Endothelial cells"
        )));
        correctAnswers.add("Alveolar macrophages");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Alveolar macrophages (Correct answer)\n" +
                        "These are the primary immune cells responsible for engulfing pathogens and debris.\n\n" +
                        "Type I pneumocytes (Incorrect)\n" +
                        "Type I cells are involved in gas exchange, not phagocytosis.\n\n" +
                        "Type II pneumocytes (Incorrect)\n" +
                        "Type II cells produce surfactant and do not primarily function in phagocytosis.\n\n" +
                        "Endothelial cells (Incorrect)\n" +
                        "Endothelial cells line blood vessels and are not involved in phagocytosis in alveolar spaces."
        );

        questions.add("Physiological dead space is defined as:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Anatomical dead space + alveolar dead space", // Correct answer
                "Volume of conducting airways only",
                "Volume of alveoli not perfused only",
                "Total lung capacity"
        )));
        correctAnswers.add("Anatomical dead space + alveolar dead space");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Anatomical dead space + alveolar dead space (Correct answer)\n" +
                        "Physiological dead space includes both anatomical and alveolar dead spaces where no gas exchange occurs.\n\n" +
                        "Volume of conducting airways only (Incorrect)\n" +
                        "This describes anatomical dead space, not physiological dead space.\n\n" +
                        "Volume of alveoli not perfused only (Incorrect)\n" +
                        "This is part of alveolar dead space, not the full definition of physiological dead space.\n\n" +
                        "Total lung capacity (Incorrect)\n" +
                        "Total lung capacity includes all volumes in the lung, not just dead space."
        );

        questions.add("The alveolar gas equation is used to estimate alveolar:");
        choices.add(new ArrayList<>(Arrays.asList(
                "O₂ pressure (PAO₂)", // Correct answer
                "CO₂ pressure (PACO₂)",
                "Arterial O₂ pressure (PaO₂)",
                "Inspired O₂ fraction (FIO₂)"
        )));
        correctAnswers.add("O₂ pressure (PAO₂)");
        rationales.put(69,
                "RATIONALE:\n" +
                        "O₂ pressure (PAO₂) (Correct answer)\n" +
                        "The alveolar gas equation is specifically used to estimate PAO₂.\n\n" +
                        "CO₂ pressure (PACO₂) (Incorrect)\n" +
                        "PACO₂ is measured directly, not estimated with the alveolar gas equation.\n\n" +
                        "Arterial O₂ pressure (PaO₂) (Incorrect)\n" +
                        "PaO₂ is directly measured from arterial blood, not from the alveolar gas equation.\n\n" +
                        "Inspired O₂ fraction (FIO₂) (Incorrect)\n" +
                        "The equation is not used to calculate FIO₂."
        );

        questions.add("At high altitude, which change in alveolar gas is observed?");
        choices.add(new ArrayList<>(Arrays.asList(
                "↓ PAO₂", // Correct answer
                "↑ PAO₂",
                "↑ PACO₂",
                "No change in PAO₂"
        )));
        correctAnswers.add("↓ PAO₂");
        rationales.put(70,
                "RATIONALE:\n" +
                        "↓ PAO₂ (Correct answer)\n" +
                        "High altitudes result in lower oxygen pressure in the alveoli due to decreased atmospheric oxygen.\n\n" +
                        "↑ PAO₂ (Incorrect)\n" +
                        "At high altitude, PAO₂ decreases due to lower atmospheric pressure.\n\n" +
                        "↑ PACO₂ (Incorrect)\n" +
                        "CO₂ levels are not significantly affected by altitude.\n\n" +
                        "No change in PAO₂ (Incorrect)\n" +
                        "PAO₂ significantly decreases with increased altitude."
        );

        questions.add("The Haldane effect describes how:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Deoxygenated hemoglobin carries more CO₂", // Correct answer
                "Increased CO₂ enhances O₂ binding",
                "pH rises with increased CO₂",
                "Decreased temperature increases O₂ affinity"
        )));
        correctAnswers.add("Deoxygenated hemoglobin carries more CO₂");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Deoxygenated hemoglobin carries more CO₂ (Correct answer)\n" +
                        "The Haldane effect refers to the ability of deoxygenated hemoglobin to bind more CO₂.\n\n" +
                        "Increased CO₂ enhances O₂ binding (Incorrect)\n" +
                        "This is more aligned with the Bohr effect, not the Haldane effect.\n\n" +
                        "pH rises with increased CO₂ (Incorrect)\n" +
                        "CO₂ increases acidity (lowers pH), not the opposite.\n\n" +
                        "Decreased temperature increases O₂ affinity (Incorrect)\n" +
                        "While decreased temperature can increase O₂ affinity, this is unrelated to the Haldane effect."
        );

        questions.add("The Bohr effect refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "↑ CO₂/H⁺ shifting O₂–Hb curve right (↓ affinity)", // Correct answer
                "↑ O₂ shifting CO₂–Hb curve right",
                "↓ CO₂ shifting CO₂ binding curve",
                "↑ temperature shifting O₂ curve left"
        )));
        correctAnswers.add("↑ CO₂/H⁺ shifting O₂–Hb curve right (↓ affinity)");
        rationales.put(72,
                "RATIONALE:\n" +
                        "↑ CO₂/H⁺ shifting O₂–Hb curve right (↓ affinity) (Correct answer)\n" +
                        "The Bohr effect describes how increased CO₂ or acidity (H⁺) decreases hemoglobin's affinity for oxygen.\n\n" +
                        "↑ O₂ shifting CO₂–Hb curve right (Incorrect)\n" +
                        "This does not accurately describe the Bohr effect.\n\n" +
                        "↓ CO₂ shifting CO₂ binding curve (Incorrect)\n" +
                        "This does not explain the Bohr effect, which focuses on CO₂ and H⁺ effects on oxygen binding.\n\n" +
                        "↑ temperature shifting O₂ curve left (Incorrect)\n" +
                        "Increased temperature shifts the curve to the right, not left."
        );

        questions.add("Which condition shifts the oxyhemoglobin dissociation curve to the left?");
        choices.add(new ArrayList<>(Arrays.asList(
                "↓ temperature", // Correct answer
                "↑ PCO₂",
                "↑ H⁺ (↓ pH)",
                "↑ 2,3‑BPG"
        )));
        correctAnswers.add("↓ temperature");
        rationales.put(73,
                "RATIONALE:\n" +
                        "↓ temperature (Correct answer)\n" +
                        "A lower temperature increases O₂ affinity, shifting the curve to the left.\n\n" +
                        "↑ PCO₂ (Incorrect)\n" +
                        "Increased CO₂ actually shifts the curve to the right.\n\n" +
                        "↑ H⁺ (↓ pH) (Incorrect)\n" +
                        "Increased acidity (low pH) shifts the curve to the right.\n\n" +
                        "↑ 2,3‑BPG (Incorrect)\n" +
                        "Higher levels of 2,3-BPG shift the curve to the right."
        );

        questions.add("Which vessel carries deoxygenated blood from the right ventricle to the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary artery", // Correct answer
                "Bronchial artery",
                "Aorta",
                "Pulmonary vein"
        )));
        correctAnswers.add("Pulmonary artery");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Pulmonary artery (Correct answer)\n" +
                        "This vessel carries deoxygenated blood from the right ventricle to the lungs for oxygenation.\n\n" +
                        "Bronchial artery (Incorrect)\n" +
                        "The bronchial artery supplies oxygenated blood to lung tissue, not deoxygenated blood for gas exchange.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "The aorta carries oxygenated blood from the left ventricle to the body.\n\n" +
                        "Pulmonary vein (Incorrect)\n" +
                        "The pulmonary vein carries oxygenated blood back to the left atrium from the lungs."
        );

        questions.add("Which hormone is secreted in chronic hypoxia to stimulate erythropoiesis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Erythropoietin", // Correct answer
                "Cortisol",
                "Aldosterone",
                "Thyroxine"
        )));
        correctAnswers.add("Erythropoietin");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Erythropoietin (Correct answer)\n" +
                        "Erythropoietin is produced in response to hypoxia and stimulates red blood cell production.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol is involved in stress responses, not erythropoiesis.\n\n" +
                        "Aldosterone (Incorrect)\n" +
                        "Aldosterone regulates sodium and water balance, not erythropoiesis.\n\n" +
                        "Thyroxine (Incorrect)\n" +
                        "Thyroxine regulates metabolism but does not directly stimulate erythropoiesis in response to hypoxia."
        );

        questions.add("The lung diffusion capacity for CO (DLCO) is decreased in:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary fibrosis", // Correct answer
                "Asthma",
                "Polycythemia",
                "Exercise"
        )));
        correctAnswers.add("Pulmonary fibrosis");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Pulmonary fibrosis (Correct answer)\n" +
                        "Pulmonary fibrosis thickens the alveolar-capillary membrane, reducing the diffusion of gases like CO.\n\n" +
                        "Asthma (Incorrect)\n" +
                        "Asthma typically involves airway narrowing, not a decrease in lung diffusion capacity.\n\n" +
                        "Polycythemia (Incorrect)\n" +
                        "Polycythemia increases RBC count but does not directly impact DLCO.\n\n" +
                        "Exercise (Incorrect)\n" +
                        "Exercise increases lung ventilation and perfusion, but DLCO is not significantly reduced."
        );

        questions.add("Hypoxic pulmonary vasoconstriction redirects blood flow to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Well-ventilated alveoli", // Correct answer
                "Poorly ventilated alveoli",
                "Areas of high shunt",
                "Zones of low V/Q"
        )));
        correctAnswers.add("Well-ventilated alveoli");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Well-ventilated alveoli (Correct answer)\n" +
                        "This process enhances ventilation-perfusion matching by directing blood flow to areas of the lung with better oxygenation.\n\n" +
                        "Poorly ventilated alveoli (Incorrect)\n" +
                        "Hypoxic pulmonary vasoconstriction redirects blood flow away from poorly ventilated alveoli to optimize oxygen exchange.\n\n" +
                        "Areas of high shunt (Incorrect)\n" +
                        "A shunt indicates a mismatch of ventilation and perfusion, so blood is not redirected there.\n\n" +
                        "Zones of low V/Q (Incorrect)\n" +
                        "Zones of low V/Q receive less blood flow in this response."
        );

        questions.add("What is the approximate water vapor pressure in alveolar air at 37 °C?");
        choices.add(new ArrayList<>(Arrays.asList(
                "47 mmHg", // Correct answer
                "10 mmHg",
                "100 mmHg",
                "760 mmHg"
        )));
        correctAnswers.add("47 mmHg");
        rationales.put(78,
                "RATIONALE:\n" +
                        "47 mmHg (Correct answer)\n" +
                        "The water vapor pressure in alveolar air at 37°C is approximately 47 mmHg.\n\n" +
                        "10 mmHg (Incorrect)\n" +
                        "This value is too low for the water vapor pressure in alveolar air.\n\n" +
                        "100 mmHg (Incorrect)\n" +
                        "This is much higher than the actual water vapor pressure in alveolar air.\n\n" +
                        "760 mmHg (Incorrect)\n" +
                        "This value corresponds to the total atmospheric pressure at sea level, not water vapor."
        );

        questions.add("Which lung volume cannot be measured by simple spirometry?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Residual volume", // Correct answer
                "Tidal volume",
                "Inspiratory reserve volume",
                "Expiratory reserve volume"
        )));
        correctAnswers.add("Residual volume");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Residual volume (Correct answer)\n" +
                        "Residual volume (the air left in the lungs after maximal exhalation) cannot be measured by spirometry because it is not exhaled.\n\n" +
                        "Tidal volume (Incorrect)\n" +
                        "Tidal volume is easily measured by spirometry during normal breathing.\n\n" +
                        "Inspiratory reserve volume (Incorrect)\n" +
                        "Inspiratory reserve volume can be measured using spirometry.\n\n" +
                        "Expiratory reserve volume (Incorrect)\n" +
                        "Expiratory reserve volume is also measurable with spirometry."
        );

        questions.add("The mucociliary escalator depends on:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ciliated epithelial cells", // Correct answer
                "Alveolar macrophages",
                "Surfactant",
                "Smooth muscle contraction"
        )));
        correctAnswers.add("Ciliated epithelial cells");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Ciliated epithelial cells (Correct answer)\n" +
                        "The mucociliary escalator relies on the movement of mucus by ciliated cells in the respiratory tract to clear debris.\n\n" +
                        "Alveolar macrophages (Incorrect)\n" +
                        "Alveolar macrophages are involved in immune defense, not in the mucociliary escalator.\n\n" +
                        "Surfactant (Incorrect)\n" +
                        "Surfactant helps reduce surface tension in the lungs, but does not directly affect the mucociliary escalator.\n\n" +
                        "Smooth muscle contraction (Incorrect)\n" +
                        "Smooth muscle contraction affects bronchoconstriction but does not directly participate in the mucociliary escalator."
        );

        questions.add("What structural feature holds the trachea open?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hyaline cartilage rings", // Correct answer
                "Smooth muscle",
                "Elastic fibers",
                "Fibrocartilage discs"
        )));
        correctAnswers.add("Hyaline cartilage rings");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Hyaline cartilage rings (Correct answer)\n" +
                        "Hyaline cartilage rings provide structural support to keep the trachea open and prevent collapse.\n\n" +
                        "Smooth muscle (Incorrect)\n" +
                        "Smooth muscle does not hold the trachea open; it regulates airway diameter.\n\n" +
                        "Elastic fibers (Incorrect)\n" +
                        "Elastic fibers contribute to lung recoil, not tracheal structure.\n\n" +
                        "Fibrocartilage discs (Incorrect)\n" +
                        "Fibrocartilage is found in joints and other structures but not in the trachea."
        );

        questions.add("Sympathetic stimulation of bronchial smooth muscle causes:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bronchodilation", // Correct answer
                "Bronchoconstriction",
                "No change",
                "↑ Mucus secretion"
        )));
        correctAnswers.add("Bronchodilation");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Bronchodilation (Correct answer)\n" +
                        "Sympathetic stimulation leads to bronchodilation through beta-2 adrenergic receptors.\n\n" +
                        "Bronchoconstriction (Incorrect)\n" +
                        "This is the effect of parasympathetic stimulation.\n\n" +
                        "No change (Incorrect)\n" +
                        "There is a significant physiological effect from sympathetic stimulation.\n\n" +
                        "↑ Mucus secretion (Incorrect)\n" +
                        "Mucus secretion is generally increased by parasympathetic stimulation, not sympathetic."
        );

        questions.add("Ventilation‑perfusion (V/Q) ratio is highest at the lung:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Apex", // Correct answer
                "Base",
                "Middle",
                "Uniform throughout"
        )));
        correctAnswers.add("Apex");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Apex (Correct answer)\n" +
                        "The V/Q ratio is highest at the lung apex where ventilation exceeds perfusion due to gravity.\n\n" +
                        "Base (Incorrect)\n" +
                        "At the base, perfusion exceeds ventilation, leading to a lower V/Q ratio.\n\n" +
                        "Middle (Incorrect)\n" +
                        "The middle zone has an intermediate V/Q ratio.\n\n" +
                        "Uniform throughout (Incorrect)\n" +
                        "V/Q ratio is not uniform; it varies based on lung zone."
        );

        questions.add("Which gas is most soluble in blood plasma?");
        choices.add(new ArrayList<>(Arrays.asList(
                "CO₂", // Correct answer
                "O₂",
                "N₂",
                "He"
        )));
        correctAnswers.add("CO₂");
        rationales.put(84,
                "RATIONALE:\n" +
                        "CO₂ (Correct answer)\n" +
                        "CO₂ is highly soluble in blood plasma compared to O₂.\n\n" +
                        "O₂ (Incorrect)\n" +
                        "O₂ is less soluble in plasma than CO₂.\n\n" +
                        "N₂ (Incorrect)\n" +
                        "Nitrogen has very low solubility in plasma.\n\n" +
                        "He (Incorrect)\n" +
                        "Helium is less soluble in blood plasma than CO₂."
        );

        questions.add("The chloride (Hamburger) shift facilitates:");
        choices.add(new ArrayList<>(Arrays.asList(
                "CO₂ uptake in tissues", // Correct answer
                "O₂ uptake in alveoli",
                "K⁺ exchange in RBCs",
                "Clotting cascade"
        )));
        correctAnswers.add("CO₂ uptake in tissues");
        rationales.put(85,
                "RATIONALE:\n" +
                        "CO₂ uptake in tissues (Correct answer)\n" +
                        "The chloride shift helps with CO₂ transport by exchanging chloride ions for bicarbonate in RBCs.\n\n" +
                        "O₂ uptake in alveoli (Incorrect)\n" +
                        "The chloride shift is related to CO₂, not O₂ uptake.\n\n" +
                        "K⁺ exchange in RBCs (Incorrect)\n" +
                        "The chloride shift involves chloride and bicarbonate, not potassium.\n\n" +
                        "Clotting cascade (Incorrect)\n" +
                        "The chloride shift is unrelated to blood clotting."
        );

        questions.add("Thickened alveolar–capillary membranes and fluid accumulation define:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary edema", // Correct answer
                "Emphysema",
                "Pneumothorax",
                "Pneumonia"
        )));
        correctAnswers.add("Pulmonary edema");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Pulmonary edema (Correct answer)\n" +
                        "Pulmonary edema is characterized by fluid accumulation in the alveoli and thickened membranes, impairing gas exchange.\n\n" +
                        "Emphysema (Incorrect)\n" +
                        "Emphysema involves alveolar destruction and decreased surface area for gas exchange.\n\n" +
                        "Pneumothorax (Incorrect)\n" +
                        "Pneumothorax is the presence of air in the pleural space, not fluid accumulation.\n\n" +
                        "Pneumonia (Incorrect)\n" +
                        "Pneumonia involves infection and inflammation, not thickening of alveolar membranes."
        );

        questions.add("During moderate exercise, pulmonary ventilation typically:");
        choices.add(new ArrayList<>(Arrays.asList(
                "↑ Respiratory rate", // Correct answer
                "↓ Tidal volume",
                "↓ Minute ventilation",
                "Remains unchanged"
        )));
        correctAnswers.add("↑ Respiratory rate");
        rationales.put(87,
                "RATIONALE:\n" +
                        "↑ Respiratory rate (Correct answer)\n" +
                        "During exercise, respiratory rate increases to enhance oxygen intake and CO₂ expulsion.\n\n" +
                        "↓ Tidal volume (Incorrect)\n" +
                        "Tidal volume increases during exercise to meet oxygen demand.\n\n" +
                        "↓ Minute ventilation (Incorrect)\n" +
                        "Minute ventilation increases with exercise.\n\n" +
                        "Remains unchanged (Incorrect)\n" +
                        "Pulmonary ventilation adapts to the increased demands of exercise."
        );

        questions.add("The A–a (alveolar–arterial) O₂ gradient increases primarily due to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Shunt", // Correct answer
                "Dead space",
                "Altitude",
                "pH changes"
        )));
        correctAnswers.add("Shunt");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Shunt (Correct answer)\n" +
                        "A shunt (e.g., atelectasis, pneumonia) causes a mismatch in ventilation and perfusion, increasing the A–a gradient.\n\n" +
                        "Dead space (Incorrect)\n" +
                        "Dead space increases ventilation but does not significantly affect the A–a gradient.\n\n" +
                        "Altitude (Incorrect)\n" +
                        "Altitude causes a decrease in PAO₂ but does not directly affect the A–a gradient in the same way a shunt does.\n\n" +
                        "pH changes (Incorrect)\n" +
                        "pH changes mainly affect hemoglobin affinity for O₂, not the A–a gradient."
        );

        questions.add("Which lymphatic channel drains most pulmonary lymph?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thoracic duct", // Correct answer
                "Right lymphatic duct",
                "Cisterna chyli",
                "Mediastinal trunk"
        )));
        correctAnswers.add("Thoracic duct");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Thoracic duct (Correct answer)\n" +
                        "The thoracic duct drains the majority of pulmonary lymph into the venous system.\n\n" +
                        "Right lymphatic duct (Incorrect)\n" +
                        "The right lymphatic duct drains the right upper quadrant of the body, not the lungs.\n\n" +
                        "Cisterna chyli (Incorrect)\n" +
                        "The cisterna chyli is the collection point of lymph but does not drain pulmonary lymph directly.\n\n" +
                        "Mediastinal trunk (Incorrect)\n" +
                        "The mediastinal trunk drains lymph from the thoracic cavity but not the majority of pulmonary lymph."
        );

        questions.add("Inspiratory capacity (IC) is the sum of which lung volumes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tidal volume + inspiratory reserve volume", // Correct answer
                "Tidal volume + residual volume",
                "Inspiratory reserve volume + expiratory reserve volume",
                "Vital capacity + residual volume"
        )));
        correctAnswers.add("Tidal volume + inspiratory reserve volume");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Tidal volume + inspiratory reserve volume (Correct answer)\n" +
                        "Inspiratory capacity is the sum of tidal volume (TV) and inspiratory reserve volume (IRV).\n\n" +
                        "Tidal volume + residual volume (Incorrect)\n" +
                        "Residual volume cannot be measured via spirometry and is not part of inspiratory capacity.\n\n" +
                        "Inspiratory reserve volume + expiratory reserve volume (Incorrect)\n" +
                        "This combination forms a different lung volume known as vital capacity, not inspiratory capacity.\n\n" +
                        "Vital capacity + residual volume (Incorrect)\n" +
                        "Vital capacity plus residual volume equals total lung capacity, not inspiratory capacity."
        );

        questions.add("Alveolar dead space refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Air in alveoli that are poorly perfused and thus not involved in exchange", // Correct answer
                "Air in conducting airways not participating in gas exchange",
                "Air remaining after maximal expiration",
                "Total lung capacity minus vital capacity"
        )));
        correctAnswers.add("Air in alveoli that are poorly perfused and thus not involved in exchange");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Air in alveoli that are poorly perfused and thus not involved in exchange (Correct answer)\n" +
                        "Alveolar dead space occurs when alveoli are ventilated but not adequately perfused for gas exchange.\n\n" +
                        "Air in conducting airways not participating in gas exchange (Incorrect)\n" +
                        "This describes anatomical dead space, not alveolar dead space.\n\n" +
                        "Air remaining after maximal expiration (Incorrect)\n" +
                        "This refers to residual volume, not dead space.\n\n" +
                        "Total lung capacity minus vital capacity (Incorrect)\n" +
                        "This calculation does not relate to dead space."
        );

        questions.add("The respiratory quotient (RQ) under normal metabolic conditions is approximately:");
        choices.add(new ArrayList<>(Arrays.asList(
                "0.8", // Correct answer
                "0.6",
                "1.0",
                "1.2"
        )));
        correctAnswers.add("0.8");
        rationales.put(92,
                "RATIONALE:\n" +
                        "0.8 (Correct answer)\n" +
                        "The normal RQ for mixed nutrient metabolism is about 0.8.\n\n" +
                        "0.6 (Incorrect)\n" +
                        "This value is typically seen in states of fat metabolism, not normal conditions.\n\n" +
                        "1.0 (Incorrect)\n" +
                        "This value indicates pure carbohydrate metabolism, which is less common in mixed conditions.\n\n" +
                        "1.2 (Incorrect)\n" +
                        "This value is seen in states of carbohydrate metabolism with increased CO₂ production."
        );

        questions.add("Peripheral chemoreceptors in the carotid and aortic bodies respond most strongly to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Low arterial O₂ (PaO₂)", // Correct answer
                "High arterial O₂ (PaO₂)",
                "High arterial CO₂ (PaCO₂)",
                "Low blood pH only"
        )));
        correctAnswers.add("Low arterial O₂ (PaO₂)");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Low arterial O₂ (PaO₂) (Correct answer)\n" +
                        "Peripheral chemoreceptors are sensitive to low PaO₂, stimulating ventilation.\n\n" +
                        "High arterial O₂ (PaO₂) (Incorrect)\n" +
                        "They do not respond significantly to high PaO₂ under normal conditions.\n\n" +
                        "High arterial CO₂ (PaCO₂) (Incorrect)\n" +
                        "Peripheral chemoreceptors are primarily sensitive to O₂, not CO₂ (central chemoreceptors respond to CO₂).\n\n" +
                        "Low blood pH only (Incorrect)\n" +
                        "While they respond to low pH, the primary stimulus is low PaO₂."
        );

        questions.add("In West’s zone 2 of the lung (mid‑lung), the relationship of pressures is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pa > PA > Pv", // Correct answer
                "PA > Pa > Pv",
                "Pa > Pv > PA",
                "Pv > Pa > PA"
        )));
        correctAnswers.add("Pa > PA > Pv");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Pa > PA > Pv (Correct answer)\n" +
                        "In zone 2 (mid-lung), arterial pressure (Pa) exceeds alveolar pressure (PA), which in turn exceeds venous pressure (Pv).\n\n" +
                        "PA > Pa > Pv (Incorrect)\n" +
                        "This is typical of zone 1 (apex of the lung), where alveolar pressure is greater than both arterial and venous pressures.\n\n" +
                        "Pa > Pv > PA (Incorrect)\n" +
                        "This relationship is more typical of zone 3 (base of the lung).\n\n" +
                        "Pv > Pa > PA (Incorrect)\n" +
                        "This describes an abnormal condition, not zone 2."
        );

        questions.add("According to Fick’s law of diffusion, which change would increase the rate of gas transfer across the respiratory membrane?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased surface area of the respiratory membrane", // Correct answer
                "Increased membrane thickness",
                "Decreased partial‑pressure gradient",
                "Reduced surface area of the respiratory membrane"
        )));
        correctAnswers.add("Increased surface area of the respiratory membrane");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Increased surface area of the respiratory membrane (Correct answer)\n" +
                        "An increase in surface area increases the rate of gas transfer across the membrane according to Fick's law.\n\n" +
                        "Increased membrane thickness (Incorrect)\n" +
                        "Fick’s law states that increased membrane thickness decreases the rate of diffusion.\n\n" +
                        "Decreased partial‑pressure gradient (Incorrect)\n" +
                        "A decreased partial-pressure gradient reduces the rate of diffusion.\n\n" +
                        "Reduced surface area of the respiratory membrane (Incorrect)\n" +
                        "Reducing surface area would decrease gas exchange efficiency."
        );

        questions.add("Which pontine center promotes prolonged inspiration and helps prevent alveolar collapse?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Apneustic center", // Correct answer
                "Dorsal respiratory group",
                "Ventral respiratory group",
                "Pneumotaxic center"
        )));
        correctAnswers.add("Apneustic center");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Apneustic center (Correct answer)\n" +
                        "The apneustic center promotes prolonged inspiration and prevents premature termination of the inspiratory phase.\n\n" +
                        "Dorsal respiratory group (Incorrect)\n" +
                        "This group is involved in rhythmic breathing but not in prolonged inspiration.\n\n" +
                        "Ventral respiratory group (Incorrect)\n" +
                        "This group is primarily responsible for forced breathing, not prolonged inspiration.\n\n" +
                        "Pneumotaxic center (Incorrect)\n" +
                        "This center regulates the rate and pattern of breathing by inhibiting the apneustic center."
        );

        questions.add("The ventral respiratory group (VRG) in the medulla primarily controls:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Forced inhalation and forced exhalation", // Correct answer
                "The basic rhythmic pattern of quiet breathing",
                "Rate modulation via lung stretch receptors",
                "Integration of olfactory stimuli into breathing"
        )));
        correctAnswers.add("Forced inhalation and forced exhalation");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Forced inhalation and forced exhalation (Correct answer)\n" +
                        "The VRG controls the muscles involved in forced breathing, such as during exercise or stress.\n\n" +
                        "The basic rhythmic pattern of quiet breathing (Incorrect)\n" +
                        "The basic rhythm is controlled by the dorsal respiratory group (DRG), not VRG.\n\n" +
                        "Rate modulation via lung stretch receptors (Incorrect)\n" +
                        "This is the function of the DRG.\n\n" +
                        "Integration of olfactory stimuli into breathing (Incorrect)\n" +
                        "This is not a function of the VRG."
        );

        questions.add("“Hysteresis” in lung compliance describes:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The difference in the pressure–volume curve during inflation versus deflation", // Correct answer
                "Rapid alveolar collapse when surfactant is absent",
                "Identical compliance for lung inflation and deflation",
                "A sudden increase in lung volume at constant pressure"
        )));
        correctAnswers.add("The difference in the pressure–volume curve during inflation versus deflation");
        rationales.put(98,
                "RATIONALE:\n" +
                        "The difference in the pressure–volume curve during inflation versus deflation (Correct answer)\n" +
                        "Hysteresis refers to the difference in lung volume at the same pressure during inhalation and exhalation due to the time delay in alveolar expansion and collapse.\n\n" +
                        "Rapid alveolar collapse when surfactant is absent (Incorrect)\n" +
                        "This describes a different mechanism, such as in acute respiratory distress syndrome (ARDS).\n\n" +
                        "Identical compliance for lung inflation and deflation (Incorrect)\n" +
                        "Hysteresis shows that lung compliance differs during these two phases.\n\n" +
                        "A sudden increase in lung volume at constant pressure (Incorrect)\n" +
                        "This does not describe hysteresis."
        );

        questions.add("Which medullary group is chiefly responsible for initiating inspiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dorsal respiratory group", // Correct answer
                "Ventral respiratory group",
                "Apneustic center",
                "Pneumotaxic center"
        )));
        correctAnswers.add("Dorsal respiratory group");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Dorsal respiratory group (Correct answer)\n" +
                        "The DRG is primarily responsible for initiating the rhythmic pattern of quiet breathing and controls the basic rhythm of inspiration.\n\n" +
                        "Ventral respiratory group (Incorrect)\n" +
                        "The VRG is more involved in forced breathing and exhalation.\n\n" +
                        "Apneustic center (Incorrect)\n" +
                        "This center promotes prolonged inspiration but doesn’t initiate it.\n\n" +
                        "Pneumotaxic center (Incorrect)\n" +
                        "This center regulates the rate and pattern of breathing but does not initiate inspiration."
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
        new AlertDialog.Builder(ChallengeMode5.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
