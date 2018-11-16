package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import info.kunalsheth.units.generated.Time

/**
 * A real-time `Clock`
 *
 * Intended for consistent, periodic updates triggered by timer interrupts
 *
 * @author Kunal
 * @see PlatformThread
 * @see EventLoop
 * @see Clock
 */
expect class Ticker private constructor(parent: Named, priority: Priority, period: Time, name: String = "Ticker") : Named, Clock {
    fun waitOnTick(): Time

    companion object {
        /**
         * `Ticker` initializer
         *
         * @receiver owner of the ticker
         * @param priority importance
         * @param period time between ticks
         * @param name logging name
         * @return new thread running the given function
         */
        fun Named.ticker(priority: Priority, period: Time, name: String = "Ticker"): Ticker
    }
}

