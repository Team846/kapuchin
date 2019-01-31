package com.lynbrookrobotics.kapuchin.timing.clock

import info.kunalsheth.units.generated.Time

/**
 * Global instance of `Clock` which the user manually `tick`s whenever appropriate
 *
 * Intended for non-realtime updates
 *
 * @author Kunal
 * @see Ticker
 * @see Clock
 */
object EventLoop : Clock {
    override val jobsToRun = mutableListOf<(tickStart: Time) -> Unit>()
    override val jobsToKill = mutableSetOf<(tickStart: Time) -> Unit>()
}