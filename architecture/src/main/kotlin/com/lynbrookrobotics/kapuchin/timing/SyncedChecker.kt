package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.control.TimeStamped
import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

fun Named.checkInSync(threshold: Time, vararg sensorReadings: TimeStamped<*>): Boolean {
    val timings = sensorReadings.map(TimeStamped<*>::x)
            .let { if (it.isEmpty()) listOf(0.Second) else it }

    return (timings.max()!! - timings.min()!! < threshold).also {
        if (!it) log(Warning) { "$name sensor readings are out of sync.\nthreshold: $threshold\ntimings: $timings" }
    }
}