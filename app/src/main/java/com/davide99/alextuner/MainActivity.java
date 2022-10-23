package com.davide99.alextuner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.davide99.alextuner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AudioRecord recorder;

    // Requesting permission to RECORD_AUDIO
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static final int SAMPLE_RATE = AudioAnalyzer.getSampleRate();
    private static final int CHUNK_SIZE = AudioAnalyzer.getChunkSize(); //Number of samples

    private boolean was_recording = false;

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                10 * AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        );

        AudioAnalyzer.init();

        Gauge gauge = binding.gauge;

        new Thread(() -> {
            recorder.startRecording();
            was_recording = true;
            short[] data = new short[CHUNK_SIZE];

            while (true) {
                recorder.read(data, 0, data.length);
                AudioAnalyzer.feedData(data);
                runOnUiThread(() -> gauge.setFrequency(AudioAnalyzer.getFreq()));
            }

            //recorder.stop();
        }).start();

        /*new Thread(() -> {
            while (true) {
                runOnUiThread(() -> tv.setText(Float.toString(AudioAnalyzer.getFreq())));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
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
}