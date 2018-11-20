package com.lynbrookrobotics.kapuchin.timing.monitoring

import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import info.kunalsheth.units.generated.Each
import info.kunalsheth.units.generated.Percent

class RealtimeChecker private constructor(parent: Ticker, private val setJitterPin: (Boolean) -> Unit)
    : Named by Named("Real-time Checker", parent) {

    val jitterPulse = parent.runOnTick {
        setJitterPin(true)
        setJitterPin(false)
    }

    companion object : Named by Named("Memory Checker") {
        val grapher = graph("Memory Usage", Percent)
        val memoryUsage = EventLoop.runOnTick {
            grapher(it, (usedMemory / maxMemory).Each)
        }

        fun Ticker.realtimeChecker(setJitterPin: (Boolean) -> Unit) = RealtimeChecker(this, setJitterPin)
    }
}

expect val usedMemory: Long
expect val maxMemory: Long