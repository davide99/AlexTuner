#include <jni.h>
#include <string>

extern "C" JNIEXPORT jfloat JNICALL
Java_com_davide99_alextuner_MainActivity_floatFromJNI(
        JNIEnv* env,
        jobject /* this */) {



    return 20.0f;
}