package com.lynbrookrobotics.kapuchin.timing.monitoring

import com.lynbrookrobotics.kapuchin.control.data.Delay
import com.lynbrookrobotics.kapuchin.control.math.Differentiator
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.First
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import info.kunalsheth.units.generated.*
import kotlin.math.sqrt
import kotlin.random.Random

class RealtimeChecker private constructor(parent: Ticker, val n: Int, private val setJitterPin: (Boolean) -> Unit, private val getPeriod: () -> Time)
    : Named by Named("Real-time Checker", parent) {

    private val delay = Delay<T>(n)

    private val zero = 0.Second
    private var sum = zero
    private var sqSum = zero * Second

    val grapher = graph("Period Standard Deviation", Millisecond)
    val periodStdev = EventLoop.runOnTick { loopStart ->

        // σ² = (Σ[xᵢ - ̄x]²) ÷ (n - 1)
        // σ² = (Σxᵢ² - 2̄xΣxᵢ + n̄x²) ÷ (n - 1)

        val xBar = sum / n
        val xBarSum2 = xBar * sum * 2 // 2̄xΣxᵢ
        val nxBarSq = xBar * xBar * n // n̄x²

        val sigmaSq = (sqSum - xBarSum2 + nxBarSq) / (n - 1)
        val sigma = T(sqrt(sigmaSq.siValue))

        grapher(loopStart, sigma)
    }

    val rand = Random(1)
    val jitterPulse = parent.runOnTick(First) {
        setJitterPin(true)
        setJitterPin(false)

        val period = getPeriod()
        if (period.siValue.isFinite()) {
            val delayed = delay(period) ?: zero
            sum += period - delayed
            sqSum += period * period - delayed * delayed
        }
    }

    companion object : Named by Named("Memory Checker") {
        val memoryUsageGraph = graph("Memory Usage", Percent)
        val gcGraph = graph("GC Period", Second)

        val differentiator = Differentiator(::div, 0.Second, 0.Percent)
        var lastGc = 0.Second

        val memoryUsage = EventLoop.runOnTick { loopStart ->

            val usage = usedMemory.Each / maxMemory
            if (differentiator(loopStart, usage).isNegative) {
                log(Debug) { "Collected Garbage" }
                gcGraph(loopStart, loopStart - lastGc)
                lastGc = loopStart
            }

            memoryUsageGraph(loopStart, usage)
        }

        fun Ticker.realtimeChecker(bufferSize: Int, setJitterPin: (Boolean) -> Unit, getPeriod: () -> Time) = RealtimeChecker(this, bufferSize, setJitterPin, getPeriod)
    }
}

expect val usedMemory: Long
expect val maxMemory: Long