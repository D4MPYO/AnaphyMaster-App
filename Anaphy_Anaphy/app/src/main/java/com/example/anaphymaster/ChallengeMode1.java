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

public class ChallengeMode1 extends AppCompatActivity {

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

        setContentView(R.layout.challenge_mode1);

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
                Toast.makeText(ChallengeMode1.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(ChallengeMode1.this)
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
            new AlertDialog.Builder(ChallengeMode1.this)
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
                new AlertDialog.Builder(ChallengeMode1.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(ChallengeMode1.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Challenge");
                            averageHelper.updateScore("Challenge", "Integumentary System", correctAnswersCount, totalQuestions);



                            intent.putExtra("difficulty", "Easy");
                            intent.putExtra("category", "Integumentary System");
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
                        Intent intent = new Intent(ChallengeMode1.this, Answer_Result.class);
                        intent.putExtra("correctAnswers", correctAnswersCount);
                        intent.putExtra("totalQuestions", totalQuestions);

                        DatabaseHelper dbHelper = new DatabaseHelper(ChallengeMode1.this);
                        AverageHelper averageHelper = new AverageHelper(ChallengeMode1.this);
                        dbHelper.updateQuizCount("Challenge");
                        averageHelper.updateScore("Challenge", "Integumentary System", correctAnswersCount, totalQuestions);

                        intent.putExtra("difficulty", "Advance");
                        intent.putExtra("category", "Integumentary System");
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
        new AlertDialog.Builder(ChallengeMode1.this)
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

        questions.add("What is the primary function of the epidermis in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secretion of sweat and sebum",
                "Protection from environmental damage", // Correct
                "Thermoregulation through vasodilation",
                "Synthesis of melanin for hair growth"
        )));
        correctAnswers.add("Protection from environmental damage");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Protection from environmental damage (Correct answer)\n" +
                        "The epidermis, particularly the stratum corneum, forms a physical barrier that protects against pathogens, UV radiation, chemicals, and mechanical injury.\n\n" +
                        "Secretion of sweat and sebum (Incorrect)\n" +
                        "Sweat is secreted by sweat glands and sebum by sebaceous glands, both located in the dermis, not the epidermis.\n\n" +
                        "Thermoregulation through vasodilation (Incorrect)\n" +
                        "Thermoregulation is managed by blood vessels and sweat glands in the dermis.\n\n" +
                        "Synthesis of melanin for hair growth (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes in the epidermis for UV protection, not specifically for hair growth."
        );

