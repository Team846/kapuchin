package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Named
import edu.wpi.first.wpilibj.hal.NotifierJNI
import info.kunalsheth.units.generated.Each
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.micro

actual class Ticker private actual constructor(parent: Named, priority: Priority, val period: Time, name: String) : Named(name, parent), Clock {
    override var jobs: List<(tickStart: Time) -> Unit> = emptyList()
    private val thread = PlatformThread(parent, name, priority) {
        while (true) tick(waitOnTick())
    }

    actual fun waitOnTick(): Time {
        updateAlarm()
        // Thread.yield()
        return NotifierJNI.waitForNotifierAlarm(notifierHandle).micro(Second)
    }

    private val notifierHandle = NotifierJNI.initializeNotifier()
    private val startTime = currentTime
    private var periodIndex = -1L
    private fun updateAlarm() {
        val dt = currentTime - startTime
        val nextPeriodIndex = (dt / period).Each.toLong() + 1

//        if (nextPeriodIndex > periodIndex + 1) log(Warning) {
//            "$name overran its ${period withDecimals 4} loop by ${dt - period * periodIndex withDecimals 4}"
//        }

        if (nextPeriodIndex != periodIndex) NotifierJNI.updateNotifierAlarm(
                notifierHandle,
                (startTime + period * nextPeriodIndex).micro(Second).toLong()
        )

        periodIndex = nextPeriodIndex
    }

    actual companion object {
        actual fun Named.ticker(priority: Priority, period: Time, name: String) = Ticker(this, priority, period, name)
    }
}