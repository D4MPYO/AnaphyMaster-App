package com.example.anaphymaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Edit_Choosing_Avatar extends AppCompatActivity {
    private EditText inputName;
    private TextView charCount;
    private Button nextButton, cancelButton;
    private ImageView selectedAvatar;
    private ImageView avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatar9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isEditing = getIntent().getBooleanExtra("IS_EDITING", false);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.edit_choosing_avatars);

        enableImmersiveMode();

        // Set the orientation to portrait for the input screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize EditText, TextView, and Button
        inputName = findViewById(R.id.inputName);
        charCount = findViewById(R.id.charCount);
        nextButton = findViewById(R.id.nextbutton);
        cancelButton = findViewById(R.id.cancelButton); // Initialize cancel button

        // Initialize avatar ImageViews
        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);
        avatar6 = findViewById(R.id.avatar6);
        avatar7 = findViewById(R.id.avatar7);
        avatar8 = findViewById(R.id.avatar8);
        avatar9 = findViewById(R.id.avatar9);

        // Add click listeners to each avatar
        avatar1.setOnClickListener(this::onAvatarSelected);
        avatar2.setOnClickListener(this::onAvatarSelected);
        avatar3.setOnClickListener(this::onAvatarSelected);
        avatar4.setOnClickListener(this::onAvatarSelected);
        avatar5.setOnClickListener(this::onAvatarSelected);
        avatar6.setOnClickListener(this::onAvatarSelected);
        avatar7.setOnClickListener(this::onAvatarSelected);
        avatar8.setOnClickListener(this::onAvatarSelected);
        avatar9.setOnClickListener(this::onAvatarSelected);

        // Check if the username is already set
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userName = preferences.getString("USER_NAME", null);

        if (!isEditing && userName != null) {
            navigateToHomeActivity(userName); // Only auto-redirect for new users
        }

        // TextWatcher to update character count
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(s.length() + "/25");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Button click listener for nextButton
        nextButton.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(Edit_Choosing_Avatar.this, "Please enter your name before proceeding.", Toast.LENGTH_SHORT).show();
            } else if (selectedAvatar == null) {
                Toast.makeText(Edit_Choosing_Avatar.this, "Please select an avatar.", Toast.LENGTH_SHORT).show();
            } else {
                savePreferences(name);
                // Show Toast for successful profile update
                Toast.makeText(Edit_Choosing_Avatar.this, "Profile has been updated!", Toast.LENGTH_SHORT).show();
                navigateToHomeActivity(name);
            }
        });

        // Button click listener for cancelButton
        cancelButton.setOnClickListener(v -> {
            // Show Toast for cancel action
            Toast.makeText(Edit_Choosing_Avatar.this, "Action cancelled", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity and return to the previous screen
        });
    }

    // Method to handle avatar selection
    private void onAvatarSelected(View view) {
        if (selectedAvatar != null) {
            // Reset previously selected avatar's selection state
            selectedAvatar.setSelected(false);
        }
        // Set the selected state for the clicked avatar
        view.setSelected(true);
        selectedAvatar = (ImageView) view; // Save the reference to the selected avatar
    }

    // Method to save preferences
    private void savePreferences(String name) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isUsernameSet", true);
        editor.putString("USER_NAME", name);
        editor.putInt("SELECTED_AVATAR", getSelectedAvatarResource()); // Save the selected avatar ID
        editor.apply();
    }

    // Method to get the resource ID of the selected avatar
    private int getSelectedAvatarResource() {
        if (selectedAvatar == avatar1) return R.drawable.avatar1; // Ensure these resources exist
        if (selectedAvatar == avatar2) return R.drawable.avatar2;
        if (selectedAvatar == avatar3) return R.drawable.avatar3;
        if (selectedAvatar == avatar4) return R.drawable.avatar4;
        if (selectedAvatar == avatar5) return R.drawable.avatar5;
        if (selectedAvatar == avatar6) return R.drawable.avatar6;
        if (selectedAvatar == avatar7) return R.drawable.avatar7;
        if (selectedAvatar == avatar8) return R.drawable.avatar8;
        if (selectedAvatar == avatar9) return R.drawable.avatar9;
        return R.drawable.avatar_icon; // Fallback if none selected
    }

    private void navigateToHomeActivity(String name) {
        Intent intent = new Intent(Edit_Choosing_Avatar.this, MainActivity.class);
        intent.putExtra("USER_NAME", name);
        startActivity(intent);
        finish();
    }

    private void enableImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}
