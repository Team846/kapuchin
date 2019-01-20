package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.CommonMotors
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<DutyCycle>>(hardware) {

    val operatingVoltage by pref(11.5, Volt)
    val startupVoltage by pref(1.35, Volt)

    val motorCurrentLimit by pref(10, Ampere)
    val motorFreeCurrent by pref(4.65, Ampere)
    private val motorType by pref("cim")
    val motor get() = CommonMotors.valueOf(motorType).spec

    val maxLeftSpeed by pref(13.8, FootPerSecond)
    val maxRightSpeed by pref(13.8, FootPerSecond)
    val maxSpeed get() = maxLeftSpeed min maxRightSpeed

    val velocityKp by pref(50, Ampere, 2, FootPerSecond)

    val maxSpinSpeed get() = maxSpeed / (hardware.trackLength / 2) * Radian
    val bearingKp by pref(2, FootPerSecond, 60, Degree)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<DutyCycle> = {
        TwoSided(0.Percent)
    }

    override fun DrivetrainHardware.output(value: TwoSided<DutyCycle>) {
        leftEsc.set(value.left.Each)
        rightEsc.set(value.right.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}