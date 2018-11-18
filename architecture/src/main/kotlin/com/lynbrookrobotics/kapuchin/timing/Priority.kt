package com.lynbrookrobotics.kapuchin.timing

/**
 * Represents the importance of something being executed
 *
 * Intended for specifying JVM and OS thread priorities. `RealTime` is the highest priority possible.
 */
enum class Priority {
    Lowest, Low, Medium, High, Highest, RealTime
}