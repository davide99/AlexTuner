package com.davide99.alextuner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.davide99.alextuner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AudioAnalyzer.init();
        AudioAnalyzer.feedData(new short[]{10, 20, 30, 40});

        TextView tv = binding.sampleText;
        tv.setText(Float.toString(AudioAnalyzer.computeFreq()));
    }
}