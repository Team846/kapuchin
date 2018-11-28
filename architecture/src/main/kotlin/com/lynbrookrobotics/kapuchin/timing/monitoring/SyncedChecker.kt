package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.control.data.TimeStamped
import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

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
        if (!it) log(Warning) { "$name sensor readings are out of sync.\ntolerance: $tolerance\ntimings: $timings" }
    }
}