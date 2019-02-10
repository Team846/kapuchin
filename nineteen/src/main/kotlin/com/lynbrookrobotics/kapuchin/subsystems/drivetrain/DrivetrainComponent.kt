package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val maxLeftSpeed by pref(13, FootPerSecond)
    val maxRightSpeed by pref(13.3, FootPerSecond)
    val maxSpeed get() = maxLeftSpeed min maxRightSpeed

    val velocityGains by pref {
        val kP by pref(10, Volt, 2, FootPerSecond)
        ({
            OffloadedPidGains(
                    hardware.conversions.nativeConversion.native(kP),
                    0.0, 0.0,
                    hardware.conversions.nativeConversion.native(
                            Gain(hardware.operatingVoltage, maxSpeed)
                    )
            )
        })
    }

    val bearingKp by pref(2, FootPerSecond, 60, Degree)
    val bearingKd by pref(0, FootPerSecond, 60, DegreePerSecond)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(PercentOutput(0.Percent))
    }

    private val leftEscOutputGraph = graph("Left ESC Output", Volt)
    private val rightEscOutputGraph = graph("Right ESC Output", Volt)

    private val leftSoftwareOutputGraph = graph("Left Software Output", Each)
    private val rightSoftwareOutputGraph = graph("Right Software Output", Each)

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)

        leftEscOutputGraph(currentTime, hardware.leftMasterEsc.motorOutputVoltage.Volt)
        rightEscOutputGraph(currentTime, hardware.rightMasterEsc.motorOutputVoltage.Volt)

        if(value.left is VelocityOutput && value.right is VelocityOutput) {
            leftSoftwareOutputGraph(currentTime, (value.left as VelocityOutput).output.Each)
            rightSoftwareOutputGraph(currentTime, (value.right as VelocityOutput).output.Each)
        }
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}