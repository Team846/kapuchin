package com.lynbrookrobotics.kapuchin.timing.monitoring

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker

class Monitoring(parent: Ticker, private val setJitterPin: (Boolean) -> Unit)
    : Named by Named("Monitoring", parent) {

    val jitterPulse = parent.runOnTick {
        setJitterPin(true)
        setJitterPin(false)
    }
}