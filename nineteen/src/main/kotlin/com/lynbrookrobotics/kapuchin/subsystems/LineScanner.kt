package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * FUCK WPILIB- WASTED 2 WEEKS TO GET THIS DUMB SHIT TO WORK
 */
class LineScannerHardware(
        private val exposurePin: DigitalOutput,
        private val thresholdPin: DigitalOutput,
        private val feedbackPin: DigitalInput,

        private val noLineRange: ClosedRange<Time> = 500.micro(Second) `±` 100.micro(Second),
        private val lineRange: ClosedRange<Time> = 1.Millisecond..2.Millisecond,
        private val pwmRate: Frequency = 1.kilo(Hertz),
        private val maxExposure: Time = 50000.micro(Second)
) {

    private val feedbackCounter = Counter(feedbackPin)

    init {
        setOf(exposurePin, thresholdPin).forEach {
            it.setPWMRate(pwmRate.Hertz)
            it.enablePWM(0.2)
        }
    }

    private fun locate(raw: Time) = lineRange.run {
        (raw - start) / (endInclusive - start)
    }

    operator fun invoke(
            exposure: Time, threshold: DutyCycle
    ): TimeStamped<Dimensionless?> {

        exposurePin.updateDutyCycle((exposure / maxExposure).Each)
        thresholdPin.updateDutyCycle(threshold.Each)

        val raw = feedbackCounter.period.Second / 2

        return (if (raw in noLineRange) null
        else locate(raw)) stampWith currentTime //stamp
    }
}

class LineScanner : RobotHardware<LineScanner>() {
    override val priority = Priority.Medium
    override val name = "Line Scanner"

    private val exposurePort = 2
    private val thresholdPort = 3
    private val feedbackPort = 4

    private val lineScanner by hardw {
        LineScannerHardware(
                DigitalOutput(exposurePort),
                DigitalOutput(thresholdPort),
                DigitalInput(feedbackPort)
        )
    }

    private val exposure by pref(10, Millisecond)
    private val threshold by pref(25, Percent)

    val lookAhead by pref(12, Inch)

    private val conversion by pref {
        val pt1 by pref {
            val pixel by pref(20, Percent)
            val location by pref(-5, Inch)
            ({ pixel to location })
        }
        val pt2 by pref {
            val pixel by pref(40, Percent)
            val location by pref(-2, Inch)
            ({ pixel to location })
        }
        val pt3 by pref {
            val pixel by pref(60, Percent)
            val location by pref(2, Inch)
            ({ pixel to location })
        }
        val pt4 by pref {
            val pixel by pref(80, Percent)
            val location by pref(5, Inch)
            ({ pixel to location })
        }

        ({
            // lagrange's formula
            // https://www.desmos.com/calculator/dffnj2jbow

            val (x1, y1) = pt1
            val (x2, y2) = pt2
            val (x3, y3) = pt3
            val (x4, y4) = pt4
            fun p1(x: Dimensionless) = (x - x2) * (x - x3) * (x - x4)
            fun p2(x: Dimensionless) = (x - x1) * (x - x3) * (x - x4)
            fun p3(x: Dimensionless) = (x - x1) * (x - x2) * (x - x4)
            fun p4(x: Dimensionless) = (x - x1) * (x - x2) * (x - x3)
            fun(x: Dimensionless) = p1(x) * (y1 / p1(x1)) +
                    p2(x) * (y2 / p2(x2)) +
                    p3(x) * (y3 / p3(x3)) +
                    p4(x) * (y4 / p4(x4))
        })
    }

    val nativeGrapher = graph("Native", Percent)
    val linePosition = sensor(lineScanner) {
        val (x, y) = lineScanner(exposure, threshold)
        nativeGrapher(x, y ?: Double.NaN.Each)
        y?.let { conversion(y) } stampWith x
    }
            .with(graph("Line Position", Inch)) { it ?: Double.NaN.Inch }

    init {
        uiBaselineTicker.runOnTick { time ->
            setOf(linePosition).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}