#ifndef AUDIOANALYZER_H
#define AUDIOANALYZER_H

#include <cstdint>
#include <q/pitch/pitch_detector.hpp>

namespace q = cycfi::q;

class AudioAnalyzer {
public:
    static constexpr auto SAMPLE_RATE = 44100.0f;
    static constexpr auto LOWEST_FREQ = 60.0f;
    static constexpr auto HIGHEST_FREQ = 500.0f;
    static constexpr auto HYSTERESIS_DB = -45.0f;
    static constexpr auto CHUNK_SIZE = 256;

private:
    q::pitch_detector pd{LOWEST_FREQ, HIGHEST_FREQ, SAMPLE_RATE, HYSTERESIS_DB};

public:
    AudioAnalyzer() = default;

    ~AudioAnalyzer() = default;

    void feed_data(short *data, size_t length);

    float get_freq() const;
};

#endif //AUDIOANALYZER_H