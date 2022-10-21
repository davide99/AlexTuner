package com.davide99.alextuner;

public class AudioAnalyzer {
    static {
        System.loadLibrary("alextuner");
    }

    static public native boolean init();
    static public native void destroy();
    static public native void feedData(short[] data);
    static public native float getFreq();
    static public native int getSampleRate();
    static public native int getChunkSize();
}
