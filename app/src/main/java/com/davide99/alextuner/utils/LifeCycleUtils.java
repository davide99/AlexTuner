package com.davide99.alextuner.utils;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LifeCycleUtils {
    public static void showToastAndExit(AppCompatActivity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.finishAffinity();
    }
}
