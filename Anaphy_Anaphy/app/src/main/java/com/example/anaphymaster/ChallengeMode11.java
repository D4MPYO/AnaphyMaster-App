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

public class ChallengeMode11 extends AppCompatActivity {

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

        setContentView(R.layout.challenge_mode11);

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
                Toast.makeText(ChallengeMode11.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode11.this)
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
            new AlertDialog.Builder(ChallengeMode11.this)
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
                new AlertDialog.Builder(ChallengeMode11.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode11.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Urinary System", correctAnswersCount, totalQuestions);


                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Urinary System");
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
                        Intent intent = new Intent(ChallengeMode11.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode11.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode11.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Urinary System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Urinary System");
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
        new AlertDialog.Builder(ChallengeMode11.this)
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

        questions.add("What is the primary function of the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To promote nutrient absorption",
                "To filter blood, remove waste products and excess water, and maintain fluid, electrolyte, and pH balance", // Correct answer
                "To produce digestive enzymes",
                "To store bile"
        )));
        correctAnswers.add("To filter blood, remove waste products and excess water, and maintain fluid, electrolyte, and pH balance");
        rationales.put(0,
                "RATIONALE:\n" +
                        "To filter blood, remove waste products and excess water, and maintain fluid, electrolyte, and pH balance (Correct answer)\n" +
                        "The urinary system’s main role is to filter blood and remove metabolic wastes, excess fluids, and maintain electrolyte and pH balance.\n\n" +
                        "To promote nutrient absorption (Incorrect)\n" +
                        "Nutrient absorption is primarily the role of the digestive system.\n\n" +
                        "To produce digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are produced in organs such as the pancreas and stomach.\n\n" +
                        "To store bile (Incorrect)\n" +
                        "Bile is produced by the liver and stored in the gallbladder, not by the urinary system."
        );

