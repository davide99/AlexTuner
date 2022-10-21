#include "AudioAnalyzer.h"
#include <cmath>
#include <algorithm>

constexpr auto CHUNK_SIZE = 1024; //Number of samples
constexpr auto BUFFER_TIMES = 50;
constexpr auto BUFFER_SIZE = CHUNK_SIZE * BUFFER_TIMES;
constexpr auto SAMPLE_RATE = 48000;
constexpr auto ZERO_PADDING = 3; //times the buffer length
constexpr auto FFT_INPUT_SIZE = BUFFER_SIZE * (1 + ZERO_PADDING);
constexpr auto FFT_OUTPUT_SIZE = FFT_INPUT_SIZE / 2 + 1;


AudioAnalyzer::AudioAnalyzer() :
        buffer(BUFFER_SIZE),
        window(std::unique_ptr<float[]>(new float[BUFFER_SIZE])),
        //fft_input allocated only once to be faster
        fft_input(std::unique_ptr<float[]>(new float[FFT_INPUT_SIZE])),
        fft_out(std::unique_ptr<fftwf_complex[]>(new fftwf_complex[FFT_OUTPUT_SIZE])),
        fft_out_magnitude(std::unique_ptr<float[]>(new float[FFT_OUTPUT_SIZE])),
        freq(0) {

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

    //Inizialize fftw plan
    fft_plan = fftwf_plan_dft_r2c_1d(BUFFER_SIZE, fft_input.get(), fft_out.get(), FFTW_ESTIMATE);
}

AudioAnalyzer::~AudioAnalyzer() {
    fftwf_destroy_plan(fft_plan);
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

    //Compute real fft on fft_input
    fftwf_execute(fft_plan);

    //Compute the magnitude
    std::transform(fft_out.get(), fft_out.get() + FFT_OUTPUT_SIZE, fft_out_magnitude.get(),
                   [](const fftwf_complex &i) -> float {
                       return std::sqrtf(i[0] * i[0] + i[1] * i[1]);
                   });

    //TODO: HPS

    //Ottengo frequenze
    float freqs[FFT_OUTPUT_SIZE];

    for (int i = 0; i < FFT_OUTPUT_SIZE; ++i) {
        freqs[i] = 2 * (float) i * (float) SAMPLE_RATE / FFT_OUTPUT_SIZE;
    }

    int pos_max = std::distance(fft_out_magnitude.get(), std::max_element(fft_out_magnitude.get(),
                                                                          fft_out_magnitude.get() +
                                                                          FFT_OUTPUT_SIZE));

    freq = freqs[pos_max];
}

float AudioAnalyzer::compute_freq() {
    return freq;
}

int AudioAnalyzer::get_sample_rate() {
    return SAMPLE_RATE;
}

int AudioAnalyzer::get_chunk_size() {
    return CHUNK_SIZE;
}
