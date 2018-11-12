package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.First
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.Last
import info.kunalsheth.units.generated.Time

interface Clock {
    var jobs: List<(tickStart: Time) -> Unit>

    fun runOnTick(order: ExecutionOrder = First, run: (tickStart: Time) -> Unit) = blockingMutex(this) {
        jobs = when (order) {
            First -> listOf(run) + jobs
            Last -> jobs + run
        }
        Cancel { blockingMutex(this) { jobs -= run } }
    }

    fun tick(atTime: Time) = jobs.forEach { it(atTime) }
}