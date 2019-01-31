package com.lynbrookrobotics.kapuchin.timing.clock

import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.logging.withDecimals
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.PlatformThread.Companion.platformThread
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.hal.NotifierJNI
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.micro

actual class Ticker private actual constructor(
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
    override val jobsToKill = mutableListOf<(tickStart: Time) -> Unit>()

    actual fun waitOnTick(): Time {
        updateAlarm()
        Thread.yield()
        return NotifierJNI.waitForNotifierAlarm(notifierHandle).micro(Second)
    }

    private val notifierHandle = NotifierJNI.initializeNotifier()
    private val startTime = currentTime
    private var periodIndex = -1L
    private fun updateAlarm() {
        val dt = currentTime - startTime
        val nextPeriodIndex = (dt / period).Each.toLong() + 1

        if (nextPeriodIndex != periodIndex) NotifierJNI.updateNotifierAlarm(
                notifierHandle,
                (startTime + period * nextPeriodIndex).micro(Second).toLong()
        )

        periodIndex = nextPeriodIndex
    }

    val overrunLogThreshold by pref(100, Millisecond)
    private val thread = platformThread(name, priority) {
        while (true) {
            val startTime = waitOnTick()
            tick(startTime)
            computeTime = currentTime - startTime

            computeTime.takeIf { it > period + overrunLogThreshold }?.also {
                log(Warning) {
                    "$name overran its ${period withDecimals 4} loop by ${(it - period) withDecimals 4}"
                }
            }
        }
    }

    actual companion object {
        actual fun Named.ticker(priority: Priority, period: Time, name: String) = Ticker(this, priority, period, name)
    }
}