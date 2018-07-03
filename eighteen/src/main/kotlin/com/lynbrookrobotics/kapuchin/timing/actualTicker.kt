package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import edu.wpi.first.wpilibj.hal.NotifierJNI
import info.kunalsheth.units.generated.*

actual class Ticker private actual constructor(parent: Named, priority: Priority, val period: Time, name: String) : Named(name, parent), Clock {
    override var jobs: Set<(tickStart: Time) -> Unit> = emptySet()
    private val thread = PlatformThread(parent, name, priority) {
        while (true) tick(waitOnTick())
    }

    actual fun waitOnTick(): Time {
        updateAlarm()
        return NotifierJNI.waitForNotifierAlarm(notifierHandle).micro(::Second)
    }

    private val notifierHandle = NotifierJNI.initializeNotifier()
    private val startTime = currentTime
    private var lastPeriodIndex = -1L
    private fun updateAlarm() {
        val periodIndex = ((currentTime - startTime) / period).Tick.toLong() + 1

        if (periodIndex > lastPeriodIndex + 1) log(Warning) {
            "$name overran its ${period withDecimals 4} loop by ${currentTime - startTime withDecimals 4}"
        }

        if (lastPeriodIndex != periodIndex) NotifierJNI.updateNotifierAlarm(
                notifierHandle,
                (startTime + period * periodIndex).micro(T::Second).toLong()
        )

        lastPeriodIndex = periodIndex
    }

    actual companion object {
        actual fun Named.ticker(priority: Priority, period: Time, name: String) = Ticker(this, priority, period, name)
    }
}