        questions.add("Which organ is primarily responsible for filtering blood in the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The bladder",
                "The ureter",
                "The kidney", // Correct answer
                "The urethra"
        )));
        correctAnswers.add("The kidney");
        rationales.put(1,
                "RATIONALE:\n" +
                        "The kidney (Correct answer)\n" +
                        "The kidneys are the main organs that filter blood using millions of nephrons.\n\n" +
                        "The bladder (Incorrect)\n" +
                        "The bladder stores urine.\n\n" +
                        "The ureter (Incorrect)\n" +
                        "The ureters transport urine from the kidneys to the bladder.\n\n" +
                        "The urethra (Incorrect)\n" +
                        "The urethra channels urine out of the body."
        );

        questions.add("What are nephrons?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Muscular tubes that transport urine",
                "The smallest functional filtering units within the kidney", // Correct answer
                "The storage compartments in the kidney",
                "Hormonal secretions that regulate blood pressure"
        )));
        correctAnswers.add("The smallest functional filtering units within the kidney");
        rationales.put(2,
                "RATIONALE:\n" +
                        "The smallest functional filtering units within the kidney (Correct answer)\n" +
                        "Nephrons are the microscopic functional units in the kidney that perform filtration and reabsorption.\n\n" +
                        "Muscular tubes that transport urine (Incorrect)\n" +
                        "Muscular tubes (ureters) transport urine.\n\n" +
                        "The storage compartments in the kidney (Incorrect)\n" +
                        "Nephrons do not store urine; they filter blood and form urine.\n\n" +
                        "Hormonal secretions that regulate blood pressure (Incorrect)\n" +
                        "Nephrons are not hormones; though the kidney does secrete hormones, nephrons are structural units."
        );

        questions.add("How do the kidneys remove metabolic waste from the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By directly absorbing nutrients",
                "Through a process of filtration, reabsorption, and secretion", // Correct answer
                "By mechanical churning",
                "By producing bile"
        )));
        correctAnswers.add("Through a process of filtration, reabsorption, and secretion");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Through a process of filtration, reabsorption, and secretion (Correct answer)\n" +
                        "The kidneys filter blood in the glomeruli, reabsorb needed substances, and secrete wastes to form urine.\n\n" +
                        "By directly absorbing nutrients (Incorrect)\n" +
                        "Absorption of nutrients is not a waste removal process in the kidney.\n\n" +
                        "By mechanical churning (Incorrect)\n" +
                        "Mechanical churning is a process in the stomach.\n\n" +
                        "By producing bile (Incorrect)\n" +
                        "Bile is produced by the liver and is unrelated to kidney waste removal."
        );

        questions.add("Which process in the kidney involves the retrieval of valuable substances from the filtrate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration",
                "Reabsorption", // Correct answer
                "Secretion",
                "Excretion"
        )));
        correctAnswers.add("Reabsorption");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Reabsorption (Correct answer)\n" +
                        "Reabsorption is the process by which needed substances (water, glucose, electrolytes) are reclaimed from the filtrate.\n\n" +
                        "Filtration (Incorrect)\n" +
                        "Filtration is the initial removal of blood plasma into the nephrons.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion is the addition of waste products into the filtrate.\n\n" +
                        "Excretion (Incorrect)\n" +
                        "Excretion refers to the final elimination of urine from the body."
        );

        questions.add("What is the end product of the kidney’s filtration process?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood plasma",
                "Bile",
                "Urine", // Correct answer
                "Digestive enzymes"
        )));
        correctAnswers.add("Urine");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Urine (Correct answer)\n" +
                        "After filtration, reabsorption, and secretion, the fluid that remains is urine.\n\n" +
                        "Blood plasma (Incorrect)\n" +
                        "Blood plasma is the fluid component of blood; during filtration, plasma is filtered, but the final product is urine.\n\n" +
                        "Bile (Incorrect)\n" +
                        "Bile is produced by the liver.\n\n" +
                        "Digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are produced by the pancreas and stomach, not the kidney."
        );

        questions.add("Which structure carries urine from the kidneys to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The urethra",
                "The nephron",
                "The ureter", // Correct answer
                "The renal artery"
        )));
        correctAnswers.add("The ureter");
        rationales.put(6,
                "RATIONALE:\n" +
                        "The ureter (Correct answer)\n" +
                        "The ureter is a narrow, muscular tube that transports urine from the kidney to the bladder using peristaltic waves.\n\n" +
                        "The urethra (Incorrect)\n" +
                        "The urethra carries urine from the bladder out of the body.\n\n" +
                        "The nephron (Incorrect)\n" +
                        "A nephron is the kidney’s filtration unit.\n\n" +
                        "The renal artery (Incorrect)\n" +
                        "The renal artery supplies blood to the kidneys; it does not carry urine."
        );

        questions.add("What mechanism do ureters use to move urine from the kidneys to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gravity alone",
                "Active diffusion across membranes",
                "Peristaltic waves", // Correct answer
                "Contraction of the bladder walls"
        )));
        correctAnswers.add("Peristaltic waves");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Peristaltic waves (Correct answer)\n" +
                        "Ureters use peristaltic waves—rhythmic muscle contractions—to propel urine downward.\n\n" +
                        "Gravity alone (Incorrect)\n" +
                        "Although gravity may assist, the primary mechanism is muscular contraction.\n\n" +
                        "Active diffusion across membranes (Incorrect)\n" +
                        "Urine movement is not by diffusion but by muscular activity.\n\n" +
                        "Contraction of the bladder walls (Incorrect)\n" +
                        "Bladder contractions are used during urination, not in ureteral transport."
        );

        questions.add("What is the primary function of the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To filter blood",
                "To chemically digest food",
                "To store urine temporarily", // Correct answer
                "To produce hormones"
        )));
        correctAnswers.add("To store urine temporarily");
        rationales.put(8,
                "RATIONALE:\n" +
                        "To store urine temporarily (Correct answer)\n" +
                        "The bladder stores urine until it is full and then signals the need to void.\n\n" +
                        "To filter blood (Incorrect)\n" +
                        "Filtering blood occurs in the kidneys.\n\n" +
                        "To chemically digest food (Incorrect)\n" +
                        "Chemical digestion is carried out by the digestive organs.\n\n" +
                        "To produce hormones (Incorrect)\n" +
                        "While the bladder has stretch receptors, it does not produce hormones."
        );

        questions.add("Which feature of the bladder signals the brain when it is full?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The presence of nephrons",
                "Stretch receptors in the bladder wall", // Correct answer
                "The secretion of digestive enzymes",
                "The linear arrangement of ureters"
        )));
        correctAnswers.add("Stretch receptors in the bladder wall");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Stretch receptors in the bladder wall (Correct answer)\n" +
                        "Stretch receptors in the bladder wall detect fullness and send signals to the brain to trigger the urge to urinate.\n\n" +
                        "The presence of nephrons (Incorrect)\n" +
                        "Nephrons are part of the kidney, not the bladder.\n\n" +
                        "The secretion of digestive enzymes (Incorrect)\n" +
                        "The bladder does not secrete digestive enzymes.\n\n" +
                        "The linear arrangement of ureters (Incorrect)\n" +
                        "Ureters are tubes that transport urine, not sensory organs."
        );

        questions.add("What is the function of the urethra?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To filter blood",
                "To transport urine from the bladder out of the body", // Correct answer
                "To store urine",
                "To produce urine"
        )));
        correctAnswers.add("To transport urine from the bladder out of the body");
        rationales.put(10,
                "RATIONALE:\n" +
                        "To transport urine from the bladder out of the body (Correct answer)\n" +
                        "The urethra is the channel through which urine exits the body.\n\n" +
                        "To filter blood (Incorrect)\n" +
                        "Blood filtration occurs in the kidneys.\n\n" +
                        "To store urine (Incorrect)\n" +
                        "Urine storage is the function of the bladder.\n\n" +
                        "To produce urine (Incorrect)\n" +
                        "Urine production is carried out by the kidneys."
        );

        questions.add("How does the urethra differ between males and females?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is the same in both sexes",
                "It is longer in males and shorter in females", // Correct answer
                "It stores urine in females but not in males",
                "It filters blood in males but not in females"
        )));
        correctAnswers.add("It is longer in males and shorter in females");
        rationales.put(11,
                "RATIONALE:\n" +
                        "It is longer in males and shorter in females (Correct answer)\n" +
                        "In males, the urethra is longer (traversing the penis), while in females, it is shorter and opens just above the vaginal opening.\n\n" +
                        "It is the same in both sexes (Incorrect)\n" +
                        "There is a distinct anatomical difference.\n\n" +
                        "It stores urine in females but not in males (Incorrect)\n" +
                        "Neither sex uses the urethra to store urine.\n\n" +
                        "It filters blood in males but not in females (Incorrect)\n" +
                        "The urethra does not filter blood in either sex."
        );

        questions.add("What is the main process by which the kidneys filter blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digestion",
                "Glomerular filtration", // Correct answer
                "Bile secretion",
                "Muscle contraction"
        )));
        correctAnswers.add("Glomerular filtration");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Glomerular filtration (Correct answer)\n" +
                        "Glomerular filtration is the process by which blood is filtered in the kidneys’ nephrons.\n\n" +
                        "Digestion (Incorrect)\n" +
                        "Digestion is a process of the gastrointestinal system.\n\n" +
                        "Bile secretion (Incorrect)\n" +
                        "Bile secretion is a function of the liver.\n\n" +
                        "Muscle contraction (Incorrect)\n" +
                        "Muscle contraction is not involved in blood filtration."
        );

        questions.add("Which of the following best describes “reabsorption” in the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The process by which urine is excreted",
                "The retrieval of essential nutrients and water from the filtrate", // Correct answer
                "The production of urine from bile",
                "The contraction of ureteral muscles"
        )));
        correctAnswers.add("The retrieval of essential nutrients and water from the filtrate");
        rationales.put(13,
                "RATIONALE:\n" +
                        "The retrieval of essential nutrients and water from the filtrate (Correct answer)\n" +
                        "Reabsorption is the process in which necessary substances (water, glucose, electrolytes) are reclaimed from the filtrate into the bloodstream.\n\n" +
                        "The process by which urine is excreted (Incorrect)\n" +
                        "Excretion is when urine is eliminated from the body.\n\n" +
                        "The production of urine from bile (Incorrect)\n" +
                        "Urine is not produced from bile.\n\n" +
                        "The contraction of ureteral muscles (Incorrect)\n" +
                        "While ureteral muscles contract for propulsion, that is not “reabsorption.”"
        );

        questions.add("What does “secretion” mean with respect to kidney function?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The addition of additional waste products into the filtrate", // Correct answer
                "The collection of urine in the bladder",
                "The removal of blood from the kidney",
                "The absorption of water by the nephrons"
        )));
        correctAnswers.add("The addition of additional waste products into the filtrate");
        rationales.put(14,
                "RATIONALE:\n" +
                        "The addition of additional waste products into the filtrate (Correct answer)\n" +
                        "Secretion in the kidneys involves actively transporting additional waste substances from blood into the nephron tubule.\n\n" +
                        "The collection of urine in the bladder (Incorrect)\n" +
                        "Collection of urine occurs in the bladder, not secretion.\n\n" +
                        "The removal of blood from the kidney (Incorrect)\n" +
                        "Removing blood is not termed secretion.\n\n" +
                        "The absorption of water by the nephrons (Incorrect)\n" +
                        "Absorption of water is called reabsorption, not secretion."
        );

        questions.add("What are the main components of urine, as formed by the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bile, digestive enzymes, and hormones",
                "Metabolic wastes (like urea), salts, and water", // Correct answer
                "Nutrients, proteins, and vitamins",
                "Blood cells, plasma, and platelets"
        )));
        correctAnswers.add("Metabolic wastes (like urea), salts, and water");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Metabolic wastes (like urea), salts, and water (Correct answer)\n" +
                        "Urine is composed primarily of metabolic wastes (such as urea), excess salts, and water.\n\n" +
                        "Bile, digestive enzymes, and hormones (Incorrect)\n" +
                        "Bile and digestive enzymes are not found in urine.\n\n" +
                        "Nutrients, proteins, and vitamins (Incorrect)\n" +
                        "Nutrients and proteins are reabsorbed and not excreted in urine (under normal conditions).\n\n" +
                        "Blood cells, plasma, and platelets (Incorrect)\n" +
                        "Blood cells and other blood components are not normally excreted as urine."
        );

        questions.add("How does the urinary system contribute to fluid and electrolyte balance?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By filtering and then excreting excess water and electrolytes", // Correct answer
                "By storing digestive juices",
                "By promoting nutrient absorption",
                "By producing bile that neutralizes acids"
        )));
        correctAnswers.add("By filtering and then excreting excess water and electrolytes");
        rationales.put(16,
                "RATIONALE:\n" +
                        "By filtering and then excreting excess water and electrolytes (Correct answer)\n" +
                        "The kidneys precisely regulate fluid and electrolyte levels through reabsorption and excretion.\n\n" +
                        "By storing digestive juices (Incorrect)\n" +
                        "Storing digestive juices is not a function of the urinary system.\n\n" +
                        "By promoting nutrient absorption (Incorrect)\n" +
                        "Nutrient absorption is performed largely by the digestive system.\n\n" +
                        "By producing bile that neutralizes acids (Incorrect)\n" +
                        "Bile neutralizes fats in the intestine; it is not involved in fluid balance."
        );

        questions.add("Which statement best describes the relationship between the urinary and digestive systems in regard to waste exchange?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The digestive system produces urine, which the urinary system stores",
                "The digestive system absorbs nutrients and simultaneously supplies the blood with metabolic wastes that the kidneys filter", // Correct answer
                "The urinary system and digestive system function completely independently",
                "The urinary system transports bile from the liver to the digestive tract"
        )));
        correctAnswers.add("The digestive system absorbs nutrients and simultaneously supplies the blood with metabolic wastes that the kidneys filter");
        rationales.put(17,
                "RATIONALE:\n" +
                        "The digestive system absorbs nutrients and simultaneously supplies the blood with metabolic wastes that the kidneys filter (Correct answer)\n" +
                        "The digestive system breaks down food and absorbs nutrients, while waste products (e.g., urea) circulate in the blood for the kidneys to filter.\n\n" +
                        "The digestive system produces urine, which the urinary system stores (Incorrect)\n" +
                        "Urine is produced by the kidneys, not the digestive system.\n\n" +
                        "The urinary system and digestive system function completely independently (Incorrect)\n" +
                        "The systems are functionally interconnected.\n\n" +
                        "The urinary system transports bile from the liver to the digestive tract (Incorrect)\n" +
                        "Bile transport is handled by the biliary system, not the urinary system."
        );

        questions.add("In what way do the kidneys act as the body’s “chemical processing plant”?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They mechanically break down food particles",
                "They filter the blood, removing metabolites and waste products from digestion and metabolism", // Correct answer
                "They produce digestive enzymes",
                "They store vitamins"
        )));
        correctAnswers.add("They filter the blood, removing metabolites and waste products from digestion and metabolism");
        rationales.put(18,
                "RATIONALE:\n" +
                        "They filter the blood, removing metabolites and waste products from digestion and metabolism (Correct answer)\n" +
                        "The kidneys “process” blood, removing toxic byproducts (like urea) and maintaining chemical balance.\n\n" +
                        "They mechanically break down food particles (Incorrect)\n" +
                        "Mechanical breakdown occurs in the digestive tract.\n\n" +
                        "They produce digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are not produced by the kidneys.\n\n" +
                        "They store vitamins (Incorrect)\n" +
                        "Although the kidneys reabsorb nutrients, they do not store vitamins."
        );

        questions.add("Which process ensures that urine is conveyed from the kidneys to the bladder despite gravity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diffusion",
                "Active transport",
                "Peristalsis of the ureters", // Correct answer
                "Bladder expansion"
        )));
        correctAnswers.add("Peristalsis of the ureters");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Peristalsis of the ureters (Correct answer)\n" +
                        "The ureters employ peristaltic contractions to push urine downward into the bladder.\n\n" +
                        "Diffusion (Incorrect)\n" +
                        "Diffusion does not propel fluids over distances in tubular structures.\n\n" +
                        "Active transport (Incorrect)\n" +
                        "Active transport is used at the cellular level, not for bulk movement of urine.\n\n" +
                        "Bladder expansion (Incorrect)\n" +
                        "Bladder expansion stores urine but does not actively move it from the kidneys."
        );

        questions.add("What triggers the need to void urine from the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "A decrease in blood pressure",
                "Stretch receptors in the bladder sensing fullness", // Correct answer
                "The filtration function of the kidneys",
                "Secretion of digestive enzymes"
        )));
        correctAnswers.add("Stretch receptors in the bladder sensing fullness");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Stretch receptors in the bladder sensing fullness (Correct answer)\n" +
                        "Stretch receptors in the bladder wall send signals to the brain when the bladder is full.\n\n" +
                        "A decrease in blood pressure (Incorrect)\n" +
                        "Blood pressure changes do not directly trigger urination.\n\n" +
                        "The filtration function of the kidneys (Incorrect)\n" +
                        "Kidney filtration is not the direct signal for bladder emptying.\n\n" +
                        "Secretion of digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are unrelated to urination."
        );

        questions.add("Which of the following is a major waste product removed by the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glucose",
                "Urea", // Correct answer
                "Bile acids",
                "Amino acids"
        )));
        correctAnswers.add("Urea");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Urea (Correct answer)\n" +
                        "Urea, a byproduct of protein metabolism, is a primary waste product filtered from the blood.\n\n" +
                        "Glucose (Incorrect)\n" +
                        "Glucose is usually reabsorbed and not excreted in significant amounts.\n\n" +
                        "Bile acids (Incorrect)\n" +
                        "Bile acids are produced in the liver and used in digestion, not excreted by the kidneys.\n\n" +
                        "Amino acids (Incorrect)\n" +
                        "Amino acids are largely reabsorbed if needed."
        );

        questions.add("Which structure in the kidney is directly involved in the initial filtration of blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ureter",
                "Bladder",
                "Glomerulus", // Correct answer
                "Renal pelvis"
        )));
        correctAnswers.add("Glomerulus");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Glomerulus (Correct answer)\n" +
                        "The glomerulus is a network of capillaries where blood filtration begins in each nephron.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "The ureter transports urine; it does not filter blood.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "The bladder stores urine.\n\n" +
                        "Renal pelvis (Incorrect)\n" +
                        "The renal pelvis collects urine after filtration and reabsorption."
        );

        questions.add("What is the role of the loop of Henle in the nephron?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It actively secretes bile into the urine.",
                "It is responsible for the majority of solute reabsorption and concentration of urine.", // Correct answer
                "It serves as the primary site of blood filtration.",
                "It transports urine from the kidney to the ureter."
        )));
        correctAnswers.add("It is responsible for the majority of solute reabsorption and concentration of urine.");
        rationales.put(23,
                "RATIONALE:\n" +
                        "It is responsible for the majority of solute reabsorption and concentration of urine. (Correct answer)\n" +
                        "The loop of Henle creates a concentration gradient that allows for water reabsorption and urine concentration.\n\n" +
                        "It actively secretes bile into the urine. (Incorrect)\n" +
                        "Bile secretion is not a function of the nephron.\n\n" +
                        "It serves as the primary site of blood filtration. (Incorrect)\n" +
                        "Filtration occurs in the glomerulus.\n\n" +
                        "It transports urine from the kidney to the ureter. (Incorrect)\n" +
                        "Urine transport from kidney to ureter is done by the ureter, not by the loop of Henle."
        );

        questions.add("How do the kidneys maintain acid–base homeostasis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By secreting digestive enzymes",
                "By reabsorbing or excreting bicarbonate and hydrogen ions", // Correct answer
                "By storing urine in the bladder",
                "By producing renin exclusively"
        )));
        correctAnswers.add("By reabsorbing or excreting bicarbonate and hydrogen ions");
        rationales.put(24,
                "RATIONALE:\n" +
                        "By reabsorbing or excreting bicarbonate and hydrogen ions (Correct answer)\n" +
                        "The kidneys regulate pH by selectively reabsorbing bicarbonate and excreting hydrogen ions.\n\n" +
                        "By secreting digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are produced by other organs.\n\n" +
                        "By storing urine in the bladder (Incorrect)\n" +
                        "Urine storage does not regulate pH.\n\n" +
                        "By producing renin exclusively (Incorrect)\n" +
                        "Although renin affects blood pressure, acid–base balance is managed by bicarbonate and proton exchange."
        );

        questions.add("Which hormone secreted by the kidney plays a role in blood pressure regulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Aldosterone",
                "Renin", // Correct answer
                "Glucagon"
        )));
        correctAnswers.add("Renin");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Renin (Correct answer)\n" +
                        "Renin is produced by the kidney's juxtaglomerular cells. It initiates the renin–angiotensin–aldosterone system, causing angiotensin II production—a potent vasoconstrictor—and stimulating aldosterone release, both of which work together to raise blood pressure.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is secreted by the pancreas and primarily regulates blood glucose levels, not blood pressure.\n\n" +
                        "Aldosterone (Incorrect)\n" +
                        "Aldosterone is secreted by the adrenal glands. Although it plays a role in regulating electrolyte balance (and thereby indirectly affects blood pressure), it is not produced by the kidney.\n\n" +
                        "Glucagon (Incorrect)\n" +
                        "Glucagon is secreted by the pancreas and primarily functions to increase blood sugar levels by promoting glycogenolysis and gluconeogenesis. It is not involved in blood pressure regulation."
        );

        questions.add("Which sequence correctly represents the steps in urine formation in the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration → Reabsorption → Secretion → Excretion", // Correct answer
                "Secretion → Filtration → Reabsorption → Excretion",
                "Reabsorption → Filtration → Secretion → Excretion",
                "Filtration → Secretion → Reabsorption → Excretion"
        )));
        correctAnswers.add("Filtration → Reabsorption → Secretion → Excretion");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Filtration → Reabsorption → Secretion → Excretion (Correct answer)\n" +
                        "Urine formation begins with filtration (blood plasma is filtered through the glomerulus), then valuable substances are reabsorbed, additional wastes are secreted, and finally, the remaining fluid is excreted.\n\n" +
                        "Secretion → Filtration → Reabsorption → Excretion (Incorrect)\n" +
                        "The steps must occur in the correct order: filtration first, reabsorption next, followed by secretion, and then excretion.\n\n" +
                        "Reabsorption → Filtration → Secretion → Excretion (Incorrect)\n" +
                        "Reabsorption does not occur before filtration.\n\n" +
                        "Filtration → Secretion → Reabsorption → Excretion (Incorrect)\n" +
                        "Secretion happens after reabsorption, not before."
        );

        questions.add("Which of the following structures uses peristaltic waves to move urine from the kidneys to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidneys",
                "Bladder",
                "Ureters", // Correct answer
                "Urethra"
        )));
        correctAnswers.add("Ureters");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Ureters (Correct answer)\n" +
                        "The ureters are narrow muscular tubes that use wave‐like contractions (peristalsis) to move urine downward.\n\n" +
                        "Kidneys (Incorrect)\n" +
                        "The kidneys filter blood but do not transport urine by peristalsis.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "The bladder stores urine but does not rely on peristalsis for transport.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra expels urine; it does not generate the peristaltic motion."
        );

        questions.add("How do the kidneys help maintain acid–base balance in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By secreting digestive enzymes",
                "By reabsorbing bicarbonate and excreting hydrogen ions", // Correct answer
                "By filtering bile acids",
                "By absorbing nutrients"
        )));
        correctAnswers.add("By reabsorbing bicarbonate and excreting hydrogen ions");
        rationales.put(28,
                "RATIONALE:\n" +
                        "By reabsorbing bicarbonate and excreting hydrogen ions (Correct answer)\n" +
                        "The kidneys adjust pH by selectively reabsorbing bicarbonate and secreting hydrogen ions.\n\n" +
                        "By secreting digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are not involved in pH regulation.\n\n" +
                        "By filtering bile acids (Incorrect)\n" +
                        "Bile acids are not used for acid–base regulation.\n\n" +
                        "By absorbing nutrients (Incorrect)\n" +
                        "Nutrient absorption occurs in the intestines."
        );

        questions.add("What is the role of stretch receptors in the bladder wall?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To signal the kidney to increase filtration",
                "To trigger contraction of the urethra",
                "To signal the brain that the bladder is full", // Correct answer
                "To initiate peristalsis in the ureters"
        )));
        correctAnswers.add("To signal the brain that the bladder is full");
        rationales.put(29,
                "RATIONALE:\n" +
                        "To signal the brain that the bladder is full (Correct answer)\n" +
                        "As the bladder fills, stretch receptors send signals to the brain, prompting the urge to void.\n\n" +
                        "To signal the kidney to increase filtration (Incorrect)\n" +
                        "The kidneys’ function is independent of bladder stretch receptors.\n\n" +
                        "To trigger contraction of the urethra (Incorrect)\n" +
                        "The urethra does not contract based on stretch receptor input.\n\n" +
                        "To initiate peristalsis in the ureters (Incorrect)\n" +
                        "Peristalsis in the ureters is not controlled by bladder stretch receptors."
        );

        questions.add("Which statement best describes the function of the urethra?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It stores urine until it is ready to be defecated",
                "It transports urine from the bladder out of the body", // Correct answer
                "It filters blood before urine formation",
                "It absorbs water to concentrate urine"
        )));
        correctAnswers.add("It transports urine from the bladder out of the body");
        rationales.put(30,
                "RATIONALE:\n" +
                        "It transports urine from the bladder out of the body (Correct answer)\n" +
                        "The urethra is the conduit which expels urine out of the body during micturition (urination).\n\n" +
                        "It stores urine until it is ready to be defecated (Incorrect)\n" +
                        "Urine storage occurs in the bladder, not the urethra.\n\n" +
                        "It filters blood before urine formation (Incorrect)\n" +
                        "Blood filtration takes place in the kidneys’ nephrons.\n\n" +
                        "It absorbs water to concentrate urine (Incorrect)\n" +
                        "The urethra is not involved in water reabsorption."
        );

        questions.add("If digestion is inefficient and produces an increased toxic load, which system’s workload is most likely to increase as a result?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The reproductive system",
                "The urinary system", // Correct answer
                "The respiratory system",
                "The skeletal system"
        )));
        correctAnswers.add("The urinary system");
        rationales.put(31,
                "RATIONALE:\n" +
                        "The urinary system (Correct answer)\n" +
                        "Inefficient digestion can lead to higher levels of metabolic wastes (e.g., urea) in the blood, thereby increasing the filtering burden on the kidneys.\n\n" +
                        "The reproductive system (Incorrect)\n" +
                        "These systems are not primarily responsible for waste clearance from the blood.\n\n" +
                        "The respiratory system (Incorrect)\n" +
                        "These systems are not primarily responsible for waste clearance from the blood.\n\n" +
                        "The skeletal system (Incorrect)\n" +
                        "These systems are not primarily responsible for waste clearance from the blood."
        );

        questions.add("Which of the following best describes one connection between the urinary and digestive systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The urinary system directly absorbs nutrients from the intestines.",
                "The digestive system produces metabolic by‐products (such as urea) that are filtered from the blood by the kidneys.", // Correct answer
                "The urinary system stores food until it is ready to be digested.",
                "The digestive system transports urine from the kidneys to the bladder."
        )));
        correctAnswers.add("The digestive system produces metabolic by‐products (such as urea) that are filtered from the blood by the kidneys.");
        rationales.put(32,
                "RATIONALE:\n" +
                        "The digestive system produces metabolic by‐products (such as urea) that are filtered from the blood by the kidneys. (Correct answer)\n" +
                        "The digestive process produces wastes (e.g., urea from protein metabolism) that enter the bloodstream and are subsequently filtered by the kidneys.\n\n" +
                        "The urinary system directly absorbs nutrients from the intestines. (Incorrect)\n" +
                        "These options do not accurately describe how the systems interact.\n\n" +
                        "The urinary system stores food until it is ready to be digested. (Incorrect)\n" +
                        "These options do not accurately describe how the systems interact.\n\n" +
                        "The digestive system transports urine from the kidneys to the bladder. (Incorrect)\n" +
                        "These options do not accurately describe how the systems interact."
        );

        questions.add("Which of the following helps maintain electrolyte balance in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The kidneys through reabsorption and secretion", // Correct answer
                "The bladder by storing urine",
                "The urethra by propelling urine",
                "The ureters through peristalsis"
        )));
        correctAnswers.add("The kidneys through reabsorption and secretion");
        rationales.put(33,
                "RATIONALE:\n" +
                        "The kidneys through reabsorption and secretion (Correct answer)\n" +
                        "The kidneys regulate electrolyte balance by reabsorbing needed ions and secreting excess ones.\n\n" +
                        "The bladder by storing urine (Incorrect)\n" +
                        "While these structures move or store urine, they do not regulate electrolyte levels.\n\n" +
                        "The urethra by propelling urine (Incorrect)\n" +
                        "While these structures move or store urine, they do not regulate electrolyte levels.\n\n" +
                        "The ureters through peristalsis (Incorrect)\n" +
                        "While these structures move or store urine, they do not regulate electrolyte levels."
        );

        questions.add("Which of the following is NOT a function of the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtering blood",
                "Removing metabolic wastes",
                "Regulating electrolyte balance",
                "Producing bile" // Correct answer
        )));
        correctAnswers.add("Producing bile");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Producing bile (Correct answer)\n" +
                        "Correct answer because bile production is a function of the liver, not the kidneys.\n\n" +
                        "Filtering blood (Incorrect)\n" +
                        "Correct kidney functions.\n\n" +
                        "Removing metabolic wastes (Incorrect)\n" +
                        "Correct kidney functions.\n\n" +
                        "Regulating electrolyte balance (Incorrect)\n" +
                        "Correct kidney functions."
        );

        questions.add("What is urea in the context of the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "A nutrient reabsorbed by the kidneys",
                "A primary metabolic waste product filtered by the kidneys", // Correct answer
                "A hormone secreted to regulate blood pressure",
                "An enzyme that catalyzes blood filtration"
        )));
        correctAnswers.add("A primary metabolic waste product filtered by the kidneys");
        rationales.put(35,
                "RATIONALE:\n" +
                        "A primary metabolic waste product filtered by the kidneys (Correct answer)\n" +
                        "Urea is formed as a byproduct of protein metabolism and is excreted in urine.\n\n" +
                        "A nutrient reabsorbed by the kidneys (Incorrect)\n" +
                        "Urea is not a nutrient but a waste product.\n\n" +
                        "A hormone secreted to regulate blood pressure (Incorrect)\n" +
                        "Urea does not regulate blood pressure.\n\n" +
                        "An enzyme that catalyzes blood filtration (Incorrect)\n" +
                        "Urea is not an enzyme."
        );

        questions.add("Approximately how many nephrons does each kidney contain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hundreds",
                "Thousands",
                "Millions", // Correct answer
                "Billions"
        )));
        correctAnswers.add("Millions");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Millions (Correct answer)\n" +
                        "Each kidney contains millions of nephrons—the tiny filtering units that perform blood filtration and urine formation.\n\n" +
                        "Hundreds (Incorrect)\n" +
                        "These numbers do not accurately reflect the known quantity.\n\n" +
                        "Thousands (Incorrect)\n" +
                        "These numbers do not accurately reflect the known quantity.\n\n" +
                        "Billions (Incorrect)\n" +
                        "These numbers do not accurately reflect the known quantity."
        );

        questions.add("Which structure uses peristalsis specifically to transport urine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidneys",
                "Ureters", // Correct answer
                "Bladder",
                "Urethra"
        )));
        correctAnswers.add("Ureters");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Ureters (Correct answer)\n" +
                        "Ureters are muscular tubes that use peristaltic waves to propel urine from the kidneys to the bladder.\n\n" +
                        "Kidneys (Incorrect)\n" +
                        "Although renal blood flow is dynamic, peristalsis is not used here.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "The bladder stores urine and the urethra expels urine, but neither uses peristalsis for transport.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The bladder stores urine and the urethra expels urine, but neither uses peristalsis for transport."
        );

        questions.add("What is the final pathway for urine to exit the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ureter",
                "Bladder",
                "Urethra", // Correct answer
                "Glomerulus"
        )));
        correctAnswers.add("Urethra");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Urethra (Correct answer)\n" +
                        "The urethra is the tube through which urine is expelled from the body.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "Ureters transport urine from the kidneys to the bladder.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "The bladder stores urine temporarily.\n\n" +
                        "Glomerulus (Incorrect)\n" +
                        "The glomerulus is the site of blood filtration."
        );

        questions.add("What role do the kidneys play in regulating blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They secrete insulin to lower blood sugar levels.",
                "They release renin to regulate fluid balance and blood pressure.", // Correct answer
                "They filter out excess glucose from the blood.",
                "They produce red blood cells to increase blood volume."
        )));
        correctAnswers.add("They release renin to regulate fluid balance and blood pressure.");
        rationales.put(39,
                "RATIONALE:\n" +
                        "They release renin to regulate fluid balance and blood pressure. (Correct answer)\n" +
                        "The kidneys release renin, which is part of the renin-angiotensin-aldosterone system (RAAS), a key mechanism in regulating blood pressure and fluid balance.\n\n" +
                        "They secrete insulin to lower blood sugar levels. (Incorrect)\n" +
                        "Insulin is produced by the pancreas and helps regulate blood sugar, not blood pressure.\n\n" +
                        "They filter out excess glucose from the blood. (Incorrect)\n" +
                        "Filtering glucose is a function of the kidneys, but not directly related to blood pressure regulation.\n\n" +
                        "They produce red blood cells to increase blood volume. (Incorrect)\n" +
                        "Red blood cell production is regulated by the bone marrow, not the kidneys."
        );

        questions.add("Which statement best describes the bladder’s main function?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To filter blood",
                "To store urine until it is excreted", // Correct answer
                "To transport urine from the kidneys to the bladder",
                "To produce metabolic wastes"
        )));
        correctAnswers.add("To store urine until it is excreted");
        rationales.put(40,
                "RATIONALE:\n" +
                        "To store urine until it is excreted (Correct answer)\n" +
                        "The bladder is a hollow, expandable organ that temporarily stores urine.\n\n" +
                        "To filter blood (Incorrect)\n" +
                        "Filtration occurs in the kidneys.\n\n" +
                        "To transport urine from the kidneys to the bladder (Incorrect)\n" +
                        "Urine is transported by the ureters, not stored by them.\n\n" +
                        "To produce metabolic wastes (Incorrect)\n" +
                        "Metabolic waste is produced by the body and filtered by the kidneys."
        );

        questions.add("Which mechanism best explains how urine is transported from the kidneys to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diffusion",
                "Peristalsis", // Correct answer
                "Osmosis",
                "Active transport"
        )));
        correctAnswers.add("Peristalsis");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Peristalsis (Correct answer)\n" +
                        "Peristaltic contractions of the ureteral smooth muscle propel urine from the kidneys to the bladder.\n\n" +
                        "Diffusion (Incorrect)\n" +
                        "Diffusion is not the primary mechanism for moving urine along a tubular structure.\n\n" +
                        "Osmosis (Incorrect)\n" +
                        "This mechanism does not account for the bulk movement of urine in the ureters.\n\n" +
                        "Active transport (Incorrect)\n" +
                        "This mechanism does not account for the bulk movement of urine in the ureters."
        );

        questions.add("After blood filtration in the kidneys, what happens to water and valuable solutes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They remain in the filtrate and are excreted",
                "They are reabsorbed back into the bloodstream", // Correct answer
                "They are stored within the kidney",
                "They convert into bile"
        )));
        correctAnswers.add("They are reabsorbed back into the bloodstream");
        rationales.put(42,
                "RATIONALE:\n" +
                        "They are reabsorbed back into the bloodstream (Correct answer)\n" +
                        "Reabsorption returns essential water and solutes from the filtrate to the bloodstream.\n\n" +
                        "They remain in the filtrate and are excreted (Incorrect)\n" +
                        "Excreting these would waste needed substances.\n\n" +
                        "They are stored within the kidney (Incorrect)\n" +
                        "They are not stored in the kidney.\n\n" +
                        "They convert into bile (Incorrect)\n" +
                        "They are not converted into bile."
        );

        questions.add("Which of the following is NOT an effect of kidney function on blood composition?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Removal of metabolic wastes",
                "Maintenance of fluid balance",
                "Regulation of pH",
                "Production of digestive enzymes" // Correct answer
        )));
        correctAnswers.add("Production of digestive enzymes");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Production of digestive enzymes (Correct answer)\n" +
                        "Production of digestive enzymes is not a function of the urinary system.\n\n" +
                        "Removal of metabolic wastes (Incorrect)\n" +
                        "A correct function of the kidneys.\n\n" +
                        "Maintenance of fluid balance (Incorrect)\n" +
                        "A correct function of the kidneys.\n\n" +
                        "Regulation of pH (Incorrect)\n" +
                        "A correct function of the kidneys."
        );

        questions.add("How do the kidneys assist in regulating blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By filtering out blood cells",
                "By releasing renin as part of the renin–angiotensin system", // Correct answer
                "By storing urine in the bladder",
                "By increasing peristalsis in the ureters"
        )));
        correctAnswers.add("By releasing renin as part of the renin–angiotensin system");
        rationales.put(44,
                "RATIONALE:\n" +
                        "By releasing renin as part of the renin–angiotensin system (Correct answer)\n" +
                        "The kidneys release renin, which helps regulate blood pressure through the renin–angiotensin system.\n\n" +
                        "By filtering out blood cells (Incorrect)\n" +
                        "Not involved in blood pressure regulation.\n\n" +
                        "By storing urine in the bladder (Incorrect)\n" +
                        "Not related to blood pressure regulation.\n\n" +
                        "By increasing peristalsis in the ureters (Incorrect)\n" +
                        "Not involved in blood pressure regulation."
        );

        questions.add("What is one potential consequence if the urinary system must filter an increased toxic load from the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "A decrease in blood filtration",
                "Increased stress on the kidneys", // Correct answer
                "Lower urine output",
                "Enhanced nutrient absorption"
        )));
        correctAnswers.add("Increased stress on the kidneys");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Increased stress on the kidneys (Correct answer)\n" +
                        "An increased toxic load requires the kidneys to work harder.\n\n" +
                        "A decrease in blood filtration (Incorrect)\n" +
                        "The filtration rate may actually increase to handle excess waste.\n\n" +
                        "Lower urine output (Incorrect)\n" +
                        "Not the typical response to increased waste load.\n\n" +
                        "Enhanced nutrient absorption (Incorrect)\n" +
                        "Not the typical response to increased waste load."
        );

        questions.add("Which substance generated as a result of protein metabolism is filtered by the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bile",
                "Urea", // Correct answer
                "Glucose",
                "Insulin"
        )));
        correctAnswers.add("Urea");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Urea (Correct answer)\n" +
                        "Urea is a byproduct of protein metabolism and is filtered from the blood by the kidneys.\n\n" +
                        "Bile (Incorrect)\n" +
                        "Bile is produced by the liver.\n\n" +
                        "Glucose (Incorrect)\n" +
                        "Glucose is reabsorbed under normal conditions.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is secreted by the pancreas."
        );

        questions.add("Which description best defines the process of secretion in the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The filtering of blood in the glomerulus",
                "The active transport of wastes into the nephron tubules", // Correct answer
                "The movement of urine through the urethra",
                "The contraction of bladder muscles during urination"
        )));
        correctAnswers.add("The active transport of wastes into the nephron tubules");
        rationales.put(47,
                "RATIONALE:\n" +
                        "The active transport of wastes into the nephron tubules (Correct answer)\n" +
                        "Secretion adds additional wastes from the blood into the filtrate, enhancing urine formation.\n\n" +
                        "The filtering of blood in the glomerulus (Incorrect)\n" +
                        "That is the process of filtration.\n\n" +
                        "The movement of urine through the urethra (Incorrect)\n" +
                        "Part of urine excretion, not secretion.\n\n" +
                        "The contraction of bladder muscles during urination (Incorrect)\n" +
                        "Part of excretion, not secretion."
        );

        questions.add("Which structure is primarily responsible for storing urine until excretion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidney",
                "Ureter",
                "Bladder", // Correct answer
                "Urethra"
        )));
        correctAnswers.add("Bladder");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Bladder (Correct answer)\n" +
                        "The bladder is designed to store urine until the body is ready to void.\n\n" +
                        "Kidney (Incorrect)\n" +
                        "The kidneys filter blood.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "Ureters transport urine.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra expels urine from the body."
        );

        questions.add("How are the urinary and digestive systems anatomically or embryologically related?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They develop from the identical precursor tissue.",
                "They are completely unrelated in development.",
                "They develop in close proximity, which can lead to interrelated disorders.", // Correct answer
                "They merge completely during adulthood."
        )));
        correctAnswers.add("They develop in close proximity, which can lead to interrelated disorders.");
        rationales.put(49,
                "RATIONALE:\n" +
                        "They develop in close proximity, which can lead to interrelated disorders. (Correct answer)\n" +
                        "Their close developmental and anatomical proximity can mean that disorders in one system may affect the other.\n\n" +
                        "They develop from the identical precursor tissue. (Incorrect)\n" +
                        "They arise from different precursors but are anatomically close.\n\n" +
                        "They are completely unrelated in development. (Incorrect)\n" +
                        "They are not completely unrelated due to their pelvic proximity.\n\n" +
                        "They merge completely during adulthood. (Incorrect)\n" +
                        "The systems remain distinct throughout life."
        );

        questions.add("Which function is common to both the urinary and digestive systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Both absorb nutrients efficiently",
                "Both contribute to overall homeostasis through waste elimination and fluid balance",
                "Both mechanically break down food",
                "Both produce bile"
        )));
        correctAnswers.add("Both contribute to overall homeostasis through waste elimination and fluid balance");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Both contribute to overall homeostasis through waste elimination and fluid balance (Correct answer)\n" +
                        "Both systems help maintain internal balance by eliminating wastes and regulating fluid levels.\n\n" +
                        "Both absorb nutrients efficiently (Incorrect)\n" +
                        "Nutrient absorption is specific to the digestive system.\n\n" +
                        "Both mechanically break down food (Incorrect)\n" +
                        "Mechanical digestion is a function of the digestive system.\n\n" +
                        "Both produce bile (Incorrect)\n" +
                        "Bile production is a function of the liver in the digestive system."
        );

        questions.add("Which of the following processes is NOT a direct function of the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration of blood",
                "Reabsorption of important solutes",
                "Production of digestive enzymes",
                "Secretion of metabolic wastes"
        )));
        correctAnswers.add("Production of digestive enzymes");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Production of digestive enzymes (Correct answer)\n" +
                        "This function belongs to the digestive system, not the urinary system.\n\n" +
                        "Filtration of blood (Incorrect)\n" +
                        "Kidneys filter blood to form urine.\n\n" +
                        "Reabsorption of important solutes (Incorrect)\n" +
                        "Essential solutes and water are reabsorbed in the nephrons.\n\n" +
                        "Secretion of metabolic wastes (Incorrect)\n" +
                        "Waste substances are secreted into the filtrate for elimination."
        );

        questions.add("What is the main functional unit of the kidney?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glomerulus",
                "Nephron",
                "Ureter",
                "Renal pelvis"
        )));
        correctAnswers.add("Nephron");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Nephron (Correct answer)\n" +
                        "The nephron performs blood filtration, reabsorption, and secretion.\n\n" +
                        "Glomerulus (Incorrect)\n" +
                        "It is part of the nephron, involved in initial filtration.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "It is a tube that transports urine, not a filtration unit.\n\n" +
                        "Renal pelvis (Incorrect)\n" +
                        "It collects urine but does not filter it."
        );

        questions.add("What is the role of the glomerulus within a nephron?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To store urine",
                "To filter blood plasma into the Bowman’s capsule",
                "To transport urine to the ureter",
                "To reabsorb glucose and electrolytes"
        )));
        correctAnswers.add("To filter blood plasma into the Bowman’s capsule");
        rationales.put(53,
                "RATIONALE:\n" +
                        "To filter blood plasma into the Bowman’s capsule (Correct answer)\n" +
                        "The glomerulus filters blood, initiating urine formation.\n\n" +
                        "To store urine (Incorrect)\n" +
                        "Urine is not stored in the glomerulus.\n\n" +
                        "To transport urine to the ureter (Incorrect)\n" +
                        "The ureter, not the glomerulus, handles transport.\n\n" +
                        "To reabsorb glucose and electrolytes (Incorrect)\n" +
                        "Reabsorption occurs in the tubules, not the glomerulus."
        );

        questions.add("Which part of the nephron is primarily responsible for creating a concentration gradient for water reabsorption?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bowman’s capsule",
                "Proximal convoluted tubule",
                "Loop of Henle",
                "Distal convoluted tubule"
        )));
        correctAnswers.add("Loop of Henle");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Loop of Henle (Correct answer)\n" +
                        "It creates an osmotic gradient that drives water reabsorption.\n\n" +
                        "Bowman’s capsule (Incorrect)\n" +
                        "It collects filtrate but does not form gradients.\n\n" +
                        "Proximal convoluted tubule (Incorrect)\n" +
                        "It reabsorbs solutes but does not establish the gradient.\n\n" +
                        "Distal convoluted tubule (Incorrect)\n" +
                        "It fine-tunes reabsorption but isn't the primary site for gradient formation."
        );

        questions.add("Which of the following is a major role of the ureters?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtering blood",
                "Transporting urine from the kidneys to the bladder",
                "Storing urine",
                "Reabsorbing water and solutes from the filtrate"
        )));
        correctAnswers.add("Transporting urine from the kidneys to the bladder");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Transporting urine from the kidneys to the bladder (Correct answer)\n" +
                        "Ureters are muscular tubes that propel urine to the bladder.\n\n" +
                        "Filtering blood (Incorrect)\n" +
                        "This occurs in the kidneys.\n\n" +
                        "Storing urine (Incorrect)\n" +
                        "Urine is stored in the bladder.\n\n" +
                        "Reabsorbing water and solutes from the filtrate (Incorrect)\n" +
                        "This function occurs in nephron tubules."
        );

        questions.add("How do the ureters ensure urine is transported effectively to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Through the force of gravity alone",
                "By peristaltic waves (muscular contractions)",
                "Through simple diffusion",
                "By active electrolyte transport"
        )));
        correctAnswers.add("By peristaltic waves (muscular contractions)");
        rationales.put(56,
                "RATIONALE:\n" +
                        "By peristaltic waves (muscular contractions) (Correct answer)\n" +
                        "Muscular contractions move urine toward the bladder.\n\n" +
                        "Through the force of gravity alone (Incorrect)\n" +
                        "Gravity helps, but peristalsis is essential.\n\n" +
                        "Through simple diffusion (Incorrect)\n" +
                        "Diffusion doesn't move large volumes of urine.\n\n" +
                        "By active electrolyte transport (Incorrect)\n" +
                        "Active transport is used in nephron cells, not ureters."
        );

        questions.add("What is the primary function of the bladder in the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtering blood",
                "Temporarily storing urine",
                "Transporting urine to the urethra",
                "Regulating blood pH"
        )));
        correctAnswers.add("Temporarily storing urine");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Temporarily storing urine (Correct answer)\n" +
                        "The bladder stores urine until urination.\n\n" +
                        "Filtering blood (Incorrect)\n" +
                        "This function is performed by the kidneys.\n\n" +
                        "Transporting urine to the urethra (Incorrect)\n" +
                        "Though the bladder pushes urine out, storage is its main role.\n\n" +
                        "Regulating blood pH (Incorrect)\n" +
                        "The kidneys help regulate blood pH."
        );

        questions.add("Which structure is the final pathway for urine to exit the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ureter",
                "Bladder",
                "Urethra",
                "Renal pelvis"
        )));
        correctAnswers.add("Urethra");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Urethra (Correct answer)\n" +
                        "It expels urine from the body during urination.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "Ureters deliver urine to the bladder.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "Bladder stores urine, does not expel it directly.\n\n" +
                        "Renal pelvis (Incorrect)\n" +
                        "It collects urine in the kidney before moving it to the ureter."
        );

        questions.add("How does the structure of the male urethra differ from the female urethra?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They are identical in length and function",
                "The male urethra is longer and passes through the penis, while the female urethra is shorter",
                "The male urethra is used for waste storage, while the female urethra is used for voiding",
                "The female urethra is longer and curved, while the male urethra is short and straight"
        )));
        correctAnswers.add("The male urethra is longer and passes through the penis, while the female urethra is shorter");
        rationales.put(59,
                "RATIONALE:\n" +
                        "The male urethra is longer and passes through the penis, while the female urethra is shorter (Correct answer)\n" +
                        "This anatomical difference is due to reproductive and structural design.\n\n" +
                        "They are identical in length and function (Incorrect)\n" +
                        "They differ greatly in length and dual function in males.\n\n" +
                        "The male urethra is used for waste storage, while the female urethra is used for voiding (Incorrect)\n" +
                        "Neither stores waste; both function in urine expulsion.\n\n" +
                        "The female urethra is longer and curved, while the male urethra is short and straight (Incorrect)\n" +
                        "The female urethra is not longer than the male urethra"
        );

        questions.add("After filtration in the kidneys, which process ensures that essential substances are retained?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secretion",
                "Diffusion",
                "Reabsorption",
                "Excretion"
        )));
        correctAnswers.add("Reabsorption");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Reabsorption (Correct answer)\n" +
                        "Reabsorption in the tubular portions of the nephron ensures that water, glucose, and electrolytes are returned to the bloodstream.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion adds extra waste substances to the filtrate.\n\n" +
                        "Diffusion (Incorrect)\n" +
                        "Diffusion is not the primary mechanism for reclaiming valuable substances.\n\n" +
                        "Excretion (Incorrect)\n" +
                        "Excretion is the final elimination of urine, not the retention of essential substances."
        );

        questions.add("Which of the following is a main component of urine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digestive enzymes",
                "Metabolic wastes (e.g., urea), salts, and water",
                "Glucose and amino acids in large quantities",
                "Bile acids and fats"
        )));
        correctAnswers.add("Metabolic wastes (e.g., urea), salts, and water");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Metabolic wastes (e.g., urea), salts, and water (Correct answer)\n" +
                        "Urine primarily consists of metabolic waste products, excess salts, and water.\n\n" +
                        "Digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are not present in urine.\n\n" +
                        "Glucose and amino acids in large quantities (Incorrect)\n" +
                        "Glucose and amino acids are reabsorbed unless there is a pathological condition.\n\n" +
                        "Bile acids and fats (Incorrect)\n" +
                        "Bile acids and fats are associated with the digestive system."
        );

        questions.add("Which of the following functions is NOT performed by the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration of blood",
                "Reabsorption of water and electrolytes",
                "Secretion of additional waste materials",
                "Production of digestive acids"
        )));
        correctAnswers.add("Production of digestive acids");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Production of digestive acids (Correct answer)\n" +
                        "The kidneys do not produce digestive acids; that function belongs to the stomach.\n\n" +
                        "Filtration of blood (Incorrect)\n" +
                        "This is a normal kidney function.\n\n" +
                        "Reabsorption of water and electrolytes (Incorrect)\n" +
                        "This is a normal kidney function.\n\n" +
                        "Secretion of additional waste materials (Incorrect)\n" +
                        "This is a normal kidney function."
        );

        questions.add("How does the urinary system help regulate blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By releasing digestive enzymes",
                "Through renin secretion as part of the renin–angiotensin system",
                "By storing urine in the bladder",
                "By promoting nutrient absorption"
        )));
        correctAnswers.add("Through renin secretion as part of the renin–angiotensin system");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Through renin secretion as part of the renin–angiotensin system (Correct answer)\n" +
                        "The kidneys secrete renin, which plays a crucial role in the renin–angiotensin system, helping regulate blood pressure.\n\n" +
                        "By releasing digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are not involved in blood pressure regulation.\n\n" +
                        "By storing urine in the bladder (Incorrect)\n" +
                        "Urine storage does not affect blood pressure regulation.\n\n" +
                        "By promoting nutrient absorption (Incorrect)\n" +
                        "Nutrient absorption is a digestive function, not a direct means of blood pressure control."
        );

        questions.add("Which statement best describes the connection between the urinary and digestive systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The digestive system filters blood and produces urine.",
                "The urinary system absorbs nutrients directly from the gastrointestinal tract.",
                "The digestive system generates metabolic wastes (e.g., urea) that are subsequently filtered by the kidneys.",
                "They function entirely independently with no interaction."
        )));
        correctAnswers.add("The digestive system generates metabolic wastes (e.g., urea) that are subsequently filtered by the kidneys.");
        rationales.put(64,
                "RATIONALE:\n" +
                        "The digestive system generates metabolic wastes (e.g., urea) that are subsequently filtered by the kidneys. (Correct answer)\n" +
                        "The digestive process produces metabolic byproducts (such as urea) that enter the blood and are filtered out by the kidneys.\n\n" +
                        "The digestive system filters blood and produces urine. (Incorrect)\n" +
                        "Urine production is carried out by the kidneys, not the digestive system.\n\n" +
                        "The urinary system absorbs nutrients directly from the gastrointestinal tract. (Incorrect)\n" +
                        "Nutrient absorption is a function of the digestive system, not the urinary system.\n\n" +
                        "They function entirely independently with no interaction. (Incorrect)\n" +
                        "The two systems are functionally interconnected."
        );

        questions.add("Which structural feature of the kidneys allows for the production of concentrated urine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bowman’s capsule",
                "Proximal convoluted tubule",
                "Loop of Henle",
                "Ureter"
        )));
        correctAnswers.add("Loop of Henle");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Loop of Henle (Correct answer)\n" +
                        "The loop of Henle plays a critical role in creating an osmotic gradient that allows for water reabsorption and urine concentration.\n\n" +
                        "Bowman’s capsule (Incorrect)\n" +
                        "Bowman’s capsule collects the filtrate but does not concentrate urine.\n\n" +
                        "Proximal convoluted tubule (Incorrect)\n" +
                        "The proximal tubule reabsorbs water and solutes but does not establish the primary concentration gradient.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "The ureter only transports urine to the bladder."
        );

        questions.add("Which of the following is NOT a role of the kidneys in blood regulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Removing metabolic waste products",
                "Regulating electrolyte concentrations",
                "Controlling blood pH",
                "Producing bile"
        )));
        correctAnswers.add("Producing bile");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Producing bile (Correct answer)\n" +
                        "Bile is produced by the liver, not the kidneys.\n\n" +
                        "Removing metabolic waste products (Incorrect)\n" +
                        "This is a function of the kidneys.\n\n" +
                        "Regulating electrolyte concentrations (Incorrect)\n" +
                        "This is a function of the kidneys.\n\n" +
                        "Controlling blood pH (Incorrect)\n" +
                        "This is a function of the kidneys."
        );

        questions.add("Which of the following accurately describes the function of secretion in the nephron?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It filters blood at the glomerulus.",
                "It returns essential nutrients to the bloodstream.",
                "It actively transports additional wastes into the tubule from the blood.",
                "It stores the final urine product."
        )));
        correctAnswers.add("It actively transports additional wastes into the tubule from the blood.");
        rationales.put(67,
                "RATIONALE:\n" +
                        "It actively transports additional wastes into the tubule from the blood. (Correct answer)\n" +
                        "Secretion in the nephron involves the active transport of substances (e.g., excess ions, drugs, and wastes) from the blood into the tubular fluid.\n\n" +
                        "It filters blood at the glomerulus. (Incorrect)\n" +
                        "Filtration is the function of the glomerulus.\n\n" +
                        "It returns essential nutrients to the bloodstream. (Incorrect)\n" +
                        "Returning nutrients is reabsorption.\n\n" +
                        "It stores the final urine product. (Incorrect)\n" +
                        "Storage occurs in the bladder."
        );

        questions.add("What is the main role of the renal pelvis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtering blood plasma",
                "Collecting urine from the kidney before it enters the ureter",
                "Absorbing essential nutrients",
                "Secreting metabolic wastes"
        )));
        correctAnswers.add("Collecting urine from the kidney before it enters the ureter");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Collecting urine from the kidney before it enters the ureter (Correct answer)\n" +
                        "The renal pelvis collects the urine formed in the kidney and funnels it into the ureter.\n\n" +
                        "Filtering blood plasma (Incorrect)\n" +
                        "The glomeruli filter blood plasma.\n\n" +
                        "Absorbing essential nutrients (Incorrect)\n" +
                        "Nutrient absorption is not a renal pelvis function.\n\n" +
                        "Secreting metabolic wastes (Incorrect)\n" +
                        "Secretion occurs in the nephron."
        );

        questions.add("Which structure ultimately expels urine from the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidney",
                "Ureter",
                "Bladder",
                "Urethra"
        )));
        correctAnswers.add("Urethra");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Urethra (Correct answer)\n" +
                        "The urethra is the final conduit through which urine is excreted from the body.\n\n" +
                        "Kidney (Incorrect)\n" +
                        "Kidneys filter blood and form urine.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "Ureters transport urine from the kidneys to the bladder.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "While the bladder stores urine, excretion occurs via the urethra."
        );

        questions.add("Which process in the nephron ensures that essential substances are not lost in the urine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration",
                "Secretion",
                "Reabsorption", // Correct answer
                "Excretion"
        )));
        correctAnswers.add("Reabsorption");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Reabsorption (Correct answer)\n" +
                        "Reabsorption is the process by which essential molecules (water, glucose, ions) are recovered from the filtrate and returned to the bloodstream.\n\n" +
                        "Filtration (Incorrect)\n" +
                        "Filtration removes substances from the blood, including both needed and waste components.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion adds extra waste substances to the filtrate.\n\n" +
                        "Excretion (Incorrect)\n" +
                        "Excretion is the final elimination of urine."
        );

        questions.add("Which factor is essential for the kidneys to concentrate urine effectively?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The presence of digestive enzymes",
                "A high glomerular filtration rate combined with the countercurrent mechanism in the loop of Henle", // Correct answer
                "Constant bladder contractions",
                "Increased peristalsis in the ureters"
        )));
        correctAnswers.add("A high glomerular filtration rate combined with the countercurrent mechanism in the loop of Henle");
        rationales.put(71,
                "RATIONALE:\n" +
                        "A high glomerular filtration rate combined with the countercurrent mechanism in the loop of Henle (Correct answer)\n" +
                        "The countercurrent mechanism in the loop of Henle, along with a high glomerular filtration rate, is key to concentrating urine by creating an osmotic gradient.\n\n" +
                        "The presence of digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are not involved in urine concentration.\n\n" +
                        "Constant bladder contractions (Incorrect)\n" +
                        "Bladder contractions relate to urine expulsion, not concentration.\n\n" +
                        "Increased peristalsis in the ureters (Incorrect)\n" +
                        "Ureteral peristalsis moves urine rather than concentrating it."
        );

        questions.add("Which hormone is directly secreted by the kidneys to help regulate blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Aldosterone",
                "Renin", // Correct answer
                "Cortisol"
        )));
        correctAnswers.add("Renin");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Renin (Correct answer)\n" +
                        "The kidneys secrete renin, an enzyme essential for the renin–angiotensin system, which plays a major role in blood pressure regulation.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is secreted by the pancreas and regulates blood glucose.\n\n" +
                        "Aldosterone (Incorrect)\n" +
                        "Aldosterone is secreted by the adrenal cortex and regulates electrolyte balance; it is not produced by the kidneys.\n\n" +
                        "Cortisol (Incorrect)\n" +
                        "Cortisol is secreted by the adrenal cortex and is primarily involved in stress responses and metabolism."
        );

        questions.add("Which statement best describes the renin–angiotensin system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "A process by which the kidneys filter blood more efficiently",
                "A hormonal cascade initiated by renin that regulates blood pressure, fluid, and electrolyte balance", // Correct answer
                "A method for the kidneys to directly absorb digestive nutrients",
                "A system that controls the contraction of the ureters"
        )));
        correctAnswers.add("A hormonal cascade initiated by renin that regulates blood pressure, fluid, and electrolyte balance");
        rationales.put(73,
                "RATIONALE:\n" +
                        "A hormonal cascade initiated by renin that regulates blood pressure, fluid, and electrolyte balance (Correct answer)\n" +
                        "The renin–angiotensin system is initiated by renin release from the kidneys and regulates blood pressure through a hormonal cascade affecting vessel constriction and sodium retention.\n\n" +
                        "A process by which the kidneys filter blood more efficiently (Incorrect)\n" +
                        "While related to kidney function, the renin–angiotensin system specifically governs blood pressure and fluid balance.\n\n" +
                        "A method for the kidneys to directly absorb digestive nutrients (Incorrect)\n" +
                        "Nutrient absorption is not a function of this system.\n\n" +
                        "A system that controls the contraction of the ureters (Incorrect)\n" +
                        "Ureteral contractions are independent of the renin–angiotensin system."
        );

        questions.add("Why is the urinary system often referred to as the body's \"chemical processing plant\"?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Because it directly digests food",
                "Because it filters blood, reabsorbs valuable substances, and excretes waste to maintain chemical balance", // Correct answer
                "Because it produces hormones that convert food into energy",
                "Because it generates electrical impulses for muscle contractions"
        )));
        correctAnswers.add("Because it filters blood, reabsorbs valuable substances, and excretes waste to maintain chemical balance");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Because it filters blood, reabsorbs valuable substances, and excretes waste to maintain chemical balance (Correct answer)\n" +
                        "The kidneys filter blood, reclaim needed substances through reabsorption, and excrete wastes via secretion and excretion, all of which maintain the chemical composition of the blood.\n\n" +
                        "Because it directly digests food (Incorrect)\n" +
                        "The urinary system does not digest food; that is the digestive system’s role.\n\n" +
                        "Because it produces hormones that convert food into energy (Incorrect)\n" +
                        "Energy conversion is carried out by cellular metabolism, not directly by the urinary system.\n\n" +
                        "Because it generates electrical impulses for muscle contractions (Incorrect)\n" +
                        "Electrical impulses are generated by nerves and muscles, not the kidneys."
        );

        questions.add("What is the main consequence of a blockade in the ureters?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Enhanced urine production by the kidneys",
                "Decreased blood filtration in the glomeruli",
                "Impaired transport of urine, potentially leading to backflow and kidney damage", // Correct answer
                "Increased reabsorption of nutrients in the bladder"
        )));
        correctAnswers.add("Impaired transport of urine, potentially leading to backflow and kidney damage");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Impaired transport of urine, potentially leading to backflow and kidney damage (Correct answer)\n" +
                        "A ureteral blockage prevents urine from reaching the bladder, causing urine to back up, which can lead to hydronephrosis and kidney damage.\n\n" +
                        "Enhanced urine production by the kidneys (Incorrect)\n" +
                        "The kidneys might continue producing urine, but a blockage impairs its passage.\n\n" +
                        "Decreased blood filtration in the glomeruli (Incorrect)\n" +
                        "Filtration itself occurs in the kidneys, although increased backpressure can eventually affect filtration.\n\n" +
                        "Increased reabsorption of nutrients in the bladder (Incorrect)\n" +
                        "Nutrient reabsorption does not occur in the bladder."
        );

        questions.add("Which structure collects urine from the kidney before it passes to the ureter?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bowman’s capsule",
                "Renal pelvis", // Correct answer
                "Distal convoluted tubule",
                "Loop of Henle"
        )));
        correctAnswers.add("Renal pelvis");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Renal pelvis (Correct answer)\n" +
                        "The renal pelvis is the area in the kidney that collects urine from the collecting ducts before it flows into the ureter.\n\n" +
                        "Bowman’s capsule (Incorrect)\n" +
                        "Bowman’s capsule collects the filtrate from the glomerulus.\n\n" +
                        "Distal convoluted tubule (Incorrect)\n" +
                        "The distal convoluted tubule is part of the nephron involved in reabsorption and secretion.\n\n" +
                        "Loop of Henle (Incorrect)\n" +
                        "The loop of Henle is involved in creating an osmotic gradient, not urine collection."
        );

        questions.add("How does the urinary system contribute to overall homeostasis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By producing digestive enzymes that break down food",
                "By filtering the blood to remove wastes, maintaining fluid and electrolyte balance, and regulating pH", // Correct answer
                "By storing nutrients for later use",
                "By directly controlling appetite through hormone secretion"
        )));
        correctAnswers.add("By filtering the blood to remove wastes, maintaining fluid and electrolyte balance, and regulating pH");
        rationales.put(77,
                "RATIONALE:\n" +
                        "By filtering the blood to remove wastes, maintaining fluid and electrolyte balance, and regulating pH (Correct answer)\n" +
                        "The kidneys filter blood to eliminate waste products and regulate the balance of fluids, electrolytes, and pH, thereby maintaining homeostasis.\n\n" +
                        "By producing digestive enzymes that break down food (Incorrect)\n" +
                        "Digestive enzymes are produced by digestive organs, not by the urinary system.\n\n" +
                        "By storing nutrients for later use (Incorrect)\n" +
                        "Nutrient storage is not a role of the urinary system.\n\n" +
                        "By directly controlling appetite through hormone secretion (Incorrect)\n" +
                        "Appetite regulation is primarily handled by the nervous and endocrine systems, not directly by the urinary system."
        );

        questions.add("Which of the following best represents the sequence of processes in urine formation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Reabsorption → Filtration → Secretion → Excretion",
                "Filtration → Reabsorption → Secretion → Excretion", // Correct answer
                "Filtration → Secretion → Reabsorption → Excretion",
                "Secretion → Filtration → Excretion → Reabsorption"
        )));
        correctAnswers.add("Filtration → Reabsorption → Secretion → Excretion");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Filtration → Reabsorption → Secretion → Excretion (Correct answer)\n" +
                        "The process starts with filtration in the glomerulus; then, essential substances are reabsorbed; waste is added by secretion; and finally, the urine is excreted.\n\n" +
                        "Reabsorption → Filtration → Secretion → Excretion (Incorrect)\n" +
                        "The sequence begins with filtration.\n\n" +
                        "Filtration → Secretion → Reabsorption → Excretion (Incorrect)\n" +
                        "The order of reabsorption and secretion is reversed here.\n\n" +
                        "Secretion → Filtration → Excretion → Reabsorption (Incorrect)\n" +
                        "The sequence is not in the proper order."
        );

        questions.add("Which of the following statements best explains why the kidneys are vital for maintaining blood chemistry?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They solely produce urine that is excreted without alteration",
                "They remove toxins and reabsorb vital substances, thus directly influencing blood composition", // Correct answer
                "They convert blood into bile directly",
                "They store blood until it can be circulated again"
        )));
        correctAnswers.add("They remove toxins and reabsorb vital substances, thus directly influencing blood composition");
        rationales.put(79,
                "RATIONALE:\n" +
                        "They remove toxins and reabsorb vital substances, thus directly influencing blood composition (Correct answer)\n" +
                        "By filtering toxins and reabsorbing essential substances, the kidneys regulate the composition of the blood, ensuring proper chemical balance.\n\n" +
                        "They solely produce urine that is excreted without alteration (Incorrect)\n" +
                        "Urine formation is a complex process; it is not about unaltered excretion.\n\n" +
                        "They convert blood into bile directly (Incorrect)\n" +
                        "Bile is produced by the liver.\n\n" +
                        "They store blood until it can be circulated again (Incorrect)\n" +
                        "The kidneys do not store blood."
        );

        questions.add("Which of the following best defines “excretion” in the context of the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The filtration of blood plasma",
                "The reabsorption of nutrients",
                "The final elimination of urine from the body", // Correct answer
                "The production of metabolic waste"
        )));
        correctAnswers.add("The final elimination of urine from the body");
        rationales.put(80,
                "RATIONALE:\n" +
                        "The final elimination of urine from the body (Correct answer)\n" +
                        "Excretion is the final step, wherein urine is eliminated from the body via the urethra.\n\n" +
                        "The filtration of blood plasma (Incorrect)\n" +
                        "Filtration is the process that initiates urine formation.\n\n" +
                        "The reabsorption of nutrients (Incorrect)\n" +
                        "Reabsorption returns valuable substances to the bloodstream.\n\n" +
                        "The production of metabolic waste (Incorrect)\n" +
                        "Metabolic waste production occurs during metabolism and is then filtered out by the kidneys."
        );

        questions.add("What is the correct order of structures urine passes through from formation until excretion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidney → Bladder → Ureter → Urethra",
                "Kidney → Urethra → Bladder → Ureter",
                "Kidney → Ureter → Bladder → Urethra", // Correct answer
                "Kidney → Ureter → Urethra → Bladder"
        )));
        correctAnswers.add("Kidney → Ureter → Bladder → Urethra");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Kidney → Ureter → Bladder → Urethra (Correct answer)\n" +
                        "Urine is produced in the kidneys, transported via the ureters to the bladder (which stores it), and finally expelled through the urethra.\n\n" +
                        "Kidney → Bladder → Ureter → Urethra (Incorrect)\n" +
                        "Urine travels from the kidneys into the ureters, not directly into the bladder first.\n\n" +
                        "Kidney → Urethra → Bladder → Ureter (Incorrect)\n" +
                        "Urine does not bypass the bladder to reach the urethra immediately.\n\n" +
                        "Kidney → Ureter → Urethra → Bladder (Incorrect)\n" +
                        "The bladder comes before the urethra; urine must be stored in the bladder and then excreted via the urethra."
        );

        questions.add("Which structure is primarily responsible for temporarily storing urine until a sufficient volume is reached?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Kidney",
                "Ureter",
                "Bladder", // Correct answer
                "Urethra"
        )));
        correctAnswers.add("Bladder");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Bladder (Correct answer)\n" +
                        "The bladder is a hollow, expandable organ that stores urine until it becomes full.\n\n" +
                        "Kidney (Incorrect)\n" +
                        "The kidneys filter blood and produce urine.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "The ureters transport urine, not store it.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra serves as the exit pathway for urine."
        );

        questions.add("How does the urinary system help maintain acid–base equilibrium in the blood?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By secreting digestive enzymes",
                "By excreting excess acids or bases through urine formation", // Correct answer
                "By converting urine into bicarbonate",
                "By storing electrolytes in the bladder"
        )));
        correctAnswers.add("By excreting excess acids or bases through urine formation");
        rationales.put(83,
                "RATIONALE:\n" +
                        "By excreting excess acids or bases through urine formation (Correct answer)\n" +
                        "The kidneys regulate pH by excreting excess hydrogen ions or conserving bicarbonate, thereby helping to maintain acid–base balance.\n\n" +
                        "By secreting digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are produced by the pancreas and stomach, not involved in pH regulation.\n\n" +
                        "By converting urine into bicarbonate (Incorrect)\n" +
                        "The kidneys do not convert urine into bicarbonate; rather, bicarbonate is reabsorbed or secreted as needed.\n\n" +
                        "By storing electrolytes in the bladder (Incorrect)\n" +
                        "Although electrolytes are managed by the kidneys, the bladder’s role is solely storage, not active regulation of pH."
        );

        questions.add("Which statement best summarizes the functional connection between the urinary and digestive systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The digestive system produces urine, which is stored by the urinary system.",
                "The urinary system extracts metabolic wastes (such as urea) produced by the digestive process and filters them from the blood.", // Correct answer
                "Both systems work together to digest proteins directly.",
                "There is no significant connection between the two systems."
        )));
        correctAnswers.add("The urinary system extracts metabolic wastes (such as urea) produced by the digestive process and filters them from the blood.");
        rationales.put(84,
                "RATIONALE:\n" +
                        "The urinary system extracts metabolic wastes (such as urea) produced by the digestive process and filters them from the blood. (Correct answer)\n" +
                        "As the digestive system metabolizes food, by‐products (e.g., urea from protein metabolism) enter the bloodstream and are then removed by the kidneys.\n\n" +
                        "The digestive system produces urine, which is stored by the urinary system. (Incorrect)\n" +
                        "Urine is produced by the kidneys, not by the digestive system.\n\n" +
                        "Both systems work together to digest proteins directly. (Incorrect)\n" +
                        "Protein digestion is handled by enzymes in the digestive tract, not a shared process.\n\n" +
                        "There is no significant connection between the two systems. (Incorrect)\n" +
                        "The systems are interconnected, particularly in managing metabolic wastes."
        );

        questions.add("Which process is NOT part of urine formation in the kidneys?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration",
                "Reabsorption",
                "Secretion",
                "Peristalsis" // Correct answer
        )));
        correctAnswers.add("Peristalsis");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Peristalsis (Correct answer)\n" +
                        "Peristalsis is not part of urine formation; it is used later in the ureters for urine transport.\n\n" +
                        "Filtration (Incorrect)\n" +
                        "Filtration occurs in the glomerulus.\n\n" +
                        "Reabsorption (Incorrect)\n" +
                        "Reabsorption returns essential substances to the bloodstream.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion adds additional wastes to the filtrate."
        );

        questions.add("Which statement best describes the kidneys’ overall contribution to homeostasis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They solely filter blood without affecting electrolyte balance.",
                "They reabsorb water and solutes, remove metabolic wastes, and help regulate blood pressure and pH.", // Correct answer
                "They store blood until it is needed by other organs.",
                "They function independently without influencing other body systems."
        )));
        correctAnswers.add("They reabsorb water and solutes, remove metabolic wastes, and help regulate blood pressure and pH.");
        rationales.put(86,
                "RATIONALE:\n" +
                        "They reabsorb water and solutes, remove metabolic wastes, and help regulate blood pressure and pH. (Correct answer)\n" +
                        "The kidneys perform multiple functions essential to homeostasis: filtering blood, reclaiming vital substances, excreting wastes, and regulating both pH and blood pressure.\n\n" +
                        "They solely filter blood without affecting electrolyte balance. (Incorrect)\n" +
                        "While filtration is a key role, the kidneys also reabsorb substances and regulate pH and blood pressure.\n\n" +
                        "They store blood until it is needed by other organs. (Incorrect)\n" +
                        "The kidneys do not store blood.\n\n" +
                        "They function independently without influencing other body systems. (Incorrect)\n" +
                        "The kidneys interact with both endocrine and circulatory systems, among others."
        );

        questions.add("According to the overview, dysfunction in the digestive system can affect the urinary system by…");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decreasing the volume of urine produced",
                "Increasing the toxic load on the kidneys", // Correct answer
                "Enhancing the absorption of digestive enzymes",
                "Directly altering urine pH without impacting filtration"
        )));
        correctAnswers.add("Increasing the toxic load on the kidneys");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Increasing the toxic load on the kidneys (Correct answer)\n" +
                        "If the digestive system is inefficient, more toxins and metabolic wastes (e.g., urea) may accumulate in the blood, thereby increasing the filtering burden on the kidneys.\n\n" +
                        "Decreasing the volume of urine produced (Incorrect)\n" +
                        "Dysfunction in digestion does not necessarily decrease urine volume.\n\n" +
                        "Enhancing the absorption of digestive enzymes (Incorrect)\n" +
                        "The urinary system does not absorb digestive enzymes.\n\n" +
                        "Directly altering urine pH without impacting filtration (Incorrect)\n" +
                        "While urine pH may be impacted indirectly, the key connection is the increased toxic load."
        );

        questions.add("Which option best describes the “Transport & Storage” stage of the urinary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Urine is produced in the kidney and immediately excreted via the urethra.",
                "Urine is transported via the ureters to the bladder, where it is temporarily stored until voiding.", // Correct answer
                "Urine is reabsorbed completely into the bloodstream.",
                "The bladder actively concentrates urine through muscular contractions."
        )));
        correctAnswers.add("Urine is transported via the ureters to the bladder, where it is temporarily stored until voiding.");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Urine is transported via the ureters to the bladder, where it is temporarily stored until voiding. (Correct answer)\n" +
                        "The overview explains that urine is conveyed from the kidneys to the bladder via the ureters and stored in the bladder until it reaches fullness.\n\n" +
                        "Urine is produced in the kidney and immediately excreted via the urethra. (Incorrect)\n" +
                        "Urine is not immediately excreted; it is stored first.\n\n" +
                        "Urine is reabsorbed completely into the bloodstream. (Incorrect)\n" +
                        "Essential water and solutes are reabsorbed, but not all urine is reabsorbed.\n\n" +
                        "The bladder actively concentrates urine through muscular contractions. (Incorrect)\n" +
                        "The bladder’s main function is storage and signaling, not concentrating urine."
        );

        questions.add("Which statement best defines the urinary system’s role as the body’s “filtration network”?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It solely filters blood for the purpose of nutrient absorption.",
                "It filters blood to remove metabolic wastes, maintain electrolyte balance, and regulate pH.", // Correct answer
                "It exports digestive enzymes to the blood.",
                "It primarily produces bile to assist in fat digestion."
        )));
        correctAnswers.add("It filters blood to remove metabolic wastes, maintain electrolyte balance, and regulate pH.");
        rationales.put(89,
                "RATIONALE:\n" +
                        "It filters blood to remove metabolic wastes, maintain electrolyte balance, and regulate pH. (Correct answer)\n" +
                        "The urinary system (primarily the kidneys) filters blood to remove wastes, manage electrolytes, and regulate pH.\n\n" +
                        "It solely filters blood for the purpose of nutrient absorption. (Incorrect)\n" +
                        "Nutrient absorption is primarily the function of the digestive system.\n\n" +
                        "It exports digestive enzymes to the blood. (Incorrect)\n" +
                        "Digestive enzymes are produced by the pancreas and stomach.\n\n" +
                        "It primarily produces bile to assist in fat digestion. (Incorrect)\n" +
                        "Bile is produced by the liver, not the urinary system."
        );

        questions.add("Which of the following best summarizes the interplay between the urinary and digestive systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The digestive system stores urine until it is processed by the urinary system.",
                "The urinary system ensures that wastes from metabolic processes (originating from digestion) are removed from the blood.", // Correct answer
                "The digestive system directly controls urination.",
                "The two systems operate entirely independently."
        )));
        correctAnswers.add("The urinary system ensures that wastes from metabolic processes (originating from digestion) are removed from the blood.");
        rationales.put(90,
                "RATIONALE:\n" +
                        "The urinary system ensures that wastes from metabolic processes (originating from digestion) are removed from the blood. (Correct answer)\n" +
                        "The digestive system produces metabolic by‑products (such as urea), which enter the bloodstream and are then filtered out by the kidneys.\n\n" +
                        "The digestive system stores urine until it is processed by the urinary system. (Incorrect)\n" +
                        "Urine is produced by the kidneys, not by the digestive system.\n\n" +
                        "The digestive system directly controls urination. (Incorrect)\n" +
                        "Control of urination is a function of the nervous system and bladder mechanics.\n\n" +
                        "The two systems operate entirely independently. (Incorrect)\n" +
                        "The systems are interconnected through the exchange of nutrients and wastes."
        );

        questions.add("How does the urinary system contribute to fluid balance in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By absorbing water in the stomach",
                "By reabsorbing water and electrolytes during the urine formation process in the kidneys", // Correct answer
                "By producing digestive juices",
                "By actively pumping fluid into the bladder for storage"
        )));
        correctAnswers.add("By reabsorbing water and electrolytes during the urine formation process in the kidneys");
        rationales.put(91,
                "RATIONALE:\n" +
                        "By reabsorbing water and electrolytes during the urine formation process in the kidneys (Correct answer)\n" +
                        "The kidneys reabsorb water and vital electrolytes from the filtrate, helping to maintain proper fluid balance in the blood.\n\n" +
                        "By absorbing water in the stomach (Incorrect)\n" +
                        "Water absorption in the stomach is minimal and not part of urinary function.\n\n" +
                        "By producing digestive juices (Incorrect)\n" +
                        "Digestive juices are produced by the digestive system.\n\n" +
                        "By actively pumping fluid into the bladder for storage (Incorrect)\n" +
                        "Although the bladder stores urine, it does not actively pump fluid; reabsorption occurs within the kidneys."
        );

        questions.add("Which process ultimately results in the elimination of urine from the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Filtration",
                "Reabsorption",
                "Secretion",
                "Excretion" // Correct answer
        )));
        correctAnswers.add("Excretion");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Excretion (Correct answer)\n" +
                        "Excretion is the final process whereby urine is expelled from the body via the urethra.\n\n" +
                        "Filtration (Incorrect)\n" +
                        "Filtration is the first step by which blood is filtered in the kidneys.\n\n" +
                        "Reabsorption (Incorrect)\n" +
                        "Reabsorption recovers essential substances, not the elimination of waste.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion adds wastes to the filtrate."
        );

        questions.add("What is formed as a result of the kidney’s processes before urine is transported to the bladder?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Glomerular filtrate, which then is modified into urine", // Correct answer
                "Pure water, which is then expelled",
                "Nutrient-rich plasma",
                "Bile"
        )));
        correctAnswers.add("Glomerular filtrate, which then is modified into urine");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Glomerular filtrate, which then is modified into urine (Correct answer)\n" +
                        "The kidneys first filter blood, creating a glomerular filtrate that is then modified by reabsorption and secretion to become urine.\n\n" +
                        "Pure water, which is then expelled (Incorrect)\n" +
                        "Although water is reabsorbed, urine is not pure water.\n\n" +
                        "Nutrient-rich plasma (Incorrect)\n" +
                        "Plasma is the liquid part of blood and contains nutrients; it is not the direct outcome of renal filtration.\n\n" +
                        "Bile (Incorrect)\n" +
                        "Bile is produced by the liver, not the kidneys."
        );

        questions.add("The term “excretory system” is another name for which of the following?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The urinary system", // Correct answer
                "The digestive system",
                "The endocrine system",
                "The respiratory system"
        )));
        correctAnswers.add("The urinary system");
        rationales.put(94,
                "RATIONALE:\n" +
                        "The urinary system (Correct answer)\n" +
                        "The urinary system, which includes the kidneys, ureters, bladder, and urethra, is also known as the excretory system because it excretes waste products in the form of urine.\n\n" +
                        "The digestive system (Incorrect)\n" +
                        "These systems perform different functions.\n\n" +
                        "The endocrine system (Incorrect)\n" +
                        "These systems perform different functions.\n\n" +
                        "The respiratory system (Incorrect)\n" +
                        "These systems perform different functions."
        );

        questions.add("How does the urinary system help regulate blood pH?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By producing digestive enzymes",
                "By excreting excess acids or bases and reabsorbing bicarbonate as needed", // Correct answer
                "By pumping blood into the bladder",
                "By releasing insulin"
        )));
        correctAnswers.add("By excreting excess acids or bases and reabsorbing bicarbonate as needed");
        rationales.put(95,
                "RATIONALE:\n" +
                        "By excreting excess acids or bases and reabsorbing bicarbonate as needed (Correct answer)\n" +
                        "The kidneys maintain pH balance by excreting excess hydrogen ions (acid) or by reabsorbing bicarbonate, thereby stabilizing the blood’s pH.\n\n" +
                        "By producing digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes do not affect blood pH regulation.\n\n" +
                        "By pumping blood into the bladder (Incorrect)\n" +
                        "Blood is not pumped into the bladder.\n\n" +
                        "By releasing insulin (Incorrect)\n" +
                        "Insulin, produced by the pancreas, regulates blood sugar, not pH."
        );

        questions.add("Which component of the urinary system is primarily involved in blood filtration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Bladder",
                "Ureter",
                "Kidney", // Correct answer
                "Urethra"
        )));
        correctAnswers.add("Kidney");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Kidney (Correct answer)\n" +
                        "The kidneys filter blood through millions of nephrons.\n\n" +
                        "Bladder (Incorrect)\n" +
                        "The bladder stores urine.\n\n" +
                        "Ureter (Incorrect)\n" +
                        "The ureters transport urine.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra is the passageway for urine excretion."
        );

        questions.add("What is the significance of the nutrient and waste exchange between the digestive and urinary systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It ensures that all ingested food is converted into urine.",
                "It allows nutrients to be absorbed by the digestive system while metabolic wastes (from digestion and metabolism) are filtered out by the kidneys.", // Correct answer
                "It has no impact on overall homeostasis.",
                "It causes the urinary system to produce digestive enzymes."
        )));
        correctAnswers.add("It allows nutrients to be absorbed by the digestive system while metabolic wastes (from digestion and metabolism) are filtered out by the kidneys.");
        rationales.put(97,
                "RATIONALE:\n" +
                        "It allows nutrients to be absorbed by the digestive system while metabolic wastes (from digestion and metabolism) are filtered out by the kidneys. (Correct answer)\n" +
                        "The digestive system extracts nutrients and, in doing so, produces metabolic waste (e.g., urea) that the urinary system must eliminate; this interplay helps maintain homeostasis.\n\n" +
                        "It ensures that all ingested food is converted into urine. (Incorrect)\n" +
                        "Food is digested and nutrients are absorbed, not converted into urine.\n\n" +
                        "It has no impact on overall homeostasis. (Incorrect)\n" +
                        "The relationship is vital for maintaining fluid, electrolyte, and pH balance.\n\n" +
                        "It causes the urinary system to produce digestive enzymes. (Incorrect)\n" +
                        "Digestive enzymes are produced by digestive organs."
        );

        questions.add("What might be a consequence if the kidneys fail to filter blood properly?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Accumulation of metabolic wastes in the bloodstream", // Correct answer
                "Increased efficient nutrient absorption",
                "Immediate production of more urine",
                "Enhanced fluid reabsorption from the bladder"
        )));
        correctAnswers.add("Accumulation of metabolic wastes in the bloodstream");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Accumulation of metabolic wastes in the bloodstream (Correct answer)\n" +
                        "Failure of the kidneys to properly filter blood leads to the buildup of toxins and wastes (such as urea) in the bloodstream, which can be life threatening.\n\n" +
                        "Increased efficient nutrient absorption (Incorrect)\n" +
                        "Nutrient absorption occurs in the digestive system and is not enhanced by renal failure.\n\n" +
                        "Immediate production of more urine (Incorrect)\n" +
                        "While urine production may be affected, the critical issue is waste accumulation.\n\n" +
                        "Enhanced fluid reabsorption from the bladder (Incorrect)\n" +
                        "Fluid reabsorption in the bladder does not occur; the bladder is only a storage organ."
        );

        questions.add("What is the overall role of the urinary system in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To filter blood, remove metabolic wastes and excess water, maintain electrolyte and pH balance, and contribute to blood pressure regulation", // Correct answer
                "To digest food and absorb nutrients",
                "To produce digestive enzymes",
                "To regulate body temperature exclusively"
        )));
        correctAnswers.add("To filter blood, remove metabolic wastes and excess water, maintain electrolyte and pH balance, and contribute to blood pressure regulation");
        rationales.put(99,
                "RATIONALE:\n" +
                        "To filter blood, remove metabolic wastes and excess water, maintain electrolyte and pH balance, and contribute to blood pressure regulation (Correct answer)\n" +
                        "This option summarizes the urinary system’s functions: blood filtration; removal of waste products and excess fluids; maintenance of electrolyte levels and pH balance; and its role in blood pressure regulation.\n\n" +
                        "To digest food and absorb nutrients (Incorrect)\n" +
                        "Digestion and nutrient absorption are functions of the digestive system.\n\n" +
                        "To produce digestive enzymes (Incorrect)\n" +
                        "Digestive enzymes are produced by organs such as the pancreas and stomach, not by the urinary system.\n\n" +
                        "To regulate body temperature exclusively (Incorrect)\n" +
                        "Body temperature regulation is primarily managed by the hypothalamus and the circulatory system, not the urinary system."
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
            new AlertDialog.Builder(ChallengeMode11.this)
                    .setTitle("Exit Quiz")
                    .setMessage("Are you sure you want to exit? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        super.onBackPressed();  // This will exit the activity
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
}
