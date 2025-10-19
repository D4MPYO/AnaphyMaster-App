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

public class Prac_10_Digestive extends AppCompatActivity {

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

        setContentView(R.layout.prac_10_digestive);

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
                Toast.makeText(Prac_10_Digestive.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_10_Digestive.this)
                    .setTitle("Restart Quiz")
                    .setMessage("Are you sure you want to restart? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> resetQuiz())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        exitIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_10_Digestive.this)
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
                new AlertDialog.Builder(Prac_10_Digestive.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_10_Digestive.this, Answer_Result.class);
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

                new AlertDialog.Builder(Prac_10_Digestive.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_10_Digestive.this, Answer_Result.class);
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

        questions.add("What is the primary function of the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To regulate body temperature",
                "To break down food into nutrients for energy and discard indigestible waste",
                "To produce hormones for growth",
                "To store blood cells"
        )));
        correctAnswers.add("To break down food into nutrients for energy and discard indigestible waste");
        rationales.put(0,
                "RATIONALE:\n" +
                        "To break down food into nutrients for energy and discard indigestible waste (Correct answer)\n" +
                        "The digestive system converts ingested food into nutrients that fuel the body while discarding waste.\n\n" +
                        "To regulate body temperature (Incorrect)\n" +
                        "Regulating body temperature is primarily managed by the hypothalamus and the circulatory system, not by digestion.\n\n" +
                        "To produce hormones for growth (Incorrect)\n" +
                        "While some hormones are produced in related endocrine organs, the main function of the digestive system is not hormone production.\n\n" +
                        "To store blood cells (Incorrect)\n" +
                        "Blood cell synthesis takes place in the bone marrow, not in the digestive system."
        );

        questions.add("Where does the digestive process begin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stomach",
                "Esophagus",
                "Mouth",
                "Small intestine"
        )));
        correctAnswers.add("Mouth");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Mouth (Correct answer)\n" +
                        "The process starts at the mouth, where food is ingested, chewed, and mixed with saliva.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "Although the stomach is vital for digestion, the process begins earlier.\n\n" +
                        "Esophagus (Incorrect)\n" +
                        "The esophagus merely transports food; it does not start the digestion.\n\n" +
                        "Small intestine (Incorrect)\n" +
                        "The small intestine is where most nutrient absorption occurs, but it is not the starting point of digestion."
        );

        questions.add("What role do salivary glands play in digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They produce bile",
                "They secrete digestive enzymes such as amylase",
                "They absorb nutrients",
                "They store waste products"
        )));
        correctAnswers.add("They secrete digestive enzymes such as amylase");
        rationales.put(2,
                "RATIONALE:\n" +
                        "They secrete digestive enzymes such as amylase (Correct answer)\n" +
                        "Salivary glands secrete saliva containing amylase, which begins the chemical digestion of carbohydrates.\n\n" +
                        "They produce bile (Incorrect)\n" +
                        "Bile is produced by the liver, not the salivary glands.\n\n" +
                        "They absorb nutrients (Incorrect)\n" +
                        "Nutrient absorption occurs mainly in the small intestine.\n\n" +
                        "They store waste products (Incorrect)\n" +
                        "Waste storage is not a function of salivary glands."
        );

        questions.add("Which process in the mouth primarily aids in breaking food into smaller pieces?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Chemical digestion",
                "Propulsion",
                "Mastication (chewing)",
                "Absorption"
        )));
        correctAnswers.add("Mastication (chewing)");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Mastication (chewing) (Correct answer)\n" +
                        "Mastication mechanically breaks food into smaller pieces, increasing its surface area.\n\n" +
                        "Chemical digestion (Incorrect)\n" +
                        "Although there is minimal chemical digestion (via amylase), the primary mechanical breakdown occurs by chewing.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion refers to moving food down the tract, not breaking it down.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption is mostly a function of the small intestine."
        );

        questions.add("How is food transported from the mouth to the stomach?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By diffusion",
                "By peristaltic waves in the esophagus",
                "By gravity",
                "By simple swallowing without muscular action"
        )));
        correctAnswers.add("By peristaltic waves in the esophagus");
        rationales.put(4,
                "RATIONALE:\n" +
                        "By peristaltic waves in the esophagus (Correct answer)\n" +
                        "The esophagus uses peristalsis—wave-like muscular contractions—to move food to the stomach.\n\n" +
                        "By diffusion (Incorrect)\n" +
                        "Diffusion is not an active mechanism for moving bulky food through the digestive tract.\n\n" +
                        "By gravity (Incorrect)\n" +
                        "While gravity may assist in a minor way, the process is primarily muscular.\n\n" +
                        "By simple swallowing without muscular action (Incorrect)\n" +
                        "Swallowing includes muscular contractions (peristalsis) to move food along."
        );

        questions.add("What function is performed by peristalsis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorbing nutrients",
                "Muscular churning of food",
                "Propelling food along the digestive tract",
                "Breaking down macromolecules"
        )));
        correctAnswers.add("Propelling food along the digestive tract");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Propelling food along the digestive tract (Correct answer)\n" +
                        "Peristaltic waves move food along the gastrointestinal tract.\n\n" +
                        "Absorbing nutrients (Incorrect)\n" +
                        "Nutrient absorption occurs primarily in the small intestine.\n\n" +
                        "Muscular churning of food (Incorrect)\n" +
                        "Churning is performed in the stomach; peristalsis specifically refers to propelling food.\n\n" +
                        "Breaking down macromolecules (Incorrect)\n" +
                        "That is the role of chemical digestion (enzymes and acids)."
        );

        questions.add("Which organ functions as an acid- and enzyme-producing chamber in the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stomach",
                "Small intestine",
                "Large intestine",
                "Gallbladder"
        )));
        correctAnswers.add("Stomach");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Stomach (Correct answer)\n" +
                        "The stomach produces gastric juices (acid and enzymes such as pepsin) and churns food to form chyme.\n\n" +
                        "Small intestine (Incorrect)\n" +
                        "Though enzymes are present, the small intestine mainly absorbs nutrients.\n\n" +
                        "Large intestine (Incorrect)\n" +
                        "Its key role is water absorption and waste formation, not chemical digestion.\n\n" +
                        "Gallbladder (Incorrect)\n" +
                        "The gallbladder stores bile but does not secrete acids or digestive enzymes."
        );

        questions.add("What is chyme?");
        choices.add(new ArrayList<>(Arrays.asList(
                "A type of enzyme secreted by the pancreas",
                "A semi-liquid mixture of partially digested food and gastric juices",
                "A hormone produced in the stomach",
                "An indigestible food particle"
        )));
        correctAnswers.add("A semi-liquid mixture of partially digested food and gastric juices");
        rationales.put(7,
                "RATIONALE:\n" +
                        "A semi-liquid mixture of partially digested food and gastric juices (Correct answer)\n" +
                        "Chyme is the mixture formed by the stomach’s mechanical and chemical digestion before entering the small intestine.\n\n" +
                        "A type of enzyme secreted by the pancreas (Incorrect)\n" +
                        "Chyme is not an enzyme but rather a mixture.\n\n" +
                        "A hormone produced in the stomach (Incorrect)\n" +
                        "No hormone by that name is produced in the stomach.\n\n" +
                        "An indigestible food particle (Incorrect)\n" +
                        "Chyme is partially digested food, not merely indigestible matter."
        );

        questions.add("Which section of the digestive system is the main site of nutrient absorption?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mouth",
                "Stomach",
                "Small intestine",
                "Large intestine"
        )));
        correctAnswers.add("Small intestine");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Small intestine (Correct answer)\n" +
                        "The small intestine, with its villi and microvilli, is designed to absorb nutrients efficiently.\n\n" +
                        "Mouth (Incorrect)\n" +
                        "Although a small amount of absorption (e.g., some simple sugars) can occur here, it is not the main site.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "The stomach’s acidic environment is not ideal for nutrient absorption.\n\n" +
                        "Large intestine (Incorrect)\n" +
                        "The large intestine mainly reabsorbs water and electrolytes rather than nutrients."
        );

        questions.add("The duodenum primarily receives digestive juices from which accessory organs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Liver and pancreas (with bile stored in the gallbladder)",
                "Stomach and esophagus",
                "Large intestine and gallbladder",
                "Mouth and pharynx"
        )));
        correctAnswers.add("Liver and pancreas (with bile stored in the gallbladder)");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Liver and pancreas (with bile stored in the gallbladder) (Correct answer)\n" +
                        "The duodenum is the first portion of the small intestine that receives bile from the liver/gallbladder and enzymes from the pancreas.\n\n" +
                        "Stomach and esophagus (Incorrect)\n" +
                        "These parts are involved earlier in the process and do not supply the characteristic secretions.\n\n" +
                        "Large intestine and gallbladder (Incorrect)\n" +
                        "The large intestine is not a source of digestive secretions.\n\n" +
                        "Mouth and pharynx (Incorrect)\n" +
                        "They only initiate digestion; they do not provide the key digestive juices."
        );

        questions.add("Which two segments of the small intestine complete nutrient absorption?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Duodenum and large intestine",
                "Jejunum and ileum", // Correct answer
                "Stomach and duodenum",
                "Esophagus and jejunum"
        )));
        correctAnswers.add("Jejunum and ileum");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Jejunum and ileum (Correct answer)\n" +
                        "After the duodenum, the jejunum and ileum absorb the remaining digested nutrients into the bloodstream.\n\n" +
                        "Duodenum and large intestine (Incorrect)\n" +
                        "The large intestine is mainly for water reabsorption and waste compaction.\n\n" +
                        "Stomach and duodenum (Incorrect)\n" +
                        "The stomach is primarily for digestion, not absorption.\n\n" +
                        "Esophagus and jejunum (Incorrect)\n" +
                        "The esophagus is only a transport conduit, not an absorptive region."
        );

        questions.add("What is the primary function of the large intestine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Major nutrient absorption",
                "Producing digestive enzymes",
                "Absorbing water and electrolytes and forming feces", // Correct answer
                "Churning food into chyme"
        )));
        correctAnswers.add("Absorbing water and electrolytes and forming feces");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Absorbing water and electrolytes and forming feces (Correct answer)\n" +
                        "The large intestine absorbs excess water and electrolytes and compacts waste into feces.\n\n" +
                        "Major nutrient absorption (Incorrect)\n" +
                        "The large intestine absorbs very little in the way of nutrients.\n\n" +
                        "Producing digestive enzymes (Incorrect)\n" +
                        "Enzyme production is not a primary function of the colon.\n\n" +
                        "Churning food into chyme (Incorrect)\n" +
                        "Churning is a function of the stomach, not the large intestine."
        );

        questions.add("What additional role does the large intestine have besides water absorption?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Producing saliva",
                "Hosting a rich microbiome", // Correct answer
                "Storing bile",
                "Secreting insulin"
        )));
        correctAnswers.add("Hosting a rich microbiome");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Hosting a rich microbiome (Correct answer)\n" +
                        "Its resident microbiota helps in digestion, supports immune function, and can even affect mental well-being.\n\n" +
                        "Producing saliva (Incorrect)\n" +
                        "Saliva is produced by the salivary glands in the mouth.\n\n" +
                        "Storing bile (Incorrect)\n" +
                        "The gallbladder stores bile, not the large intestine.\n\n" +
                        "Secreting insulin (Incorrect)\n" +
                        "Insulin is produced by the pancreas."
        );

        questions.add("Which of the following is considered an accessory organ in digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Heart",
                "Liver", // Correct answer
                "Esophagus",
                "Lungs"
        )));
        correctAnswers.add("Liver");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Liver (Correct answer)\n" +
                        "The liver, along with the gallbladder and pancreas, assists in digestion but is not part of the direct food pathway.\n\n" +
                        "Heart (Incorrect)\n" +
                        "The heart is part of the circulatory system.\n\n" +
                        "Esophagus (Incorrect)\n" +
                        "The esophagus is a primary digestive tract organ (a conduit).\n\n" +
                        "Lungs (Incorrect)\n" +
                        "The lungs are part of the respiratory system."
        );

        questions.add("What is the main function of the liver in the digestive process?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorbing proteins",
                "Producing bile to emulsify fats and detoxify chemicals", // Correct answer
                "Churning food",
                "Storing carbohydrates"
        )));
        correctAnswers.add("Producing bile to emulsify fats and detoxify chemicals");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Producing bile to emulsify fats and detoxify chemicals (Correct answer)\n" +
                        "The liver produces bile, an essential substance for fat emulsification and detoxification.\n\n" +
                        "Absorbing proteins (Incorrect)\n" +
                        "Protein absorption occurs in the small intestine, not the liver.\n\n" +
                        "Churning food (Incorrect)\n" +
                        "Churning is a function of the stomach muscles.\n\n" +
                        "Storing carbohydrates (Incorrect)\n" +
                        "Although the liver does store glycogen, this is not its primary digestive role."
        );

        questions.add("The gallbladder is responsible for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Producing digestive enzymes",
                "Storing and concentrating bile for release into the small intestine", // Correct answer
                "Absorbing nutrients",
                "Initiating peristalsis"
        )));
        correctAnswers.add("Storing and concentrating bile for release into the small intestine");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Storing and concentrating bile for release into the small intestine (Correct answer)\n" +
                        "The gallbladder stores bile made by the liver until it is needed for fat digestion.\n\n" +
                        "Producing digestive enzymes (Incorrect)\n" +
                        "The gallbladder only stores bile; it does not produce enzymes.\n\n" +
                        "Absorbing nutrients (Incorrect)\n" +
                        "Nutrient absorption occurs in the small intestine, not the gallbladder.\n\n" +
                        "Initiating peristalsis (Incorrect)\n" +
                        "Peristalsis is a muscular action carried out by the digestive tract."
        );

        questions.add("How does the pancreas contribute to digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It manufactures bile",
                "It produces enzymes that break down proteins, carbohydrates, and fats as well as hormones like insulin", // Correct answer
                "It stores digestive waste",
                "It absorbs water in the intestines"
        )));
        correctAnswers.add("It produces enzymes that break down proteins, carbohydrates, and fats as well as hormones like insulin");
        rationales.put(16,
                "RATIONALE:\n" +
                        "It produces enzymes that break down proteins, carbohydrates, and fats as well as hormones like insulin (Correct answer)\n" +
                        "The pancreas plays both an exocrine role (producing digestive enzymes) and an endocrine role (regulating blood sugar).\n\n" +
                        "It manufactures bile (Incorrect)\n" +
                        "Bile is produced by the liver, not the pancreas.\n\n" +
                        "It stores digestive waste (Incorrect)\n" +
                        "Waste storage is not part of pancreatic function.\n\n" +
                        "It absorbs water in the intestines (Incorrect)\n" +
                        "Water absorption takes place mainly in the intestines."
        );

        questions.add("Which stage of the digestive process involves taking in food?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Propulsion",
                "Ingestion", // Correct answer
                "Absorption",
                "Defecation"
        )));
        correctAnswers.add("Ingestion");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Ingestion (Correct answer)\n" +
                        "Ingestion is the process of taking food into the mouth.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion refers to moving food, not introducing it.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption involves the uptake of nutrients after digestion.\n\n" +
                        "Defecation (Incorrect)\n" +
                        "Defecation is the elimination of waste."
        );

        questions.add("During propulsion, which actions are involved?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Chewing and enzyme secretion",
                "Swallowing and peristalsis", // Correct answer
                "Nutrient absorption and storage",
                "Defecation and waste compaction"
        )));
        correctAnswers.add("Swallowing and peristalsis");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Swallowing and peristalsis (Correct answer)\n" +
                        "Propulsion includes swallowing and peristaltic movements that move food along the tract.\n\n" +
                        "Chewing and enzyme secretion (Incorrect)\n" +
                        "Chewing is part of ingestion and mechanical digestion; enzyme secretion is chemical digestion.\n\n" +
                        "Nutrient absorption and storage (Incorrect)\n" +
                        "Absorption is a separate process that occurs after digestion.\n\n" +
                        "Defecation and waste compaction (Incorrect)\n" +
                        "These occur at the end of the process and are not part of propulsion."
        );

        questions.add("Mechanical digestion refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The breakdown of food by physical processes like chewing and stomach churning", // Correct answer
                "The chemical breakdown by enzymes",
                "The absorption of nutrients",
                "The elimination of waste"
        )));
        correctAnswers.add("The breakdown of food by physical processes like chewing and stomach churning");
        rationales.put(19,
                "RATIONALE:\n" +
                        "The breakdown of food by physical processes like chewing and stomach churning (Correct answer)\n" +
                        "Mechanical digestion physically breaks food into smaller pieces, aiding subsequent chemical digestion.\n\n" +
                        "The chemical breakdown by enzymes (Incorrect)\n" +
                        "That is chemical digestion.\n\n" +
                        "The absorption of nutrients (Incorrect)\n" +
                        "Absorption is a separate stage that follows digestion.\n\n" +
                        "The elimination of waste (Incorrect)\n" +
                        "Waste elimination is called defecation."
        );

        questions.add("Chemical digestion involves:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Physical shredding of food",
                "Enzymes and digestive juices breaking down macromolecules", // Correct answer
                "The movement of food along the gut",
                "The absorption of water"
        )));
        correctAnswers.add("Enzymes and digestive juices breaking down macromolecules");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Enzymes and digestive juices breaking down macromolecules (Correct answer)\n" +
                        "Chemical digestion uses enzymes and acids to decompose food into absorbable molecules.\n\n" +
                        "Physical shredding of food (Incorrect)\n" +
                        "Physical breakdown is mechanical digestion.\n\n" +
                        "The movement of food along the gut (Incorrect)\n" +
                        "That refers to propulsion.\n\n" +
                        "The absorption of water (Incorrect)\n" +
                        "Absorption occurs later, primarily in the small and large intestines."
        );

        questions.add("The final stage of the digestive process is called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ingestion",
                "Propulsion",
                "Defecation", // Correct answer
                "Absorption"
        )));
        correctAnswers.add("Defecation");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Defecation (Correct answer)\n" +
                        "Defecation is when the undigested and waste materials are expelled from the body.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is the entry of food.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion moves food along the tract.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption occurs before waste elimination."
        );

        questions.add("What governs the peristaltic movement in the gastrointestinal tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The endocrine system",
                "The enteric nervous system", // Correct answer
                "The circulatory system",
                "The skeletal system"
        )));
        correctAnswers.add("The enteric nervous system");
        rationales.put(22,
                "RATIONALE:\n" +
                        "The enteric nervous system (Correct answer)\n" +
                        "The enteric nervous system, sometimes called the \"second brain,\" controls peristalsis and other gut functions.\n\n" +
                        "The endocrine system (Incorrect)\n" +
                        "While hormones influence digestion, peristalsis is controlled by the nervous system.\n\n" +
                        "The circulatory system (Incorrect)\n" +
                        "The circulatory system transports blood; it does not control muscle contractions in the gut.\n\n" +
                        "The skeletal system (Incorrect)\n" +
                        "The skeletal system provides support but does not regulate digestion."
        );

        questions.add("The enteric nervous system is often referred to as the “second brain” because it:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Controls heart rate",
                "Functions independently to regulate digestion", // Correct answer
                "Produces cerebrospinal fluid",
                "Is responsible for motor coordination"
        )));
        correctAnswers.add("Functions independently to regulate digestion");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Functions independently to regulate digestion (Correct answer)\n" +
                        "It autonomously governs many aspects of gastrointestinal activity, from motility to enzyme secretion.\n\n" +
                        "Controls heart rate (Incorrect)\n" +
                        "The heart rate is primarily controlled by the autonomic nervous system and central nervous system, not the enteric system.\n\n" +
                        "Produces cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is produced in the brain’s ventricles.\n\n" +
                        "Is responsible for motor coordination (Incorrect)\n" +
                        "Motor coordination is handled by the central nervous system and the cerebellum."
        );

        questions.add("The central nervous system influences digestion by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulating appetite and satiety through signals", // Correct answer
                "Producing digestive enzymes",
                "Absorbing nutrients directly",
                "Generating bile"
        )));
        correctAnswers.add("Regulating appetite and satiety through signals");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Regulating appetite and satiety through signals (Correct answer)\n" +
                        "The CNS modulates digestive functions by sending regulatory signals regarding hunger and fullness.\n\n" +
                        "Producing digestive enzymes (Incorrect)\n" +
                        "Enzymes are produced by organs like the stomach, pancreas, and small intestine.\n\n" +
                        "Absorbing nutrients directly (Incorrect)\n" +
                        "Nutrient absorption is conducted by the intestinal cells.\n\n" +
                        "Generating bile (Incorrect)\n" +
                        "Bile is produced by the liver."
        );

        questions.add("Which hormones are mentioned as key regulators of digestive secretions?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin, adrenaline, and cortisol",
                "Gastrin, secretin, and cholecystokinin (CCK)", // Correct answer
                "Oxytocin, prolactin, and thyroxine",
                "Estrogen, testosterone, and progesterone"
        )));
        correctAnswers.add("Gastrin, secretin, and cholecystokinin (CCK)");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Gastrin, secretin, and cholecystokinin (CCK) (Correct answer)\n" +
                        "These hormones coordinate digestive secretions to ensure proper chemical digestion.\n\n" +
                        "Insulin, adrenaline, and cortisol (Incorrect)\n" +
                        "These hormones are involved in metabolism and stress responses, not specifically in coordinating digestion the way gastrointestinal hormones do.\n\n" +
                        "Oxytocin, prolactin, and thyroxine (Incorrect)\n" +
                        "These hormones are not the primary regulators of digestion.\n\n" +
                        "Estrogen, testosterone, and progesterone (Incorrect)\n" +
                        "These are sex hormones and do not primarily regulate digestive secretions."
        );

        questions.add("How does the endocrine system contribute to digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By breaking down fats mechanically",
                "By releasing hormones that regulate digestive secretions and metabolism", // Correct answer
                "By absorbing nutrients via the villi",
                "By directly churning food in the stomach"
        )));
        correctAnswers.add("By releasing hormones that regulate digestive secretions and metabolism");
        rationales.put(26,
                "RATIONALE:\n" +
                        "By releasing hormones that regulate digestive secretions and metabolism (Correct answer)\n" +
                        "The endocrine system releases hormones (like those mentioned in Q26) that help coordinate digestion.\n\n" +
                        "By breaking down fats mechanically (Incorrect)\n" +
                        "Mechanical digestion is not an endocrine function.\n\n" +
                        "By absorbing nutrients via the villi (Incorrect)\n" +
                        "Nutrient absorption is a function of the intestinal epithelium.\n\n" +
                        "By directly churning food in the stomach (Incorrect)\n" +
                        "Churning is achieved through muscle contractions, not by hormonal action."
        );

        questions.add("What role does the immune system play in the digestive tract?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It produces bile",
                "It forms gut-associated lymphoid tissue (GALT) to defend against pathogens", // Correct answer
                "It increases peristaltic movements",
                "It mixes food with enzymes"
        )));
        correctAnswers.add("It forms gut-associated lymphoid tissue (GALT) to defend against pathogens");
        rationales.put(27,
                "RATIONALE:\n" +
                        "It forms gut-associated lymphoid tissue (GALT) to defend against pathogens (Correct answer)\n" +
                        "GALT is vital in protecting the digestive tract from invading pathogens.\n\n" +
                        "It produces bile (Incorrect)\n" +
                        "Bile production is a function of the liver.\n\n" +
                        "It increases peristaltic movements (Incorrect)\n" +
                        "Peristalsis is regulated by nervous and muscular activity, not directly by immune cells.\n\n" +
                        "It mixes food with enzymes (Incorrect)\n" +
                        "Food mixing is a mechanical and chemical process, not an immune function."
        );

        questions.add("Once nutrients are absorbed, which system distributes them throughout the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The nervous system",
                "The endocrine system",
                "The circulatory system", // Correct answer
                "The respiratory system"
        )));
        correctAnswers.add("The circulatory system");
        rationales.put(28,
                "RATIONALE:\n" +
                        "The circulatory system (Correct answer)\n" +
                        "The circulatory system transports absorbed nutrients to the various cells that need them.\n\n" +
                        "The nervous system (Incorrect)\n" +
                        "The nervous system sends signals but does not transport nutrients.\n\n" +
                        "The endocrine system (Incorrect)\n" +
                        "Although it releases hormones, it does not distribute nutrients.\n\n" +
                        "The respiratory system (Incorrect)\n" +
                        "The respiratory system is involved in gas exchange, not nutrient distribution."
        );

        questions.add("Which of the following is NOT a function of the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Breaking down food into nutrients",
                "Fueling bodily functions",
                "Discarding indigestible waste",
                "Directly oxygenating blood" // Correct answer
        )));
        correctAnswers.add("Directly oxygenating blood");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Directly oxygenating blood (Correct answer)\n" +
                        "Oxygenation is a function of the respiratory system, not the digestive system.\n\n" +
                        "Breaking down food into nutrients (Incorrect)\n" +
                        "It is a function of the digestive system.\n\n" +
                        "Fueling bodily functions (Incorrect)\n" +
                        "The nutrients produced fuel various processes in the body.\n\n" +
                        "Discarding indigestible waste (Incorrect)\n" +
                        "The elimination of waste is part of the digestive process."
        );

        questions.add("In the overview, the digestive system is described as a 'finely tuned, multistep process.' This implies that digestion involves:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Only one simple action",
                "Several coordinated stages from ingestion to defecation", // Correct answer
                "A random series of events",
                "Only chemical reactions"
        )));
        correctAnswers.add("Several coordinated stages from ingestion to defecation");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Several coordinated stages from ingestion to defecation (Correct answer)\n" +
                        "The process includes ingestion, propulsion, mechanical and chemical digestion, absorption, and defecation.\n\n" +
                        "Only one simple action (Incorrect)\n" +
                        "The phrase “multistep” indicates complexity.\n\n" +
                        "A random series of events (Incorrect)\n" +
                        "The process is carefully regulated rather than random.\n\n" +
                        "Only chemical reactions (Incorrect)\n" +
                        "Both mechanical and chemical processes are involved."
        );

        questions.add("Which component primarily ensures that food is physically broken into smaller pieces?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Chewing (mastication) in the mouth", // Correct answer
                "Enzymatic activity in the stomach",
                "Bile emulsification in the duodenum",
                "Peristalsis in the esophagus"
        )));
        correctAnswers.add("Chewing (mastication) in the mouth");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Chewing (mastication) in the mouth (Correct answer)\n" +
                        "Mastication is the mechanical process that reduces food size.\n\n" +
                        "Enzymatic activity in the stomach (Incorrect)\n" +
                        "Enzymes chemically break down food but do not physically reduce its size.\n\n" +
                        "Bile emulsification in the duodenum (Incorrect)\n" +
                        "Bile emulsifies fats; it doesn’t mechanically fragment food.\n\n" +
                        "Peristalsis in the esophagus (Incorrect)\n" +
                        "Peristalsis transports food rather than breaking it apart."
        );

        questions.add("What is the main role of the salivary enzyme amylase?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To digest proteins",
                "To begin the breakdown of carbohydrates", // Correct answer
                "To emulsify fats",
                "To neutralize stomach acid"
        )));
        correctAnswers.add("To begin the breakdown of carbohydrates");
        rationales.put(32,
                "RATIONALE:\n" +
                        "To begin the breakdown of carbohydrates (Correct answer)\n" +
                        "Amylase converts starches into simpler sugars during mastication.\n\n" +
                        "To digest proteins (Incorrect)\n" +
                        "Protein digestion begins in the stomach with pepsin, not with amylase.\n\n" +
                        "To emulsify fats (Incorrect)\n" +
                        "Emulsification of fats is performed by bile.\n\n" +
                        "To neutralize stomach acid (Incorrect)\n" +
                        "Neutralization of acid occurs later in the digestive process, primarily in the duodenum by bicarbonate."
        );

        questions.add("The pharynx functions to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digest food chemically",
                "Channel food from the mouth into the esophagus", // Correct answer
                "Absorb nutrients",
                "Store bile"
        )));
        correctAnswers.add("Channel food from the mouth into the esophagus");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Channel food from the mouth into the esophagus (Correct answer)\n" +
                        "It acts as a passageway guiding food into the esophagus.\n\n" +
                        "Digest food chemically (Incorrect)\n" +
                        "Chemical digestion occurs later (in the stomach and intestines).\n\n" +
                        "Absorb nutrients (Incorrect)\n" +
                        "Nutrient absorption does not occur in the pharynx.\n\n" +
                        "Store bile (Incorrect)\n" +
                        "Bile is stored in the gallbladder."
        );

        questions.add("Which statement best describes the stomach’s role in digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It absorbs most nutrients",
                "It stores food long-term",
                "It churns food and mixes it with gastric juices, breaking it down into chyme", // Correct answer
                "It exclusively produces bile"
        )));
        correctAnswers.add("It churns food and mixes it with gastric juices, breaking it down into chyme");
        rationales.put(34,
                "RATIONALE:\n" +
                        "It churns food and mixes it with gastric juices, breaking it down into chyme (Correct answer)\n" +
                        "The stomach’s churning action and secretions transform food into chyme.\n\n" +
                        "It absorbs most nutrients (Incorrect)\n" +
                        "Nutrient absorption is minimal in the stomach; the small intestine does most of this work.\n\n" +
                        "It stores food long-term (Incorrect)\n" +
                        "Although the stomach temporarily holds food, its primary function is digestion.\n\n" +
                        "It exclusively produces bile (Incorrect)\n" +
                        "Bile is produced by the liver, not the stomach."
        );

        questions.add("What substance is produced by the liver to aid in fat digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Insulin",
                "Bile", // Correct answer
                "Amylase",
                "Cholecystokinin"
        )));
        correctAnswers.add("Bile");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Bile (Correct answer)\n" +
                        "Bile from the liver emulsifies fats, making them easier for enzymes to digest.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is a hormone secreted by the pancreas.\n\n" +
                        "Amylase (Incorrect)\n" +
                        "Amylase is produced by the salivary glands and pancreas to break down carbohydrates.\n\n" +
                        "Cholecystokinin (Incorrect)\n" +
                        "Cholecystokinin (CCK) is a hormone that signals the release of bile; it is not bile itself."
        );

        questions.add("How does bile facilitate fat digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By directly breaking down fats",
                "By emulsifying fats into smaller droplets for enzyme action", // Correct answer
                "By absorbing fats into the bloodstream",
                "By converting fats into carbohydrates"
        )));
        correctAnswers.add("By emulsifying fats into smaller droplets for enzyme action");
        rationales.put(36,
                "RATIONALE:\n" +
                        "By emulsifying fats into smaller droplets for enzyme action (Correct answer)\n" +
                        "Emulsification increases the surface area of fats so that lipase can work more efficiently.\n\n" +
                        "By directly breaking down fats (Incorrect)\n" +
                        "Bile does not chemically digest fats; it emulsifies them.\n\n" +
                        "By absorbing fats into the bloodstream (Incorrect)\n" +
                        "Absorption occurs after digestion in the small intestine.\n\n" +
                        "By converting fats into carbohydrates (Incorrect)\n" +
                        "Fats are not converted into carbohydrates at any stage in digestion."
        );

        questions.add("Which accessory organ stores bile produced by the liver?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pancreas",
                "Gallbladder", // Correct answer
                "Stomach",
                "Small intestine"
        )));
        correctAnswers.add("Gallbladder");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Gallbladder (Correct answer)\n" +
                        "The gallbladder stores and concentrates bile until it is needed.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "The pancreas produces enzymes and hormones, not bile.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "The stomach is responsible for mixing food with gastric juices, not storing bile.\n\n" +
                        "Small intestine (Incorrect)\n" +
                        "The small intestine receives bile but does not store it."
        );

        questions.add("During digestion, what role does the pancreas play?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It churns food into chyme",
                "It produces both digestive enzymes and hormones such as insulin and glucagon", // Correct answer
                "It absorbs water from food",
                "It secretes bile"
        )));
        correctAnswers.add("It produces both digestive enzymes and hormones such as insulin and glucagon");
        rationales.put(38,
                "RATIONALE:\n" +
                        "It produces both digestive enzymes and hormones such as insulin and glucagon (Correct answer)\n" +
                        "The pancreas functions exocrinely by releasing enzymes and endocrinely by regulating blood sugar.\n\n" +
                        "It churns food into chyme (Incorrect)\n" +
                        "The stomach performs the churning.\n\n" +
                        "It absorbs water from food (Incorrect)\n" +
                        "Water absorption occurs in the intestines.\n\n" +
                        "It secretes bile (Incorrect)\n" +
                        "Bile is secreted by the liver."
        );

        questions.add("What does the large intestine absorb besides water?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nutrients fully broken down",
                "Electrolytes", // Correct answer
                "Digestive enzymes",
                "Bile salts exclusively"
        )));
        correctAnswers.add("Electrolytes");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Electrolytes (Correct answer)\n" +
                        "The large intestine reabsorbs electrolytes along with water, helping to form solid waste.\n\n" +
                        "Nutrients fully broken down (Incorrect)\n" +
                        "While some vitamins may be produced, the large intestine is not the main site for nutrient absorption.\n\n" +
                        "Digestive enzymes (Incorrect)\n" +
                        "Enzymes are secreted along the pathway, not absorbed.\n\n" +
                        "Bile salts exclusively (Incorrect)\n" +
                        "Although bile salts may be reabsorbed, the primary function here is water and electrolyte absorption."
        );

        questions.add("Which process is characterized by the movement of food through muscular waves?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Peristalsis", // Correct answer
                "Chemical digestion",
                "Absorption",
                "Secretion"
        )));
        correctAnswers.add("Peristalsis");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Peristalsis (Correct answer)\n" +
                        "Peristalsis is the rhythmic contraction that propels food through the digestive tract.\n\n" +
                        "Chemical digestion (Incorrect)\n" +
                        "Chemical digestion involves enzymatic reactions, not muscular movements.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption is the uptake of nutrients, not the transport of food.\n\n" +
                        "Secretion (Incorrect)\n" +
                        "Secretion involves releasing digestive juices, not moving food."
        );

        questions.add("Mechanical digestion in the stomach is accomplished by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The churning movements of the stomach muscles", // Correct answer
                "The action of bile",
                "The absorption through villi",
                "Enzymatic reactions"
        )));
        correctAnswers.add("The churning movements of the stomach muscles");
        rationales.put(41,
                "RATIONALE:\n" +
                        "The churning movements of the stomach muscles (Correct answer)\n" +
                        "The stomach’s churning physically breaks down food, complementing chemical digestion.\n\n" +
                        "The action of bile (Incorrect)\n" +
                        "Bile is involved in fat emulsification, not mechanical churning.\n\n" +
                        "The absorption through villi (Incorrect)\n" +
                        "Villi are structures in the small intestine for nutrient absorption.\n\n" +
                        "Enzymatic reactions (Incorrect)\n" +
                        "Enzymatic reactions are part of chemical digestion, not mechanical digestion."
        );

        questions.add("Chemical digestion refers to the breakdown of macromolecules by:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gastric juices, bile, and pancreatic enzymes", // Correct answer
                "Saliva only",
                "Chewing alone",
                "Mechanical churning exclusively"
        )));
        correctAnswers.add("Gastric juices, bile, and pancreatic enzymes");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Gastric juices, bile, and pancreatic enzymes (Correct answer)\n" +
                        "A combination of digestive juices and enzymes chemically decomposes food molecules.\n\n" +
                        "Saliva only (Incorrect)\n" +
                        "Chemical digestion occurs at various stages and involves multiple digestive juices and enzymes, not just saliva.\n\n" +
                        "Chewing alone (Incorrect)\n" +
                        "Chewing is a mechanical process and does not involve enzymatic activity.\n\n" +
                        "Mechanical churning exclusively (Incorrect)\n" +
                        "Churning contributes to mechanical digestion but is not a chemical process."
        );

        questions.add("Absorption of nutrients predominantly occurs in the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Small intestine", // Correct answer
                "Mouth",
                "Stomach",
                "Large intestine"
        )));
        correctAnswers.add("Small intestine");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Small intestine (Correct answer)\n" +
                        "The small intestine’s lining—with its villi and microvilli—is specialized for nutrient absorption.\n\n" +
                        "Mouth (Incorrect)\n" +
                        "The mouth is involved in ingestion and mechanical digestion, with minimal absorption.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "While some substances like alcohol may be absorbed here, the stomach is not the main site of nutrient absorption.\n\n" +
                        "Large intestine (Incorrect)\n" +
                        "The large intestine focuses on water and electrolyte absorption, not nutrient absorption."
        );

        questions.add("Defecation is best described as:");
        choices.add(new ArrayList<>(Arrays.asList(
                "The expulsion of undigested waste", // Correct answer
                "The ingestion of food",
                "The process of moving food into the stomach",
                "The chemical breakdown of carbohydrates"
        )));
        correctAnswers.add("The expulsion of undigested waste");
        rationales.put(44,
                "RATIONALE:\n" +
                        "The expulsion of undigested waste (Correct answer)\n" +
                        "Defecation expels the remains of food that could not be digested or absorbed.\n\n" +
                        "The ingestion of food (Incorrect)\n" +
                        "Ingestion is the process of taking in food through the mouth.\n\n" +
                        "The process of moving food into the stomach (Incorrect)\n" +
                        "That is part of propulsion, not defecation.\n\n" +
                        "The chemical breakdown of carbohydrates (Incorrect)\n" +
                        "Carbohydrate breakdown is part of chemical digestion, not waste elimination."
        );

        questions.add("Which of the following stages involves both mechanical and chemical processes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digestion", // Correct answer
                "Ingestion",
                "Propulsion",
                "Defecation"
        )));
        correctAnswers.add("Digestion");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Digestion (Correct answer)\n" +
                        "Digestion includes both the mechanical breakdown and the chemical decomposition of food.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is simply the act of taking in food.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion moves food along the digestive tract and does not involve digestion.\n\n" +
                        "Defecation (Incorrect)\n" +
                        "Defecation is purely the elimination of waste without digestive processes."
        );

        questions.add("The network that governs local gut function is known as the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Enteric nervous system", // Correct answer
                "Central nervous system",
                "Autonomic nervous system",
                "Peripheral nervous system"
        )));
        correctAnswers.add("Enteric nervous system");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Enteric nervous system (Correct answer)\n" +
                        "The enteric nervous system independently regulates many digestive processes.\n\n" +
                        "Central nervous system (Incorrect)\n" +
                        "The CNS supports the digestive process but does not directly govern gut function.\n\n" +
                        "Autonomic nervous system (Incorrect)\n" +
                        "Although the autonomic system influences digestion, the enteric system governs local gut activity.\n\n" +
                        "Peripheral nervous system (Incorrect)\n" +
                        "The peripheral nervous system handles signals to and from limbs, not digestion."
        );

        questions.add("Which phrase best describes the integration of the digestive system with the endocrine system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It releases hormones that coordinate digestive secretions", // Correct answer
                "It produces mechanical movements",
                "It solely focuses on nutrient absorption",
                "It stores indigestible waste"
        )));
        correctAnswers.add("It releases hormones that coordinate digestive secretions");
        rationales.put(47,
                "RATIONALE:\n" +
                        "It releases hormones that coordinate digestive secretions (Correct answer)\n" +
                        "Endocrine hormones (e.g., gastrin, secretin, CCK) ensure coordinated secretions and metabolic regulation.\n\n" +
                        "It produces mechanical movements (Incorrect)\n" +
                        "Mechanical movements result from muscular action, not hormones.\n\n" +
                        "It solely focuses on nutrient absorption (Incorrect)\n" +
                        "While hormone release supports digestion, absorption is carried out by the intestines.\n\n" +
                        "It stores indigestible waste (Incorrect)\n" +
                        "Storing waste is a function of the large intestine, unrelated to endocrine regulation."
        );

        questions.add("GALT, mentioned in the overview, stands for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Gut-Associated Lymphoid Tissue", // Correct answer
                "Gastrointestinal Absorption Lymphatic Tissue",
                "Gallbladder and Liver Tissue",
                "General Acidic Lumen Tissue"
        )));
        correctAnswers.add("Gut-Associated Lymphoid Tissue");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Gut-Associated Lymphoid Tissue (Correct answer)\n" +
                        "GALT is essential for immune responses within the digestive tract.\n\n" +
                        "Gastrointestinal Absorption Lymphatic Tissue (Incorrect)\n" +
                        "Absorption is not a specific function of GALT.\n\n" +
                        "Gallbladder and Liver Tissue (Incorrect)\n" +
                        "This misrepresents the acronym.\n\n" +
                        "General Acidic Lumen Tissue (Incorrect)\n" +
                        "This term is unrelated to digestion or GALT."
        );

        questions.add("How does the circulatory system assist the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By transporting absorbed nutrients to cells throughout the body", // Correct answer
                "By breaking down food mechanically",
                "By producing digestive enzymes",
                "By storing bile"
        )));
        correctAnswers.add("By transporting absorbed nutrients to cells throughout the body");
        rationales.put(49,
                "RATIONALE:\n" +
                        "By transporting absorbed nutrients to cells throughout the body (Correct answer)\n" +
                        "Once nutrients are absorbed, they are distributed via the bloodstream to nourish tissues.\n\n" +
                        "By breaking down food mechanically (Incorrect)\n" +
                        "Mechanical digestion is handled by muscles and chewing, not circulation.\n\n" +
                        "By producing digestive enzymes (Incorrect)\n" +
                        "Enzymes are secreted by organs like the pancreas and stomach.\n\n" +
                        "By storing bile (Incorrect)\n" +
                        "The gallbladder, not the circulatory system, stores bile."
        );

        questions.add("Which of the following is NOT part of the digestive process as outlined in the overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Photosynthesis", // Correct answer
                "Ingestion",
                "Propulsion",
                "Absorption"
        )));
        correctAnswers.add("Photosynthesis");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Photosynthesis (Correct answer)\n" +
                        "Photosynthesis is not a part of human digestion.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is a central part of the digestive process.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion is involved in moving food along the digestive tract.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption is essential for nutrient uptake during digestion."
        );

        questions.add("The “multistep process” mentioned in the overview includes all of the following stages except:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Circulation", // Correct answer
                "Ingestion",
                "Propulsion",
                "Absorption"
        )));
        correctAnswers.add("Circulation");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Circulation (Correct answer)\n" +
                        "Circulation is handled by the circulatory system; the digestive process itself includes ingestion, propulsion, mechanical and chemical digestion, absorption, and defecation.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is part of the digestive sequence.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion is a necessary step in moving food through the digestive system.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption allows the body to gain nutrients from digested food."
        );

        questions.add("What ensures that the digestive process is “efficient and regulated”?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Integration with the nervous, endocrine, and immune systems", // Correct answer
                "Random movements of food",
                "The exclusive use of mechanical digestion",
                "The storage of food in the stomach indefinitely"
        )));
        correctAnswers.add("Integration with the nervous, endocrine, and immune systems");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Integration with the nervous, endocrine, and immune systems (Correct answer)\n" +
                        "Coordination with other systems makes digestion both efficient and well-regulated.\n\n" +
                        "Random movements of food (Incorrect)\n" +
                        "Random or uncoordinated movements would lead to inefficient digestion.\n\n" +
                        "The exclusive use of mechanical digestion (Incorrect)\n" +
                        "Both mechanical and chemical processes are required, and regulation involves more than physical action.\n\n" +
                        "The storage of food in the stomach indefinitely (Incorrect)\n" +
                        "Food is not stored indefinitely; timely processing is essential for efficiency."
        );

        questions.add("Which accessory organ is primarily involved in detoxifying chemicals?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Liver", // Correct answer
                "Gallbladder",
                "Pancreas",
                "Large intestine"
        )));
        correctAnswers.add("Liver");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Liver (Correct answer)\n" +
                        "The liver not only produces bile but also detoxifies chemicals.\n\n" +
                        "Gallbladder (Incorrect)\n" +
                        "The gallbladder stores and concentrates bile but does not detoxify chemicals.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "The pancreas produces digestive enzymes and hormones but is not the primary detoxification organ.\n\n" +
                        "Large intestine (Incorrect)\n" +
                        "The large intestine mainly absorbs water and electrolytes and forms feces."
        );

        questions.add("How are fats primarily digested according to the overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Through the emulsifying action of bile and digestion by enzymes", // Correct answer
                "By chewing in the mouth",
                "By absorption in the large intestine",
                "Through mechanical churning alone"
        )));
        correctAnswers.add("Through the emulsifying action of bile and digestion by enzymes");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Through the emulsifying action of bile and digestion by enzymes (Correct answer)\n" +
                        "Bile emulsifies fats so that enzymes (from the pancreas) can efficiently digest them.\n\n" +
                        "By chewing in the mouth (Incorrect)\n" +
                        "While chewing helps mix food, it does not significantly break down fats.\n\n" +
                        "By absorption in the large intestine (Incorrect)\n" +
                        "The large intestine is not the site where significant fat digestion occurs.\n\n" +
                        "Through mechanical churning alone (Incorrect)\n" +
                        "Churning helps mix food, but fats require emulsification and enzyme action for proper digestion."
        );

        questions.add("Which statement best describes the role of the duodenum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It receives bile and pancreatic enzymes to continue chemical digestion", // Correct answer
                "It stores digestive waste",
                "It is the primary absorption site for all nutrients",
                "It is responsible for mechanical digestion only"
        )));
        correctAnswers.add("It receives bile and pancreatic enzymes to continue chemical digestion");
        rationales.put(55,
                "RATIONALE:\n" +
                        "It receives bile and pancreatic enzymes to continue chemical digestion (Correct answer)\n" +
                        "The duodenum is the first segment of the small intestine to receive digestive secretions.\n\n" +
                        "It stores digestive waste (Incorrect)\n" +
                        "Storage of waste is not a function of the duodenum.\n\n" +
                        "It is the primary absorption site for all nutrients (Incorrect)\n" +
                        "Although absorption begins here, most nutrient absorption occurs in the jejunum and ileum.\n\n" +
                        "It is responsible for mechanical digestion only (Incorrect)\n" +
                        "Its role is chiefly chemical, receiving secretions to aid in digestion."
        );

        questions.add("What ensures that the inner lining of the small intestine is optimized for absorption?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The presence of folds, villi, and microvilli", // Correct answer
                "Its smooth, flat surface",
                "A high concentration of gastric acid",
                "Continuous secretion of bile"
        )));
        correctAnswers.add("The presence of folds, villi, and microvilli");
        rationales.put(56,
                "RATIONALE:\n" +
                        "The presence of folds, villi, and microvilli (Correct answer)\n" +
                        "These structures increase the surface area for nutrient absorption.\n\n" +
                        "Its smooth, flat surface (Incorrect)\n" +
                        "A smooth, flat surface would actually reduce the area available for absorption.\n\n" +
                        "A high concentration of gastric acid (Incorrect)\n" +
                        "High acid levels would harm the delicate intestinal lining and are not used for absorption.\n\n" +
                        "Continuous secretion of bile (Incorrect)\n" +
                        "Bile aids in fat digestion but does not directly increase the absorptive surface area."
        );

        questions.add("The integration of the digestive system with the immune system helps to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Defend against pathogens entering the gut", // Correct answer
                "Increase peristalsis",
                "Enhance bile production",
                "Accelerate nutrient absorption"
        )));
        correctAnswers.add("Defend against pathogens entering the gut");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Defend against pathogens entering the gut (Correct answer)\n" +
                        "Immune components like GALT protect the gut from harmful organisms.\n\n" +
                        "Increase peristalsis (Incorrect)\n" +
                        "Peristalsis is a muscular function not directly increased by immune activity.\n\n" +
                        "Enhance bile production (Incorrect)\n" +
                        "Bile production is regulated by the liver and endocrine signals, not directly by the immune system.\n\n" +
                        "Accelerate nutrient absorption (Incorrect)\n" +
                        "While a healthy immune system supports overall function, its primary role here is protection against pathogens."
        );

        questions.add("Which of the following is a key feature that distinguishes the small intestine from the large intestine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Its role in absorbing most nutrients", // Correct answer
                "Presence of peristaltic waves",
                "Hosting the gut microbiome",
                "Forming feces"
        )));
        correctAnswers.add("Its role in absorbing most nutrients");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Its role in absorbing most nutrients (Correct answer)\n" +
                        "The small intestine is primarily devoted to nutrient absorption, while the large intestine focuses on water absorption and waste formation.\n\n" +
                        "Presence of peristaltic waves (Incorrect)\n" +
                        "Both the small and large intestines exhibit peristalsis.\n\n" +
                        "Hosting the gut microbiome (Incorrect)\n" +
                        "Partially correct, but both intestines host bacteria; however, the large intestine has a richer microbiome.\n\n" +
                        "Forming feces (Incorrect)\n" +
                        "Feces are formed primarily in the large intestine."
        );

        questions.add("What process occurs immediately after chemical digestion has broken macromolecules into smaller subunits?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Absorption", // Correct answer
                "Ingestion",
                "Propulsion",
                "Defecation"
        )));
        correctAnswers.add("Absorption");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Absorption (Correct answer)\n" +
                        "Once macromolecules are broken down, the resultant nutrients are absorbed.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is the intake of food, which occurs before digestion begins.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion moves food along the tract and is not the next step after chemical digestion.\n\n" +
                        "Defecation (Incorrect)\n" +
                        "Defecation is the elimination of waste, which comes after absorption."
        );

        questions.add("Which part of the digestive process is responsible for “compacting” undigested food?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Defecation", // Correct answer
                "Ingestion",
                "Propulsion",
                "Absorption"
        )));
        correctAnswers.add("Defecation");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Defecation (Correct answer)\n" +
                        "The large intestine compacts indigestible food into feces, which are then expelled.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is about taking in food, not processing waste.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "While propulsion moves the food, it does not compact it.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption involves nutrient uptake rather than waste compaction."
        );

        questions.add("What is the significance of the “accessory organs” (liver, gallbladder, pancreas) in digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They assist in producing and storing substances that facilitate chemical digestion", // Correct answer
                "They serve as the primary sites of nutrient absorption",
                "They replace the need for peristalsis",
                "They delay the digestive process"
        )));
        correctAnswers.add("They assist in producing and storing substances that facilitate chemical digestion");
        rationales.put(61,
                "RATIONALE:\n" +
                        "They assist in producing and storing substances that facilitate chemical digestion (Correct answer)\n" +
                        "These organs produce bile and digestive enzymes and help regulate blood sugar, enhancing digestion.\n\n" +
                        "They serve as the primary sites of nutrient absorption (Incorrect)\n" +
                        "Nutrient absorption mainly occurs in the small intestine.\n\n" +
                        "They replace the need for peristalsis (Incorrect)\n" +
                        "Peristalsis is a muscular process that these organs do not influence directly.\n\n" +
                        "They delay the digestive process (Incorrect)\n" +
                        "Their roles enhance and coordinate digestion, not delay it."
        );

        questions.add("Which of the following best describes the relationship between mechanical and chemical digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mechanical digestion increases the surface area for chemical digestion", // Correct answer
                "They are unrelated processes",
                "Chemical digestion precedes mechanical digestion",
                "Mechanical digestion eliminates the need for enzymes"
        )));
        correctAnswers.add("Mechanical digestion increases the surface area for chemical digestion");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Mechanical digestion increases the surface area for chemical digestion (Correct answer)\n" +
                        "Chewing and churning reduce food particle size, facilitating more effective enzyme action.\n\n" +
                        "They are unrelated processes (Incorrect)\n" +
                        "They work hand-in-hand; mechanical digestion aids chemical digestion.\n\n" +
                        "Chemical digestion precedes mechanical digestion (Incorrect)\n" +
                        "Typically, mechanical digestion (e.g., chewing) begins in the mouth before chemical digestion.\n\n" +
                        "Mechanical digestion eliminates the need for enzymes (Incorrect)\n" +
                        "Enzymes are essential for chemically breaking down food molecules."
        );

        questions.add("What does ingestion involve?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Taking food into the mouth and initial mechanical breakdown", // Correct answer
                "Swallowing and peristalsis",
                "The direct absorption of nutrients",
                "The expulsion of waste"
        )));
        correctAnswers.add("Taking food into the mouth and initial mechanical breakdown");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Taking food into the mouth and initial mechanical breakdown (Correct answer)\n" +
                        "Ingestion is the entry point for food, where it is both taken in and mechanically broken down.\n\n" +
                        "Swallowing and peristalsis (Incorrect)\n" +
                        "Partially correct but not complete; these actions move food once it’s in the mouth, but ingestion specifically begins with taking the food in.\n\n" +
                        "The direct absorption of nutrients (Incorrect)\n" +
                        "Absorption occurs later in the small intestine.\n\n" +
                        "The expulsion of waste (Incorrect)\n" +
                        "That is defecation, which is the final stage."
        );

        questions.add("Which statement about the digestive process is true?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It encompasses ingestion, propulsion, mechanical and chemical digestion, absorption, and defecation", // Correct answer
                "It only involves chemical digestion",
                "It is a unidirectional process that ends at the stomach",
                "It is regulated solely by the digestive organs themselves"
        )));
        correctAnswers.add("It encompasses ingestion, propulsion, mechanical and chemical digestion, absorption, and defecation");
        rationales.put(64,
                "RATIONALE:\n" +
                        "It encompasses ingestion, propulsion, mechanical and chemical digestion, absorption, and defecation (Correct answer)\n" +
                        "All these stages work together to digest food efficiently.\n\n" +
                        "It only involves chemical digestion (Incorrect)\n" +
                        "It involves both mechanical and chemical digestion.\n\n" +
                        "It is a unidirectional process that ends at the stomach (Incorrect)\n" +
                        "The process continues well beyond the stomach through absorption and waste elimination.\n\n" +
                        "It is regulated solely by the digestive organs themselves (Incorrect)\n" +
                        "Regulation also involves the nervous, endocrine, and immune systems."
        );

        questions.add("What is the significance of bile’s emulsification of fats?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It breaks fats into smaller droplets, increasing the efficiency of lipase", // Correct answer
                "It directly absorbs water",
                "It neutralizes stomach acid",
                "It solidifies fats for easier absorption"
        )));
        correctAnswers.add("It breaks fats into smaller droplets, increasing the efficiency of lipase");
        rationales.put(65,
                "RATIONALE:\n" +
                        "It breaks fats into smaller droplets, increasing the efficiency of lipase (Correct answer)\n" +
                        "Emulsification makes fats accessible to pancreatic lipase.\n\n" +
                        "It directly absorbs water (Incorrect)\n" +
                        "Emulsification is about breaking up fat molecules, not water absorption.\n\n" +
                        "It neutralizes stomach acid (Incorrect)\n" +
                        "Bile does not neutralize acid; bicarbonate secreted by the pancreas performs that function.\n\n" +
                        "It solidifies fats for easier absorption (Incorrect)\n" +
                        "Fats must remain in a emulsified state for proper enzymatic digestion, not solidified."
        );

        questions.add("The pancreas aids in the digestion of macromolecules by producing enzymes that break down:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Proteins, carbohydrates, and fats", // Correct answer
                "Only proteins",
                "Only carbohydrates",
                "Only vitamins"
        )));
        correctAnswers.add("Proteins, carbohydrates, and fats");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Proteins, carbohydrates, and fats (Correct answer)\n" +
                        "The pancreas secretes a diverse range of enzymes to digest all major nutrient classes.\n\n" +
                        "Only proteins (Incorrect)\n" +
                        "It digests more than just proteins.\n\n" +
                        "Only carbohydrates (Incorrect)\n" +
                        "It also digests fats and proteins.\n\n" +
                        "Only vitamins (Incorrect)\n" +
                        "Vitamins are not 'digested' by enzymes; they are absorbed as micronutrients."
        );

        questions.add("What does “propulsion” in the digestive process refer to?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The movement of food along the digestive tract via muscular contractions", // Correct answer
                "The chemical breakdown of food",
                "The storage of nutrients in the bloodstream",
                "The elimination of waste"
        )));
        correctAnswers.add("The movement of food along the digestive tract via muscular contractions");
        rationales.put(67,
                "RATIONALE:\n" +
                        "The movement of food along the digestive tract via muscular contractions (Correct answer)\n" +
                        "Propulsion involves swallowing and peristalsis to move food forward.\n\n" +
                        "The chemical breakdown of food (Incorrect)\n" +
                        "That is chemical digestion.\n\n" +
                        "The storage of nutrients in the bloodstream (Incorrect)\n" +
                        "Nutrient storage occurs in cells and tissues, not as part of propulsion.\n\n" +
                        "The elimination of waste (Incorrect)\n" +
                        "Waste elimination is defined as defecation."
        );

        questions.add("Which system is directly responsible for the coordination of hormone signals affecting digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The endocrine system", // Correct answer
                "The skeletal system",
                "The respiratory system",
                "The integumentary system"
        )));
        correctAnswers.add("The endocrine system");
        rationales.put(68,
                "RATIONALE:\n" +
                        "The endocrine system (Correct answer)\n" +
                        "The endocrine system releases hormones that regulate digestive secretions and processes.\n\n" +
                        "The skeletal system (Incorrect)\n" +
                        "The skeletal system provides structure but is not involved in hormonal coordination.\n\n" +
                        "The respiratory system (Incorrect)\n" +
                        "The respiratory system is involved in gas exchange, not in hormonal regulation of digestion.\n\n" +
                        "The integumentary system (Incorrect)\n" +
                        "The integumentary system (skin, hair, nails) is unrelated to digestion."
        );

        questions.add("The term “defecation” in the context of digestion means:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Eliminating indigestible material from the body", // Correct answer
                "Absorbing nutrients",
                "Chewing food thoroughly",
                "Stirring food with gastric juices"
        )));
        correctAnswers.add("Eliminating indigestible material from the body");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Eliminating indigestible material from the body (Correct answer)\n" +
                        "Defecation is the expulsion of waste after processing and absorption.\n\n" +
                        "Absorbing nutrients (Incorrect)\n" +
                        "Absorption is the uptake of nutrients.\n\n" +
                        "Chewing food thoroughly (Incorrect)\n" +
                        "Chewing is part of ingestion and mechanical digestion.\n\n" +
                        "Stirring food with gastric juices (Incorrect)\n" +
                        "That is a part of mechanical/chemical digestion in the stomach, not elimination."
        );

        questions.add("Which of the following best illustrates the concept that “digestion is both efficient and regulated”?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Coordinated interplay between the digestive, nervous, endocrine, and immune systems", // Correct answer
                "Random mechanical movements of food",
                "The exclusive reliance on chemical digestion",
                "Uncoordinated hormone release"
        )));
        correctAnswers.add("Coordinated interplay between the digestive, nervous, endocrine, and immune systems");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Coordinated interplay between the digestive, nervous, endocrine, and immune systems (Correct answer)\n" +
                        "This ensures that digestion is efficiently controlled and regulated.\n\n" +
                        "Random mechanical movements of food (Incorrect)\n" +
                        "Random movements would lead to inefficient processing.\n\n" +
                        "The exclusive reliance on chemical digestion (Incorrect)\n" +
                        "Both mechanical and chemical processes are required for digestion.\n\n" +
                        "Uncoordinated hormone release (Incorrect)\n" +
                        "Uncoordinated signals would lead to dysfunction rather than efficiency."
        );

        questions.add("Within the digestive process, what is the main contribution of chemical digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Decomposing macromolecules into absorbable units", // Correct answer
                "Physically breaking down food",
                "Transporting food through the digestive tract",
                "Forming feces in the large intestine"
        )));
        correctAnswers.add("Decomposing macromolecules into absorbable units");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Decomposing macromolecules into absorbable units (Correct answer)\n" +
                        "Chemical digestion uses enzymes and juices to break down food into absorbable molecules.\n\n" +
                        "Physically breaking down food (Incorrect)\n" +
                        "Physical breakdown is the role of mechanical digestion, not chemical digestion.\n\n" +
                        "Transporting food through the digestive tract (Incorrect)\n" +
                        "Transport is managed by propulsion, not chemical digestion.\n\n" +
                        "Forming feces in the large intestine (Incorrect)\n" +
                        "Feces formation is the result of water absorption and waste compaction, not chemical digestion."
        );

        questions.add("Which structure significantly increases the surface area for nutrient absorption in the small intestine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Villi and microvilli", // Correct answer
                "Rugae",
                "Folds of the large intestine",
                "Sphincters"
        )));
        correctAnswers.add("Villi and microvilli");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Villi and microvilli (Correct answer)\n" +
                        "These structures are adaptations that maximize absorptive efficiency in the small intestine.\n\n" +
                        "Rugae (Incorrect)\n" +
                        "Rugae are folds found in the stomach, not in the small intestine.\n\n" +
                        "Folds of the large intestine (Incorrect)\n" +
                        "Although the large intestine has folds, they are not specialized for nutrient absorption.\n\n" +
                        "Sphincters (Incorrect)\n" +
                        "Sphincters regulate food passage but do not increase surface area."
        );

        questions.add("What is the role of the gastrointestinal (GI) tract’s “propulsion” activity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It systematically moves food from the mouth to the anus", // Correct answer
                "It reduces the need for digestive enzymes",
                "It stops chemical digestion",
                "It stores nutrients"
        )));
        correctAnswers.add("It systematically moves food from the mouth to the anus");
        rationales.put(73,
                "RATIONALE:\n" +
                        "It systematically moves food from the mouth to the anus (Correct answer)\n" +
                        "Propulsion, via peristalsis, is responsible for moving food through the digestive tract.\n\n" +
                        "It reduces the need for digestive enzymes (Incorrect)\n" +
                        "Propulsion does not influence the need for digestive enzymes.\n\n" +
                        "It stops chemical digestion (Incorrect)\n" +
                        "Propulsion is a part of the digestion process, not a factor that halts digestion.\n\n" +
                        "It stores nutrients (Incorrect)\n" +
                        "Propulsion is involved in food movement, not nutrient storage."
        );

        questions.add("Which of the following is an example of the interplay between the digestive and immune systems?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The activity of gut-associated lymphoid tissue (GALT)", // Correct answer
                "Secretion of gastric acid",
                "Absorption in the small intestine",
                "The hormonal release by the pancreas"
        )));
        correctAnswers.add("The activity of gut-associated lymphoid tissue (GALT)");
        rationales.put(74,
                "RATIONALE:\n" +
                        "The activity of gut-associated lymphoid tissue (GALT) (Correct answer)\n" +
                        "GALT protects the digestive system from pathogens, integrating immunity with digestion.\n\n" +
                        "Secretion of gastric acid (Incorrect)\n" +
                        "Gastric acid helps kill pathogens but does not represent direct immune system interaction.\n\n" +
                        "Absorption in the small intestine (Incorrect)\n" +
                        "Absorption is a digestive function and does not illustrate immune system interaction.\n\n" +
                        "The hormonal release by the pancreas (Incorrect)\n" +
                        "Pancreatic hormones relate to metabolism and blood sugar regulation, not immune defense."
        );

        questions.add("In the overview, the digestive system is said to discard indigestible waste. What does this imply?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Only nutrients are used, while nonusable parts are eliminated", // Correct answer
                "All ingested food is fully absorbed",
                "The body retains all food material",
                "Waste is converted back into nutrients"
        )));
        correctAnswers.add("Only nutrients are used, while nonusable parts are eliminated");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Only nutrients are used, while nonusable parts are eliminated (Correct answer)\n" +
                        "The digestive system absorbs nutrients from food and eliminates the indigestible components.\n\n" +
                        "All ingested food is fully absorbed (Incorrect)\n" +
                        "Not all food is absorbable; indigestible components are expelled.\n\n" +
                        "The body retains all food material (Incorrect)\n" +
                        "This would not be beneficial, as indigestible waste must be removed.\n\n" +
                        "Waste is converted back into nutrients (Incorrect)\n" +
                        "Indigestible waste is eliminated and not converted back into nutrients."
        );

        questions.add("Which of the following is NOT an accessory organ of digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Stomach", // Correct answer
                "Liver",
                "Gallbladder",
                "Pancreas"
        )));
        correctAnswers.add("Stomach");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Stomach (Correct answer)\n" +
                        "The stomach is a primary digestive organ, not an accessory organ.\n\n" +
                        "Liver (Incorrect)\n" +
                        "The liver is an accessory organ that produces bile, aiding digestion.\n\n" +
                        "Gallbladder (Incorrect)\n" +
                        "The gallbladder stores bile, which is essential for digestion.\n\n" +
                        "Pancreas (Incorrect)\n" +
                        "The pancreas produces enzymes and hormones that help in digestion."
        );

        questions.add("What stage in the digestive process involves the 'compacting' of undigested material?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Defecation", // Correct answer
                "Ingestion",
                "Absorption",
                "Mechanical digestion"
        )));
        correctAnswers.add("Defecation");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Defecation (Correct answer)\n" +
                        "The large intestine compacts waste for elimination during defecation.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is the intake of food, not compaction of waste.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption extracts nutrients from food, not waste.\n\n" +
                        "Mechanical digestion (Incorrect)\n" +
                        "Mechanical digestion breaks food apart but does not compact it."
        );

        questions.add("The overview states that digestion is a 'multistep process.' Which sequence of stages is correct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ingestion → Propulsion → Mechanical Digestion → Chemical Digestion → Absorption → Defecation", // Correct answer
                "Propulsion → Ingestion → Defecation → Absorption → Chemical Digestion → Mechanical Digestion",
                "Absorption → Ingestion → Chemical Digestion → Defecation → Mechanical Digestion → Propulsion",
                "Ingestion → Absorption → Chemical Digestion → Propulsion → Mechanical Digestion → Defecation"
        )));
        correctAnswers.add("Ingestion → Propulsion → Mechanical Digestion → Chemical Digestion → Absorption → Defecation");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Ingestion → Propulsion → Mechanical Digestion → Chemical Digestion → Absorption → Defecation (Correct answer)\n" +
                        "This is the correct order of stages in the digestive process.\n\n" +
                        "Propulsion → Ingestion → Defecation → Absorption → Chemical Digestion → Mechanical Digestion (Incorrect)\n" +
                        "This order does not follow the step-by-step nature of digestion.\n\n" +
                        "Absorption → Ingestion → Chemical Digestion → Defecation → Mechanical Digestion → Propulsion (Incorrect)\n" +
                        "This order is not correct for the digestive stages.\n\n" +
                        "Ingestion → Absorption → Chemical Digestion → Propulsion → Mechanical Digestion → Defecation (Incorrect)\n" +
                        "This order does not reflect the correct sequence for digestion."
        );

        questions.add("The phrase 'discarding indigestible waste' specifically refers to which digestive stage?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Defecation", // Correct answer
                "Chemical digestion",
                "Absorption",
                "Propulsion"
        )));
        correctAnswers.add("Defecation");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Defecation (Correct answer)\n" +
                        "The elimination of indigestible waste is referred to as defecation.\n\n" +
                        "Chemical digestion (Incorrect)\n" +
                        "Chemical digestion involves breaking down food into nutrients, not waste elimination.\n\n" +
                        "Absorption (Incorrect)\n" +
                        "Absorption is the process of taking nutrients into the body, not discarding waste.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion moves food along the digestive tract but does not eliminate waste."
        );

        questions.add("Which structure directly receives and then later expels the compacted material in the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Large intestine", // Correct answer
                "Small intestine",
                "Esophagus",
                "Stomach"
        )));
        correctAnswers.add("Large intestine");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Large intestine (Correct answer)\n" +
                        "The large intestine absorbs water and forms feces that are later expelled.\n\n" +
                        "Small intestine (Incorrect)\n" +
                        "The small intestine is primarily absorptive.\n\n" +
                        "Esophagus (Incorrect)\n" +
                        "The esophagus only transports food to the stomach.\n\n" +
                        "Stomach (Incorrect)\n" +
                        "The stomach is involved in digestion, not in waste compaction or expulsion."
        );

        questions.add("The integration of the nervous, endocrine, and immune systems with digestion primarily ensures that digestion is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Highly coordinated and efficient", // Correct answer
                "Unregulated",
                "Random and inconsistent",
                "Independent of the rest of the body"
        )));
        correctAnswers.add("Highly coordinated and efficient");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Highly coordinated and efficient (Correct answer)\n" +
                        "This integration ensures proper coordination, control, and efficiency in digestion.\n\n" +
                        "Unregulated (Incorrect)\n" +
                        "Integration leads to precise regulation, not a lack thereof.\n\n" +
                        "Random and inconsistent (Incorrect)\n" +
                        "The coordinated efforts lead to consistency in the digestive process.\n\n" +
                        "Independent of the rest of the body (Incorrect)\n" +
                        "The digestive process is closely linked to other body systems."
        );

        questions.add("What is the role of hormones like secretin and cholecystokinin (CCK) in the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "They regulate the release of bile and pancreatic enzymes", // Correct answer
                "They initiate chewing",
                "They store indigestible waste",
                "They form the structural basis of the intestinal wall"
        )));
        correctAnswers.add("They regulate the release of bile and pancreatic enzymes");
        rationales.put(82,
                "RATIONALE:\n" +
                        "They regulate the release of bile and pancreatic enzymes (Correct answer)\n" +
                        "These hormones help coordinate the chemical digestion in the small intestine.\n\n" +
                        "They initiate chewing (Incorrect)\n" +
                        "Chewing is a voluntary, mechanical process not triggered by these hormones.\n\n" +
                        "They store indigestible waste (Incorrect)\n" +
                        "Waste storage is not a function of these hormones.\n\n" +
                        "They form the structural basis of the intestinal wall (Incorrect)\n" +
                        "Structural support is provided by the tissue architecture, not by hormones."
        );

        questions.add("Which statement best describes the term “nutrient absorption” as used in the overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The transfer of digested nutrients from the intestinal lumen into the bloodstream or lymphatic system", // Correct answer
                "The process by which food is mechanically broken down",
                "The storage of waste products",
                "The expulsion of indigestible matter"
        )));
        correctAnswers.add("The transfer of digested nutrients from the intestinal lumen into the bloodstream or lymphatic system");
        rationales.put(83,
                "RATIONALE:\n" +
                        "The transfer of digested nutrients from the intestinal lumen into the bloodstream or lymphatic system (Correct answer)\n" +
                        "Nutrient absorption is the uptake of digested food molecules into the body’s circulation.\n\n" +
                        "The process by which food is mechanically broken down (Incorrect)\n" +
                        "Mechanical digestion is separate from absorption.\n\n" +
                        "The storage of waste products (Incorrect)\n" +
                        "Waste storage is not part of nutrient absorption.\n\n" +
                        "The expulsion of indigestible matter (Incorrect)\n" +
                        "Expulsion of waste is defecation, not absorption."
        );

        questions.add("How does the structure of the small intestine enhance its absorptive capacity?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is lined with villi and microvilli that increase surface area", // Correct answer
                "It has a smooth surface",
                "It produces large amounts of gastric acid",
                "It contains many sphincters"
        )));
        correctAnswers.add("It is lined with villi and microvilli that increase surface area");
        rationales.put(84,
                "RATIONALE:\n" +
                        "It is lined with villi and microvilli that increase surface area (Correct answer)\n" +
                        "The folds, villi, and microvilli multiply the absorptive area and maximize nutrient uptake.\n\n" +
                        "It has a smooth surface (Incorrect)\n" +
                        "A completely smooth surface would reduce absorptive area.\n\n" +
                        "It produces large amounts of gastric acid (Incorrect)\n" +
                        "Gastric acid is produced in the stomach, not in the small intestine, and would damage absorptive tissues if produced there.\n\n" +
                        "It contains many sphincters (Incorrect)\n" +
                        "Sphincters help regulate passage of food but do not enhance absorption."
        );

        questions.add("What is the primary purpose of mechanical digestion in the mouth and stomach?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To reduce food particle size and increase the effectiveness of enzymes", // Correct answer
                "To chemically break down food molecules",
                "To produce hormones",
                "To absorb water"
        )));
        correctAnswers.add("To reduce food particle size and increase the effectiveness of enzymes");
        rationales.put(85,
                "RATIONALE:\n" +
                        "To reduce food particle size and increase the effectiveness of enzymes (Correct answer)\n" +
                        "Mechanical digestion increases the surface area available for chemical enzymes to work on.\n\n" +
                        "To chemically break down food molecules (Incorrect)\n" +
                        "Chemical breakdown is performed by enzymes in chemical digestion.\n\n" +
                        "To produce hormones (Incorrect)\n" +
                        "Hormone production is not a function of mechanical digestion.\n\n" +
                        "To absorb water (Incorrect)\n" +
                        "Absorption of water mainly occurs in the intestines, not by mechanical digestion."
        );

        questions.add("Which system distributes the absorbed nutrients as described in the digestive overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The circulatory system", // Correct answer
                "The nervous system",
                "The respiratory system",
                "The urinary system"
        )));
        correctAnswers.add("The circulatory system");
        rationales.put(86,
                "RATIONALE:\n" +
                        "The circulatory system (Correct answer)\n" +
                        "Blood vessels in the circulatory system transport nutrients to cells throughout the body.\n\n" +
                        "The nervous system (Incorrect)\n" +
                        "The nervous system transmits signals rather than transporting nutrients.\n\n" +
                        "The respiratory system (Incorrect)\n" +
                        "The respiratory system handles gas exchange, not nutrient transport.\n\n" +
                        "The urinary system (Incorrect)\n" +
                        "The urinary system is responsible for eliminating waste products from the blood."
        );

        questions.add("The digestive system is deeply integrated with which of the following systems to ensure proper regulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nervous, endocrine, and immune systems", // Correct answer
                "Skeletal system",
                "Integumentary system",
                "Muscular system exclusively"
        )));
        correctAnswers.add("Nervous, endocrine, and immune systems");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Nervous, endocrine, and immune systems (Correct answer)\n" +
                        "Integration with these systems supports regulated digestion and overall metabolic balance.\n\n" +
                        "The skeletal system (Incorrect)\n" +
                        "The skeletal system provides support but is not involved in regulating digestion.\n\n" +
                        "The integumentary system (Incorrect)\n" +
                        "The integumentary system (skin, hair, nails) is not directly involved in digestion.\n\n" +
                        "The muscular system exclusively (Incorrect)\n" +
                        "Although muscles are needed for mechanical digestion, regulation requires more than just the muscular system."
        );

        questions.add("Which stage of digestion overlaps with both mechanical and chemical processes?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Digestion in the stomach", // Correct answer
                "Ingestion",
                "Propulsion",
                "Defecation"
        )));
        correctAnswers.add("Digestion in the stomach");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Digestion in the stomach (Correct answer)\n" +
                        "In the stomach, food is both mechanically churned and chemically processed via gastric juices.\n\n" +
                        "Ingestion (Incorrect)\n" +
                        "Ingestion is mainly about the intake of food.\n\n" +
                        "Propulsion (Incorrect)\n" +
                        "Propulsion by peristalsis moves food along the tract but does not combine mechanical and chemical breakdown.\n\n" +
                        "Defecation (Incorrect)\n" +
                        "Defecation is solely about waste elimination."
        );

        questions.add("The overview describes the digestive system as “specialized.” This means that it:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Is uniquely adapted to break down food into nutrients and eliminate waste", // Correct answer
                "Performs only one function",
                "Functions identically to all other body systems",
                "Does not change over time"
        )));
        correctAnswers.add("Is uniquely adapted to break down food into nutrients and eliminate waste");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Is uniquely adapted to break down food into nutrients and eliminate waste (Correct answer)\n" +
                        "Its specialization is evident in its multi-organ, coordinated approach to digestion.\n\n" +
                        "Performs only one function (Incorrect)\n" +
                        "The system performs a variety of functions, not just one.\n\n" +
                        "Functions identically to all other body systems (Incorrect)\n" +
                        "Each system has unique adaptations—for digestion, the adaptations focus on breaking down food and absorbing nutrients.\n\n" +
                        "Does not change over time (Incorrect)\n" +
                        "While it is specialized, the digestive system can change (e.g., due to diet, age, or disease)."
        );

        questions.add("Which of the following best captures the overall goal of the digestive process as described?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To transform food into absorbable molecules and remove waste", // Correct answer
                "To cool the body by processing food",
                "To store food for long periods",
                "To produce hormones exclusively"
        )));
        correctAnswers.add("To transform food into absorbable molecules and remove waste");
        rationales.put(90,
                "RATIONALE:\n" +
                        "To transform food into absorbable molecules and remove waste (Correct answer)\n" +
                        "The goal is to extract essential nutrients while discarding what cannot be used.\n\n" +
                        "To cool the body by processing food (Incorrect)\n" +
                        "Digestive processes are about nutrient extraction, not temperature regulation.\n\n" +
                        "To store food for long periods (Incorrect)\n" +
                        "Storage is not the primary goal; rather, processing and absorption are.\n\n" +
                        "To produce hormones exclusively (Incorrect)\n" +
                        "Hormone production is one of many functions, not the sole goal."
        );

        questions.add("What does the term “multistep process” imply about digestion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It includes ordered phases such as ingestion, propulsion, digestion (mechanical and chemical), absorption, and defecation", // Correct answer
                "It happens in one quick step",
                "It does not involve any coordination",
                "It is entirely dependent on the liver"
        )));
        correctAnswers.add("It includes ordered phases such as ingestion, propulsion, digestion (mechanical and chemical), absorption, and defecation");
        rationales.put(91,
                "RATIONALE:\n" +
                        "It includes ordered phases such as ingestion, propulsion, digestion (mechanical and chemical), absorption, and defecation (Correct answer)\n" +
                        "The process is sequential and complex, involving several stages.\n\n" +
                        "It happens in one quick step (Incorrect)\n" +
                        "“Multistep” clearly means it happens in several coordinated phases.\n\n" +
                        "It does not involve any coordination at all (Incorrect)\n" +
                        "Digestion is highly coordinated by multiple systems.\n\n" +
                        "It is entirely dependent on the liver (Incorrect)\n" +
                        "While the liver plays an important role (via bile production), digestion involves many organs and stages."
        );

        questions.add("Which of the following is a direct result of the efficient absorption process in the small intestine?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Distribution of nutrients to cells via the circulatory system", // Correct answer
                "Immediate waste expulsion",
                "Increased production of bile",
                "Slower metabolism"
        )));
        correctAnswers.add("Distribution of nutrients to cells via the circulatory system");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Distribution of nutrients to cells via the circulatory system (Correct answer)\n" +
                        "Nutrients absorbed in the small intestine are delivered to cells throughout the body.\n\n" +
                        "Immediate waste expulsion (Incorrect)\n" +
                        "Waste expulsion occurs later in defecation, not directly as a result of absorption.\n\n" +
                        "Increased production of bile (Incorrect)\n" +
                        "Bile production is regulated by liver function and hormonal signals, not directly by absorption efficiency.\n\n" +
                        "Slower metabolism (Incorrect)\n" +
                        "Efficient absorption generally supports normal metabolism rather than causing it to slow down."
        );

        questions.add("What is the definition of “defecation” as used in the overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "The expulsion of indigestible material from the large intestine", // Correct answer
                "The process of swallowing food",
                "The mixing of food with digestive enzymes",
                "The absorption of nutrients"
        )));
        correctAnswers.add("The expulsion of indigestible material from the large intestine");
        rationales.put(93,
                "RATIONALE:\n" +
                        "The expulsion of indigestible material from the large intestine (Correct answer)\n" +
                        "Defecation is the final step in eliminating waste from the body.\n\n" +
                        "The process of swallowing food (Incorrect)\n" +
                        "Swallowing is part of ingestion, not defecation.\n\n" +
                        "The mixing of food with digestive enzymes (Incorrect)\n" +
                        "Mixing is part of the chemical and mechanical digestion stages.\n\n" +
                        "The absorption of nutrients (Incorrect)\n" +
                        "Absorption happens primarily in the small intestine and is distinct from defecation."
        );

        questions.add("Which of the following processes allows the digestive system to “fuel bodily functions”?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Breaking down food into nutrients that are absorbed and utilized", // Correct answer
                "Storing food in the stomach",
                "Increasing water absorption only",
                "Producing excess bile"
        )));
        correctAnswers.add("Breaking down food into nutrients that are absorbed and utilized");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Breaking down food into nutrients that are absorbed and utilized (Correct answer)\n" +
                        "The nutrients derived from food are essential for energizing and maintaining bodily functions.\n\n" +
                        "Storing food in the stomach (Incorrect)\n" +
                        "Food is stored only temporarily, and storage does not supply energy to cells.\n\n" +
                        "Increasing water absorption only (Incorrect)\n" +
                        "Water absorption is important but does not itself fuel bodily functions.\n\n" +
                        "Producing excess bile (Incorrect)\n" +
                        "Bile aids in fat digestion but is not directly responsible for “fueling” the body."
        );

        questions.add("The term “regulated” in the context of the digestive system means that the process is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Controlled by integrated signals from various systems (nervous, endocrine, immune)", // Correct answer
                "Random and uncoordinated",
                "Only mechanical with no chemical input",
                "Entirely independent of other body functions"
        )));
        correctAnswers.add("Controlled by integrated signals from various systems (nervous, endocrine, immune)");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Controlled by integrated signals from various systems (nervous, endocrine, immune) (Correct answer)\n" +
                        "Regulation is achieved through coordinated interactions with multiple body systems.\n\n" +
                        "Random and uncoordinated (Incorrect)\n" +
                        "Regulation implies a controlled, orderly process.\n\n" +
                        "Only mechanical with no chemical input (Incorrect)\n" +
                        "Regulation involves both mechanical and chemical processes.\n\n" +
                        "Entirely independent of other body functions (Incorrect)\n" +
                        "The digestive system is closely coordinated with other body systems."
        );

        questions.add("How does the pancreas’ endocrine function contribute to digestion, aside from enzyme production?");
        choices.add(new ArrayList<>(Arrays.asList(
                "By releasing hormones like insulin and glucagon to regulate blood sugar", // Correct answer
                "By storing food particles",
                "By mechanically churning food",
                "By absorbing vitamins"
        )));
        correctAnswers.add("By releasing hormones like insulin and glucagon to regulate blood sugar");
        rationales.put(96,
                "RATIONALE:\n" +
                        "By releasing hormones like insulin and glucagon to regulate blood sugar (Correct answer)\n" +
                        "The pancreas regulates blood glucose levels during digestion through its endocrine function by releasing hormones such as insulin and glucagon.\n\n" +
                        "By storing food particles (Incorrect)\n" +
                        "The pancreas does not store food.\n\n" +
                        "By mechanically churning food (Incorrect)\n" +
                        "Mechanical churning is a function of the stomach, not the pancreas.\n\n" +
                        "By absorbing vitamins (Incorrect)\n" +
                        "The absorption of vitamins occurs in the intestines, not via the pancreas’ endocrine function."
        );

        questions.add("Which statement accurately reflects the overview’s description of the digestive system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is an integrated, multistep process that breaks down food, absorbs nutrients, and discards waste while interacting with other systems", // Correct answer
                "It solely relies on mechanical digestion.",
                "It is a simple process that occurs only in the stomach.",
                "It functions independently without any hormonal influences."
        )));
        correctAnswers.add("It is an integrated, multistep process that breaks down food, absorbs nutrients, and discards waste while interacting with other systems");
        rationales.put(97,
                "RATIONALE:\n" +
                        "It is an integrated, multistep process that breaks down food, absorbs nutrients, and discards waste while interacting with other systems (Correct answer)\n" +
                        "This statement accurately describes the complex and coordinated nature of digestion.\n\n" +
                        "It solely relies on mechanical digestion. (Incorrect)\n" +
                        "The digestive system uses both mechanical and chemical processes, not solely mechanical digestion.\n\n" +
                        "It is a simple process that occurs only in the stomach. (Incorrect)\n" +
                        "Digestion is a complex process that involves multiple organs, not just the stomach.\n\n" +
                        "It functions independently without any hormonal influences. (Incorrect)\n" +
                        "The digestive system is regulated by hormonal, neural, and immune factors."
        );

        questions.add("Which element is critical for the chemical digestion of macromolecules?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Enzymes and digestive juices", // Correct answer
                "Only water",
                "The act of chewing alone",
                "Muscular contractions only"
        )));
        correctAnswers.add("Enzymes and digestive juices");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Enzymes and digestive juices (Correct answer)\n" +
                        "Enzymes and digestive juices chemically break down large macromolecules into smaller, absorbable units.\n\n" +
                        "Only water (Incorrect)\n" +
                        "While water is essential in digestion, it is not the primary agent responsible for chemical digestion.\n\n" +
                        "The act of chewing alone (Incorrect)\n" +
                        "Chewing is a mechanical process, not a chemical one.\n\n" +
                        "Muscular contractions only (Incorrect)\n" +
                        "Muscular contractions aid in mixing and propulsion, but chemical digestion requires enzymatic action."
        );

        questions.add("Overall, what is the primary role of the digestive system as presented in the overview?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To break down food into nutrients for energy and remove indigestible waste, while integrating with other bodily systems", // Correct answer
                "To produce blood cells and hormones exclusively",
                "To store food without processing it",
                "To solely regulate body temperature"
        )));
        correctAnswers.add("To break down food into nutrients for energy and remove indigestible waste, while integrating with other bodily systems");
        rationales.put(99,
                "RATIONALE:\n" +
                        "To break down food into nutrients for energy and remove indigestible waste, while integrating with other bodily systems (Correct answer)\n" +
                        "The digestive system processes food, extracts nutrients, and eliminates waste while integrating with other systems like the nervous, endocrine, immune, and circulatory systems.\n\n" +
                        "To produce blood cells and hormones exclusively (Incorrect)\n" +
                        "Blood cell production occurs in the bone marrow, and hormones are produced by several body systems, not just the digestive system.\n\n" +
                        "To store food without processing it (Incorrect)\n" +
                        "Food is processed by the digestive system; it is not merely stored.\n\n" +
                        "To solely regulate body temperature (Incorrect)\n" +
                        "Body temperature regulation is handled by the hypothalamus and circulatory system, not the digestive system."
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
        new AlertDialog.Builder(Prac_10_Digestive.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
