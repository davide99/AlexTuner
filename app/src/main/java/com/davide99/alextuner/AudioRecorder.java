package com.davide99.alextuner;

public class AudioRecorder {
    static {
        System.loadLibrary("alextuner");
    }

    static public native void stopRecording();
    static public native void startRecording(String fullPathToFile);
    static public native void delete();
    static public native boolean create();
}
