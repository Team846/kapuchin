package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.ctre.phoenix.motorcontrol.ControlMode
import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<DutyCycle>>(hardware) {

    val operatingVoltage by pref(11.5, Volt)
    val startupVoltage by pref(1.35, Volt)

    val motorCurrentLimit by pref(10, Ampere)
    val motorStallCurrent by pref(131, Ampere)

    val maxLeftSpeed by pref(13, FootPerSecond)
    val maxRightSpeed by pref(13.3, FootPerSecond)
    val maxSpeed get() = maxLeftSpeed min maxRightSpeed

    val velocityKp by pref(10, Volt, 2, FootPerSecond)

    private val trackSize by pref(2, Foot)
    val maxSpinSpeed get() = maxSpeed / (trackSize / 2) * Radian
    val bearingKp by pref(2, FootPerSecond, 60, Degree)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<DutyCycle> = {
        TwoSided(0.Percent)
    }

    override fun DrivetrainHardware.output(value: TwoSided<DutyCycle>) {
        leftMasterEsc.set(ControlMode.PercentOutput, value.left.Each)
        leftMasterEsc.set(ControlMode.PercentOutput, value.right.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}