        questions.add("Which layer of the epidermis is responsible for continuous cell division to replace dead skin cells?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stratum corneum",
                "Stratum lucidum",
                "Stratum basale", // Correct
                "Stratum granulosum"
        )));
        correctAnswers.add("Stratum basale");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Stratum basale (Correct answer)\n" +
                        "The deepest layer of the epidermis where keratinocytes and stem cells continuously divide to produce new skin cells.\n\n" +
                        "Stratum corneum (Incorrect)\n" +
                        "The outermost layer made of dead, keratinized cells that provide protection.\n\n" +
                        "Stratum lucidum (Incorrect)\n" +
                        "A thin, clear layer of dead cells found only in thick skin areas like palms and soles.\n\n" +
                        "Stratum granulosum (Incorrect)\n" +
                        "This layer produces keratin and lipids but does not perform cell division."
        );

        questions.add("What is the primary role of melanin in the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate body temperature",
                "Protect against UV radiation", // Correct
                "Promote wound healing",
                "Enhance sensory perception"
        )));
        correctAnswers.add("Protect against UV radiation");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Protect against UV radiation (Correct answer)\n" +
                        "Melanin, produced by melanocytes, absorbs and dissipates UV radiation, protecting skin cells' DNA from damage.\n\n" +
                        "Regulate body temperature (Incorrect)\n" +
                        "Body temperature is controlled by blood vessels and sweat glands, not melanin.\n\n" +
                        "Promote wound healing (Incorrect)\n" +
                        "Wound healing involves fibroblasts, collagen, and immune responses, not melanin.\n\n" +
                        "Enhance sensory perception (Incorrect)\n" +
                        "Sensory perception is managed by nerve endings located in the dermis."
        );

        questions.add("Which structure in the dermis is responsible for producing fingerprints?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sebaceous glands",
                "Papillary layer", // Correct
                "Reticular layer",
                "Sweat glands"
        )));
        correctAnswers.add("Papillary layer");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Papillary layer (Correct answer)\n" +
                        "The papillary layer contains dermal papillae that form ridges projecting into the epidermis, creating fingerprint patterns.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "These glands produce sebum to lubricate skin and hair, not fingerprints.\n\n" +
                        "Reticular layer (Incorrect)\n" +
                        "This layer provides strength and elasticity but does not form fingerprints.\n\n" +
                        "Sweat glands (Incorrect)\n" +
                        "Sweat glands help in thermoregulation by producing sweat, not fingerprints."
        );

        questions.add("What is the main function of sebaceous glands in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce sweat to cool the body",
                "Secrete sebum to lubricate skin and hair", // Correct
                "Synthesize vitamin D in response to sunlight",
                "Detect touch and pressure stimuli"
        )));
        correctAnswers.add("Secrete sebum to lubricate skin and hair");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Secrete sebum to lubricate skin and hair (Correct answer)\n" +
                        "Sebaceous glands secrete sebum, an oily substance that moisturizes and protects the skin and hair.\n\n" +
                        "Produce sweat to cool the body (Incorrect)\n" +
                        "Sweat is produced by sweat glands, not sebaceous glands.\n\n" +
                        "Synthesize vitamin D in response to sunlight (Incorrect)\n" +
                        "Vitamin D synthesis occurs in the epidermis when exposed to UV light.\n\n" +
                        "Detect touch and pressure stimuli (Incorrect)\n" +
                        "Touch and pressure are detected by specialized sensory receptors in the dermis, not by sebaceous glands."
        );

        questions.add("Which component of the integumentary system is primarily responsible for thermoregulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epidermis",
                "Dermal blood vessels", // Correct
                "Keratinocytes",
                "Melanocytes"
        )));
        correctAnswers.add("Dermal blood vessels");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Dermal blood vessels (Correct answer)\n" +
                        "Blood vessels in the dermis dilate to release heat (vasodilation) or constrict to conserve heat (vasoconstriction), and sweat glands aid cooling, making them key in thermoregulation.\n\n" +
                        "Epidermis (Incorrect)\n" +
                        "The epidermis provides a protective barrier but has minimal direct role in thermoregulation.\n\n" +
                        "Keratinocytes (Incorrect)\n" +
                        "Keratinocytes produce keratin for structural protection, not thermoregulation.\n\n" +
                        "Melanocytes (Incorrect)\n" +
                        "Melanocytes produce melanin for UV protection, not thermoregulation."
        );

        questions.add("What type of tissue primarily composes the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stratified squamous epithelium",
                "Dense irregular connective tissue", // Correct
                "Adipose tissue",
                "Simple cuboidal epithelium"
        )));
        correctAnswers.add("Dense irregular connective tissue");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Dense irregular connective tissue (Correct answer)\n" +
                        "The dermis, particularly the reticular layer, is composed of dense irregular connective tissue containing collagen and elastin fibers, providing strength and elasticity.\n\n" +
                        "Stratified squamous epithelium (Incorrect)\n" +
                        "This tissue type forms the epidermis, not the dermis.\n\n" +
                        "Adipose tissue (Incorrect)\n" +
                        "Adipose tissue is found in the hypodermis, not the dermis.\n\n" +
                        "Simple cuboidal epithelium (Incorrect)\n" +
                        "This tissue is found in glands and tubules, not the dermis."
        );

        questions.add("Which skin accessory structure is primarily involved in sensory perception?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hair follicles",
                "Nail beds",
                "Merkel cells", // Correct
                "Sebaceous glands"
        )));
        correctAnswers.add("Merkel cells");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Merkel cells (Correct answer)\n" +
                        "Merkel cells, located in the epidermis, are mechanoreceptors that detect light touch and pressure, contributing to sensory perception.\n\n" +
                        "Hair follicles (Incorrect)\n" +
                        "Hair follicles produce hair and are associated with sensory nerve endings, but they are not the primary sensory structure.\n\n" +
                        "Nail beds (Incorrect)\n" +
                        "Nail beds support nail growth and do not play a significant role in sensory perception.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands produce sebum, not sensory functions."
        );

        questions.add("What is the primary source of vitamin D production in the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanocytes in the stratum basale",
                "Keratinocytes in the stratum spinosum",
                "7-dehydrocholesterol in the epidermis", // Correct
                "Collagen fibers in the dermis"
        )));
        correctAnswers.add("7-dehydrocholesterol in the epidermis");
        rationales.put(8,
                "RATIONALE:\n" +
                        "7-dehydrocholesterol in the epidermis (Correct answer)\n" +
                        "UV light converts 7-dehydrocholesterol in the epidermis into previtamin D3, which becomes vitamin D3 (cholecalciferol).\n\n" +
                        "Melanocytes in the stratum basale (Incorrect)\n" +
                        "Melanocytes produce melanin, not vitamin D.\n\n" +
                        "Keratinocytes in the stratum spinosum (Incorrect)\n" +
                        "While keratinocytes are involved in the process, the precursor molecule, not the cells themselves, is the key.\n\n" +
                        "Collagen fibers in the dermis (Incorrect)\n" +
                        "Collagen provides structural support, not vitamin D production."
        );

        questions.add("Which condition results from the overproduction of sebum and clogged hair follicles?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Psoriasis",
                "Eczema",
                "Acne", // Correct
                "Melanoma"
        )));
        correctAnswers.add("Acne");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Acne (Correct answer)\n" +
                        "Acne results from excess sebum production, dead skin cells, and bacteria clogging hair follicles, leading to pimples and inflammation.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition causing rapid skin cell turnover and scaly plaques, not related to sebum.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema involves inflammation and itchy rashes, often due to immune responses, not sebum overproduction.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma is a type of skin cancer originating from melanocytes, not related to sebum or clogged follicles."
        );

        questions.add("Which type of sweat gland is primarily involved in thermoregulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eccrine sweat glands", // Correct
                "Apocrine sweat glands",
                "Sebaceous glands",
                "Ceruminous glands"
        )));
        correctAnswers.add("Eccrine sweat glands");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Eccrine sweat glands (Correct answer)\n" +
                        "Eccrine sweat glands, distributed across most of the body, produce watery sweat that evaporates to cool the body, making them the primary glands for thermoregulation.\n\n" +
                        "Apocrine sweat glands (Incorrect)\n" +
                        "Apocrine sweat glands, found in areas like the armpits and groin, produce a thicker sweat associated with stress or hormonal triggers, not primary thermoregulation.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands secrete sebum to lubricate skin and hair, not sweat for thermoregulation.\n\n" +
                        "Ceruminous glands (Incorrect)\n" +
                        "Ceruminous glands in the ear canal produce cerumen (earwax), which protects the ear, not thermoregulation."
        );

        questions.add("What is the primary component of hair and nails?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Keratin", // Correct
                "Melanin",
                "Collagen",
                "Elastin"
        )));
        correctAnswers.add("Keratin");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Keratin (Correct answer)\n" +
                        "Keratin, a tough, fibrous protein produced by keratinocytes, forms the primary structural component of hair and nails, providing strength and durability.\n\n" +
                        "Melanin (Incorrect)\n" +
                        "Melanin provides pigmentation to hair and skin but is not the structural component of hair or nails.\n\n" +
                        "Collagen (Incorrect)\n" +
                        "Collagen is a connective tissue protein found in the dermis, not the primary component of hair or nails.\n\n" +
                        "Elastin (Incorrect)\n" +
                        "Elastin provides elasticity in the dermis but is not a major component of hair or nails."
        );

        questions.add("Which layer of the skin contains the most blood vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dermis", // Correct
                "Epidermis",
                "Hypodermis",
                "Stratum corneum"
        )));
        correctAnswers.add("Dermis");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Dermis (Correct answer)\n" +
                        "The dermis contains a rich network of blood vessels in both the papillary and reticular layers, supplying nutrients, aiding thermoregulation, and supporting immune responses.\n\n" +
                        "Epidermis (Incorrect)\n" +
                        "The epidermis is avascular (lacking blood vessels) and relies on diffusion from the dermis for nutrients.\n\n" +
                        "Hypodermis (Incorrect)\n" +
                        "The hypodermis has fewer blood vessels and is primarily composed of adipose tissue and connective tissue.\n\n" +
                        "Stratum corneum (Incorrect)\n" +
                        "The stratum corneum, the outermost epidermal layer, is composed of dead, keratinized cells and contains no blood vessels."
        );

        questions.add("What is the function of the arrector pili muscle in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cause hair to stand upright", // Correct
                "Secrete sebum into hair follicles",
                "Produce sweat for cooling",
                "Synthesize melanin for pigmentation"
        )));
        correctAnswers.add("Cause hair to stand upright");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Cause hair to stand upright (Correct answer)\n" +
                        "The arrector pili muscle, a smooth muscle attached to hair follicles, contracts in response to cold or fear, causing hair to stand upright (goosebumps) to trap heat or signal alertness.\n\n" +
                        "Secrete sebum into hair follicles (Incorrect)\n" +
                        "Sebum is secreted by sebaceous glands, not arrector pili muscles.\n\n" +
                        "Produce sweat for cooling (Incorrect)\n" +
                        "Sweat is produced by sweat glands, not arrector pili muscles.\n\n" +
                        "Synthesize melanin for pigmentation (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes in the epidermis, not by muscles."
        );

        questions.add("Which skin condition is characterized by an autoimmune response leading to rapid skin cell turnover?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Psoriasis", // Correct
                "Acne",
                "Dermatitis",
                "Vitiligo"
        )));
        correctAnswers.add("Psoriasis");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Psoriasis (Correct answer)\n" +
                        "Psoriasis is an autoimmune condition where the immune system triggers excessive keratinocyte proliferation, leading to rapid skin cell turnover and scaly, red plaques.\n\n" +
                        "Acne (Incorrect)\n" +
                        "Acne results from clogged hair follicles due to excess sebum and bacteria, not an autoimmune response.\n\n" +
                        "Dermatitis (Incorrect)\n" +
                        "Dermatitis involves inflammation of the skin, often due to allergens or irritants, not necessarily rapid cell turnover or autoimmunity.\n\n" +
                        "Vitiligo (Incorrect)\n" +
                        "Vitiligo is an autoimmune condition causing loss of melanocytes and depigmentation, not rapid skin cell turnover."
        );

        questions.add("Which type of cell in the epidermis is responsible for immune surveillance?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Langerhans cells", // Correct answer
                "Keratinocytes",
                "Melanocytes",
                "Merkel cells"
        )));
        correctAnswers.add("Langerhans cells");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Langerhans cells (Correct answer)\n" +
                        "Langerhans cells are dendritic cells in the epidermis that act as antigen-presenting cells, detecting pathogens and initiating immune responses.\n\n" +
                        "Keratinocytes (Incorrect)\n" +
                        "Keratinocytes produce keratin for structural protection, not immune surveillance.\n\n" +
                        "Melanocytes (Incorrect)\n" +
                        "Melanocytes produce melanin for UV protection, not immune functions.\n\n" +
                        "Merkel cells (Incorrect)\n" +
                        "Merkel cells are mechanoreceptors for touch sensation, not immune surveillance."
        );

        questions.add("What is the primary function of the hypodermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Store fat and provide insulation", // Correct answer
                "Produce keratin for skin strength",
                "Synthesize melanin for pigmentation",
                "Secrete sweat for thermoregulation"
        )));
        correctAnswers.add("Store fat and provide insulation");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Store fat and provide insulation (Correct answer)\n" +
                        "The hypodermis, composed of adipose tissue and loose connective tissue, stores fat for energy, provides insulation, and cushions underlying structures.\n\n" +
                        "Produce keratin for skin strength (Incorrect)\n" +
                        "Keratin is produced by keratinocytes in the epidermis, not the hypodermis.\n\n" +
                        "Synthesize melanin for pigmentation (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes in the epidermis.\n\n" +
                        "Secrete sweat for thermoregulation (Incorrect)\n" +
                        "Sweat is secreted by sweat glands in the dermis."
        );

        questions.add("Which structure anchors the epidermis to the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hemidesmosomes", // Correct answer
                "Dermal papillae",
                "Hair follicles",
                "Sebaceous glands"
        )));
        correctAnswers.add("Hemidesmosomes");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Hemidesmosomes (Correct answer)\n" +
                        "Hemidesmosomes are protein complexes in the basement membrane that anchor the basal cells of the epidermis to the underlying dermis.\n\n" +
                        "Dermal papillae (Incorrect)\n" +
                        "Dermal papillae interlock with the epidermis to increase surface area and strength but are not the primary anchoring structures.\n\n" +
                        "Hair follicles (Incorrect)\nHair follicles are accessory structures that produce hair, not anchor the epidermis to the dermis.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands secrete sebum, not anchor skin layers."
        );

        questions.add("What is the primary cause of skin aging and wrinkles?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Loss of collagen and elastin", // Correct answer
                "Overproduction of sebum",
                "Excessive melanin synthesis",
                "Increased keratinocyte division"
        )));
        correctAnswers.add("Loss of collagen and elastin");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Loss of collagen and elastin (Correct answer)\n" +
                        "Aging reduces collagen and elastin production in the dermis, leading to decreased skin strength, elasticity, and the formation of wrinkles.\n\n" +
                        "Overproduction of sebum (Incorrect)\n" +
                        "Excess sebum contributes to acne, not skin aging or wrinkles.\n\n" +
                        "Excessive melanin synthesis (Incorrect)\n" +
                        "Excess melanin causes hyperpigmentation (e.g., age spots), not wrinkles.\n\n" +
                        "Increased keratinocyte division (Incorrect)\n" +
                        "Increased keratinocyte division is associated with conditions like psoriasis, not aging."
        );

        questions.add("Which type of burn affects both the epidermis and the entire dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Third-degree burn", // Correct answer
                "First-degree burn",
                "Second-degree burn",
                "Fourth-degree burn"
        )));
        correctAnswers.add("Third-degree burn");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Third-degree burn (Correct answer)\n" +
                        "Third-degree burns destroy the epidermis and the entire dermis, often appearing white or charred and requiring medical intervention.\n\n" +
                        "First-degree burn (Incorrect)\n" +
                        "First-degree burns affect only the epidermis, causing redness and pain (e.g., mild sunburn).\n\n" +
                        "Second-degree burn (Incorrect)\n" +
                        "Second-degree burns affect the epidermis and part of the dermis, causing blisters and severe pain.\n\n" +
                        "Fourth-degree burn (Incorrect)\n" +
                        "Fourth-degree burns extend beyond the skin into underlying tissues like muscle or bone, but specifies damage to the epidermis and dermis, making third-degree the correct choice."
        );

        questions.add("What is the outermost layer of the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stratum corneum", // Correct answer
                "Stratum basale",
                "Stratum spinosum",
                "Stratum granulosum"
        )));
        correctAnswers.add("Stratum corneum");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Stratum corneum (Correct answer)\n" +
                        "The stratum corneum is the outermost layer of the epidermis, composed of dead, keratinized cells that form a protective barrier.\n\n" +
                        "Stratum basale (Incorrect)\n" +
                        "The stratum basale is the deepest layer of the epidermis, where cell division occurs.\n\n" +
                        "Stratum spinosum (Incorrect)\n" +
                        "The stratum spinosum lies above the stratum basale and provides strength but is not the outermost layer.\n\n" +
                        "Stratum granulosum (Incorrect)\n" +
                        "The stratum granulosum lies below the stratum corneum and contains cells producing keratin and lipids."
        );

        questions.add("Which pigment gives skin its color?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanin", // Correct answer
                "Keratin",
                "Hemoglobin",
                "Collagen"
        )));
        correctAnswers.add("Melanin");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Melanin (Correct answer)\n" +
                        "Melanin, produced by melanocytes, is the primary pigment responsible for skin, hair, and eye color, absorbing UV radiation.\n\n" +
                        "Keratin (Incorrect)\n" +
                        "Keratin is a structural protein in the epidermis, hair, and nails, not a pigment.\n\n" +
                        "Hemoglobin (Incorrect)\n" +
                        "Hemoglobin in blood vessels contributes to the pinkish hue of light skin but is not the primary skin pigment.\n\n" +
                        "Collagen (Incorrect)\n" +
                        "Collagen is a structural protein in the dermis, not a pigment."
        );

        questions.add("Which type of gland is found in the ear canal and produces earwax?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ceruminous gland", // Correct answer
                "Eccrine sweat gland",
                "Apocrine sweat gland",
                "Sebaceous gland"
        )));
        correctAnswers.add("Ceruminous gland");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Ceruminous gland (Correct answer)\n" +
                        "Ceruminous glands, modified apocrine glands in the ear canal, produce cerumen (earwax) to protect the ear from debris and pathogens.\n\n" +
                        "Eccrine sweat gland (Incorrect)\n" +
                        "Eccrine sweat glands produce watery sweat for thermoregulation and are not located in the ear canal.\n\n" +
                        "Apocrine sweat gland (Incorrect)\n" +
                        "Apocrine sweat glands produce thicker sweat in areas like the armpits, not earwax.\n\n" +
                        "Sebaceous gland (Incorrect)\n" +
                        "Sebaceous glands secrete sebum to lubricate skin and hair, not earwax."
        );

        questions.add("What is the primary function of the stratum lucidum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Provide a barrier against water loss", // Correct answer
                "Produce melanin for UV protection",
                "Synthesize keratin for cell strength",
                "Facilitate cell division"
        )));
        correctAnswers.add("Provide a barrier against water loss");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Provide a barrier against water loss (Correct answer)\n" +
                        "The stratum lucidum, found in thick skin (e.g., palms, soles), is a clear layer of dead cells that enhances the epidermis’s barrier function, preventing water loss.\n\n" +
                        "Produce melanin for UV protection (Incorrect)\n" +
                        "Melanin is produced by melanocytes in the stratum basale, not the stratum lucidum.\n\n" +
                        "Synthesize keratin for cell strength (Incorrect)\n" +
                        "Keratin synthesis primarily occurs in the stratum granulosum, not the stratum lucidum.\n\n" +
                        "Facilitate cell division (Incorrect)\n" +
                        "Cell division occurs in the stratum basale, not the stratum lucidum."
        );

        questions.add("Which receptor in the skin is responsible for detecting deep pressure and vibration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pacinian corpuscles", // Correct answer
                "Meissner’s corpuscles",
                "Merkel’s disks",
                "Free nerve endings"
        )));
        correctAnswers.add("Pacinian corpuscles");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Pacinian corpuscles (Correct answer)\n" +
                        "Pacinian corpuscles, located deep in the dermis or hypodermis, are mechanoreceptors that detect deep pressure and high-frequency vibrations.\n\n" +
                        "Meissner’s corpuscles (Incorrect)\n" +
                        "Meissner’s corpuscles, located in the papillary dermis, detect light touch and low-frequency vibrations.\n\n" +
                        "Merkel’s disks (Incorrect)\n" +
                        "Merkel’s disks, found in the epidermis, detect sustained touch and pressure, not deep pressure or vibration.\n\n" +
                        "Free nerve endings (Incorrect)\n" +
                        "Free nerve endings detect pain, temperature, and some touch but are not specialized for deep pressure or vibration."
        );

        questions.add("What is the primary role of sweat in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cool the body", // Correct answer
                "Lubricate the skin",
                "Protect against UV radiation",
                "Provide structural support"
        )));
        correctAnswers.add("Cool the body");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Cool the body (Correct answer)\n" +
                        "Sweat, primarily from eccrine glands, evaporates on the skin surface, removing heat and cooling the body during thermoregulation.\n\n" +
                        "Lubricate the skin (Incorrect)\n" +
                        "Sebum from sebaceous glands lubricates the skin, not sweat.\n\n" +
                        "Protect against UV radiation (Incorrect)\n" +
                        "Melanin protects against UV radiation, not sweat.\n\n" +
                        "Provide structural support (Incorrect)\n" +
                        "Structural support is provided by collagen and elastin in the dermis, not sweat."
        );

        questions.add("Which skin condition results from the destruction of melanocytes, leading to depigmented patches?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Vitiligo", // Correct answer
                "Psoriasis",
                "Eczema",
                "Acne"
        )));
        correctAnswers.add("Vitiligo");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Vitiligo (Correct answer)\n" +
                        "Vitiligo is an autoimmune disorder where melanocytes are destroyed, resulting in white, depigmented patches on the skin.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition causing rapid skin cell turnover and scaly plaques, not depigmentation.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema involves inflamed, itchy skin, often due to allergens, not melanocyte destruction.\n\n" +
                        "Acne (Incorrect)\n" +
                        "Acne results from clogged hair follicles, not melanocyte loss."
        );

        questions.add("What is the primary source of energy for the synthesis of vitamin D in the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ultraviolet B (UVB) radiation", // Correct answer
                "Infrared radiation",
                "Visible light",
                "Gamma radiation"
        )));
        correctAnswers.add("Ultraviolet B (UVB) radiation");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Ultraviolet B (UVB) radiation (Correct answer)\n" +
                        "UVB radiation penetrates the epidermis, converting 7-dehydrocholesterol into previtamin D3, which becomes vitamin D3 (cholecalciferol).\n\n" +
                        "Infrared radiation (Incorrect)\n" +
                        "Infrared radiation contributes to heat sensation but does not drive vitamin D synthesis.\n\n" +
                        "Visible light (Incorrect)\n" +
                        "Visible light affects circadian rhythms but does not contribute to vitamin D production.\n\n" +
                        "Gamma radiation (Incorrect): Gamma radiation is harmful and does not play a role in vitamin D synthesis."
        );

        questions.add("Which structure in the hair follicle is responsible for hair growth?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hair bulb", // Correct answer
                "Hair shaft",
                "Arrector pili muscle",
                "Sebaceous gland"
        )));
        correctAnswers.add("Hair bulb");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Hair bulb (Correct answer)\n" +
                        "The hair bulb, located at the base of the hair follicle, contains the hair matrix, where actively dividing cells produce the hair shaft, driving hair growth.\n\n" +
                        "Hair shaft (Incorrect)\n" +
                        "The hair shaft is the visible, dead portion of the hair above the skin, not responsible for growth.\n\n" +
                        "Arrector pili muscle (Incorrect)\n" +
                        "The arrector pili muscle causes hair to stand upright but does not contribute to hair growth.\n\n" +
                        "Sebaceous gland (Incorrect)\n" +
                        "Sebaceous glands secrete sebum to lubricate hair but do not produce hair."
        );

        questions.add("Which type of skin cancer originates from melanocytes and is the most dangerous?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanoma", // Correct answer
                "Basal cell carcinoma",
                "Squamous cell carcinoma",
                "Merkel cell carcinoma"
        )));
        correctAnswers.add("Melanoma");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Melanoma (Correct answer)\n" +
                        "Melanoma arises from melanocytes and is the most dangerous skin cancer due to its high potential for metastasis and rapid spread.\n\n" +
                        "Basal cell carcinoma (Incorrect)\n" +
                        "Basal cell carcinoma arises from basal cells in the epidermis and is the most common but least aggressive skin cancer.\n\n" +
                        "Squamous cell carcinoma (Incorrect)\n" +
                        "Squamous cell carcinoma originates from squamous cells in the epidermis and can metastasize but is less deadly than melanoma.\n\n" +
                        "Merkel cell carcinoma (Incorrect)\n" +
                        "Merkel cell carcinoma is a rare, aggressive cancer from Merkel cells, but it is less common than melanoma."
        );

        questions.add("Which layer of the skin is also known as the subcutaneous layer?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypodermis", // Correct answer
                "Epidermis",
                "Dermis",
                "Stratum corneum"
        )));
        correctAnswers.add("Hypodermis");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Hypodermis (Correct answer)\n" +
                        "The hypodermis, also called the subcutaneous layer, is the deepest layer, composed of adipose tissue and loose connective tissue, providing insulation and cushioning.\n\n" +
                        "Epidermis (Incorrect)\n" +
                        "The epidermis is the outermost layer of the skin, composed of stratified squamous epithelium.\n\n" +
                        "Dermis (Incorrect)\n" +
                        "The dermis lies beneath the epidermis and contains connective tissue, blood vessels, and glands.\n\n" +
                        "Stratum corneum (Incorrect)\n" +
                        "The stratum corneum is the outermost layer of the epidermis, not the subcutaneous layer."
        );

        questions.add("What is the primary function of nails in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Protection of fingertips", // Correct answer
                "Thermoregulation",
                "Secretion of sebum",
                "Synthesis of melanin"
        )));
        correctAnswers.add("Protection of fingertips");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Protection of fingertips (Correct answer)\n" +
                        "Nails, made of keratin, protect the sensitive tips of fingers and toes from injury and enhance dexterity.\n\n" +
                        "Thermoregulation (Incorrect)\n" +
                        "Thermoregulation is managed by sweat glands and dermal blood vessels, not nails.\n\n" +
                        "Secretion of sebum (Incorrect)\n" +
                        "Sebum is secreted by sebaceous glands, not nails.\n\n" +
                        "Synthesis of melanin (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes in the epidermis, not nails."
        );

        questions.add("Which type of epithelial tissue forms the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stratified squamous epithelium", // Correct answer
                "Simple squamous epithelium",
                "Simple cuboidal epithelium",
                "Transitional epithelium"
        )));
        correctAnswers.add("Stratified squamous epithelium");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Stratified squamous epithelium (Correct answer)\n" +
                        "The epidermis is composed of stratified squamous epithelium, with multiple layers of cells that flatten as they move toward the surface, providing a durable barrier.\n\n" +
                        "Simple squamous epithelium (Incorrect)\n" +
                        "Simple squamous epithelium is found in areas like alveoli or blood vessels, not the epidermis.\n\n" +
                        "Simple cuboidal epithelium (Incorrect)\n" +
                        "Simple cuboidal epithelium is found in glandular tissue or kidney tubules, not the epidermis.\n\n" +
                        "Transitional epithelium (Incorrect)\n" +
                        "Transitional epithelium lines the urinary bladder, allowing stretching, not found in the epidermis."
        );

        questions.add("What is the primary source of nutrients for the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diffusion from dermal blood vessels", // Correct answer
                "Blood vessels in the epidermis",
                "Sweat glands in the dermis",
                "Sebaceous glands in the hypodermis"
        )));
        correctAnswers.add("Diffusion from dermal blood vessels");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Diffusion from dermal blood vessels (Correct answer)\n" +
                        "The epidermis receives nutrients and oxygen via diffusion from blood vessels in the underlying dermis, particularly in the papillary layer.\n\n" +
                        "Blood vessels in the epidermis (Incorrect)\n" +
                        "The epidermis is avascular, meaning it contains no blood vessels.\n\n" +
                        "Sweat glands in the dermis (Incorrect)\n" +
                        "Sweat glands produce sweat for thermoregulation, not nutrients for the epidermis.\n\n" +
                        "Sebaceous glands in the hypodermis (Incorrect)\n" +
                        "Sebaceous glands secrete sebum, and most are in the dermis, not the hypodermis; they do not provide nutrients."
        );

        questions.add("Which enzyme is involved in the crosslinking of keratin fibers in the stratum corneum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Transglutaminase", // Correct answer
                "Tyrosinase",
                "Collagenase",
                "Elastase"
        )));
        correctAnswers.add("Transglutaminase");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Transglutaminase (Correct answer)\n" +
                        "Transglutaminase is an enzyme in the epidermis that catalyzes the crosslinking of keratin fibers, strengthening the stratum corneum’s protective barrier.\n\n" +
                        "Tyrosinase (Incorrect)\n" +
                        "Tyrosinase is involved in melanin synthesis in melanocytes, not keratin crosslinking.\n\n" +
                        "Collagenase (Incorrect)\n" +
                        "Collagenase breaks down collagen in the dermis during tissue remodeling, not keratin in the epidermis.\n\n" +
                        "Elastase (Incorrect)\n" +
                        "Elastase degrades elastin in the dermis, not keratin in the stratum corneum."
        );

        questions.add("Which part of the hair is visible above the skin surface?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hair shaft", // Correct answer
                "Hair bulb",
                "Hair root",
                "Hair matrix"
        )));
        correctAnswers.add("Hair shaft");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Hair shaft (Correct answer)\n" +
                        "The hair shaft is the dead, keratinized portion of the hair that extends above the skin surface, forming the visible hair.\n\n" +
                        "Hair bulb (Incorrect)\n" +
                        "The hair bulb is the base of the hair follicle, located below the skin, where hair growth occurs.\n\n" +
                        "Hair root (Incorrect)\n" +
                        "The hair root is the portion of the hair embedded in the skin, not visible.\n\n" +
                        "Hair matrix (Incorrect)\n" +
                        "The hair matrix, within the hair bulb, contains dividing cells that produce the hair but is not visible."
        );

        questions.add("Which skin condition is caused by a bacterial infection of hair follicles or sebaceous glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Folliculitis", // Correct answer
                "Psoriasis",
                "Vitiligo",
                "Melanoma"
        )));
        correctAnswers.add("Folliculitis");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Folliculitis (Correct answer)\n" +
                        "Folliculitis is an infection of the hair follicles or sebaceous glands, often caused by bacteria like Staphylococcus aureus, leading to inflamed, pus-filled bumps.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition causing rapid skin cell turnover, not a bacterial infection.\n\n" +
                        "Vitiligo (Incorrect)\n" +
                        "Vitiligo is an autoimmune disorder causing loss of melanocytes, not an infection.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma is a malignant skin cancer, not a bacterial infection."
        );

        questions.add("Which factor contributes most significantly to the tensile strength of the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Collagen fibers", // Correct answer
                "Keratin filaments",
                "Elastic fibers",
                "Reticular fibers"
        )));
        correctAnswers.add("Collagen fibers");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Collagen fibers (Correct answer)\n" +
                        "Collagen fibers, abundant in the dermis (especially in the reticular layer), provide tensile strength, resisting stretching and tearing.\n\n" +
                        "Keratin filaments (Incorrect)\n" +
                        "Keratin filaments provide strength in the epidermis, hair, and nails, not the dermis.\n\n" +
                        "Elastic fibers (Incorrect)\n" +
                        "Elastic fibers provide elasticity, allowing the skin to recoil, but contribute less to tensile strength.\n\n" +
                        "Reticular fibers (Incorrect)\n" +
                        "Reticular fibers, a type of collagen, form a supportive network but are less significant for tensile strength than thick collagen fibers."
        );

        questions.add("What is the primary clinical feature of a first-degree burn?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Redness and mild pain", // Correct answer
                "Blisters and severe pain",
                "White or charred skin",
                "Destruction of underlying muscle"
        )));
        correctAnswers.add("Redness and mild pain");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Redness and mild pain (Correct answer)\n" +
                        "First-degree burns affect only the epidermis, causing redness, mild pain, and swelling (e.g., mild sunburn).\n\n" +
                        "Blisters and severe pain (Incorrect)\n" +
                        "These are characteristic of second-degree burns, which affect the epidermis and part of the dermis.\n\n" +
                        "White or charred skin (Incorrect)\n" +
                        "This describes third-degree burns, which destroy the epidermis and entire dermis.\n\n" +
                        "Destruction of underlying muscle (Incorrect)\n" +
                        "This occurs in fourth-degree burns, which extend beyond the skin."
        );

        questions.add("Which type of skin cancer is most likely to metastasize to distant sites?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanoma", // Correct answer
                "Basal cell carcinoma",
                "Squamous cell carcinoma",
                "Actinic keratosis"
        )));
        correctAnswers.add("Melanoma");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Melanoma (Correct answer)\n" +
                        "Melanoma, arising from melanocytes, is the most aggressive skin cancer with a high likelihood of metastasis to lymph nodes and distant organs.\n\n" +
                        "Basal cell carcinoma (Incorrect)\n" +
                        "Basal cell carcinoma, the most common skin cancer, rarely metastasizes but can cause local tissue destruction.\n\n" +
                        "Squamous cell carcinoma (Incorrect)\n" +
                        "Squamous cell carcinoma can metastasize, but less frequently and aggressively than melanoma.\n\n" +
                        "Actinic keratosis (Incorrect)\n" +
                        "Actinic keratosis is a precancerous lesion, not a malignant cancer, and does not metastasize."
        );

        questions.add("Which type of cell in the epidermis produces keratin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Keratinocytes", // Correct answer
                "Melanocytes",
                "Langerhans cells",
                "Merkel cells"
        )));
        correctAnswers.add("Keratinocytes");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Keratinocytes (Correct answer)\n" +
                        "Keratinocytes, the most abundant cells in the epidermis, produce keratin, a tough protein that provides structural strength and forms the basis of the stratum corneum.\n\n" +
                        "Melanocytes (Incorrect)\n" +
                        "Melanocytes produce melanin for pigmentation and UV protection, not keratin.\n\n" +
                        "Langerhans cells (Incorrect)\n" +
                        "Langerhans cells are involved in immune surveillance, not keratin production.\n\n" +
                        "Merkel cells (Incorrect)\n" +
                        "Merkel cells are mechanoreceptors for touch sensation, not keratin producers."
        );

        questions.add("What is the primary function of the papillary layer of the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increase surface area for nutrient exchange", // Correct answer
                "Provide tensile strength",
                "Store adipose tissue",
                "Produce sebum for lubrication"
        )));
        correctAnswers.add("Increase surface area for nutrient exchange");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Increase surface area for nutrient exchange (Correct answer)\n" +
                        "The papillary layer, with its dermal papillae, interlocks with the epidermis, increasing surface area for nutrient diffusion from dermal blood vessels to the avascular epidermis.\n\n" +
                        "Provide tensile strength (Incorrect)\n" +
                        "Tensile strength is primarily provided by collagen in the reticular layer of the dermis.\n\n" +
                        "Store adipose tissue (Incorrect)\n" +
                        "Adipose tissue is stored in the hypodermis, not the papillary layer.\n\n" +
                        "Produce sebum for lubrication (Incorrect)\n" +
                        "Sebum is produced by sebaceous glands, mostly in the reticular layer, not the papillary layer."
        );

        questions.add("Which structure determines the texture and color of hair?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hair cortex", // Correct answer
                "Hair shaft",
                "Hair bulb",
                "Arrector pili muscle"
        )));
        correctAnswers.add("Hair cortex");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Hair cortex (Correct answer)\n" +
                        "The hair cortex, the middle layer of the hair shaft, contains keratin and melanin, determining hair texture (straight, wavy, curly) and color (via melanin concentration).\n\n" +
                        "Hair shaft (Incorrect)\n" +
                        "The hair shaft is the visible, dead portion of the hair but does not determine its properties.\n\n" +
                        "Hair bulb (Incorrect)\n" +
                        "The hair bulb contains the matrix for hair growth but does not directly determine texture or color.\n\n" +
                        "Arrector pili muscle (Incorrect)\n" +
                        "The arrector pili muscle causes hair to stand upright but does not affect texture or color."
        );

        questions.add("What is the primary cause of goosebumps on the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Contraction of arrector pili muscles", // Correct answer
                "Contraction of dermal blood vessels",
                "Secretion of sweat glands",
                "Loss of melanin in hair follicles"
        )));
        correctAnswers.add("Contraction of arrector pili muscles");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Contraction of arrector pili muscles (Correct answer)\n" +
                        "Arrector pili muscles, attached to hair follicles, contract in response to cold or fear, pulling hairs upright and causing goosebumps.\n\n" +
                        "Contraction of dermal blood vessels (Incorrect)\n" +
                        "Blood vessel constriction conserves heat but does not cause goosebumps.\n\n" +
                        "Secretion of sweat glands (Incorrect)\n" +
                        "Sweat gland secretion aids cooling, not goosebumps.\n\n" +
                        "Loss of melanin in hair follicles (Incorrect)\n" +
                        "Melanin loss causes graying of hair, not goosebumps."
        );

        questions.add("Which skin condition is characterized by inflamed, itchy patches often triggered by allergens?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eczema", // Correct answer
                "Psoriasis",
                "Melanoma",
                "Folliculitis"
        )));
        correctAnswers.add("Eczema");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Eczema (Correct answer)\n" +
                        "Eczema (atopic dermatitis) is characterized by inflamed, itchy patches, often triggered by allergens, irritants, or stress, with a genetic predisposition.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition causing scaly plaques, not primarily allergen-triggered.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma is a malignant skin cancer, not an inflammatory condition.\n\n" +
                        "Folliculitis (Incorrect)\n" +
                        "Folliculitis is a bacterial infection of hair follicles, not typically allergen-related."
        );

        questions.add("Which component of the skin is most affected by a second-degree burn?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epidermis and part of the dermis", // Correct answer
                "Epidermis only",
                "Epidermis and entire dermis",
                "Epidermis, dermis, and hypodermis"
        )));
        correctAnswers.add("Epidermis and part of the dermis");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Epidermis and part of the dermis (Correct answer)\n" +
                        "Second-degree burns affect the epidermis and the upper dermis (papillary layer), causing blisters, severe pain, and swelling.\n\n" +
                        "Epidermis only (Incorrect)\n" +
                        "Epidermis only damage occurs in first-degree burns, causing redness and mild pain.\n\n" +
                        "Epidermis and entire dermis (Incorrect)\n" +
                        "Entire dermis damage occurs in third-degree burns, appearing white or charred.\n\n" +
                        "Epidermis, dermis, and hypodermis (Incorrect)\n" +
                        "Damage extending to the hypodermis is characteristic of fourth-degree burns."
        );

        questions.add("What is the primary role of elastic fibers in the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Allow skin to stretch and recoil", // Correct answer
                "Provide tensile strength",
                "Anchor the epidermis to the dermis",
                "Produce keratin for protection"
        )));
        correctAnswers.add("Allow skin to stretch and recoil");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Allow skin to stretch and recoil (Correct answer)\n" +
                        "Elastic fibers in the dermis provide elasticity, allowing the skin to stretch during movement and recoil to its original shape.\n\n" +
                        "Provide tensile strength (Incorrect)\n" +
                        "Tensile strength is primarily provided by collagen fibers in the dermis.\n\n" +
                        "Anchor the epidermis to the dermis (Incorrect)\n" +
                        "Hemidesmosomes and dermal papillae anchor the epidermis to the dermis.\n\n" +
                        "Produce keratin for protection (Incorrect)\n" +
                        "Keratin is produced by keratinocytes in the epidermis, not elastic fibers."
        );

        questions.add("Which type of gland is associated with hair follicles and secretes an oily substance?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sebaceous gland", // Correct answer
                "Eccrine sweat gland",
                "Apocrine sweat gland",
                "Ceruminous gland"
        )));
        correctAnswers.add("Sebaceous gland");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Sebaceous gland (Correct answer)\n" +
                        "Sebaceous glands, typically connected to hair follicles, secrete sebum, an oily substance that lubricates skin and hair.\n\n" +
                        "Eccrine sweat gland (Incorrect)\n" +
                        "Eccrine sweat glands produce watery sweat for thermoregulation and are not associated with hair follicles.\n\n" +
                        "Apocrine sweat gland (Incorrect)\n" +
                        "Apocrine sweat glands produce thicker sweat in areas like the armpits, often associated with hair follicles, but do not secrete oil.\n\n" +
                        "Ceruminous gland (Incorrect)\n" +
                        "Ceruminous glands produce earwax in the ear canal, not associated with hair follicles."
        );

        questions.add("Which skin receptor is responsible for detecting pain and temperature?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Free nerve endings", // Correct answer
                "Meissner’s corpuscles",
                "Pacinian corpuscles",
                "Merkel’s disks"
        )));
        correctAnswers.add("Free nerve endings");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Free nerve endings (Correct answer)\n" +
                        "Free nerve endings, unencapsulated sensory receptors in the dermis, detect pain, temperature, and some touch stimuli.\n\n" +
                        "Meissner’s corpuscles (Incorrect)\n" +
                        "Meissner’s corpuscles detect light touch and low-frequency vibrations.\n\n" +
                        "Pacinian corpuscles (Incorrect)\n" +
                        "Pacinian corpuscles detect deep pressure and high-frequency vibrations.\n\n" +
                        "Merkel’s disks (Incorrect)\n" +
                        "Merkel’s disks detect sustained touch and pressure, not pain or temperature."
        );

        questions.add("What is the primary precursor molecule for vitamin D synthesis in the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "7-dehydrocholesterol", // Correct answer
                "Tyrosine",
                "Tryptophan",
                "Cholesterol sulfate"
        )));
        correctAnswers.add("7-dehydrocholesterol");
        rationales.put(49,
                "RATIONALE:\n" +
                        "7-dehydrocholesterol (Correct answer)\n" +
                        "7-dehydrocholesterol in the epidermis is converted to pre-vitamin D3 by UVB radiation, which then becomes vitamin D3 (cholecalciferol).\n\n" +
                        "Tyrosine (Incorrect)\n" +
                        "Tyrosine is a precursor for melanin synthesis, not vitamin D.\n\n" +
                        "Tryptophan (Incorrect)\n" +
                        "Tryptophan is involved in serotonin synthesis, not vitamin D.\n\n" +
                        "Cholesterol sulfate (Incorrect)\n" +
                        "Cholesterol sulfate is not a precursor for vitamin D synthesis."
        );

        questions.add("Which layer of the epidermis contains cells that produce lipid-rich substances to prevent water loss?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stratum basale",
                "Stratum spinosum",
                "Stratum granulosum", // Correct answer
                "Stratum corneum"
        )));
        correctAnswers.add("Stratum granulosum");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Stratum granulosum (Correct answer)\n" +
                        "The stratum granulosum contains cells that produce lipid-rich substances (e.g., lamellar bodies) that contribute to the skin’s waterproof barrier.\n\n" +
                        "Stratum basale (Incorrect)\n" +
                        "The stratum basale is the deepest layer where cell division occurs, not lipid production.\n\n" +
                        "Stratum spinosum (Incorrect)\n" +
                        "The stratum spinosum provides strength through desmosomes but does not primarily produce lipids.\n\n" +
                        "Stratum corneum (Incorrect)\n" +
                        "The stratum corneum is composed of dead, keratinized cells that utilize lipids from the stratum granulosum but does not produce them."
        );

        questions.add("What is the primary function of cerumen produced by ceruminous glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cool the body",
                "Protect the ear canal", // Correct answer
                "Lubricate hair follicles",
                "Synthesize vitamin D"
        )));
        correctAnswers.add("Protect the ear canal");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Protect the ear canal (Correct answer)\n" +
                        "Cerumen (earwax), produced by ceruminous glands in the ear canal, traps debris, repels water, and has antimicrobial properties to protect the ear.\n\n" +
                        "Cool the body (Incorrect)\n" +
                        "Cooling is achieved by eccrine sweat glands, not ceruminous glands.\n\n" +
                        "Lubricate hair follicles (Incorrect)\n" +
                        "Sebum from sebaceous glands lubricates hair follicles, not cerumen.\n\n" +
                        "Synthesize vitamin D (Incorrect)\n" +
                        "Vitamin D is synthesized in the epidermis, not by ceruminous glands."
        );

        questions.add("Which structure in the dermis provides the skin with elasticity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Collagen fibers",
                "Elastic fibers", // Correct answer
                "Reticular fibers",
                "Keratin filaments"
        )));
        correctAnswers.add("Elastic fibers");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Elastic fibers (Correct answer)\n" +
                        "Elastic fibers, found in the dermis, allow the skin to stretch and recoil, contributing to its elasticity.\n\n" +
                        "Collagen fibers (Incorrect)\n" +
                        "Collagen fibers provide tensile strength, resisting stretching and tearing.\n\n" +
                        "Reticular fibers (Incorrect)\n" +
                        "Reticular fibers form a supportive network but are less significant for elasticity.\n\n" +
                        "Keratin filaments (Incorrect)\n" +
                        "Keratin filaments are in the epidermis and hair, not the dermis, and provide structural strength, not elasticity."
        );

        questions.add("What is the primary cause of skin discoloration in vitiligo?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Overproduction of melanin",
                "Loss of melanocytes", // Correct answer
                "Excessive keratin production",
                "Bacterial infection"
        )));
        correctAnswers.add("Loss of melanocytes");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Loss of melanocytes (Correct answer)\n" +
                        "Vitiligo is an autoimmune condition where melanocytes, the cells that produce melanin, are destroyed, leading to depigmented (white) patches.\n\n" +
                        "Overproduction of melanin (Incorrect)\n" +
                        "Overproduction of melanin causes hyperpigmentation, not vitiligo.\n\n" +
                        "Excessive keratin production (Incorrect)\n" +
                        "Excessive keratin is associated with conditions like psoriasis, not vitiligo.\n\n" +
                        "Bacterial infection (Incorrect)\n" +
                        "Bacterial infections cause conditions like folliculitis, not depigmentation."
        );

        questions.add("Which type of burn is characterized by blisters and severe pain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "First-degree burn",
                "Second-degree burn", // Correct answer
                "Third-degree burn",
                "Fourth-degree burn"
        )));
        correctAnswers.add("Second-degree burn");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Second-degree burn (Correct answer)\n" +
                        "Second-degree burns affect the epidermis and part of the dermis, causing blisters, severe pain, and swelling due to fluid leakage.\n\n" +
                        "First-degree burn (Incorrect)\n" +
                        "First-degree burns affect only the epidermis, causing redness and mild pain, not blisters.\n\n" +
                        "Third-degree burn (Incorrect)\n" +
                        "Third-degree burns destroy the epidermis and entire dermis, often appearing white or charred with little to no pain due to nerve damage.\n\n" +
                        "Fourth-degree burn (Incorrect)\n" +
                        "Fourth-degree burns extend into underlying tissues (e.g., muscle, bone), causing severe damage but not typically blisters."
        );

        questions.add("Which cell type in the epidermis is most abundant?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanocytes",
                "Keratinocytes", // Correct answer
                "Langerhans cells",
                "Merkel cells"
        )));
        correctAnswers.add("Keratinocytes");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Keratinocytes (Correct answer)\n" +
                        "Keratinocytes are the most abundant cells in the epidermis (about 90%), producing keratin and forming the skin’s protective barrier.\n\n" +
                        "Melanocytes (Incorrect)\n" +
                        "Melanocytes produce melanin but make up a small percentage of epidermal cells.\n\n" +
                        "Langerhans cells (Incorrect)\n" +
                        "Langerhans cells are immune cells, less numerous than keratinocytes.\n\n" +
                        "Merkel cells (Incorrect)\n" +
                        "Merkel cells are sensory receptors, also less abundant."
        );

        questions.add("What is the primary role of the hair matrix in the hair follicle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce sebum",
                "Generate new hair cells", // Correct answer
                "Cause hair to stand upright",
                "Protect against UV radiation"
        )));
        correctAnswers.add("Generate new hair cells");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Generate new hair cells (Correct answer)\n" +
                        "The hair matrix, located in the hair bulb, contains rapidly dividing cells that produce the hair shaft, driving hair growth.\n\n" +
                        "Produce sebum (Incorrect)\n" +
                        "Sebum is produced by sebaceous glands, not the hair matrix.\n\n" +
                        "Cause hair to stand upright (Incorrect)\n" +
                        "The arrector pili muscle causes hair to stand upright, not the hair matrix.\n\n" +
                        "Protect against UV radiation (Incorrect)\n" +
                        "Melanin in the hair and skin provides UV protection, not the hair matrix."
        );

        questions.add("Which skin condition is a precancerous lesion caused by chronic sun exposure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Basal cell carcinoma",
                "Actinic keratosis", // Correct answer
                "Melanoma",
                "Squamous cell carcinoma"
        )));
        correctAnswers.add("Actinic keratosis");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Actinic keratosis (Correct answer)\n" +
                        "Actinic keratosis is a rough, scaly patch caused by chronic UV exposure, considered precancerous as it may progress to squamous cell carcinoma.\n\n" +
                        "Basal cell carcinoma (Incorrect)\n" +
                        "Basal cell carcinoma is a malignant cancer, not a precancerous lesion.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma is a malignant cancer, not a precancerous lesion.\n\n" +
                        "Squamous cell carcinoma (Incorrect)\n" +
                        "Squamous cell carcinoma is a malignant cancer that may develop from actinic keratosis but is not precancerous itself."
        );

        questions.add("Which sensory receptor in the skin detects light touch?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pacinian corpuscles",
                "Meissner’s corpuscles", // Correct answer
                "Free nerve endings",
                "Ruffini endings"
        )));
        correctAnswers.add("Meissner’s corpuscles");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Meissner’s corpuscles (Correct answer)\n" +
                        "Meissner’s corpuscles, located in the papillary dermis, are mechanoreceptors that detect light touch and low-frequency vibrations.\n\n" +
                        "Pacinian corpuscles (Incorrect)\n" +
                        "Pacinian corpuscles detect deep pressure and high-frequency vibrations.\n\n" +
                        "Free nerve endings (Incorrect)\n" +
                        "Free nerve endings detect pain, temperature, and some touch but are not specialized for light touch.\n\n" +
                        "Ruffini endings (Incorrect)\n" +
                        "Ruffini endings detect skin stretch and sustained pressure, not light touch."
        );

        questions.add("What is the primary function of the reticular layer of the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Provide a barrier against pathogens",
                "Facilitate nutrient diffusion",
                "Provide strength and elasticity", // Correct answer
                "Produce sweat for cooling"
        )));
        correctAnswers.add("Provide strength and elasticity");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Provide strength and elasticity (Correct answer)\n" +
                        "The reticular layer, composed of dense irregular connective tissue with collagen and elastic fibers, provides the dermis with strength and elasticity.\n\n" +
                        "Provide a barrier against pathogens (Incorrect)\n" +
                        "The epidermis, particularly the stratum corneum, provides the pathogen barrier.\n\n" +
                        "Facilitate nutrient diffusion (Incorrect)\n" +
                        "Nutrient diffusion is facilitated by the papillary layer’s blood vessels.\n\n" +
                        "Produce sweat for cooling (Incorrect)\n" +
                        "Sweat is produced by sweat glands, not the reticular layer itself."
        );

        questions.add("Which type of sweat gland is activated during emotional stress and found in the armpits?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eccrine sweat glands",
                "Apocrine sweat glands", // Correct answer
                "Sebaceous glands",
                "Ceruminous glands"
        )));
        correctAnswers.add("Apocrine sweat glands");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Apocrine sweat glands (Correct answer)\n" +
                        "Apocrine sweat glands, located in areas like the armpits and groin, produce a thicker sweat activated by emotional stress or hormonal changes.\n\n" +
                        "Eccrine sweat glands (Incorrect)\n" +
                        "Eccrine sweat glands are distributed across the body and produce watery sweat for thermoregulation, not primarily stress-related.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands secrete sebum, not sweat, and are not stress-related.\n\n" +
                        "Ceruminous glands (Incorrect)\n" +
                        "Ceruminous glands produce earwax in the ear canal, not sweat."
        );

        questions.add("What is the primary role of the stratum basale in the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Form a waterproof barrier",
                "Produce melanin for pigmentation",
                "Facilitate cell division", // Correct answer
                "Store lipids for insulation"
        )));
        correctAnswers.add("Facilitate cell division");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Facilitate cell division (Correct answer)\n" +
                        "The stratum basale, the deepest epidermal layer, contains stem cells and keratinocytes that continuously divide to replenish the epidermis.\n\n" +
                        "Form a waterproof barrier (Incorrect)\n" +
                        "The waterproof barrier is formed by the stratum corneum and lipids from the stratum granulosum.\n\n" +
                        "Produce melanin for pigmentation (Incorrect)\n" +
                        "Melanin is produced by melanocytes, which are in the stratum basale, but this is not the layer’s primary role.\n\n" +
                        "Store lipids for insulation (Incorrect)\n" +
                        "Lipids are stored in the hypodermis, not the stratum basale."
        );

        questions.add("Which skin structure is responsible for the formation of fingerprints?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sebaceous glands",
                "Dermal papillae", // Correct answer
                "Sweat glands",
                "Hair follicles"
        )));
        correctAnswers.add("Dermal papillae");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Dermal papillae (Correct answer)\n" +
                        "Dermal papillae in the papillary layer of the dermis form ridges that project into the epidermis, creating unique fingerprint patterns.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands secrete sebum, not involved in fingerprint formation.\n\n" +
                        "Sweat glands (Incorrect)\n" +
                        "Sweat glands produce sweat for thermoregulation, not fingerprints.\n\n" +
                        "Hair follicles (Incorrect)\n" +
                        "Hair follicles produce hair, not fingerprints."
        );

        questions.add("What is the primary cause of acne development?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Loss of melanocytes",
                "Overproduction of sebum and clogged follicles", // Correct answer
                "Autoimmune attack on keratinocytes",
                "Destruction of collagen fibers"
        )));
        correctAnswers.add("Overproduction of sebum and clogged follicles");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Overproduction of sebum and clogged follicles (Correct answer)\n" +
                        "Acne develops when sebaceous glands overproduce sebum, which, combined with dead skin cells and bacteria, clogs hair follicles, leading to inflammation.\n\n" +
                        "Loss of melanocytes (Incorrect)\n" +
                        "Loss of melanocytes causes vitiligo, not acne.\n\n" +
                        "Autoimmune attack on keratinocytes (Incorrect)\n" +
                        "Autoimmune attacks on keratinocytes are associated with psoriasis, not acne.\n\n" +
                        "Destruction of collagen fibers (Incorrect)\n" +
                        "Collagen destruction contributes to aging or scarring, not acne."
        );

        questions.add("Which type of skin cancer originates from the stratum basale?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanoma",
                "Squamous cell carcinoma",
                "Basal cell carcinoma", // Correct answer
                "Merkel cell carcinoma"
        )));
        correctAnswers.add("Basal cell carcinoma");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Basal cell carcinoma (Correct answer)\n" +
                        "Basal cell carcinoma originates from basal cells in the stratum basale, the deepest epidermal layer, and is the most common skin cancer.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma originates from melanocytes, not specifically the stratum basale’s basal cells.\n\n" +
                        "Squamous cell carcinoma (Incorrect)\n" +
                        "Squamous cell carcinoma arises from squamous cells in the upper epidermis (e.g., stratum spinosum).\n\n" +
                        "Merkel cell carcinoma (Incorrect)\n" +
                        "Merkel cell carcinoma arises from Merkel cells, not basal cells."
        );

        questions.add("What is the primary function of Merkel cells in the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Produce keratin",
                "Detect light touch", // Correct answer
                "Synthesize melanin",
                "Initiate immune responses"
        )));
        correctAnswers.add("Detect light touch");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Detect light touch (Correct answer)\n" +
                        "Merkel cells, located in the stratum basale, are mechanoreceptors that detect light touch and pressure, contributing to sensory perception.\n\n" +
                        "Produce keratin (Incorrect)\n" +
                        "Keratin is produced by keratinocytes, not Merkel cells.\n\n" +
                        "Synthesize melanin (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes, not Merkel cells.\n\n" +
                        "Initiate immune responses (Incorrect)\n" +
                        "Immune responses are initiated by Langerhans cells, not Merkel cells."
        );

        questions.add("Which component of the nail provides a protective covering for the nail root?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nail bed",
                "Nail matrix",
                "Cuticle", // Correct answer
                "Lunula"
        )));
        correctAnswers.add("Cuticle");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Cuticle (Correct answer)\n" +
                        "The cuticle, or eponychium, is a fold of skin that covers and protects the nail root, preventing pathogen entry.\n\n" +
                        "Nail bed (Incorrect)\n" +
                        "The nail bed is the skin beneath the nail plate, supporting the nail but not covering the root.\n\n" +
                        "Nail matrix (Incorrect)\n" +
                        "The nail matrix is the area where nail growth occurs, not a protective covering.\n\n" +
                        "Lunula (Incorrect)\n" +
                        "The lunula is the white, crescent-shaped area at the nail’s base, not a protective covering."
        );

        questions.add("What is the primary source of blood supply to the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epidermal capillaries",
                "Subcutaneous arteries",
                "Dermal blood vessels", // Correct answer
                "Hypodermal veins"
        )));
        correctAnswers.add("Dermal blood vessels");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Dermal blood vessels (Correct answer)\n" +
                        "The dermis contains a rich network of blood vessels, particularly in the papillary and reticular layers, supplying nutrients and oxygen.\n\n" +
                        "Epidermal capillaries (Incorrect)\n" +
                        "The epidermis is avascular and contains no capillaries.\n\n" +
                        "Subcutaneous arteries (Incorrect)\n" +
                        "Subcutaneous arteries supply the hypodermis, but the dermis has its own vascular network.\n\n" +
                        "Hypodermal veins (Incorrect)\n" +
                        "Hypodermal veins drain blood but are not the primary supply to the dermis."
        );

        questions.add("Which skin condition is associated with thickened, scaly plaques due to rapid keratinocyte turnover?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eczema",
                "Psoriasis", // Correct answer
                "Vitiligo",
                "Acne"
        )));
        correctAnswers.add("Psoriasis");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Psoriasis (Correct answer)\n" +
                        "Psoriasis is an autoimmune condition causing rapid keratinocyte proliferation, leading to thickened, scaly plaques on the skin.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema causes inflamed, itchy patches, not thickened plaques from rapid cell turnover.\n\n" +
                        "Vitiligo (Incorrect)\n" +
                        "Vitiligo results in depigmented patches due to melanocyte loss, not keratinocyte turnover.\n\n" +
                        "Acne (Incorrect)\n" +
                        "Acne involves clogged follicles and inflammation, not rapid keratinocyte turnover."
        );

        questions.add("Which factor most significantly contributes to the skin’s ability to resist abrasion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanin concentration",
                "Keratin in the stratum corneum", // Correct answer
                "Elastic fibers in the dermis",
                "Adipose tissue in the hypodermis"
        )));
        correctAnswers.add("Keratin in the stratum corneum");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Keratin in the stratum corneum (Correct answer)\n" +
                        "Keratin, a tough protein in the stratum corneum, forms a durable barrier that resists abrasion and mechanical damage.\n\n" +
                        "Melanin concentration (Incorrect)\n" +
                        "Melanin protects against UV radiation, not abrasion.\n\n" +
                        "Elastic fibers in the dermis (Incorrect)\n" +
                        "Elastic fibers provide elasticity, not abrasion resistance.\n\n" +
                        "Adipose tissue in the hypodermis (Incorrect)\n" +
                        "Adipose tissue provides cushioning and insulation, not abrasion resistance."
        );

        questions.add("Which type of tissue primarily composes the reticular layer of the dermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dense irregular connective tissue", // Correct answer
                "Stratified squamous epithelium",
                "Simple columnar epithelium",
                "Adipose tissue"
        )));
        correctAnswers.add("Dense irregular connective tissue");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Dense irregular connective tissue (Correct answer)\n" +
                        "The reticular layer of the dermis is primarily composed of dense irregular connective tissue, containing collagen and elastic fibers for strength and elasticity.\n\n" +
                        "Stratified squamous epithelium (Incorrect)\n" +
                        "Stratified squamous epithelium forms the epidermis, not the dermis.\n\n" +
                        "Simple columnar epithelium (Incorrect)\n" +
                        "Simple columnar epithelium is found in areas like the digestive tract, not the dermis.\n\n" +
                        "Adipose tissue (Incorrect)\n" +
                        "Adipose tissue is primarily in the hypodermis, not the reticular layer."
        );

        questions.add("What is the primary function of Langerhans cells in the epidermis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Initiate immune responses", // Correct answer
                "Produce keratin for structural support",
                "Synthesize melanin for UV protection",
                "Detect touch and pressure"
        )));
        correctAnswers.add("Initiate immune responses");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Initiate immune responses (Correct answer)\n" +
                        "Langerhans cells are dendritic cells in the epidermis that act as antigen-presenting cells, initiating immune responses against pathogens.\n\n" +
                        "Produce keratin for structural support (Incorrect)\n" +
                        "Keratin is produced by keratinocytes, not Langerhans cells.\n\n" +
                        "Synthesize melanin for UV protection (Incorrect)\n" +
                        "Melanin is synthesized by melanocytes, not Langerhans cells.\n\n" +
                        "Detect touch and pressure (Incorrect)\n" +
                        "Touch and pressure are detected by Merkel cells and dermal receptors, not Langerhans cells."
        );

        questions.add("Which structure in the skin is responsible for producing the nail plate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nail matrix", // Correct answer
                "Nail bed",
                "Cuticle",
                "Lunula"
        )));
        correctAnswers.add("Nail matrix");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Nail matrix (Correct answer)\n" +
                        "The nail matrix, located beneath the nail root, contains dividing cells that produce the keratinized nail plate.\n\n" +
                        "Nail bed (Incorrect)\n" +
                        "The nail bed is the skin beneath the nail plate, supporting but not producing the nail.\n\n" +
                        "Cuticle (Incorrect)\n" +
                        "The cuticle protects the nail root but does not produce the nail.\n\n" +
                        "Lunula (Incorrect)\n" +
                        "The lunula is the visible white crescent at the nail’s base, not responsible for nail production."
        );

        questions.add("What is the primary cause of skin sagging with age?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Loss of elastic fibers", // Correct answer
                "Overproduction of sebum",
                "Increased melanin synthesis",
                "Excessive sweat production"
        )));
        correctAnswers.add("Loss of elastic fibers");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Loss of elastic fibers (Correct answer)\n" +
                        "Aging reduces elastic fibers in the dermis, decreasing the skin’s ability to recoil, leading to sagging and wrinkles.\n\n" +
                        "Overproduction of sebum (Incorrect)\n" +
                        "Excess sebum contributes to acne, not sagging.\n\n" +
                        "Increased melanin synthesis (Incorrect)\n" +
                        "Increased melanin causes hyperpigmentation, not sagging.\n\n" +
                        "Excessive sweat production (Incorrect)\n" +
                        "Sweat production affects thermoregulation, not skin sagging."
        );

        questions.add("Which type of skin cancer is characterized by scaly, red patches that may bleed?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Squamous cell carcinoma", // Correct answer
                "Basal cell carcinoma",
                "Melanoma",
                "Actinic keratosis"
        )));
        correctAnswers.add("Squamous cell carcinoma");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Squamous cell carcinoma (Correct answer)\n" +
                        "Squamous cell carcinoma often presents as scaly, red patches or sores that may bleed, originating from squamous cells and potentially metastasizing.\n\n" +
                        "Basal cell carcinoma (Incorrect)\n" +
                        "Basal cell carcinoma typically appears as pearly or waxy nodules, not scaly patches.\n\n" +
                        "Melanoma (Incorrect)\n" +
                        "Melanoma appears as asymmetrical, pigmented lesions with irregular borders, not scaly patches.\n\n" +
                        "Actinic keratosis (Incorrect)\n" +
                        "Actinic keratosis is a precancerous lesion with rough, scaly patches but is not yet cancerous."
        );

        questions.add("What is the primary role of the hair cuticle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Protect the hair shaft", // Correct answer
                "Produce hair pigment",
                "Anchor the hair to the dermis",
                "Facilitate hair growth"
        )));
        correctAnswers.add("Protect the hair shaft");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Protect the hair shaft (Correct answer)\n" +
                        "The hair cuticle, the outermost layer of the hair shaft, consists of overlapping keratinized cells that protect the inner layers from damage.\n\n" +
                        "Produce hair pigment (Incorrect)\n" +
                        "Hair pigment (melanin) is produced by melanocytes in the hair matrix, not the cuticle.\n\n" +
                        "Anchor the hair to the dermis (Incorrect)\n" +
                        "The hair root and dermal papilla anchor the hair, not the cuticle.\n\n" +
                        "Facilitate hair growth (Incorrect)\n" +
                        "Hair growth occurs in the hair matrix, not the cuticle."
        );

        questions.add("Which skin receptor is responsible for detecting skin stretch?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ruffini endings", // Correct answer
                "Meissner’s corpuscles",
                "Pacinian corpuscles",
                "Free nerve endings"
        )));
        correctAnswers.add("Ruffini endings");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Ruffini endings (Correct answer)\n" +
                        "Ruffini endings, located in the dermis, detect skin stretch and sustained pressure, contributing to proprioception.\n\n" +
                        "Meissner’s corpuscles (Incorrect)\n" +
                        "Meissner’s corpuscles detect light touch and low-frequency vibrations.\n\n" +
                        "Pacinian corpuscles (Incorrect)\n" +
                        "Pacinian corpuscles detect deep pressure and high-frequency vibrations.\n\n" +
                        "Free nerve endings (Incorrect)\n" +
                        "Free nerve endings detect pain, temperature, and some touch, not skin stretch."
        );

        questions.add("What is the primary function of sebum in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lubricate skin and hair", // Correct answer
                "Cool the body",
                "Protect against UV radiation",
                "Facilitate cell division"
        )));
        correctAnswers.add("Lubricate skin and hair");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Lubricate skin and hair (Correct answer)\n" +
                        "Sebum, secreted by sebaceous glands, lubricates skin and hair, preventing dryness and cracking.\n\n" +
                        "Cool the body (Incorrect)\n" +
                        "Cooling is achieved by sweat from eccrine glands, not sebum.\n\n" +
                        "Protect against UV radiation (Incorrect)\n" +
                        "UV protection is provided by melanin, not sebum.\n\n" +
                        "Facilitate cell division (Incorrect)\n" +
                        "Cell division occurs in the stratum basale and hair matrix, not influenced by sebum."
        );

        questions.add("Which condition results from a fungal infection of the skin, often presenting as a ring-shaped rash?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tinea corporis", // Correct answer
                "Psoriasis",
                "Eczema",
                "Vitiligo"
        )));
        correctAnswers.add("Tinea corporis");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Tinea corporis (Correct answer)\n" +
                        "Tinea corporis, or ringworm, is a fungal infection causing a red, ring-shaped rash with a clear center, commonly on the body.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition causing scaly plaques, not a fungal infection.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema involves inflamed, itchy patches, typically allergen-related, not fungal.\n\n" +
                        "Vitiligo (Incorrect)\n" +
                        "Vitiligo causes depigmented patches due to melanocyte loss, not a fungal infection."
        );

        questions.add("Which factor is most critical for the epidermis to maintain its protective barrier?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Presence of keratinized cells", // Correct answer
                "High melanin concentration",
                "Dense blood vessel network",
                "Thick adipose layer"
        )));
        correctAnswers.add("Presence of keratinized cells");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Presence of keratinized cells (Correct answer)\n" +
                        "Keratinized cells in the stratum corneum, packed with keratin and lipids, form a tough, protective barrier against pathogens, abrasion, and water loss.\n\n" +
                        "High melanin concentration (Incorrect)\n" +
                        "Melanin protects against UV radiation but is not critical for the physical barrier.\n\n" +
                        "Dense blood vessel network (Incorrect)\n" +
                        "Blood vessels in the dermis supply nutrients but do not directly form the barrier.\n\n" +
                        "Thick adipose layer (Incorrect)\n" +
                        "Adipose tissue in the hypodermis provides insulation, not the epidermal barrier."
        );


        questions.add("What is the main purpose of the skin’s sweat glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To help cool the body", // Correct answer
                "To make the skin stronger",
                "To produce skin color",
                "To protect against germs"
        )));
        correctAnswers.add("To help cool the body");
        rationales.put(80,
                "RATIONALE:\n" +
                        "To help cool the body (Correct answer)\n" +
                        "Sweat glands, especially eccrine glands, produce sweat that evaporates, removing heat and cooling the body.\n\n" +
                        "To make the skin stronger (Incorrect)\n" +
                        "Skin strength comes from keratin and collagen, not sweat glands.\n\n" +
                        "To produce skin color (Incorrect)\n" +
                        "Skin color is due to melanin from melanocytes, not sweat glands.\n\n" +
                        "To protect against germs (Incorrect)\n" +
                        "Protection against germs is primarily from the skin’s barrier and immune cells, not sweat, though sweat has some antimicrobial properties."
        );

        questions.add("Which part of the skin helps keep it flexible and stretchy?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Elastic fibers in the dermis", // Correct answer
                "Keratin in the epidermis",
                "Blood vessels in the dermis",
                "Fat in the epidermis"
        )));
        correctAnswers.add("Elastic fibers in the dermis");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Elastic fibers in the dermis (Correct answer)\n" +
                        "Elastic fibers in the dermis allow the skin to stretch and return to its original shape, keeping it flexible.\n\n" +
                        "Keratin in the epidermis (Incorrect)\n" +
                        "Keratin makes the skin tough, not flexible.\n\n" +
                        "Blood vessels in the dermis (Incorrect)\n" +
                        "Blood vessels supply nutrients and help with temperature control, not flexibility.\n\n" +
                        "Fat in the epidermis (Incorrect)\n" +
                        "The epidermis has no fat; fat is in the hypodermis for insulation, not flexibility."
        );

        questions.add("What does the nail bed do in the integumentary system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Supports the nail plate", // Correct answer
                "Grows the nail",
                "Protects the nail root",
                "Makes the nail hard"
        )));
        correctAnswers.add("Supports the nail plate");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Supports the nail plate (Correct answer)\n" +
                        "The nail bed is the skin beneath the nail plate, providing support and blood supply to the nail.\n\n" +
                        "Grows the nail (Incorrect)\n" +
                        "The nail matrix, not the nail bed, grows the nail.\n\n" +
                        "Protects the nail root (Incorrect)\n" +
                        "The cuticle protects the nail root, not the nail bed.\n\n" +
                        "Makes the nail hard (Incorrect)\n" +
                        "Keratin in the nail matrix makes the nail hard, not the nail bed."
        );

        questions.add("Why is melanin important for the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It protects against sunburn", // Correct answer
                "It helps the skin stay hydrated",
                "It makes the skin stretchy",
                "It helps heal wounds"
        )));
        correctAnswers.add("It protects against sunburn");
        rationales.put(83,
                "RATIONALE:\n" +
                        "It protects against sunburn (Correct answer)\n" +
                        "Melanin, produced by melanocytes, absorbs UV rays, protecting the skin from sunburn and UV damage.\n\n" +
                        "It helps the skin stay hydrated (Incorrect)\n" +
                        "Hydration is maintained by lipids and the stratum corneum, not melanin.\n\n" +
                        "It makes the skin stretchy (Incorrect)\n" +
                        "Elastic fibers in the dermis make the skin stretchy, not melanin.\n\n" +
                        "It helps heal wounds (Incorrect)\n" +
                        "Wound healing involves immune cells and fibroblasts, not melanin."
        );

        questions.add("What is the top layer of the skin called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epidermis", // Correct answer
                "Dermis",
                "Hypodermis",
                "Subcutaneous layer"
        )));
        correctAnswers.add("Epidermis");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Epidermis (Correct answer)\n" +
                        "The epidermis is the outermost layer of the skin, providing a protective barrier with keratinized cells.\n\n" +
                        "Dermis (Incorrect)\n" +
                        "The dermis is the middle layer of the skin, containing blood vessels and glands.\n\n" +
                        "Hypodermis (Incorrect)\n" +
                        "The hypodermis is the deepest layer, containing fat and connective tissue.\n\n" +
                        "Subcutaneous layer (Incorrect)\n" +
                        "This is another name for the hypodermis, not the top layer."
        );

        questions.add("What is the oily substance that keeps skin and hair soft?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sebum", // Correct answer
                "Sweat",
                "Melanin",
                "Keratin"
        )));
        correctAnswers.add("Sebum");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Sebum (Correct answer)\n" +
                        "Sebum, produced by sebaceous glands, is an oily substance that lubricates and softens skin and hair.\n\n" +
                        "Sweat (Incorrect)\n" +
                        "Sweat cools the body but does not soften skin or hair.\n\n" +
                        "Melanin (Incorrect)\n" +
                        "Melanin provides color and UV protection, not softening.\n\n" +
                        "Keratin (Incorrect)\n" +
                        "Keratin makes skin and hair tough, not soft."
        );

        questions.add("What is a common sign of a third-degree burn?");
        choices.add(new ArrayList<>(Arrays.asList(
                "White or charred skin", // Correct answer
                "Red, painful skin",
                "Blisters and swelling",
                "Itchy, dry patches"
        )));
        correctAnswers.add("White or charred skin");
        rationales.put(86,
                "RATIONALE:\n" +
                        "White or charred skin (Correct answer)\n" +
                        "Third-degree burns destroy the epidermis and entire dermis, often appearing white, charred, or leathery with little pain due to nerve damage.\n\n" +
                        "Red, painful skin (Incorrect)\n" +
                        "This describes a first-degree burn, affecting only the epidermis.\n\n" +
                        "Blisters and swelling (Incorrect)\n" +
                        "This is typical of a second-degree burn, affecting the epidermis and part of the dermis.\n\n" +
                        "Itchy, dry patches (Incorrect)\n" +
                        "This is more typical of eczema, not a burn."
        );

        questions.add("What part of the skin helps store energy for the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypodermis", // Correct answer
                "Epidermis",
                "Dermis",
                "Stratum corneum"
        )));
        correctAnswers.add("Hypodermis");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Hypodermis (Correct answer)\n" +
                        "The hypodermis, with its adipose tissue, stores fat as an energy reserve for the body.\n\n" +
                        "Epidermis (Incorrect)\n" +
                        "The epidermis is a protective barrier, not an energy storage site.\n\n" +
                        "Dermis (Incorrect)\n" +
                        "The dermis contains connective tissue and glands, not significant energy storage.\n\n" +
                        "Stratum corneum (Incorrect)\n" +
                        "The stratum corneum is the outer layer of dead cells, not involved in energy storage."
        );

        questions.add("What is the white, crescent-shaped area at the base of the nail called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lunula", // Correct answer
                "Cuticle",
                "Nail matrix",
                "Nail bed"
        )));
        correctAnswers.add("Lunula");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Lunula (Correct answer)\n" +
                        "The lunula is the white, crescent-shaped area at the base of the nail, visible through the nail plate.\n\n" +
                        "Cuticle (Incorrect)\n" +
                        "The cuticle is the skin fold protecting the nail root.\n\n" +
                        "Nail matrix (Incorrect)\n" +
                        "The nail matrix is the growth area under the nail, not visible.\n\n" +
                        "Nail bed (Incorrect)\n" +
                        "The nail bed is the skin beneath the nail plate, not the crescent shape."
        );

        questions.add("What skin condition causes red, itchy patches and is often linked to allergies?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eczema", // Correct answer
                "Acne",
                "Psoriasis",
                "Ringworm"
        )));
        correctAnswers.add("Eczema");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Eczema (Correct answer)\n" +
                        "Eczema (atopic dermatitis) causes red, itchy patches, often triggered by allergies or irritants.\n\n" +
                        "Acne (Incorrect)\n" +
                        "Acne involves clogged pores and pimples, not primarily allergy-related.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis causes scaly plaques due to an autoimmune condition, not allergies.\n\n" +
                        "Ringworm (Incorrect)\n" +
                        "Ringworm is a fungal infection causing ring-shaped rashes, not allergy-related."
        );

        questions.add("What is the main job of the hair on the scalp?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To protect the scalp", // Correct answer
                "To cool the head",
                "To produce sweat",
                "To store fat"
        )));
        correctAnswers.add("To protect the scalp");
        rationales.put(90,
                "RATIONALE:\n" +
                        "To protect the scalp (Correct answer)\n" +
                        "Hair on the scalp protects the skin from UV radiation, physical injury, and temperature extremes.\n\n" +
                        "To cool the head (Incorrect)\n" +
                        "Hair does not cool the head; sweat glands handle cooling.\n\n" +
                        "To produce sweat (Incorrect)\n" +
                        "Sweat is produced by sweat glands, not hair.\n\n" +
                        "To store fat (Incorrect)\n" +
                        "Fat is stored in the hypodermis, not hair."
        );

        questions.add("Which layer of the skin contains the most fat?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypodermis", // Correct answer
                "Epidermis",
                "Dermis",
                "Stratum basale"
        )));
        correctAnswers.add("Hypodermis");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Hypodermis (Correct answer)\n" +
                        "The hypodermis, or subcutaneous layer, contains adipose tissue, which is the skin’s primary fat storage area.\n\n" +
                        "Epidermis (Incorrect)\n" +
                        "The epidermis is made of epithelial cells and contains no fat.\n\n" +
                        "Dermis (Incorrect)\n" +
                        "The dermis has connective tissue, blood vessels, and glands but minimal fat.\n\n" +
                        "Stratum basale (Incorrect)\n" +
                        "The stratum basale is an epidermal layer with dividing cells, not fat."
        );

        questions.add("What is the hard, protective part of the nail called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nail plate", // Correct answer
                "Nail bed",
                "Cuticle",
                "Lunula"
        )));
        correctAnswers.add("Nail plate");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Nail plate (Correct answer)\n" +
                        "The nail plate is the hard, keratinized part of the nail that covers the fingertip, providing protection.\n\n" +
                        "Nail bed (Incorrect)\n" +
                        "The nail bed is the skin under the nail, supporting it.\n\n" +
                        "Cuticle (Incorrect)\n" +
                        "The cuticle is the skin fold around the nail root, not the hard part.\n\n" +
                        "Lunula (Incorrect)\n" +
                        "The lunula is the white crescent at the nail’s base, not the hard part."
        );

        questions.add("What does the skin do when exposed to sunlight to grounds for vitamin D production?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Creates vitamin D", // Correct answer
                "Produces sweat",
                "Makes melanin",
                "Grows hair"
        )));
        correctAnswers.add("Creates vitamin D");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Creates vitamin D (Correct answer)\n" +
                        "Sunlight (UVB) triggers the skin to convert 7-dehydrocholesterol in the epidermis into vitamin D3, essential for bone health.\n\n" +
                        "Produces sweat (Incorrect)\n" +
                        "Sweat is produced by sweat glands for cooling, not related to sunlight.\n\n" +
                        "Makes melanin (Incorrect)\n" +
                        "Melanin is produced for UV protection, not vitamin D.\n\n" +
                        "Grows hair (Incorrect)\n" +
                        "Hair growth is not directly linked to sunlight exposure."
        );

        questions.add("What is a common symptom of a first-degree burn?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Red, sore skin", // Correct answer
                "Blisters",
                "Blackened skin",
                "No pain"
        )));
        correctAnswers.add("Red, sore skin");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Red, sore skin (Correct answer)\n" +
                        "First-degree burns affect only the epidermis, causing redness, soreness, and mild swelling, like a mild sunburn.\n\n" +
                        "Blisters (Incorrect)\n" +
                        "Blisters occur in second-degree burns, not first-degree.\n\n" +
                        "Blackened skin (Incorrect)\n" +
                        "Blackened skin indicates a third-degree burn, much deeper.\n\n" +
                        "No pain (Incorrect)\n" +
                        "First-degree burns are painful, unlike deeper burns that may damage nerves."
        );

        questions.add("What is the name of the skin condition that causes pimples?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eczema",
                "Acne", // Correct answer
                "Psoriasis",
                "Vitiligo"
        )));
        correctAnswers.add("Acne");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Acne (Correct answer)\n" +
                        "Acne is caused by clogged pores with sebum, dead skin, and bacteria, leading to pimples and blackheads.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema causes itchy, red patches, not pimples.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis causes scaly, red plaques, not pimples.\n\n" +
                        "Vitiligo (Incorrect)\n" +
                        "Vitiligo causes white patches due to pigment loss, not pimples."
        );

        questions.add("Which part of the skin helps with feeling touch?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sweat glands",
                "Hair follicles",
                "Nerve endings", // Correct answer
                "Sebaceous glands"
        )));
        correctAnswers.add("Nerve endings");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Nerve endings (Correct answer)\n" +
                        "Nerve endings in the skin, including free nerve endings and receptors like Meissner’s corpuscles, detect touch, pressure, and other sensations.\n\n" +
                        "Sweat glands (Incorrect)\n" +
                        "Sweat glands produce sweat for cooling, not sensation.\n\n" +
                        "Hair follicles (Incorrect)\n" +
                        "Hair follicles produce hair and may have associated nerves, but they don’t directly feel touch.\n\n" +
                        "Sebaceous glands (Incorrect)\n" +
                        "Sebaceous glands produce sebum, not involved in sensation."
        );

        questions.add("What is the thin layer of skin that protects the nail’s growth area?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nail bed",
                "Nail matrix",
                "Cuticle", // Correct answer
                "Nail plate"
        )));
        correctAnswers.add("Cuticle");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Cuticle (Correct answer)\n" +
                        "The cuticle, or eponychium, is a thin layer of skin that protects the nail matrix, the area where the nail grows.\n\n" +
                        "Nail bed (Incorrect)\n" +
                        "The nail bed supports the nail plate, not the growth area.\n\n" +
                        "Nail matrix (Incorrect)\n" +
                        "The nail matrix is the growth area itself, not a protective layer.\n\n" +
                        "Nail plate (Incorrect)\n" +
                        "The nail plate is the hard, visible nail, not a protective layer."
        );

        questions.add("What helps the skin stay strong and tough?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Melanin",
                "Keratin", // Correct answer
                "Sebum",
                "Sweat"
        )));
        correctAnswers.add("Keratin");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Keratin (Correct answer)\n" +
                        "Keratin, a protein made by keratinocytes, makes the epidermis, hair, and nails strong and tough.\n\n" +
                        "Melanin (Incorrect)\n" +
                        "Melanin provides UV protection and color, not strength.\n\n" +
                        "Sebum (Incorrect)\n" +
                        "Sebum lubricates the skin, not strengthens it.\n\n" +
                        "Sweat (Incorrect)\n" +
                        "Sweat cools the body, not adds strength."
        );

        questions.add("What is the name of the skin condition caused by a fungus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Acne",
                "Eczema",
                "Ringworm", // Correct answer
                "Psoriasis"
        )));
        correctAnswers.add("Ringworm");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Ringworm (Correct answer)\n" +
                        "Ringworm (tinea) is a fungal infection causing a red, ring-shaped rash on the skin.\n\n" +
                        "Acne (Incorrect)\n" +
                        "Acne is caused by clogged pores and bacteria, not a fungus.\n\n" +
                        "Eczema (Incorrect)\n" +
                        "Eczema is linked to allergies or immune issues, not fungi.\n\n" +
                        "Psoriasis (Incorrect)\n" +
                        "Psoriasis is an autoimmune condition, not fungal."
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
        new AlertDialog.Builder(ChallengeMode1.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
