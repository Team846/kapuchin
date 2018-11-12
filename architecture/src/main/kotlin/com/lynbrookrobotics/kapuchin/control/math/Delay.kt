package com.lynbrookrobotics.kapuchin.control.math

/**
 * Delays values
 *
 * Adds new inputs to a queue, returning the oldest element
 *
 * @authors Kunal
 *
 * @param T type of delayed values
 * @param lookBack must be greater than zero
 *
 * @property lookBack size of value queue
 */
class Delay<T>(val lookBack: Int) : (T) -> T? {

    @Suppress("UNCHECKED_CAST") // this is a workaround
    private val buffer = arrayOfNulls<Any>(lookBack) as Array<T?>

    private var index: Int = 0

    override fun invoke(newValue: T): T? {
        val oldestIndex = (index + 1) % lookBack
        return buffer[oldestIndex].also {
            buffer[index] = newValue
            index = oldestIndex
        }
    }
}