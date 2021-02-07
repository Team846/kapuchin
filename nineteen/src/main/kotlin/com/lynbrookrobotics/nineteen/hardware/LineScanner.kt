package com.lynbrookrobotics.nineteen.hardware

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.ranges.rangeTo

/**
 * FUCK WPILIB- WASTED 2 WEEKS TO GET THIS DUMB SHIT TO WORK
 */
class LineScanner(
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