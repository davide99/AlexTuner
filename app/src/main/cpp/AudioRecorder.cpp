#include <oboe/Oboe.h>
#include "AudioRecorder.h"
#include "logging_macros.h"
#include <fstream>


AudioRecorder::AudioRecorder() {
}

AudioRecorder::~AudioRecorder() {
    stopStream();
}

void AudioRecorder::startRecording(const char *fullPathToFile) {
    LOGD(TAG, "startRecording(): ");

    openStream();
    if (mStream) {
        startStream(fullPathToFile);
    } else {
        LOGE(TAG, "startRecording(): Failed to create (%p) stream", mStream);
        stopRecording();
    }
}

void AudioRecorder::openStream() {
    LOGD(TAG, "openStream(): ");

    oboe::AudioStreamBuilder builder;
    builder.setDeviceId(mRecordingDeviceId);
    builder.setDirection(oboe::Direction::Input);
    builder.setFormat(mFormat);
    builder.setSampleRate(mSampleRate);
    builder.setChannelCount(oboe::ChannelCount::Mono);

    oboe::Result result = builder.openStream(&mStream);
    if (result == oboe::Result::OK && mStream) {
        mRecordingDeviceId = mStream->getDeviceId();
        mSampleRate = mStream->getSampleRate();
        LOGV(TAG, "sample rate = ");
        LOGV(TAG, std::to_string(mSampleRate).c_str());
        mFormat = mStream->getFormat();
        LOGV(TAG, "format = ");
        LOGV(TAG, oboe::convertToText(mFormat));
        mRequestedFrames = discoverRequestedFrames();
        LOGD(TAG, "requested frames = ");
        LOGD(TAG, std::to_string(mRequestedFrames).c_str());
    } else {
        LOGE(TAG, "Failed to create recording stream. Error: ");
        LOGE(TAG, oboe::convertToText(result));
    }
}

void AudioRecorder::startStream(const char *fullPathToFile) {
    LOGD(TAG, "startStream(): ");

    if (mStream) {
        mRecording = true;
        oboe::Result result = mStream->start();
        if (result != oboe::Result::OK) {
            LOGE(TAG, "error starting stream: ");
            LOGE(TAG, oboe::convertToText(result));
            return;
        }

        std::ofstream fileStream = prepareFileStream(fullPathToFile);
        // Write the data chunk header
        size_t dataChunkPos = fileStream.tellp();
        fileStream << "data----";  // (chunk size to be filled in later)
        // fileStream.flush();

        auto state = mStream->getState();
        if (state == oboe::StreamState::Started) {

            mSampleBuffer = new int16_t[mRequestedFrames]{0};

            // prepare delay
            mFramesDelta = (int) (mMillisecondsDelayTime * (mSampleRate / oboe::kMillisPerSecond));
            mDelayBuffer = new int16_t[mFramesDelta]{0};

            // skip silent frames at start
            int emptyFramesRead = 0;
            do {
                auto readResult = mStream->read(mSampleBuffer, mRequestedFrames, 0);
                if (readResult != oboe::Result::OK) {
                    break;
                }
                emptyFramesRead = readResult.value();
                if (emptyFramesRead > 0) {
                    break;
                }
            } while (emptyFramesRead == 0);

            // do read, delay and save samples
            do {
                auto readResult = mStream->read(mSampleBuffer, mRequestedFrames, mReadTimeout);
                if (readResult == oboe::Result::OK) {
                    auto framesRead = readResult.value();
                    for (int i = 0; i < framesRead; i++) {
                        auto sample = applyDelay(mSampleBuffer[i]);
                        writeWord(fileStream, (int) (sample), 2);
                    }
                } else {
                    LOGD(TAG, "error reading from stream: ");
                    LOGD(TAG, convertToText(readResult.error()));
                }
            } while (mRecording);

            finalizeFileStream(fileStream, dataChunkPos);
        }
    }
}

int32_t AudioRecorder::discoverRequestedFrames() const {
    return (int32_t) (mMillisecondsToRecord * (mSampleRate / oboe::kMillisPerSecond));
}

short AudioRecorder::applyDelay(short sample) {
    int readIndex = mDelayIndex - mFramesDelta + 1;
    if (readIndex < 0) {
        readIndex += mFramesDelta;
    }

    short delaySample = mDelayBuffer[readIndex];
    mDelayBuffer[mDelayIndex] = sample + delaySample * mFeedback;

    if (++mDelayIndex == mFramesDelta) {
        mDelayIndex = 0;
    }
    sample += (delaySample * mMix * (1.5f - mFeedback));
    return sample;
}

std::ofstream AudioRecorder::prepareFileStream(const char *fullPathToFile) {
    const char *path = fullPathToFile;
    int bitsPerSample = 16; // multiple of 8
    int numChannels = 1; // 2 for stereo, 1 for mono

    std::ofstream fileStream;
    fileStream.open(path, std::ios::binary);
    // Write the file headers
    fileStream << "RIFF----WAVEfmt ";     // (chunk size to be filled in later)
    writeWord(fileStream, 16, 4);  // no extension data
    writeWord(fileStream, 1, 2);  // PCM - integer samples
    writeWord(fileStream, numChannels, 2);  // one channel (mono) or two channels (stereo file)
    writeWord(fileStream, mSampleRate, 4);  // samples per second (Hz)
    writeWord(fileStream, (mSampleRate * bitsPerSample * numChannels) / 8,
              4);  // (Sample Rate * BitsPerSample * Channels) / 8
    writeWord(fileStream, 4,
              2);  // data block size (size of two integer samples, one for each channel, in bytes)
    writeWord(fileStream, bitsPerSample, 2);  // number of bits per sample (use a multiple of 8)

    return fileStream;
}

void AudioRecorder::finalizeFileStream(std::ofstream &fileStream, size_t dataChunkPos) {
    // We'll need the final file size to fix the chunk sizes above
    size_t fileLength = fileStream.tellp();

    // Fix the data chunk header to contain the data size
    fileStream.seekp(dataChunkPos + 4);
    writeWord(fileStream, fileLength - dataChunkPos + 8);

    // Fix the file header to contain the proper RIFF chunk size, which is (file size - 8) bytes
    fileStream.seekp(0 + 4);
    writeWord(fileStream, fileLength - 8, 4);
    fileStream.close();
}

template<typename Word>
std::ostream &AudioRecorder::writeWord(std::ostream &outStream, Word value, unsigned size) {
    for (; size; --size, value >>= 8) {
        outStream.put(static_cast <char> (value & 0xFF));
    }
    return outStream;
}

void AudioRecorder::stopRecording() {
    LOGD(TAG, "stopRecording(): ");

    mRecording = false;
    stopStream();
}

void AudioRecorder::stopStream() {
    LOGD(TAG, "stopStream(): ");

    if (mStream) {
        oboe::Result result = mStream->stop(500000L);
        if (result == oboe::Result::OK) {
            closeStream();
        } else {
            LOGE(TAG, "error stopping stream: ");
            LOGE(TAG, oboe::convertToText(result));
        }
    }
}

void AudioRecorder::closeStream() {
    LOGD(TAG, "closeStream(): ");

    if (mStream) {
        oboe::Result result = mStream->close();
        if (result == oboe::Result::OK) {
            mStream = nullptr;

            delete mSampleBuffer;
            mSampleBuffer = nullptr;

            mFramesDelta = 0;
            mDelayIndex = 0;
            delete mDelayBuffer;
            mDelayBuffer = nullptr;
        } else {
            LOGE(TAG, "error closing stream: ");
            LOGE(TAG, oboe::convertToText(result));
        }
    }
}

