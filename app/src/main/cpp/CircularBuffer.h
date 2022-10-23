#ifndef ALEXTUNER_CIRCULARBUFFER_H
#define ALEXTUNER_CIRCULARBUFFER_H

#include <memory>

template<class T>
class CircularBuffer {
private:
    std::unique_ptr<T[]> buffer;
    std::size_t last, max_size;

public:
    explicit CircularBuffer<T>(size_t max_size) :
            buffer(std::unique_ptr<T[]>(new T[max_size])),
            last(0),
            max_size(max_size) {};

    /**
     * Add an item to this circular buffer
     * @param item Item to add
     */
    void enqueue(T item) {
        // insert item at back of buffer
        buffer[last] = item;
        // increment last
        last = (last + 1) % max_size;
    }

    inline const T &get(const std::size_t &pos) {
        return buffer[(last + pos) % max_size];
    }
};


#endif
