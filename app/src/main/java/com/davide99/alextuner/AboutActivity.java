package com.davide99.alextuner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import com.davide99.alextuner.databinding.ActivityAboutBinding;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.licenzeBtn.setOnClickListener((View v) -> {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
        });

        binding.fftwLicense.setMovementMethod(LinkMovementMethod.getInstance());
    }
}