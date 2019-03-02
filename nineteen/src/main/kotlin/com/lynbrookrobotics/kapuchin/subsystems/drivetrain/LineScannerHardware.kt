package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.Subsystems.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
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

    val sideShift by pref(8, Inch)
    val range by pref(53, Degree)
    val height by pref(6, Inch)

    val each = lineScanner(exposure, threshold).y!!.Each

    val distFromHeight = sideShift - each.Inch
    val angle = atan(Dimensionless(scanWidth.Inch + distFromHeight.Inch) / height.Inch) - range / 2
    val input: Angle = Angle(each)

    val linePosition = sensor(lineScanner) { _ ->
        val (x, y) = lineScanner(exposure, threshold)
        y?.let { (height * sin(input)) / (cos(angle) * cos(angle + input)) } stampWith x
    }
            .with(graph("Line Position", Inch)) { it ?: 0.Inch }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(linePosition).forEach {
                it.optimizedRead(time, 1.Second)
            }
        }
    }
}