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

public class Prac_3_Skeletal extends AppCompatActivity {

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

        setContentView(R.layout.prac_3_skeletal);

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
                Toast.makeText(Prac_3_Skeletal.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_3_Skeletal.this)
                    .setTitle("Restart Quiz")
                    .setMessage("Are you sure you want to restart? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> resetQuiz())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        exitIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_3_Skeletal.this)
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
                new AlertDialog.Builder(Prac_3_Skeletal.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_3_Skeletal.this, Answer_Result.class);
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

                new AlertDialog.Builder(Prac_3_Skeletal.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_3_Skeletal.this, Answer_Result.class);
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

        questions.add("Which of the following is NOT a function of the skeletal system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hormone secretion", // Correct answer
                "Mineral storage",
                "Blood cell production",
                "Protection of internal organs"
        )));
        correctAnswers.add("Hormone secretion");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Hormone secretion (Correct answer)\n" +
                        "The skeletal system does not secrete hormones as a primary function. Hormone secretion is mainly the role of the endocrine system.\n\n" +
                        "Mineral storage (Incorrect)\n" +
                        "Bones store calcium and phosphorus.\n\n" +
                        "Blood cell production (Incorrect)\n" +
                        "Red bone marrow in bones produces blood cells.\n\n" +
                        "Protection of internal organs (Incorrect)\n" +
                        "Bones like the skull and ribs protect organs."
        );

        questions.add("Which bone is an example of a long bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Femur", // Correct answer
                "Patella",
                "Vertebra",
                "Sternum"
        )));
        correctAnswers.add("Femur");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Femur (Correct answer)\n" +
                        "Long bones are longer than they are wide and support weight and movement. The femur is a classic example.\n\n" +
                        "Patella (Incorrect)\n" +
                        "The patella is a sesamoid bone.\n\n" +
                        "Vertebra (Incorrect)\n" +
                        "The vertebra is an irregular bone.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is a flat bone."
        );

        questions.add("Where does hematopoiesis occur in adults?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spongy bone", // Correct answer
                "Yellow bone marrow",
                "Compact bone",
                "Periosteum"
        )));
        correctAnswers.add("Spongy bone");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Spongy bone (Correct answer)\n" +
                        "Spongy bone contains red bone marrow, which produces blood cells.\n\n" +
                        "Yellow bone marrow (Incorrect)\n" +
                        "Yellow marrow stores fat, not blood cells.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone is for structure, not hematopoiesis.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "Periosteum is the outer layer, not involved in blood production."
        );

        questions.add("What type of joint is found in the skull?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Synarthrosis", // Correct answer
                "Diarthrosis",
                "Amphiarthrosis",
                "Synovial"
        )));
        correctAnswers.add("Synarthrosis");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Synarthrosis (Correct answer)\n" +
                        "Synarthrosis joints are immovable, such as the sutures of the skull.\n\n" +
                        "Diarthrosis (Incorrect)\n" +
                        "Diarthrosis joints are freely movable (e.g., shoulder).\n\n" +
                        "Amphiarthrosis (Incorrect)\n" +
                        "Amphiarthrosis joints allow slight movement (e.g., spine).\n\n" +
                        "Synovial (Incorrect)\n" +
                        "Synovial is another name for diarthrosis."
        );

        questions.add("Which cell is responsible for breaking down bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteoclast", // Correct answer
                "Osteoblast",
                "Osteocyte",
                "Chondrocyte"
        )));
        correctAnswers.add("Osteoclast");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Osteoclast (Correct answer)\n" +
                        "Osteoclasts break down bone tissue during remodeling and calcium release.\n\n" +
                        "Osteoblast (Incorrect)\n" +
                        "Osteoblasts build bone.\n\n" +
                        "Osteocyte (Incorrect)\n" +
                        "Osteocytes maintain bone tissue.\n\n" +
                        "Chondrocyte (Incorrect)\n" +
                        "Chondrocytes are cartilage cells."
        );

        questions.add("What is the function of the medullary cavity in long bones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Houses yellow bone marrow", // Correct answer
                "Protects organs",
                "Facilitates joint movement",
                "Contains red bone marrow"
        )));
        correctAnswers.add("Houses yellow bone marrow");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Houses yellow bone marrow (Correct answer)\n" +
                        "The medullary cavity contains yellow bone marrow, which stores fat.\n\n" +
                        "Protects organs (Incorrect)\n" +
                        "Protection is a general skeletal function.\n\n" +
                        "Facilitates joint movement (Incorrect)\n" +
                        "Joints, not the medullary cavity, aid movement.\n\n" +
                        "Contains red bone marrow (Incorrect)\n" +
                        "Red marrow is mainly found in spongy bone, not the medullary cavity."
        );

        questions.add("Which is considered part of the axial skeleton?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sternum", // Correct answer
                "Femur",
                "Scapula",
                "Pelvis"
        )));
        correctAnswers.add("Sternum");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Sternum (Correct answer)\n" +
                        "The sternum is part of the axial skeleton which includes the skull, spine, and thoracic cage.\n\n" +
                        "Femur (Incorrect)\n" +
                        "Femur is in the appendicular skeleton.\n\n" +
                        "Scapula (Incorrect)\n" +
                        "Scapula belongs to the appendicular skeleton.\n\n" +
                        "Pelvis (Incorrect)\n" +
                        "Pelvis is part of the appendicular skeleton."
        );

        questions.add("Which bone marking is a shallow depression?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fossa", // Correct answer
                "Foramen",
                "Condyle",
                "Tuberosity"
        )));
        correctAnswers.add("Fossa");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Fossa (Correct answer)\n" +
                        "A fossa is a shallow depression in a bone, often for muscle attachment or articulation.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is a hole.\n\n" +
                        "Condyle (Incorrect)\n" +
                        "A condyle is a rounded articular surface.\n\n" +
                        "Tuberosity (Incorrect)\n" +
                        "A tuberosity is a roughened projection."
        );

        questions.add("What is the outer surface membrane of a bone called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Periosteum", // Correct answer
                "Endosteum",
                "Epiphysis",
                "Diaphysis"
        )));
        correctAnswers.add("Periosteum");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Periosteum (Correct answer)\n" +
                        "The periosteum is a dense membrane that covers the outer surface of bones.\n\n" +
                        "Endosteum (Incorrect)\n" +
                        "Endosteum lines the inner cavity.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "Epiphysis is the end of a long bone.\n\n" +
                        "Diaphysis (Incorrect)\n" +
                        "Diaphysis is the shaft of a long bone."
        );

        questions.add("Which stage of bone healing involves formation of a soft callus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fibrocartilaginous callus formation", // Correct answer
                "Hematoma formation",
                "Bone remodeling",
                "Bony callus formation"
        )));
        correctAnswers.add("Fibrocartilaginous callus formation");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Fibrocartilaginous callus formation (Correct answer)\n" +
                        "This stage creates a soft callus made of collagen and cartilage to bridge the break.\n\n" +
                        "Hematoma formation (Incorrect)\n" +
                        "Hematoma is the initial blood clot.\n\n" +
                        "Bone remodeling (Incorrect)\n" +
                        "Bone remodeling is the final stage.\n\n" +
                        "Bony callus formation (Incorrect)\n" +
                        "Bony callus comes after the fibrocartilaginous callus."
        );

        questions.add("What is the shaft of a long bone called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphysis", // Correct answer
                "Epiphysis",
                "Metaphysis",
                "Periosteum"
        )));
        correctAnswers.add("Diaphysis");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Diaphysis (Correct answer)\n" +
                        "The diaphysis is the long, cylindrical shaft of a long bone.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "Epiphysis refers to the ends of a long bone.\n\n" +
                        "Metaphysis (Incorrect)\n" +
                        "Metaphysis is the growth region between the epiphysis and diaphysis.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "Periosteum is the outer membrane covering the bone."
        );

        questions.add("Which of the following bones is classified as a sesamoid bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Patella", // Correct answer
                "Radius",
                "Tibia",
                "Ulna"
        )));
        correctAnswers.add("Patella");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Patella (Correct answer)\n" +
                        "The patella is a sesamoid bone because it is embedded within a tendon.\n\n" +
                        "Radius (Incorrect)\n" +
                        "Radius is a long bone.\n\n" +
                        "Tibia (Incorrect)\n" +
                        "Tibia is also a long bone.\n\n" +
                        "Ulna (Incorrect)\n" +
                        "Ulna is a long bone as well."
        );

        questions.add("Which of these bones is part of the appendicular skeleton?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Humerus", // Correct answer
                "Sternum",
                "Vertebra",
                "Skull"
        )));
        correctAnswers.add("Humerus");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Humerus (Correct answer)\n" +
                        "The humerus is part of the upper limb and classified under the appendicular skeleton.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is part of the axial skeleton.\n\n" +
                        "Vertebra (Incorrect)\n" +
                        "Vertebrae belong to the axial skeleton.\n\n" +
                        "Skull (Incorrect)\n" +
                        "The skull is part of the axial skeleton."
        );

        questions.add("What type of bone cell builds new bone tissue?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteoblast", // Correct answer
                "Osteocyte",
                "Osteoclast",
                "Chondroblast"
        )));
        correctAnswers.add("Osteoblast");
        rationales.put(13,
                "RATIONALE:\n" +
                        "Osteoblast (Correct answer)\n" +
                        "Osteoblasts secrete bone matrix and are responsible for bone formation.\n\n" +
                        "Osteocyte (Incorrect)\n" +
                        "Osteocytes maintain bone tissue.\n\n" +
                        "Osteoclast (Incorrect)\n" +
                        "Osteoclasts break down bone.\n\n" +
                        "Chondroblast (Incorrect)\n" +
                        "Chondroblasts are cartilage-forming cells."
        );

        questions.add("Which disorder is characterized by decreased bone density?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteoporosis", // Correct answer
                "Arthritis",
                "Scoliosis",
                "Rickets"
        )));
        correctAnswers.add("Osteoporosis");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Osteoporosis (Correct answer)\n" +
                        "Osteoporosis leads to fragile bones due to reduced bone mass and density.\n\n" +
                        "Arthritis (Incorrect)\n" +
                        "Arthritis involves inflammation of joints.\n\n" +
                        "Scoliosis (Incorrect)\n" +
                        "Scoliosis is a lateral curvature of the spine.\n\n" +
                        "Rickets (Incorrect)\n" +
                        "Rickets results from vitamin D deficiency, leading to bone softening."
        );

        questions.add("What type of joint allows for the greatest range of motion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diarthrosis", // Correct answer
                "Synarthrosis",
                "Amphiarthrosis",
                "Suture"
        )));
        correctAnswers.add("Diarthrosis");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Diarthrosis (Correct answer)\n" +
                        "Diarthrosis (synovial joints) are freely movable joints like the shoulder and knee.\n\n" +
                        "Synarthrosis (Incorrect)\n" +
                        "Synarthrosis joints are immovable (e.g., skull).\n\n" +
                        "Amphiarthrosis (Incorrect)\n" +
                        "Amphiarthrosis joints have limited movement.\n\n" +
                        "Suture (Incorrect)\n" +
                        "Sutures are a type of synarthrosis."
        );

        questions.add("What part of the bone contains the red marrow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spongy bone", // Correct answer
                "Compact bone",
                "Diaphysis",
                "Endosteum"
        )));
        correctAnswers.add("Spongy bone");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Spongy bone (Correct answer)\n" +
                        "Spongy bone (cancellous bone) contains red marrow responsible for hematopoiesis.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone is dense and mainly provides structure.\n\n" +
                        "Diaphysis (Incorrect)\n" +
                        "The diaphysis usually contains yellow marrow in adults.\n\n" +
                        "Endosteum (Incorrect)\n" +
                        "Endosteum lines the medullary cavity but doesn’t house red marrow."
        );

        questions.add("What is the rounded articular projection at the end of a bone called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Condyle", // Correct answer
                "Foramen",
                "Tuberosity",
                "Fossa"
        )));
        correctAnswers.add("Condyle");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Condyle (Correct answer)\n" +
                        "A condyle is a smooth, rounded projection that forms a joint with another bone.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is a hole for nerves or blood vessels.\n\n" +
                        "Tuberosity (Incorrect)\n" +
                        "A tuberosity is a rough projection for muscle attachment.\n\n" +
                        "Fossa (Incorrect)\n" +
                        "A fossa is a shallow depression."
        );

        questions.add("Which bone disorder is commonly caused by a vitamin D deficiency?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rickets", // Correct answer
                "Scoliosis",
                "Osteoporosis",
                "Arthritis"
        )));
        correctAnswers.add("Rickets");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Rickets (Correct answer)\n" +
                        "Rickets results from a lack of vitamin D, leading to soft and weak bones in children.\n\n" +
                        "Scoliosis (Incorrect)\n" +
                        "Scoliosis is a spinal deformity.\n\n" +
                        "Osteoporosis (Incorrect)\n" +
                        "Osteoporosis is caused by bone mass loss, often due to aging or hormonal changes.\n\n" +
                        "Arthritis (Incorrect)\n" +
                        "Arthritis is joint inflammation, not a vitamin-related disease."
        );

        questions.add("What structure lines the inner cavity of a bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Endosteum", // Correct answer
                "Periosteum",
                "Compact bone",
                "Metaphysis"
        )));
        correctAnswers.add("Endosteum");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Endosteum (Correct answer)\n" +
                        "The endosteum is a thin membrane lining the medullary cavity of bones.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "Periosteum lines the outer surface of bones.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone is the dense outer layer, not a lining.\n\n" +
                        "Metaphysis (Incorrect)\n" +
                        "Metaphysis is the growth zone between epiphysis and diaphysis."
        );

        questions.add("Which bone is classified as a flat bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sternum", // Correct answer
                "Femur",
                "Patella",
                "Carpals"
        )));
        correctAnswers.add("Sternum");
        rationales.put(20,
                "RATIONALE:\n" +
                        "Sternum (Correct answer)\n" +
                        "The sternum is a flat bone that helps protect organs like the heart and lungs.\n\n" +
                        "Femur (Incorrect)\n" +
                        "The femur is a long bone, not flat.\n\n" +
                        "Patella (Incorrect)\n" +
                        "The patella is a sesamoid bone, not flat.\n\n" +
                        "Carpals (Incorrect)\n" +
                        "Carpals are short bones, not flat."
        );

        questions.add("Which structure covers the outer surface of a bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Periosteum", // Correct answer
                "Endosteum",
                "Epiphysis",
                "Medullary cavity"
        )));
        correctAnswers.add("Periosteum");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Periosteum (Correct answer)\n" +
                        "The periosteum is a fibrous membrane covering the outer surface of bones, important for bone growth and repair.\n\n" +
                        "Endosteum (Incorrect)\n" +
                        "The endosteum lines the inner bone cavity.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "The epiphysis refers to the end part of a long bone, not a covering.\n\n" +
                        "Medullary cavity (Incorrect)\n" +
                        "The medullary cavity is an internal space that contains marrow."
        );

        questions.add("Which part of a long bone contains yellow marrow in adults?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphysis", // Correct answer
                "Epiphysis",
                "Compact bone",
                "Spongy bone"
        )));
        correctAnswers.add("Diaphysis");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Diaphysis (Correct answer)\n" +
                        "In adults, the diaphysis contains the medullary cavity filled with yellow marrow for fat storage.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "The epiphysis mainly contains spongy bone with red marrow.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone surrounds the diaphysis but does not store marrow.\n\n" +
                        "Spongy bone (Incorrect)\n" +
                        "Spongy bone contains red marrow, not yellow."
        );

        questions.add("Which of the following is NOT a function of the skeletal system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hormone production", // Correct answer
                "Blood cell production",
                "Fat storage",
                "Mineral storage"
        )));
        correctAnswers.add("Hormone production");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Hormone production (Correct answer)\n" +
                        "Hormone production is primarily an endocrine function, not a role of the skeletal system.\n\n" +
                        "Blood cell production (Incorrect)\n" +
                        "Bone marrow produces blood cells.\n\n" +
                        "Fat storage (Incorrect)\n" +
                        "Yellow marrow stores fat.\n\n" +
                        "Mineral storage (Incorrect)\n" +
                        "Bones store calcium and phosphorus."
        );

        questions.add("What is the term for a shallow depression in a bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fossa", // Correct answer
                "Condyle",
                "Foramen",
                "Tuberosity"
        )));
        correctAnswers.add("Fossa");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Fossa (Correct answer)\n" +
                        "A fossa is a shallow depression that may articulate with a condyle.\n\n" +
                        "Condyle (Incorrect)\n" +
                        "A condyle is a rounded articular projection.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is a hole for blood vessels/nerves.\n\n" +
                        "Tuberosity (Incorrect)\n" +
                        "A tuberosity is a large projection for muscle attachment."
        );

        questions.add("Which joint type is found between the vertebrae?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Amphiarthrosis", // Correct answer
                "Synarthrosis",
                "Diarthrosis",
                "Suture"
        )));
        correctAnswers.add("Amphiarthrosis");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Amphiarthrosis (Correct answer)\n" +
                        "Amphiarthroses allow limited movement, such as the intervertebral discs between vertebrae.\n\n" +
                        "Synarthrosis (Incorrect)\n" +
                        "Synarthroses are immovable joints, like skull sutures.\n\n" +
                        "Diarthrosis (Incorrect)\n" +
                        "Diarthroses are freely movable joints.\n\n" +
                        "Suture (Incorrect)\n" +
                        "Sutures are a type of synarthrosis in the skull."
        );

        questions.add("The bone cells responsible for breaking down bone are called:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteoclasts", // Correct answer
                "Osteoblasts",
                "Osteocytes",
                "Chondrocytes"
        )));
        correctAnswers.add("Osteoclasts");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Osteoclasts (Correct answer)\n" +
                        "Osteoclasts resorb (break down) bone matrix during bone remodeling.\n\n" +
                        "Osteoblasts (Incorrect)\n" +
                        "Osteoblasts form new bone.\n\n" +
                        "Osteocytes (Incorrect)\n" +
                        "Osteocytes maintain bone tissue.\n\n" +
                        "Chondrocytes (Incorrect)\n" +
                        "Chondrocytes are cartilage cells."
        );

        questions.add("What type of bone tissue forms the dense outer layer of bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Compact bone", // Correct answer
                "Spongy bone",
                "Red marrow",
                "Yellow marrow"
        )));
        correctAnswers.add("Compact bone");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Compact bone (Correct answer)\n" +
                        "Compact bone is dense and forms the hard outer surface of bones.\n\n" +
                        "Spongy bone (Incorrect)\n" +
                        "Spongy bone is found inside bones and is porous.\n\n" +
                        "Red marrow (Incorrect)\n" +
                        "Red marrow is found in spongy bone, not a type of bone tissue.\n\n" +
                        "Yellow marrow (Incorrect)\n" +
                        "Yellow marrow is found inside the medullary cavity."
        );

        questions.add("The growth zone between the epiphysis and diaphysis is called the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Metaphysis", // Correct answer
                "Periosteum",
                "Endosteum",
                "Condyle"
        )));
        correctAnswers.add("Metaphysis");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Metaphysis (Correct answer)\n" +
                        "The metaphysis contains the growth plate where bone lengthening occurs in children and adolescents.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "Periosteum is the outer membrane.\n\n" +
                        "Endosteum (Incorrect)\n" +
                        "Endosteum lines the medullary cavity.\n\n" +
                        "Condyle (Incorrect)\n" +
                        "A condyle is a rounded projection, not a growth zone."
        );

        questions.add("Which of the following is an example of a short bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Carpals", // Correct answer
                "Femur",
                "Ribs",
                "Vertebrae"
        )));
        correctAnswers.add("Carpals");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Carpals (Correct answer)\n" +
                        "Carpals are cube-shaped bones in the wrist and are classified as short bones.\n\n" +
                        "Femur (Incorrect)\n" +
                        "The femur is a long bone.\n\n" +
                        "Ribs (Incorrect)\n" +
                        "Ribs are flat bones.\n\n" +
                        "Vertebrae (Incorrect)\n" +
                        "Vertebrae are irregular bones."
        );

        questions.add("Which of the following bones is part of the axial skeleton?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Skull", // Correct answer
                "Humerus",
                "Pelvis",
                "Femur"
        )));
        correctAnswers.add("Skull");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Skull (Correct answer)\n" +
                        "The skull is part of the axial skeleton, which includes the skull, vertebral column, and thoracic cage.\n\n" +
                        "Humerus (Incorrect)\n" +
                        "The humerus is part of the appendicular skeleton.\n\n" +
                        "Pelvis (Incorrect)\n" +
                        "The pelvis belongs to the appendicular skeleton.\n\n" +
                        "Femur (Incorrect)\n" +
                        "The femur is part of the appendicular skeleton."
        );

        questions.add("Which of the following bones is considered irregular in shape?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Vertebra", // Correct answer
                "Tibia",
                "Sternum",
                "Scapula"
        )));
        correctAnswers.add("Vertebra");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Vertebra (Correct answer)\n" +
                        "Vertebrae are irregular bones due to their complex shape.\n\n" +
                        "Tibia (Incorrect)\n" +
                        "The tibia is a long bone.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is a flat bone.\n\n" +
                        "Scapula (Incorrect)\n" +
                        "The scapula is a flat bone."
        );

        questions.add("Hematopoiesis occurs in which part of the bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spongy bone", // Correct answer
                "Yellow marrow",
                "Compact bone",
                "Periosteum"
        )));
        correctAnswers.add("Spongy bone");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Spongy bone (Correct answer)\n" +
                        "Spongy bone contains red marrow, where blood cell production (hematopoiesis) takes place.\n\n" +
                        "Yellow marrow (Incorrect)\n" +
                        "Yellow marrow stores fat, not involved in blood cell production.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone provides strength but does not house marrow.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "Periosteum is the outer covering and does not contain marrow."
        );

        questions.add("The patella is classified as which type of bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sesamoid", // Correct answer
                "Irregular",
                "Long",
                "Flat"
        )));
        correctAnswers.add("Sesamoid");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Sesamoid (Correct answer)\n" +
                        "The patella is a sesamoid bone, formed within tendons to reduce friction.\n\n" +
                        "Irregular (Incorrect)\n" +
                        "Irregular bones include vertebrae.\n\n" +
                        "Long (Incorrect)\n" +
                        "Long bones include the femur and humerus.\n\n" +
                        "Flat (Incorrect)\n" +
                        "Flat bones include the skull and sternum."
        );

        questions.add("What is the function of osteoblasts?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Build bone tissue", // Correct answer
                "Break down bone matrix",
                "Store calcium",
                "Maintain bone matrix"
        )));
        correctAnswers.add("Build bone tissue");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Build bone tissue (Correct answer)\n" +
                        "Osteoblasts are bone-forming cells that synthesize and secrete bone matrix.\n\n" +
                        "Break down bone matrix (Incorrect)\n" +
                        "Osteoclasts break down bone.\n\n" +
                        "Store calcium (Incorrect)\n" +
                        "Calcium is stored in bone, but osteoblasts do not directly store it.\n\n" +
                        "Maintain bone matrix (Incorrect)\n" +
                        "Osteocytes maintain the bone matrix."
        );

        questions.add("Which of the following joints is classified as a diarthrosis?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Shoulder joint", // Correct answer
                "Skull suture",
                "Intervertebral disc",
                "Pubic symphysis"
        )));
        correctAnswers.add("Shoulder joint");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Shoulder joint (Correct answer)\n" +
                        "Diarthroses are freely movable joints, like the shoulder.\n\n" +
                        "Skull suture (Incorrect)\n" +
                        "Skull sutures are synarthroses (immovable).\n\n" +
                        "Intervertebral disc (Incorrect)\n" +
                        "Intervertebral discs are amphiarthroses (slightly movable).\n\n" +
                        "Pubic symphysis (Incorrect)\n" +
                        "The pubic symphysis is also an amphiarthrosis."
        );

        questions.add("Which of the following is NOT a function of the skeletal system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulation of body temperature", // Correct answer
                "Protection of organs",
                "Support of body structures",
                "Mineral storage"
        )));
        correctAnswers.add("Regulation of body temperature");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Regulation of body temperature (Correct answer)\n" +
                        "Regulating body temperature is a function of the integumentary system, not skeletal.\n\n" +
                        "Protection of organs (Incorrect)\n" +
                        "The skeleton protects organs (e.g., skull protects the brain).\n\n" +
                        "Support of body structures (Incorrect)\n" +
                        "The skeleton provides structural support.\n\n" +
                        "Mineral storage (Incorrect)\n" +
                        "Bones store minerals like calcium and phosphorus."
        );

        questions.add("What type of bone tissue contains red marrow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Spongy bone", // Correct answer
                "Compact bone",
                "Diaphysis",
                "Yellow bone marrow"
        )));
        correctAnswers.add("Spongy bone");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Spongy bone (Correct answer)\n" +
                        "Red marrow found in spongy bone is responsible for blood cell production.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone is dense and found on the outer surface.\n\n" +
                        "Diaphysis (Incorrect)\n" +
                        "The diaphysis typically houses yellow marrow.\n\n" +
                        "Yellow bone marrow (Incorrect)\n" +
                        "Yellow marrow stores fat, not involved in hematopoiesis."
        );

        questions.add("What term describes a hole through a bone for passage of nerves or blood vessels?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Foramen", // Correct answer
                "Tuberosity",
                "Fossa",
                "Condyle"
        )));
        correctAnswers.add("Foramen");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Foramen (Correct answer)\n" +
                        "A foramen is an opening that allows passage of structures like nerves or vessels (e.g., foramen magnum).\n\n" +
                        "Tuberosity (Incorrect)\n" +
                        "A tuberosity is a rough projection for muscle attachment.\n\n" +
                        "Fossa (Incorrect)\n" +
                        "A fossa is a shallow depression.\n\n" +
                        "Condyle (Incorrect)\n" +
                        "A condyle is a rounded articular surface."
        );

        questions.add("Which of the following bones is part of the appendicular skeleton?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Scapula", // Correct answer
                "Rib",
                "Vertebra",
                "Skull"
        )));
        correctAnswers.add("Scapula");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Scapula (Correct answer)\n" +
                        "The scapula (shoulder blade) is part of the appendicular skeleton.\n\n" +
                        "Rib (Incorrect)\n" +
                        "Ribs are part of the axial skeleton.\n\n" +
                        "Vertebra (Incorrect)\n" +
                        "Vertebrae form the vertebral column, which is axial.\n\n" +
                        "Skull (Incorrect)\n" +
                        "The skull is part of the axial skeleton."
        );

        questions.add("The shaft of a long bone is called the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphysis", // Correct answer
                "Epiphysis",
                "Periosteum",
                "Metaphysis"
        )));
        correctAnswers.add("Diaphysis");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Diaphysis (Correct answer)\n" +
                        "The diaphysis is the long, cylindrical shaft of a long bone.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "The epiphysis is the end part of a long bone.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "The periosteum is the membrane covering the bone.\n\n" +
                        "Metaphysis (Incorrect)\n" +
                        "The metaphysis is the growth region between diaphysis and epiphysis."
        );

        questions.add("What is the name of the stage in bone healing where a temporary cartilaginous bridge is formed?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fibrocartilaginous callus", // Correct answer
                "Hematoma formation",
                "Bony callus",
                "Bone remodeling"
        )));
        correctAnswers.add("Fibrocartilaginous callus");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Fibrocartilaginous callus (Correct answer)\n" +
                        "This stage forms a soft callus of collagen and cartilage that bridges the fracture.\n\n" +
                        "Hematoma formation (Incorrect)\n" +
                        "Hematoma formation is the first stage involving blood clot.\n\n" +
                        "Bony callus (Incorrect)\n" +
                        "Bony callus is the stage after cartilage, forming hard bone.\n\n" +
                        "Bone remodeling (Incorrect)\n" +
                        "Bone remodeling is the final stage."
        );

        questions.add("The end part of a long bone is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Epiphysis", // Correct answer
                "Diaphysis",
                "Metaphysis",
                "Medullary cavity"
        )));
        correctAnswers.add("Epiphysis");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Epiphysis (Correct answer)\n" +
                        "The epiphysis is the expanded end of a long bone, often housing spongy bone.\n\n" +
                        "Diaphysis (Incorrect)\n" +
                        "Diaphysis is the shaft.\n\n" +
                        "Metaphysis (Incorrect)\n" +
                        "Metaphysis lies between epiphysis and diaphysis.\n\n" +
                        "Medullary cavity (Incorrect)\n" +
                        "The medullary cavity is inside the diaphysis."
        );

        questions.add("What is the role of the endosteum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lines the internal bone cavity", // Correct answer
                "Stores fat",
                "Covers the outer surface of bone",
                "Protects joints"
        )));
        correctAnswers.add("Lines the internal bone cavity");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Lines the internal bone cavity (Correct answer)\n" +
                        "The endosteum is a thin membrane lining the medullary cavity of bones.\n\n" +
                        "Stores fat (Incorrect)\n" +
                        "Fat is stored in yellow marrow.\n\n" +
                        "Covers the outer surface of bone (Incorrect)\n" +
                        "The periosteum covers the outer surface.\n\n" +
                        "Protects joints (Incorrect)\n" +
                        "Joints are protected by cartilage and ligaments."
        );

        questions.add("Which condition is caused by vitamin D deficiency in children?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rickets", // Correct answer
                "Osteoporosis",
                "Scoliosis",
                "Arthritis"
        )));
        correctAnswers.add("Rickets");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Rickets (Correct answer)\n" +
                        "Rickets results from vitamin D deficiency, leading to soft, weak bones in children.\n\n" +
                        "Osteoporosis (Incorrect)\n" +
                        "Osteoporosis is a decrease in bone density, common in adults.\n\n" +
                        "Scoliosis (Incorrect)\n" +
                        "Scoliosis is an abnormal lateral curvature of the spine.\n\n" +
                        "Arthritis (Incorrect)\n" +
                        "Arthritis is joint inflammation."
        );

        questions.add("A rounded articular projection on a bone is called a:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Condyle", // Correct answer
                "Tuberosity",
                "Fossa",
                "Foramen"
        )));
        correctAnswers.add("Condyle");
        rationales.put(45,
                "RATIONALE:\n" +
                        "Condyle (Correct answer)\n" +
                        "A condyle is a rounded projection that forms part of a joint.\n\n" +
                        "Tuberosity (Incorrect)\n" +
                        "A tuberosity is a rough projection for muscle attachment.\n\n" +
                        "Fossa (Incorrect)\n" +
                        "A fossa is a shallow depression.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is a hole."
        );

        questions.add("The medullary cavity of a long bone contains:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Yellow marrow", // Correct answer
                "Red marrow",
                "Compact bone",
                "Spongy bone"
        )));
        correctAnswers.add("Yellow marrow");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Yellow marrow (Correct answer)\n" +
                        "The medullary cavity holds yellow marrow, primarily for fat storage in adults.\n\n" +
                        "Red marrow (Incorrect)\n" +
                        "Red marrow is found in spongy bone.\n\n" +
                        "Compact bone (Incorrect)\n" +
                        "Compact bone surrounds the cavity.\n\n" +
                        "Spongy bone (Incorrect)\n" +
                        "Spongy bone is located in the epiphyses."
        );

        questions.add("What is the primary mineral stored in bones?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Calcium", // Correct answer
                "Sodium",
                "Potassium",
                "Iron"
        )));
        correctAnswers.add("Calcium");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Calcium (Correct answer)\n" +
                        "Bones store calcium, which is essential for muscle contraction and nerve function.\n\n" +
                        "Sodium (Incorrect)\n" +
                        "Sodium is important for fluid balance but not stored in bone.\n\n" +
                        "Potassium (Incorrect)\n" +
                        "Potassium is found in cells, not stored in bone.\n\n" +
                        "Iron (Incorrect)\n" +
                        "Iron is stored in the liver and used in hemoglobin, not primarily in bone."
        );

        questions.add("Which bone marking is used for muscle attachment?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tuberosity", // Correct answer
                "Fossa",
                "Foramen",
                "Condyle"
        )));
        correctAnswers.add("Tuberosity");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Tuberosity (Correct answer)\n" +
                        "A tuberosity is a large, rough projection that serves as a site for muscle attachment.\n\n" +
                        "Fossa (Incorrect)\n" +
                        "A fossa is a shallow depression.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is an opening.\n\n" +
                        "Condyle (Incorrect)\n" +
                        "A condyle is a rounded joint surface."
        );

        questions.add("Which condition is characterized by a decrease in bone density?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteoporosis", // Correct answer
                "Arthritis",
                "Scoliosis",
                "Rickets"
        )));
        correctAnswers.add("Osteoporosis");
        rationales.put(49,
                "RATIONALE:\n" +
                        "Osteoporosis (Correct answer)\n" +
                        "Osteoporosis involves decreased bone mass, leading to fragility and fractures.\n\n" +
                        "Arthritis (Incorrect)\n" +
                        "Arthritis is joint inflammation.\n\n" +
                        "Scoliosis (Incorrect)\n" +
                        "Scoliosis is spinal curvature.\n\n" +
                        "Rickets (Incorrect)\n" +
                        "Rickets is due to vitamin D deficiency."
        );

        questions.add("What is the primary precursor molecule for vitamin D synthesis in the skin?");
        choices.add(new ArrayList<>(Arrays.asList(
                "7-dehydrocholesterol", // Correct answer
                "Tyrosine",
                "Tryptophan",
                "Cholesterol sulfate"
        )));

        correctAnswers.add("Hyaline cartilage");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Hyaline cartilage (Correct answer)\n" +
                        "Hyaline cartilage, also called articular cartilage, reduces friction and absorbs shock at joint surfaces.\n\n" +
                        "Elastic cartilage (Incorrect)\n" +
                        "Elastic cartilage is found in the ear and epiglottis.\n\n" +
                        "Fibrocartilage (Incorrect)\n" +
                        "Fibrocartilage is found in intervertebral discs.\n\n" +
                        "Articular disc (Incorrect)\n" +
                        "An articular disc is a pad of fibrocartilage, not a covering."
        );

        questions.add("Which bone contains the foramen magnum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Occipital", // Correct answer
                "Temporal",
                "Frontal",
                "Parietal"
        )));
        correctAnswers.add("Occipital");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Occipital (Correct answer)\n" +
                        "The foramen magnum is located in the occipital bone, allowing the spinal cord to connect with the brain.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "The temporal bone contains the ear canal.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "The frontal bone forms the forehead.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "The parietal bones form the sides of the cranium."
        );

        questions.add("Which of the following bones is part of the pelvic girdle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ilium", // Correct answer
                "Femur",
                "Coccyx",
                "Sacrum"
        )));
        correctAnswers.add("Ilium");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Ilium (Correct answer)\n" +
                        "The ilium is one of the three bones that form the pelvic girdle.\n\n" +
                        "Femur (Incorrect)\n" +
                        "The femur is part of the lower limb.\n\n" +
                        "Coccyx (Incorrect)\n" +
                        "The coccyx is part of the axial skeleton.\n\n" +
                        "Sacrum (Incorrect)\n" +
                        "The sacrum is part of the vertebral column."
        );

        questions.add("Which region of the vertebral column has the greatest number of vertebrae?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thoracic", // Correct answer
                "Cervical",
                "Lumbar",
                "Sacral"
        )));
        correctAnswers.add("Thoracic");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Thoracic (Correct answer)\n" +
                        "There are 12 thoracic vertebrae – the most in any region.\n\n" +
                        "Cervical (Incorrect)\n" +
                        "Cervical has 7 vertebrae.\n\n" +
                        "Lumbar (Incorrect)\n" +
                        "Lumbar has 5 vertebrae.\n\n" +
                        "Sacral (Incorrect)\n" +
                        "The sacral region has 5 fused vertebrae."
        );

        questions.add("The femur articulates proximally with which bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pelvis", // Correct answer
                "Tibia",
                "Patella",
                "Fibula"
        )));
        correctAnswers.add("Pelvis");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Pelvis (Correct answer)\n" +
                        "The femur articulates with the pelvis at the hip joint.\n\n" +
                        "Tibia (Incorrect)\n" +
                        "The tibia articulates with the femur distally.\n\n" +
                        "Patella (Incorrect)\n" +
                        "The patella is a sesamoid bone that glides over the femur.\n\n" +
                        "Fibula (Incorrect)\n" +
                        "The fibula does not articulate directly with the femur."
        );

        questions.add("The bone cells that become trapped in lacunae and maintain the matrix are:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Osteocytes", // Correct answer
                "Osteoblasts",
                "Osteoclasts",
                "Chondrocytes"
        )));
        correctAnswers.add("Osteocytes");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Osteocytes (Correct answer)\n" +
                        "Osteocytes are mature bone cells that maintain bone tissue.\n\n" +
                        "Osteoblasts (Incorrect)\n" +
                        "Osteoblasts build new bone.\n\n" +
                        "Osteoclasts (Incorrect)\n" +
                        "Osteoclasts break down bone.\n\n" +
                        "Chondrocytes (Incorrect)\n" +
                        "Chondrocytes are cartilage cells."
        );

        questions.add("What structure connects bone to bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ligament", // Correct answer
                "Tendon",
                "Cartilage",
                "Meniscus"
        )));
        correctAnswers.add("Ligament");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Ligament (Correct answer)\n" +
                        "Ligaments are strong bands of connective tissue that link bones together at joints.\n\n" +
                        "Tendon (Incorrect)\n" +
                        "Tendons connect muscles to bones.\n\n" +
                        "Cartilage (Incorrect)\n" +
                        "Cartilage cushions joints but doesn’t connect bones.\n\n" +
                        "Meniscus (Incorrect)\n" +
                        "Meniscus is cartilage that cushions and stabilizes joints."
        );

        questions.add("Which part of the sternum articulates with the clavicles?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Manubrium", // Correct answer
                "Body",
                "Xiphoid process",
                "Jugular notch"
        )));
        correctAnswers.add("Manubrium");
        rationales.put(57,
                "RATIONALE:\n" +
                        "Manubrium (Correct answer)\n" +
                        "The manubrium is the superior portion of the sternum that articulates with the clavicles.\n\n" +
                        "Body (Incorrect)\n" +
                        "The body articulates with ribs, not clavicles.\n\n" +
                        "Xiphoid process (Incorrect)\n" +
                        "The xiphoid process is the smallest and lowest part.\n\n" +
                        "Jugular notch (Incorrect)\n" +
                        "The jugular notch is a depression on the manubrium."
        );

        questions.add("What is the name of the soft spot on a baby’s skull?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Fontanelle", // Correct answer
                "Epiphysis",
                "Suture",
                "Foramen"
        )));
        correctAnswers.add("Fontanelle");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Fontanelle (Correct answer)\n" +
                        "Fontanelles are soft, membranous gaps between the cranial bones of infants.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "Epiphysis refers to the end of long bones.\n\n" +
                        "Suture (Incorrect)\n" +
                        "Sutures are immovable joints in the skull.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "Foramen is an opening or hole in a bone."
        );

        questions.add("Which bone is known as the collarbone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Clavicle", // Correct answer
                "Scapula",
                "Sternum",
                "Humerus"
        )));
        correctAnswers.add("Clavicle");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Clavicle (Correct answer)\n" +
                        "The clavicle, or collarbone, connects the arm to the body and stabilizes shoulder movement.\n\n" +
                        "Scapula (Incorrect)\n" +
                        "The scapula is the shoulder blade.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is the breastbone.\n\n" +
                        "Humerus (Incorrect)\n" +
                        "The humerus is the upper arm bone."
        );

        questions.add("Which bone articulates with both the radius and ulna?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Humerus", // Correct answer
                "Scapula",
                "Clavicle",
                "Sternum"
        )));
        correctAnswers.add("Humerus");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Humerus (Correct answer)\n" +
                        "The humerus connects with the radius and ulna at the elbow joint.\n\n" +
                        "Scapula (Incorrect)\n" +
                        "The scapula articulates with the humerus, not the forearm bones.\n\n" +
                        "Clavicle (Incorrect)\n" +
                        "The clavicle connects with the scapula and sternum.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is part of the axial skeleton."
        );

        questions.add("The cranium protects which organ?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Brain", // Correct answer
                "Heart",
                "Lungs",
                "Kidneys"
        )));
        correctAnswers.add("Brain");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Brain (Correct answer)\n" +
                        "The cranium encases and protects the brain.\n\n" +
                        "Heart (Incorrect)\n" +
                        "The heart is protected by the rib cage.\n\n" +
                        "Lungs (Incorrect)\n" +
                        "Lungs are protected by ribs and sternum.\n\n" +
                        "Kidneys (Incorrect)\n" +
                        "Kidneys are partly protected by the lower ribs."
        );

        questions.add("Which is the largest bone in the body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Femur", // Correct answer
                "Humerus",
                "Tibia",
                "Pelvis"
        )));
        correctAnswers.add("Femur");
        rationales.put(62,
                "RATIONALE:\n" +
                        "Femur (Correct answer)\n" +
                        "The femur is the longest and strongest bone in the human body.\n\n" +
                        "Humerus (Incorrect)\n" +
                        "The humerus is the upper arm bone, but smaller.\n\n" +
                        "Tibia (Incorrect)\n" +
                        "The tibia is in the lower leg.\n\n" +
                        "Pelvis (Incorrect)\n" +
                        "The pelvis is large but not a single bone."
        );

        questions.add("Which bone forms the prominence of the cheek?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Zygomatic", // Correct answer
                "Maxilla",
                "Temporal",
                "Sphenoid"
        )));
        correctAnswers.add("Zygomatic");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Zygomatic (Correct answer)\n" +
                        "The zygomatic bones are commonly called cheekbones.\n\n" +
                        "Maxilla (Incorrect)\n" +
                        "The maxilla is the upper jaw.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "The temporal bone forms part of the side of the skull.\n\n" +
                        "Sphenoid (Incorrect)\n" +
                        "The sphenoid is deep in the skull base."
        );

        questions.add("What is the function of the periosteum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cover the outer surface of bones", // Correct answer
                "Produce red blood cells",
                "Store yellow marrow",
                "Lubricate joints"
        )));
        correctAnswers.add("Cover the outer surface of bones");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Cover the outer surface of bones (Correct answer)\n" +
                        "The periosteum is a fibrous membrane that covers the bone and aids in growth and repair.\n\n" +
                        "Produce red blood cells (Incorrect)\n" +
                        "Red blood cell production occurs in red marrow.\n\n" +
                        "Store yellow marrow (Incorrect)\n" +
                        "Yellow marrow is stored in the medullary cavity.\n\n" +
                        "Lubricate joints (Incorrect)\n" +
                        "Synovial fluid lubricates joints."
        );

        questions.add("Which bone does the patella protect?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Femur", // Correct answer
                "Tibia",
                "Fibula",
                "Pelvis"
        )));
        correctAnswers.add("Femur");
        rationales.put(65,
                "RATIONALE:\n" +
                        "Femur (Correct answer)\n" +
                        "The patella, or kneecap, protects the front of the femur at the knee joint.\n\n" +
                        "Tibia (Incorrect)\n" +
                        "Tibia is the larger lower leg bone but not directly protected by the patella.\n\n" +
                        "Fibula (Incorrect)\n" +
                        "The fibula is lateral to the tibia.\n\n" +
                        "Pelvis (Incorrect)\n" +
                        "The pelvis is not associated with the patella."
        );

        questions.add("What type of joint is found at the shoulder and hip?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ball-and-socket", // Correct answer
                "Hinge",
                "Pivot",
                "Gliding"
        )));
        correctAnswers.add("Ball-and-socket");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Ball-and-socket (Correct answer)\n" +
                        "These joints allow rotational movement and a wide range of motion.\n\n" +
                        "Hinge (Incorrect)\n" +
                        "Hinge joints (e.g., elbow) move in one plane.\n\n" +
                        "Pivot (Incorrect)\n" +
                        "Pivot joints allow rotation (e.g., atlas and axis).\n\n" +
                        "Gliding (Incorrect)\n" +
                        "Gliding joints allow sliding (e.g., carpals)."
        );

        questions.add("Which bone houses the pituitary gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sphenoid", // Correct answer
                "Ethmoid",
                "Frontal",
                "Temporal"
        )));
        correctAnswers.add("Sphenoid");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Sphenoid (Correct answer)\n" +
                        "The sphenoid bone contains the sella turcica, a depression where the pituitary gland sits.\n\n" +
                        "Ethmoid (Incorrect)\n" +
                        "Ethmoid forms part of the nasal cavity.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "Frontal bone forms the forehead.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "Temporal bone houses ear structures."
        );

        questions.add("Which part of the vertebral column supports the weight of the upper body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Lumbar", // Correct answer
                "Cervical",
                "Thoracic",
                "Coccygeal"
        )));
        correctAnswers.add("Lumbar");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Lumbar (Correct answer)\n" +
                        "The lumbar vertebrae are large and support the most weight.\n\n" +
                        "Cervical (Incorrect)\n" +
                        "Cervical supports the head.\n\n" +
                        "Thoracic (Incorrect)\n" +
                        "Thoracic supports the rib cage.\n\n" +
                        "Coccygeal (Incorrect)\n" +
                        "Coccygeal is a small tailbone with minimal weight-bearing function."
        );

        questions.add("Which bone forms the lower jaw?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mandible", // Correct answer
                "Maxilla",
                "Zygomatic",
                "Palatine"
        )));
        correctAnswers.add("Mandible");
        rationales.put(69,
                "RATIONALE:\n" +
                        "Mandible (Correct answer)\n" +
                        "The mandible is the only movable bone of the skull and forms the lower jaw.\n\n" +
                        "Maxilla (Incorrect)\n" +
                        "Maxilla is the upper jaw.\n\n" +
                        "Zygomatic (Incorrect)\n" +
                        "Zygomatic is the cheekbone.\n\n" +
                        "Palatine (Incorrect)\n" +
                        "Palatine forms part of the roof of the mouth."
        );

        questions.add("Which bone is NOT part of the axial skeleton?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Femur", // Correct answer
                "Sternum",
                "Rib",
                "Skull"
        )));
        correctAnswers.add("Femur");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Femur (Correct answer)\n" +
                        "The femur is part of the appendicular skeleton, not the axial.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is part of the axial skeleton, located in the chest.\n\n" +
                        "Rib (Incorrect)\n" +
                        "Ribs are part of the axial skeleton, forming part of the thoracic cage.\n\n" +
                        "Skull (Incorrect)\n" +
                        "The skull is a key part of the axial skeleton, protecting the brain."
        );

        questions.add("What bone forms the forehead?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Frontal", // Correct answer
                "Parietal",
                "Temporal",
                "Occipital"
        )));
        correctAnswers.add("Frontal");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Frontal (Correct answer)\n" +
                        "The frontal bone forms the forehead and the roof of the eye sockets.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "The parietal bones form the sides and roof of the cranium.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "The temporal bones form the lower sides of the cranium.\n\n" +
                        "Occipital (Incorrect)\n" +
                        "The occipital bone forms the back of the skull."
        );

        questions.add("What part of a long bone contains yellow bone marrow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Diaphysis", // Correct answer
                "Epiphysis",
                "Periosteum",
                "Endosteum"
        )));
        correctAnswers.add("Diaphysis");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Diaphysis (Correct answer)\n" +
                        "The diaphysis (shaft) contains the medullary cavity filled with yellow marrow.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "The epiphysis contains red bone marrow, involved in blood cell production.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "The periosteum is a membrane covering the bone, not a marrow-containing cavity.\n\n" +
                        "Endosteum (Incorrect)\n" +
                        "The endosteum lines the medullary cavity but does not contain marrow."
        );

        questions.add("Which bone is located in the upper arm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Humerus", // Correct answer
                "Radius",
                "Ulna",
                "Clavicle"
        )));
        correctAnswers.add("Humerus");
        rationales.put(73,
                "RATIONALE:\n" +
                        "Humerus (Correct answer)\n" +
                        "The humerus is the single bone of the upper arm.\n\n" +
                        "Radius (Incorrect)\n" +
                        "The radius is a bone of the forearm, located on the lateral side.\n\n" +
                        "Ulna (Incorrect)\n" +
                        "The ulna is a forearm bone, located on the medial side.\n\n" +
                        "Clavicle (Incorrect)\n" +
                        "The clavicle is part of the shoulder girdle, not the upper arm."
        );

        questions.add("Which of the following bones does NOT contribute to the orbit?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Mandible", // Correct answer
                "Maxilla",
                "Ethmoid",
                "Zygomatic"
        )));
        correctAnswers.add("Mandible");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Mandible (Correct answer)\n" +
                        "The mandible is not part of the eye socket; it forms the lower jaw.\n\n" +
                        "Maxilla (Incorrect)\n" +
                        "The maxilla contributes to the formation of the orbit, supporting the upper jaw.\n\n" +
                        "Ethmoid (Incorrect)\n" +
                        "The ethmoid bone is part of the orbital structure, forming the medial wall of the orbit.\n\n" +
                        "Zygomatic (Incorrect)\n" +
                        "The zygomatic bone forms part of the orbital rim and cheek."
        );

        questions.add("Which hormone stimulates osteoclast activity to release calcium?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Parathyroid hormone", // Correct answer
                "Calcitonin",
                "Insulin",
                "Estrogen"
        )));
        correctAnswers.add("Parathyroid hormone");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Parathyroid hormone (Correct answer)\n" +
                        "Parathyroid hormone increases blood calcium levels by activating osteoclasts, which break down bone tissue.\n\n" +
                        "Calcitonin (Incorrect)\n" +
                        "Calcitonin lowers blood calcium levels by inhibiting osteoclast activity.\n\n" +
                        "Insulin (Incorrect)\n" +
                        "Insulin is involved in regulating glucose, not calcium.\n\n" +
                        "Estrogen (Incorrect)\n" +
                        "Estrogen affects bone density but does not directly stimulate osteoclast activity."
        );

        questions.add("What part of the vertebra bears the most weight?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Vertebral body", // Correct answer
                "Spinous process",
                "Vertebral foramen",
                "Transverse process"
        )));
        correctAnswers.add("Vertebral body");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Vertebral body (Correct answer)\n" +
                        "The vertebral body is the thick, weight-bearing portion of the vertebra that supports the weight of the body.\n\n" +
                        "Spinous process (Incorrect)\n" +
                        "The spinous process serves as a site for muscle attachment, not weight-bearing.\n\n" +
                        "Vertebral foramen (Incorrect)\n" +
                        "The vertebral foramen houses the spinal cord, but does not bear weight.\n\n" +
                        "Transverse process (Incorrect)\n" +
                        "The transverse process serves as an attachment point for muscles and ligaments, not weight-bearing."
        );

        questions.add("Which type of fracture is characterized by bone breaking into multiple pieces?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Comminuted", // Correct answer
                "Greenstick",
                "Spiral",
                "Transverse"
        )));
        correctAnswers.add("Comminuted");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Comminuted (Correct answer)\n" +
                        "A comminuted fracture occurs when the bone breaks into several pieces or fragments.\n\n" +
                        "Greenstick (Incorrect)\n" +
                        "A greenstick fracture is an incomplete fracture common in children, where the bone bends and cracks on one side.\n\n" +
                        "Spiral (Incorrect)\n" +
                        "A spiral fracture is caused by a twisting force that results in a helical break.\n\n" +
                        "Transverse (Incorrect)\n" +
                        "A transverse fracture is a straight horizontal break across the bone."
        );

        questions.add("Which bone is commonly fractured in a fall on an outstretched hand (FOOSH)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Scaphoid", // Correct answer
                "Lunate",
                "Radius",
                "Clavicle"
        )));
        correctAnswers.add("Scaphoid");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Scaphoid (Correct answer)\n" +
                        "The scaphoid is the most commonly fractured carpal bone when a person falls on an outstretched hand (FOOSH).\n\n" +
                        "Lunate (Incorrect)\n" +
                        "Lunate dislocation is more common than fracture in a FOOSH injury.\n\n" +
                        "Radius (Incorrect)\n" +
                        "The radius can also fracture in a FOOSH injury, but it is less common than a scaphoid fracture.\n\n" +
                        "Clavicle (Incorrect)\n" +
                        "Clavicle fractures can occur from a FOOSH injury, but they are not the most common."
        );

        questions.add("What type of bone is the patella?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sesamoid", // Correct answer
                "Long",
                "Irregular",
                "Short"
        )));
        correctAnswers.add("Sesamoid");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Sesamoid (Correct answer)\n" +
                        "The patella is a sesamoid bone, which is embedded within a tendon and functions to protect the tendon and increase its mechanical advantage.\n\n" +
                        "Long (Incorrect)\n" +
                        "Long bones are longer than they are wide, like the femur, but the patella is not categorized as such.\n\n" +
                        "Irregular (Incorrect)\n" +
                        "Irregular bones have complex shapes, like vertebrae, but the patella is a sesamoid bone.\n\n" +
                        "Short (Incorrect)\n" +
                        "Short bones are cube-like in shape, like the carpals and tarsals, but the patella does not fit this description."
        );

        questions.add("Which bone contains the external auditory meatus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Temporal", // Correct answer
                "Occipital",
                "Frontal",
                "Parietal"
        )));
        correctAnswers.add("Temporal");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Temporal (Correct answer)\n" +
                        "The external ear canal passes through the temporal bone.\n\n" +
                        "Occipital (Incorrect)\n" +
                        "This bone does not house the auditory canal.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "This bone does not house the auditory canal.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "This bone does not house the auditory canal."
        );

        questions.add("Which bone is also known as the shinbone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Tibia", // Correct answer
                "Fibula",
                "Femur",
                "Patella"
        )));
        correctAnswers.add("Tibia");
        rationales.put(81,
                "RATIONALE:\n" +
                        "Tibia (Correct answer)\n" +
                        "The tibia is the larger, weight-bearing bone of the lower leg.\n\n" +
                        "Fibula (Incorrect)\n" +
                        "The fibula is the thinner bone, not weight-bearing.\n\n" +
                        "Femur (Incorrect)\n" +
                        "The femur is the thigh bone.\n\n" +
                        "Patella (Incorrect)\n" +
                        "The patella is the kneecap."
        );

        questions.add("Which part of a long bone is the growth plate located in children?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Metaphysis", // Correct answer
                "Epiphysis",
                "Diaphysis",
                "Periosteum"
        )));
        correctAnswers.add("Metaphysis");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Metaphysis (Correct answer)\n" +
                        "The metaphysis includes the epiphyseal plate in growing bones.\n\n" +
                        "Epiphysis (Incorrect)\n" +
                        "The epiphysis is the end part of the bone, not where the growth plate is located.\n\n" +
                        "Diaphysis (Incorrect)\n" +
                        "The diaphysis is the shaft of the bone, not where the growth plate is located.\n\n" +
                        "Periosteum (Incorrect)\n" +
                        "The periosteum is the outer covering of the bone, not related to the growth plate."
        );

        questions.add("Which bones make up the pectoral girdle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Scapula and clavicle", // Correct answer
                "Humerus and radius",
                "Clavicle and sternum",
                "Scapula and sternum"
        )));
        correctAnswers.add("Scapula and clavicle");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Scapula and clavicle (Correct answer)\n" +
                        "These two bones form the shoulder girdle.\n\n" +
                        "Humerus and radius (Incorrect)\n" +
                        "These bones are part of the arm, not the pectoral girdle.\n\n" +
                        "Clavicle and sternum (Incorrect)\n" +
                        "These are not the correct bones for the pectoral girdle.\n\n" +
                        "Scapula and sternum (Incorrect)\n" +
                        "The scapula is part of the pectoral girdle, but not the sternum."
        );

        questions.add("Which part of the pelvis is the most inferior?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ischium", // Correct answer
                "Ilium",
                "Pubis",
                "Sacrum"
        )));
        correctAnswers.add("Ischium");
        rationales.put(84,
                "RATIONALE:\n" +
                        "Ischium (Correct answer)\n" +
                        "The ischium forms the lower and back part of the pelvis.\n\n" +
                        "Ilium (Incorrect)\n" +
                        "The ilium is the upper portion of the pelvis.\n\n" +
                        "Pubis (Incorrect)\n" +
                        "The pubis is the anterior part of the pelvis.\n\n" +
                        "Sacrum (Incorrect)\n" +
                        "The sacrum is part of the vertebral column, not the pelvic bones."
        );

        questions.add("What bone marking is a large, rough projection where muscles attach?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Trochanter", // Correct answer
                "Fossa",
                "Foramen",
                "Tubercle"
        )));
        correctAnswers.add("Trochanter");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Trochanter (Correct answer)\n" +
                        "Trochanters are large projections on the femur for muscle attachment.\n\n" +
                        "Fossa (Incorrect)\n" +
                        "A fossa is a shallow depression, not a large projection.\n\n" +
                        "Foramen (Incorrect)\n" +
                        "A foramen is a hole for vessels/nerves, not a projection.\n\n" +
                        "Tubercle (Incorrect)\n" +
                        "A tubercle is a smaller projection than a trochanter."
        );

        questions.add("What is the primary function of red bone marrow?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood cell production", // Correct answer
                "Fat storage",
                "Mineral storage",
                "Hormone production"
        )));
        correctAnswers.add("Blood cell production");
        rationales.put(86,
                "RATIONALE:\n" +
                        "Blood cell production (Correct answer)\n" +
                        "Red marrow produces RBCs, WBCs, and platelets.\n\n" +
                        "Fat storage (Incorrect)\n" +
                        "Fat storage occurs in yellow marrow, not red marrow.\n\n" +
                        "Mineral storage (Incorrect)\n" +
                        "Mineral storage occurs in bone, not specifically in red marrow.\n\n" +
                        "Hormone production (Incorrect)\n" +
                        "Hormones are not produced by red bone marrow."
        );

        questions.add("The canal that runs through the core of each osteon is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Central (Haversian) canal", // Correct answer
                "Volkmann’s canal",
                "Medullary canal",
                "Lamella"
        )));
        correctAnswers.add("Central (Haversian) canal");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Central (Haversian) canal (Correct answer)\n" +
                        "This canal contains blood vessels and nerves.\n\n" +
                        "Volkmann’s canal (Incorrect)\n" +
                        "These canals are perpendicular to the Haversian canals.\n\n" +
                        "Medullary canal (Incorrect)\n" +
                        "The medullary canal is in the center of the bone shaft.\n\n" +
                        "Lamella (Incorrect)\n" +
                        "Lamellae are concentric rings of bone matrix."
        );

        questions.add("The function of osteoblasts is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Form new bone", // Correct answer
                "Destroy bone",
                "Maintain bone",
                "Resorb calcium"
        )));
        correctAnswers.add("Form new bone");
        rationales.put(88,
                "RATIONALE:\n" +
                        "Form new bone (Correct answer)\n" +
                        "Osteoblasts secrete bone matrix and promote calcification.\n\n" +
                        "Destroy bone (Incorrect)\n" +
                        "Osteoclasts are responsible for destroying bone.\n\n" +
                        "Maintain bone (Incorrect)\n" +
                        "Osteocytes maintain bone but do not form it.\n\n" +
                        "Resorb calcium (Incorrect)\n" +
                        "Osteoclasts resorb calcium from bone."
        );

        questions.add("Which joint allows for the greatest range of motion?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ball-and-socket", // Correct answer
                "Hinge",
                "Pivot",
                "Saddle"
        )));
        correctAnswers.add("Ball-and-socket");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Ball-and-socket (Correct answer)\n" +
                        "Allows movement in multiple axes (shoulder, hip).\n\n" +
                        "Hinge (Incorrect)\n" +
                        "Hinge joints allow movement in one plane only.\n\n" +
                        "Pivot (Incorrect)\n" +
                        "Pivot joints allow rotation around a single axis.\n\n" +
                        "Saddle (Incorrect)\n" +
                        "Saddle joints allow more movement than hinge, but less than ball-and-socket."
        );

        questions.add("What structure cushions bones at joints?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cartilage", // Correct answer
                "Ligament",
                "Synovial fluid",
                "Bursa"
        )));
        correctAnswers.add("Cartilage");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Cartilage (Correct answer)\n" +
                        "Articular cartilage reduces friction and absorbs shock.\n\n" +
                        "Ligament (Incorrect)\n" +
                        "Ligaments stabilize joints but do not cushion them.\n\n" +
                        "Synovial fluid (Incorrect)\n" +
                        "Synovial fluid lubricates joints but does not cushion bones directly.\n\n" +
                        "Bursa (Incorrect)\n" +
                        "Bursa cushions tendons and muscles around joints, not bones."
        );

        questions.add("The zygomatic arch is formed by the zygomatic bone and which other bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Temporal", // Correct answer
                "Maxilla",
                "Frontal",
                "Parietal"
        )));
        correctAnswers.add("Temporal");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Temporal (Correct answer)\n" +
                        "The temporal process of the zygomatic bone and the zygomatic process of the temporal bone form the arch.\n\n" +
                        "Maxilla (Incorrect)\n" +
                        "The maxilla does not contribute to the zygomatic arch.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "The frontal bone is not involved in the zygomatic arch.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "The parietal bone is not involved in the zygomatic arch."
        );

        questions.add("Which bone is lateral in the forearm?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Radius", // Correct answer
                "Ulna",
                "Humerus",
                "Scapula"
        )));
        correctAnswers.add("Radius");
        rationales.put(92,
                "RATIONALE:\n" +
                        "Radius (Correct answer)\n" +
                        "The radius is on the thumb side and is lateral in anatomical position.\n\n" +
                        "Ulna (Incorrect)\n" +
                        "The ulna is medial in anatomical position.\n\n" +
                        "Humerus (Incorrect)\n" +
                        "The humerus is the upper arm bone, not part of the forearm.\n\n" +
                        "Scapula (Incorrect)\n" +
                        "The scapula is a shoulder blade, not a forearm bone."
        );

        questions.add("Which part of the skeleton includes the limbs?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Appendicular", // Correct answer
                "Axial",
                "Central",
                "Peripheral"
        )));
        correctAnswers.add("Appendicular");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Appendicular (Correct answer)\n" +
                        "The appendicular skeleton includes the limbs and girdles.\n\n" +
                        "Axial (Incorrect)\n" +
                        "The axial skeleton includes the skull, spine, and ribs.\n\n" +
                        "Central (Incorrect)\n" +
                        "Central is not a standard anatomical term for parts of the skeleton.\n\n" +
                        "Peripheral (Incorrect)\n" +
                        "Peripheral is not a standard anatomical term for parts of the skeleton."
        );

        questions.add("Which bone houses the olfactory foramina?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Ethmoid", // Correct answer
                "Sphenoid",
                "Frontal",
                "Maxilla"
        )));
        correctAnswers.add("Ethmoid");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Ethmoid (Correct answer)\n" +
                        "The cribriform plate of the ethmoid bone contains olfactory foramina for the olfactory nerves.\n\n" +
                        "Sphenoid (Incorrect)\n" +
                        "The sphenoid bone does not contain olfactory foramina.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "The frontal bone does not contain olfactory foramina.\n\n" +
                        "Maxilla (Incorrect)\n" +
                        "The maxilla does not contain olfactory foramina."
        );

        questions.add("What is the anatomical term for the shoulder blade?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Scapula", // Correct answer
                "Clavicle",
                "Humerus",
                "Sternum"
        )));
        correctAnswers.add("Scapula");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Scapula (Correct answer)\n" +
                        "The scapula is the flat, triangular bone that forms the back part of the shoulder.\n\n" +
                        "Clavicle (Incorrect)\n" +
                        "The clavicle is the collarbone, not the shoulder blade.\n\n" +
                        "Humerus (Incorrect)\n" +
                        "The humerus is the upper arm bone, not the shoulder blade.\n\n" +
                        "Sternum (Incorrect)\n" +
                        "The sternum is the chest bone, not the shoulder blade."
        );

        questions.add("The acetabulum is the socket for which joint?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hip", // Correct answer
                "Knee",
                "Elbow",
                "Shoulder"
        )));
        correctAnswers.add("Hip");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Hip (Correct answer)\n" +
                        "The acetabulum is the pelvic socket that receives the head of the femur, forming the hip joint.\n\n" +
                        "Knee (Incorrect)\n" +
                        "The knee does not involve the acetabulum.\n\n" +
                        "Elbow (Incorrect)\n" +
                        "The elbow joint does not involve the acetabulum.\n\n" +
                        "Shoulder (Incorrect)\n" +
                        "The shoulder joint involves the glenoid cavity, not the acetabulum."
        );

        questions.add("Which of the following is a function of the skeletal system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood cell formation", // Correct answer
                "Hormone production",
                "Oxygen exchange",
                "Vitamin synthesis"
        )));
        correctAnswers.add("Blood cell formation");
        rationales.put(97,
                "RATIONALE:\n" +
                        "Blood cell formation (Correct answer)\n" +
                        "Hematopoiesis occurs in the red bone marrow, a major function of the skeletal system.\n\n" +
                        "Hormone production (Incorrect)\n" +
                        "Hormone production is not a primary function of the skeletal system.\n\n" +
                        "Oxygen exchange (Incorrect)\n" +
                        "Oxygen exchange is a function of the respiratory system, not the skeletal system.\n\n" +
                        "Vitamin synthesis (Incorrect)\n" +
                        "Vitamin synthesis is not a primary function of the skeletal system."
        );

        questions.add("Which joint is an example of a hinge joint?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Knee", // Correct answer
                "Shoulder",
                "Hip",
                "Wrist"
        )));
        correctAnswers.add("Knee");
        rationales.put(98,
                "RATIONALE:\n" +
                        "Knee (Correct answer)\n" +
                        "The knee joint is a hinge joint, allowing only flexion and extension.\n\n" +
                        "Shoulder (Incorrect)\n" +
                        "The shoulder is a ball-and-socket joint, not a hinge joint.\n\n" +
                        "Hip (Incorrect)\n" +
                        "The hip is a ball-and-socket joint, not a hinge joint.\n\n" +
                        "Wrist (Incorrect)\n" +
                        "The wrist is a condyloid joint, not a hinge joint."
        );

        questions.add("Which mineral is most abundant in bone?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Calcium", // Correct answer
                "Sodium",
                "Potassium",
                "Iron"
        )));
        correctAnswers.add("Calcium");
        rationales.put(99,
                "RATIONALE:\n" +
                        "Calcium (Correct answer)\n" +
                        "Calcium phosphate is the major mineral in bones, providing strength and hardness.\n\n" +
                        "Sodium (Incorrect)\n" +
                        "Sodium is present in smaller amounts in bone.\n\n" +
                        "Potassium (Incorrect)\n" +
                        "Potassium is found in bones in trace amounts, not abundantly.\n\n" +
                        "Iron (Incorrect)\n" +
                        "Iron is found in bone marrow, not in large amounts in bone itself."
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
        new AlertDialog.Builder(Prac_3_Skeletal.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
