#ifndef AUDIOANALYZER_H
#define AUDIOANALYZER_H

#include "CircularBuffer.h"
#include <memory>

class AudioAnalyzer {
private:
    CircularBuffer<std::int16_t> buffer;
    std::unique_ptr<float[]> window;
    std::unique_ptr<float[]> fft_input;


public:
    AudioAnalyzer();
    ~AudioAnalyzer();

    void feed_data(short *data, int length);

    float compute_freq();

    static int get_sample_rate();

    static int get_chunk_size();
};

#endif //AUDIOANALYZER_H