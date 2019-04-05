package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.can.SlotConfiguration
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val maxLeftSpeed by pref(11.9, FootPerSecond)
    val maxRightSpeed by pref(12.5, FootPerSecond)
    val maxSpeed get() = maxLeftSpeed min maxRightSpeed
    val maxOmega get() = maxSpeed / hardware.conversions.trackLength / 2 * Radian

    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            SlotConfiguration().also {
                it.kP = hardware.conversions.nativeConversion.native(kP)
                it.kF = hardware.conversions.nativeConversion.native(
                        Gain(hardware.escConfig.voltageCompSaturation, maxSpeed)
                ) * kF.Each
            }
        })
    }

    val bearingKp by pref(5, FootPerSecond, 45, Degree)
    val bearingKd by pref(3, FootPerSecond, 360, DegreePerSecond)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(PercentOutput(hardware.escConfig, 0.Percent))
    }

    private val leftEscOutputGraph = graph("Left ESC Output", Volt)
    private val rightEscOutputGraph = graph("Right ESC Output", Volt)

    private val leftEscErrorGraph = graph("Left ESC Error", Each)
    private val rightEscErrorGraph = graph("Right ESC Error", Each)

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        value.left.writeTo(leftMasterEsc)
        value.right.writeTo(rightMasterEsc)

        leftEscOutputGraph(currentTime, leftMasterEsc.motorOutputVoltage.Volt)
        rightEscOutputGraph(currentTime, rightMasterEsc.motorOutputVoltage.Volt)

        leftEscErrorGraph(currentTime, leftMasterEsc.closedLoopError.Each)
        rightEscErrorGraph(currentTime, rightMasterEsc.closedLoopError.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}