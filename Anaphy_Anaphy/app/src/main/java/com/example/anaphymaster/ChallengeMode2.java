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

public class ChallengeMode2 extends AppCompatActivity {

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
        AverageHelper averageHelper = new AverageHelper(this);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.challenge_mode2);

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
                Toast.makeText(ChallengeMode2.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode2.this)
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
            new AlertDialog.Builder(ChallengeMode2.this)
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
                new AlertDialog.Builder(ChallengeMode2.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode2.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Cardiovascular System", correctAnswersCount, totalQuestions);


                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Cardiovascular System");
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
                        Intent intent = new Intent(ChallengeMode2.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode2.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode2.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Cardiovascular System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Cardiovascular System");
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
        new AlertDialog.Builder(ChallengeMode2.this)
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

        questions.add("Which chamber of the heart receives deoxygenated blood from the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Right Atrium", // Correct answer
                "Left Atrium",
                "Left Ventricle",
                "Right Ventricle"
        )));
        correctAnswers.add("Right Atrium");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Right Atrium (Correct answer)\n" +
                        "The right atrium receives deoxygenated blood from the superior and inferior vena cava and the coronary sinus.\n\n" +
                        "Left Atrium (Incorrect)\n" +
                        "The left atrium receives oxygenated blood from the lungs, not deoxygenated blood.\n\n" +
                        "Left Ventricle (Incorrect)\n" +
                        "The left ventricle pumps oxygenated blood into the aorta.\n\n" +
                        "Right Ventricle (Incorrect)\n" +
                        "The right ventricle pumps blood to the lungs, not where deoxygenated blood first enters."
        );

        questions.add("What is the function of the pericardial cavity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Reduces friction around the heart", // Correct answer
                "Pumps blood through the heart",
                "Houses cardiac valves",
                "Prevents backflow of blood"
        )));
        correctAnswers.add("Reduces friction around the heart");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Reduces friction around the heart (Correct answer)\n" +
                        "The pericardial cavity contains serous fluid, which lubricates and reduces friction as the heart beats.\n\n" +
                        "Pumps blood through the heart (Incorrect)\n" +
                        "This is the myocardium’s role.\n\n" +
                        "Houses cardiac valves (Incorrect)\n" +
                        "The heart chambers and connective structures do this.\n\n" +
                        "Prevents backflow of blood (Incorrect)\n" +
                        "Valves perform that function."
        );

        questions.add("Which valve prevents backflow of blood from the left ventricle to the left atrium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bicuspid (Mitral) valve", // Correct answer
                "Aortic valve",
                "Tricuspid valve",
                "Pulmonary valve"
        )));
        correctAnswers.add("Bicuspid (Mitral) valve");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Bicuspid (Mitral) valve (Correct answer)\n" +
                        "Located between the left atrium and left ventricle, the mitral valve prevents backflow into the atrium during ventricular contraction.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve prevents backflow into the left ventricle from the aorta.\n\n" +
                        "Tricuspid valve (Incorrect)\n" +
                        "Found between the right atrium and right ventricle.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve prevents backflow from the pulmonary artery into the right ventricle."
        );

        questions.add("Which layer of the heart is responsible for its pumping action?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myocardium", // Correct answer
                "Endocardium",
                "Epicardium",
                "Pericardium"
        )));
        correctAnswers.add("Myocardium");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Myocardium (Correct answer)\n" +
                        "The myocardium is the thick muscular middle layer responsible for contraction.\n\n" +
                        "Endocardium (Incorrect)\n" +
                        "The endocardium lines the inner chambers but doesn’t contract.\n\n" +
                        "Epicardium (Incorrect)\n" +
                        "The epicardium is the outer layer of the heart wall.\n\n" +
                        "Pericardium (Incorrect)\n" +
                        "The pericardium is the protective sac outside the heart wall."
        );

        questions.add("What is the correct sequence of blood flow through the pulmonary circulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Right ventricle → Pulmonary artery → Lungs → Pulmonary vein → Left atrium", // Correct answer
                "Right atrium → Right ventricle → Pulmonary artery → Lungs",
                "Left ventricle → Aorta → Lungs → Pulmonary vein",
                "Right atrium → Aorta → Lungs → Left atrium"
        )));
        correctAnswers.add("Right ventricle → Pulmonary artery → Lungs → Pulmonary vein → Left atrium");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Right ventricle → Pulmonary artery → Lungs → Pulmonary vein → Left atrium (Correct answer)\n" +
                        "This pathway ensures deoxygenated blood is sent to the lungs for oxygenation and returns oxygenated blood to the heart.\n\n" +
                        "Right atrium → Right ventricle → Pulmonary artery → Lungs (Incorrect)\n" +
                        "This starts correctly but ends too early.\n\n" +
                        "Left ventricle → Aorta → Lungs → Pulmonary vein (Incorrect)\n" +
                        "This is systemic circulation, not pulmonary.\n\n" +
                        "Right atrium → Aorta → Lungs → Left atrium (Incorrect)\n" +
                        "The aorta is not part of pulmonary circulation."
        );

        questions.add("What structure prevents the backflow of blood into the right atrium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve", // Correct answer
                "Aortic valve",
                "Pulmonary valve",
                "Mitral valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve is located between the right atrium and right ventricle and prevents backflow into the right atrium during ventricular contraction.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve prevents backflow into the left ventricle, not the atrium.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle.\n\n" +
                        "Mitral valve (Incorrect)\n" +
                        "The mitral valve is found between the left atrium and ventricle."
        );

        questions.add("Which of the following best describes the function of the aorta?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Distributes oxygenated blood to the body", // Correct answer
                "Receives blood from the lungs",
                "Carries blood to the lungs",
                "Returns blood to the heart"
        )));
        correctAnswers.add("Distributes oxygenated blood to the body");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Distributes oxygenated blood to the body (Correct answer)\n" +
                        "The aorta is the main artery that carries oxygen-rich blood from the left ventricle to all parts of the body.\n\n" +
                        "Receives blood from the lungs (Incorrect)\n" +
                        "The pulmonary veins, not the aorta, receive blood from the lungs.\n\n" +
                        "Carries blood to the lungs (Incorrect)\n" +
                        "The pulmonary artery carries blood to the lungs.\n\n" +
                        "Returns blood to the heart (Incorrect)\n" +
                        "Veins (like the vena cava) return blood to the heart."
        );

        questions.add("What is the function of the semilunar valves?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prevent backflow into the ventricles", // Correct answer
                "Prevent backflow into the atria",
                "Regulate blood pressure",
                "Open to allow atrial filling"
        )));
        correctAnswers.add("Prevent backflow into the ventricles");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Prevent backflow into the ventricles (Correct answer)\n" +
                        "The semilunar valves (aortic and pulmonary) prevent the blood from flowing back into the ventricles after ejection.\n\n" +
                        "Prevent backflow into the atria (Incorrect)\n" +
                        "This is the role of AV valves, not semilunar valves.\n\n" +
                        "Regulate blood pressure (Incorrect)\n" +
                        "Semilunar valves don’t directly regulate blood pressure.\n\n" +
                        "Open to allow atrial filling (Incorrect)\n" +
                        "Atrial filling is unrelated to the function of semilunar valves."
        );

        questions.add("Which component of the conduction system is responsible for ventricular contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Purkinje fibers", // Correct answer
                "SA node",
                "AV node",
                "Bundle of His"
        )));
        correctAnswers.add("Purkinje fibers");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Purkinje fibers (Correct answer)\n" +
                        "Purkinje fibers rapidly conduct impulses through the ventricles, triggering ventricular contraction.\n\n" +
                        "SA node (Incorrect)\n" +
                        "The SA node initiates the heartbeat but does not directly cause ventricular contraction.\n\n" +
                        "AV node (Incorrect)\n" +
                        "The AV node delays the impulse, not responsible for contraction.\n\n" +
                        "Bundle of His (Incorrect)\n" +
                        "The Bundle of His conducts impulses but doesn’t trigger contraction directly."
        );

        questions.add("Which of the following best defines stroke volume?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood volume ejected per heartbeat", // Correct answer
                "Volume of blood in the lungs",
                "Blood volume ejected per minute",
                "Volume of blood in the aorta"
        )));
        correctAnswers.add("Blood volume ejected per heartbeat");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Blood volume ejected per heartbeat (Correct answer)\n" +
                        "Stroke volume is the amount of blood ejected by the ventricle with each heartbeat.\n\n" +
                        "Volume of blood in the lungs (Incorrect)\n" +
                        "This is not a functional definition of stroke volume.\n\n" +
                        "Blood volume ejected per minute (Incorrect)\n" +
                        "That’s cardiac output (HR × SV), not stroke volume.\n\n" +
                        "Volume of blood in the aorta (Incorrect)\n" +
                        "The aorta temporarily holds blood, but it is not the total stroke volume."
        );

        questions.add("What blood vessel carries oxygenated blood from the lungs to the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary vein", // Correct answer
                "Pulmonary artery",
                "Aorta",
                "Superior vena cava"
        )));
        correctAnswers.add("Pulmonary vein");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Pulmonary vein (Correct answer)\n" +
                        "The pulmonary veins are unique as they carry oxygenated blood from the lungs to the left atrium of the heart.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "Carries deoxygenated blood from the right ventricle to the lungs.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "Carries oxygenated blood from the heart to the body, not from the lungs.\n\n" +
                        "Superior vena cava (Incorrect)\n" +
                        "Returns deoxygenated blood from the upper body to the right atrium."
        );

        questions.add("Which part of the conduction system delays the electrical impulse to allow atrial contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Atrioventricular (AV) node", // Correct answer
                "Sinoatrial (SA) node",
                "Bundle of His",
                "Purkinje fibers"
        )));
        correctAnswers.add("Atrioventricular (AV) node");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Atrioventricular (AV) node (Correct answer)\n" +
                        "The AV node delays the electrical impulse so the atria can contract and empty before the ventricles contract.\n\n" +
                        "Sinoatrial (SA) node (Incorrect)\n" +
                        "Initiates heartbeat but doesn’t delay it.\n\n" +
                        "Bundle of His (Incorrect)\n" +
                        "Conducts impulses to the ventricles.\n\n" +
                        "Purkinje fibers (Incorrect)\n" +
                        "Spread the impulse through ventricles for contraction."
        );

        questions.add("What is the normal average value for blood pressure in a healthy adult?");
        choices.add(new ArrayList<>(Arrays.asList(
                "120/80 mmHg", // Correct answer
                "110/70 mmHg",
                "130/90 mmHg",
                "100/60 mmHg"
        )));
        correctAnswers.add("120/80 mmHg");
        rationales.put(12,
                "RATIONALE:\n" +
                        "120/80 mmHg (Correct answer)\n" +
                        "This is considered the standard normal blood pressure for a healthy adult.\n\n" +
                        "110/70 mmHg (Incorrect)\n" +
                        "Slightly low; may be normal for some, but not the average.\n\n" +
                        "130/90 mmHg (Incorrect)\n" +
                        "Borderline high – considered prehypertension.\n\n" +
                        "100/60 mmHg (Incorrect)\n" +
                        "Considered low blood pressure (hypotension)."
        );

        questions.add("Which of the following best describes systole?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Contraction of the heart", // Correct answer
                "Ventricular filling phase",
                "Relaxation of the heart",
                "Closure of semilunar valves"
        )));
        correctAnswers.add("Contraction of the heart");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Contraction of the heart (Correct answer)\n" +
                        "Systole refers to the contraction phase of the heart when blood is pumped out of the chambers.\n\n" +
                        "Ventricular filling phase (Incorrect)\n" +
                        "Occurs during diastole, not systole.\n\n" +
                        "Relaxation of the heart (Incorrect)\n" +
                        "This is diastole, not systole.\n\n" +
                        "Closure of semilunar valves (Incorrect)\n" +
                        "This is a result of pressure changes, not a definition of systole."
        );

        questions.add("Which vessel type has the thickest tunica media?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Arteries", // Correct answer
                "Veins",
                "Capillaries",
                "Venules"
        )));
        correctAnswers.add("Arteries");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Arteries (Correct answer)\n" +
                        "Arteries have a thick tunica media made of smooth muscle, allowing them to withstand and regulate high pressure.\n\n" +
                        "Veins (Incorrect)\n" +
                        "Have thinner walls and a larger lumen.\n\n" +
                        "Capillaries (Incorrect)\n" +
                        "Only a single endothelial layer.\n\n" +
                        "Venules (Incorrect)\n" +
                        "Small vessels with thin walls."
        );

        questions.add("What sound is produced by closure of the atrioventricular valves?");
        choices.add(new ArrayList<>(Arrays.asList(
                "S1 (Lub)", // Correct answer
                "S2 (Dub)",
                "Murmur",
                "Click"
        )));
        correctAnswers.add("S1 (Lub)");
        rationales.put(15,
                "RATIONALE:\n" +
                        "S1 (Lub) (Correct answer)\n" +
                        "The first heart sound (S1), or 'lub,' occurs when the AV valves (mitral and tricuspid) close at the beginning of ventricular systole.\n\n" +
                        "S2 (Dub) (Incorrect)\n" +
                        "Is the closure of semilunar valves.\n\n" +
                        "Murmur (Incorrect)\n" +
                        "Abnormal sounds due to turbulent flow.\n\n" +
                        "Click (Incorrect)\n" +
                        "Could indicate valve problems, not normal sounds."
        );

        questions.add("Which structure is known as the pacemaker of the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "SA node", // Correct answer
                "AV node",
                "Purkinje fibers",
                "Left atrium"
        )));
        correctAnswers.add("SA node");
        rationales.put(16,
                "RATIONALE:\n" +
                        "SA node (Correct answer)\n" +
                        "The sinoatrial (SA) node initiates the electrical impulses, setting the rhythm of the heartbeat.\n\n" +
                        "AV node (Incorrect)\n" +
                        "The AV node delays the impulse, but it does not initiate it.\n\n" +
                        "Purkinje fibers (Incorrect)\n" +
                        "The Purkinje fibers are responsible for spreading the electrical impulse in the ventricles, not initiating it.\n\n" +
                        "Left atrium (Incorrect)\n" +
                        "The left atrium contains the SA node, but it is not the pacemaker itself."
        );

        questions.add("What component of cardiac output increases when stroke volume increases?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cardiac output", // Correct answer
                "Heart rate",
                "Cardiac cycle",
                "Blood pressure"
        )));
        correctAnswers.add("Cardiac output");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Cardiac output (Correct answer)\n" +
                        "Cardiac Output = Heart Rate × Stroke Volume, so increasing stroke volume increases cardiac output.\n\n" +
                        "Heart rate (Incorrect)\n" +
                        "Heart rate may remain constant; it is not directly affected by stroke volume.\n\n" +
                        "Cardiac cycle (Incorrect)\n" +
                        "The cardiac cycle refers to the phases of systole and diastole, not the volume.\n\n" +
                        "Blood pressure (Incorrect)\n" +
                        "Blood pressure may rise with increased cardiac output but isn't directly calculated from stroke volume."
        );

        questions.add("What type of circulation supplies the heart muscle itself?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Coronary", // Correct answer
                "Pulmonary",
                "Systemic",
                "Portal"
        )));
        correctAnswers.add("Coronary");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Coronary (Correct answer)\n" +
                        "Coronary circulation delivers oxygenated blood to the myocardium through the coronary arteries.\n\n" +
                        "Pulmonary (Incorrect)\n" +
                        "Pulmonary circulation circulates blood between the heart and lungs, not to the heart muscle itself.\n\n" +
                        "Systemic (Incorrect)\n" +
                        "Systemic circulation delivers blood to the rest of the body, not to the heart muscle.\n\n" +
                        "Portal (Incorrect)\n" +
                        "Portal circulation refers to the venous system that connects organs like the liver, not the heart."
        );

        questions.add("Which heart valve lies between the right atrium and right ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve", // Correct answer
                "Pulmonary valve",
                "Aortic valve",
                "Mitral valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve separates the right atrium and right ventricle, preventing backflow during contraction.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve is located between the right ventricle and pulmonary artery.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve is located between the left ventricle and aorta.\n\n" +
                        "Mitral valve (Incorrect)\n" +
                        "The mitral valve is located between the left atrium and left ventricle."
        );

        questions.add("What is the role of the coronary arteries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Supply blood to the heart muscle", // Correct answer
                "Carry oxygenated blood to the brain",
                "Carry deoxygenated blood to the lungs",
                "Transport blood to the abdominal organs"
        )));
        correctAnswers.add("Supply blood to the heart muscle");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Supply blood to the heart muscle (Correct answer)\n" +
                        "The coronary arteries supply oxygenated blood to the heart muscle to meet its metabolic demands.\n\n" +
                        "Carry oxygenated blood to the brain (Incorrect)\n" +
                        "Blood to the brain is supplied by the carotid arteries.\n\n" +
                        "Carry deoxygenated blood to the lungs (Incorrect)\n" +
                        "The pulmonary arteries carry deoxygenated blood to the lungs.\n\n" +
                        "Transport blood to the abdominal organs (Incorrect)\n" +
                        "The abdominal aorta supplies blood to the abdominal organs."
        );

        questions.add("What is the function of the sinoatrial (SA) node?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Initiate the heartbeat", // Correct answer
                "Delay the electrical impulse",
                "Conduct impulses to the ventricles",
                "Prevent backflow of blood"
        )));
        correctAnswers.add("Initiate the heartbeat");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Initiate the heartbeat (Correct answer)\n" +
                        "The SA node is known as the heart's pacemaker, responsible for initiating electrical impulses that trigger the heartbeat.\n\n" +
                        "Delay the electrical impulse (Incorrect)\n" +
                        "The AV node delays the impulse.\n\n" +
                        "Conduct impulses to the ventricles (Incorrect)\n" +
                        "The Purkinje fibers conduct impulses to the ventricles.\n\n" +
                        "Prevent backflow of blood (Incorrect)\n" +
                        "The semilunar valves prevent backflow, not the SA node."
        );

        questions.add("Which of the following is the first structure blood encounters as it leaves the left ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Aorta", // Correct answer
                "Pulmonary artery",
                "Left atrium",
                "Pulmonary vein"
        )));
        correctAnswers.add("Aorta");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Aorta (Correct answer)\n" +
                        "The aorta is the first major vessel that blood encounters after being ejected from the left ventricle.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "The pulmonary artery is part of pulmonary circulation, not systemic.\n\n" +
                        "Left atrium (Incorrect)\n" +
                        "Blood flows from the left ventricle to the aorta, not the left atrium.\n\n" +
                        "Pulmonary vein (Incorrect)\n" +
                        "Pulmonary veins carry oxygenated blood from the lungs to the left atrium."
        );

        questions.add("Which valve prevents backflow of blood from the left ventricle to the left atrium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mitral valve", // Correct answer
                "Pulmonary valve",
                "Tricuspid valve",
                "Aortic valve"
        )));
        correctAnswers.add("Mitral valve");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Mitral valve (Correct answer)\n" +
                        "The mitral valve (bicuspid) prevents backflow into the left atrium during left ventricular contraction.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle.\n\n" +
                        "Tricuspid valve (Incorrect)\n" +
                        "The tricuspid valve is between the right atrium and ventricle.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve prevents backflow into the left ventricle from the aorta."
        );

        questions.add("What is the function of the capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Allow for gas and nutrient exchange", // Correct answer
                "Carry oxygenated blood to the body",
                "Carry deoxygenated blood to the lungs",
                "Control blood pressure"
        )));
        correctAnswers.add("Allow for gas and nutrient exchange");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Allow for gas and nutrient exchange (Correct answer)\n" +
                        "Capillaries are the site of exchange where oxygen, carbon dioxide, nutrients, and wastes move between the blood and tissues.\n\n" +
                        "Carry oxygenated blood to the body (Incorrect)\n" +
                        "Arteries carry blood away from the heart, but capillaries facilitate exchange.\n\n" +
                        "Carry deoxygenated blood to the lungs (Incorrect)\n" +
                        "Pulmonary arteries carry blood to the lungs, not the capillaries.\n\n" +
                        "Control blood pressure (Incorrect)\n" +
                        "Blood pressure is regulated by factors like vascular resistance, not by capillaries."
        );

        questions.add("Which part of the heart receives oxygenated blood from the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left atrium", // Correct answer
                "Right atrium",
                "Right ventricle",
                "Left ventricle"
        )));
        correctAnswers.add("Left atrium");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Left atrium (Correct answer)\n" +
                        "The left atrium receives oxygenated blood from the lungs via the pulmonary veins.\n\n" +
                        "Right atrium (Incorrect)\n" +
                        "The right atrium receives deoxygenated blood from the body.\n\n" +
                        "Right ventricle (Incorrect)\n" +
                        "The right ventricle pumps deoxygenated blood to the lungs.\n\n" +
                        "Left ventricle (Incorrect)\n" +
                        "The left ventricle pumps oxygenated blood into the aorta, but it doesn't receive it."
        );

        questions.add("Which of the following valves is located between the right atrium and right ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve", // Correct answer
                "Mitral valve",
                "Aortic valve",
                "Pulmonary valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve is located between the right atrium and right ventricle, preventing backflow into the atrium during ventricular contraction.\n\n" +
                        "Mitral valve (Incorrect)\n" +
                        "The mitral valve is between the left atrium and ventricle.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve is between the left ventricle and aorta.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve is located between the right ventricle and pulmonary artery."
        );

        questions.add("Which layer of the heart is responsible for its contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myocardium", // Correct answer
                "Endocardium",
                "Epicardium",
                "Pericardium"
        )));
        correctAnswers.add("Myocardium");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Myocardium (Correct answer)\n" +
                        "The myocardium is the muscular layer of the heart that is responsible for the heart's contraction.\n\n" +
                        "Endocardium (Incorrect)\n" +
                        "The endocardium lines the chambers and valves but does not contract.\n\n" +
                        "Epicardium (Incorrect)\n" +
                        "The epicardium is the outermost layer of the heart.\n\n" +
                        "Pericardium (Incorrect)\n" +
                        "The pericardium is the protective sac around the heart."
        );

        questions.add("Which of the following is NOT a function of the cardiovascular system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtering metabolic waste", // Correct answer
                "Transporting nutrients",
                "Protecting vital organs",
                "Regulating body temperature"
        )));
        correctAnswers.add("Filtering metabolic waste");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Filtering metabolic waste (Correct answer)\n" +
                        "While the cardiovascular system transports nutrients, oxygen, and hormones, metabolic waste is filtered primarily by the kidneys.\n\n" +
                        "Transporting nutrients (Incorrect)\n" +
                        "The cardiovascular system transports nutrients.\n\n" +
                        "Protecting vital organs (Incorrect)\n" +
                        "The system helps protect vital organs by providing circulation.\n\n" +
                        "Regulating body temperature (Incorrect)\n" +
                        "The system helps regulate body temperature by distributing heat."
        );

        questions.add("Which of the following factors does NOT affect cardiac output?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary circulation", // Correct answer
                "Heart rate",
                "Stroke volume",
                "Preload"
        )));
        correctAnswers.add("Pulmonary circulation");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Pulmonary circulation (Correct answer)\n" +
                        "Pulmonary circulation refers to blood flow to the lungs and does not directly affect cardiac output.\n\n" +
                        "Heart rate (Incorrect)\n" +
                        "Heart rate affects cardiac output (CO = HR × SV).\n\n" +
                        "Stroke volume (Incorrect)\n" +
                        "Stroke volume is the amount of blood ejected per heartbeat.\n\n" +
                        "Preload (Incorrect)\n" +
                        "Preload affects the volume of blood filling the heart and impacts cardiac output."
        );

        questions.add("What is the purpose of the baroreceptors in the cardiovascular system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Monitor blood pressure", // Correct answer
                "Detect blood gases",
                "Regulate heart rate",
                "Detect pain"
        )));
        correctAnswers.add("Monitor blood pressure");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Monitor blood pressure (Correct answer)\n" +
                        "Baroreceptors detect changes in blood pressure and help regulate cardiovascular function.\n\n" +
                        "Detect blood gases (Incorrect)\n" +
                        "Chemoreceptors detect blood gases, not baroreceptors.\n\n" +
                        "Regulate heart rate (Incorrect)\n" +
                        "Baroreceptors affect heart rate by influencing autonomic control, but they do not directly regulate it.\n\n" +
                        "Detect pain (Incorrect)\n" +
                        "Pain is not the primary function of baroreceptors."
        );

        questions.add("Which vessel carries oxygenated blood from the lungs to the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary vein", // Correct answer
                "Pulmonary artery",
                "Aorta",
                "Vena cava"
        )));
        correctAnswers.add("Pulmonary vein");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Pulmonary vein (Correct answer)\n" +
                        "The pulmonary veins carry oxygenated blood from the lungs to the left atrium of the heart.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "The pulmonary artery carries deoxygenated blood to the lungs.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "The aorta carries oxygenated blood from the heart to the body.\n\n" +
                        "Vena cava (Incorrect)\n" +
                        "The vena cava carries deoxygenated blood to the heart."
        );

        questions.add("Which of the following is responsible for the \"lub\" sound of the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Closing of the atrioventricular (AV) valves", // Correct answer
                "Closing of the semilunar valves",
                "Ventricular contraction",
                "Atrial contraction"
        )));
        correctAnswers.add("Closing of the atrioventricular (AV) valves");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Closing of the atrioventricular (AV) valves (Correct answer)\n" +
                        "The \"lub\" sound (S1) is caused by the closure of the AV valves (mitral and tricuspid) as the ventricles contract.\n\n" +
                        "Closing of the semilunar valves (Incorrect)\n" +
                        "The \"dub\" sound (S2) is caused by the closing of the semilunar valves.\n\n" +
                        "Ventricular contraction (Incorrect)\n" +
                        "Ventricular contraction does not produce the heart sounds directly.\n\n" +
                        "Atrial contraction (Incorrect)\n" +
                        "Atrial contraction does not directly create heart sounds."
        );

        questions.add("Which of the following is the most important factor influencing stroke volume?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Contractility of the heart muscle", // Correct answer
                "Heart rate",
                "Blood volume",
                "Blood vessel resistance"
        )));
        correctAnswers.add("Contractility of the heart muscle");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Contractility of the heart muscle (Correct answer)\n" +
                        "Contractility refers to the strength of ventricular contraction, which directly impacts stroke volume (the volume of blood pumped with each heartbeat).\n\n" +
                        "Heart rate (Incorrect)\n" +
                        "Heart rate affects cardiac output but not directly stroke volume.\n\n" +
                        "Blood volume (Incorrect)\n" +
                        "Blood volume affects preload, but contractility is the primary factor influencing stroke volume.\n\n" +
                        "Blood vessel resistance (Incorrect)\n" +
                        "Blood vessel resistance impacts afterload, but does not directly influence stroke volume."
        );

        questions.add("The QRS complex in an ECG represents:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ventricular depolarization", // Correct answer
                "Atrial depolarization",
                "Atrial repolarization",
                "Ventricular repolarization"
        )));
        correctAnswers.add("Ventricular depolarization");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Ventricular depolarization (Correct answer)\n" +
                        "The QRS complex represents the electrical activity associated with ventricular depolarization, leading to ventricular contraction.\n\n" +
                        "Atrial depolarization (Incorrect)\n" +
                        "Atrial depolarization is represented by the P wave.\n\n" +
                        "Atrial repolarization (Incorrect)\n" +
                        "Atrial repolarization occurs during the QRS complex, but it is not the cause of the complex.\n\n" +
                        "Ventricular repolarization (Incorrect)\n" +
                        "Ventricular repolarization is represented by the T wave."
        );

        questions.add("The function of the heart valves is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prevent blood from flowing backward", // Correct answer
                "Prevent blood flow between the chambers",
                "Increase heart rate",
                "Regulate blood pressure"
        )));
        correctAnswers.add("Prevent blood from flowing backward");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Prevent blood from flowing backward (Correct answer)\n" +
                        "The primary function of the heart valves is to ensure that blood flows in the correct direction and prevent backflow (regurgitation) between heart chambers.\n\n" +
                        "Prevent blood flow between the chambers (Incorrect)\n" +
                        "Heart valves regulate blood flow but do not prevent flow between chambers entirely.\n\n" +
                        "Increase heart rate (Incorrect)\n" +
                        "Heart rate is regulated by the SA node, not the valves.\n\n" +
                        "Regulate blood pressure (Incorrect)\n" +
                        "Blood pressure is regulated by the circulatory system but not directly by the valves."
        );

        questions.add("The primary function of the right ventricle is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pump deoxygenated blood to the lungs", // Correct answer
                "Pump oxygenated blood to the body",
                "Pump deoxygenated blood to the body",
                "Receive oxygenated blood from the lungs"
        )));
        correctAnswers.add("Pump deoxygenated blood to the lungs");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Pump deoxygenated blood to the lungs (Correct answer)\n" +
                        "The right ventricle pumps deoxygenated blood to the lungs via the pulmonary artery for oxygenation.\n\n" +
                        "Pump oxygenated blood to the body (Incorrect)\n" +
                        "The left ventricle pumps oxygenated blood to the body.\n\n" +
                        "Pump deoxygenated blood to the body (Incorrect)\n" +
                        "The right ventricle does not pump blood to the body; it pumps it to the lungs.\n\n" +
                        "Receive oxygenated blood from the lungs (Incorrect)\n" +
                        "Oxygenated blood is received by the left atrium, not the right ventricle."
        );

        questions.add("Which of the following is NOT part of the conduction system of the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Coronary arteries", // Correct answer
                "SA Node",
                "AV Node",
                "Bundle of His"
        )));
        correctAnswers.add("Coronary arteries");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Coronary arteries (Correct answer)\n" +
                        "The coronary arteries supply blood to the heart muscle but are not part of the heart's electrical conduction system. The SA node, AV node, and Bundle of His are part of the conduction system.\n\n" +
                        "SA Node, AV Node, Bundle of His (Incorrect)\n" +
                        "These structures are involved in the heart's electrical conduction, initiating and propagating impulses."
        );

        questions.add("What is the effect of sympathetic nervous system stimulation on the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increases heart rate", // Correct answer
                "Decreases heart rate",
                "Decreases blood pressure",
                "Increases stroke volume only"
        )));
        correctAnswers.add("Increases heart rate");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Increases heart rate (Correct answer)\n" +
                        "The sympathetic nervous system increases heart rate and contractility, preparing the body for 'fight or flight' responses.\n\n" +
                        "Decreases heart rate (Incorrect)\n" +
                        "The parasympathetic nervous system slows the heart rate.\n\n" +
                        "Decreases blood pressure (Incorrect)\n" +
                        "Sympathetic stimulation generally increases blood pressure.\n\n" +
                        "Increases stroke volume only (Incorrect)\n" +
                        "Sympathetic stimulation increases both heart rate and stroke volume."
        );

        questions.add("Which of the following is the primary site of gas exchange in the cardiovascular system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Capillaries", // Correct answer
                "Arteries",
                "Veins",
                "Heart"
        )));
        correctAnswers.add("Capillaries");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Capillaries (Correct answer)\n" +
                        "Capillaries are the site of exchange for oxygen, carbon dioxide, and nutrients between the blood and the tissues.\n\n" +
                        "Arteries (Incorrect)\n" +
                        "Arteries carry blood away from the heart but are not involved in gas exchange.\n\n" +
                        "Veins (Incorrect)\n" +
                        "Veins return blood to the heart, not involved in exchange.\n\n" +
                        "Heart (Incorrect)\n" +
                        "The heart pumps blood but does not directly exchange gases."
        );

        questions.add("The T wave on an ECG represents:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ventricular repolarization", // Correct answer
                "Atrial depolarization",
                "Ventricular depolarization",
                "Atrial repolarization"
        )));
        correctAnswers.add("Ventricular repolarization");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Ventricular repolarization (Correct answer)\n" +
                        "The T wave represents ventricular repolarization, the process by which the ventricles recover electrically after contraction.\n\n" +
                        "Atrial depolarization (Incorrect)\n" +
                        "Atrial depolarization is represented by the P wave.\n\n" +
                        "Ventricular depolarization (Incorrect)\n" +
                        "Ventricular depolarization is represented by the QRS complex.\n\n" +
                        "Atrial repolarization (Incorrect)\n" +
                        "Atrial repolarization occurs during the QRS complex."
        );

        questions.add("What is the primary function of the left atrium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Receive oxygenated blood from the lungs", // Correct answer
                "Pump oxygenated blood to the body",
                "Receive deoxygenated blood from the body",
                "Pump deoxygenated blood to the lungs"
        )));
        correctAnswers.add("Receive oxygenated blood from the lungs");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Receive oxygenated blood from the lungs (Correct answer)\n" +
                        "The left atrium receives oxygenated blood from the lungs via the pulmonary veins.\n\n" +
                        "Pump oxygenated blood to the body (Incorrect)\n" +
                        "The left ventricle pumps oxygenated blood to the body.\n\n" +
                        "Receive deoxygenated blood from the body (Incorrect)\n" +
                        "The right atrium receives deoxygenated blood from the body.\n\n" +
                        "Pump deoxygenated blood to the lungs (Incorrect)\n" +
                        "The right ventricle pumps deoxygenated blood to the lungs."
        );

        questions.add("The term 'preload' refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The volume of blood in the ventricles at the end of diastole", // Correct answer
                "The pressure the ventricles generate to pump blood to the lungs",
                "The resistance the ventricles must overcome to pump blood",
                "The volume of blood returning to the heart"
        )));
        correctAnswers.add("The volume of blood in the ventricles at the end of diastole");
        rationales.put(42,
                "RATIONALE:\n" +
                        "The volume of blood in the ventricles at the end of diastole (Correct answer)\n" +
                        "Preload is the stretch of the ventricular walls at the end of diastole, which is determined by the volume of blood returning to the heart.\n\n" +
                        "The pressure the ventricles generate to pump blood to the lungs (Incorrect)\n" +
                        "This describes afterload, not preload.\n\n" +
                        "The resistance the ventricles must overcome to pump blood (Incorrect)\n" +
                        "Afterload is the resistance the heart works against.\n\n" +
                        "The volume of blood returning to the heart (Incorrect)\n" +
                        "Blood returning to the heart is related to preload but not the same concept."
        );

        questions.add("What is the function of the pulmonary veins?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Carry oxygenated blood from the lungs to the left atrium", // Correct answer
                "Carry deoxygenated blood to the right atrium",
                "Carry oxygenated blood from the heart to the body",
                "Carry deoxygenated blood to the lungs"
        )));
        correctAnswers.add("Carry oxygenated blood from the lungs to the left atrium");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Carry oxygenated blood from the lungs to the left atrium (Correct answer)\n" +
                        "The pulmonary veins carry oxygenated blood from the lungs to the left atrium.\n\n" +
                        "Carry deoxygenated blood to the right atrium (Incorrect)\n" +
                        "The vena cava carries deoxygenated blood to the right atrium.\n\n" +
                        "Carry oxygenated blood from the heart to the body (Incorrect)\n" +
                        "The aorta carries oxygenated blood to the body.\n\n" +
                        "Carry deoxygenated blood to the lungs (Incorrect)\n" +
                        "Pulmonary arteries carry deoxygenated blood to the lungs."
        );

        questions.add("The term 'afterload' refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The pressure the ventricles must overcome to pump blood", // Correct answer
                "The volume of blood in the ventricles at the end of diastole",
                "The resistance of the arteries",
                "The volume of blood returning to the heart"
        )));
        correctAnswers.add("The pressure the ventricles must overcome to pump blood");
        rationales.put(44,
                "RATIONALE:\n" +
                        "The pressure the ventricles must overcome to pump blood (Correct answer)\n" +
                        "Afterload is the resistance or pressure the heart must work against during ventricular ejection.\n\n" +
                        "The volume of blood in the ventricles at the end of diastole (Incorrect)\n" +
                        "Preload refers to blood volume in the ventricles at the end of diastole.\n\n" +
                        "The resistance of the arteries (Incorrect)\n" +
                        "Arterial resistance is part of afterload but not the full definition.\n\n" +
                        "The volume of blood returning to the heart (Incorrect)\n" +
                        "The volume of blood returning to the heart is preload."
        );

        questions.add("Which of the following vessels carries deoxygenated blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary artery", // Correct answer
                "Pulmonary veins",
                "Aorta",
                "Coronary artery"
        )));
        correctAnswers.add("Pulmonary artery");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Pulmonary artery (Correct answer)\n" +
                        "The pulmonary artery carries deoxygenated blood from the right ventricle to the lungs for oxygenation.\n\n" +
                        "Pulmonary veins (Incorrect)\n" +
                        "The pulmonary veins carry oxygenated blood from the lungs to the left atrium.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "The aorta carries oxygenated blood from the left ventricle to the body.\n\n" +
                        "Coronary artery (Incorrect)\n" +
                        "The coronary arteries supply oxygenated blood to the heart muscle."
        );

        questions.add("Which of the following is the correct sequence of blood flow through the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Right atrium → Right ventricle → Lungs → Left atrium → Left ventricle", // Correct answer
                "Right atrium → Left atrium → Right ventricle → Left ventricle",
                "Left atrium → Left ventricle → Right atrium → Right ventricle",
                "Right atrium → Right ventricle → Left atrium → Left ventricle"
        )));
        correctAnswers.add("Right atrium → Right ventricle → Lungs → Left atrium → Left ventricle");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Right atrium → Right ventricle → Lungs → Left atrium → Left ventricle (Correct answer)\n" +
                        "Blood flows through the heart in this sequence: right atrium → right ventricle → lungs (for oxygenation) → left atrium → left ventricle.\n\n" +
                        "Right atrium → Left atrium → Right ventricle → Left ventricle (Incorrect)\n" +
                        "This sequence does not reflect the proper order of blood flow.\n\n" +
                        "Left atrium → Left ventricle → Right atrium → Right ventricle (Incorrect)\n" +
                        "Blood must pass through the right side of the heart before reaching the lungs.\n\n" +
                        "Right atrium → Right ventricle → Left atrium → Left ventricle (Incorrect)\n" +
                        "Blood must pass through the lungs before entering the left atrium."
        );

        questions.add("The primary role of the coronary arteries is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Supply oxygenated blood to the heart muscle", // Correct answer
                "Carry deoxygenated blood to the lungs",
                "Transport blood to the brain",
                "Return deoxygenated blood to the right atrium"
        )));
        correctAnswers.add("Supply oxygenated blood to the heart muscle");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Supply oxygenated blood to the heart muscle (Correct answer)\n" +
                        "The coronary arteries supply oxygenated blood to the heart muscle itself, ensuring the heart has the nutrients and oxygen it needs.\n\n" +
                        "Carry deoxygenated blood to the lungs (Incorrect)\n" +
                        "The pulmonary arteries transport deoxygenated blood to the lungs, not the coronary arteries.\n\n" +
                        "Transport blood to the brain (Incorrect)\n" +
                        "The carotid arteries carry oxygenated blood to the brain.\n\n" +
                        "Return deoxygenated blood to the right atrium (Incorrect)\n" +
                        "The vena cava returns deoxygenated blood to the right atrium, not the coronary arteries."
        );

        questions.add("What is the term for the amount of blood pumped by the heart in one minute?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cardiac output", // Correct answer
                "Stroke volume",
                "Ejection fraction",
                "Heart rate"
        )));
        correctAnswers.add("Cardiac output");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Cardiac output (Correct answer)\n" +
                        "Cardiac output is the amount of blood pumped by the heart per minute, calculated as stroke volume × heart rate.\n\n" +
                        "Stroke volume (Incorrect)\n" +
                        "Stroke volume refers to the amount of blood pumped with each heartbeat, not per minute.\n\n" +
                        "Ejection fraction (Incorrect)\n" +
                        "Ejection fraction is the percentage of blood ejected from the left ventricle per beat.\n\n" +
                        "Heart rate (Incorrect)\n" +
                        "Heart rate refers to the number of heartbeats per minute, not the amount of blood pumped."
        );

        questions.add("Which of the following would most likely cause an increase in blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased vascular resistance", // Correct answer
                "Decreased heart rate",
                "Decreased blood volume",
                "Vasodilation"
        )));
        correctAnswers.add("Increased vascular resistance");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Increased vascular resistance (Correct answer)\n" +
                        "Increased vascular resistance (due to factors like narrowed arteries or higher viscosity of blood) will raise blood pressure.\n\n" +
                        "Decreased heart rate (Incorrect)\n" +
                        "Decreased heart rate would generally lead to a decrease in blood pressure.\n\n" +
                        "Decreased blood volume (Incorrect)\n" +
                        "Decreased blood volume (such as from dehydration) would typically lower blood pressure.\n\n" +
                        "Vasodilation (Incorrect)\n" +
                        "Vasodilation (the widening of blood vessels) typically lowers blood pressure by reducing vascular resistance."
        );

        questions.add("What is the correct pathway for blood flow through the systemic circulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left atrium → Left ventricle → Aorta → Body → Vena cava → Right atrium", // Correct answer
                "Right atrium → Right ventricle → Pulmonary artery → Lungs → Left atrium",
                "Left ventricle → Pulmonary artery → Lungs → Pulmonary veins → Left atrium",
                "Right ventricle → Pulmonary veins → Left atrium → Aorta"
        )));
        correctAnswers.add("Left atrium → Left ventricle → Aorta → Body → Vena cava → Right atrium");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Left atrium → Left ventricle → Aorta → Body → Vena cava → Right atrium (Correct answer)\n" +
                        "In systemic circulation, oxygenated blood flows from the left atrium to the left ventricle, then to the aorta, through the body, and returns via the vena cava to the right atrium.\n\n" +
                        "Right atrium → Right ventricle → Pulmonary artery → Lungs → Left atrium (Incorrect)\n" +
                        "This describes the pulmonary circulation, not systemic.\n\n" +
                        "Left ventricle → Pulmonary artery → Lungs → Pulmonary veins → Left atrium (Incorrect)\n" +
                        "This sequence is for pulmonary circulation.\n\n" +
                        "Right ventricle → Pulmonary veins → Left atrium → Aorta (Incorrect)\n" +
                        "Pulmonary veins do not carry blood to the left atrium directly from the right ventricle."
        );

        questions.add("What is the function of the aortic valve?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prevents backflow from the left ventricle to the aorta", // Correct answer
                "Prevents backflow from the left atrium to the left ventricle",
                "Prevents backflow from the right atrium to the right ventricle",
                "Prevents backflow from the right ventricle to the pulmonary artery"
        )));
        correctAnswers.add("Prevents backflow from the left ventricle to the aorta");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Prevents backflow from the left ventricle to the aorta (Correct answer)\n" +
                        "The aortic valve ensures that blood does not flow backward from the aorta into the left ventricle after ventricular contraction.\n\n" +
                        "Prevents backflow from the left atrium to the left ventricle (Incorrect)\n" +
                        "The mitral valve prevents backflow between the left atrium and ventricle.\n\n" +
                        "Prevents backflow from the right atrium to the right ventricle (Incorrect)\n" +
                        "The tricuspid valve prevents backflow in the right side of the heart.\n\n" +
                        "Prevents backflow from the right ventricle to the pulmonary artery (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle."
        );

        questions.add("What is the role of the AV node in the heart’s conduction system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Delays the electrical impulse for atrial contraction", // Correct answer
                "Initiates the electrical impulse",
                "Stimulates ventricular contraction",
                "Recycles the electrical impulse"
        )));
        correctAnswers.add("Delays the electrical impulse for atrial contraction");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Delays the electrical impulse for atrial contraction (Correct answer)\n" +
                        "The AV node delays the electrical impulse slightly to allow for the atrial contraction to complete before the impulse travels to the ventricles.\n\n" +
                        "Initiates the electrical impulse (Incorrect)\n" +
                        "The SA node initiates the electrical impulse, not the AV node.\n\n" +
                        "Stimulates ventricular contraction (Incorrect)\n" +
                        "The AV node does not stimulate ventricular contraction, but allows the impulse to pass to the ventricles.\n\n" +
                        "Recycles the electrical impulse (Incorrect)\n" +
                        "The AV node does not recycle the electrical impulse."
        );

        questions.add("The function of the left ventricle is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pump oxygenated blood to the body", // Correct answer
                "Pump deoxygenated blood to the lungs",
                "Receive oxygenated blood from the lungs",
                "Receive deoxygenated blood from the body"
        )));
        correctAnswers.add("Pump oxygenated blood to the body");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Pump oxygenated blood to the body (Correct answer)\n" +
                        "The left ventricle pumps oxygenated blood into the aorta, which delivers it to the rest of the body.\n\n" +
                        "Pump deoxygenated blood to the lungs (Incorrect)\n" +
                        "The right ventricle pumps deoxygenated blood to the lungs.\n\n" +
                        "Receive oxygenated blood from the lungs (Incorrect)\n" +
                        "The left atrium receives oxygenated blood from the lungs.\n\n" +
                        "Receive deoxygenated blood from the body (Incorrect)\n" +
                        "The right atrium receives deoxygenated blood from the body."
        );

        questions.add("Which part of the heart initiates the electrical impulses for contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sinoatrial (SA) Node", // Correct answer
                "Atrioventricular (AV) Node",
                "Bundle of His",
                "Purkinje Fibers"
        )));
        correctAnswers.add("Sinoatrial (SA) Node");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Sinoatrial (SA) Node (Correct answer)\n" +
                        "The SA node is the pacemaker of the heart and initiates electrical impulses that set the rhythm for the heart's contraction.\n\n" +
                        "Atrioventricular (AV) Node (Incorrect)\n" +
                        "The AV node delays the impulse to allow the atria to contract fully before the ventricles.\n\n" +
                        "Bundle of His (Incorrect)\n" +
                        "The Bundle of His transmits impulses from the AV node to the ventricles.\n\n" +
                        "Purkinje Fibers (Incorrect)\n" +
                        "Purkinje fibers help spread the electrical impulse throughout the ventricles."
        );

        questions.add("What type of blood vessel carries oxygenated blood from the heart to the rest of the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Arteries", // Correct answer
                "Veins",
                "Capillaries",
                "Lymphatic vessels"
        )));
        correctAnswers.add("Arteries");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Arteries (Correct answer)\n" +
                        "Arteries carry oxygenated blood from the heart to the body's tissues (except the pulmonary artery).\n\n" +
                        "Veins (Incorrect)\n" +
                        "Veins carry deoxygenated blood back to the heart.\n\n" +
                        "Capillaries (Incorrect)\n" +
                        "Capillaries are the sites of nutrient and gas exchange but don't carry oxygenated blood.\n\n" +
                        "Lymphatic vessels (Incorrect)\n" +
                        "Lymphatic vessels transport lymph, not blood."
        );

        questions.add("Which of the following is primarily responsible for regulating the heart rate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Autonomic nervous system", // Correct answer
                "Cerebellum",
                "Somatic nervous system",
                "Medulla oblongata"
        )));
        correctAnswers.add("Autonomic nervous system");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Autonomic nervous system (Correct answer)\n" +
                        "The autonomic nervous system (ANS) regulates the heart rate through its sympathetic (increases HR) and parasympathetic (decreases HR) branches.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum coordinates motor functions, not heart rate.\n\n" +
                        "Somatic nervous system (Incorrect)\n" +
                        "The somatic nervous system controls voluntary movements, not heart rate.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata is involved in autonomic regulation but does not directly regulate heart rate."
        );

        questions.add("Which of the following heart valves prevents backflow of blood into the right atrium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve", // Correct answer
                "Aortic valve",
                "Pulmonary valve",
                "Bicuspid valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve is located between the right atrium and right ventricle, preventing backflow of blood into the atrium during ventricular contraction.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve prevents backflow into the left ventricle from the aorta.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle from the pulmonary artery.\n\n" +
                        "Bicuspid valve (Incorrect)\n" +
                        "The bicuspid valve (mitral valve) prevents backflow into the left atrium."
        );

        questions.add("The process by which blood clots form to stop bleeding is called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hemostasis", // Correct answer
                "Hemolysis",
                "Hematopoiesis",
                "Hemorrhage"
        )));
        correctAnswers.add("Hemostasis");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Hemostasis (Correct answer)\n" +
                        "Hemostasis is the process of stopping bleeding, which involves clot formation.\n\n" +
                        "Hemolysis (Incorrect)\n" +
                        "Hemolysis refers to the destruction of red blood cells.\n\n" +
                        "Hematopoiesis (Incorrect)\n" +
                        "Hematopoiesis is the process of blood cell formation.\n\n" +
                        "Hemorrhage (Incorrect)\n" +
                        "Hemorrhage is excessive bleeding, not a process that stops it."
        );

        questions.add("Which of the following factors increases the afterload on the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased arterial stiffness", // Correct answer
                "Increased blood volume",
                "Decreased blood viscosity",
                "Decreased heart rate"
        )));
        correctAnswers.add("Increased arterial stiffness");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Increased arterial stiffness (Correct answer)\n" +
                        "Arterial stiffness increases the resistance the heart must overcome to pump blood, thereby increasing afterload.\n\n" +
                        "Increased blood volume (Incorrect)\n" +
                        "Increased blood volume increases preload, not afterload.\n\n" +
                        "Decreased blood viscosity (Incorrect)\n" +
                        "Decreased blood viscosity reduces resistance, decreasing afterload.\n\n" +
                        "Decreased heart rate (Incorrect)\n" +
                        "Decreased heart rate would reduce workload but not directly affect afterload."
        );

        questions.add("What is the term for the volume of blood pumped by the left ventricle in one contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stroke volume",
                "Cardiac output",
                "Ejection fraction",
                "Preload"
        )));
        correctAnswers.add("Stroke volume");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Stroke volume (Correct answer)\n" +
                        "Stroke volume refers to the amount of blood pumped by the left ventricle with each beat.\n\n" +
                        "Cardiac output (Incorrect)\n" +
                        "Cardiac output is the volume of blood pumped per minute, which is stroke volume × heart rate.\n\n" +
                        "Ejection fraction (Incorrect)\n" +
                        "Ejection fraction is the percentage of blood ejected from the left ventricle per beat.\n\n" +
                        "Preload (Incorrect)\n" +
                        "Preload refers to the volume of blood in the ventricles before contraction."
        );

        questions.add("Which of the following structures connects the right atrium to the right ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve",
                "Pulmonary valve",
                "Aortic valve",
                "Mitral valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve is located between the right atrium and right ventricle, preventing backflow into the atrium.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve connects the right ventricle to the pulmonary artery.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve connects the left ventricle to the aorta.\n\n" +
                        "Mitral valve (Incorrect)\n" +
                        "The mitral valve connects the left atrium to the left ventricle."
        );

        questions.add("Which of the following is NOT part of the systemic circulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary artery",
                "Aorta",
                "Superior vena cava",
                "Coronary arteries"
        )));
        correctAnswers.add("Pulmonary artery");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Pulmonary artery (Correct answer)\n" +
                        "The pulmonary artery is part of the pulmonary circulation, which carries deoxygenated blood to the lungs.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "The aorta carries oxygenated blood from the left ventricle to the body.\n\n" +
                        "Superior vena cava (Incorrect)\n" +
                        "The superior vena cava returns deoxygenated blood from the upper body to the right atrium.\n\n" +
                        "Coronary arteries (Incorrect)\n" +
                        "The coronary arteries supply oxygenated blood to the heart muscle."
        );

        questions.add("Which of the following is an effect of the sympathetic nervous system on the cardiovascular system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increases heart rate",
                "Decreases heart rate",
                "Decreases contractility",
                "Dilates blood vessels"
        )));
        correctAnswers.add("Increases heart rate");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Increases heart rate (Correct answer)\n" +
                        "The sympathetic nervous system increases heart rate and contractility to prepare the body for \"fight or flight.\"\n\n" +
                        "Decreases heart rate (Incorrect)\n" +
                        "Parasympathetic stimulation decreases heart rate.\n\n" +
                        "Decreases contractility (Incorrect)\n" +
                        "Parasympathetic stimulation decreases contractility.\n\n" +
                        "Dilates blood vessels (Incorrect)\n" +
                        "Sympathetic stimulation constricts blood vessels, increasing vascular resistance."
        );

        questions.add("Which of the following valves prevents backflow of blood from the aorta into the left ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Aortic valve",
                "Pulmonary valve",
                "Tricuspid valve",
                "Mitral valve"
        )));
        correctAnswers.add("Aortic valve");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Aortic valve (Correct answer)\n" +
                        "The aortic valve prevents backflow of blood from the aorta into the left ventricle after contraction.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle from the pulmonary artery.\n\n" +
                        "Tricuspid valve (Incorrect)\n" +
                        "The tricuspid valve prevents backflow into the right atrium from the right ventricle.\n\n" +
                        "Mitral valve (Incorrect)\n" +
                        "The mitral valve prevents backflow into the left atrium from the left ventricle."
        );

        questions.add("What does an electrocardiogram (ECG) measure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Heart electrical activity", // Correct answer
                "Blood pressure",
                "Blood flow",
                "Heart rate only"
        )));
        correctAnswers.add("Heart electrical activity");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Heart electrical activity (Correct answer)\n" +
                        "An ECG records the electrical impulses that trigger the heart's contraction, including the P, QRS, and T waves.\n\n" +
                        "Blood pressure (Incorrect)\n" +
                        "Blood pressure is measured using a sphygmomanometer, not an ECG.\n\n" +
                        "Blood flow (Incorrect)\n" +
                        "Blood flow is measured with a Doppler ultrasound, not an ECG.\n\n" +
                        "Heart rate only (Incorrect)\n" +
                        "Heart rate can be determined from an ECG, but it measures electrical activity as a whole."
        );

        questions.add("Which blood vessel has the thickest walls to withstand high pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Arteries", // Correct answer
                "Veins",
                "Capillaries",
                "Lymphatic vessels"
        )));
        correctAnswers.add("Arteries");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Arteries (Correct answer)\n" +
                        "Arteries have the thickest walls to withstand the high pressure exerted by blood being pumped from the heart.\n\n" +
                        "Veins (Incorrect)\n" +
                        "Veins have thinner walls because they carry blood under lower pressure.\n\n" +
                        "Capillaries (Incorrect)\n" +
                        "Capillaries are thin-walled to allow for nutrient and gas exchange.\n\n" +
                        "Lymphatic vessels (Incorrect)\n" +
                        "Lymphatic vessels are thin-walled, but they carry lymph, not blood."
        );

        questions.add("What term describes the volume of blood in the ventricles at the end of diastole?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Preload", // Correct answer
                "Afterload",
                "Stroke volume",
                "Cardiac output"
        )));
        correctAnswers.add("Preload");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Preload (Correct answer)\n" +
                        "Preload refers to the volume of blood in the ventricles at the end of diastole, before the heart contracts.\n\n" +
                        "Afterload (Incorrect)\n" +
                        "Afterload refers to the resistance the heart must overcome to eject blood.\n\n" +
                        "Stroke volume (Incorrect)\n" +
                        "Stroke volume is the amount of blood pumped by the heart in one beat.\n\n" +
                        "Cardiac output (Incorrect)\n" +
                        "Cardiac output is the total blood volume pumped per minute, calculated as stroke volume × heart rate."
        );

        questions.add("Which layer of the heart is responsible for its pumping action?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myocardium", // Correct answer
                "Endocardium",
                "Epicardium",
                "Pericardium"
        )));
        correctAnswers.add("Myocardium");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Myocardium (Correct answer)\n" +
                        "The myocardium is the thick, muscular middle layer of the heart wall that is responsible for the contraction and pumping of blood.\n\n" +
                        "Endocardium (Incorrect)\n" +
                        "The endocardium is the inner lining of the heart chambers and valves.\n\n" +
                        "Epicardium (Incorrect)\n" +
                        "The epicardium is the outermost layer of the heart wall.\n\n" +
                        "Pericardium (Incorrect)\n" +
                        "The pericardium is the double-walled sac that surrounds and protects the heart, not involved in pumping."
        );

        questions.add("Which of the following conditions is characterized by the narrowing or blockage of coronary arteries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Coronary artery disease", // Correct answer
                "Myocarditis",
                "Cardiomyopathy",
                "Pericarditis"
        )));
        correctAnswers.add("Coronary artery disease");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Coronary artery disease (Correct answer)\n" +
                        "Coronary artery disease (CAD) occurs when the coronary arteries that supply blood to the heart muscle become narrowed or blocked, often due to atherosclerosis.\n\n" +
                        "Myocarditis (Incorrect)\n" +
                        "Myocarditis is inflammation of the heart muscle.\n\n" +
                        "Cardiomyopathy (Incorrect)\n" +
                        "Cardiomyopathy refers to diseases of the heart muscle that affect its function.\n\n" +
                        "Pericarditis (Incorrect)\n" +
                        "Pericarditis is inflammation of the pericardium, the outer lining of the heart."
        );

        questions.add("Which of the following structures is the pacemaker of the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "SA Node", // Correct answer
                "AV Node",
                "Bundle of His",
                "Purkinje Fibers"
        )));
        correctAnswers.add("SA Node");
        rationales.put(70,
                "RATIONALE:\n" +
                        "SA Node (Correct answer)\n" +
                        "The SA node (Sinoatrial node) is the natural pacemaker of the heart, initiating the electrical impulse that regulates the heartbeat.\n\n" +
                        "AV Node (Incorrect)\n" +
                        "The AV node conducts the impulse from the atria to the ventricles but does not initiate it.\n\n" +
                        "Bundle of His (Incorrect)\n" +
                        "The Bundle of His conducts the impulse to the left and right ventricles.\n\n" +
                        "Purkinje Fibers (Incorrect)\n" +
                        "The Purkinje fibers carry the impulse to the heart's outer muscle layers."
        );

        questions.add("What is the primary role of the coronary arteries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To provide oxygenated blood to the heart muscle", // Correct answer
                "To deliver oxygenated blood to the lungs",
                "To return deoxygenated blood to the right atrium",
                "To pump oxygenated blood to the body"
        )));
        correctAnswers.add("To provide oxygenated blood to the heart muscle");
        rationales.put(71,
                "RATIONALE:\n" +
                        "To provide oxygenated blood to the heart muscle (Correct answer)\n" +
                        "The coronary arteries supply oxygenated blood to the heart muscle, ensuring it functions efficiently.\n\n" +
                        "To deliver oxygenated blood to the lungs (Incorrect)\n" +
                        "The pulmonary arteries carry deoxygenated blood to the lungs.\n\n" +
                        "To return deoxygenated blood to the right atrium (Incorrect)\n" +
                        "The vena cava returns deoxygenated blood to the right atrium.\n\n" +
                        "To pump oxygenated blood to the body (Incorrect)\n" +
                        "The aorta pumps oxygenated blood to the body."
        );

        questions.add("What does the term “systole” refer to in the cardiac cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Contraction of the ventricles", // Correct answer
                "Relaxation of the heart",
                "Contraction of the atria",
                "Filling of the heart chambers"
        )));
        correctAnswers.add("Contraction of the ventricles");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Contraction of the ventricles (Correct answer)\n" +
                        "Systole refers to the phase of the cardiac cycle when the ventricles contract, pumping blood out to the lungs and body.\n\n" +
                        "Relaxation of the heart (Incorrect)\n" +
                        "Diastole is the phase of the heart's relaxation.\n\n" +
                        "Contraction of the atria (Incorrect)\n" +
                        "Atrial contraction occurs during atrial systole, but systole refers to ventricular contraction.\n\n" +
                        "Filling of the heart chambers (Incorrect)\n" +
                        "Chamber filling occurs during diastole."
        );

        questions.add("What is the role of the aortic valve?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prevents blood from flowing back into the left ventricle", // Correct answer
                "Prevents blood from flowing back into the right ventricle",
                "Prevents blood from flowing back into the left atrium",
                "Prevents blood from flowing back into the aorta"
        )));
        correctAnswers.add("Prevents blood from flowing back into the left ventricle");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Prevents blood from flowing back into the left ventricle (Correct answer)\n" +
                        "The aortic valve prevents the backflow of blood from the aorta into the left ventricle after ventricular contraction.\n\n" +
                        "Prevents blood from flowing back into the right ventricle (Incorrect)\n" +
                        "The pulmonary valve prevents backflow into the right ventricle.\n\n" +
                        "Prevents blood from flowing back into the left atrium (Incorrect)\n" +
                        "The mitral valve prevents backflow into the left atrium.\n\n" +
                        "Prevents blood from flowing back into the aorta (Incorrect)\n" +
                        "The aortic valve prevents backflow into the left ventricle, not the aorta."
        );

        questions.add("Which of the following is NOT a function of the cardiovascular system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stores fat", // Correct answer
                "Transports oxygen and nutrients to tissues",
                "Helps regulate body temperature",
                "Assists in immune function"
        )));
        correctAnswers.add("Stores fat");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Stores fat (Correct answer)\n" +
                        "The cardiovascular system transports nutrients, oxygen, and wastes but does not store fat; that is a function of adipose tissue.\n\n" +
                        "Transports oxygen and nutrients to tissues (Incorrect)\n" +
                        "The cardiovascular system delivers oxygen and nutrients via blood.\n\n" +
                        "Helps regulate body temperature (Incorrect)\n" +
                        "Blood flow helps regulate temperature.\n\n" +
                        "Assists in immune function (Incorrect)\n" +
                        "The immune system uses blood vessels to transport white blood cells."
        );

        questions.add("What is the function of the semilunar valves?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To regulate blood flow from the ventricles to arteries", // Correct answer
                "To prevent backflow into the ventricles",
                "To prevent backflow into the atria",
                "To aid in the contraction of the heart"
        )));
        correctAnswers.add("To regulate blood flow from the ventricles to arteries");
        rationales.put(75,
                "RATIONALE:\n" +
                        "To regulate blood flow from the ventricles to arteries (Correct answer)\n" +
                        "The semilunar valves (pulmonary and aortic valves) regulate blood flow from the ventricles to the pulmonary artery and aorta, respectively.\n\n" +
                        "To prevent backflow into the ventricles (Incorrect)\n" +
                        "The AV valves prevent backflow into the ventricles.\n\n" +
                        "To prevent backflow into the atria (Incorrect)\n" +
                        "The AV valves also prevent backflow into the atria.\n\n" +
                        "To aid in the contraction of the heart (Incorrect)\n" +
                        "Contraction is driven by the myocardium, not the valves."
        );

        questions.add("What is the main function of capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Exchange gases, nutrients, and waste products between blood and tissues", // Correct answer
                "Carry blood away from the heart",
                "Transport oxygenated blood to the lungs",
                "Carry blood toward the heart"
        )));
        correctAnswers.add("Exchange gases, nutrients, and waste products between blood and tissues");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Exchange gases, nutrients, and waste products between blood and tissues (Correct answer)\n" +
                        "Capillaries are small vessels where gas exchange (O2 and CO2) and nutrient and waste exchange occur.\n\n" +
                        "Carry blood away from the heart (Incorrect)\n" +
                        "Arteries carry blood away from the heart.\n\n" +
                        "Transport oxygenated blood to the lungs (Incorrect)\n" +
                        "The pulmonary artery carries deoxygenated blood to the lungs, not for exchange.\n\n" +
                        "Carry blood toward the heart (Incorrect)\n" +
                        "Veins carry blood toward the heart."
        );

        questions.add("What causes the 'lub-dub' heart sounds?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Closure of the heart valves", // Correct answer
                "Opening of the heart valves",
                "The contraction of the atria and ventricles",
                "The flow of blood through the heart chambers"
        )));
        correctAnswers.add("Closure of the heart valves");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Closure of the heart valves (Correct answer)\n" +
                        "The 'lub' sound (S1) is caused by the closure of the AV valves, and the 'dub' sound (S2) is caused by the closure of the semilunar valves.\n\n" +
                        "Opening of the heart valves (Incorrect)\n" +
                        "Valve opening does not create sound.\n\n" +
                        "The contraction of the atria and ventricles (Incorrect)\n" +
                        "Contraction is related to heart action but does not cause the heart sounds.\n\n" +
                        "The flow of blood through the heart chambers (Incorrect)\n" +
                        "Blood flow through chambers does not produce the heart sounds."
        );

        questions.add("The left atrium receives blood from which of the following vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary veins", // Correct answer
                "Pulmonary artery",
                "Inferior vena cava",
                "Superior vena cava"
        )));
        correctAnswers.add("Pulmonary veins");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Pulmonary veins (Correct answer)\n" +
                        "The left atrium receives oxygenated blood from the lungs through the pulmonary veins.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "The pulmonary artery carries deoxygenated blood from the right ventricle to the lungs.\n\n" +
                        "Inferior vena cava (Incorrect)\n" +
                        "The inferior vena cava brings deoxygenated blood from the lower body to the right atrium.\n\n" +
                        "Superior vena cava (Incorrect)\n" +
                        "The superior vena cava brings deoxygenated blood from the upper body to the right atrium."
        );

        questions.add("Which of the following factors does NOT affect cardiac output?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood type", // Correct answer
                "Heart rate",
                "Stroke volume",
                "Blood vessel resistance"
        )));
        correctAnswers.add("Blood type");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Blood type (Correct answer)\n" +
                        "Blood type does not affect cardiac output. However, heart rate, stroke volume, and blood vessel resistance do.\n\n" +
                        "Heart rate (Incorrect)\n" +
                        "Heart rate directly affects cardiac output.\n\n" +
                        "Stroke volume (Incorrect)\n" +
                        "Stroke volume is the amount of blood pumped per beat, affecting cardiac output.\n\n" +
                        "Blood vessel resistance (Incorrect)\n" +
                        "Blood vessel resistance impacts the efficiency of blood flow, affecting cardiac output."
        );

        questions.add("The function of the AV node in the heart is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Slow down the electrical impulse between atria and ventricles", // Correct answer
                "Initiate the electrical impulse in the heart",
                "Stimulate the contraction of the ventricles",
                "Conduct the electrical impulse to the Purkinje fibers"
        )));
        correctAnswers.add("Slow down the electrical impulse between atria and ventricles");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Slow down the electrical impulse between atria and ventricles (Correct answer)\n" +
                        "The AV node delays the electrical impulse to ensure that the atria have time to fully contract before ventricular contraction begins.\n\n" +
                        "Initiate the electrical impulse in the heart (Incorrect)\n" +
                        "The SA node initiates the electrical impulse, not the AV node.\n\n" +
                        "Stimulate the contraction of the ventricles (Incorrect)\n" +
                        "The AV node does not directly stimulate the ventricles to contract.\n\n" +
                        "Conduct the electrical impulse to the Purkinje fibers (Incorrect)\n" +
                        "The impulse travels to the Bundle of His and then to the Purkinje fibers, not directly from the AV node."
        );

        questions.add("What is the primary purpose of the myocardial cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To contract and pump blood", // Correct answer
                "To store calcium for muscle contraction",
                "To allow the heart to relax after each beat",
                "To generate electrical impulses"
        )));
        correctAnswers.add("To contract and pump blood");
        rationales.put(81,
                "RATIONALE:\n" +
                        "To contract and pump blood (Correct answer)\n" +
                        "The myocardial cells are responsible for contracting and generating the force needed to pump blood throughout the circulatory system.\n\n" +
                        "To store calcium for muscle contraction (Incorrect)\n" +
                        "Myocytes store calcium, but their primary function is contraction.\n\n" +
                        "To allow the heart to relax after each beat (Incorrect)\n" +
                        "Relaxation is the role of the myocardial cells after contraction.\n\n" +
                        "To generate electrical impulses (Incorrect)\n" +
                        "The SA node and other components of the conduction system generate impulses."
        );

        questions.add("Which of the following blood vessels carries oxygenated blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Aorta", // Correct answer
                "Pulmonary artery",
                "Vena cava",
                "Pulmonary veins"
        )));
        correctAnswers.add("Aorta");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Aorta (Correct answer)\n" +
                        "The aorta carries oxygenated blood from the left ventricle to the body.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "The pulmonary artery carries deoxygenated blood to the lungs.\n\n" +
                        "Vena cava (Incorrect)\n" +
                        "The vena cava brings deoxygenated blood to the right atrium.\n\n" +
                        "Pulmonary veins (Incorrect)\n" +
                        "The pulmonary veins carry oxygenated blood from the lungs to the left atrium."
        );

        questions.add("Which structure is responsible for regulating blood flow from the ventricles to the arteries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Semilunar valves", // Correct answer
                "AV valves",
                "Tricuspid valve",
                "Pulmonary veins"
        )));
        correctAnswers.add("Semilunar valves");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Semilunar valves (Correct answer)\n" +
                        "The semilunar valves (pulmonary and aortic) regulate the blood flow from the ventricles into the arteries.\n\n" +
                        "AV valves (Incorrect)\n" +
                        "The AV valves regulate blood flow between the atria and ventricles.\n\n" +
                        "Tricuspid valve (Incorrect)\n" +
                        "The tricuspid valve regulates blood flow between the right atrium and right ventricle.\n\n" +
                        "Pulmonary veins (Incorrect)\n" +
                        "The pulmonary veins carry blood from the lungs to the left atrium."
        );

        questions.add("What is the effect of parasympathetic stimulation on the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decreases heart rate", // Correct answer
                "Increases heart rate",
                "Increases contraction force",
                "Raises blood pressure"
        )));
        correctAnswers.add("Decreases heart rate");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Decreases heart rate (Correct answer)\n" +
                        "Parasympathetic stimulation (via the vagus nerve) slows the heart rate by decreasing activity of the SA node.\n\n" +
                        "Increases heart rate (Incorrect)\n" +
                        "Sympathetic, not parasympathetic, stimulation increases heart rate.\n\n" +
                        "Increases contraction force (Incorrect)\n" +
                        "Parasympathetic stimulation does not increase contraction force.\n\n" +
                        "Raises blood pressure (Incorrect)\n" +
                        "Parasympathetic stimulation typically lowers, not raises, blood pressure."
        );

        questions.add("What initiates the electrical impulse in the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "SA Node", // Correct answer
                "AV Node",
                "Purkinje Fibers",
                "Bundle of His"
        )));
        correctAnswers.add("SA Node");
        rationales.put(85,
                "RATIONALE:\n" +
                        "SA Node (Correct answer)\n" +
                        "The Sinoatrial (SA) Node is the natural pacemaker of the heart that initiates the electrical impulses, setting the rhythm for heart contractions.\n\n" +
                        "AV Node (Incorrect)\n" +
                        "The AV Node delays impulse, allowing atria to contract, but does not initiate it.\n\n" +
                        "Purkinje Fibers (Incorrect)\n" +
                        "Purkinje fibers distribute impulses to ventricles but don’t initiate them.\n\n" +
                        "Bundle of His (Incorrect)\n" +
                        "The Bundle of His conducts impulses, not initiates them."
        );

        questions.add("What valve is located between the right atrium and right ventricle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tricuspid valve", // Correct answer
                "Bicuspid valve",
                "Aortic valve",
                "Pulmonary valve"
        )));
        correctAnswers.add("Tricuspid valve");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Tricuspid valve (Correct answer)\n" +
                        "The tricuspid valve lies between the right atrium and right ventricle, preventing backflow of blood during ventricular contraction.\n\n" +
                        "Bicuspid (Mitral) valve (Incorrect)\n" +
                        "The Bicuspid valve is located on the left side between the left atrium and left ventricle.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve is between the left ventricle and the aorta.\n\n" +
                        "Pulmonary valve (Incorrect)\n" +
                        "The pulmonary valve is located between the right ventricle and pulmonary artery."
        );

        questions.add("What is the normal range for adult systolic blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "90–119 mmHg", // Correct answer
                "80–100 mmHg",
                "120–129 mmHg",
                "120–139 mmHg"
        )));
        correctAnswers.add("90–119 mmHg");
        rationales.put(87,
                "RATIONALE:\n" +
                        "90–119 mmHg (Correct answer)\n" +
                        "Normal systolic BP is generally below 120 mmHg, with 90–119 mmHg considered normal.\n\n" +
                        "80–100 mmHg (Incorrect)\n" +
                        "This range is below the normal systolic BP range.\n\n" +
                        "120–129 mmHg (Incorrect)\n" +
                        "This range is considered elevated, not normal.\n\n" +
                        "120–139 mmHg (Incorrect)\n" +
                        "This range includes prehypertension and stage 1 hypertension levels."
        );

        questions.add("Which vessel returns oxygenated blood to the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary vein", // Correct answer
                "Pulmonary artery",
                "Superior vena cava",
                "Coronary artery"
        )));
        correctAnswers.add("Pulmonary vein");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Pulmonary vein (Correct answer)\n" +
                        "Pulmonary veins return oxygenated blood from the lungs to the left atrium.\n\n" +
                        "Pulmonary artery (Incorrect)\n" +
                        "The pulmonary artery carries deoxygenated blood to the lungs.\n\n" +
                        "Superior vena cava (Incorrect)\n" +
                        "The superior vena cava carries deoxygenated blood from the upper body to the heart.\n\n" +
                        "Coronary artery (Incorrect)\n" +
                        "The coronary artery supplies oxygenated blood to the heart muscle but doesn’t return blood."
        );

        questions.add("What is the purpose of capillaries?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Exchange gases and nutrients", // Correct answer
                "Carry blood to the lungs",
                "Store blood",
                "Pump blood"
        )));
        correctAnswers.add("Exchange gases and nutrients");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Exchange gases and nutrients (Correct answer)\n" +
                        "Capillaries are the smallest vessels, responsible for gas, nutrient, and waste exchange between blood and tissues.\n\n" +
                        "Carry blood to the lungs (Incorrect)\n" +
                        "That’s the job of arteries and veins, not capillaries.\n\n" +
                        "Store blood (Incorrect)\n" +
                        "Veins can act as reservoirs but capillaries do not store blood.\n\n" +
                        "Pump blood (Incorrect)\n" +
                        "The heart pumps blood, not capillaries."
        );

        questions.add("What happens during diastole?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The heart relaxes and fills with blood", // Correct answer
                "Ventricles contract",
                "Atria contract",
                "Semilunar valves open"
        )));
        correctAnswers.add("The heart relaxes and fills with blood");
        rationales.put(90,
                "RATIONALE:\n" +
                        "The heart relaxes and fills with blood (Correct answer)\n" +
                        "During diastole, the heart relaxes, allowing the chambers to fill with blood.\n\n" +
                        "Ventricles contract (Incorrect)\n" +
                        "Ventricles contract during systole, not diastole.\n\n" +
                        "Atria contract (Incorrect)\n" +
                        "Atria contract during atrial systole, not diastole.\n\n" +
                        "Semilunar valves open (Incorrect)\n" +
                        "Semilunar valves open during ventricular systole, not diastole."
        );

        questions.add("Which blood vessels have the thickest tunica media?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Arteries", // Correct answer
                "Veins",
                "Capillaries",
                "Venules"
        )));
        correctAnswers.add("Arteries");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Arteries (Correct answer)\n" +
                        "Arteries have the thickest tunica media to handle high pressure and regulate blood flow.\n\n" +
                        "Veins (Incorrect)\n" +
                        "Veins have thinner walls and larger lumens than arteries.\n\n" +
                        "Capillaries (Incorrect)\n" +
                        "Capillaries are very thin, allowing for efficient gas exchange.\n\n" +
                        "Venules (Incorrect)\n" +
                        "Venules are smaller and don’t have a thick muscle layer like arteries."
        );

        questions.add("What structure connects the atria to the ventricles in electrical conduction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "AV Node", // Correct answer
                "SA Node",
                "Purkinje fibers",
                "Aortic valve"
        )));
        correctAnswers.add("AV Node");
        rationales.put(92,
                "RATIONALE:\n" +
                        "AV Node (Correct answer)\n" +
                        "The AV Node delays the impulse from the SA Node briefly before transmitting it to the ventricles.\n\n" +
                        "SA Node (Incorrect)\n" +
                        "The SA Node initiates the impulse but doesn’t connect the atria to the ventricles.\n\n" +
                        "Purkinje fibers (Incorrect)\n" +
                        "Purkinje fibers are involved in conduction within the ventricles, not in the AV junction.\n\n" +
                        "Aortic valve (Incorrect)\n" +
                        "The aortic valve is not part of the electrical conduction system."
        );

        questions.add("What causes the first heart sound (S1)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Closure of AV valves", // Correct answer
                "Opening of AV valves",
                "Closure of semilunar valves",
                "Opening of pulmonary valve"
        )));
        correctAnswers.add("Closure of AV valves");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Closure of AV valves (Correct answer)\n" +
                        "S1 (lub) is caused by the closure of the AV valves (tricuspid and mitral) at the beginning of ventricular systole.\n\n" +
                        "Opening of AV valves (Incorrect)\n" +
                        "The opening of valves is silent and does not contribute to heart sounds.\n\n" +
                        "Closure of semilunar valves (Incorrect)\n" +
                        "S2 (dub) is caused by the closure of semilunar valves.\n\n" +
                        "Opening of pulmonary valve (Incorrect)\n" +
                        "Opening of the pulmonary valve does not produce heart sounds."
        );

        questions.add("Which of the following factors increases cardiac output?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increased heart rate", // Correct answer
                "Decreased stroke volume",
                "Bradycardia",
                "Low contractility"
        )));
        correctAnswers.add("Increased heart rate");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Increased heart rate (Correct answer)\n" +
                        "Cardiac Output (CO) is the product of heart rate and stroke volume. Increasing either increases CO.\n\n" +
                        "Decreased stroke volume (Incorrect)\n" +
                        "A decrease in stroke volume lowers CO.\n\n" +
                        "Bradycardia (Incorrect)\n" +
                        "Bradycardia (slow heart rate) reduces CO.\n\n" +
                        "Low contractility (Incorrect)\n" +
                        "Low contractility leads to reduced stroke volume and thus lower CO."
        );

        questions.add("Which vessel carries deoxygenated blood to the lungs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pulmonary artery", // Correct answer
                "Pulmonary vein",
                "Aorta",
                "Coronary artery"
        )));
        correctAnswers.add("Pulmonary artery");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Pulmonary artery (Correct answer)\n" +
                        "The pulmonary artery is the only artery that carries deoxygenated blood from the right ventricle to the lungs.\n\n" +
                        "Pulmonary vein (Incorrect)\n" +
                        "Pulmonary vein carries oxygenated blood from the lungs to the heart.\n\n" +
                        "Aorta (Incorrect)\n" +
                        "Aorta carries oxygenated blood systemically from the left ventricle.\n\n" +
                        "Coronary artery (Incorrect)\n" +
                        "Coronary artery supplies oxygenated blood to the heart muscle."
        );

        questions.add("What heart chamber pumps blood into the systemic circulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left ventricle", // Correct answer
                "Right atrium",
                "Right ventricle",
                "Left atrium"
        )));
        correctAnswers.add("Left ventricle");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Left ventricle (Correct answer)\n" +
                        "The left ventricle pumps oxygenated blood into the aorta for systemic circulation.\n\n" +
                        "Right atrium (Incorrect)\n" +
                        "Right atrium receives deoxygenated blood from the body.\n\n" +
                        "Right ventricle (Incorrect)\n" +
                        "Right ventricle sends deoxygenated blood to the lungs for oxygenation.\n\n" +
                        "Left atrium (Incorrect)\n" +
                        "Left atrium receives oxygenated blood from the lungs."
        );

        questions.add("Which of the following monitors blood pressure changes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Baroreceptors", // Correct answer
                "Chemoreceptors",
                "SA Node",
                "AV Node"
        )));
        correctAnswers.add("Baroreceptors");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Baroreceptors (Correct answer)\n" +
                        "Baroreceptors are pressure-sensitive receptors in arteries that detect changes in blood pressure.\n\n" +
                        "Chemoreceptors (Incorrect)\n" +
                        "Chemoreceptors monitor CO2, O2, and pH levels in the blood.\n\n" +
                        "SA Node (Incorrect)\n" +
                        "SA Node is responsible for initiating the electrical signals of the heart.\n\n" +
                        "AV Node (Incorrect)\n" +
                        "AV Node delays conduction between the atria and ventricles."
        );

        questions.add("What is the normal resting heart rate range for adults?");
        choices.add(new ArrayList<>(Arrays.asList(
                "60–100 bpm", // Correct answer
                "40–60 bpm",
                "100–120 bpm",
                "120–140 bpm"
        )));
        correctAnswers.add("60–100 bpm");
        rationales.put(98,
                "RATIONALE:\n" +
                        "60–100 bpm (Correct answer)\n" +
                        "A normal adult heart rate ranges from 60–100 beats per minute at rest.\n\n" +
                        "40–60 bpm (Incorrect)\n" +
                        "40–60 bpm is considered bradycardia in adults.\n\n" +
                        "100–120 bpm (Incorrect)\n" +
                        "100–120 bpm is considered tachycardia.\n\n" +
                        "120–140 bpm (Incorrect)\n" +
                        "120–140 bpm is an abnormally fast heart rate at rest."
        );

        questions.add("What is the correct pathway of systemic circulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Left ventricle → Aorta → Body → Vena cava → Right atrium", // Correct answer
                "Right atrium → Right ventricle → Aorta → Body",
                "Left atrium → Left ventricle → Pulmonary artery → Body",
                "Right ventricle → Pulmonary artery → Lungs → Pulmonary vein"
        )));
        correctAnswers.add("Left ventricle → Aorta → Body → Vena cava → Right atrium");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Left ventricle → Aorta → Body → Vena cava → Right atrium (Correct answer)\n" +
                        "Systemic circulation starts from the left ventricle, moves through the aorta, supplies tissues, and returns deoxygenated blood to the right atrium via the vena cava.\n\n" +
                        "Right atrium → Right ventricle → Aorta → Body (Incorrect)\n" +
                        "This pathway is not correct for systemic circulation as the aorta comes from the left ventricle.\n\n" +
                        "Left atrium → Left ventricle → Pulmonary artery → Body (Incorrect)\n" +
                        "Pulmonary artery is part of pulmonary circulation, not systemic circulation.\n\n" +
                        "Right ventricle → Pulmonary artery → Lungs → Pulmonary vein (Incorrect)\n" +
                        "This describes pulmonary circulation, not systemic."
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
        new AlertDialog.Builder(ChallengeMode2.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
