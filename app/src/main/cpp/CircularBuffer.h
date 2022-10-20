#ifndef ALEXTUNER_CIRCULARBUFFER_H
#define ALEXTUNER_CIRCULARBUFFER_H

#include <memory>

template<class T>
class CircularBuffer {
private:
    std::unique_ptr<T[]> buffer;
    size_t head, tail, max_size;

public:
    CircularBuffer<T>(size_t max_size) :
            buffer(std::unique_ptr<T[]>(new T[max_size])),
            head(0), tail(0),
            max_size(max_size) {};

    /**
     * Add an item to this circular buffer
     * @param item Item to add
     */
    void enqueue(T item) {
        if (is_full())
            dequeue();

        // insert item at back of buffer
        buffer[tail] = item;
        // increment tail
        tail = (tail + 1) % max_size;
    }

    /**
     * Remove an item from this circular buffer and return it
     * @return Removed item
     */
    T dequeue() {
        // if buffer is empty, throw an error
        if (is_empty())
            throw std::runtime_error("buffer is empty");

        // get item at head
        T item = buffer[head];
        // move head forward
        head = (head + 1) % max_size;

        return item;
    }

    /**
     * @return the item at the front of this circular buffer
     */
    T front() {
        return buffer[head];
    }

    /**
     * @return true if this circular buffer is empty, and false otherwise
     */
    bool is_empty() {
        return head == tail;
    }

    /**
     * @return true if this circular buffer is full, and false otherwise
     */
    bool is_full() {
        return tail == (head - 1) % max_size;
    }
};


#endif
