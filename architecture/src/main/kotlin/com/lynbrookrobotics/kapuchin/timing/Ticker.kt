package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import info.kunalsheth.units.generated.Time

expect class Ticker private constructor(parent: Named, priority: Priority, period: Time, name: String = "Ticker") : Named, Clock {
    fun waitOnTick(): Time

    companion object {
        fun Named.ticker(priority: Priority, period: Time, name: String = "Ticker"): Ticker
    }
}

