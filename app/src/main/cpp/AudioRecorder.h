#ifndef AUDIORECORDER_AUDIORECORDER_H
#define AUDIORECORDER_AUDIORECORDER_H

#ifndef MODULE_NAME
#define MODULE_NAME  "AudioRecorder"
#endif

#include <oboe/Definitions.h>
#include <oboe/AudioStream.h>
#include "logging_macros.h"


class AudioRecorder {

public:
    AudioRecorder();

    ~AudioRecorder();

    void startRecording(const char *fullPathToFile);

    void stopRecording();

private:
    const char *TAG = "AudioRecorder:: %s";

    bool mRecording = false;
    oboe::AudioStream *mStream = nullptr;

    // recording params
    int32_t mRecordingDeviceId = oboe::VoiceRecognition;
    oboe::AudioFormat mFormat = oboe::AudioFormat::I16;
    int32_t mSampleRate = 48000;

    const int mMillisecondsToRecord = 2;
    int32_t mRequestedFrames = discoverRequestedFrames();
    const int64_t mReadTimeout = 4 * oboe::kNanosPerMillisecond;
    int16_t *mSampleBuffer;

    // delay params
    int mMillisecondsDelayTime = 30;
    float mMix = 0.9f;
    float mFeedback = 0.9f;
    int mFramesDelta = 0;
    int16_t *mDelayBuffer;
    int mDelayIndex = 0;

    void openStream();

    void startStream(const char *fullPathToFile);

    int32_t discoverRequestedFrames() const;

    short applyDelay(short);

    template<typename Word>
    std::ostream &writeWord(std::ostream &outs, Word value, unsigned int size = sizeof(Word));

    std::ofstream prepareFileStream(const char *fullPathToFile);

    void finalizeFileStream(std::ofstream &fileStream, size_t dataChunkPos);

    void stopStream();

    void closeStream();
};

#endif //AUDIORECORDER_AUDIORECORDER_H