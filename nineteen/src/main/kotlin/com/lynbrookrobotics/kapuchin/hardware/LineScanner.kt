package com.lynbrookrobotics.kapuchin.hardware

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
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
        private val lineRange: ClosedRange<Time> = 1.Millisecond..2.Millisecond
) {

    private val feedbackCounter = Counter(feedbackPin)

    init {
//        feedbackPin.requestInterrupts()
//        feedbackPin.setUpSourceEdge(true, true)
    }

    private fun locate(raw: Time) = lineRange.run {
        endInclusive / (endInclusive - start + raw)
    }

    operator fun invoke(
            exposure: Time, threshold: DutyCycle
    ): TimeStamped<Dimensionless?> {

        if(!exposurePin.isPulsing) exposurePin.pulse(exposure.Second)
        if(!thresholdPin.isPulsing) thresholdPin.pulse((exposure * threshold).Second)

//        val rise = feedbackPin.readRisingTimestamp().Second
//        val fall = feedbackPin.readFallingTimestamp().Second
        val raw = feedbackCounter.period.Second / 2

        println("pw: ${raw.micro(Second) withDecimals 3} us, ex: ${exposure.Second withDecimals 3}, th: ${(exposure * threshold).Second withDecimals 3}")

        return (if (raw in noLineRange) null
        else locate(raw)) stampWith currentTime //stamp
    }
}