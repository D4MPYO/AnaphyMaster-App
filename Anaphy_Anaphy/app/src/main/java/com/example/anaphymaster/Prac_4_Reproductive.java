package com.example.anaphymaster;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Prac_4_Reproductive extends AppCompatActivity {

    private int correctAnswersCount = 0;  // To track the number of correct answers
    private int totalQuestions = 100;       // Total number of questions
    private final int maxQuestions = 100;

    private List<String> questions;
    private List<List<String>> choices;
    private List<String> correctAnswers;

    private TextView questionText, counterText, rationaleTextView;
    private Button btnA, btnB, btnC, btnD, nextQuestionButton;
    private ImageView restartIcon, exitIcon, rationaleIcon;

    private int currentIndex = 0;
    private List<Integer> questionOrder;

    private View rationaleCard;

    private HashMap<Integer, String> rationales;

    private boolean hasAnswered = false;

    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.prac_4_reproductive);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        skipButton = findViewById(R.id.skipButton);


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

        btnA.setOnClickListener(view -> checkAnswer(btnA));
        btnB.setOnClickListener(view -> checkAnswer(btnB));
        btnC.setOnClickListener(view -> checkAnswer(btnC));
        btnD.setOnClickListener(view -> checkAnswer(btnD));

        rationaleIcon.setOnClickListener(v -> {
            if (hasAnswered) {
                showRationale(questionOrder.get(currentIndex));
            } else {
                Toast.makeText(Prac_4_Reproductive.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_4_Reproductive.this)
                    .setTitle("Restart Quiz")
                    .setMessage("Are you sure you want to restart? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> resetQuiz())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        exitIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_4_Reproductive.this)
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
                rationaleCard.setVisibility(View.GONE);  // Hide rationale card when proceeding to next question
            } else {
                new AlertDialog.Builder(Prac_4_Reproductive.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_4_Reproductive.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Practice");

                            intent.putExtra("difficulty", "Practice");
                            intent.putExtra("category", "Integumentary System");
                            intent.putExtra("mode", "Practice Mode");

                            startActivity(intent);
                        })
                        .show();
            }
        });

        skipButton.setOnClickListener(v -> {
            if (currentIndex < maxQuestions - 1) {
                totalQuestions--; // Decrease total questions count
                currentIndex++;
                displayQuestion(currentIndex);
                rationaleCard.setVisibility(View.GONE); // Hide rationale card when skipping
            } else {
                // Decrease total questions count for the last question
                totalQuestions--;

                new AlertDialog.Builder(Prac_4_Reproductive.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_4_Reproductive.this, Answer_Result.class);
                            intent.putExtra("correctAnswers", correctAnswersCount);
                            intent.putExtra("totalQuestions", totalQuestions);
                            dbHelper.updateQuizCount("Practice");

                            intent.putExtra("difficulty", "Practice");
                            intent.putExtra("category", "Integumentary System");
                            intent.putExtra("mode", "Practice Mode");

                            startActivity(intent);
                        })
                        .show();
            }
        });
    }

    private void setupQuestionsAndChoices() {
        questions = new ArrayList<>();
        choices = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        rationales = new HashMap<>();

        questions.add("Which structure is the site of sperm production?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Seminiferous tubules", // Correct answer
                "Epididymis",
                "Seminal vesicles",
                "Vas deferens"
        )));
        correctAnswers.add("Seminiferous tubules");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Seminiferous tubules (Correct answer)\n" +
                        "The seminiferous tubules in the testes are where spermatogenesis, or sperm production, occurs.\n\n" +
                        "Epididymis (Incorrect)\n" +
                        "The epididymis is responsible for sperm maturation and storage, not production.\n\n" +
                        "Seminal vesicles (Incorrect)\n" +
                        "The seminal vesicles produce seminal fluid, not sperm.\n\n" +
                        "Vas deferens (Incorrect)\n" +
                        "The vas deferens transports sperm, but does not produce them."
        );

        questions.add("What cells produce testosterone in the testes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Interstitial (Leydig) cells", // Correct answer
                "Sertoli cells",
                "Spermatogonia",
                "Spermatocytes"
        )));
        correctAnswers.add("Interstitial (Leydig) cells");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Interstitial (Leydig) cells (Correct answer)\n" +
                        "Leydig cells produce testosterone in response to luteinizing hormone (LH).\n\n" +
                        "Sertoli cells (Incorrect)\n" +
                        "Sertoli cells support and nourish developing sperm, but do not produce testosterone.\n\n" +
                        "Spermatogonia (Incorrect)\n" +
                        "Spermatogonia are stem cells involved in spermatogenesis but do not produce testosterone.\n\n" +
                        "Spermatocytes (Incorrect)\n" +
                        "Spermatocytes are cells in the process of becoming sperm, but they do not produce testosterone."
        );

        questions.add("The site of sperm maturation and storage is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epididymis", // Correct answer
                "Vas deferens",
                "Prostate gland",
                "Urethra"
        )));
        correctAnswers.add("Epididymis");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Epididymis (Correct answer)\n" +
                        "The epididymis is where sperm mature and are stored before ejaculation.\n\n" +
                        "Vas deferens (Incorrect)\n" +
                        "The vas deferens transports sperm, but does not store or mature them.\n\n" +
                        "Prostate gland (Incorrect)\n" +
                        "The prostate contributes to seminal fluid but is not involved in sperm maturation or storage.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra is the passageway for urine and semen, but it is not involved in sperm maturation or storage."
        );

        questions.add("Which part of the sperm contains enzymes necessary for fertilization?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Acrosome", // Correct answer
                "Midpiece",
                "Head nucleus",
                "Tail"
        )));
        correctAnswers.add("Acrosome");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Acrosome (Correct answer)\n" +
                        "The acrosome, located on the head of the sperm, contains enzymes that help the sperm penetrate the egg during fertilization.\n\n" +
                        "Midpiece (Incorrect)\n" +
                        "The midpiece contains mitochondria for energy but no enzymes for fertilization.\n\n" +
                        "Head nucleus (Incorrect)\n" +
                        "The head houses the sperm's genetic material, but not enzymes for fertilization.\n\n" +
                        "Tail (Incorrect)\n" +
                        "The tail provides motility but does not contain enzymes needed for fertilization."
        );

        questions.add("The function of the seminal vesicles includes:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Producing alkaline fluid", // Correct answer
                "Secreting testosterone",
                "Storing sperm",
                "Preventing urine flow"
        )));
        correctAnswers.add("Producing alkaline fluid");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Producing alkaline fluid (Correct answer)\n" +
                        "Seminal vesicles produce an alkaline fluid that helps neutralize the acidic environment of the vagina and provides nutrients to sperm.\n\n" +
                        "Secreting testosterone (Incorrect)\n" +
                        "Testosterone is secreted by the Leydig cells in the testes, not the seminal vesicles.\n\n" +
                        "Storing sperm (Incorrect)\n" +
                        "Sperm are stored in the epididymis, not the seminal vesicles.\n\n" +
                        "Preventing urine flow (Incorrect)\n" +
                        "The seminal vesicles do not have a role in regulating urine flow."
        );

        questions.add("The hormone responsible for stimulating spermatogenesis is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "FSH", // Correct answer
                "LH",
                "Testosterone",
                "Estrogen"
        )));
        correctAnswers.add("FSH");
        rationales.put(5,
                "RATIONALE:\n" +
                        "FSH (Correct answer)\n" +
                        "Follicle-stimulating hormone (FSH) stimulates spermatogenesis in the seminiferous tubules.\n\n" +
                        "LH (Incorrect)\n" +
                        "Luteinizing hormone (LH) stimulates the Leydig cells to produce testosterone, but does not directly stimulate spermatogenesis.\n\n" +
                        "Testosterone (Incorrect)\n" +
                        "Testosterone supports spermatogenesis but is not the primary hormone stimulating it.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen plays a role in female reproduction and does not stimulate spermatogenesis."
        );

        questions.add("Which structure transports sperm from the epididymis to the ejaculatory duct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Vas deferens", // Correct answer
                "Urethra",
                "Prostate",
                "Bulbourethral gland"
        )));
        correctAnswers.add("Vas deferens");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Vas deferens (Correct answer)\n" +
                        "The vas deferens carries sperm from the epididymis to the ejaculatory duct during ejaculation.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra carries semen out of the body, but it does not transport sperm from the epididymis.\n\n" +
                        "Prostate (Incorrect)\n" +
                        "The prostate contributes to semen but does not transport sperm.\n\n" +
                        "Bulbourethral gland (Incorrect)\n" +
                        "The bulbourethral glands produce pre-ejaculate fluid but do not transport sperm."
        );

        questions.add("The prostate gland contributes to semen by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secreting enzymes that activate sperm", // Correct answer
                "Producing sperm",
                "Providing nutrients",
                "Neutralizing vaginal acid"
        )));
        correctAnswers.add("Secreting enzymes that activate sperm");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Secreting enzymes that activate sperm (Correct answer)\n" +
                        "The prostate secretes enzymes that help activate sperm and allow them to swim.\n\n" +
                        "Producing sperm (Incorrect)\n" +
                        "The prostate does not produce sperm; sperm are produced in the seminiferous tubules.\n\n" +
                        "Providing nutrients (Incorrect)\n" +
                        "While the prostate contributes fluids to semen, it doesn't primarily provide nutrients.\n\n" +
                        "Neutralizing vaginal acid (Incorrect)\n" +
                        "The seminal vesicles, not the prostate, contribute most to neutralizing vaginal acidity."
        );

        questions.add("What is the primary female reproductive organ?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ovary", // Correct answer
                "Vagina",
                "Uterus",
                "Fallopian tube"
        )));
        correctAnswers.add("Ovary");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Ovary (Correct answer)\n" +
                        "The ovaries are the primary female reproductive organs that produce eggs (ova) and hormones like estrogen and progesterone.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "The vagina is part of the birth canal, not the primary reproductive organ.\n\n" +
                        "Uterus (Incorrect)\n" +
                        "The uterus is important for embryo implantation and development, but it is not the primary reproductive organ.\n\n" +
                        "Fallopian tube (Incorrect)\n" +
                        "The fallopian tubes are involved in transporting eggs from the ovaries to the uterus but are not the primary reproductive organs."
        );

        questions.add("Ovulation typically occurs around which day of a 28-day cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Day 14", // Correct answer
                "Day 1",
                "Day 7",
                "Day 21"
        )));
        correctAnswers.add("Day 14");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Day 14 (Correct answer)\n" +
                        "In a typical 28-day cycle, ovulation occurs around day 14, triggered by a surge in LH.\n\n" +
                        "Day 1 (Incorrect)\n" +
                        "Day 1 marks the beginning of menstrual bleeding, not ovulation.\n\n" +
                        "Day 7 (Incorrect)\n" +
                        "Ovulation happens later in the cycle, not on day 7.\n\n" +
                        "Day 21 (Incorrect)\n" +
                        "This is typically too late in the cycle, as ovulation generally occurs earlier."
        );

        questions.add("The hormone responsible for ovulation is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "LH", // Correct answer
                "FSH",
                "Estrogen",
                "Progesterone"
        )));
        correctAnswers.add("LH");
        rationales.put(10,
                "RATIONALE:\n" +
                        "LH (Correct answer)\n" +
                        "A surge in LH triggers ovulation, the release of the egg from the ovary.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH stimulates the growth of follicles but does not trigger ovulation.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen helps in follicle maturation but does not directly trigger ovulation.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone is involved in maintaining the uterine lining after ovulation, not in triggering ovulation."
        );

        questions.add("Progesterone is primarily secreted by the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Corpus luteum", // Correct answer
                "Follicle",
                "Endometrium",
                "Pituitary gland"
        )));
        correctAnswers.add("Corpus luteum");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Corpus luteum (Correct answer)\n" +
                        "After ovulation, the corpus luteum secretes progesterone to maintain the uterine lining for implantation.\n\n" +
                        "Follicle (Incorrect)\n" +
                        "The follicle primarily secretes estrogen during the first half of the menstrual cycle.\n\n" +
                        "Endometrium (Incorrect)\n" +
                        "The endometrium responds to hormonal signals but does not secrete progesterone.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary gland secretes FSH and LH, not progesterone."
        );

        questions.add("FSH stimulates:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Development of ovarian follicles", // Correct answer
                "Ovulation",
                "Growth of the corpus luteum",
                "Endometrial thickening"
        )));
        correctAnswers.add("Development of ovarian follicles");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Development of ovarian follicles (Correct answer)\n" +
                        "FSH stimulates the growth and maturation of ovarian follicles.\n\n" +
                        "Ovulation (Incorrect)\n" +
                        "FSH supports follicular growth but does not directly trigger ovulation.\n\n" +
                        "Growth of the corpus luteum (Incorrect)\n" +
                        "The corpus luteum is stimulated by LH, not FSH.\n\n" +
                        "Endometrial thickening (Incorrect)\n" +
                        "Estrogen is the hormone that promotes endometrial thickening during the follicular phase."
        );

        questions.add("A surge in which hormone directly triggers ovulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "LH", // Correct answer
                "Estrogen",
                "FSH",
                "Progesterone"
        )));
        correctAnswers.add("LH");
        rationales.put(13,
                "RATIONALE:\n" +
                        "LH (Correct answer)\n" +
                        "The LH surge triggers ovulation by causing the mature follicle to release the egg.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "While estrogen peaks before ovulation, it is the LH surge that directly triggers ovulation.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH helps with follicle development but does not directly trigger ovulation.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone’s primary role is to support the uterine lining after ovulation."
        );

        questions.add("Which hormone maintains the endometrium for implantation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Progesterone", // Correct answer
                "LH",
                "FSH",
                "Testosterone"
        )));
        correctAnswers.add("Progesterone");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Progesterone (Correct answer)\n" +
                        "Progesterone is responsible for maintaining the endometrium to support implantation of a fertilized egg.\n\n" +
                        "LH (Incorrect)\n" +
                        "LH triggers ovulation but does not maintain the endometrium.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH supports follicle development, not the maintenance of the endometrium.\n\n" +
                        "Testosterone (Incorrect)\n" +
                        "Testosterone is not involved in the menstrual cycle."
        );

        questions.add("The first day of the menstrual cycle is marked by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Menstrual bleeding", // Correct answer
                "Ovulation",
                "LH surge",
                "Progesterone peak"
        )));
        correctAnswers.add("Menstrual bleeding");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Menstrual bleeding (Correct answer)\n" +
                        "The first day of the menstrual cycle is marked by the onset of menstrual bleeding.\n\n" +
                        "Ovulation (Incorrect)\n" +
                        "Ovulation occurs later in the cycle, not at the start.\n\n" +
                        "LH surge (Incorrect)\n" +
                        "The LH surge precedes ovulation but is not the first event of the cycle.\n\n" +
                        "Progesterone peak (Incorrect)\n" +
                        "Progesterone peaks after ovulation, not at the start of the cycle."
        );

        questions.add("In the absence of fertilization, the corpus luteum degenerates into the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Corpus albicans", // Correct answer
                "Corpus cavernosum",
                "Graafian follicle",
                "Oogonium"
        )));
        correctAnswers.add("Corpus albicans");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Corpus albicans (Correct answer)\n" +
                        "If pregnancy does not occur, the corpus luteum degenerates into the corpus albicans, a non-functional structure.\n\n" +
                        "Corpus cavernosum (Incorrect)\n" +
                        "The corpus cavernosum is a structure in the penis, unrelated to the menstrual cycle.\n\n" +
                        "Graafian follicle (Incorrect)\n" +
                        "The Graafian follicle refers to the mature follicle before ovulation, not after fertilization.\n\n" +
                        "Oogonium (Incorrect)\n" +
                        "Oogonia are precursors to oocytes, not involved in the degeneration of the corpus luteum."
        );

        questions.add("Estrogen is mainly produced by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Granulosa cells of developing follicles", // Correct answer
                "Corpus luteum",
                "Hypothalamus",
                "Endometrium"
        )));
        correctAnswers.add("Granulosa cells of developing follicles");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Granulosa cells of developing follicles (Correct answer)\n" +
                        "Estrogen is mainly produced by the granulosa cells in developing ovarian follicles during the follicular phase.\n\n" +
                        "Corpus luteum (Incorrect)\n" +
                        "The corpus luteum primarily produces progesterone after ovulation, not estrogen.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "The hypothalamus produces GnRH, which stimulates the pituitary, but it does not directly produce estrogen.\n\n" +
                        "Endometrium (Incorrect)\n" +
                        "The endometrium responds to estrogen but does not produce it."
        );

        questions.add("The hormone that inhibits FSH and LH during the luteal phase is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Progesterone", // Correct answer
                "GnRH",
                "Estrogen",
                "Oxytocin"
        )));
        correctAnswers.add("Progesterone");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Progesterone (Correct answer)\n" +
                        "Progesterone inhibits the secretion of FSH and LH during the luteal phase to prevent further ovulation.\n\n" +
                        "GnRH (Incorrect)\n" +
                        "GnRH stimulates the release of FSH and LH, it does not inhibit them.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "While estrogen levels rise during the luteal phase, progesterone has a stronger inhibitory effect on FSH and LH.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Oxytocin is involved in childbirth and lactation, not in the regulation of the menstrual cycle."
        );

        questions.add("The uterine phase that coincides with follicular development is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Proliferative phase", // Correct answer
                "Secretory phase",
                "Menstrual phase",
                "Luteal phase"
        )));
        correctAnswers.add("Proliferative phase");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Proliferative phase (Correct answer)\n" +
                        "The proliferative phase occurs during the follicular phase of the ovarian cycle and is characterized by the thickening of the endometrium in response to estrogen.\n\n" +
                        "Secretory phase (Incorrect)\n" +
                        "The secretory phase follows ovulation and is marked by the secretion of progesterone, preparing the endometrium for implantation.\n\n" +
                        "Menstrual phase (Incorrect)\n" +
                        "The menstrual phase marks the shedding of the endometrial lining and occurs at the start of the cycle.\n\n" +
                        "Luteal phase (Incorrect)\n" +
                        "The luteal phase follows ovulation and corresponds with the secretory phase of the endometrial cycle."
        );

        questions.add("Where does fertilization most commonly occur?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Uterine (fallopian) tube", // Correct answer
                "Uterus",
                "Ovary",
                "Vagina"
        )));
        correctAnswers.add("Uterine (fallopian) tube");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Uterine (fallopian) tube (Correct answer)\n" +
                        "Fertilization typically occurs in the fallopian tube, where the egg meets the sperm.\n\n" +
                        "Uterus (Incorrect)\n" +
                        "Fertilization does not occur in the uterus but in the fallopian tube.\n\n" +
                        "Ovary (Incorrect)\n" +
                        "The ovary is where the egg is released, not where fertilization occurs.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "The vagina is where sperm enters, but fertilization occurs in the fallopian tube."
        );

        questions.add("What hormone is detected by pregnancy tests?");
        choices.add(new ArrayList<>(Arrays.asList(
                "hCG", // Correct answer
                "FSH",
                "Progesterone",
                "LH"
        )));
        correctAnswers.add("hCG");
        rationales.put(21,
                "RATIONALE:\n" +
                        "hCG (Correct answer)\n" +
                        "Human chorionic gonadotropin (hCG) is the hormone detected in pregnancy tests.\n\n" +
                        "FSH (Incorrect)\n" +
                        "Follicle-stimulating hormone (FSH) is involved in the reproductive cycle but not in pregnancy detection.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone is important during pregnancy but not detected by pregnancy tests.\n\n" +
                        "LH (Incorrect)\n" +
                        "Luteinizing hormone (LH) is involved in ovulation but not in pregnancy tests."
        );

        questions.add("hCG is produced by the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Embryo (chorion)", // Correct answer
                "Corpus luteum",
                "Anterior pituitary",
                "Endometrium"
        )));
        correctAnswers.add("Embryo (chorion)");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Embryo (chorion) (Correct answer)\n" +
                        "hCG is produced by the trophoblast (chorion) of the embryo during early pregnancy.\n\n" +
                        "Corpus luteum (Incorrect)\n" +
                        "The corpus luteum produces progesterone, not hCG.\n\n" +
                        "Anterior pituitary (Incorrect)\n" +
                        "The anterior pituitary produces gonadotropins like FSH and LH but not hCG.\n\n" +
                        "Endometrium (Incorrect)\n" +
                        "The endometrium does not produce hCG."
        );

        questions.add("The placenta functions to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Exchange nutrients and gases", // Correct answer
                "Produce sperm",
                "Secrete milk",
                "Store eggs"
        )));
        correctAnswers.add("Exchange nutrients and gases");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Exchange nutrients and gases (Correct answer)\n" +
                        "The placenta's primary function is to exchange nutrients, gases, and waste products between the mother and fetus.\n\n" +
                        "Produce sperm (Incorrect)\n" +
                        "The placenta does not produce sperm; it provides nutrients to the developing fetus.\n\n" +
                        "Secrete milk (Incorrect)\n" +
                        "Milk secretion is the function of the mammary glands.\n\n" +
                        "Store eggs (Incorrect)\n" +
                        "Eggs are stored in the ovaries, not the placenta."
        );

        questions.add("Which hormone initiates milk production after birth?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prolactin", // Correct answer
                "Estrogen",
                "Oxytocin",
                "Progesterone"
        )));
        correctAnswers.add("Prolactin");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Prolactin (Correct answer)\n" +
                        "Prolactin is the hormone responsible for initiating milk production after birth.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen stimulates the development of the milk glands but does not initiate milk production.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Oxytocin stimulates milk ejection, not production.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone prepares the breast for milk production but does not initiate it."
        );

        questions.add("Which hormone causes uterine contractions during labor?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Oxytocin", // Correct answer
                "Progesterone",
                "LH",
                "Prolactin"
        )));
        correctAnswers.add("Oxytocin");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Oxytocin (Correct answer)\n" +
                        "Oxytocin stimulates uterine contractions during labor.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone helps maintain pregnancy but inhibits uterine contractions.\n\n" +
                        "LH (Incorrect)\n" +
                        "Luteinizing hormone (LH) is involved in ovulation, not in uterine contractions.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Prolactin stimulates milk production, not uterine contractions."
        );

        questions.add("What structure in the breast stores milk prior to release?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lactiferous sinus", // Correct answer
                "Alveoli",
                "Areola",
                "Ductules"
        )));
        correctAnswers.add("Lactiferous sinus");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Lactiferous sinus (Correct answer)\n" +
                        "The lactiferous sinus is where milk is stored before it is released.\n\n" +
                        "Alveoli (Incorrect)\n" +
                        "Alveoli produce milk, but it is stored in the lactiferous sinuses.\n\n" +
                        "Areola (Incorrect)\n" +
                        "The areola is the pigmented area around the nipple, not a milk storage area.\n\n" +
                        "Ductules (Incorrect)\n" +
                        "Ductules carry milk, but they do not store it."
        );

        questions.add("What term describes the first menstrual period in females?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Menarche", // Correct answer
                "Menopause",
                "Ovulation",
                "Menstruation"
        )));
        correctAnswers.add("Menarche");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Menarche (Correct answer)\n" +
                        "Menarche is the first menstrual period in females.\n\n" +
                        "Menopause (Incorrect)\n" +
                        "Menopause is the cessation of menstrual periods.\n\n" +
                        "Ovulation (Incorrect)\n" +
                        "Ovulation is the release of an egg from the ovary, not the first period.\n\n" +
                        "Menstruation (Incorrect)\n" +
                        "Menstruation refers to the shedding of the uterine lining, not the first period."
        );

        questions.add("Male puberty is typically initiated by increased secretion of:");
        choices.add(new ArrayList<>(Arrays.asList(
                "GnRH, leading to LH and FSH secretion", // Correct answer
                "FSH alone",
                "Testosterone from adrenal glands",
                "Inhibin"
        )));
        correctAnswers.add("GnRH, leading to LH and FSH secretion");
        rationales.put(28,
                "RATIONALE:\n" +
                        "GnRH, leading to LH and FSH secretion (Correct answer)\n" +
                        "Puberty is initiated by increased secretion of gonadotropin-releasing hormone (GnRH), which stimulates the release of LH and FSH.\n\n" +
                        "FSH alone (Incorrect)\n" +
                        "FSH plays a role but is not the primary hormone for initiating puberty.\n\n" +
                        "Testosterone from adrenal glands (Incorrect)\n" +
                        "Testosterone is crucial but is primarily produced in the testes during puberty.\n\n" +
                        "Inhibin (Incorrect)\n" +
                        "Inhibin regulates FSH but does not initiate puberty."
        );

        questions.add("Meiosis in females results in:");
        choices.add(new ArrayList<>(Arrays.asList(
                "One ovum and three polar bodies", // Correct answer
                "Four equal ovum",
                "Four sperm",
                "No gametes"
        )));
        correctAnswers.add("One ovum and three polar bodies");
        rationales.put(29,
                "RATIONALE:\n" +
                        "One ovum and three polar bodies (Correct answer)\n" +
                        "Meiosis in females results in one viable ovum and three smaller polar bodies that degenerate.\n\n" +
                        "Four equal ovum (Incorrect)\n" +
                        "Meiosis in females results in one ovum and three polar bodies, not four equal ova.\n\n" +
                        "Four sperm (Incorrect)\n" +
                        "Meiosis in males results in four sperm, not in females.\n\n" +
                        "No gametes (Incorrect)\n" +
                        "Meiosis in females results in gametes (ova)."
        );

        questions.add("Which structure is homologous to the male penis in females?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Clitoris", // Correct answer
                "Vagina",
                "Cervix",
                "Urethra"
        )));
        correctAnswers.add("Clitoris");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Clitoris (Correct answer)\n" +
                        "The clitoris is homologous to the male penis, both derived from the same embryonic tissue and containing erectile tissue.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "The vagina is a separate reproductive structure, not homologous to the penis.\n\n" +
                        "Cervix (Incorrect)\n" +
                        "The cervix is part of the female reproductive tract, not homologous to the penis.\n\n" +
                        "Urethra (Incorrect)\n" +
                        "The urethra is a passage for urine, not a reproductive structure homologous to the penis."
        );

        questions.add("The scrotum helps regulate the temperature of the testes by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Contracting or relaxing the dartos and cremaster muscles", // Correct answer
                "Releasing testosterone",
                "Producing sweat",
                "Secreting cooling fluid"
        )));
        correctAnswers.add("Contracting or relaxing the dartos and cremaster muscles");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Contracting or relaxing the dartos and cremaster muscles (Correct answer)\n" +
                        "The scrotum regulates temperature by contracting and relaxing the dartos and cremaster muscles, bringing the testes closer to or farther from the body for temperature control.\n\n" +
                        "Releasing testosterone (Incorrect)\n" +
                        "The release of testosterone is the function of Leydig cells, not the scrotum.\n\n" +
                        "Producing sweat (Incorrect)\n" +
                        "The scrotum does not produce sweat to regulate temperature.\n\n" +
                        "Secreting cooling fluid (Incorrect)\n" +
                        "The scrotum does not secrete cooling fluid but helps regulate temperature through muscle movement."
        );

        questions.add("The blood–testis barrier is formed by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sertoli cells", // Correct answer
                "Leydig cells",
                "Germ cells",
                "Spermatogonia"
        )));
        correctAnswers.add("Sertoli cells");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Sertoli cells (Correct answer)\n" +
                        "Sertoli cells form the blood-testis barrier by creating tight junctions that protect developing sperm from harmful substances.\n\n" +
                        "Leydig cells (Incorrect)\n" +
                        "Leydig cells produce testosterone but do not form the blood-testis barrier.\n\n" +
                        "Germ cells (Incorrect)\n" +
                        "Germ cells are involved in spermatogenesis, not the blood-testis barrier.\n\n" +
                        "Spermatogonia (Incorrect)\n" +
                        "Spermatogonia are early germ cells, not involved in forming the blood-testis barrier."
        );

        questions.add("Oogenesis begins during which stage of life?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fetal development", // Correct answer
                "Infancy",
                "Puberty",
                "Adolescence"
        )));
        correctAnswers.add("Fetal development");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Fetal development (Correct answer)\n" +
                        "Oogenesis begins during fetal development when primary oocytes are formed.\n\n" +
                        "Infancy (Incorrect)\n" +
                        "Oogenesis does not begin during infancy; it starts during fetal development.\n\n" +
                        "Puberty (Incorrect)\n" +
                        "Puberty is when oogenesis resumes, not when it begins.\n\n" +
                        "Adolescence (Incorrect)\n" +
                        "Oogenesis does not begin during adolescence, though it resumes at puberty."
        );

        questions.add("The primary oocyte is arrested in which phase until ovulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prophase I", // Correct answer
                "Metaphase II",
                "Anaphase I",
                "Telophase II"
        )));
        correctAnswers.add("Prophase I");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Prophase I (Correct answer)\n" +
                        "The primary oocyte is arrested in Prophase I of meiosis until puberty and ovulation.\n\n" +
                        "Metaphase II (Incorrect)\n" +
                        "The primary oocyte is arrested in Prophase I, not Metaphase II.\n\n" +
                        "Anaphase I (Incorrect)\n" +
                        "The primary oocyte is not arrested in Anaphase I.\n\n" +
                        "Telophase II (Incorrect)\n" +
                        "Telophase II occurs after ovulation and fertilization, not before."
        );

        questions.add("The layer of the uterus that is shed during menstruation is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Endometrium – functional layer", // Correct answer
                "Myometrium",
                "Endometrium – basal layer",
                "Perimetrium"
        )));
        correctAnswers.add("Endometrium – functional layer");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Endometrium – functional layer (Correct answer)\n" +
                        "The functional layer of the endometrium is shed during menstruation.\n\n" +
                        "Myometrium (Incorrect)\n" +
                        "The myometrium is the muscular layer of the uterus, not involved in menstruation.\n\n" +
                        "Endometrium – basal layer (Incorrect)\n" +
                        "The basal layer of the endometrium remains intact during menstruation.\n\n" +
                        "Perimetrium (Incorrect)\n" +
                        "The perimetrium is the outermost layer of the uterus and is not shed during menstruation."
        );

        questions.add("Which hormone directly stimulates Leydig cells to produce testosterone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "LH", // Correct answer
                "FSH",
                "GnRH",
                "Inhibin"
        )));
        correctAnswers.add("LH");
        rationales.put(36,
                "RATIONALE:\n" +
                        "LH (Correct answer)\n" +
                        "Luteinizing hormone (LH) directly stimulates Leydig cells to produce testosterone.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH stimulates Sertoli cells, not Leydig cells.\n\n" +
                        "GnRH (Incorrect)\n" +
                        "GnRH stimulates the release of LH and FSH but does not directly affect Leydig cells.\n\n" +
                        "Inhibin (Incorrect)\n" +
                        "Inhibin inhibits FSH production but does not stimulate testosterone production."
        );

        questions.add("The female reproductive cycle is primarily controlled by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypothalamic-pituitary-gonadal axis", // Correct answer
                "Insulin",
                "Thyroid hormone",
                "Parathyroid hormone"
        )));
        correctAnswers.add("Hypothalamic-pituitary-gonadal axis");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Hypothalamic-pituitary-gonadal axis (Correct answer)\n" +
                        "The hypothalamic-pituitary-gonadal axis is responsible for regulating the female reproductive cycle.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin regulates metabolism, not the reproductive cycle.\n\n" +
                        "Thyroid hormone (Incorrect)\n" +
                        "Thyroid hormones affect metabolism but do not directly control the female reproductive cycle.\n\n" +
                        "Parathyroid hormone (Incorrect)\n" +
                        "Parathyroid hormone regulates calcium levels, not the reproductive cycle."
        );

        questions.add("What structure carries the oocyte from the ovary to the uterus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Uterine tube (fallopian tube)", // Correct answer
                "Vagina",
                "Cervix",
                "Round ligament"
        )));
        correctAnswers.add("Uterine tube (fallopian tube)");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Uterine tube (fallopian tube) (Correct answer)\n" +
                        "The uterine (fallopian) tube carries the oocyte from the ovary to the uterus.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "The vagina is part of the birth canal, not the passage for the oocyte.\n\n" +
                        "Cervix (Incorrect)\n" +
                        "The cervix connects the uterus to the vagina but does not carry the oocyte.\n\n" +
                        "Round ligament (Incorrect)\n" +
                        "The round ligament helps support the uterus but does not carry the oocyte."
        );

        questions.add("The process of forming mature sperm cells is called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spermatogenesis", // Correct answer
                "Oogenesis",
                "Fertilization",
                "Mitosis"
        )));
        correctAnswers.add("Spermatogenesis");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Spermatogenesis (Correct answer)\n" +
                        "Spermatogenesis is the process of forming mature sperm cells.\n\n" +
                        "Oogenesis (Incorrect)\n" +
                        "Oogenesis is the process of forming oocytes (egg cells), not sperm.\n\n" +
                        "Fertilization (Incorrect)\n" +
                        "Fertilization is the fusion of sperm and egg, not the formation of sperm.\n\n" +
                        "Mitosis (Incorrect)\n" +
                        "Mitosis is a type of cell division but not the process of forming sperm cells."
        );

        questions.add("What structure receives the ovulated oocyte?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Infundibulum", // Correct answer
                "Cervix",
                "Vagina",
                "Endometrium"
        )));
        correctAnswers.add("Infundibulum");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Infundibulum (Correct answer)\n" +
                        "The infundibulum is the funnel-shaped structure at the end of the fallopian tube that captures the ovulated oocyte.\n\n" +
                        "Cervix (Incorrect)\n" +
                        "The cervix is the lower part of the uterus and does not receive the oocyte.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "The vagina is where sperm is deposited, not where the oocyte is captured.\n\n" +
                        "Endometrium (Incorrect)\n" +
                        "The endometrium is the uterine lining where implantation occurs, but not where the oocyte is received."
        );

        questions.add("Which hormone is primarily responsible for female secondary sexual characteristics?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen", // Correct answer
                "LH",
                "FSH",
                "Progesterone"
        )));
        correctAnswers.add("Estrogen");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Estrogen (Correct answer)\n" +
                        "Estrogen is the hormone responsible for the development of female secondary sexual characteristics, such as breast development and widening of hips.\n\n" +
                        "LH (Incorrect)\n" +
                        "Luteinizing hormone (LH) is involved in ovulation, but not in the development of secondary sexual characteristics.\n\n" +
                        "FSH (Incorrect)\n" +
                        "Follicle-stimulating hormone (FSH) stimulates ovarian follicles but is not responsible for secondary sexual characteristics.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone helps regulate the menstrual cycle and supports pregnancy, but it doesn't directly cause secondary sexual characteristics."
        );

        questions.add("Which of the following contributes most to the volume of semen?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Seminal vesicles", // Correct answer
                "Testes",
                "Epididymis",
                "Prostate gland"
        )));
        correctAnswers.add("Seminal vesicles");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Seminal vesicles (Correct answer)\n" +
                        "The seminal vesicles contribute the majority of the fluid that makes up semen, providing nutrients and helping sperm mobility.\n\n" +
                        "Testes (Incorrect)\n" +
                        "The testes produce sperm but contribute minimally to semen volume.\n\n" +
                        "Epididymis (Incorrect)\n" +
                        "The epididymis stores sperm but does not contribute significantly to semen volume.\n\n" +
                        "Prostate gland (Incorrect)\n" +
                        "The prostate produces a portion of the seminal fluid but not the majority."
        );

        questions.add("The corpus luteum is formed from the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Collapsed follicle", // Correct answer
                "Ovulated oocyte",
                "Endometrial lining",
                "Zygote"
        )));
        correctAnswers.add("Collapsed follicle");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Collapsed follicle (Correct answer)\n" +
                        "The corpus luteum forms from the collapsed follicle after ovulation, secreting progesterone to support early pregnancy.\n\n" +
                        "Ovulated oocyte (Incorrect)\n" +
                        "The ovulated oocyte does not form the corpus luteum; it is released from the follicle.\n\n" +
                        "Endometrial lining (Incorrect)\n" +
                        "The endometrial lining is not involved in the formation of the corpus luteum.\n\n" +
                        "Zygote (Incorrect)\n" +
                        "The zygote forms after fertilization, not from the corpus luteum."
        );

        questions.add("How many sperm are produced from one primary spermatocyte?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Four", // Correct answer
                "One",
                "Two",
                "Three"
        )));
        correctAnswers.add("Four");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Four (Correct answer)\n" +
                        "One primary spermatocyte undergoes meiosis, resulting in four sperm cells.\n\n" +
                        "One (Incorrect)\n" +
                        "One primary spermatocyte undergoes meiosis to produce more than one sperm.\n\n" +
                        "Two (Incorrect)\n" +
                        "The primary spermatocyte produces four sperm, not just two.\n\n" +
                        "Three (Incorrect)\n" +
                        "Three sperm are not produced from one primary spermatocyte."
        );

        questions.add("Capacitation of sperm refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Activation of enzymes in the acrosome", // Correct answer
                "Division of sperm into four mature sperm cells",
                "Fusion with the egg",
                "Attachment to the uterine wall"
        )));
        correctAnswers.add("Activation of enzymes in the acrosome");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Activation of enzymes in the acrosome (Correct answer)\n" +
                        "Capacitation is the process by which sperm undergo enzymatic changes in the acrosome, enabling them to fertilize an egg.\n\n" +
                        "Division of sperm into four mature sperm cells (Incorrect)\n" +
                        "This is the result of meiosis, not capacitation.\n\n" +
                        "Fusion with the egg (Incorrect)\n" +
                        "Fusion with the egg occurs after capacitation during fertilization.\n\n" +
                        "Attachment to the uterine wall (Incorrect)\n" +
                        "This is not related to capacitation; sperm do not attach to the uterine wall during capacitation."
        );

        questions.add("Polyspermy is prevented by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Zona pellucida hardening", // Correct answer
                "Multiple ovulations",
                "Sperm degradation",
                "Oocyte inactivation"
        )));
        correctAnswers.add("Zona pellucida hardening");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Zona pellucida hardening (Correct answer)\n" +
                        "After the first sperm fertilizes the oocyte, the zona pellucida hardens, preventing additional sperm from entering.\n\n" +
                        "Multiple ovulations (Incorrect)\n" +
                        "Multiple ovulations increase the chance of multiple sperm fertilizing eggs but do not prevent polyspermy.\n\n" +
                        "Sperm degradation (Incorrect)\n" +
                        "Sperm degradation does not directly prevent polyspermy; the zona pellucida's hardening is the main mechanism.\n\n" +
                        "Oocyte inactivation (Incorrect)\n" +
                        "Oocyte inactivation is not the primary mechanism to prevent polyspermy."
        );

        questions.add("Which hormone keeps the corpus luteum active in early pregnancy?");
        choices.add(new ArrayList<>(Arrays.asList(
                "LH",
                "FSH",
                "hCG", // Correct answer
                "Estrogen"
        )));
        correctAnswers.add("hCG");
        rationales.put(47,
                "RATIONALE:\n" +
                        "hCG (Correct answer)\n" +
                        "Human chorionic gonadotropin (hCG) is produced by the embryo and maintains the corpus luteum in early pregnancy.\n\n" +
                        "LH (Incorrect)\n" +
                        "LH triggers ovulation but does not maintain the corpus luteum during pregnancy.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH regulates the menstrual cycle but does not maintain the corpus luteum.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen supports pregnancy but does not directly maintain the corpus luteum."
        );

        questions.add("Implantation of the embryo normally occurs in the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cervix",
                "Uterine tube",
                "Endometrium of the uterus", // Correct answer
                "Vagina"
        )));
        correctAnswers.add("Endometrium of the uterus");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Endometrium of the uterus (Correct answer)\n" +
                        "The embryo implants into the endometrial lining of the uterus.\n\n" +
                        "Cervix (Incorrect)\n" +
                        "Implantation does not occur in the cervix; it happens in the uterus.\n\n" +
                        "Uterine tube (Incorrect)\n" +
                        "The uterine tube is where fertilization occurs, not implantation.\n\n" +
                        "Vagina (Incorrect)\n" +
                        "Implantation does not occur in the vagina."
        );

        questions.add("The placenta is derived from:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Maternal endometrium only",
                "Fetal chorion and maternal endometrium", // Correct answer
                "Fetal yolk sac",
                "Amnion only"
        )));
        correctAnswers.add("Fetal chorion and maternal endometrium");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Fetal chorion and maternal endometrium (Correct answer)\n" +
                        "The placenta is formed from both the fetal chorion and the maternal endometrium.\n\n" +
                        "Maternal endometrium only (Incorrect)\n" +
                        "The placenta is not solely derived from the maternal endometrium.\n\n" +
                        "Fetal yolk sac (Incorrect)\n" +
                        "The yolk sac contributes to early development but does not form the placenta.\n\n" +
                        "Amnion only (Incorrect)\n" +
                        "The amnion surrounds the fetus but does not form the placenta."
        );

        questions.add("The hormone oxytocin facilitates:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Uterine contractions and milk ejection", // Correct answer
                "Milk production",
                "Ovulation",
                "Follicle maturation"
        )));
        correctAnswers.add("Uterine contractions and milk ejection");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Uterine contractions and milk ejection (Correct answer)\n" +
                        "Oxytocin stimulates uterine contractions during labor and milk ejection during breastfeeding.\n\n" +
                        "Milk production (Incorrect)\n" +
                        "Oxytocin does not produce milk, but it helps with milk ejection.\n\n" +
                        "Ovulation (Incorrect)\n" +
                        "Oxytocin does not play a role in ovulation.\n\n" +
                        "Follicle maturation (Incorrect)\n" +
                        "Oxytocin does not directly affect follicle maturation."
        );

        questions.add("The three germ layers formed during gastrulation are:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ectoderm, mesoderm, endoderm", // Correct answer
                "Amnion, chorion, yolk sac",
                "Morula, blastocyst, trophoblast",
                "Ovum, zygote, embryo"
        )));
        correctAnswers.add("Ectoderm, mesoderm, endoderm");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Ectoderm, mesoderm, endoderm (Correct answer)\n" +
                        "The three primary germ layers formed during gastrulation are the ectoderm, mesoderm, and endoderm.\n\n" +
                        "Amnion, chorion, yolk sac (Incorrect)\n" +
                        "These are membranes involved in fetal development but are not germ layers.\n\n" +
                        "Morula, blastocyst, trophoblast (Incorrect)\n" +
                        "These are stages of early embryonic development, not germ layers.\n\n" +
                        "Ovum, zygote, embryo (Incorrect)\n" +
                        "These are stages of fertilization and early development, not germ layers."
        );

        questions.add("Which structure produces progesterone in early pregnancy until the placenta takes over?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Corpus luteum", // Correct answer
                "Follicle",
                "Uterine wall",
                "Hypothalamus"
        )));
        correctAnswers.add("Corpus luteum");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Corpus luteum (Correct answer)\n" +
                        "The corpus luteum produces progesterone during early pregnancy until the placenta takes over.\n\n" +
                        "Follicle (Incorrect)\n" +
                        "The follicle produces estrogen and eggs but not progesterone after ovulation.\n\n" +
                        "Uterine wall (Incorrect)\n" +
                        "The uterine wall supports pregnancy but does not produce progesterone in early pregnancy.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "The hypothalamus regulates hormone release but does not produce progesterone."
        );

        questions.add("The stage of development immediately following fertilization is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Zygote", // Correct answer
                "Embryo",
                "Fetus",
                "Morula"
        )));
        correctAnswers.add("Zygote");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Zygote (Correct answer)\n" +
                        "Immediately following fertilization, the fertilized egg is called a zygote.\n\n" +
                        "Embryo (Incorrect)\n" +
                        "The embryo develops after the zygote.\n\n" +
                        "Fetus (Incorrect)\n" +
                        "The fetus develops after the embryo stage.\n\n" +
                        "Morula (Incorrect)\n" +
                        "The morula is an early stage in development, but it follows fertilization."
        );

        questions.add("Which hormone helps soften the cervix before labor?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Relaxin", // Correct answer
                "Progesterone",
                "Estrogen",
                "Testosterone"
        )));
        correctAnswers.add("Relaxin");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Relaxin (Correct answer)\n" +
                        "Relaxin is a hormone that helps soften the cervix in preparation for labor.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone maintains pregnancy but does not soften the cervix before labor.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen plays a role in pregnancy but does not soften the cervix before labor.\n\n" +
                        "Testosterone (Incorrect)\n" +
                        "Testosterone is involved in male development, not in the preparation for labor."
        );

        questions.add("A surge in which hormone initiates parturition (labor)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Oxytocin", // Correct answer
                "Progesterone",
                "hCG",
                "Inhibin"
        )));
        correctAnswers.add("Oxytocin");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Oxytocin (Correct answer)\n" +
                        "Oxytocin causes uterine contractions and is critical for initiating labor.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone helps maintain pregnancy but inhibits uterine contractions, so it doesn't initiate labor.\n\n" +
                        "hCG (Incorrect)\n" +
                        "hCG is important in early pregnancy but does not trigger labor.\n\n" +
                        "Inhibin (Incorrect)\n" +
                        "Inhibin regulates FSH but does not influence labor."
        );

        questions.add("What best describes the fetal stage of development?");
        choices.add(new ArrayList<>(Arrays.asList(
                "9 weeks to birth", // Correct answer
                "Fertilization to implantation",
                "First 8 weeks post-fertilization",
                "Zygote to gastrula"
        )));
        correctAnswers.add("9 weeks to birth");
        rationales.put(56,
                "RATIONALE:\n" +
                        "9 weeks to birth (Correct answer)\n" +
                        "The fetal stage begins at 9 weeks and lasts until birth.\n\n" +
                        "Fertilization to implantation (Incorrect)\n" +
                        "This refers to the early embryonic stages before fetal development.\n\n" +
                        "First 8 weeks post-fertilization (Incorrect)\n" +
                        "This is the embryonic stage, not the fetal stage.\n\n" +
                        "Zygote to gastrula (Incorrect)\n" +
                        "This refers to early development before the fetal stage."
        );

        questions.add("Lactation is maintained by which hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Prolactin", // Correct answer
                "LH",
                "FSH",
                "GnRH"
        )));
        correctAnswers.add("Prolactin");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Prolactin (Correct answer)\n" +
                        "Prolactin is the primary hormone responsible for maintaining milk production.\n\n" +
                        "LH (Incorrect)\n" +
                        "Luteinizing hormone (LH) is involved in ovulation, not lactation.\n\n" +
                        "FSH (Incorrect)\n" +
                        "Follicle-stimulating hormone (FSH) is involved in follicular development, not lactation.\n\n" +
                        "GnRH (Incorrect)\n" +
                        "Gonadotropin-releasing hormone (GnRH) regulates reproductive hormones but does not directly maintain lactation."
        );

        questions.add("What is the function of the amniotic fluid?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Protect the embryo from physical shock", // Correct answer
                "Deliver nutrients",
                "Form the placenta",
                "Facilitate fertilization"
        )));
        correctAnswers.add("Protect the embryo from physical shock");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Protect the embryo from physical shock (Correct answer)\n" +
                        "Amniotic fluid cushions and protects the fetus from mechanical injury.\n\n" +
                        "Deliver nutrients (Incorrect)\n" +
                        "Nutrients are delivered through the placenta, not the amniotic fluid.\n\n" +
                        "Form the placenta (Incorrect)\n" +
                        "The placenta forms from fetal and maternal tissues, not the amniotic fluid.\n\n" +
                        "Facilitate fertilization (Incorrect)\n" +
                        "Amniotic fluid does not play a role in fertilization."
        );

        questions.add("Which hormone inhibits GnRH during pregnancy to prevent follicular development?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Progesterone", // Correct answer
                "Estrogen",
                "Prolactin",
                "Oxytocin"
        )));
        correctAnswers.add("Progesterone");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Progesterone (Correct answer)\n" +
                        "Progesterone inhibits GnRH to prevent ovulation and follicular development during pregnancy.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen promotes the development of the follicle but doesn't inhibit GnRH during pregnancy.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Prolactin helps with milk production, not in inhibiting GnRH during pregnancy.\n\n" +
                        "Oxytocin (Incorrect)\n" +
                        "Oxytocin promotes uterine contractions, not the inhibition of GnRH."
        );

        questions.add("What part of the male reproductive system is responsible for the production of sperm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Testes", // Correct answer
                "Vas deferens",
                "Prostate gland",
                "Seminal vesicles"
        )));
        correctAnswers.add("Testes");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Testes (Correct answer)\n" +
                        "Sperm are produced in the testes, specifically in the seminiferous tubules.\n\n" +
                        "Vas deferens (Incorrect)\n" +
                        "The vas deferens transports sperm, but it does not produce them.\n\n" +
                        "Prostate gland (Incorrect)\n" +
                        "The prostate contributes fluids to semen but does not produce sperm.\n\n" +
                        "Seminal vesicles (Incorrect)\n" +
                        "The seminal vesicles produce seminal fluid, but not sperm."
        );

        questions.add("During the first trimester, the placenta is responsible for the secretion of which hormone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "hCG", // Correct answer
                "Prolactin",
                "Progesterone",
                "Estrogen"
        )));
        correctAnswers.add("hCG");
        rationales.put(61,
                "RATIONALE:\n" +
                        "hCG (Correct answer)\n" +
                        "Human chorionic gonadotropin (hCG) is secreted by the placenta during the first trimester and helps maintain the corpus luteum.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Prolactin is important for milk production, not for maintaining pregnancy in the first trimester.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone is secreted later by the placenta but isn't the primary hormone in early pregnancy.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen levels increase during pregnancy, but hCG is the key hormone in the first trimester."
        );

        questions.add("Which layer of the uterus is involved in contractions during labor?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myometrium", // Correct answer
                "Endometrium",
                "Perimetrium",
                "Basal layer"
        )));
        correctAnswers.add("Myometrium");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Myometrium (Correct answer)\n" +
                        "The myometrium is the muscular layer of the uterus that contracts during labor.\n\n" +
                        "Endometrium (Incorrect)\n" +
                        "The endometrium is the inner lining of the uterus that thickens for pregnancy but is not involved in contractions.\n\n" +
                        "Perimetrium (Incorrect)\n" +
                        "The perimetrium is the outer layer of the uterus and is not responsible for contractions.\n\n" +
                        "Basal layer (Incorrect)\n" +
                        "The basal layer is part of the endometrium and does not contribute to uterine contractions."
        );

        questions.add("The fertilized egg divides to form a:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Morula", // Correct answer
                "Zygote",
                "Blastocyst",
                "Embryo"
        )));
        correctAnswers.add("Morula");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Morula (Correct answer)\n" +
                        "After fertilization, the zygote undergoes several divisions to form a morula.\n\n" +
                        "Zygote (Incorrect)\n" +
                        "The zygote is the fertilized egg, not the result of cell division.\n\n" +
                        "Blastocyst (Incorrect)\n" +
                        "The blastocyst forms after the morula and is the structure that implants into the uterine wall.\n\n" +
                        "Embryo (Incorrect)\n" +
                        "The embryo forms after the blastocyst implants, not immediately after fertilization."
        );

        questions.add("What is the function of the chorionic villi?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Facilitate maternal-fetal gas exchange", // Correct answer
                "Provide nutrients to the developing embryo",
                "Produce hCG",
                "Store fetal blood"
        )));
        correctAnswers.add("Facilitate maternal-fetal gas exchange");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Facilitate maternal-fetal gas exchange (Correct answer)\n" +
                        "The chorionic villi are part of the placenta and facilitate the exchange of oxygen, carbon dioxide, and other gases between mother and fetus.\n\n" +
                        "Provide nutrients to the developing embryo (Incorrect)\n" +
                        "While the chorionic villi contribute to nutrient exchange, they specifically facilitate maternal-fetal gas exchange.\n\n" +
                        "Produce hCG (Incorrect)\n" +
                        "The chorionic villi do not produce hCG, though they are involved in the production of other substances like hormones.\n\n" +
                        "Store fetal blood (Incorrect)\n" +
                        "The chorionic villi are not involved in storing blood."
        );

        questions.add("Which of the following best describes a full-term pregnancy?");
        choices.add(new ArrayList<>(Arrays.asList(
                "37–40 weeks", // Correct answer
                "35–36 weeks",
                "41–42 weeks",
                "42–44 weeks"
        )));
        correctAnswers.add("37–40 weeks");
        rationales.put(65,
                "RATIONALE:\n" +
                        "37–40 weeks (Correct answer)\n" +
                        "Full-term pregnancies typically occur between 37 and 40 weeks.\n\n" +
                        "35–36 weeks (Incorrect)\n" +
                        "This is considered preterm; full-term pregnancies start from 37 weeks.\n\n" +
                        "41–42 weeks (Incorrect)\n" +
                        "This refers to post-term pregnancies, which may require medical intervention.\n\n" +
                        "42–44 weeks (Incorrect)\n" +
                        "This is considered prolonged or post-term pregnancy."
        );

        questions.add("What structure forms the umbilical cord?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Chorion and allantois", // Correct answer
                "Yolk sac and placenta",
                "Amniotic sac and placenta",
                "Fetal and maternal tissues"
        )));
        correctAnswers.add("Chorion and allantois");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Chorion and allantois (Correct answer)\n" +
                        "The chorion and allantois contribute to forming the umbilical cord, which connects the fetus to the placenta.\n\n" +
                        "Yolk sac and placenta (Incorrect)\n" +
                        "The yolk sac is involved early but not in forming the umbilical cord.\n\n" +
                        "Amniotic sac and placenta (Incorrect)\n" +
                        "The amniotic sac is not directly involved in the umbilical cord formation.\n\n" +
                        "Fetal and maternal tissues (Incorrect)\n" +
                        "While the umbilical cord connects both, the primary structures are the chorion and allantois."
        );

        questions.add("Fetal hemoglobin differs from adult hemoglobin in that it:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Has a higher affinity for oxygen", // Correct answer
                "Has a lower affinity for oxygen",
                "Carries more CO₂",
                "Is produced only during the first trimester"
        )));
        correctAnswers.add("Has a higher affinity for oxygen");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Has a higher affinity for oxygen (Correct answer)\n" +
                        "Fetal hemoglobin binds oxygen more strongly than adult hemoglobin, aiding oxygen transfer from the mother.\n\n" +
                        "Has a lower affinity for oxygen (Incorrect)\n" +
                        "Fetal hemoglobin has a higher affinity for oxygen to extract oxygen from maternal blood.\n\n" +
                        "Carries more CO₂ (Incorrect)\n" +
                        "Fetal hemoglobin is designed for oxygen transport, not carbon dioxide.\n\n" +
                        "Is produced only during the first trimester (Incorrect)\n" +
                        "Fetal hemoglobin is produced throughout fetal development but is more prevalent during early stages."
        );

        questions.add("Which of the following is NOT a function of the placenta?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Acts as a barrier to infections", // Correct answer
                "Provides oxygen and nutrients to the fetus",
                "Produces hCG to maintain the corpus luteum",
                "Stores fetal waste products"
        )));
        correctAnswers.add("Acts as a barrier to infections");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Acts as a barrier to infections (Correct answer)\n" +
                        "The placenta does not act as a complete barrier to infections. Some pathogens can cross it.\n\n" +
                        "Provides oxygen and nutrients to the fetus (Incorrect)\n" +
                        "This is a key function of the placenta.\n\n" +
                        "Produces hCG to maintain the corpus luteum (Incorrect)\n" +
                        "The placenta produces hCG in early pregnancy to sustain the corpus luteum.\n\n" +
                        "Stores fetal waste products (Incorrect)\n" +
                        "The placenta does not store waste; it helps eliminate it."
        );

        questions.add("The fetal heartbeat is first detected around:");
        choices.add(new ArrayList<>(Arrays.asList(
                "6 weeks", // Correct answer
                "4 weeks",
                "8 weeks",
                "12 weeks"
        )));
        correctAnswers.add("6 weeks");
        rationales.put(69,
                "RATIONALE:\n" +
                        "6 weeks (Correct answer)\n" +
                        "The fetal heartbeat is usually detected around 6 weeks using an ultrasound.\n\n" +
                        "4 weeks (Incorrect)\n" +
                        "The fetal heartbeat is typically not detectable at 4 weeks.\n\n" +
                        "8 weeks (Incorrect)\n" +
                        "While the heart is formed, detection of the heartbeat is typically earlier.\n\n" +
                        "12 weeks (Incorrect)\n" +
                        "The heartbeat may be audible by 12 weeks with a Doppler device, but it's detected earlier with ultrasound."
        );

        questions.add("What is the purpose of the amniotic sac?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Protects the fetus by absorbing shocks", // Correct answer
                "Secretes nutrients for the fetus",
                "Serves as the site for gas exchange",
                "Stores fetal blood"
        )));
        correctAnswers.add("Protects the fetus by absorbing shocks");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Protects the fetus by absorbing shocks (Correct answer)\n" +
                        "The amniotic sac and its fluid provide cushioning and protect the fetus from physical shocks.\n\n" +
                        "Secretes nutrients for the fetus (Incorrect)\n" +
                        "Nutrients are delivered through the placenta, not the amniotic sac.\n\n" +
                        "Serves as the site for gas exchange (Incorrect)\n" +
                        "Gas exchange occurs at the placenta, not the amniotic sac.\n\n" +
                        "Stores fetal blood (Incorrect)\n" +
                        "The amniotic sac does not store blood."
        );

        questions.add("The term 'quickening' refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The first movement of the fetus felt by the mother", // Correct answer
                "The first breath taken by the neonate",
                "The onset of labor",
                "The first signs of pregnancy"
        )));
        correctAnswers.add("The first movement of the fetus felt by the mother");
        rationales.put(71,
                "RATIONALE:\n" +
                        "The first movement of the fetus felt by the mother (Correct answer)\n" +
                        "Quickening refers to the first perceptible fetal movements felt by the mother.\n\n" +
                        "The first breath taken by the neonate (Incorrect)\n" +
                        "This is called the neonatal breath, not quickening.\n\n" +
                        "The onset of labor (Incorrect)\n" +
                        "Labor onset is not quickening; it involves contractions and cervical changes.\n\n" +
                        "The first signs of pregnancy (Incorrect)\n" +
                        "Quickening occurs after early pregnancy signs, typically around 18-20 weeks."
        );

        questions.add("At what stage of fetal development are most organs fully formed?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Early second trimester", // Correct answer
                "First trimester",
                "Late second trimester",
                "Third trimester"
        )));
        correctAnswers.add("Early second trimester");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Early second trimester (Correct answer)\n" +
                        "By the early second trimester, most organ systems are well-formed and functioning.\n\n" +
                        "First trimester (Incorrect)\n" +
                        "The first trimester is primarily when organ systems begin to form, but they're not fully developed.\n\n" +
                        "Late second trimester (Incorrect)\n" +
                        "While development continues, many organs are already developed by the early second trimester.\n\n" +
                        "Third trimester (Incorrect)\n" +
                        "The third trimester focuses on growth and maturation rather than forming new organs."
        );

        questions.add("Which part of the fetal brain controls vital functions such as heart rate and respiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Brainstem", // Correct answer
                "Cerebrum",
                "Thalamus",
                "Hypothalamus"
        )));
        correctAnswers.add("Brainstem");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Brainstem (Correct answer)\n" +
                        "The brainstem controls vital functions such as heart rate and respiration.\n\n" +
                        "Cerebrum (Incorrect)\n" +
                        "The cerebrum is involved in higher functions like thinking, not vital functions.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "The thalamus relays sensory information but does not control vital functions.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "The hypothalamus regulates hormonal functions but not directly vital functions like respiration and heart rate."
        );

        questions.add("Which hormone is produced by the placenta and helps in the relaxation of the uterus and prevents premature contractions?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Relaxin", // Correct answer
                "Prolactin",
                "Estrogen",
                "Progesterone"
        )));
        correctAnswers.add("Relaxin");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Relaxin (Correct answer)\n" +
                        "Relaxin helps relax the uterus and softens the cervix, reducing the risk of premature contractions.\n\n" +
                        "Prolactin (Incorrect)\n" +
                        "Prolactin is involved in milk production, not uterine relaxation.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen promotes uterine growth but does not specifically prevent premature contractions.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone supports pregnancy but does not prevent contractions directly; relaxin plays a more specific role."
        );

        questions.add("Which of the following is a common cause of female infertility?");
        choices.add(new ArrayList<>(Arrays.asList(
                "All of the above", // Correct answer
                "Ovarian cysts",
                "Endometriosis",
                "Polycystic ovary syndrome (PCOS)"
        )));
        correctAnswers.add("All of the above");
        rationales.put(75,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Ovarian cysts, endometriosis, and PCOS are all common causes of female infertility.\n\n" +
                        "Ovarian cysts (Incorrect)\n" +
                        "Ovarian cysts can cause infertility, but they are not the most common cause.\n\n" +
                        "Endometriosis (Incorrect)\n" +
                        "Endometriosis can lead to infertility, but it's not the most common cause.\n\n" +
                        "Polycystic ovary syndrome (PCOS) (Incorrect)\n" +
                        "PCOS is a common cause of infertility, but not the only cause."
        );

        questions.add("Which of the following is a common symptom of menopause?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hot flashes", // Correct answer
                "Excessive hair growth",
                "Increased fertility",
                "Increased libido"
        )));
        correctAnswers.add("Hot flashes");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Hot flashes (Correct answer)\n" +
                        "Hot flashes are a hallmark and most frequent symptom of menopause due to fluctuating estrogen levels.\n\n" +
                        "Excessive hair growth (Incorrect)\n" +
                        "While some women experience mild androgen-related changes, it's not the most common symptom.\n\n" +
                        "Increased fertility (Incorrect)\n" +
                        "Fertility declines significantly in menopause.\n\n" +
                        "Increased libido (Incorrect)\n" +
                        "Libido often decreases due to hormonal changes, though it can vary individually."
        );

        questions.add("The process by which a sperm and egg unite is called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fertilization", // Correct answer
                "Spermatogenesis",
                "Oogenesis",
                "Mitosis"
        )));
        correctAnswers.add("Fertilization");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Fertilization (Correct answer)\n" +
                        "Fertilization is the union of sperm and egg.\n\n" +
                        "Spermatogenesis (Incorrect)\n" +
                        "This is the process of sperm production.\n\n" +
                        "Oogenesis (Incorrect)\n" +
                        "This refers to egg formation.\n\n" +
                        "Mitosis (Incorrect)\n" +
                        "Mitosis is a type of cell division, not gamete union."
        );

        questions.add("A normal karyotype of a male is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "XY", // Correct answer
                "XX",
                "XXX",
                "XO"
        )));
        correctAnswers.add("XY");
        rationales.put(78,
                "RATIONALE:\n" +
                        "XY (Correct answer)\n" +
                        "This is the typical male chromosomal pattern.\n\n" +
                        "XX (Incorrect)\n" +
                        "This denotes a typical female.\n\n" +
                        "XXX (Incorrect)\n" +
                        "This represents a chromosomal abnormality in females.\n\n" +
                        "XO (Incorrect)\n" +
                        "This indicates Turner syndrome in females."
        );

        questions.add("Which of the following is a key difference between primary and secondary sex characteristics?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Primary sex characteristics are present at birth, while secondary appear during puberty", // Correct answer
                "Primary sex characteristics are directly involved in reproduction, while secondary are not",
                "Secondary sex characteristics involve external genitalia, while primary do not",
                "Secondary sex characteristics are only present in females"
        )));
        correctAnswers.add("Primary sex characteristics are present at birth, while secondary appear during puberty");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Primary sex characteristics are present at birth, while secondary appear during puberty (Correct answer)\n" +
                        "Primary characteristics (e.g., ovaries, testes) are directly involved in reproduction, while secondary traits (e.g., body hair, voice changes) appear later.\n\n" +
                        "Primary sex characteristics are directly involved in reproduction, while secondary are not (Incorrect)\n" +
                        "This is true, but it doesn't fully explain the difference in timing between primary and secondary sex characteristics.\n\n" +
                        "Secondary sex characteristics involve external genitalia, while primary do not (Incorrect)\n" +
                        "External genitalia are primary characteristics.\n\n" +
                        "Secondary sex characteristics are only present in females (Incorrect)\n" +
                        "Secondary characteristics occur in both sexes, not just females."
        );

        questions.add("Which of the following could be a consequence of a failure to ovulate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Amenorrhea", // Correct answer
                "Ovarian cancer",
                "Prolactinoma",
                "Dysmenorrhea"
        )));
        correctAnswers.add("Amenorrhea");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Amenorrhea (Correct answer)\n" +
                        "A lack of ovulation often leads to missed or absent periods, which is the definition of amenorrhea.\n\n" +
                        "Ovarian cancer (Incorrect)\n" +
                        "There is no direct link between failure to ovulate and ovarian cancer.\n\n" +
                        "Prolactinoma (Incorrect)\n" +
                        "Prolactinoma is a pituitary tumor and not directly related to ovulation failure.\n\n" +
                        "Dysmenorrhea (Incorrect)\n" +
                        "Dysmenorrhea refers to painful menstruation, typically not caused by anovulation."
        );

        questions.add("A Pap smear is primarily used to detect:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cervical cancer", // Correct answer
                "Ovarian cysts",
                "Uterine fibroids",
                "Endometriosis"
        )));
        correctAnswers.add("Cervical cancer");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Cervical cancer (Correct answer)\n" +
                        "A Pap smear is primarily used to screen for cervical cancer by detecting abnormal cells in the cervix.\n\n" +
                        "Ovarian cysts (Incorrect)\n" +
                        "Ovarian cysts are typically detected by ultrasound, not a Pap smear.\n\n" +
                        "Uterine fibroids (Incorrect)\n" +
                        "Uterine fibroids are detected by imaging techniques such as ultrasound.\n\n" +
                        "Endometriosis (Incorrect)\n" +
                        "Endometriosis is diagnosed through laparoscopy, not by a Pap smear."
        );

        questions.add("Which of the following is associated with polycystic ovary syndrome (PCOS)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "All of the above", // Correct answer
                "Irregular periods",
                "Excessive androgen production",
                "Insulin resistance"
        )));
        correctAnswers.add("All of the above");
        rationales.put(82,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "PCOS is characterized by irregular periods, excessive androgen production, and insulin resistance, which are all hallmark features.\n\n" +
                        "Irregular periods (Incorrect)\n" +
                        "Irregular periods are a common symptom of PCOS, but they are not the only feature.\n\n" +
                        "Excessive androgen production (Incorrect)\n" +
                        "Excessive androgen production leads to symptoms like hirsutism, but it is not the only aspect of PCOS.\n\n" +
                        "Insulin resistance (Incorrect)\n" +
                        "Insulin resistance is frequently associated with PCOS, but it is not the only defining factor."
        );

        questions.add("What is a major risk factor for developing ovarian cancer?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Advanced age", // Correct answer
                "Early menopause",
                "Hormone replacement therapy",
                "Having multiple children"
        )));
        correctAnswers.add("Advanced age");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Advanced age (Correct answer)\n" +
                        "The biggest risk factor for ovarian cancer is increasing age, especially after the age of 50.\n\n" +
                        "Early menopause (Incorrect)\n" +
                        "Early menopause actually decreases the risk of ovarian cancer.\n\n" +
                        "Hormone replacement therapy (Incorrect)\n" +
                        "Hormone replacement therapy may slightly increase the risk, but advanced age remains the most significant factor.\n\n" +
                        "Having multiple children (Incorrect)\n" +
                        "Having multiple children can lower the risk of ovarian cancer, not increase it."
        );

        questions.add("Which of the following diseases is commonly associated with male infertility?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Varicocele", // Correct answer
                "Prostate cancer",
                "Testicular torsion",
                "Endometriosis"
        )));
        correctAnswers.add("Varicocele");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Varicocele (Correct answer)\n" +
                        "Varicocele, which involves enlarged veins in the scrotum, is the most common cause of male infertility.\n\n" +
                        "Prostate cancer (Incorrect)\n" +
                        "Prostate cancer can affect fertility but is less common as a direct cause of infertility.\n\n" +
                        "Testicular torsion (Incorrect)\n" +
                        "Testicular torsion is a medical emergency, but it is a rare cause of infertility.\n\n" +
                        "Endometriosis (Incorrect)\n" +
                        "Endometriosis is a condition that affects females, not males."
        );

        questions.add("During menopause, the levels of which hormone typically decrease?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Progesterone",
                "LH",
                "All of the above"
        )));
        correctAnswers.add("All of the above");
        rationales.put(85,
                "RATIONALE:\n" +
                        "All of the above (Correct answer)\n" +
                        "Except LH which rises; this makes the question ambiguous as phrased. The better choice would be A or B.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Decreases.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Decreases.\n\n" +
                        "LH (Incorrect)\n" +
                        "Increases."
        );

        questions.add("What is the function of the bulbourethral glands in males?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Secrete testosterone",
                "Secrete fluid to lubricate the urethra",
                "Secrete enzymes that activate sperm",
                "Produce semen"
        )));
        correctAnswers.add("Secrete fluid to lubricate the urethra");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Secrete fluid to lubricate the urethra (Correct answer)\n" +
                        "Clear fluid precedes semen.\n\n" +
                        "Secrete testosterone (Incorrect)\n" +
                        "Done by testes.\n\n" +
                        "Secrete enzymes that activate sperm (Incorrect)\n" +
                        "Prostate gland does this.\n\n" +
                        "Produce semen (Incorrect)\n" +
                        "Semen comes from several glands combined."
        );

        questions.add("A miscarriage in the first trimester is most commonly due to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Maternal stress",
                "Genetic abnormalities",
                "Maternal infection",
                "Maternal age"
        )));
        correctAnswers.add("Genetic abnormalities");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Genetic abnormalities (Correct answer)\n" +
                        "Chromosomal errors cause 50-60% of early losses.\n\n" +
                        "Maternal stress (Incorrect)\n" +
                        "Not the primary cause.\n\n" +
                        "Maternal infection (Incorrect)\n" +
                        "Less common.\n\n" +
                        "Maternal age (Incorrect)\n" +
                        "Increases risk but indirect."
        );

        questions.add("Which hormone is responsible for the development of secondary sexual characteristics in males?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Estrogen",
                "Progesterone",
                "Testosterone",
                "FSH"
        )));
        correctAnswers.add("Testosterone");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Testosterone (Correct answer)\n" +
                        "Responsible for muscle mass, hair, voice deepening.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Female hormone.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Female hormone.\n\n" +
                        "FSH (Incorrect)\n" +
                        "Regulates spermatogenesis, not secondary traits."
        );

        questions.add("Which of the following best describes an ectopic pregnancy?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Implantation of the embryo outside the uterine cavity",
                "Pregnancy that occurs in the cervix",
                "Fetal development in the fallopian tubes",
                "Implantation of the embryo within the ovary"
        )));
        correctAnswers.add("Implantation of the embryo outside the uterine cavity");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Implantation outside uterine cavity (Correct answer)\n\n" +
                        "Pregnancy in cervix (Incorrect)\n" +
                        "Rare, a type of ectopic.\n\n" +
                        "Fetal development in fallopian tubes (Incorrect)\n" +
                        "One type of ectopic.\n\n" +
                        "Implantation in ovary (Incorrect)\n" +
                        "Rare, also ectopic."
        );

        questions.add("The ovarian cycle is regulated primarily by hormones from which gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Anterior pituitary", // Correct answer
                "Pineal gland",
                "Posterior pituitary",
                "Adrenal glands"
        )));
        correctAnswers.add("Anterior pituitary");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Anterior pituitary (Correct answer)\n" +
                        "The anterior pituitary releases FSH and LH, which regulate the ovarian cycle.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "The pineal gland is involved in regulating sleep cycles, not the ovarian cycle.\n\n" +
                        "Posterior pituitary (Incorrect)\n" +
                        "The posterior pituitary releases oxytocin and ADH, not hormones that regulate the ovarian cycle.\n\n" +
                        "Adrenal glands (Incorrect)\n" +
                        "The adrenal glands produce corticosteroids and androgens but do not regulate the ovarian cycle."
        );

        questions.add("Which of the following is a key feature of a karyotype analysis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Provides a visual representation of chromosomes", // Correct answer
                "Determines the type of hormone imbalance",
                "Identifies the presence of ovarian cysts",
                "Measures ovarian reserve"
        )));
        correctAnswers.add("Provides a visual representation of chromosomes");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Provides a visual representation of chromosomes (Correct answer)\n" +
                        "A karyotype provides a visual representation of the chromosomes in an individual, used for identifying genetic disorders.\n\n" +
                        "Determines the type of hormone imbalance (Incorrect)\n" +
                        "Karyotyping is not used for hormone analysis.\n\n" +
                        "Identifies the presence of ovarian cysts (Incorrect)\n" +
                        "Ovarian cysts are typically identified through ultrasound, not karyotyping.\n\n" +
                        "Measures ovarian reserve (Incorrect)\n" +
                        "Ovarian reserve is measured by tests like AMH, not karyotyping."
        );

        questions.add("Which of the following is a characteristic of male puberty?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Growth of facial hair", // Correct answer
                "Decrease in size of the testes",
                "Increase in body fat",
                "Decrease in testosterone levels"
        )));
        correctAnswers.add("Growth of facial hair");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Growth of facial hair (Correct answer)\n" +
                        "During male puberty, the growth of facial, pubic, and body hair is a key secondary sex characteristic.\n\n" +
                        "Decrease in size of the testes (Incorrect)\n" +
                        "The testes enlarge during puberty, not decrease in size.\n\n" +
                        "Increase in body fat (Incorrect)\n" +
                        "Male puberty typically involves an increase in muscle mass rather than body fat.\n\n" +
                        "Decrease in testosterone levels (Incorrect)\n" +
                        "Testosterone levels actually rise during puberty to initiate physical changes."
        );

        questions.add("What structure produces the majority of estrogen in a female?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ovarian follicles", // Correct answer
                "Uterine lining",
                "Pituitary gland",
                "Adrenal glands"
        )));
        correctAnswers.add("Ovarian follicles");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Ovarian follicles (Correct answer)\n" +
                        "The developing ovarian follicles are the primary source of estrogen production before ovulation.\n\n" +
                        "Uterine lining (Incorrect)\n" +
                        "The uterine lining responds to estrogen but does not produce it.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary gland produces FSH and LH, which stimulate estrogen production in the ovaries.\n\n" +
                        "Adrenal glands (Incorrect)\n" +
                        "The adrenal glands produce small amounts of estrogen, but not the majority."
        );

        questions.add("A zygote is formed after:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fertilization", // Correct answer
                "Meiosis",
                "Mitosis",
                "Oogenesis"
        )));
        correctAnswers.add("Fertilization");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Fertilization (Correct answer)\n" +
                        "A zygote is formed when sperm fertilizes an egg, combining their genetic material.\n\n" +
                        "Meiosis (Incorrect)\n" +
                        "Meiosis forms gametes (egg and sperm), not a zygote.\n\n" +
                        "Mitosis (Incorrect)\n" +
                        "Mitosis occurs after fertilization to enable cell division in the zygote and embryo.\n\n" +
                        "Oogenesis (Incorrect)\n" +
                        "Oogenesis is the process of forming an ovum (egg cell), not a zygote."
        );

        questions.add("Which of the following genetic disorders is linked to an extra X chromosome in males?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Klinefelter syndrome", // Correct answer
                "Down syndrome",
                "Turner syndrome",
                "Hemophilia"
        )));
        correctAnswers.add("Klinefelter syndrome");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Klinefelter syndrome (Correct answer)\n" +
                        "Klinefelter syndrome is characterized by a 47,XXY karyotype, which includes an extra X chromosome in males.\n\n" +
                        "Down syndrome (Incorrect)\n" +
                        "Down syndrome is caused by trisomy 21, where there is an extra chromosome 21, not an extra X chromosome.\n\n" +
                        "Turner syndrome (Incorrect)\n" +
                        "Turner syndrome affects females and is characterized by a single X chromosome (45,X).\n\n" +
                        "Hemophilia (Incorrect)\n" +
                        "Hemophilia is a sex-linked bleeding disorder, not caused by an extra chromosome."
        );

        questions.add("Which of the following hormones peaks just before ovulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "LH", // Correct answer
                "FSH",
                "Estrogen",
                "Progesterone"
        )));
        correctAnswers.add("LH");
        rationales.put(96,
                "RATIONALE:\n" +
                        "LH (Correct answer)\n" +
                        "LH surges just before ovulation, triggering the release of the egg.\n\n" +
                        "FSH (Incorrect)\n" +
                        "FSH rises during the follicular phase but does not peak immediately before ovulation.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen rises during the follicular phase, but the peak that precedes ovulation is caused by LH.\n\n" +
                        "Progesterone (Incorrect)\n" +
                        "Progesterone increases after ovulation, during the luteal phase."
        );

        questions.add("What is the first step in the menstrual cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Menstruation", // Correct answer
                "Ovulation",
                "Follicular phase",
                "Luteal phase"
        )));
        correctAnswers.add("Menstruation");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Menstruation (Correct answer)\n" +
                        "The menstrual cycle begins with menstruation, where the uterine lining is shed.\n\n" +
                        "Ovulation (Incorrect)\n" +
                        "Ovulation occurs midway through the cycle, not at the start.\n\n" +
                        "Follicular phase (Incorrect)\n" +
                        "The follicular phase follows menstruation, during which the follicles in the ovaries mature.\n\n" +
                        "Luteal phase (Incorrect)\n" +
                        "The luteal phase begins after ovulation, not at the start of the cycle."
        );

        questions.add("The primary function of the vas deferens is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Transport sperm from the epididymis to the ejaculatory duct", // Correct answer
                "Produce sperm",
                "Produce semen",
                "Store sperm"
        )));
        correctAnswers.add("Transport sperm from the epididymis to the ejaculatory duct");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Transport sperm from the epididymis to the ejaculatory duct (Correct answer)\n" +
                        "The vas deferens carries mature sperm from the epididymis to the ejaculatory duct.\n\n" +
                        "Produce sperm (Incorrect)\n" +
                        "Sperm production occurs in the testes, not the vas deferens.\n\n" +
                        "Produce semen (Incorrect)\n" +
                        "Semen is produced by the seminal vesicles, prostate, and bulbourethral glands.\n\n" +
                        "Store sperm (Incorrect)\n" +
                        "Sperm is temporarily stored in the epididymis, not in the vas deferens."
        );

        questions.add("Which of the following occurs during the luteal phase of the menstrual cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The corpus luteum secretes progesterone", // Correct answer
                "The follicle is maturing",
                "Ovulation occurs",
                "The endometrial lining is shed"
        )));
        correctAnswers.add("The corpus luteum secretes progesterone");
        rationales.put(99,
                "RATIONALE:\n" +
                        "The corpus luteum secretes progesterone (Correct answer)\n" +
                        "After ovulation, the corpus luteum forms and secretes progesterone to prepare the endometrium for potential implantation.\n\n" +
                        "The follicle is maturing (Incorrect)\n" +
                        "Follicle maturation occurs in the follicular phase, not the luteal phase.\n\n" +
                        "Ovulation occurs (Incorrect)\n" +
                        "Ovulation occurs before the luteal phase, marking the end of the follicular phase.\n\n" +
                        "The endometrial lining is shed (Incorrect)\n" +
                        "The shedding of the endometrial lining occurs during menstruation, not the luteal phase."
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

        rationaleCard.setVisibility(View.GONE);
        skipButton.setEnabled(true); // Re-enable skip for the next question

    }

    private void checkAnswer(Button selectedButton) {
        int realIndex = questionOrder.get(currentIndex);
        String correct = correctAnswers.get(realIndex);
        String selectedText = selectedButton.getText().toString();

        setButtonsEnabled(false);
        hasAnswered = true;

        if (selectedText.equals(correct)) {
            correctAnswersCount++;
            selectedButton.setBackgroundResource(R.drawable.correct_border);
        } else {
            selectedButton.setBackgroundResource(R.drawable.incorrect_border);
            highlightCorrectAnswer(correct);
        }

        new Handler().postDelayed(() -> showRationale(realIndex), 800); // Delay for rationale display
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
        rationaleCard.setVisibility(View.VISIBLE);
        skipButton.setEnabled(false); // Disable skip while rationale is visible


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
        new AlertDialog.Builder(Prac_4_Reproductive.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
