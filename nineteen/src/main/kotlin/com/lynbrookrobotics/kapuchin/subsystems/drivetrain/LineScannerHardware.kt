package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LineScannerHardware : RobotHardware<LineScannerHardware>() {
    override val priority = Priority.Medium
    override val name = "Line Scanner"

    val exposurePort by pref(2)
    val thresholdPort by pref(3)
    val feedbackPort by pref(4)

    private val lineScanner by hardw {
        LineScanner(
                DigitalOutput(exposurePort),
                DigitalOutput(thresholdPort),
                DigitalInput(feedbackPort)
        )
    }

    private val exposure by pref(10, Millisecond)
    private val threshold by pref(25, Percent)
    private val scanWidth by pref(12, Inch)

    val linePosition = sensor(lineScanner) { t ->
        val (x, y) = lineScanner(exposure, threshold)
        y?.let { it * scanWidth - scanWidth / 2 } stampWith x
    }
            .with(graph("Line Position", Inch)) { it ?: 0.Inch }

    init {
        EventLoop.runOnTick { time ->
            setOf(linePosition).forEach {
                it.optimizedRead(time, 200.milli(Second))
            }
        }
    }
}