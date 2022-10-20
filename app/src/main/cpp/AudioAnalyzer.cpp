#include "AudioAnalyzer.h"
#include <cmath>

constexpr auto CHUNK_SIZE = 1024; //Number of samples
constexpr auto BUFFER_TIMES = 50;
constexpr auto BUFFER_SIZE = CHUNK_SIZE * BUFFER_TIMES;


AudioAnalyzer::AudioAnalyzer() :
        buffer(BUFFER_SIZE),
        window(std::unique_ptr<float[]>(new float[BUFFER_SIZE])) {

    //Initially fill circular buffer with zeros
    while (!buffer.is_full())
        buffer.enqueue(0);

    //Compute the hanning window
    for (int i = 0; i < BUFFER_SIZE; ++i)
        window[i] = 0.5f - 0.5f * cosf(2.0f * (float) M_PI * (float) i / (BUFFER_SIZE - 1));
}

AudioAnalyzer::~AudioAnalyzer() {

}

/**
 * Metodo chiamato quando il microfono legge i nuovi dati
 * @param data array dei dati
 * @param length lunghezza array
 */
void AudioAnalyzer::feed_data(short *data, int length) {
    //Put new data into the circular buffer
    for (int i = 0; i < length; ++i)
        buffer.enqueue(data[i]);
}

float AudioAnalyzer::compute_freq() {
    return 0;
}
