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

public class AdvanceChallengeMode6 extends AppCompatActivity {

    private int correctAnswersCount = 0;  // To track the number of correct answers
    private int totalQuestions = 40;       // Total number of questions


    private final int maxQuestions = 40;

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
    private long totalTimeInMillis = 30000; // 1 minute per question
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

        setContentView(R.layout.advance_challenge_mode6);

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
                Toast.makeText(AdvanceChallengeMode6.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(AdvanceChallengeMode6.this)
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
            new AlertDialog.Builder(AdvanceChallengeMode6.this)
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
                new AlertDialog.Builder(AdvanceChallengeMode6.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(AdvanceChallengeMode6.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Muscular System", correctAnswersCount, totalQuestions);

                            intent.putExtra("difficulty", "Advance");
                            intent.putExtra("category", "Muscular System");
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
                        Intent intent = new Intent(AdvanceChallengeMode6.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(AdvanceChallengeMode6.this);
                        AverageHelper averageHelper = new AverageHelper(AdvanceChallengeMode6.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Muscular System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Muscular System");
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
        new AlertDialog.Builder(AdvanceChallengeMode6.this)
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

        questions.add("What is the primary function of skeletal muscle in the human body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Involuntary contraction for organ function",
                "Movement and posture maintenance", // Correct answer
                "Pumping blood throughout the body",
                "Regulating blood vessel diameter"
        )));
        correctAnswers.add("Movement and posture maintenance");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Movement and posture maintenance (Correct answer)\n" +
                        "Skeletal muscles are responsible for voluntary movements, such as walking or lifting, and maintaining posture by resisting gravity.\n\n" +
                        "Involuntary contraction for organ function (Incorrect)\n" +
                        "Involuntary contractions are characteristic of smooth and cardiac muscles, not skeletal muscles, which are voluntary.\n\n" +
                        "Pumping blood throughout the body (Incorrect)\n" +
                        "Pumping blood is the function of cardiac muscle, found in the heart.\n\n" +
                        "Regulating blood vessel diameter (Incorrect)\n" +
                        "Regulating blood vessel diameter is a function of smooth muscle in blood vessel walls."
        );

        questions.add("Which muscle type is striated, involuntary, and found only in the heart?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Skeletal muscle",
                "Smooth muscle",
                "Cardiac muscle", // Correct answer
                "Visceral muscle"
        )));
        correctAnswers.add("Cardiac muscle");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Cardiac muscle (Correct answer)\n" +
                        "Cardiac muscle is striated, involuntary, and exclusive to the heart, with intercalated discs for coordinated contractions.\n\n" +
                        "Skeletal muscle (Incorrect)\n" +
                        "Skeletal muscle is striated but voluntary and attached to bones.\n\n" +
                        "Smooth muscle (Incorrect)\n" +
                        "Smooth muscle is involuntary but non-striated and found in organs.\n\n" +
                        "Visceral muscle (Incorrect)\n" +
                        "“Visceral muscle” is another term for smooth muscle, which is non-striated."
        );

        questions.add("What is the functional unit of skeletal muscle contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myofibril",
                "Sarcomere", // Correct answer
                "Muscle fiber",
                "Actin filament"
        )));
        correctAnswers.add("Sarcomere");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Sarcomere (Correct answer)\n" +
                        "The sarcomere is the smallest contractile unit of a muscle, where actin and myosin filaments interact to produce contraction.\n\n" +
                        "Myofibril (Incorrect)\n" +
                        "Myofibrils are organelles containing sarcomeres but are not the functional unit.\n\n" +
                        "Muscle fiber (Incorrect)\n" +
                        "Muscle fibers are single muscle cells containing many sarcomeres.\n\n" +
                        "Actin filament (Incorrect)\n" +
                        "Actin filaments are components of sarcomeres, not the functional unit."
        );

        questions.add("Which connective tissue surrounds an entire skeletal muscle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Endomysium",
                "Perimysium",
                "Epimysium", // Correct answer
                "Sarcolemma"
        )));
        correctAnswers.add("Epimysium");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Epimysium (Correct answer)\n" +
                        "Epimysium is the connective tissue that encases the entire muscle.\n\n" +
                        "Endomysium (Incorrect)\n" +
                        "Endomysium surrounds individual muscle fibers.\n\n" +
                        "Perimysium (Incorrect)\n" +
                        "Perimysium surrounds fascicles (bundles of muscle fibers).\n\n" +
                        "Sarcolemma (Incorrect)\n" +
                        "Sarcolemma is the muscle fiber’s plasma membrane, not a connective tissue."
        );

        questions.add("What triggers the release of calcium ions in a muscle fiber during contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "ATP binding to myosin",
                "Acetylcholine release at the neuromuscular junction",
                "Action potential in the sarcolemma", // Correct answer
                "Cross-bridge formation"
        )));
        correctAnswers.add("Action potential in the sarcolemma");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Action potential in the sarcolemma (Correct answer)\n" +
                        "An action potential in the sarcolemma spreads through T-tubules, triggering calcium release from the sarcoplasmic reticulum.\n\n" +
                        "ATP binding to myosin (Incorrect)\n" +
                        "ATP binding to myosin occurs during the cross-bridge cycle, not calcium release.\n\n" +
                        "Acetylcholine release at the neuromuscular junction (Incorrect)\n" +
                        "Acetylcholine initiates the action potential but does not directly trigger calcium release.\n\n" +
                        "Cross-bridge formation (Incorrect)\n" +
                        "Cross-bridge formation occurs after calcium binds to troponin."
        );

        questions.add("Which molecule directly powers the myosin head’s power stroke during contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Calcium",
                "ATP", // Correct answer
                "Acetylcholine",
                "Glucose"
        )));
        correctAnswers.add("ATP");
        rationales.put(5,
                "RATIONALE:\n" +
                        "ATP (Correct answer)\n" +
                        "ATP is hydrolyzed to provide energy for the myosin head’s movement during the power stroke.\n\n" +
                        "Calcium (Incorrect)\n" +
                        "Calcium exposes binding sites on actin but does not power the stroke.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Acetylcholine initiates contraction but does not power the stroke.\n\n" +
                        "Glucose (Incorrect)\n" +
                        "Glucose is a fuel source for ATP production, not a direct energy source."
        );

        questions.add("Which muscle property allows it to stretch without damage?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Excitability",
                "Contractility",
                "Extensibility", // Correct answer
                "Elasticity"
        )));
        correctAnswers.add("Extensibility");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Extensibility (Correct answer)\n" +
                        "Extensibility allows muscles to stretch without tearing.\n\n" +
                        "Excitability (Incorrect)\n" +
                        "Excitability is the ability to respond to stimuli.\n\n" +
                        "Contractility (Incorrect)\n" +
                        "Contractility is the ability to shorten and generate force.\n\n" +
                        "Elasticity (Incorrect)\n" +
                        "Elasticity allows muscles to return to their original shape."
        );

        questions.add("What type of muscle contraction occurs when holding a plank position?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Concentric",
                "Eccentric",
                "Isometric", // Correct answer
                "Isotonic"
        )));
        correctAnswers.add("Isometric");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Isometric (Correct answer)\n" +
                        "Isometric contractions generate tension without changing muscle length, as in a plank.\n\n" +
                        "Concentric (Incorrect)\n" +
                        "Concentric contractions involve muscle shortening (e.g., lifting a weight).\n\n" +
                        "Eccentric (Incorrect)\n" +
                        "Eccentric contractions involve muscle lengthening under tension.\n\n" +
                        "Isotonic (Incorrect)\n" +
                        "Isotonic contractions involve muscle length changes (e.g., lifting or lowering)."
        );

        questions.add("Which muscle acts as the agonist during elbow flexion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Triceps brachii",
                "Biceps brachii", // Correct answer
                "Brachioradialis",
                "Deltoid"
        )));
        correctAnswers.add("Biceps brachii");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Biceps brachii (Correct answer)\n" +
                        "Biceps brachii is the primary muscle (agonist) for elbow flexion.\n\n" +
                        "Triceps brachii (Incorrect)\n" +
                        "Triceps brachii is the antagonist, extending the elbow.\n\n" +
                        "Brachioradialis (Incorrect)\n" +
                        "Brachioradialis is a synergist, assisting in elbow flexion.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid acts on the shoulder, not the elbow."
        );

        questions.add("Which muscle is primarily responsible for smiling?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Orbicularis oris",
                "Zygomaticus major", // Correct answer
                "Buccinator",
                "Frontalis"
        )));
        correctAnswers.add("Zygomaticus major");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Zygomaticus major (Correct answer)\n" +
                        "Zygomaticus major elevates the corners of the mouth, producing a smile.\n\n" +
                        "Orbicularis oris (Incorrect)\n" +
                        "Orbicularis oris purses the lips, as in kissing.\n\n" +
                        "Buccinator (Incorrect)\n" +
                        "Buccinator compresses the cheeks, aiding chewing.\n\n" +
                        "Frontalis (Incorrect)\n" +
                        "Frontalis raises the eyebrows and wrinkles the forehead."
        );

        questions.add("Which muscle elevates the mandible during chewing?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Temporalis", // Correct answer
                "Platysma",
                "Sternocleidomastoid",
                "Orbicularis oculi"
        )));
        correctAnswers.add("Temporalis");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Temporalis (Correct answer)\n" +
                        "Temporalis elevates and retracts the mandible, closing the jaw.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible and tenses neck skin.\n\n" +
                        "Sternocleidomastoid (Incorrect)\n" +
                        "Sternocleidomastoid flexes and rotates the neck.\n\n" +
                        "Orbicularis oculi (Incorrect)\n" +
                        "Orbicularis oculi closes the eyelids."
        );

        questions.add("What is the primary function of the sternocleidomastoid muscle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexes the trunk",
                "Rotates and flexes the neck", // Correct answer
                "Elevates the scapula",
                "Compresses the abdomen"
        )));
        correctAnswers.add("Rotates and flexes the neck");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Rotates and flexes the neck (Correct answer)\n" +
                        "Sternocleidomastoid flexes the neck and rotates the head to the opposite side.\n\n" +
                        "Flexes the trunk (Incorrect)\n" +
                        "Trunk flexion is performed by muscles like rectus abdominis.\n\n" +
                        "Elevates the scapula (Incorrect)\n" +
                        "Scapula elevation is performed by the trapezius.\n\n" +
                        "Compresses the abdomen (Incorrect)\n" +
                        "Abdominal compression is performed by muscles like transversus abdominis."
        );

        questions.add("Which muscle is the primary muscle of respiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphragm", // Correct answer
                "External intercostals",
                "Rectus abdominis",
                "Pectoralis major"
        )));
        correctAnswers.add("Diaphragm");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Diaphragm (Correct answer)\n" +
                        "The diaphragm contracts to increase thoracic volume, driving inhalation.\n\n" +
                        "External intercostals (Incorrect)\n" +
                        "External intercostals assist in inhalation but are secondary to the diaphragm.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk, not involved in respiration.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major moves the arm, not the thorax."
        );

        questions.add("Which muscle group extends the vertebral column to maintain posture?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Quadratus lumborum",
                "Erector spinae", // Correct answer
                "External oblique",
                "Transversus abdominis"
        )));
        correctAnswers.add("Erector spinae");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Erector spinae (Correct answer)\n" +
                        "Erector spinae (iliocostalis, longissimus, spinalis) extends the back and maintains posture.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum laterally flexes the trunk.\n\n" +
                        "External oblique (Incorrect)\n" +
                        "External oblique rotates and flexes the trunk.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen."
        );

        questions.add("Which muscle compresses the abdomen and supports the viscera?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rectus abdominis",
                "Internal oblique",
                "Transversus abdominis", // Correct answer
                "Quadratus lumborum"
        )));
        correctAnswers.add("Transversus abdominis");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Transversus abdominis (Correct answer)\n" +
                        "Transversus abdominis compresses the abdomen and supports internal organs.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk.\n\n" +
                        "Internal oblique (Incorrect)\n" +
                        "Internal oblique rotates and flexes the trunk.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum laterally flexes the trunk."
        );

        questions.add("Which muscle is responsible for flexing the trunk during sit-ups?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Erector spinae",
                "Rectus abdominis", // Correct answer
                "Latissimus dorsi",
                "Trapezius"
        )));
        correctAnswers.add("Rectus abdominis");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Rectus abdominis (Correct answer)\n" +
                        "Rectus abdominis flexes the vertebral column, as in sit-ups.\n\n" +
                        "Erector spinae (Incorrect)\n" +
                        "Erector spinae extends the trunk.\n\n" +
                        "Latissimus dorsi (Incorrect)\n" +
                        "Latissimus dorsi acts on the arm, not the trunk.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula and neck."
        );

        questions.add("Which muscle pulls the arm downward and backward, as in swimming?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pectoralis major",
                "Deltoid",
                "Latissimus dorsi", // Correct answer
                "Trapezius"
        )));
        correctAnswers.add("Latissimus dorsi");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Latissimus dorsi (Correct answer)\n" +
                        "Latissimus dorsi extends, adducts, and medially rotates the arm, as in swimming strokes.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major flexes and adducts the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula."
        );

        questions.add("Which muscle is the primary abductor of the arm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Latissimus dorsi",
                "Pectoralis major",
                "Deltoid", // Correct answer
                "Biceps brachii"
        )));
        correctAnswers.add("Deltoid");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Deltoid (Correct answer)\n" +
                        "Deltoid’s middle fibers abduct the arm, raising it laterally.\n\n" +
                        "Latissimus dorsi (Incorrect)\n" +
                        "Latissimus dorsi adducts the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major adducts and flexes the arm.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow."
        );

        questions.add("Which muscle flexes the elbow and supinates the forearm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Triceps brachii",
                "Brachialis",
                "Biceps brachii", // Correct answer
                "Brachioradialis"
        )));
        correctAnswers.add("Biceps brachii");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Biceps brachii (Correct answer)\n" +
                        "Biceps brachii flexes the elbow and supinates the forearm (turns palm up).\n\n" +
                        "Triceps brachii (Incorrect)\n" +
                        "Triceps brachii extends the elbow.\n\n" +
                        "Brachialis (Incorrect)\n" +
                        "Brachialis flexes the elbow but does not supinate.\n\n" +
                        "Brachioradialis (Incorrect)\n" +
                        "Brachioradialis flexes the elbow but does not supinate."
        );

        questions.add("Which muscle extends the elbow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Biceps brachii",
                "Triceps brachii", // Correct answer
                "Brachialis",
                "Flexor carpi radialis"
        )));
        correctAnswers.add("Triceps brachii");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Triceps brachii (Correct answer)\n" +
                        "Triceps brachii is the primary extensor of the elbow.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow.\n\n" +
                        "Brachialis (Incorrect)\n" +
                        "Brachialis flexes the elbow.\n\n" +
                        "Flexor carpi radialis (Incorrect)\n" +
                        "Flexor carpi radialis flexes the wrist."
        );

        questions.add("Which muscle flexes the wrist and abducts the hand?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Extensor carpi radialis",
                "Flexor carpi radialis",
                "Flexor carpi ulnaris",
                "Extensor digitorum"
        )));
        correctAnswers.add("Flexor carpi radialis");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Flexor carpi radialis (Correct answer)\n" +
                        "Flexor carpi radialis flexes the wrist and abducts the hand (moves it laterally).\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends and abducts the wrist.\n\n" +
                        "Flexor carpi ulnaris (Incorrect)\n" +
                        "Flexor carpi ulnaris flexes and adducts the wrist.\n\n" +
                        "Extensor digitorum (Incorrect)\n" +
                        "Extensor digitorum extends the fingers."
        );

        questions.add("Which muscle extends the fingers?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor digitorum superficialis",
                "Extensor digitorum",
                "Flexor digitorum profundus",
                "Abductor pollicis"
        )));
        correctAnswers.add("Extensor digitorum");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Extensor digitorum (Correct answer)\n" +
                        "Extensor digitorum extends the fingers, straightening them.\n\n" +
                        "Flexor digitorum superficialis (Incorrect)\n" +
                        "Flexor digitorum superficialis flexes the fingers.\n\n" +
                        "Flexor digitorum profundus (Incorrect)\n" +
                        "Flexor digitorum profundus flexes the fingers.\n\n" +
                        "Abductor pollicis (Incorrect)\n" +
                        "Abductor pollicis abducts the thumb."
        );

        questions.add("Which muscle flexes the hip, raising the thigh?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus maximus",
                "Iliopsoas",
                "Adductor magnus",
                "Quadriceps femoris"
        )));
        correctAnswers.add("Iliopsoas");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Iliopsoas (Correct answer)\n" +
                        "Iliopsoas (psoas major and iliacus) flexes the hip, as in climbing stairs.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adductor magnus adducts the thigh.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee."
        );

        questions.add("Which muscle extends and laterally rotates the hip?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus medius",
                "Gluteus maximus",
                "Iliopsoas",
                "Adductor longus"
        )));
        correctAnswers.add("Gluteus maximus");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Gluteus maximus (Correct answer)\n" +
                        "Gluteus maximus extends and laterally rotates the hip, as in standing up.\n\n" +
                        "Gluteus medius (Incorrect)\n" +
                        "Gluteus medius abducts and medially rotates the hip.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip.\n\n" +
                        "Adductor longus (Incorrect)\n" +
                        "Adductor longus adducts the thigh."
        );

        questions.add("Which muscle abducts the thigh and stabilizes the pelvis during walking?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus maximus",
                "Gluteus medius",
                "Adductor magnus",
                "Hamstrings"
        )));
        correctAnswers.add("Gluteus medius");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Gluteus medius (Correct answer)\n" +
                        "Gluteus medius abducts the thigh and stabilizes the pelvis during gait.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adductor magnus adducts the thigh.\n\n" +
                        "Hamstrings (Incorrect)\n" +
                        "Hamstrings flex the knee and extend the hip."
        );

        questions.add("Which muscle group extends the knee?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hamstrings",
                "Quadriceps femoris",
                "Adductor group",
                "Gastrocnemius"
        )));
        correctAnswers.add("Quadriceps femoris");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Quadriceps femoris (Correct answer)\n" +
                        "Quadriceps femoris (rectus femoris, vastus muscles) extends the knee, as in kicking.\n\n" +
                        "Hamstrings (Incorrect)\n" +
                        "Hamstrings flex the knee.\n\n" +
                        "Adductor group (Incorrect)\n" +
                        "Adductor group adducts the thigh.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot."
        );

        questions.add("Which muscle flexes the knee and extends the hip?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Quadriceps femoris",
                "Hamstrings",
                "Tibialis anterior",
                "Soleus"
        )));
        correctAnswers.add("Hamstrings");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Hamstrings (Correct answer)\n" +
                        "Hamstrings (biceps femoris, semitendinosus, semimembranosus) flex the knee and extend the hip.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot."
        );

        questions.add("Which muscle dorsiflexes and inverts the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gastrocnemius",
                "Tibialis anterior",
                "Peroneus longus",
                "Soleus"
        )));
        correctAnswers.add("Tibialis anterior");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Tibialis anterior (Correct answer)\n" +
                        "Tibialis anterior dorsiflexes (lifts) and inverts (turns sole inward) the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot.\n\n" +
                        "Peroneus longus (Incorrect)\n" +
                        "Peroneus longus everts and plantarflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot."
        );

        questions.add("Which muscle points the toes and stabilizes the ankle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Gastrocnemius",
                "Soleus",
                "Peroneus brevis"
        )));
        correctAnswers.add("Soleus");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Soleus (Correct answer)\n" +
                        "Soleus plantarflexes the foot and stabilizes the ankle, especially during standing.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot and flexes the knee.\n\n" +
                        "Peroneus brevis (Incorrect)\n" +
                        "Peroneus brevis everts the foot."
        );

        questions.add("Which muscle everts and plantarflexes the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Peroneus longus",
                "Soleus",
                "Gastrocnemius"
        )));
        correctAnswers.add("Peroneus longus");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Peroneus longus (Correct answer)\n" +
                        "Peroneus (fibularis) longus everts (turns sole outward) and plantarflexes the foot.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes and inverts the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes but does not evert the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes but does not evert the foot."
        );

        questions.add("Which energy source is used first during intense muscle activity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glucose",
                "Creatine phosphate",
                "Lactic acid",
                "Fatty acids"
        )));
        correctAnswers.add("Creatine phosphate");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Creatine phosphate (Correct answer)\n" +
                        "Creatine phosphate rapidly donates phosphate to ADP, regenerating ATP for immediate energy.\n\n" +
                        "Glucose (Incorrect)\n" +
                        "Glucose is used in glycolysis, a secondary source after creatine phosphate.\n\n" +
                        "Lactic acid (Incorrect)\n" +
                        "Lactic acid is a byproduct of anaerobic glycolysis, not an energy source.\n\n" +
                        "Fatty acids (Incorrect)\n" +
                        "Fatty acids are used in aerobic respiration, a slower process."
        );

        questions.add("What causes muscle fatigue during prolonged exercise?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Excess ATP production",
                "Lactic acid accumulation",
                "Increased calcium release",
                "Myosin overactivity"
        )));
        correctAnswers.add("Lactic acid accumulation");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Lactic acid accumulation (Correct answer)\n" +
                        "Lactic acid buildup from anaerobic glycolysis lowers pH, impairing muscle function.\n\n" +
                        "Excess ATP production (Incorrect)\n" +
                        "Excess ATP does not cause fatigue; ATP depletion does.\n\n" +
                        "Increased calcium release (Incorrect)\n" +
                        "Increased calcium supports contraction, not fatigue.\n\n" +
                        "Myosin overactivity (Incorrect)\n" +
                        "Myosin activity decreases with fatigue due to energy depletion."
        );

        questions.add("Which neurotransmitter is released at the neuromuscular junction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dopamine",
                "Acetylcholine",
                "Serotonin",
                "GABA"
        )));
        correctAnswers.add("Acetylcholine");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Acetylcholine (Correct answer)\n" +
                        "Acetylcholine is released by motor neurons to initiate muscle contraction.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in brain signaling, not neuromuscular junctions.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Serotonin regulates mood, not muscle contraction.\n\n" +
                        "GABA (Incorrect)\n" +
                        "GABA is an inhibitory neurotransmitter, not used at neuromuscular junctions."
        );

        questions.add("Which structure propagates the action potential into the muscle fiber?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sarcoplasmic reticulum",
                "T-tubules",
                "Myofibrils",
                "Sarcomeres"
        )));
        correctAnswers.add("T-tubules");
        rationales.put(33,
                "RATIONALE:\n" +
                        "T-tubules (Correct answer)\n" +
                        "T-tubules (transverse tubules) conduct the action potential deep into the muscle fiber.\n\n" +
                        "Sarcoplasmic reticulum (Incorrect)\n" +
                        "Sarcoplasmic reticulum stores and releases calcium.\n\n" +
                        "Myofibrils (Incorrect)\n" +
                        "Myofibrils contain sarcomeres and are contractile, not conductive.\n\n" +
                        "Sarcomeres (Incorrect)\n" +
                        "Sarcomeres are contractile units, not involved in signal propagation."
        );

        questions.add("What binds to troponin to initiate muscle contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "ATP",
                "Myosin",
                "Calcium",
                "Actin"
        )));
        correctAnswers.add("Calcium");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Calcium (Correct answer)\n" +
                        "Calcium binds to troponin, causing a conformational change that exposes actin’s binding sites.\n\n" +
                        "ATP (Incorrect)\n" +
                        "ATP binds to myosin, not troponin.\n\n" +
                        "Myosin (Incorrect)\n" +
                        "Myosin binds to actin during contraction, not troponin.\n\n" +
                        "Actin (Incorrect)\n" +
                        "Actin interacts with myosin, not troponin directly."
        );

        questions.add("Which muscle is responsible for pursing the lips, as in whistling?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Zygomaticus major",
                "Orbicularis oris",
                "Buccinator",
                "Masseter"
        )));
        correctAnswers.add("Orbicularis oris");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Orbicularis oris (Correct answer)\n" +
                        "Orbicularis oris closes and purses the lips, as in whistling or kissing.\n\n" +
                        "Zygomaticus major (Incorrect)\n" +
                        "Zygomaticus major elevates the mouth for smiling.\n\n" +
                        "Buccinator (Incorrect)\n" +
                        "Buccinator compresses the cheeks.\n\n" +
                        "Masseter (Incorrect)\n" +
                        "Masseter elevates the mandible for chewing."
        );

        questions.add("Which muscle wrinkles the forehead and raises the eyebrows?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Occipitalis",
                "Frontalis",
                "Orbicularis oculi",
                "Platysma"
        )));
        correctAnswers.add("Frontalis");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Frontalis (Correct answer)\n" +
                        "Frontalis raises the eyebrows and wrinkles the forehead.\n\n" +
                        "Occipitalis (Incorrect)\n" +
                        "Occipitalis retracts the scalp.\n\n" +
                        "Orbicularis oculi (Incorrect)\n" +
                        "Orbicularis oculi closes the eyelids.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible and tenses neck skin."
        );

        questions.add("Which muscle compresses the cheeks during chewing?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Masseter",
                "Temporalis",
                "Buccinator",
                "Zygomaticus minor"
        )));
        correctAnswers.add("Buccinator");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Buccinator (Correct answer)\n" +
                        "Buccinator compresses the cheeks, keeping food against the teeth.\n\n" +
                        "Masseter (Incorrect)\n" +
                        "Masseter elevates the mandible.\n\n" +
                        "Temporalis (Incorrect)\n" +
                        "Temporalis elevates and retracts the mandible.\n\n" +
                        "Zygomaticus minor (Incorrect)\n" +
                        "Zygomaticus minor assists in smiling."
        );

        questions.add("Which muscle rotates the head to the opposite side?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Platysma",
                "Sternocleidomastoid",
                "Trapezius",
                "Quadratus lumborum"
        )));
        correctAnswers.add("Sternocleidomastoid");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Sternocleidomastoid (Correct answer)\n" +
                        "Sternocleidomastoid rotates the head to the opposite side and flexes the neck.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma tenses neck skin and depresses the mandible.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula and extends the neck.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum flexes the trunk laterally."
        );

        questions.add("Which muscle retracts the scalp?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Frontalis",
                "Occipitalis",
                "Orbicularis oris",
                "Masseter"
        )));
        correctAnswers.add("Occipitalis");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Occipitalis (Correct answer)\n" +
                        "Occipitalis pulls the scalp posteriorly.\n\n" +
                        "Frontalis (Incorrect)\n" +
                        "Frontalis raises the eyebrows.\n\n" +
                        "Orbicularis oris (Incorrect)\n" +
                        "Orbicularis oris purses the lips.\n\n" +
                        "Masseter (Incorrect)\n" +
                        "Masseter elevates the mandible."
        );

        questions.add("Which muscle is primarily responsible for lateral trunk flexion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rectus abdominis",
                "Quadratus lumborum",
                "Transversus abdominis",
                "Internal oblique"
        )));
        correctAnswers.add("Quadratus lumborum");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Quadratus lumborum (Correct answer)\n" +
                        "Quadratus lumborum laterally flexes the vertebral column.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk anteriorly.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen.\n\n" +
                        "Internal oblique (Incorrect)\n" +
                        "Internal oblique rotates and flexes the trunk."
        );

        questions.add("Which muscle rotates the trunk to the opposite side?");
        choices.add(new ArrayList<>(Arrays.asList(
                "External oblique",
                "Internal oblique",
                "Rectus abdominis",
                "Erector spinae"
        )));
        correctAnswers.add("External oblique");
        rationales.put(41,
                "RATIONALE:\n" +
                        "External oblique (Correct answer)\n" +
                        "External oblique rotates the trunk to the opposite side when contracted unilaterally.\n\n" +
                        "Internal oblique (Incorrect)\n" +
                        "Internal oblique rotates the trunk to the same side.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk.\n\n" +
                        "Erector spinae (Incorrect)\n" +
                        "Erector spinae extends the trunk."
        );

        questions.add("Which muscle elevates the ribs during inhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Internal intercostals",
                "External intercostals",
                "Diaphragm",
                "Transversus abdominis"
        )));
        correctAnswers.add("External intercostals");
        rationales.put(42,
                "RATIONALE:\n" +
                        "External intercostals (Correct answer)\n" +
                        "External intercostals lift the ribs, aiding inhalation.\n\n" +
                        "Internal intercostals (Incorrect)\n" +
                        "Internal intercostals depress the ribs during exhalation.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Diaphragm is the primary muscle of inhalation but does not elevate ribs directly.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen."
        );

        questions.add("Which muscle stabilizes the scapula during arm movements?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pectoralis major",
                "Latissimus dorsi",
                "Trapezius",
                "Deltoid"
        )));
        correctAnswers.add("Trapezius");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Trapezius (Correct answer)\n" +
                        "Trapezius stabilizes and moves the scapula (e.g., elevation, retraction).\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major moves the arm.\n\n" +
                        "Latissimus dorsi (Incorrect)\n" +
                        "Latissimus dorsi moves the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid moves the arm, not the scapula."
        );

        questions.add("Which muscle adducts and medially rotates the arm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Deltoid",
                "Pectoralis major",
                "Trapezius",
                "Biceps brachii"
        )));
        correctAnswers.add("Pectoralis major");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Pectoralis major (Correct answer)\n" +
                        "Pectoralis major adducts, flexes, and medially rotates the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow."
        );

        questions.add("Which muscle is the primary flexor of the elbow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Biceps brachii",
                "Brachialis",
                "Triceps brachii",
                "Brachioradialis"
        )));
        correctAnswers.add("Brachialis");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Brachialis (Correct answer)\n" +
                        "Brachialis is the primary flexor of the elbow due to its optimal mechanical advantage.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow and supinates, but is not the primary flexor.\n\n" +
                        "Triceps brachii (Incorrect)\n" +
                        "Triceps brachii extends the elbow.\n\n" +
                        "Brachioradialis (Incorrect)\n" +
                        "Brachioradialis assists in elbow flexion but is not the primary flexor."
        );

        questions.add("Which muscle abducts the thumb?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor pollicis longus",
                "Abductor pollicis brevis",
                "Extensor pollicis brevis",
                "Adductor pollicis"
        )));
        correctAnswers.add("Abductor pollicis brevis");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Abductor pollicis brevis (Correct answer)\n" +
                        "Abductor pollicis brevis abducts the thumb, moving it away from the palm.\n\n" +
                        "Flexor pollicis longus (Incorrect)\n" +
                        "Flexor pollicis longus flexes the thumb.\n\n" +
                        "Extensor pollicis brevis (Incorrect)\n" +
                        "Extensor pollicis brevis extends the thumb.\n\n" +
                        "Adductor pollicis (Incorrect)\n" +
                        "Adductor pollicis adducts the thumb."
        );

        questions.add("Which muscle extends the hip and flexes the knee?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Quadriceps femoris",
                "Hamstrings",
                "Gluteus maximus",
                "Iliopsoas"
        )));
        correctAnswers.add("Hamstrings");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Hamstrings (Correct answer)\n" +
                        "Hamstrings extend the hip and flex the knee.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip but does not flex the knee.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip."
        );

        questions.add("Which muscle stabilizes the pelvis during walking?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus maximus",
                "Gluteus medius",
                "Adductor magnus",
                "Rectus femoris"
        )));
        correctAnswers.add("Gluteus medius");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Gluteus medius (Correct answer)\n" +
                        "Gluteus medius abducts the thigh and stabilizes the pelvis during gait.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adductor magnus adducts the thigh.\n\n" +
                        "Rectus femoris (Incorrect)\n" +
                        "Rectus femoris flexes the hip and extends the knee."
        );

        questions.add("Which muscle plantarflexes the foot and flexes the knee?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Gastrocnemius",
                "Soleus",
                "Peroneus longus"
        )));
        correctAnswers.add("Gastrocnemius");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Gastrocnemius (Correct answer)\n" +
                        "Gastrocnemius plantarflexes the foot and flexes the knee.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot but does not flex the knee.\n\n" +
                        "Peroneus longus (Incorrect)\n" +
                        "Peroneus longus everts and plantarflexes the foot."
        );

        questions.add("What is the primary source of ATP during prolonged aerobic exercise?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Creatine phosphate",
                "Glycolysis",
                "Cellular respiration", // Correct answer
                "Lactic acid"
        )));
        correctAnswers.add("Cellular respiration");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Cellular respiration (Correct answer)\n" +
                        "Cellular respiration in mitochondria produces large amounts of ATP from glucose or fats during aerobic exercise.\n\n" +
                        "Creatine phosphate (Incorrect)\n" +
                        "Creatine phosphate is used for short bursts of activity.\n\n" +
                        "Glycolysis (Incorrect)\n" +
                        "Glycolysis provides ATP quickly but is less efficient for prolonged exercise.\n\n" +
                        "Lactic acid (Incorrect)\n" +
                        "Lactic acid is a byproduct, not a source of ATP."
        );

        questions.add("Which muscle disorder is characterized by progressive muscle weakness?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myasthenia gravis",
                "Muscular dystrophy", // Correct answer
                "Fibromyalgia",
                "Cramps"
        )));
        correctAnswers.add("Muscular dystrophy");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Muscular dystrophy (Correct answer)\n" +
                        "Muscular dystrophy is a genetic disorder causing progressive muscle degeneration and weakness.\n\n" +
                        "Myasthenia gravis (Incorrect)\n" +
                        "Myasthenia gravis causes muscle fatigue due to impaired neuromuscular transmission.\n\n" +
                        "Fibromyalgia (Incorrect)\n" +
                        "Fibromyalgia causes widespread pain and fatigue, not progressive weakness.\n\n" +
                        "Cramps (Incorrect)\n" +
                        "Cramps are temporary, painful contractions."
        );

        questions.add("What causes myasthenia gravis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Genetic mutation in muscle proteins",
                "Autoimmune attack on acetylcholine receptors", // Correct answer
                "Electrolyte imbalance",
                "Overuse of muscles"
        )));
        correctAnswers.add("Autoimmune attack on acetylcholine receptors");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Autoimmune attack on acetylcholine receptors (Correct answer)\n" +
                        "Myasthenia gravis is an autoimmune disorder targeting acetylcholine receptors, impairing muscle contraction.\n\n" +
                        "Genetic mutation in muscle proteins (Incorrect)\n" +
                        "Genetic mutations cause muscular dystrophy, not myasthenia gravis.\n\n" +
                        "Electrolyte imbalance (Incorrect)\n" +
                        "Electrolyte imbalances may cause cramps, not myasthenia gravis.\n\n" +
                        "Overuse of muscles (Incorrect)\n" +
                        "Overuse causes fatigue, not myasthenia gravis."
        );

        questions.add("Which muscle is responsible for blinking and squinting?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Orbicularis oris",
                "Zygomaticus major",
                "Orbicularis oculi", // Correct answer
                "Frontalis"
        )));
        correctAnswers.add("Orbicularis oculi");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Orbicularis oculi (Correct answer)\n" +
                        "Orbicularis oculi closes the eyelids, enabling blinking and squinting.\n\n" +
                        "Orbicularis oris (Incorrect)\n" +
                        "Orbicularis oris purses the lips.\n\n" +
                        "Zygomaticus major (Incorrect)\n" +
                        "Zygomaticus major elevates the mouth for smiling.\n\n" +
                        "Frontalis (Incorrect)\n" +
                        "Frontalis raises the eyebrows."
        );

        questions.add("Which muscle depresses the mandible and tenses neck skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Platysma", // Correct answer
                "Sternocleidomastoid",
                "Masseter",
                "Temporalis"
        )));
        correctAnswers.add("Platysma");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Platysma (Correct answer)\n" +
                        "Platysma depresses the mandible and tenses the skin of the neck.\n\n" +
                        "Sternocleidomastoid (Incorrect)\n" +
                        "Sternocleidomastoid rotates and flexes the neck.\n\n" +
                        "Masseter (Incorrect)\n" +
                        "Masseter elevates the mandible.\n\n" +
                        "Temporalis (Incorrect)\n" +
                        "Temporalis elevates and retracts the mandible."
        );

        questions.add("Which muscle rotates the trunk to the same side?");
        choices.add(new ArrayList<>(Arrays.asList(
                "External oblique",
                "Internal oblique", // Correct answer
                "Rectus abdominis",
                "Transversus abdominis"
        )));
        correctAnswers.add("Internal oblique");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Internal oblique (Correct answer)\n" +
                        "Internal oblique rotates the trunk to the same side when contracted unilaterally.\n\n" +
                        "External oblique (Incorrect)\n" +
                        "External oblique rotates the trunk to the opposite side.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen."
        );

        questions.add("Which muscle elevates the scapula?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Latissimus dorsi",
                "Trapezius", // Correct answer
                "Pectoralis major",
                "Deltoid"
        )));
        correctAnswers.add("Trapezius");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Trapezius (Correct answer)\n" +
                        "Trapezius elevates, retracts, and rotates the scapula.\n\n" +
                        "Latissimus dorsi (Incorrect)\n" +
                        "Latissimus dorsi moves the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major moves the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid moves the arm."
        );

        questions.add("Which muscle flexes the fingers?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Extensor digitorum",
                "Flexor digitorum profundus", // Correct answer
                "Abductor digiti minimi",
                "Extensor carpi radialis"
        )));
        correctAnswers.add("Flexor digitorum profundus");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Flexor digitorum profundus (Correct answer)\n" +
                        "Flexor digitorum profundus flexes the fingers, enabling gripping.\n\n" +
                        "Extensor digitorum (Incorrect)\n" +
                        "Extensor digitorum extends the fingers.\n\n" +
                        "Abductor digiti minimi (Incorrect)\n" +
                        "Abductor digiti minimi abducts the little finger.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends the wrist."
        );

        questions.add("Which muscle adducts the thigh?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus medius",
                "Adductor magnus", // Correct answer
                "Iliopsoas",
                "Rectus femoris"
        )));
        correctAnswers.add("Adductor magnus");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Adductor magnus (Correct answer)\n" +
                        "Adductor magnus adducts the thigh, pulling it inward.\n\n" +
                        "Gluteus medius (Incorrect)\n" +
                        "Gluteus medius abducts the thigh.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip.\n\n" +
                        "Rectus femoris (Incorrect)\n" +
                        "Rectus femoris flexes the hip and extends the knee."
        );

        questions.add("Which muscle extends the fingers and wrist?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor carpi ulnaris",
                "Extensor digitorum", // Correct answer
                "Flexor digitorum superficialis",
                "Abductor pollicis"
        )));
        correctAnswers.add("Extensor digitorum");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Extensor digitorum (Correct answer)\n" +
                        "Extensor digitorum extends the fingers and assists in wrist extension.\n\n" +
                        "Flexor carpi ulnaris (Incorrect)\n" +
                        "Flexor carpi ulnaris flexes the wrist.\n\n" +
                        "Flexor digitorum superficialis (Incorrect)\n" +
                        "Flexor digitorum superficialis flexes the fingers.\n\n" +
                        "Abductor pollicis (Incorrect)\n" +
                        "Abductor pollicis abducts the thumb."
        );

        questions.add("Which muscle is responsible for eversion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Peroneus longus",
                "Soleus",
                "Gastrocnemius"
        )));
        correctAnswers.add("Peroneus longus");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Peroneus longus (Correct answer)\n" +
                        "Peroneus longus everts the foot, turning the sole outward.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior inverts the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot."
        );

        questions.add("Which muscle is the primary extensor of the vertebral column?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Quadratus lumborum",
                "Erector spinae",
                "External oblique",
                "Rectus abdominis"
        )));
        correctAnswers.add("Erector spinae");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Erector spinae (Correct answer)\n" +
                        "Erector spinae extends the vertebral column, maintaining posture.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum laterally flexes the trunk.\n\n" +
                        "External oblique (Incorrect)\n" +
                        "External oblique rotates and flexes the trunk.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk."
        );

        questions.add("Which muscle compresses the abdomen during forced exhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphragm",
                "Transversus abdominis",
                "Erector spinae",
                "Pectoralis major"
        )));
        correctAnswers.add("Transversus abdominis");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Transversus abdominis (Correct answer)\n" +
                        "Transversus abdominis compresses the abdomen, aiding forced exhalation.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Diaphragm drives inhalation.\n\n" +
                        "Erector spinae (Incorrect)\n" +
                        "Erector spinae extends the back.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major moves the arm."
        );

        questions.add("Which muscle flexes the neck and rotates the head?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Trapezius",
                "Sternocleidomastoid",
                "Platysma",
                "Occipitalis"
        )));
        correctAnswers.add("Sternocleidomastoid");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Sternocleidomastoid (Correct answer)\n" +
                        "Sternocleidomastoid flexes the neck and rotates the head to the opposite side.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula and extends the neck.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible.\n\n" +
                        "Occipitalis (Incorrect)\n" +
                        "Occipitalis retracts the scalp."
        );

        questions.add("Which muscle is responsible for chewing by elevating the mandible?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Buccinator",
                "Masseter",
                "Orbicularis oris",
                "Zygomaticus major"
        )));
        correctAnswers.add("Masseter");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Masseter (Correct answer)\n" +
                        "Masseter elevates the mandible, closing the jaw for chewing.\n\n" +
                        "Buccinator (Incorrect)\n" +
                        "Buccinator compresses the cheeks.\n\n" +
                        "Orbicularis oris (Incorrect)\n" +
                        "Orbicularis oris purses the lips.\n\n" +
                        "Zygomaticus major (Incorrect)\n" +
                        "Zygomaticus major elevates the mouth for smiling."
        );

        questions.add("Which muscle extends the arm at the shoulder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pectoralis major",
                "Deltoid",
                "Latissimus dorsi",
                "Biceps brachii"
        )));
        correctAnswers.add("Latissimus dorsi");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Latissimus dorsi (Correct answer)\n" +
                        "Latissimus dorsi extends, adducts, and medially rotates the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major flexes and adducts the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm, with some fibers assisting in extension.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow."
        );

        questions.add("Which muscle flexes the wrist and adducts the hand?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor carpi radialis",
                "Extensor carpi ulnaris",
                "Flexor carpi ulnaris",
                "Extensor carpi radialis"
        )));
        correctAnswers.add("Flexor carpi ulnaris");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Flexor carpi ulnaris (Correct answer)\n" +
                        "Flexor carpi ulnaris flexes the wrist and adducts the hand.\n\n" +
                        "Flexor carpi radialis (Incorrect)\n" +
                        "Flexor carpi radialis flexes and abducts the wrist.\n\n" +
                        "Extensor carpi ulnaris (Incorrect)\n" +
                        "Extensor carpi ulnaris extends and adducts the wrist.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends and abducts the wrist."
        );

        questions.add("Which muscle extends the knee and flexes the hip?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hamstrings",
                "Rectus femoris",
                "Gluteus maximus",
                "Adductor magnus"
        )));
        correctAnswers.add("Rectus femoris");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Rectus femoris (Correct answer)\n" +
                        "Rectus femoris, part of the quadriceps, extends the knee and flexes the hip.\n\n" +
                        "Hamstrings (Incorrect)\n" +
                        "Hamstrings flex the knee and extend the hip.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adductor magnus adducts the thigh."
        );

        questions.add("Which muscle is responsible for dorsiflexion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gastrocnemius",
                "Soleus",
                "Tibialis anterior",
                "Peroneus brevis"
        )));
        correctAnswers.add("Tibialis anterior");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Tibialis anterior (Correct answer)\n" +
                        "Tibialis anterior dorsiflexes and inverts the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot.\n\n" +
                        "Peroneus brevis (Incorrect)\n" +
                        "Peroneus brevis everts the foot."
        );

        questions.add("Which muscle is involved in forced inhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Internal intercostals",
                "External intercostals",
                "Transversus abdominis",
                "Rectus abdominis"
        )));
        correctAnswers.add("External intercostals");
        rationales.put(69,
                "RATIONALE:\n" +
                        "External intercostals (Correct answer)\n" +
                        "External intercostals elevate the ribs, assisting in inhalation.\n\n" +
                        "Internal intercostals (Incorrect)\n" +
                        "Internal intercostals aid exhalation.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk."
        );

        questions.add("Which muscle rotates the arm medially?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Deltoid",
                "Pectoralis major",
                "Trapezius",
                "Biceps brachii"
        )));
        correctAnswers.add("Pectoralis major");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Pectoralis major (Correct answer)\n" +
                        "Pectoralis major medially rotates, flexes, and adducts the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow."
        );

        questions.add("Which muscle flexes the hip and stabilizes the pelvis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus maximus",
                "Iliopsoas",
                "Adductor longus",
                "Quadriceps femoris"
        )));
        correctAnswers.add("Iliopsoas");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Iliopsoas (Correct answer)\n" +
                        "Iliopsoas flexes the hip and helps stabilize the pelvis.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip.\n\n" +
                        "Adductor longus (Incorrect)\n" +
                        "Adductor longus adducts the thigh.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee."
        );

        questions.add("Which muscle is responsible for plantarflexion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Gastrocnemius",
                "Peroneus longus",
                "All of the above"
        )));
        correctAnswers.add("All of the above");
        rationales.put(72,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Both gastrocnemius and peroneus longus contribute to plantarflexion, making this the best choice.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes the foot, not plantarflexes.\n\n" +
                        "Gastrocnemius (Correct)\n" +
                        "Gastrocnemius plantarflexes the foot and flexes the knee.\n\n" +
                        "Peroneus longus (Correct)\n" +
                        "Peroneus longus everts and plantarflexes the foot."
        );

        questions.add("Which muscle extends the vertebral column and rotates it?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Erector spinae",
                "Quadratus lumborum",
                "External oblique",
                "Transversus abdominis"
        )));
        correctAnswers.add("Erector spinae");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Erector spinae (Correct answer)\n" +
                        "Erector spinae extends and rotates the vertebral column.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum laterally flexes the trunk.\n\n" +
                        "External oblique (Incorrect)\n" +
                        "External oblique rotates and flexes the trunk.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen."
        );

        questions.add("Which muscle is responsible for closing the jaw?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Platysma",
                "Masseter",
                "Buccinator",
                "Orbicularis oris"
        )));
        correctAnswers.add("Masseter");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Masseter (Correct answer)\n" +
                        "Masseter elevates the mandible, closing the jaw.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible.\n\n" +
                        "Buccinator (Incorrect)\n" +
                        "Buccinator compresses the cheeks.\n\n" +
                        "Orbicularis oris (Incorrect)\n" +
                        "Orbicularis oris purses the lips."
        );

        questions.add("Which muscle flexes the elbow and is located deep to the biceps brachii?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Triceps brachii",
                "Brachialis",
                "Brachioradialis",
                "Anconeus"
        )));
        correctAnswers.add("Brachialis");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Brachialis (Correct answer)\n" +
                        "Brachialis, deep to the biceps brachii, is the primary flexor of the elbow.\n\n" +
                        "Triceps brachii (Incorrect)\n" +
                        "Triceps brachii extends the elbow.\n\n" +
                        "Brachioradialis (Incorrect)\n" +
                        "Brachioradialis is superficial and assists in elbow flexion.\n\n" +
                        "Anconeus (Incorrect)\n" +
                        "Anconeus extends the elbow."
        );

        questions.add("Which muscle abducts the fingers?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor digitorum profundus",
                "Dorsal interossei",
                "Extensor digitorum",
                "Flexor digitorum superficialis"
        )));
        correctAnswers.add("Dorsal interossei");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Dorsal interossei (Correct answer)\n" +
                        "Dorsal interossei abduct the fingers, spreading them apart.\n\n" +
                        "Flexor digitorum profundus (Incorrect)\n" +
                        "Flexor digitorum profundus flexes the fingers.\n\n" +
                        "Extensor digitorum (Incorrect)\n" +
                        "Extensor digitorum extends the fingers.\n\n" +
                        "Flexor digitorum superficialis (Incorrect)\n" +
                        "Flexor digitorum superficialis flexes the fingers."
        );

        questions.add("Which muscle extends the hip and is the largest muscle in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus medius",
                "Gluteus maximus",
                "Iliopsoas",
                "Quadriceps femoris"
        )));
        correctAnswers.add("Gluteus maximus");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Gluteus maximus (Correct answer)\n" +
                        "Gluteus maximus, the largest muscle, extends and laterally rotates the hip.\n\n" +
                        "Gluteus medius (Incorrect)\n" +
                        "Gluteus medius abducts the hip.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee."
        );

        questions.add("Which muscle inverts the foot and assists in dorsiflexion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Peroneus longus",
                "Tibialis anterior",
                "Gastrocnemius",
                "Soleus"
        )));
        correctAnswers.add("Tibialis anterior");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Tibialis anterior (Correct answer)\n" +
                        "Tibialis anterior dorsiflexes and inverts the foot.\n\n" +
                        "Peroneus longus (Incorrect)\n" +
                        "Peroneus longus everts the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot."
        );

        questions.add("Which muscle is responsible for forced exhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphragm",
                "External intercostals",
                "Internal intercostals",
                "Transversus abdominis"
        )));
        correctAnswers.add("Internal intercostals");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Internal intercostals (Correct answer)\n" +
                        "Internal intercostals depress the ribs, aiding forced exhalation.\n\n" +
                        "Diaphragm (Incorrect)\n" +
                        "Diaphragm drives inhalation.\n\n" +
                        "External intercostals (Incorrect)\n" +
                        "External intercostals aid inhalation.\n\n" +
                        "Transversus abdominis (Incorrect)\n" +
                        "Transversus abdominis compresses the abdomen but is secondary in exhalation."
        );

        questions.add("Which muscle extends the arm and adducts it?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pectoralis major",
                "Latissimus dorsi", // Correct answer
                "Deltoid",
                "Trapezius"
        )));
        correctAnswers.add("Latissimus dorsi");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Latissimus dorsi (Correct answer)\n" +
                        "Latissimus dorsi extends, adducts, and medially rotates the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major flexes and adducts the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula."
        );

        questions.add("Which muscle flexes the wrist and abducts the hand?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor carpi ulnaris",
                "Extensor carpi radialis",
                "Flexor carpi radialis", // Correct answer
                "Extensor carpi ulnaris"
        )));
        correctAnswers.add("Flexor carpi radialis");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Flexor carpi radialis (Correct answer)\n" +
                        "Flexor carpi radialis flexes the wrist and abducts the hand.\n\n" +
                        "Flexor carpi ulnaris (Incorrect)\n" +
                        "Flexor carpi ulnaris flexes and adducts the wrist.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends and abducts the wrist.\n\n" +
                        "Extensor carpi ulnaris (Incorrect)\n" +
                        "Extensor carpi ulnaris extends and adducts the wrist."
        );

        questions.add("Which muscle flexes the knee and extends the hip?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Quadriceps femoris",
                "Hamstrings", // Correct answer
                "Gluteus maximus",
                "Iliopsoas"
        )));
        correctAnswers.add("Hamstrings");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Hamstrings (Correct answer)\n" +
                        "Hamstrings flex the knee and extend the hip.\n\n" +
                        "Quadriceps femoris (Incorrect)\n" +
                        "Quadriceps femoris extends the knee.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Gluteus maximus extends the hip but does not flex the knee.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip."
        );

        questions.add("Which muscle is responsible for eversion and plantarflexion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Peroneus longus", // Correct answer
                "Soleus",
                "Gastrocnemius"
        )));
        correctAnswers.add("Peroneus longus");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Peroneus longus (Correct answer)\n" +
                        "Peroneus longus everts and plantarflexes the foot.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Tibialis anterior dorsiflexes and inverts the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot but does not evert.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot but does not evert."
        );

        questions.add("Which muscle is the primary muscle of inhalation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "External intercostals",
                "Diaphragm", // Correct answer
                "Internal intercostals",
                "Rectus abdominis"
        )));
        correctAnswers.add("Diaphragm");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Diaphragm (Correct answer)\n" +
                        "The diaphragm is the primary muscle of inhalation, increasing thoracic volume.\n\n" +
                        "External intercostals (Incorrect)\n" +
                        "External intercostals assist in inhalation but are secondary.\n\n" +
                        "Internal intercostals (Incorrect)\n" +
                        "Internal intercostals aid exhalation.\n\n" +
                        "Rectus abdominis (Incorrect)\n" +
                        "Rectus abdominis flexes the trunk."
        );

        questions.add("Which muscle rotates the head to the same side?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sternocleidomastoid",
                "Trapezius",
                "Splenius capitis", // Correct answer
                "Platysma"
        )));
        correctAnswers.add("Splenius capitis");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Splenius capitis (Correct answer)\n" +
                        "Splenius capitis rotates the head to the same side and extends the neck.\n\n" +
                        "Sternocleidomastoid (Incorrect)\n" +
                        "Sternocleidomastoid rotates the head to the opposite side.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius extends the neck and moves the scapula.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible."
        );

        questions.add("Which muscle flexes the fingers and wrist?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Extensor digitorum",
                "Flexor digitorum profundus", // Correct answer
                "Extensor carpi radialis",
                "Abductor pollicis"
        )));
        correctAnswers.add("Flexor digitorum profundus");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Flexor digitorum profundus (Correct answer)\n" +
                        "Flexor digitorum profundus flexes the fingers and assists in wrist flexion.\n\n" +
                        "Extensor digitorum (Incorrect)\n" +
                        "Extensor digitorum extends the fingers.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends the wrist.\n\n" +
                        "Abductor pollicis (Incorrect)\n" +
                        "Abductor pollicis abducts the thumb."
        );

        questions.add("Which muscle adducts and flexes the thigh?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus medius",
                "Adductor longus", // Correct answer
                "Iliopsoas",
                "Rectus femoris"
        )));
        correctAnswers.add("Adductor longus");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Adductor longus (Correct answer)\n" +
                        "Adductor longus adducts and flexes the thigh.\n\n" +
                        "Gluteus medius (Incorrect)\n" +
                        "Gluteus medius abducts the thigh.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Iliopsoas flexes the hip but does not adduct.\n\n" +
                        "Rectus femoris (Incorrect)\n" +
                        "Rectus femoris flexes the hip and extends the knee."
        );

        questions.add("Which muscle extends the wrist and abducts the hand?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor carpi radialis",
                "Extensor carpi radialis", // Correct answer
                "Flexor carpi ulnaris",
                "Extensor carpi ulnaris"
        )));
        correctAnswers.add("Extensor carpi radialis");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Extensor carpi radialis (Correct answer)\n" +
                        "Extensor carpi radialis extends the wrist and abducts the hand.\n\n" +
                        "Flexor carpi radialis (Incorrect)\n" +
                        "Flexor carpi radialis flexes and abducts the wrist.\n\n" +
                        "Flexor carpi ulnaris (Incorrect)\n" +
                        "Flexor carpi ulnaris flexes and adducts the wrist.\n\n" +
                        "Extensor carpi ulnaris (Incorrect)\n" +
                        "Extensor carpi ulnaris extends and adducts the wrist."
        );

        questions.add("Which muscle is responsible for dorsiflexion and inversion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Peroneus longus",
                "Tibialis anterior", // Correct answer
                "Gastrocnemius",
                "Soleus"
        )));
        correctAnswers.add("Tibialis anterior");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Tibialis anterior (Correct answer)\n" +
                        "Tibialis anterior dorsiflexes and inverts the foot.\n\n" +
                        "Peroneus longus (Incorrect)\n" +
                        "Peroneus longus everts and plantarflexes the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Gastrocnemius plantarflexes the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Soleus plantarflexes the foot."
        );

        questions.add("Which muscle compresses the abdomen and aids in trunk flexion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Erector spinae",
                "Rectus abdominis",
                "Quadratus lumborum",
                "Trapezius"
        )));
        correctAnswers.add("Rectus abdominis");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Rectus abdominis (Correct answer)\n" +
                        "Rectus abdominis compresses the abdomen and flexes the trunk.\n\n" +
                        "Erector spinae (Incorrect)\n" +
                        "Erector spinae extends the trunk.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Quadratus lumborum laterally flexes the trunk.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Trapezius moves the scapula."
        );

        questions.add("Which muscle elevates the mandible and retracts it?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Masseter",
                "Temporalis",
                "Buccinator",
                "Platysma"
        )));
        correctAnswers.add("Temporalis");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Temporalis (Correct answer)\n" +
                        "Temporalis elevates and retracts the mandible.\n\n" +
                        "Masseter (Incorrect)\n" +
                        "Masseter elevates the mandible but does not retract it significantly.\n\n" +
                        "Buccinator (Incorrect)\n" +
                        "Buccinator compresses the cheeks.\n\n" +
                        "Platysma (Incorrect)\n" +
                        "Platysma depresses the mandible."
        );

        questions.add("Which muscle extends the arm and rotates it medially?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pectoralis major",
                "Latissimus dorsi",
                "Deltoid",
                "Biceps brachii"
        )));
        correctAnswers.add("Latissimus dorsi");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Latissimus dorsi (Correct answer)\n" +
                        "Latissimus dorsi extends, adducts, and medially rotates the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Pectoralis major flexes and medially rotates the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Deltoid abducts the arm.\n\n" +
                        "Biceps brachii (Incorrect)\n" +
                        "Biceps brachii flexes the elbow."
        );

        questions.add("Which muscle flexes the wrist and adducts the hand?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Flexor carpi radialis",
                "Extensor carpi ulnaris",
                "Flexor carpi ulnaris",
                "Extensor carpi radialis"
        )));
        correctAnswers.add("Flexor carpi ulnaris");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Flexor carpi ulnaris (Correct answer)\n" +
                        "Flexor carpi ulnaris flexes the wrist and adducts the hand.\n\n" +
                        "Flexor carpi radialis (Incorrect)\n" +
                        "Flexor carpi radialis flexes and abducts the wrist.\n\n" +
                        "Extensor carpi ulnaris (Incorrect)\n" +
                        "Extensor carpi ulnaris extends and adducts the wrist.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extensor carpi radialis extends and abducts the wrist."
        );

        questions.add("Which muscle extends the knee and is part of the quadriceps group?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Biceps femoris",
                "Vastus lateralis",
                "Gluteus maximus",
                "Adductor magnus"
        )));
        correctAnswers.add("Vastus lateralis");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Vastus lateralis (Correct answer)\n" +
                        "Part of the quadriceps femoris group, extends the knee.\n\n" +
                        "Biceps femoris (Incorrect)\n" +
                        "Flexes the knee.\n\n" +
                        "Gluteus maximus (Incorrect)\n" +
                        "Extends the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adducts the thigh."
        );

        questions.add("Which muscle is responsible for eversion of the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibialis anterior",
                "Peroneus brevis",
                "Soleus",
                "Gastrocnemius"
        )));
        correctAnswers.add("Peroneus brevis");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Peroneus brevis (Correct answer)\n" +
                        "Everts the foot, turning the sole outward.\n\n" +
                        "Tibialis anterior (Incorrect)\n" +
                        "Inverts the foot.\n\n" +
                        "Soleus (Incorrect)\n" +
                        "Plantarflexes the foot.\n\n" +
                        "Gastrocnemius (Incorrect)\n" +
                        "Plantarflexes the foot."
        );

        questions.add("Which muscle flexes the trunk and compresses the abdomen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Erector spinae",
                "Rectus abdominis",
                "Quadratus lumborum",
                "Trapezius"
        )));
        correctAnswers.add("Rectus abdominis");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Rectus abdominis (Correct answer)\n" +
                        "Flexes the trunk and compresses the abdomen.\n\n" +
                        "Erector spinae (Incorrect)\n" +
                        "Extends the trunk.\n\n" +
                        "Quadratus lumborum (Incorrect)\n" +
                        "Laterally flexes the trunk.\n\n" +
                        "Trapezius (Incorrect)\n" +
                        "Moves the scapula."
        );

        questions.add("Which muscle elevates the scapula and extends the neck?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Latissimus dorsi",
                "Trapezius",
                "Pectoralis major",
                "Deltoid"
        )));
        correctAnswers.add("Trapezius");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Trapezius (Correct answer)\n" +
                        "Elevates the scapula and extends the neck.\n\n" +
                        "Latissimus dorsi (Incorrect)\n" +
                        "Moves the arm.\n\n" +
                        "Pectoralis major (Incorrect)\n" +
                        "Moves the arm.\n\n" +
                        "Deltoid (Incorrect)\n" +
                        "Moves the arm."
        );

        questions.add("Which muscle flexes the fingers and is deep to the flexor digitorum superficialis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Extensor digitorum",
                "Flexor digitorum profundus",
                "Abductor digiti minimi",
                "Extensor carpi radialis"
        )));
        correctAnswers.add("Flexor digitorum profundus");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Flexor digitorum profundus (Correct answer)\n" +
                        "Deep to the superficialis, flexes the fingers.\n\n" +
                        "Extensor digitorum (Incorrect)\n" +
                        "Extends the fingers.\n\n" +
                        "Abductor digiti minimi (Incorrect)\n" +
                        "Abducts the little finger.\n\n" +
                        "Extensor carpi radialis (Incorrect)\n" +
                        "Extends the wrist."
        );

        questions.add("Which muscle extends the hip and stabilizes the pelvis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gluteus maximus",
                "Iliopsoas",
                "Adductor magnus",
                "Rectus femoris"
        )));
        correctAnswers.add("Gluteus maximus");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Gluteus maximus (Correct answer)\n" +
                        "Extends the hip and stabilizes the pelvis during movement.\n\n" +
                        "Iliopsoas (Incorrect)\n" +
                        "Flexes the hip.\n\n" +
                        "Adductor magnus (Incorrect)\n" +
                        "Adducts the thigh.\n\n" +
                        "Rectus femoris (Incorrect)\n" +
                        "Flexes the hip and extends the knee."
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
        new AlertDialog.Builder(AdvanceChallengeMode6.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
