#ifndef AUDIOANALYZER_H
#define AUDIOANALYZER_H

#include "CircularBuffer.h"
#include <memory>

class AudioAnalyzer {
private:
    CircularBuffer<std::int16_t> buffer;
    std::unique_ptr<float[]> window;



public:
    AudioAnalyzer();
    ~AudioAnalyzer();

    void feed_data(short *data, int length);

    float compute_freq();
};

#endif //AUDIOANALYZER_H