@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package com.lynbrookrobotics.kapuchin.timing.clock

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.hal.NotifierJNI
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

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
    val overrunFrequencyThreshold by pref(10, Percent)
    private val thread = platformThread(name, priority) {
        var totalRuns = 0uL
        var overruns = 0uL

        while (true) {
            val startTime = waitOnTick()
            tick(startTime)
            computeTime = currentTime - startTime

            totalRuns++
            if (computeTime > period) overruns++
            if (overruns * 100u / totalRuns > overrunFrequencyThreshold.Percent.toUInt()) {
                log(Error) {
                    "more than ${overrunFrequencyThreshold.Percent withDecimals 0}% of loops overrun"
                }
                overruns = 0uL
                totalRuns = 0uL
            }

            if (computeTime > period + overrunLogThreshold) {
                val computeTimeCopy = computeTime
                log(Warning) {
                    "overran ${period withDecimals 4} loop by ${(computeTimeCopy - period) withDecimals 4}"
                }
            }
        }
    }
}