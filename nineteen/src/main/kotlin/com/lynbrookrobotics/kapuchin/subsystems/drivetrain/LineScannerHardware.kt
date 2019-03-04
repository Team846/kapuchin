package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
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

    private val exposurePort = 2
    private val thresholdPort = 3
    private val feedbackPort = 4

    private val lineScanner by hardw {
        LineScanner(
                DigitalOutput(exposurePort),
                DigitalOutput(thresholdPort),
                DigitalInput(feedbackPort)
        )
    }

    private val exposure by pref(10, Millisecond)
    private val threshold by pref(25, Percent)

    private val scannerFov by pref(53.13, Degree)
    private val bisectionPoint by pref(2, Inch)
    private val zeroOffset by pref(-3, Inch)
    private val mounting by pref {
        val x by pref(-12, Inch)
        val y by pref(12, Inch)
        val z by pref(6, Inch)
        ({ UomVector(x, y, z) })
    }

    private val scanEdge get() = atan(bisectionPoint / mounting.z) - (scannerFov / 2)
    private val scanShift get() = mounting.z * tan(scanEdge)
    private fun locate(raw: Dimensionless) = (mounting.z * tan(scanEdge + raw * scannerFov)) - scanShift

    val nativeGrapher = graph("Native", Each)
    val linePosition = sensor(lineScanner) {
        val (x, y) = lineScanner(exposure, threshold)
        nativeGrapher(x, y ?: Double.NaN.Each)
        y?.let { locate(it) - zeroOffset } stampWith x
    }
            .with(graph("Line Position", Inch)) { it ?: 0.Inch }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(linePosition).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}