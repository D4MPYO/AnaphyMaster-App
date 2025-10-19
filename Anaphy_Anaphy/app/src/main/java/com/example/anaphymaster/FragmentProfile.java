package com.example.anaphymaster;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class FragmentProfile extends Fragment {

    private ImageView profileAvatar;
    private TextView usernameText;
    private View profileCardView, aboutCard, termsCard, privacyCard, rateCard, shareCard;

    public FragmentProfile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_four, container, false);

        // Initialize views
        profileAvatar = view.findViewById(R.id.ProfileAvatar);
        usernameText = view.findViewById(R.id.usernameText);
        profileCardView = view.findViewById(R.id.editProfile);
        aboutCard = view.findViewById(R.id.aboutCard);
        termsCard = view.findViewById(R.id.termsCard);
        privacyCard = view.findViewById(R.id.privacyCard);
        rateCard = view.findViewById(R.id.rateCard);
        shareCard = view.findViewById(R.id.shareCard);

        // Fetch data from SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userName = preferences.getString("USER_NAME", "Guest");
        int avatarResId = preferences.getInt("SELECTED_AVATAR", R.drawable.avatar_icon);

        // Set values to views
        usernameText.setText(userName);
        profileAvatar.setImageResource(avatarResId);

        // Set up the clickable profile card
        profileCardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Edit_Choosing_Avatar.class);
            intent.putExtra("IS_EDITING", true);
            startActivity(intent);
        });

        // Set up the clickable About Card
        aboutCard.setOnClickListener(v -> {
            // Navigate to AboutActivity
            Intent intent = new Intent(getActivity(), AboutApp.class);
            startActivity(intent);
        });

        // Set up the clickable Terms Card
        termsCard.setOnClickListener(v -> {
            // Navigate to TermsActivity
            Intent intent = new Intent(getActivity(), AboutTermsCondition.class);
            startActivity(intent);
        });

        // Set up the clickable Privacy Card
        privacyCard.setOnClickListener(v -> {
            // Navigate to PrivacyPolicyActivity
            Intent intent = new Intent(getActivity(), AboutPrivacy.class);
            startActivity(intent);
        });

        // Set up the clickable Rate Card
        rateCard.setOnClickListener(v -> {
            // Navigate to RateUsActivity
            Intent intent = new Intent(getActivity(), AboutRateApp.class);
            startActivity(intent);
        });

        // Set up the clickable Share Card
        shareCard.setOnClickListener(v -> {
            // The message you want to copy and share
            String shareMessage = "Check out this amazing app:\n\n" +
                    "Discover helpful resources and tools tailored just for you. Try it now! \n\n" +
                    "https://anaphymaster.netlify.app/";

            // Copy the message to clipboard
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("App Share Message", shareMessage);
            clipboard.setPrimaryClip(clip);

            // Show a toast to inform the user that the message is copied
            Toast.makeText(requireContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();

            // Intent to share the app
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share app via"));
        });


        return view;
    }
}
