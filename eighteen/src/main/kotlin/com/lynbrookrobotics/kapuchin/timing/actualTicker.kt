package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import edu.wpi.first.wpilibj.hal.NotifierJNI
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.T
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.generated.micro

actual class Ticker internal actual constructor(parent: Named, priority: Priority, val period: Time, name: String) : Named(parent, name) {
    private val notifierHandle = NotifierJNI.initializeNotifier()
    private val startTime = currentTime

    private val thread = PlatformThread(parent, name, priority) {
        while (true) {
            val tickStart = waitOnTick()
            runOnTick.forEach { it(tickStart) }
        }
    }

    private var runOnTick: Set<(tickStart: Time) -> Unit> = emptySet()
    actual fun runOnTick(order: ExecutionOrder, run: (tickStart: Time) -> Unit): Cancel {
        runOnTick = when (order) {
            ExecutionOrder.First -> setOf(run) + runOnTick
            ExecutionOrder.Last -> runOnTick + run
        }
        return { runOnTick -= run }
    }

    actual fun waitOnTick(): Time {
        updateAlarm()
        return NotifierJNI.waitForNotifierAlarm(notifierHandle).micro(::Second)
    }

    private var lastPeriodIndex = -1L
    private fun updateAlarm() {
        val periodIndex = ((currentTime - startTime) / period)
                .siValue.toLong() + 1

        if (periodIndex > lastPeriodIndex + 1) log(Level.Warning) {
            "$thread overran its loop by ${currentTime - startTime} out of $period"
        }

        if (lastPeriodIndex != periodIndex) NotifierJNI.updateNotifierAlarm(
                notifierHandle,
                (startTime + period * periodIndex).micro(T::Second).toLong()
        )

        lastPeriodIndex = periodIndex
    }
}