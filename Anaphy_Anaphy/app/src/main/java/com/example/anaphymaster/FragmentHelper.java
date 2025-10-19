package com.example.anaphymaster;

import androidx.fragment.app.Fragment;

public class FragmentHelper {
    public static Fragment getFragmentByIndex(int index) {
        switch (index) {
            case 0:
                return new FragmentHome();
            case 1:
                return new FragmentProfile();
            case 2:
                return new FragmentLeaderboard();
            case 3:
                return new FragmentSocial();
            default:
                return new FragmentHome();
        }
    }
}
