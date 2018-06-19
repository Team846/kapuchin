package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.timing.ExecutionOrder.*
import edu.wpi.first.wpilibj.hal.NotifierJNI
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.micro

actual class Ticker actual constructor(name: String, priority: Priority, private val period: Time) {

    private val notifierHandle = NotifierJNI.initializeNotifier()
    private val startTime = currentTime

    private val thread = PlatformThread("$name Thread", priority) {
        while (true) {
            val tickStart = waitOnTick()
            runOnTick.forEach { it(tickStart) }
        }
    }

    private var runOnTick: Set<(tickStart: Time) -> Unit> = emptySet()
    actual fun runOnTick(order: ExecutionOrder, run: (tickStart: Time) -> Unit) {
        runOnTick = when (order) {
            First -> setOf(run) + runOnTick
            Last -> runOnTick + run
        }
    }

    actual fun waitOnTick(): Time {
        updateAlarm()
        return NotifierJNI.waitForNotifierAlarm(notifierHandle).micro(::Second)
    }

    private var lastPeriodIndex = -1L
    private fun updateAlarm() {
        val periodIndex = ((currentTime - startTime) / period)
                .siValue.toLong() + 1

        if (periodIndex > lastPeriodIndex + 1) log(Warning) {
            "$thread overran its loop by ${currentTime - startTime} out of $period"
        }

        if (lastPeriodIndex != periodIndex) NotifierJNI.updateNotifierAlarm(
                notifierHandle,
                (startTime + period * periodIndex).micro(T::Second).toLong()
        )

        lastPeriodIndex = periodIndex
    }
}