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
constexpr auto NUM_HPS = 3; //harmonic product spectrum


AudioAnalyzer::AudioAnalyzer() :
        buffer(BUFFER_SIZE),
        window(std::unique_ptr<float[]>(new float[BUFFER_SIZE])),
        //fft_input allocated only once to be faster
        fft_input(std::unique_ptr<float[]>(new float[FFT_INPUT_SIZE])),
        fft_out(std::unique_ptr<fftwf_complex[]>(new fftwf_complex[FFT_OUTPUT_SIZE])),
        fft_out_magnitude(std::unique_ptr<float[]>(new float[FFT_OUTPUT_SIZE])),
        fft_out_magnitude_copy(std::unique_ptr<float[]>(new float[FFT_OUTPUT_SIZE])),
        freq(0) {

    //Initially fill circular buffer with zeros
    for (int i = 0; i < BUFFER_SIZE; i++)
        buffer.enqueue(0);

    //Compute the hanning window
    for (int i = 0; i < BUFFER_SIZE; ++i)
        window[i] = 0.5f - 0.5f * std::cosf(2.0f * (float) M_PI * (float) i / (BUFFER_SIZE - 1));

    //Zero-out the padding in fft_input
    //TODO: provare con memset?
    for (int i = BUFFER_SIZE; i < FFT_INPUT_SIZE; i++)
        fft_input[i] = 0;

    //Inizialize fftw plan
    fft_plan = fftwf_plan_dft_r2c_1d(FFT_INPUT_SIZE, fft_input.get(), fft_out.get(), FFTW_ESTIMATE);
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

    //HPS: multiply data by itself with different scalings (Harmonic Product Spectrum)
    std::copy(
            fft_out_magnitude.get(), fft_out_magnitude.get() + FFT_OUTPUT_SIZE,
            fft_out_magnitude_copy.get());

    for (int i = 2; i <= NUM_HPS; i++) {
        auto hps_len = static_cast<int>(std::ceil(FFT_OUTPUT_SIZE / static_cast<float>(i)));

        for (int j = 0; j < hps_len; j++) {
            fft_out_magnitude.get()[j] *= fft_out_magnitude_copy.get()[i * j];
        }
    }

    //Set magnitudes of al frequencies below 60Hz to zero
    size_t above_60 = 60.0f * static_cast<float>(FFT_INPUT_SIZE) / static_cast<float>(SAMPLE_RATE);
    for (int i = 0; i < above_60; i++)
        fft_out_magnitude[i] = 0;

    //Find the maximum magnitude position in the array
    size_t pos_max = std::distance(
            fft_out_magnitude.get(),
            std::max_element(
                    fft_out_magnitude.get(), fft_out_magnitude.get() + FFT_OUTPUT_SIZE
            )
    );

    //Compute corresponding frequency
    freq = static_cast<float>(pos_max) * static_cast<float>(SAMPLE_RATE) /
           static_cast<float>(FFT_INPUT_SIZE);
}

float AudioAnalyzer::get_freq() const {
    return freq;
}

int AudioAnalyzer::get_sample_rate() {
    return SAMPLE_RATE;
}

int AudioAnalyzer::get_chunk_size() {
    return CHUNK_SIZE;
}
