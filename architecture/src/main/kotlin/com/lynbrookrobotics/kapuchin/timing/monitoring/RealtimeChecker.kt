package com.lynbrookrobotics.kapuchin.timing.monitoring

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.*
import info.kunalsheth.units.generated.*

class RealtimeChecker private constructor(parent: Ticker, private val setJitterPin: (Boolean) -> Unit, private val getPeriod: () -> Time)
    : Named by Named("Real-time Checker", parent) {

    val loopPeriodGraph = graph("Loop Period", Millisecond)
    val computeTimeGraph = graph("Compute Time", Millisecond)

    val jitterPulse = parent.runOnTick(First) { tickStart ->
        setJitterPin(true)
        setJitterPin(false)
        val period = getPeriod()

        if (period.siValue.isFinite()) {
            loopPeriodGraph(tickStart, period)
            computeTimeGraph(tickStart, parent.computeTime)
        }
    }

    companion object : Named by Named("Memory Checker") {
        val logGc by pref(false)
        val memoryUsageGraph = graph("Memory Usage", Percent)
        val garbageProductionGraph = graph("Garbage Production", PercentPerSecond)

        val deriv = differentiator(::p, 0.Second, 0.Percent)

        val memoryUsage = EventLoop.runOnTick { loopStart ->
            val usage = usedMemory.Each / maxMemory
            if (usage > 80.Percent) log(Warning) { "RAM usage ≥ 80%. (Using $usedMemory of $maxMemory bytes)" }

            val deriv = deriv(loopStart, usage)

            memoryUsageGraph(loopStart, usage)
            garbageProductionGraph(loopStart, deriv)

            if (logGc && deriv.isNegative) log(Debug) { "Collected Garbage" }
        }

        fun Ticker.realtimeChecker(setJitterPin: (Boolean) -> Unit, getPeriod: () -> Time) = RealtimeChecker(this, setJitterPin, getPeriod)
    }
}

expect val usedMemory: Long
expect val maxMemory: Long