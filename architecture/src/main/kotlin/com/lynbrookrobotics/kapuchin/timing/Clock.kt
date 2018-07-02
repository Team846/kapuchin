package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.First
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import info.kunalsheth.units.generated.Time

interface Clock {
    var jobs: Set<(tickStart: Time) -> Unit>
    fun runOnTick(order: ExecutionOrder = First, run: (tickStart: Time) -> Unit): Cancel {
        jobs = when (order) {
            First -> setOf(run) + jobs
            Last -> jobs + run
        }
        return Cancel { jobs -= run }
    }

    fun tick(atTime: Time) = jobs.forEach { it(atTime) }
}