package com.davide99.alextuner.utils;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LifeCycleUtils {
    public static void showToastAndExit(AppCompatActivity activity) {
        Toast.makeText(activity, "Permesso non concesso, esco", Toast.LENGTH_SHORT).show();
        activity.finishAffinity();
    }
}
