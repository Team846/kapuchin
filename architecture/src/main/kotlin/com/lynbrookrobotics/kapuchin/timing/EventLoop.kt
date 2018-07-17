package com.lynbrookrobotics.kapuchin.timing

import info.kunalsheth.units.generated.Time

object EventLoop : Clock {
    override var jobs: List<(tickStart: Time) -> Unit> = emptyList()
}