package com.davide99.alextuner;

public class Consts {
    public static final int FPS = 30;
    public static final int MILLIS_FPS = Math.round(1000.0f / FPS);

    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final float A4 = 440.0f;
    public static final float C0 = (float) (A4 * Math.pow(2, -4.75));

    public static final int SAMPLE_RATE = AudioAnalyzer.getSampleRate();
    public static final int CHUNK_SIZE = AudioAnalyzer.getChunkSize(); //Number of samples
}
