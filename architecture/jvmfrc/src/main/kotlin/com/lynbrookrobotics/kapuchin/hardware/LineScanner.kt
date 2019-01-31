package com.lynbrookrobotics.kapuchin.hardware

import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.kilo
import info.kunalsheth.units.math.milli

class LineScanner(
        private val exposurePin: DigitalOutput,
        private val thresholdPin: DigitalOutput,
        private val feedbackPin: AnalogInput
) {

    private val pwmFrequency = 1.kilo(Hertz)

    init {
        thresholdPin.setPWMRate(pwmFrequency.kilo(Hertz))
        this(10.milli(Second), 25.Percent)
    }

    operator fun invoke(
            exposure: Time, threshold: DutyCycle
    ): Dimensionless {

        exposurePin.pulse(exposure.Second)
        thresholdPin.enablePWM(threshold.Each)

        return feedbackPin.voltage.Volt / RobotController.getVoltage5V().Volt
    }
}