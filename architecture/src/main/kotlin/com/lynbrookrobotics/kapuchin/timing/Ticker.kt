package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.First
import info.kunalsheth.units.generated.Time

fun Named.ticker(priority: Priority, period: Time, name: String = "ticker") = Ticker(this, priority, period, name)

expect class Ticker internal constructor(parent: Named, priority: Priority, period: Time, name: String = "ticker") : Named {
    fun runOnTick(order: ExecutionOrder = First, run: (Time) -> Unit)
    fun waitOnTick(): Time
}

