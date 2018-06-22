package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.subsystems.Named
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.First
import info.kunalsheth.units.generated.Time

fun Named.Ticker(priority: Priority, period: Time, name: String = "Ticker") = Ticker(this, priority, period, name)

expect class Ticker internal constructor(parent: Named, priority: Priority, period: Time, name: String = "Ticker") : Named {
    fun runOnTick(order: ExecutionOrder = First, run: (Time) -> Unit)
    fun waitOnTick(): Time
}

