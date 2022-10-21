#include "AudioAnalyzer.h"
#include <cmath>

constexpr auto CHUNK_SIZE = 1024; //Number of samples
constexpr auto BUFFER_TIMES = 50;
constexpr auto BUFFER_SIZE = CHUNK_SIZE * BUFFER_TIMES;
constexpr auto SAMPLE_RATE = 48000;
constexpr auto ZERO_PADDING = 3; //times the buffer length
constexpr auto FFT_INPUT_SIZE = BUFFER_SIZE * (1 + ZERO_PADDING);


AudioAnalyzer::AudioAnalyzer() :
        buffer(BUFFER_SIZE),
        window(std::unique_ptr<float[]>(new float[BUFFER_SIZE])),
        //fft_input allocated only once to be faster
        fft_input(std::unique_ptr<float[]>(new float[FFT_INPUT_SIZE])) {

    //Initially fill circular buffer with zeros
    for (int i = 0; i < BUFFER_SIZE; i++)
        buffer.enqueue(0);

    //Compute the hanning window
    for (int i = 0; i < BUFFER_SIZE; ++i)
        window[i] = 0.5f - 0.5f * cosf(2.0f * (float) M_PI * (float) i / (BUFFER_SIZE - 1));

    //Zero-out the padding in fft_input
    //TODO: provare con memset?
    for (int i = BUFFER_SIZE; i < FFT_INPUT_SIZE; i++)
        fft_input[i] = 0;
}

AudioAnalyzer::~AudioAnalyzer() {

}

/**
 * Metodo chiamato quando il microfono legge i nuovi dati
 * @param data array dei dati
 * @param length lunghezza array
 */
void AudioAnalyzer::feed_data(short *data, int length) {
    //Append data to audio buffer
    for (int i = 0; i < length; ++i)
        buffer.enqueue(data[i]);

    //Multiply the window by the input
    for (int i = 0; i < BUFFER_SIZE; i++)
        fft_input[i] = (float) buffer.get(i) * window[i];
}

float AudioAnalyzer::compute_freq() {
    return rand();
}

int AudioAnalyzer::get_sample_rate() {
    return SAMPLE_RATE;
}

int AudioAnalyzer::get_chunk_size() {
    return CHUNK_SIZE;
}
