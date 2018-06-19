package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.*
import info.kunalsheth.units.generated.Time

expect class Ticker(name: String, priority: Priority, period: Time) {
    fun runOnTick(order: ExecutionOrder = First, run: (Time) -> Unit)
    fun waitOnTick(): Time
}
