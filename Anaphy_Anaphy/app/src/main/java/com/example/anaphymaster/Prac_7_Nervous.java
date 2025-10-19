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

public class Prac_7_Nervous extends AppCompatActivity {

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

        setContentView(R.layout.prac_7_nervous);

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
                Toast.makeText(Prac_7_Nervous.this, "This feature is available after submitting an answer.", Toast.LENGTH_LONG).show();
            }
        });

        restartIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_7_Nervous.this)
                    .setTitle("Restart Quiz")
                    .setMessage("Are you sure you want to restart? All progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> resetQuiz())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        exitIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(Prac_7_Nervous.this)
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
                new AlertDialog.Builder(Prac_7_Nervous.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_7_Nervous.this, Answer_Result.class);
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

                new AlertDialog.Builder(Prac_7_Nervous.this)
                        .setTitle("Quiz Finished")
                        .setMessage("You have completed the quiz. Your results will be shown shortly.")
                        .setCancelable(false) // Prevents dismissal by back button or outside touch
                        .setPositiveButton("Next", (dialog, which) -> {
                            Intent intent = new Intent(Prac_7_Nervous.this, Answer_Result.class);
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

        questions.add("Which part of the neuron is responsible for receiving signals from other neurons?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Axon",
                "Dendrite", // Correct answer
                "Synaptic knob",
                "Myelin sheath"
        )));
        correctAnswers.add("Dendrite");
        rationales.put(0,
                "RATIONALE:\n" +
                        "Dendrite (Correct answer)\n" +
                        "Dendrites receive incoming signals from other neurons and transmit them to the cell body.\n\n" +
                        "Axon (Incorrect)\n" +
                        "Axons transmit signals away from the cell body, not towards it.\n\n" +
                        "Synaptic knob (Incorrect)\n" +
                        "This is the terminal end of the axon that releases neurotransmitters, not the signal receiver.\n\n" +
                        "Myelin sheath (Incorrect)\n" +
                        "This insulates the axon and speeds up impulse transmission but doesn't receive signals."
        );

        questions.add("The gaps between myelinated sections of an axon are known as:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Axon terminals",
                "Synaptic clefts",
                "Nodes of Ranvier", // Correct answer
                "Neurofibrils"
        )));
        correctAnswers.add("Nodes of Ranvier");
        rationales.put(1,
                "RATIONALE:\n" +
                        "Nodes of Ranvier (Correct answer)\n" +
                        "These gaps between myelinated segments facilitate saltatory conduction, speeding up nerve impulses.\n\n" +
                        "Axon terminals (Incorrect)\n" +
                        "These are the ends of axons where neurotransmitters are released, not gaps in myelin.\n\n" +
                        "Synaptic clefts (Incorrect)\n" +
                        "This is the space between neurons at a synapse, not between myelin sections.\n\n" +
                        "Neurofibrils (Incorrect)\n" +
                        "These are cytoskeletal components in neurons, not related to the myelin sheath."
        );

        questions.add("Which of the following neurotransmitters is primarily involved in muscle contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Serotonin",
                "Dopamine",
                "Acetylcholine", // Correct answer
                "GABA"
        )));
        correctAnswers.add("Acetylcholine");
        rationales.put(2,
                "RATIONALE:\n" +
                        "Acetylcholine (Correct answer)\n" +
                        "This is the neurotransmitter released at neuromuscular junctions to initiate muscle contraction.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Involved in mood regulation, not primarily in muscle contraction.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Important in reward and motor control, but not the key neurotransmitter for muscle contraction.\n\n" +
                        "GABA (Incorrect)\n" +
                        "This is an inhibitory neurotransmitter, not involved in muscle stimulation."
        );

        questions.add("The primary function of the cerebellum is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Control emotions",
                "Coordinate voluntary movements", // Correct answer
                "Regulate breathing",
                "Interpret sensory information"
        )));
        correctAnswers.add("Coordinate voluntary movements");
        rationales.put(3,
                "RATIONALE:\n" +
                        "Coordinate voluntary movements (Correct answer)\n" +
                        "The cerebellum ensures smooth and balanced muscular activity.\n\n" +
                        "Control emotions (Incorrect)\n" +
                        "This is primarily a function of the limbic system.\n\n" +
                        "Regulate breathing (Incorrect)\n" +
                        "This is controlled by the brainstem, especially the medulla oblongata.\n\n" +
                        "Interpret sensory information (Incorrect)\n" +
                        "This is a function of the parietal lobe, not the cerebellum."
        );

        questions.add("Which of the following is part of the peripheral nervous system (PNS)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Brain",
                "Spinal cord",
                "Cranial nerves", // Correct answer
                "Cerebellum"
        )));
        correctAnswers.add("Cranial nerves");
        rationales.put(4,
                "RATIONALE:\n" +
                        "Cranial nerves (Correct answer)\n" +
                        "These are part of the PNS and connect the brain to the head and neck.\n\n" +
                        "Brain (Incorrect)\n" +
                        "The brain is part of the central nervous system (CNS).\n\n" +
                        "Spinal cord (Incorrect)\n" +
                        "Also part of the CNS.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "This is a region of the brain and thus part of the CNS."
        );

        questions.add("Which lobe of the brain is responsible for processing visual information?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Temporal",
                "Frontal",
                "Parietal",
                "Occipital" // Correct answer
        )));
        correctAnswers.add("Occipital");
        rationales.put(5,
                "RATIONALE:\n" +
                        "Occipital (Correct answer)\n" +
                        "The occipital lobe is responsible for interpreting visual stimuli and information.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "This processes auditory information and memory.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "Responsible for decision-making, motor control, and personality.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "Processes sensory information such as touch and pressure."
        );

        questions.add("Which part of the brain regulates vital functions such as heart rate and respiration?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cerebellum",
                "Medulla oblongata", // Correct answer
                "Hypothalamus",
                "Thalamus"
        )));
        correctAnswers.add("Medulla oblongata");
        rationales.put(6,
                "RATIONALE:\n" +
                        "Medulla oblongata (Correct answer)\n" +
                        "Controls autonomic functions like heartbeat and breathing.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "Coordinates movement and balance but does not control vital signs.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "Regulates endocrine and autonomic responses, but not directly the heart rate.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "Relays sensory and motor signals but not autonomic functions."
        );

        questions.add("Which glial cell forms the myelin sheath in the central nervous system (CNS)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Schwann cell",
                "Microglia",
                "Oligodendrocyte", // Correct answer
                "Astrocyte"
        )));
        correctAnswers.add("Oligodendrocyte");
        rationales.put(7,
                "RATIONALE:\n" +
                        "Oligodendrocyte (Correct answer)\n" +
                        "These glial cells create the myelin sheath around CNS neurons.\n\n" +
                        "Schwann cell (Incorrect)\n" +
                        "Forms myelin in the PNS, not CNS.\n\n" +
                        "Microglia (Incorrect)\n" +
                        "These are the immune cells of the CNS.\n\n" +
                        "Astrocyte (Incorrect)\n" +
                        "Maintains the blood-brain barrier, not myelin formation."
        );

        questions.add("The sympathetic nervous system is responsible for which response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rest and digest",
                "Fight or flight", // Correct answer
                "Memory storage",
                "Voluntary movement"
        )));
        correctAnswers.add("Fight or flight");
        rationales.put(8,
                "RATIONALE:\n" +
                        "Fight or flight (Correct answer)\n" +
                        "The sympathetic nervous system prepares the body for stressful situations.\n\n" +
                        "Rest and digest (Incorrect)\n" +
                        "This is the role of the parasympathetic nervous system.\n\n" +
                        "Memory storage (Incorrect)\n" +
                        "This is managed by the hippocampus and temporal lobes.\n\n" +
                        "Voluntary movement (Incorrect)\n" +
                        "This is a function of the somatic nervous system."
        );

        questions.add("What ion is primarily responsible for the depolarization phase of an action potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Potassium (K⁺)",
                "Sodium (Na⁺)", // Correct answer
                "Calcium (Ca²⁺)",
                "Chloride (Cl⁻)"
        )));
        correctAnswers.add("Sodium (Na⁺)");
        rationales.put(9,
                "RATIONALE:\n" +
                        "Sodium (Na⁺) (Correct answer)\n" +
                        "During depolarization, Na⁺ rushes into the neuron, making the inside more positive.\n\n" +
                        "Potassium (K⁺) (Incorrect)\n" +
                        "Potassium is involved in repolarization, not depolarization.\n\n" +
                        "Calcium (Ca²⁺) (Incorrect)\n" +
                        "Important for neurotransmitter release, not directly for depolarization.\n\n" +
                        "Chloride (Cl⁻) (Incorrect)\n" +
                        "Generally contributes to inhibitory postsynaptic potentials."
        );

        questions.add("Which part of the brain acts as a relay station for sensory impulses traveling to the cerebral cortex?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thalamus", // Correct answer
                "Medulla oblongata",
                "Pons",
                "Hypothalamus"
        )));
        correctAnswers.add("Thalamus");
        rationales.put(10,
                "RATIONALE:\n" +
                        "Thalamus (Correct answer)\n" +
                        "The thalamus processes and relays most sensory information to the cerebral cortex.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "Controls autonomic functions but not sensory relay.\n\n" +
                        "Pons (Incorrect)\n" +
                        "Helps with breathing and serves as a bridge between different brain regions, not the primary relay.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "Regulates body temperature, hunger, and hormones, not primarily a sensory relay."
        );

        questions.add("Which of the following best describes the function of the blood-brain barrier?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Protects brain tissue from harmful substances", // Correct answer
                "Allows all substances to pass into the brain",
                "Blocks oxygen from entering the brain",
                "Prevents hormones from entering the bloodstream"
        )));
        correctAnswers.add("Protects brain tissue from harmful substances");
        rationales.put(11,
                "RATIONALE:\n" +
                        "Protects brain tissue from harmful substances (Correct answer)\n" +
                        "The blood-brain barrier selectively filters substances, keeping toxins out.\n\n" +
                        "Allows all substances to pass into the brain (Incorrect)\n" +
                        "The barrier is selective, not permissive.\n\n" +
                        "Blocks oxygen from entering the brain (Incorrect)\n" +
                        "Oxygen passes freely; the brain needs it constantly.\n\n" +
                        "Prevents hormones from entering the bloodstream (Incorrect)\n" +
                        "Hormones can travel in the bloodstream; this is not the BBB’s role."
        );

        questions.add("What type of neuron carries impulses away from the CNS to muscles or glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Motor neuron", // Correct answer
                "Sensory neuron",
                "Interneuron",
                "Bipolar neuron"
        )));
        correctAnswers.add("Motor neuron");
        rationales.put(12,
                "RATIONALE:\n" +
                        "Motor neuron (Correct answer)\n" +
                        "Motor neurons transmit impulses from the CNS to effectors like muscles.\n\n" +
                        "Sensory neuron (Incorrect)\n" +
                        "These carry impulses to the CNS, not away.\n\n" +
                        "Interneuron (Incorrect)\n" +
                        "These connect neurons within the CNS, not to muscles.\n\n" +
                        "Bipolar neuron (Incorrect)\n" +
                        "A structural classification, not a functional one."
        );

        questions.add("Which condition is characterized by progressive degeneration of motor neurons, leading to muscle weakness?");
        choices.add(new ArrayList<>(Arrays.asList(
                "ALS (Amyotrophic Lateral Sclerosis", // Correct answer
                "Parkinson’s disease",
                "Multiple sclerosis",
                "Huntington’s disease"
        )));
        correctAnswers.add("ALS (Amyotrophic Lateral Sclerosis");
        rationales.put(13,
                "RATIONALE:\n" +
                        "ALS (Amyotrophic Lateral Sclerosis (Correct answer)\n" +
                        "ALS destroys motor neurons, leading to paralysis and muscle atrophy.\n\n" +
                        "Parkinson’s disease (Incorrect)\n" +
                        "Involves dopamine deficiency and tremors, not motor neuron degeneration.\n\n" +
                        "Multiple sclerosis (Incorrect)\n" +
                        "Involves demyelination, not direct motor neuron death.\n\n" +
                        "Huntington’s disease (Incorrect)\n" +
                        "A genetic disorder causing involuntary movements and cognitive decline."
        );

        questions.add("What is the main role of cerebrospinal fluid (CSF)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cushion and protect the brain and spinal cord", // Correct answer
                "Transmit nerve impulses",
                "Act as a hormone transporter",
                "Provide insulation for neurons"
        )));
        correctAnswers.add("Cushion and protect the brain and spinal cord");
        rationales.put(14,
                "RATIONALE:\n" +
                        "Cushion and protect the brain and spinal cord (Correct answer)\n" +
                        "CSF acts as a shock absorber and removes waste.\n\n" +
                        "Transmit nerve impulses (Incorrect)\n" +
                        "This is the role of neurons, not CSF.\n\n" +
                        "Act as a hormone transporter (Incorrect)\n" +
                        "CSF is not the primary medium for hormone transport.\n\n" +
                        "Provide insulation for neurons (Incorrect)\n" +
                        "Myelin provides insulation, not CSF."
        );

        questions.add("Which nerve is the longest and extends from the lower back down to the foot?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sciatic nerve", // Correct answer
                "Vagus nerve",
                "Femoral nerve",
                "Radial nerve"
        )));
        correctAnswers.add("Sciatic nerve");
        rationales.put(15,
                "RATIONALE:\n" +
                        "Sciatic nerve (Correct answer)\n" +
                        "It is the longest and largest nerve, extending from the lumbar spine to the foot.\n\n" +
                        "Vagus nerve (Incorrect)\n" +
                        "Though long, it mainly innervates thoracic and abdominal organs.\n\n" +
                        "Femoral nerve (Incorrect)\n" +
                        "Supplies the anterior thigh, but is not the longest.\n\n" +
                        "Radial nerve (Incorrect)\n" +
                        "Supplies the arm, not extending to the lower body."
        );

        questions.add("Which division of the autonomic nervous system conserves energy and slows down the heart rate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Parasympathetic", // Correct answer
                "Sympathetic",
                "Somatic",
                "Central"
        )));
        correctAnswers.add("Parasympathetic");
        rationales.put(16,
                "RATIONALE:\n" +
                        "Parasympathetic (Correct answer)\n" +
                        "Responsible for \"rest and digest\", slowing the heart and conserving energy.\n\n" +
                        "Sympathetic (Incorrect)\n" +
                        "Prepares for \"fight or flight\", increasing heart rate and energy use.\n\n" +
                        "Somatic (Incorrect)\n" +
                        "Controls voluntary muscle movement, not involuntary functions.\n\n" +
                        "Central (Incorrect)\n" +
                        "The CNS includes the brain and spinal cord, not a division of autonomic control."
        );

        questions.add("Which part of the neuron contains the nucleus and metabolic machinery?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cell body", // Correct answer
                "Axon",
                "Dendrite",
                "Node of Ranvier"
        )));
        correctAnswers.add("Cell body");
        rationales.put(17,
                "RATIONALE:\n" +
                        "Cell body (Correct answer)\n" +
                        "Contains the nucleus and organelles essential for cell function.\n\n" +
                        "Axon (Incorrect)\n" +
                        "Transmits impulses, does not house the nucleus.\n\n" +
                        "Dendrite (Incorrect)\n" +
                        "Receives impulses but lacks the nucleus.\n\n" +
                        "Node of Ranvier (Incorrect)\n" +
                        "Gaps between myelin, not part of the neuron's metabolic machinery."
        );

        questions.add("In reflex arcs, which type of neuron directly connects the sensory neuron to the motor neuron?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Interneuron", // Correct answer
                "Afferent neuron",
                "Efferent neuron",
                "Multipolar neuron"
        )));
        correctAnswers.add("Interneuron");
        rationales.put(18,
                "RATIONALE:\n" +
                        "Interneuron (Correct answer)\n" +
                        "Interneurons connect sensory and motor neurons in the spinal cord.\n\n" +
                        "Afferent neuron (Incorrect)\n" +
                        "These are sensory neurons that bring impulses to the CNS.\n\n" +
                        "Efferent neuron (Incorrect)\n" +
                        "These are motor neurons that carry impulses away from the CNS.\n\n" +
                        "Multipolar neuron (Incorrect)\n" +
                        "This describes neuron shape, not function."
        );

        questions.add("Which cranial nerve is responsible for vision?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Optic nerve (II)", // Correct answer
                "Oculomotor nerve (III)",
                "Trochlear nerve (IV)",
                "Abducens nerve (VI)"
        )));
        correctAnswers.add("Optic nerve (II)");
        rationales.put(19,
                "RATIONALE:\n" +
                        "Optic nerve (II) (Correct answer)\n" +
                        "Transmits visual information from the retina to the brain.\n\n" +
                        "Oculomotor nerve (III) (Incorrect)\n" +
                        "Controls most eye movements, not vision.\n\n" +
                        "Trochlear nerve (IV) (Incorrect)\n" +
                        "Innervates the superior oblique muscle, aiding in eye movement.\n\n" +
                        "Abducens nerve (VI) (Incorrect)\n" +
                        "Controls lateral rectus muscle for eye abduction."
        );

        questions.add("What is the main function of myelin sheath in a neuron?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To store neurotransmitters",
                "To slow down nerve impulses",
                "To insulate the axon and increase impulse speed", // Correct answer
                "To protect the cell body"
        )));
        correctAnswers.add("To insulate the axon and increase impulse speed");
        rationales.put(20,
                "RATIONALE:\n" +
                        "To insulate the axon and increase impulse speed (Correct answer)\n" +
                        "Myelin acts as an insulator and enables faster signal transmission via saltatory conduction.\n\n" +
                        "To store neurotransmitters (Incorrect)\n" +
                        "Neurotransmitters are stored in synaptic vesicles, not in the myelin sheath.\n\n" +
                        "To slow down nerve impulses (Incorrect)\n" +
                        "Myelin speeds up impulse conduction, not slows it.\n\n" +
                        "To protect the cell body (Incorrect)\n" +
                        "The cell body is separate from the axon; myelin doesn’t protect it."
        );

        questions.add("Which part of the brain controls balance and coordination?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cerebrum",
                "Medulla oblongata",
                "Cerebellum", // Correct answer
                "Hypothalamus"
        )));
        correctAnswers.add("Cerebellum");
        rationales.put(21,
                "RATIONALE:\n" +
                        "Cerebellum (Correct answer)\n" +
                        "Coordinates voluntary movements, balance, and posture.\n\n" +
                        "Cerebrum (Incorrect)\n" +
                        "Handles higher functions like reasoning and memory, not coordination.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "Controls autonomic functions like breathing and heartbeat.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "Regulates homeostasis and endocrine functions, not motor coordination."
        );

        questions.add("Which of the following is responsible for transmitting impulses from sensory receptors to the CNS?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Motor neurons",
                "Interneurons",
                "Efferent neurons",
                "Afferent neurons" // Correct answer
        )));
        correctAnswers.add("Afferent neurons");
        rationales.put(22,
                "RATIONALE:\n" +
                        "Afferent neurons (Correct answer)\n" +
                        "These carry sensory input to the CNS from sensory receptors.\n\n" +
                        "Motor neurons (Incorrect)\n" +
                        "Carry signals from the CNS to muscles.\n\n" +
                        "Interneurons (Incorrect)\n" +
                        "Located within the CNS, connect neurons but don’t bring input from receptors.\n\n" +
                        "Efferent neurons (Incorrect)\n" +
                        "These are motor neurons, carrying impulses away from the CNS."
        );

        questions.add("Damage to the Broca’s area of the brain affects what function?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Understanding written language",
                "Controlling eye movements",
                "Producing speech", // Correct answer
                "Maintaining balance"
        )));
        correctAnswers.add("Producing speech");
        rationales.put(23,
                "RATIONALE:\n" +
                        "Producing speech (Correct answer)\n" +
                        "Broca’s area (in the frontal lobe) is essential for motor aspects of speech production.\n\n" +
                        "Understanding written language (Incorrect)\n" +
                        "This is handled more by Wernicke’s area.\n\n" +
                        "Controlling eye movements (Incorrect)\n" +
                        "Eye movement is controlled by cranial nerves.\n\n" +
                        "Maintaining balance (Incorrect)\n" +
                        "Controlled by the cerebellum."
        );

        questions.add("What is the junction between two neurons called?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Axon hillock",
                "Synapse", // Correct answer
                "Dendrite",
                "Myelin sheath"
        )));
        correctAnswers.add("Synapse");
        rationales.put(24,
                "RATIONALE:\n" +
                        "Synapse (Correct answer)\n" +
                        "The synapse is the gap where neurotransmitters transmit impulses from one neuron to another.\n\n" +
                        "Axon hillock (Incorrect)\n" +
                        "This is where action potentials are initiated, not a neuron junction.\n\n" +
                        "Dendrite (Incorrect)\n" +
                        "Dendrites receive impulses but are not the junction itself.\n\n" +
                        "Myelin sheath (Incorrect)\n" +
                        "This insulates axons, not related to synaptic transmission directly."
        );

        questions.add("Which type of glial cell forms the myelin sheath in the central nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Schwann cells",
                "Astrocytes",
                "Microglia",
                "Oligodendrocytes" // Correct answer
        )));
        correctAnswers.add("Oligodendrocytes");
        rationales.put(25,
                "RATIONALE:\n" +
                        "Oligodendrocytes (Correct answer)\n" +
                        "These form the myelin sheath around CNS axons.\n\n" +
                        "Schwann cells (Incorrect)\n" +
                        "These form myelin in the peripheral nervous system.\n\n" +
                        "Astrocytes (Incorrect)\n" +
                        "They maintain the blood-brain barrier and support neurons.\n\n" +
                        "Microglia (Incorrect)\n" +
                        "These are immune cells in the CNS."
        );

        questions.add("Which part of the spinal cord contains motor neurons?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dorsal horn",
                "Ventral horn", // Correct answer
                "Dorsal root ganglion",
                "Central canal"
        )));
        correctAnswers.add("Ventral horn");
        rationales.put(26,
                "RATIONALE:\n" +
                        "Ventral horn (Correct answer)\n" +
                        "Houses motor neurons that send signals to muscles.\n\n" +
                        "Dorsal horn (Incorrect)\n" +
                        "Contains sensory neurons.\n\n" +
                        "Dorsal root ganglion (Incorrect)\n" +
                        "Contains the cell bodies of sensory neurons.\n\n" +
                        "Central canal (Incorrect)\n" +
                        "A small channel containing cerebrospinal fluid."
        );

        questions.add("Which neurotransmitter is primarily involved in the 'fight or flight' response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Serotonin",
                "Acetylcholine",
                "Dopamine",
                "Norepinephrine" // Correct answer
        )));
        correctAnswers.add("Norepinephrine");
        rationales.put(27,
                "RATIONALE:\n" +
                        "Norepinephrine (Correct answer)\n" +
                        "Key neurotransmitter of the sympathetic nervous system.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Involved in mood and sleep, not sympathetic activation.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Mainly involved in parasympathetic and neuromuscular junctions.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Related to reward and movement."
        );

        questions.add("What condition is associated with the destruction of myelin sheaths in the CNS?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Parkinson’s disease",
                "Epilepsy",
                "Multiple sclerosis", // Correct answer
                "Alzheimer’s disease"
        )));
        correctAnswers.add("Multiple sclerosis");
        rationales.put(28,
                "RATIONALE:\n" +
                        "Multiple sclerosis (Correct answer)\n" +
                        "Autoimmune disorder that destroys CNS myelin, affecting conduction.\n\n" +
                        "Parkinson’s disease (Incorrect)\n" +
                        "Caused by dopamine deficiency, not demyelination.\n\n" +
                        "Epilepsy (Incorrect)\n" +
                        "Characterized by abnormal electrical activity in the brain.\n\n" +
                        "Alzheimer’s disease (Incorrect)\n" +
                        "Involves neurofibrillary tangles and plaques, not demyelination."
        );

        questions.add("Which part of the autonomic nervous system increases heart rate and dilates pupils during stress?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Central nervous system",
                "Sympathetic nervous system", // Correct answer
                "Parasympathetic nervous system",
                "Somatic nervous system"
        )));
        correctAnswers.add("Sympathetic nervous system");
        rationales.put(29,
                "RATIONALE:\n" +
                        "Sympathetic nervous system (Correct answer)\n" +
                        "Prepares the body for \"fight or flight\" by increasing HR and dilating pupils.\n\n" +
                        "Central nervous system (Incorrect)\n" +
                        "It includes the brain and spinal cord, not directly responsible for autonomic changes.\n\n" +
                        "Parasympathetic nervous system (Incorrect)\n" +
                        "Has the opposite effect — it calms the body.\n\n" +
                        "Somatic nervous system (Incorrect)\n" +
                        "Controls voluntary muscle movements."
        );

        questions.add("Which ion is primarily responsible for initiating an action potential in neurons?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Potassium (K⁺)",
                "Sodium (Na⁺)", // Correct answer
                "Calcium (Ca²⁺)",
                "Chloride (Cl⁻)"
        )));
        correctAnswers.add("Sodium (Na⁺)");
        rationales.put(30,
                "RATIONALE:\n" +
                        "Sodium (Na⁺) (Correct answer)\n" +
                        "During depolarization, Na⁺ rushes into the neuron to initiate the action potential.\n\n" +
                        "Potassium (K⁺) (Incorrect)\n" +
                        "Potassium is involved in repolarization, not initiation.\n\n" +
                        "Calcium (Ca²⁺) (Incorrect)\n" +
                        "Important for neurotransmitter release, not action potential initiation.\n\n" +
                        "Chloride (Cl⁻) (Incorrect)\n" +
                        "Involved in inhibitory responses, not depolarization."
        );

        questions.add("What is the function of cerebrospinal fluid (CSF)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Transmits electrical signals",
                "Nourishes and protects the brain and spinal cord", // Correct answer
                "Produces hormones",
                "Supplies oxygen directly to neurons"
        )));
        correctAnswers.add("Nourishes and protects the brain and spinal cord");
        rationales.put(31,
                "RATIONALE:\n" +
                        "Nourishes and protects the brain and spinal cord (Correct answer)\n" +
                        "CSF cushions, nourishes, and removes waste from CNS.\n\n" +
                        "Transmits electrical signals (Incorrect)\n" +
                        "This is the function of neurons, not CSF.\n\n" +
                        "Produces hormones (Incorrect)\n" +
                        "Hormones are produced by glands, not CSF.\n\n" +
                        "Supplies oxygen directly to neurons (Incorrect)\n" +
                        "Oxygen is supplied via blood, not CSF."
        );

        questions.add("Which lobe of the brain processes visual information?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Frontal",
                "Parietal",
                "Temporal",
                "Occipital" // Correct answer
        )));
        correctAnswers.add("Occipital");
        rationales.put(32,
                "RATIONALE:\n" +
                        "Occipital (Correct answer)\n" +
                        "Primary visual cortex is located here.\n\n" +
                        "Frontal (Incorrect)\n" +
                        "Involved in decision-making, planning, and motor function.\n\n" +
                        "Parietal (Incorrect)\n" +
                        "Processes sensory input like touch and temperature.\n\n" +
                        "Temporal (Incorrect)\n" +
                        "Processes hearing and memory."
        );

        questions.add("Which type of neuron connects sensory and motor neurons within the CNS?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sensory neuron",
                "Interneuron", // Correct answer
                "Motor neuron",
                "Efferent neuron"
        )));
        correctAnswers.add("Interneuron");
        rationales.put(33,
                "RATIONALE:\n" +
                        "Interneuron (Correct answer)\n" +
                        "Found only in CNS, connecting sensory and motor neurons.\n\n" +
                        "Sensory neuron (Incorrect)\n" +
                        "Brings impulses from receptors to CNS.\n\n" +
                        "Motor neuron (Incorrect)\n" +
                        "Sends signals from CNS to effectors.\n\n" +
                        "Efferent neuron (Incorrect)\n" +
                        "Another term for motor neuron."
        );

        questions.add("The parasympathetic nervous system is responsible for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dilating pupils and increasing heart rate",
                "Stimulating the \"fight or flight\" response",
                "Calming the body and conserving energy", // Correct answer
                "Increasing blood sugar levels"
        )));
        correctAnswers.add("Calming the body and conserving energy");
        rationales.put(34,
                "RATIONALE:\n" +
                        "Calming the body and conserving energy (Correct answer)\n" +
                        "Parasympathetic system slows heart rate, increases digestion.\n\n" +
                        "Dilating pupils and increasing heart rate (Incorrect)\n" +
                        "These are sympathetic effects.\n\n" +
                        "Stimulating the \"fight or flight\" response (Incorrect)\n" +
                        "That’s the sympathetic system.\n\n" +
                        "Increasing blood sugar levels (Incorrect)\n" +
                        "Sympathetic system stimulates glucose release."
        );

        questions.add("Which of the following is NOT part of the brainstem?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Medulla oblongata",
                "Pons",
                "Cerebellum", // Correct answer
                "Midbrain"
        )));
        correctAnswers.add("Cerebellum");
        rationales.put(35,
                "RATIONALE:\n" +
                        "Cerebellum (Correct answer)\n" +
                        "It is separate from the brainstem; controls coordination.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "This is a lower part of the brainstem.\n\n" +
                        "Pons (Incorrect)\n" +
                        "It is the middle portion of the brainstem.\n\n" +
                        "Midbrain (Incorrect)\n" +
                        "Forms the upper part of the brainstem."
        );

        questions.add("Which structure in the neuron receives impulses and sends them to the cell body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Axon",
                "Synapse",
                "Dendrite", // Correct answer
                "Node of Ranvier"
        )));
        correctAnswers.add("Dendrite");
        rationales.put(36,
                "RATIONALE:\n" +
                        "Dendrite (Correct answer)\n" +
                        "These short projections receive incoming signals.\n\n" +
                        "Axon (Incorrect)\n" +
                        "Axons carry impulses away from the cell body.\n\n" +
                        "Synapse (Incorrect)\n" +
                        "It is a gap between neurons, not a receptor.\n\n" +
                        "Node of Ranvier (Incorrect)\n" +
                        "These gaps help in saltatory conduction, not signal reception."
        );

        questions.add("Which of the following is an effect of sympathetic stimulation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pupil constriction",
                "Decreased heart rate",
                "Increased digestive activity",
                "Bronchodilation" // Correct answer
        )));
        correctAnswers.add("Bronchodilation");
        rationales.put(37,
                "RATIONALE:\n" +
                        "Bronchodilation (Correct answer)\n" +
                        "Sympathetic system widens airways to improve oxygen delivery.\n\n" +
                        "Pupil constriction (Incorrect)\n" +
                        "This is a parasympathetic effect.\n\n" +
                        "Decreased heart rate (Incorrect)\n" +
                        "Parasympathetic effect.\n\n" +
                        "Increased digestive activity (Incorrect)\n" +
                        "Parasympathetic promotes digestion."
        );

        questions.add("Which condition is characterized by sudden, uncontrolled electrical activity in the brain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Parkinson’s disease",
                "Stroke",
                "Epilepsy", // Correct answer
                "Multiple sclerosis"
        )));
        correctAnswers.add("Epilepsy");
        rationales.put(38,
                "RATIONALE:\n" +
                        "Epilepsy (Correct answer)\n" +
                        "Marked by abnormal electrical discharges causing seizures.\n\n" +
                        "Parkinson’s disease (Incorrect)\n" +
                        "Involves dopamine deficiency and motor symptoms.\n\n" +
                        "Stroke (Incorrect)\n" +
                        "Caused by blood supply disruption, not electrical surges.\n\n" +
                        "Multiple sclerosis (Incorrect)\n" +
                        "Involves demyelination, not seizure activity."
        );

        questions.add("What type of neuron carries impulses away from the CNS to muscles or glands?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sensory neuron",
                "Interneuron",
                "Motor neuron", // Correct answer
                "Afferent neuron"
        )));
        correctAnswers.add("Motor neuron");
        rationales.put(39,
                "RATIONALE:\n" +
                        "Motor neuron (Correct answer)\n" +
                        "Sends impulses from CNS to effectors (muscles/glands).\n\n" +
                        "Sensory neuron (Incorrect)\n" +
                        "Brings signals to the CNS.\n\n" +
                        "Interneuron (Incorrect)\n" +
                        "Connects neurons within the CNS.\n\n" +
                        "Afferent neuron (Incorrect)\n" +
                        "Another name for sensory neuron."
        );

        questions.add("The blood-brain barrier is responsible for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Allowing all substances to pass freely into the brain",
                "Protecting the brain from toxic substances and pathogens", // Correct answer
                "Producing cerebrospinal fluid",
                "Supplying glucose to neurons"
        )));
        correctAnswers.add("Protecting the brain from toxic substances and pathogens");
        rationales.put(40,
                "RATIONALE:\n" +
                        "Protecting the brain from toxic substances and pathogens (Correct answer)\n" +
                        "The blood-brain barrier helps prevent harmful substances from reaching the brain.\n\n" +
                        "Allowing all substances to pass freely into the brain (Incorrect)\n" +
                        "The blood-brain barrier selectively limits what can pass into the brain.\n\n" +
                        "Producing cerebrospinal fluid (Incorrect)\n" +
                        "CSF is produced by the choroid plexus, not the blood-brain barrier.\n\n" +
                        "Supplying glucose to neurons (Incorrect)\n" +
                        "The blood-brain barrier regulates glucose supply, but it does not directly provide it."
        );

        questions.add("Which neurotransmitter is most associated with the 'fight or flight' response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dopamine",
                "Acetylcholine",
                "Norepinephrine", // Correct answer
                "Serotonin"
        )));
        correctAnswers.add("Norepinephrine");
        rationales.put(41,
                "RATIONALE:\n" +
                        "Norepinephrine (Correct answer)\n" +
                        "It is a key neurotransmitter in the sympathetic nervous system that triggers the 'fight or flight' response.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in reward and motor control, not the stress response.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Acetylcholine is important in parasympathetic functions.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Serotonin regulates mood and sleep, not the stress response."
        );

        questions.add("The primary function of myelin in neurons is to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Increase the number of synaptic connections",
                "Facilitate faster transmission of electrical impulses", // Correct answer
                "Support neuron structure and shape",
                "Protect the neuron from pathogens"
        )));
        correctAnswers.add("Facilitate faster transmission of electrical impulses");
        rationales.put(42,
                "RATIONALE:\n" +
                        "Facilitate faster transmission of electrical impulses (Correct answer)\n" +
                        "Myelin acts as insulation, speeding up impulse conduction (saltatory conduction).\n\n" +
                        "Increase the number of synaptic connections (Incorrect)\n" +
                        "Myelin doesn’t affect the number of synapses; it aids conduction speed.\n\n" +
                        "Support neuron structure and shape (Incorrect)\n" +
                        "This function is mainly attributed to the neuron’s cytoskeleton.\n\n" +
                        "Protect the neuron from pathogens (Incorrect)\n" +
                        "Myelin doesn't provide immune protection."
        );

        questions.add("Which part of the neuron is responsible for transmitting impulses away from the cell body?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dendrite",
                "Synapse",
                "Axon", // Correct answer
                "Soma"
        )));
        correctAnswers.add("Axon");
        rationales.put(43,
                "RATIONALE:\n" +
                        "Axon (Correct answer)\n" +
                        "The axon carries impulses away from the neuron’s cell body to other neurons or effectors.\n\n" +
                        "Dendrite (Incorrect)\n" +
                        "Dendrites receive impulses toward the cell body, not away.\n\n" +
                        "Synapse (Incorrect)\n" +
                        "The synapse is the gap where signals are transmitted between neurons.\n\n" +
                        "Soma (Incorrect)\n" +
                        "The soma (cell body) contains the nucleus and is responsible for maintaining cell functions."
        );

        questions.add("Which of the following conditions is associated with the degeneration of dopamine-producing neurons?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Alzheimer’s disease",
                "Parkinson’s disease", // Correct answer
                "Multiple sclerosis",
                "Amyotrophic lateral sclerosis"
        )));
        correctAnswers.add("Parkinson’s disease");
        rationales.put(44,
                "RATIONALE:\n" +
                        "Parkinson’s disease (Correct answer)\n" +
                        "Caused by degeneration of dopamine-producing neurons in the basal ganglia, leading to motor issues.\n\n" +
                        "Alzheimer’s disease (Incorrect)\n" +
                        "Characterized by the loss of memory and cognitive function, not dopamine-producing neurons.\n\n" +
                        "Multiple sclerosis (Incorrect)\n" +
                        "It involves demyelination of neurons in the CNS, not dopamine loss.\n\n" +
                        "Amyotrophic lateral sclerosis (Incorrect)\n" +
                        "ALS is a progressive degeneration of motor neurons, not specifically dopamine-producing neurons."
        );

        questions.add("The sympathetic nervous system primarily prepares the body for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Rest and digestion",
                "\"Fight or flight\" responses", // Correct answer
                "Energy conservation",
                "Memory storage"
        )));
        correctAnswers.add("\"Fight or flight\" responses");
        rationales.put(45,
                "RATIONALE:\n" +
                        "\"Fight or flight\" responses (Correct answer)\n" +
                        "The sympathetic nervous system activates the body’s alertness and readiness to face stress or danger.\n\n" +
                        "Rest and digestion (Incorrect)\n" +
                        "This is primarily the function of the parasympathetic nervous system.\n\n" +
                        "Energy conservation (Incorrect)\n" +
                        "Sympathetic activation is energy-consuming, not conserving.\n\n" +
                        "Memory storage (Incorrect)\n" +
                        "This is not directly related to the autonomic nervous systems."
        );

        questions.add("The term \"somatic nervous system\" refers to the part of the peripheral nervous system that:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Controls involuntary bodily functions",
                "Controls voluntary movements of skeletal muscles", // Correct answer
                "Regulates heart rate and breathing",
                "Manages hormone release"
        )));
        correctAnswers.add("Controls voluntary movements of skeletal muscles");
        rationales.put(46,
                "RATIONALE:\n" +
                        "Controls voluntary movements of skeletal muscles (Correct answer)\n" +
                        "The somatic nervous system controls voluntary movements through motor neurons.\n\n" +
                        "Controls involuntary bodily functions (Incorrect)\n" +
                        "This is the role of the autonomic nervous system, not the somatic.\n\n" +
                        "Regulates heart rate and breathing (Incorrect)\n" +
                        "These are autonomic functions controlled by the sympathetic and parasympathetic systems.\n\n" +
                        "Manages hormone release (Incorrect)\n" +
                        "This is controlled by the endocrine system, not the somatic nervous system."
        );

        questions.add("The brain region responsible for regulating basic life functions like heart rate, breathing, and blood pressure is the:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Cerebrum",
                "Medulla oblongata", // Correct answer
                "Thalamus",
                "Hypothalamus"
        )));
        correctAnswers.add("Medulla oblongata");
        rationales.put(47,
                "RATIONALE:\n" +
                        "Medulla oblongata (Correct answer)\n" +
                        "The medulla controls autonomic functions like heart rate and breathing.\n\n" +
                        "Cerebrum (Incorrect)\n" +
                        "Responsible for higher functions like thought, memory, and voluntary movements, not basic life functions.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "Acts as a relay station for sensory and motor signals but doesn't regulate basic life functions.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "Regulates hunger, temperature, and other functions but does not directly control breathing or heart rate."
        );

        questions.add("The neurotransmitter that is primarily involved in muscle contraction is:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dopamine",
                "Serotonin",
                "Acetylcholine", // Correct answer
                "Norepinephrine"
        )));
        correctAnswers.add("Acetylcholine");
        rationales.put(48,
                "RATIONALE:\n" +
                        "Acetylcholine (Correct answer)\n" +
                        "Acetylcholine is released at neuromuscular junctions to stimulate muscle contraction.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in movement and reward, not muscle contraction.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Serotonin is involved in mood regulation, not directly in muscle function.\n\n" +
                        "Norepinephrine (Incorrect)\n" +
                        "Norepinephrine is involved in the \"fight or flight\" response, not muscle contraction."
        );

        questions.add("Which of the following is a characteristic of an action potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It decreases in strength as it travels along the axon",
                "It can be partially blocked by local anesthetics",
                "It is a graded response to a stimulus",
                "It is a rapid, all-or-nothing electrical signal" // Correct answer
        )));
        correctAnswers.add("It is a rapid, all-or-nothing electrical signal");
        rationales.put(49,
                "RATIONALE:\n" +
                        "It is a rapid, all-or-nothing electrical signal (Correct answer)\n" +
                        "Action potentials either occur fully or not at all when the threshold is reached.\n\n" +
                        "It decreases in strength as it travels along the axon (Incorrect)\n" +
                        "Action potentials maintain the same strength as they propagate.\n\n" +
                        "It can be partially blocked by local anesthetics (Incorrect)\n" +
                        "Local anesthetics block action potentials by inhibiting sodium channels.\n\n" +
                        "It is a graded response to a stimulus (Incorrect)\n" +
                        "Action potentials are \"all-or-nothing\" and don't vary in magnitude."
        );

        questions.add("Which part of the nervous system is responsible for the 'rest and digest' response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sympathetic nervous system",
                "Parasympathetic nervous system", // Correct answer
                "Somatic nervous system",
                "Central nervous system"
        )));
        correctAnswers.add("Parasympathetic nervous system");
        rationales.put(50,
                "RATIONALE:\n" +
                        "Parasympathetic nervous system (Correct answer)\n" +
                        "It controls the body’s relaxation functions, such as slowing the heart rate and promoting digestion.\n\n" +
                        "Sympathetic nervous system (Incorrect)\n" +
                        "The sympathetic nervous system is responsible for the 'fight or flight' response, not 'rest and digest.'\n\n" +
                        "Somatic nervous system (Incorrect)\n" +
                        "The somatic nervous system controls voluntary movements, not 'rest and digest.'\n\n" +
                        "Central nervous system (Incorrect)\n" +
                        "The CNS processes information, but the parasympathetic system specifically controls the 'rest and digest' functions."
        );

        questions.add("Which of the following is a key function of the hypothalamus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulating memory and learning",
                "Controlling the autonomic nervous system and endocrine system", // Correct answer
                "Processing sensory information",
                "Coordinating voluntary muscle movements"
        )));
        correctAnswers.add("Controlling the autonomic nervous system and endocrine system");
        rationales.put(51,
                "RATIONALE:\n" +
                        "Controlling the autonomic nervous system and endocrine system (Correct answer)\n" +
                        "The hypothalamus regulates functions like temperature control, hunger, and hormonal balance.\n\n" +
                        "Regulating memory and learning (Incorrect)\n" +
                        "Memory and learning are primarily regulated by the hippocampus, not the hypothalamus.\n\n" +
                        "Processing sensory information (Incorrect)\n" +
                        "This function is mainly attributed to the thalamus, not the hypothalamus.\n\n" +
                        "Coordinating voluntary muscle movements (Incorrect)\n" +
                        "Voluntary muscle movements are coordinated by the motor cortex and cerebellum, not the hypothalamus."
        );

        questions.add("The term 'gray matter' in the brain refers to:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Myelinated axons",
                "Neuron cell bodies and dendrites", // Correct answer
                "Cerebrospinal fluid",
                "The blood-brain barrier"
        )));
        correctAnswers.add("Neuron cell bodies and dendrites");
        rationales.put(52,
                "RATIONALE:\n" +
                        "Neuron cell bodies and dendrites (Correct answer)\n" +
                        "Gray matter consists of the neuron cell bodies, dendrites, and unmyelinated axons.\n\n" +
                        "Myelinated axons (Incorrect)\n" +
                        "Myelinated axons make up 'white matter,' not 'gray matter.'\n\n" +
                        "Cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is a clear liquid found in the ventricles of the brain and spinal cord, not gray matter.\n\n" +
                        "The blood-brain barrier (Incorrect)\n" +
                        "The blood-brain barrier is a selective barrier to protect the brain, not a component of gray matter."
        );

        questions.add("Which of the following structures is responsible for the production of cerebrospinal fluid (CSF)?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Choroid plexus", // Correct answer
                "Pineal gland",
                "Pituitary gland",
                "Medulla oblongata"
        )));
        correctAnswers.add("Choroid plexus");
        rationales.put(53,
                "RATIONALE:\n" +
                        "Choroid plexus (Correct answer)\n" +
                        "The choroid plexus is located in the ventricles of the brain and produces cerebrospinal fluid.\n\n" +
                        "Pineal gland (Incorrect)\n" +
                        "The pineal gland produces melatonin, not cerebrospinal fluid.\n\n" +
                        "Pituitary gland (Incorrect)\n" +
                        "The pituitary gland is involved in hormone production, not CSF production.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata controls autonomic functions but does not produce cerebrospinal fluid."
        );

        questions.add("Which part of the nervous system controls voluntary muscle movements?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Autonomic nervous system",
                "Somatic nervous system", // Correct answer
                "Parasympathetic nervous system",
                "Sympathetic nervous system"
        )));
        correctAnswers.add("Somatic nervous system");
        rationales.put(54,
                "RATIONALE:\n" +
                        "Somatic nervous system (Correct answer)\n" +
                        "The somatic nervous system controls voluntary movements of skeletal muscles.\n\n" +
                        "Autonomic nervous system (Incorrect)\n" +
                        "The autonomic nervous system controls involuntary functions like heart rate and digestion.\n\n" +
                        "Parasympathetic nervous system (Incorrect)\n" +
                        "The parasympathetic system regulates involuntary functions such as digestion and heart rate, not voluntary muscle movements.\n\n" +
                        "Sympathetic nervous system (Incorrect)\n" +
                        "The sympathetic nervous system activates 'fight or flight' responses, but it doesn’t control voluntary muscle movements."
        );

        questions.add("What is the main function of the cerebellum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulating heart rate and blood pressure",
                "Processing sensory information",
                "Coordinating voluntary muscle movements and balance", // Correct answer
                "Regulating emotional responses"
        )));
        correctAnswers.add("Coordinating voluntary muscle movements and balance");
        rationales.put(55,
                "RATIONALE:\n" +
                        "Coordinating voluntary muscle movements and balance (Correct answer)\n" +
                        "The cerebellum is crucial for motor control, balance, and coordination of voluntary movements.\n\n" +
                        "Regulating heart rate and blood pressure (Incorrect)\n" +
                        "This function is controlled by the medulla oblongata, not the cerebellum.\n\n" +
                        "Processing sensory information (Incorrect)\n" +
                        "Sensory processing is primarily managed by the thalamus and sensory cortex.\n\n" +
                        "Regulating emotional responses (Incorrect)\n" +
                        "Emotional regulation involves the limbic system, not the cerebellum."
        );

        questions.add("Which of the following is a function of the spinal cord?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Processing higher cognitive functions",
                "Relaying information between the brain and body", // Correct answer
                "Producing hormones",
                "Maintaining balance and coordination"
        )));
        correctAnswers.add("Relaying information between the brain and body");
        rationales.put(56,
                "RATIONALE:\n" +
                        "Relaying information between the brain and body (Correct answer)\n" +
                        "The spinal cord transmits signals between the brain and the rest of the body.\n\n" +
                        "Processing higher cognitive functions (Incorrect)\n" +
                        "Higher cognitive functions like thinking and memory are processed by the cerebrum, not the spinal cord.\n\n" +
                        "Producing hormones (Incorrect)\n" +
                        "Hormone production is done by the endocrine system, not the spinal cord.\n\n" +
                        "Maintaining balance and coordination (Incorrect)\n" +
                        "Balance and coordination are mainly functions of the cerebellum."
        );

        questions.add("Which of the following is a characteristic of a reflex arc?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It requires conscious thought",
                "It involves a direct connection between sensory neurons and motor neurons", // Correct answer
                "It always involves the brain",
                "It is always voluntary"
        )));
        correctAnswers.add("It involves a direct connection between sensory neurons and motor neurons");
        rationales.put(57,
                "RATIONALE:\n" +
                        "It involves a direct connection between sensory neurons and motor neurons (Correct answer)\n" +
                        "Reflex arcs are direct pathways that involve sensory and motor neurons for quick responses.\n\n" +
                        "It requires conscious thought (Incorrect)\n" +
                        "Reflexes are automatic and do not require conscious thought.\n\n" +
                        "It always involves the brain (Incorrect)\n" +
                        "Reflex arcs can bypass the brain, involving only the spinal cord.\n\n" +
                        "It is always voluntary (Incorrect)\n" +
                        "Reflexes are involuntary responses to stimuli."
        );

        questions.add("Which of the following structures is involved in the formation of long-term memories?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hippocampus", // Correct answer
                "Cerebellum",
                "Medulla oblongata",
                "Thalamus"
        )));
        correctAnswers.add("Hippocampus");
        rationales.put(58,
                "RATIONALE:\n" +
                        "Hippocampus (Correct answer)\n" +
                        "The hippocampus plays a crucial role in forming and storing long-term memories.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum is involved in coordination and motor control, not memory formation.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla controls autonomic functions, not memory.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "The thalamus acts as a relay station for sensory signals but is not primarily involved in memory formation."
        );

        questions.add("Which neurotransmitter is primarily involved in mood regulation and is often linked to depression?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Acetylcholine",
                "Dopamine",
                "Serotonin", // Correct answer
                "Norepinephrine"
        )));
        correctAnswers.add("Serotonin");
        rationales.put(59,
                "RATIONALE:\n" +
                        "Serotonin (Correct answer)\n" +
                        "Serotonin is a neurotransmitter that plays a key role in mood regulation, and low levels are linked to depression.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Acetylcholine is involved in muscle contraction and parasympathetic functions, not mood regulation.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in reward, motivation, and motor control, but serotonin is more directly related to mood regulation.\n\n" +
                        "Norepinephrine (Incorrect)\n" +
                        "Norepinephrine is involved in the 'fight or flight' response, but serotonin is more involved in mood regulation."
        );

        questions.add("Which of the following is the main function of the corpus callosum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulating autonomic functions",
                "Connecting the left and right hemispheres of the brain", // Correct answer
                "Coordinating muscle movements",
                "Processing sensory information"
        )));
        correctAnswers.add("Connecting the left and right hemispheres of the brain");
        rationales.put(60,
                "RATIONALE:\n" +
                        "Connecting the left and right hemispheres of the brain (Correct answer)\n" +
                        "The corpus callosum is the large bundle of nerve fibers that connects the left and right hemispheres, allowing for communication between them.\n\n" +
                        "Regulating autonomic functions (Incorrect)\n" +
                        "Autonomic functions are primarily regulated by the brainstem and hypothalamus, not the corpus callosum.\n\n" +
                        "Coordinating muscle movements (Incorrect)\n" +
                        "Muscle coordination is controlled by the cerebellum, not the corpus callosum.\n\n" +
                        "Processing sensory information (Incorrect)\n" +
                        "Sensory processing mainly occurs in the thalamus and sensory cortex, not the corpus callosum."
        );

        questions.add("The blood-brain barrier is responsible for:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Preventing blood from entering the brain",
                "Preventing harmful substances from entering the brain", // Correct answer
                "Delivering nutrients to brain cells",
                "Filtering cerebrospinal fluid"
        )));
        correctAnswers.add("Preventing harmful substances from entering the brain");
        rationales.put(61,
                "RATIONALE:\n" +
                        "Preventing harmful substances from entering the brain (Correct answer)\n" +
                        "The blood-brain barrier selectively filters out harmful substances, protecting the brain from toxins and infections.\n\n" +
                        "Preventing blood from entering the brain (Incorrect)\n" +
                        "Blood does enter the brain through blood vessels; the blood-brain barrier regulates what can pass through.\n\n" +
                        "Delivering nutrients to brain cells (Incorrect)\n" +
                        "Nutrients are delivered to the brain through blood flow, but the blood-brain barrier controls what substances pass into the brain, not directly delivering nutrients.\n\n" +
                        "Filtering cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is produced and filtered by the choroid plexus, not the blood-brain barrier."
        );

        questions.add("Which of the following is true about the sympathetic nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It conserves energy during rest",
                "It stimulates the \"fight or flight\" response", // Correct answer
                "It regulates digestion",
                "It lowers heart rate and blood pressure"
        )));
        correctAnswers.add("It stimulates the \"fight or flight\" response");
        rationales.put(62,
                "RATIONALE:\n" +
                        "It stimulates the \"fight or flight\" response (Correct answer)\n" +
                        "The sympathetic nervous system activates the body's \"fight or flight\" response, increasing heart rate, blood pressure, and energy levels.\n\n" +
                        "It conserves energy during rest (Incorrect)\n" +
                        "The sympathetic nervous system prepares the body for action, not rest. The parasympathetic system conserves energy during rest.\n\n" +
                        "It regulates digestion (Incorrect)\n" +
                        "Digestion is primarily regulated by the parasympathetic nervous system, not the sympathetic system.\n\n" +
                        "It lowers heart rate and blood pressure (Incorrect)\n" +
                        "The sympathetic system increases heart rate and blood pressure in response to stress or danger."
        );

        questions.add("Which of the following is responsible for the \"fight or flight\" response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypothalamus",
                "Sympathetic nervous system", // Correct answer
                "Parasympathetic nervous system",
                "Cerebellum"
        )));
        correctAnswers.add("Sympathetic nervous system");
        rationales.put(63,
                "RATIONALE:\n" +
                        "Sympathetic nervous system (Correct answer)\n" +
                        "The sympathetic nervous system triggers the body’s \"fight or flight\" response, preparing it for action.\n\n" +
                        "Hypothalamus (Incorrect)\n" +
                        "While the hypothalamus is involved in regulating the autonomic nervous system, it is the sympathetic nervous system that directly triggers the \"fight or flight\" response.\n\n" +
                        "Parasympathetic nervous system (Incorrect)\n" +
                        "The parasympathetic nervous system promotes relaxation and recovery, counteracting the \"fight or flight\" response.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum is involved in motor coordination, not the \"fight or flight\" response."
        );

        questions.add("Which of the following neurotransmitters is primarily involved in muscle contraction?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dopamine",
                "Acetylcholine", // Correct answer
                "Serotonin",
                "Norepinephrine"
        )));
        correctAnswers.add("Acetylcholine");
        rationales.put(64,
                "RATIONALE:\n" +
                        "Acetylcholine (Correct answer)\n" +
                        "Acetylcholine is the primary neurotransmitter involved in muscle contraction at neuromuscular junctions.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in reward pathways and motor control, but it does not directly cause muscle contraction.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Serotonin regulates mood and some other functions, but it is not directly involved in muscle contraction.\n\n" +
                        "Norepinephrine (Incorrect)\n" +
                        "Norepinephrine is involved in arousal and alertness, but not muscle contraction."
        );

        questions.add("Which of the following is a characteristic of an action potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is a graded response",
                "It travels in both directions along the axon",
                "It is an all-or-nothing response", // Correct answer
                "It decreases in amplitude as it travels"
        )));
        correctAnswers.add("It is an all-or-nothing response");
        rationales.put(65,
                "RATIONALE:\n" +
                        "It is an all-or-nothing response (Correct answer)\n" +
                        "Once the threshold is reached, an action potential is generated and always travels at the same amplitude.\n\n" +
                        "It is a graded response (Incorrect)\n" +
                        "Action potentials are all-or-nothing; they do not vary in strength like graded potentials.\n\n" +
                        "It travels in both directions along the axon (Incorrect)\n" +
                        "Action potentials usually travel in one direction, from the axon hillock to the axon terminals.\n\n" +
                        "It decreases in amplitude as it travels (Incorrect)\n" +
                        "Action potentials do not decrease in amplitude as they travel; they are regenerated along the axon, maintaining constant strength."
        );

        questions.add("Which part of the brain is primarily responsible for regulating breathing and heart rate?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Medulla oblongata", // Correct answer
                "Cerebellum",
                "Thalamus",
                "Corpus callosum"
        )));
        correctAnswers.add("Medulla oblongata");
        rationales.put(66,
                "RATIONALE:\n" +
                        "Medulla oblongata (Correct answer)\n" +
                        "The medulla oblongata controls autonomic functions such as breathing, heart rate, and blood pressure.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum is involved in coordination and motor control, not autonomic functions.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "The thalamus acts as a relay station for sensory information, but does not regulate breathing or heart rate.\n\n" +
                        "Corpus callosum (Incorrect)\n" +
                        "The corpus callosum connects the two hemispheres of the brain, but does not regulate autonomic functions."
        );

        questions.add("Which of the following structures is responsible for the processing of emotions?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hippocampus",
                "Amygdala", // Correct answer
                "Cerebellum",
                "Medulla oblongata"
        )));
        correctAnswers.add("Amygdala");
        rationales.put(67,
                "RATIONALE:\n" +
                        "Amygdala (Correct answer)\n" +
                        "The amygdala is primarily responsible for processing emotions such as fear and anger.\n\n" +
                        "Hippocampus (Incorrect)\n" +
                        "The hippocampus is involved in memory formation, not emotion processing.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum is involved in motor coordination, not emotion processing.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata controls autonomic functions like breathing and heart rate, not emotions."
        );

        questions.add("Which of the following is a function of the frontal lobe?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Processing visual information",
                "Processing auditory information",
                "Voluntary motor control and decision-making", // Correct answer
                "Regulating autonomic functions"
        )));
        correctAnswers.add("Voluntary motor control and decision-making");
        rationales.put(68,
                "RATIONALE:\n" +
                        "Voluntary motor control and decision-making (Correct answer)\n" +
                        "The frontal lobe is involved in higher cognitive functions, including voluntary movement and decision-making.\n\n" +
                        "Processing visual information (Incorrect)\n" +
                        "Visual information is processed in the occipital lobe, not the frontal lobe.\n\n" +
                        "Processing auditory information (Incorrect)\n" +
                        "Auditory information is processed in the temporal lobe.\n\n" +
                        "Regulating autonomic functions (Incorrect)\n" +
                        "Autonomic functions are regulated by the brainstem, not the frontal lobe."
        );

        questions.add("What is the function of oligodendrocytes in the central nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To support and protect neurons",
                "To form myelin sheaths around axons", // Correct answer
                "To create cerebrospinal fluid",
                "To remove waste products from neurons"
        )));
        correctAnswers.add("To form myelin sheaths around axons");
        rationales.put(69,
                "RATIONALE:\n" +
                        "To form myelin sheaths around axons (Correct answer)\n" +
                        "Oligodendrocytes form the myelin sheath around axons in the central nervous system, aiding in fast signal transmission.\n\n" +
                        "To support and protect neurons (Incorrect)\n" +
                        "While oligodendrocytes support neurons, their main function is to form myelin sheaths.\n\n" +
                        "To create cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is produced by the choroid plexus, not oligodendrocytes.\n\n" +
                        "To remove waste products from neurons (Incorrect)\n" +
                        "Microglia are responsible for cleaning up waste in the CNS, not oligodendrocytes."
        );

        questions.add("Which of the following structures is responsible for the coordination of voluntary movements?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Medulla oblongata",
                "Cerebellum", // Correct answer
                "Thalamus",
                "Basal ganglia"
        )));
        correctAnswers.add("Cerebellum");
        rationales.put(70,
                "RATIONALE:\n" +
                        "Cerebellum (Correct answer)\n" +
                        "The cerebellum coordinates voluntary movements, balance, and motor learning.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata is responsible for autonomic functions like breathing and heart rate, not voluntary movement coordination.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "The thalamus acts as a relay station for sensory information, but it doesn't coordinate voluntary movements.\n\n" +
                        "Basal ganglia (Incorrect)\n" +
                        "The basal ganglia help with motor control but are not the primary structure for movement coordination like the cerebellum."
        );

        questions.add("The sympathetic nervous system primarily uses which neurotransmitter?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Dopamine",
                "Acetylcholine",
                "Norepinephrine", // Correct answer
                "Serotonin"
        )));
        correctAnswers.add("Norepinephrine");
        rationales.put(71,
                "RATIONALE:\n" +
                        "Norepinephrine (Correct answer)\n" +
                        "Norepinephrine is the primary neurotransmitter of the sympathetic nervous system, responsible for the \"fight or flight\" response.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in pleasure, reward, and movement control, not the primary neurotransmitter of the sympathetic nervous system.\n\n" +
                        "Acetylcholine (Incorrect)\n" +
                        "Acetylcholine is used by the parasympathetic nervous system, not the sympathetic system.\n\n" +
                        "Serotonin (Incorrect)\n" +
                        "Serotonin regulates mood and sleep but does not play a major role in the sympathetic nervous system."
        );

        questions.add("Which of the following is a primary function of the hypothalamus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulate body temperature and hunger", // Correct answer
                "Coordinate voluntary motor movements",
                "Control heart rate and blood pressure",
                "Process sensory information"
        )));
        correctAnswers.add("Regulate body temperature and hunger");
        rationales.put(72,
                "RATIONALE:\n" +
                        "Regulate body temperature and hunger (Correct answer)\n" +
                        "The hypothalamus is involved in maintaining homeostasis, including regulating body temperature, hunger, thirst, and other autonomic functions.\n\n" +
                        "Coordinate voluntary motor movements (Incorrect)\n" +
                        "Voluntary motor movements are coordinated by the motor cortex and cerebellum.\n\n" +
                        "Control heart rate and blood pressure (Incorrect)\n" +
                        "The medulla oblongata is responsible for controlling heart rate and blood pressure.\n\n" +
                        "Process sensory information (Incorrect)\n" +
                        "Sensory processing occurs in the thalamus and sensory cortex, not the hypothalamus."
        );

        questions.add("Which of the following best describes a graded potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is an all-or-nothing response",
                "It is a variable response, the magnitude depends on the stimulus strength", // Correct answer
                "It travels without decrement",
                "It involves the release of neurotransmitters"
        )));
        correctAnswers.add("It is a variable response, the magnitude depends on the stimulus strength");
        rationales.put(73,
                "RATIONALE:\n" +
                        "It is a variable response, the magnitude depends on the stimulus strength (Correct answer)\n" +
                        "Graded potentials vary in size depending on the strength of the stimulus.\n\n" +
                        "It is an all-or-nothing response (Incorrect)\n" +
                        "This describes an action potential, not a graded potential.\n\n" +
                        "It travels without decrement (Incorrect)\n" +
                        "Graded potentials diminish in strength as they travel.\n\n" +
                        "It involves the release of neurotransmitters (Incorrect)\n" +
                        "Neurotransmitter release is typically involved in synaptic transmission, not the generation of graded potentials."
        );

        questions.add("What is the role of the pineal gland?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Regulation of body temperature",
                "Production of melatonin", // Correct answer
                "Regulation of blood glucose levels",
                "Production of growth hormone"
        )));
        correctAnswers.add("Production of melatonin");
        rationales.put(74,
                "RATIONALE:\n" +
                        "Production of melatonin (Correct answer)\n" +
                        "The pineal gland produces melatonin, which regulates the sleep-wake cycle.\n\n" +
                        "Regulation of body temperature (Incorrect)\n" +
                        "Body temperature regulation is managed by the hypothalamus, not the pineal gland.\n\n" +
                        "Regulation of blood glucose levels (Incorrect)\n" +
                        "Blood glucose levels are regulated by the pancreas, not the pineal gland.\n\n" +
                        "Production of growth hormone (Incorrect)\n" +
                        "Growth hormone is produced by the pituitary gland, not the pineal gland."
        );

        questions.add("The blood-brain barrier is composed of:");
        choices.add(new ArrayList<>(Arrays.asList(
                "Blood vessels only",
                "Glial cells and endothelial cells of capillaries", // Correct answer
                "Myelin sheaths",
                "Cerebrospinal fluid"
        )));
        correctAnswers.add("Glial cells and endothelial cells of capillaries");
        rationales.put(75,
                "RATIONALE:\n" +
                        "Glial cells and endothelial cells of capillaries (Correct answer)\n" +
                        "The blood-brain barrier is formed by the tight junctions between endothelial cells in brain capillaries, supported by glial cells like astrocytes.\n\n" +
                        "Blood vessels only (Incorrect)\n" +
                        "The blood-brain barrier is not just made of blood vessels, it also involves glial cells and endothelial cells.\n\n" +
                        "Myelin sheaths (Incorrect)\n" +
                        "Myelin sheaths are formed by oligodendrocytes and Schwann cells to insulate axons, not a part of the blood-brain barrier.\n\n" +
                        "Cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is found in the ventricles of the brain and around the spinal cord, not in the blood-brain barrier."
        );

        questions.add("Which of the following ions is primarily responsible for the depolarization phase of an action potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Potassium (K⁺)",
                "Sodium (Na⁺)", // Correct answer
                "Chloride (Cl⁻)",
                "Calcium (Ca²⁺)"
        )));
        correctAnswers.add("Sodium (Na⁺)");
        rationales.put(76,
                "RATIONALE:\n" +
                        "Sodium (Na⁺) (Correct answer)\n" +
                        "During depolarization, sodium channels open, allowing Na⁺ ions to rush into the neuron, causing the membrane potential to become more positive.\n\n" +
                        "Potassium (K⁺) (Incorrect)\n" +
                        "Potassium ions are involved in the repolarization phase, not depolarization.\n\n" +
                        "Chloride (Cl⁻) (Incorrect)\n" +
                        "Chloride ions are involved in inhibitory postsynaptic potentials, not depolarization.\n\n" +
                        "Calcium (Ca²⁺) (Incorrect)\n" +
                        "Calcium ions are important in neurotransmitter release and muscle contraction, but they do not play a major role in depolarization."
        );

        questions.add("Which part of the brain is involved in the regulation of autonomic functions such as heart rate and blood pressure?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Medulla oblongata", // Correct answer
                "Cerebellum",
                "Occipital lobe",
                "Temporal lobe"
        )));
        correctAnswers.add("Medulla oblongata");
        rationales.put(77,
                "RATIONALE:\n" +
                        "Medulla oblongata (Correct answer)\n" +
                        "The medulla oblongata controls autonomic functions like heart rate, blood pressure, and respiration.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum coordinates motor functions but does not regulate autonomic functions.\n\n" +
                        "Occipital lobe (Incorrect)\n" +
                        "The occipital lobe is primarily responsible for visual processing.\n\n" +
                        "Temporal lobe (Incorrect)\n" +
                        "The temporal lobe is involved in auditory processing and memory, not autonomic regulation."
        );

        questions.add("Which of the following structures is responsible for the processing of sensory information in the brain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Thalamus", // Correct answer
                "Cerebellum",
                "Medulla oblongata",
                "Hippocampus"
        )));
        correctAnswers.add("Thalamus");
        rationales.put(78,
                "RATIONALE:\n" +
                        "Thalamus (Correct answer)\n" +
                        "The thalamus is the brain’s relay station for sensory information, transmitting it to the appropriate cortical areas for processing.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum is involved in motor control, not sensory processing.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata is involved in autonomic functions, not sensory processing.\n\n" +
                        "Hippocampus (Incorrect)\n" +
                        "The hippocampus is involved in memory formation, not sensory processing."
        );

        questions.add("Which of the following cells are responsible for myelinating axons in the peripheral nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Astrocytes",
                "Oligodendrocytes",
                "Schwann cells", // Correct answer
                "Microglia"
        )));
        correctAnswers.add("Schwann cells");
        rationales.put(79,
                "RATIONALE:\n" +
                        "Schwann cells (Correct answer)\n" +
                        "Schwann cells are responsible for myelinating axons in the peripheral nervous system, which increases the speed of nerve impulse transmission.\n\n" +
                        "Astrocytes (Incorrect)\n" +
                        "Astrocytes are glial cells that provide support and nourishment to neurons, but they do not myelinate axons.\n\n" +
                        "Oligodendrocytes (Incorrect)\n" +
                        "Oligodendrocytes myelinate axons in the central nervous system, not the peripheral nervous system.\n\n" +
                        "Microglia (Incorrect)\n" +
                        "Microglia are immune cells in the central nervous system that remove debris, not myelinating cells."
        );

        questions.add("Which part of the nervous system is responsible for the 'rest and digest' response?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sympathetic nervous system",
                "Parasympathetic nervous system", // Correct answer
                "Somatic nervous system",
                "Autonomic nervous system"
        )));
        correctAnswers.add("Parasympathetic nervous system");
        rationales.put(80,
                "RATIONALE:\n" +
                        "Parasympathetic nervous system (Correct answer)\n" +
                        "The parasympathetic nervous system is responsible for conserving energy and promoting activities that occur during rest, such as digestion.\n\n" +
                        "Sympathetic nervous system (Incorrect)\n" +
                        "The sympathetic nervous system is responsible for the 'fight or flight' response, not the 'rest and digest' response.\n\n" +
                        "Somatic nervous system (Incorrect)\n" +
                        "The somatic nervous system controls voluntary movements, not autonomic functions like 'rest and digest.'\n\n" +
                        "Autonomic nervous system (Incorrect)\n" +
                        "While the autonomic nervous system controls involuntary functions, it is divided into the sympathetic and parasympathetic systems, with the parasympathetic responsible for 'rest and digest.'"
        );

        questions.add("What is the primary role of the blood-brain barrier?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To prevent toxins from entering the brain", // Correct answer
                "To regulate cerebrospinal fluid levels",
                "To remove metabolic waste from the brain",
                "To maintain electrolyte balance in the brain"
        )));
        correctAnswers.add("To prevent toxins from entering the brain");
        rationales.put(81,
                "RATIONALE:\n" +
                        "To prevent toxins from entering the brain (Correct answer)\n" +
                        "The blood-brain barrier protects the brain by restricting the passage of harmful substances, including toxins and pathogens.\n\n" +
                        "To regulate cerebrospinal fluid levels (Incorrect)\n" +
                        "The cerebrospinal fluid is regulated by the choroid plexus, not the blood-brain barrier.\n\n" +
                        "To remove metabolic waste from the brain (Incorrect)\n" +
                        "The removal of metabolic waste is mainly handled by the lymphatic system, particularly the glymphatic system.\n\n" +
                        "To maintain electrolyte balance in the brain (Incorrect)\n" +
                        "While the blood-brain barrier helps maintain a stable environment, electrolyte balance is more directly regulated by ion channels and transporters in neurons."
        );

        questions.add("Which of the following ions is primarily responsible for repolarization of the neuron during an action potential?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Sodium (Na⁺)",
                "Potassium (K⁺)", // Correct answer
                "Chloride (Cl⁻)",
                "Calcium (Ca²⁺)"
        )));
        correctAnswers.add("Potassium (K⁺)");
        rationales.put(82,
                "RATIONALE:\n" +
                        "Potassium (K⁺) (Correct answer)\n" +
                        "During repolarization, potassium ions flow out of the neuron, returning the membrane potential to a more negative value.\n\n" +
                        "Sodium (Na⁺) (Incorrect)\n" +
                        "Sodium ions are responsible for depolarization, not repolarization.\n\n" +
                        "Chloride (Cl⁻) (Incorrect)\n" +
                        "Chloride ions are involved in inhibitory postsynaptic potentials, not repolarization.\n\n" +
                        "Calcium (Ca²⁺) (Incorrect)\n" +
                        "Calcium ions are important for neurotransmitter release and muscle contraction but are not directly involved in repolarization."
        );

        questions.add("Which of the following is the main neurotransmitter in the parasympathetic nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Norepinephrine",
                "Acetylcholine", // Correct answer
                "Dopamine",
                "Glutamate"
        )));
        correctAnswers.add("Acetylcholine");
        rationales.put(83,
                "RATIONALE:\n" +
                        "Acetylcholine (Correct answer)\n" +
                        "Acetylcholine is the primary neurotransmitter in the parasympathetic nervous system, facilitating 'rest and digest' functions.\n\n" +
                        "Norepinephrine (Incorrect)\n" +
                        "Norepinephrine is the primary neurotransmitter in the sympathetic nervous system, not the parasympathetic system.\n\n" +
                        "Dopamine (Incorrect)\n" +
                        "Dopamine is involved in reward, mood regulation, and movement control, not parasympathetic functions.\n\n" +
                        "Glutamate (Incorrect)\n" +
                        "Glutamate is the main excitatory neurotransmitter in the central nervous system, not specific to the parasympathetic nervous system."
        );

        questions.add("What is the role of Schwann cells in the peripheral nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To generate action potentials",
                "To provide myelination to axons", // Correct answer
                "To support neurotransmitter release",
                "To remove cellular debris"
        )));
        correctAnswers.add("To provide myelination to axons");
        rationales.put(84,
                "RATIONALE:\n" +
                        "To provide myelination to axons (Correct answer)\n" +
                        "Schwann cells myelinate axons in the peripheral nervous system, speeding up nerve impulse conduction.\n\n" +
                        "To generate action potentials (Incorrect)\n" +
                        "Schwann cells do not generate action potentials; they support neurons by forming myelin.\n\n" +
                        "To support neurotransmitter release (Incorrect)\n" +
                        "Neurotransmitter release is managed by neurons, not Schwann cells.\n\n" +
                        "To remove cellular debris (Incorrect)\n" +
                        "Removing cellular debris is primarily the role of microglia in the central nervous system, not Schwann cells."
        );

        questions.add("Which of the following areas of the brain is primarily responsible for controlling language comprehension?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Broca’s area",
                "Wernicke’s area", // Correct answer
                "Medulla oblongata",
                "Hippocampus"
        )));
        correctAnswers.add("Wernicke’s area");
        rationales.put(85,
                "RATIONALE:\n" +
                        "Wernicke’s area (Correct answer)\n" +
                        "Wernicke’s area, located in the left temporal lobe, is responsible for language comprehension.\n\n" +
                        "Broca’s area (Incorrect)\n" +
                        "Broca's area is responsible for speech production, not comprehension.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata controls autonomic functions, not language comprehension.\n\n" +
                        "Hippocampus (Incorrect)\n" +
                        "The hippocampus is involved in memory formation, not language comprehension."
        );

        questions.add("Which of the following statements about the action potential is correct?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It is a graded response that varies in magnitude",
                "It always causes the same magnitude of depolarization regardless of the stimulus strength", // Correct answer
                "It can be influenced by the frequency of stimuli",
                "It decreases in strength as it travels along the axon"
        )));
        correctAnswers.add("It always causes the same magnitude of depolarization regardless of the stimulus strength");
        rationales.put(86,
                "RATIONALE:\n" +
                        "It always causes the same magnitude of depolarization regardless of the stimulus strength (Correct answer)\n" +
                        "The action potential follows the 'all-or-nothing' principle, meaning that once it reaches the threshold, it will always have the same magnitude of depolarization.\n\n" +
                        "It is a graded response that varies in magnitude (Incorrect)\n" +
                        "An action potential is an all-or-nothing response; it does not vary in magnitude.\n\n" +
                        "It can be influenced by the frequency of stimuli (Incorrect)\n" +
                        "The action potential itself is an all-or-nothing event, but the frequency of action potentials can affect the overall response.\n\n" +
                        "It decreases in strength as it travels along the axon (Incorrect)\n" +
                        "The action potential does not decrease in strength as it travels; this is a feature of graded potentials."
        );

        questions.add("Which part of the nervous system is responsible for controlling voluntary muscle movements?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Autonomic nervous system",
                "Somatic nervous system", // Correct answer
                "Sympathetic nervous system",
                "Parasympathetic nervous system"
        )));
        correctAnswers.add("Somatic nervous system");
        rationales.put(87,
                "RATIONALE:\n" +
                        "Somatic nervous system (Correct answer)\n" +
                        "The somatic nervous system controls voluntary movements, including those of skeletal muscles.\n\n" +
                        "Autonomic nervous system (Incorrect)\n" +
                        "The autonomic nervous system controls involuntary functions, such as heart rate and digestion.\n\n" +
                        "Sympathetic nervous system (Incorrect)\n" +
                        "The sympathetic nervous system is involved in the 'fight or flight' response, not voluntary muscle control.\n\n" +
                        "Parasympathetic nervous system (Incorrect)\n" +
                        "The parasympathetic nervous system is responsible for 'rest and digest' functions, not voluntary movements."
        );

        questions.add("What is the function of oligodendrocytes in the central nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To support the blood-brain barrier",
                "To produce cerebrospinal fluid",
                "To myelinate axons", // Correct answer
                "To remove waste and debris"
        )));
        correctAnswers.add("To myelinate axons");
        rationales.put(88,
                "RATIONALE:\n" +
                        "To myelinate axons (Correct answer)\n" +
                        "Oligodendrocytes myelinate axons in the central nervous system, increasing the speed of action potential conduction.\n\n" +
                        "To support the blood-brain barrier (Incorrect)\n" +
                        "The blood-brain barrier is supported by endothelial cells and astrocytes, not oligodendrocytes.\n\n" +
                        "To produce cerebrospinal fluid (Incorrect)\n" +
                        "Cerebrospinal fluid is produced by the choroid plexus, not oligodendrocytes.\n\n" +
                        "To remove waste and debris (Incorrect)\n" +
                        "Microglia are responsible for removing waste and debris, not oligodendrocytes."
        );

        questions.add("Which of the following structures regulates the sleep-wake cycle?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Pineal gland", // Correct answer
                "Thalamus",
                "Hippocampus",
                "Cerebellum"
        )));
        correctAnswers.add("Pineal gland");
        rationales.put(89,
                "RATIONALE:\n" +
                        "Pineal gland (Correct answer)\n" +
                        "The pineal gland secretes melatonin, which helps regulate the sleep-wake cycle.\n\n" +
                        "Thalamus (Incorrect)\n" +
                        "The thalamus is involved in sensory processing and relay, not directly regulating sleep.\n\n" +
                        "Hippocampus (Incorrect)\n" +
                        "The hippocampus is involved in memory formation, not sleep regulation.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum coordinates motor functions but does not regulate sleep."
        );

        questions.add("Which type of glial cell is responsible for the formation of the blood-brain barrier?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Astrocytes", // Correct answer
                "Oligodendrocytes",
                "Microglia",
                "Schwann cells"
        )));
        correctAnswers.add("Astrocytes");
        rationales.put(90,
                "RATIONALE:\n" +
                        "Astrocytes (Correct answer)\n" +
                        "Astrocytes contribute to the blood-brain barrier by regulating the permeability of blood vessels and forming tight junctions.\n\n" +
                        "Oligodendrocytes (Incorrect)\n" +
                        "Oligodendrocytes are responsible for myelinating axons in the central nervous system, not forming the blood-brain barrier.\n\n" +
                        "Microglia (Incorrect)\n" +
                        "Microglia are involved in immune responses and debris removal, not the blood-brain barrier.\n\n" +
                        "Schwann cells (Incorrect)\n" +
                        "Schwann cells myelinate axons in the peripheral nervous system, not the blood-brain barrier."
        );

        questions.add("What is the primary function of the cerebellum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Coordinate voluntary muscle movements and balance", // Correct answer
                "Regulate emotion and behavior",
                "Control autonomic functions",
                "Process sensory information from the body"
        )));
        correctAnswers.add("Coordinate voluntary muscle movements and balance");
        rationales.put(91,
                "RATIONALE:\n" +
                        "Coordinate voluntary muscle movements and balance (Correct answer)\n" +
                        "The cerebellum plays a key role in coordinating voluntary movements, maintaining posture, and balance.\n\n" +
                        "Regulate emotion and behavior (Incorrect)\n" +
                        "The cerebrum, not the cerebellum, is primarily responsible for emotions and behavior.\n\n" +
                        "Control autonomic functions (Incorrect)\n" +
                        "The brainstem is responsible for autonomic functions like heart rate and breathing.\n\n" +
                        "Process sensory information from the body (Incorrect)\n" +
                        "Sensory information is processed by the thalamus and other parts of the brain, not the cerebellum."
        );

        questions.add("Which of the following statements best describes the role of the medulla oblongata?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It controls vital autonomic functions like heart rate and breathing", // Correct answer
                "It regulates emotions and memory",
                "It processes sensory and motor signals from the body",
                "It is involved in motor coordination and balance"
        )));
        correctAnswers.add("It controls vital autonomic functions like heart rate and breathing");
        rationales.put(92,
                "RATIONALE:\n" +
                        "It controls vital autonomic functions like heart rate and breathing (Correct answer)\n" +
                        "The medulla oblongata regulates essential autonomic functions such as breathing, heart rate, and blood pressure.\n\n" +
                        "It regulates emotions and memory (Incorrect)\n" +
                        "Emotional regulation and memory are primarily associated with the limbic system and hippocampus.\n\n" +
                        "It processes sensory and motor signals from the body (Incorrect)\n" +
                        "The thalamus processes sensory and motor signals, not the medulla.\n\n" +
                        "It is involved in motor coordination and balance (Incorrect)\n" +
                        "Motor coordination and balance are managed by the cerebellum, not the medulla oblongata."
        );

        questions.add("What is the function of the hippocampus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Store and process memories", // Correct answer
                "Regulate the sleep-wake cycle",
                "Control motor coordination",
                "Regulate heart rate"
        )));
        correctAnswers.add("Store and process memories");
        rationales.put(93,
                "RATIONALE:\n" +
                        "Store and process memories (Correct answer)\n" +
                        "The hippocampus plays a central role in forming, storing, and processing memories.\n\n" +
                        "Regulate the sleep-wake cycle (Incorrect)\n" +
                        "The pineal gland regulates the sleep-wake cycle, not the hippocampus.\n\n" +
                        "Control motor coordination (Incorrect)\n" +
                        "Motor coordination is controlled by the cerebellum.\n\n" +
                        "Regulate heart rate (Incorrect)\n" +
                        "Heart rate regulation is handled by the medulla oblongata, not the hippocampus."
        );

        questions.add("What type of receptor is responsible for detecting pain?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Nociceptors", // Correct answer
                "Thermoreceptors",
                "Photoreceptors",
                "Mechanoreceptors"
        )));
        correctAnswers.add("Nociceptors");
        rationales.put(94,
                "RATIONALE:\n" +
                        "Nociceptors (Correct answer)\n" +
                        "Nociceptors are sensory receptors that detect painful stimuli.\n\n" +
                        "Thermoreceptors (Incorrect)\n" +
                        "Thermoreceptors detect temperature changes, not pain.\n\n" +
                        "Photoreceptors (Incorrect)\n" +
                        "Photoreceptors are responsible for detecting light and vision.\n\n" +
                        "Mechanoreceptors (Incorrect)\n" +
                        "Mechanoreceptors detect pressure, vibration, and stretch, but not pain specifically."
        );

        questions.add("Which of the following is the primary function of the thalamus?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Process and relay sensory information to the cortex", // Correct answer
                "Control voluntary muscle movements",
                "Regulate heart rate and respiration",
                "Control emotional responses"
        )));
        correctAnswers.add("Process and relay sensory information to the cortex");
        rationales.put(95,
                "RATIONALE:\n" +
                        "Process and relay sensory information to the cortex (Correct answer)\n" +
                        "The thalamus serves as a relay station for sensory signals, sending them to the appropriate areas of the cerebral cortex for processing.\n\n" +
                        "Control voluntary muscle movements (Incorrect)\n" +
                        "Voluntary muscle movements are controlled by the motor cortex, not the thalamus.\n\n" +
                        "Regulate heart rate and respiration (Incorrect)\n" +
                        "The medulla oblongata regulates heart rate and respiration, not the thalamus.\n\n" +
                        "Control emotional responses (Incorrect)\n" +
                        "Emotional responses are regulated by the limbic system, not the thalamus."
        );

        questions.add("Which part of the brain is responsible for the regulation of body temperature?");
        choices.add(new ArrayList<>(Arrays.asList(
                "Hypothalamus", // Correct answer
                "Medulla oblongata",
                "Pons",
                "Cerebellum"
        )));
        correctAnswers.add("Hypothalamus");
        rationales.put(96,
                "RATIONALE:\n" +
                        "Hypothalamus (Correct answer)\n" +
                        "The hypothalamus is responsible for regulating body temperature, hunger, thirst, and other homeostatic processes.\n\n" +
                        "Medulla oblongata (Incorrect)\n" +
                        "The medulla oblongata controls vital functions like breathing and heart rate but not body temperature.\n\n" +
                        "Pons (Incorrect)\n" +
                        "The pons primarily regulates sleep and arousal, not body temperature.\n\n" +
                        "Cerebellum (Incorrect)\n" +
                        "The cerebellum coordinates motor functions and balance, not body temperature."
        );

        questions.add("What is the primary function of the sympathetic nervous system?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To prepare the body for \"fight or flight\" responses", // Correct answer
                "To promote rest and digestion",
                "To control voluntary muscle movements",
                "To regulate sleep and wakefulness"
        )));
        correctAnswers.add("To prepare the body for \"fight or flight\" responses");
        rationales.put(97,
                "RATIONALE:\n" +
                        "To prepare the body for \"fight or flight\" responses (Correct answer)\n" +
                        "The sympathetic nervous system activates the body's \"fight or flight\" response, increasing heart rate, dilating pupils, and redirecting blood flow to muscles.\n\n" +
                        "To promote rest and digestion (Incorrect)\n" +
                        "This is the function of the parasympathetic nervous system, not the sympathetic system.\n\n" +
                        "To control voluntary muscle movements (Incorrect)\n" +
                        "Voluntary muscle movements are controlled by the somatic nervous system, not the sympathetic system.\n\n" +
                        "To regulate sleep and wakefulness (Incorrect)\n" +
                        "Sleep-wake regulation is controlled by the hypothalamus and other brain structures."
        );

        questions.add("What is the function of the corpus callosum?");
        choices.add(new ArrayList<>(Arrays.asList(
                "To connect the left and right hemispheres of the brain", // Correct answer
                "To regulate breathing and heart rate",
                "To control voluntary movements",
                "To process visual information"
        )));
        correctAnswers.add("To connect the left and right hemispheres of the brain");
        rationales.put(98,
                "RATIONALE:\n" +
                        "To connect the left and right hemispheres of the brain (Correct answer)\n" +
                        "The corpus callosum is a bundle of nerve fibers that connects the left and right hemispheres of the brain, allowing communication between them.\n\n" +
                        "To regulate breathing and heart rate (Incorrect)\n" +
                        "Breathing and heart rate regulation are controlled by the brainstem, not the corpus callosum.\n\n" +
                        "To control voluntary movements (Incorrect)\n" +
                        "Voluntary movements are controlled by the motor cortex, not the corpus callosum.\n\n" +
                        "To process visual information (Incorrect)\n" +
                        "Visual processing occurs in the occipital lobe, not the corpus callosum."
        );

        questions.add("What is the role of the sympathetic nervous system during a stressful situation?");
        choices.add(new ArrayList<>(Arrays.asList(
                "It stimulates the \"fight or flight\" response, preparing the body for action", // Correct answer
                "It decreases heart rate and promotes digestion",
                "It promotes rest and recovery",
                "It enhances sleep and relaxation"
        )));
        correctAnswers.add("It stimulates the \"fight or flight\" response, preparing the body for action");
        rationales.put(99,
                "RATIONALE:\n" +
                        "It stimulates the \"fight or flight\" response, preparing the body for action (Correct answer)\n" +
                        "The sympathetic nervous system increases heart rate, dilates pupils, and redirects blood flow to muscles to prepare the body for action in response to stress.\n\n" +
                        "It decreases heart rate and promotes digestion (Incorrect)\n" +
                        "The parasympathetic nervous system is responsible for these functions, not the sympathetic system.\n\n" +
                        "It promotes rest and recovery (Incorrect)\n" +
                        "This function is handled by the parasympathetic nervous system.\n\n" +
                        "It enhances sleep and relaxation (Incorrect)\n" +
                        "Sleep and relaxation are promoted by the parasympathetic nervous system, not the sympathetic system."
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
        new AlertDialog.Builder(Prac_7_Nervous.this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? All progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();  // This will exit the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
