#ifndef MODULE_NAME
#define MODULE_NAME  "jni_bridge"
#endif

#include <jni.h>
#include "AudioRecorder.h"
#include "logging_macros.h"

const char *TAG = "jni_bridge:: %s";

static AudioRecorder *audioRecorder = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_tk_atna_audiorecorder_data_datasource_AudioRecorderDataSource_create(
        JNIEnv *env,
        jclass
) {
    LOGD(TAG, "create(): ");

    if (audioRecorder == nullptr) {
        audioRecorder = new AudioRecorder();
    }
    return (audioRecorder != nullptr);
}

extern "C" JNIEXPORT void JNICALL
Java_tk_atna_audiorecorder_data_datasource_AudioRecorderDataSource_delete(
        JNIEnv *env,
        jclass
) {
    LOGD(TAG, "delete(): ");

    delete audioRecorder;
    audioRecorder = nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_tk_atna_audiorecorder_data_datasource_AudioRecorderDataSource_startRecording(
        JNIEnv *env,
        jclass,
        jstring fullPathToFile
) {
    LOGD(TAG, "startRecording(): ");

    if (audioRecorder == nullptr) {
        LOGE(TAG, "audioRecorder is null, you must call create() method before calling this method");
        return;
    }
    const char *path = (*env).GetStringUTFChars(fullPathToFile, nullptr);
    audioRecorder->startRecording(path);
}

extern "C" JNIEXPORT void JNICALL
Java_tk_atna_audiorecorder_data_datasource_AudioRecorderDataSource_stopRecording(
        JNIEnv *env,
        jclass
) {
    LOGD(TAG, "stopRecording(): ");

    if (audioRecorder == nullptr) {
        LOGE(TAG, "audioRecorder is null, nothing to stop");
        return;
    }
    audioRecorder->stopRecording();
}