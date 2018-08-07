package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.withDecimals
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.timing.Clock
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time

abstract class TC<This, H>(hardware: H, customClock: Clock? = null) : Component<This, H, String>(hardware, customClock)
        where This : TC<This, H>,
              H : SubsystemHardware<H, This> {

    override val fallbackController: This.(Time) -> String = { "fallback controller" }
    var out = emptyList<String>()

    override fun H.output(value: String) {
        out += value
        println("output @ ${currentTime withDecimals 2} by thread #${Thread.currentThread().id} = $value")
    }
}

suspend fun Component<*, *, String>.countTo(n: Int) = startRoutine("count to $n") {
    var counter = 0
    { "countTo($n)".takeIf { counter++ < n } }
}

fun TC<*, *>.checkCount(number: Int, times: Int) {
    out.count { it == "countTo($number)" } `is equal to?` times
}

abstract class TSH<This, C>(override val subsystemName: String) : SubsystemHardware<This, C>()
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {

    final override val priority = Priority.RealTime
    final override val period = 0.2.Second
    final override val syncThreshold = period / 10
}