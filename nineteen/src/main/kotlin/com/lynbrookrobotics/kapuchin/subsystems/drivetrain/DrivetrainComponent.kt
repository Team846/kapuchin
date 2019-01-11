package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.data.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<DutyCycle>>(hardware) {

    private val maxLeftSpeed by pref(13, FootPerSecond)
    private val maxRightSpeed by pref(13.3, FootPerSecond)
    val topSpeed get() = maxLeftSpeed min maxRightSpeed

    private val velocityGains by pref {
        val kP by pref(10, Volt, 2, FootPerSecond)
        val kI by pref(0, Volt, 1, Foot)
        val kD by pref(0, Volt, 1, FootPerSecondSquared)
        ({ PidGains(kP, kI, kD) })
    }
    val leftVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxLeftSpeed))
    val rightVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxRightSpeed))

    val trackSize by pref(2, Foot)
    val maxTurningSpeed get() = topSpeed / (trackSize / 2) * Radian
    val turningPositionGains by pref {
        val kP by pref(2, FootPerSecond, 60, Degree)
        val kI by pref(0, FootPerSecond, 45, DegreeSecond)
        val kD by pref(0, FootPerSecond, 90, DegreePerSecond)
        ({ PidGains(kP, kI, kD) })
    }

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