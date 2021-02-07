package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

abstract class TC<This, H>(hardware: H, customClock: Clock? = null) : Component<This, H, String>(hardware, customClock)
        where This : TC<This, H>,
              H : SubsystemHardware<H, This> {

    override val fallbackController: This.(Time) -> String = { fallbackOutput }

    private var count = 0
    val out = ConcurrentLinkedQueue<String>()

    override fun H.output(value: String) {
        count++
        if (value != fallbackOutput) {
            out += value
            log(Level.Debug) { "$name output #$count @ ${currentTime withDecimals 2} by ${Thread.currentThread().name} thread = $value" }
        }
    }

    companion object {
        const val fallbackOutput = "fallback controller"
    }
}

suspend fun Component<*, *, String>.countTo(n: Int) = startRoutine("count to $n") {
    var counter = 0
    { "countTo($n)".takeIf { counter++ < n } }
}

fun TC<*, *>.checkCount(number: Int, times: Int, tolerance: Int = 0) {
    tolerance `is greater than or equal to?` abs(out.count { it == "countTo($number)" } - times)
}

abstract class TSH<This, C>(override val name: String) : SubsystemHardware<This, C>()
        where This : SubsystemHardware<This, C>,
              C : Component<C, This, *> {

    final override val priority = Priority.RealTime
    final override val period = 0.1.Second
    final override val syncThreshold = period / 10
}