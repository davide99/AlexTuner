#include "AudioAnalyzer.h"
#include <limits>
#include <android/log.h>

namespace q = cycfi::q;

static inline float short_to_float(short x) {
    return static_cast<float>(x - std::numeric_limits<short>::min()) /
           (std::numeric_limits<short>::max() - std::numeric_limits<short>::min()) *
           (1.0f - (-1.0f)) + (-1.0f);
}

/**
 * Metodo chiamato quando il microfono legge i nuovi dati
 * @param data array dei dati
 * @param length lunghezza array
 */
void AudioAnalyzer::feed_data(short *data, size_t length) {
    for (size_t i = 0; i < length; i++) {
        pd(short_to_float(data[i]));
    }
}

float AudioAnalyzer::get_freq() const {
    return pd.get_frequency();
}
