package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.LineScanner
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.Inch
import info.kunalsheth.units.generated.Millisecond
import info.kunalsheth.units.generated.Percent
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.times
import info.kunalsheth.units.math.milli

class LineScannerHardware : SubsystemHardware<LineScannerHardware, Nothing>() {
    override val priority = Priority.Medium
    override val period = 15.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val name = "Line Scanner"

    val exposurePort by pref(2)
    val thresholdPort by pref(3)
    val feedbackPort by pref(0)

    private val lineScanner by hardw {
        LineScanner(
                DigitalOutput(exposurePort),
                DigitalOutput(thresholdPort),
                AnalogInput(feedbackPort)
        )
    }

    private val exposure by pref(10, Millisecond)
    private val threshold by pref(25, Percent)
    private val scanWidth by pref(12, Inch)

    val linePosition = sensor(lineScanner) {
        lineScanner(exposure, threshold) * scanWidth - scanWidth / 2 stampWith it
    }
            .with(graph("Line Position", Inch)) { it }

    init {
        EventLoop.runOnTick { time ->
            setOf(linePosition).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}