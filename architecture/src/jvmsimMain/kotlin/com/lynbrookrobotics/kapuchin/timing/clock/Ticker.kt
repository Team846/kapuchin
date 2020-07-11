package com.lynbrookrobotics.kapuchin.timing.clock

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

actual class Ticker internal actual constructor(
    parent: Named,
    priority: Priority,
    val period: Time,
    name: String
) :
    Named by Named(name, parent),
    Clock {

    actual var computeTime = 0.Second
        private set

    override val jobsToRun = mutableListOf<(tickStart: Time) -> Unit>()
    override val jobsToKill = mutableSetOf<(tickStart: Time) -> Unit>()

    init {
        exec.scheduleAtFixedRate(
            {
                computeTime = measureNanoTime {
                    try {
                        tick(currentTime)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }.nano(Second)
            },
            100, period.milli(Second).toLong(), TimeUnit.MILLISECONDS
        )
    }

    actual fun waitOnTick(): Time {
        // todo do this correctly
        blockingDelay(period)
        return currentTime
    }

    companion object {
        private val exec = Executors.newSingleThreadScheduledExecutor()!!
    }
}