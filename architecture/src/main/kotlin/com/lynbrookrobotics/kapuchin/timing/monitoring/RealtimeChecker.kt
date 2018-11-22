package com.lynbrookrobotics.kapuchin.timing.monitoring

import com.lynbrookrobotics.kapuchin.control.data.Delay
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.First
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import info.kunalsheth.units.generated.*
import kotlin.math.sqrt

class RealtimeChecker private constructor(parent: Ticker, val n: Int, private val setJitterPin: (Boolean) -> Unit, private val getPeriod: () -> Time)
    : Named by Named("Real-time Checker", parent) {

    private val delay = Delay<T>(n)

    private val `0` = 0.Second
    private var `Σxᵢ` = `0`
    private var `Σxᵢ²` = `0` * Second

    val grapher = graph("Period Standard Deviation", Second)
    val periodStdev = EventLoop.runOnTick { loopStart ->

        // σ² = (Σ[xᵢ - ̄x]²) ÷ (n - 1)
        // σ² = (Σxᵢ² - 2̄xΣxᵢ + n̄x²) ÷ (n - 1)

        val `̄x` = `Σxᵢ` / n
        val `2̄xΣxᵢ` = `̄x` * `Σxᵢ` * 2
        val `n̄x²` = `̄x` * `̄x` * n

        val `σ²` = (`Σxᵢ²` - `2̄xΣxᵢ` + `n̄x²`) / (n - 1)

        grapher(T(sqrt(`σ²`.siValue)), loopStart)
    }

    val jitterPulse = parent.runOnTick(First) {
        setJitterPin(true)
        setJitterPin(false)

        val period = getPeriod()
        val delayed = delay(period) ?: `0`
        `Σxᵢ` += period - delayed
        `Σxᵢ²` += period * period - delayed * delayed
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