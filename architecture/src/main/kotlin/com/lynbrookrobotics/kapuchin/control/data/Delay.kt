package com.lynbrookrobotics.kapuchin.control.data

/**
 * Delays values
 *
 * Adds new inputs to a queue, returning the oldest element
 *
 * @author Kunal
 *
 * @param T type of delayed values
 * @param lookBack must be greater than zero
 * @param lookBack size of value queue
 */
fun <T> delay(lookBack: Int): (T) -> T? {

    @Suppress("UNCHECKED_CAST") // this is a workaround
    val buffer = arrayOfNulls<Any>(lookBack) as Array<T?>

    var index: Int = 0

    return fun(newValue: T): T? {
        val oldestIndex = (index + 1) % lookBack
        return buffer[oldestIndex].also {
            buffer[index] = newValue
            index = oldestIndex
        }
    }
}