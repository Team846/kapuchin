package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.milli
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

actual class Ticker private actual constructor(
        parent: Named,
        priority: Priority,
        val period: Time,
        name: String
) :
        Named by Named(name, parent),
        Clock {

    override var jobs: List<(tickStart: Time) -> Unit> = emptyList()

    private val exec = Executors.newSingleThreadScheduledExecutor()!!

    init {
        exec.scheduleAtFixedRate(
                {
                    try {
                        tick(currentTime)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                },
                100, period.milli(Second).toLong(), TimeUnit.MILLISECONDS
        )
    }

    actual fun waitOnTick(): Time {
        // todo do this correctly
        Thread.sleep(period.milli(Second).toLong())
        return currentTime
    }

    actual companion object {
        actual fun Named.ticker(priority: Priority, period: Time, name: String) = Ticker(this, priority, period, name)
    }
}