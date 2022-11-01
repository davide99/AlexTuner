package com.davide99.alextuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;

import com.davide99.alextuner.databinding.ActivityMainBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private AudioRecord recorder;
    private boolean was_recording = false;
    private Thread recording_thread;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionToRecordAccepted = false;

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permissionToRecordAccepted) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.aboutButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            MainActivity.this.startActivity(intent);
        });

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                Consts.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                10 * AudioRecord.getMinBufferSize(Consts.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        );

        AudioAnalyzer.init();

        recording_thread = new Thread(() -> {
            recorder.startRecording();
            was_recording = true;
            short[] data = new short[Consts.CHUNK_SIZE];

            while (!Thread.currentThread().isInterrupted()) {
                recorder.read(data, 0, data.length);
                AudioAnalyzer.feedData(data);
            }
        });
        recording_thread.start();

        Gauge gauge = binding.gauge;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() ->
                runOnUiThread(
                        () -> gauge.setFrequency(AudioAnalyzer.getFreq())
                ), 0, Consts.MILLIS_FPS, TimeUnit.MILLISECONDS);

        binding.notes.setNotes(new String[]{"E2", "A2", "D3", "G3", "B3", "E4"});
        gauge.setListener(binding.notes::setTuned);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (was_recording)
            recorder.startRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (was_recording)
            recorder.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (was_recording)
            recorder.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recording_thread.interrupt();
        try {
            recording_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AudioAnalyzer.destroy();
    }
}