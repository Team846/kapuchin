package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.LogLevel.*
import info.kunalsheth.units.generated.*

/**
 * Checks if the domain of `TimeStamped` values is small enough
 *
 * Intended for control code to verify that all sensor inputs are up-to-date
 *
 * @author Kunal
 * @see Sensor
 * @see TimeStamped
 *
 * @param tolerance maximum domain
 * @param sensorReadings set of `TimeStamped` values to check
 * @return if the `sensorReadings` were taken at about the same time
 */
fun Named.checkInSync(tolerance: Time, vararg sensorReadings: TimeStamped<*>): Boolean {
    val timings = sensorReadings.map(TimeStamped<*>::x)
        .let { if (it.isEmpty()) listOf(0.Second) else it }

    return (timings.max()!! - timings.min()!! < tolerance).also {
        if (!it) log(WARN) { "$name sensor readings are out of sync.\ntolerance: $tolerance\ntimings: $timings" }
    }
}