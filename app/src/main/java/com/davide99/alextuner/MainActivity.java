package com.davide99.alextuner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.davide99.alextuner.databinding.ActivityMainBinding;
import com.davide99.alextuner.utils.LifeCycleUtils;
import com.davide99.alextuner.utils.PermissionsManager;
import com.davide99.alextuner.utils.StyleUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private AudioRecord recorder;
    private boolean was_recording;
    private Thread recording_thread;
    private ActivityMainBinding binding;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StyleUtils.setDarkMode();
        StyleUtils.useTransparentStatusBarAndThen(this, binding.getRoot(), insets -> {
            binding.gauge.setPaddingTop(insets.top);
            binding.notes.setPaddingTop(insets.top);
        });

        was_recording = false;

        permissionsManager = new PermissionsManager(
                this,
                this::initializeRecorderAndStartThread,
                () -> LifeCycleUtils.showToastAndExit(this, "Permesso non concesso, esco"),
                Manifest.permission.RECORD_AUDIO
        );

        permissionsManager.requestPermissions();

        binding.aboutButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            MainActivity.this.startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    private void initializeRecorderAndStartThread() {
        recorder = new AudioRecord(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                        MediaRecorder.AudioSource.UNPROCESSED : MediaRecorder.AudioSource.DEFAULT,
                Consts.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                10 * AudioRecord.getMinBufferSize(Consts.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        );

        if (!AudioAnalyzer.init())
            return;

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
                        ),
                0, Consts.MILLIS_FPS, TimeUnit.MILLISECONDS
        );

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