package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware, driver: DriverHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val maxLeftSpeed by pref(13::FootPerSecond)
    val maxRightSpeed by pref(13.3::FootPerSecond)
    val topSpeed get() = maxLeftSpeed min maxRightSpeed

    val velocityGains by pref {
        val kP by pref(12::Volt, 3::FootPerSecond)
        val kI by pref(4::Volt, 1::Foot)
        val kD by pref(0::Volt, 1::FootPerSecondSquared)
        ({ PidGains(kP, kI, kD) })
    }
    val leftVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxLeftSpeed))
    val rightVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxRightSpeed))

    val trackSize by pref(2::Foot)
    val maxTurningSpeed get() = topSpeed / (trackSize / 2)
    val turningVelocityGains by pref {
        val kP by pref(6::FootPerSecond, 45::DegreePerSecond)
        val kI by pref(2::FootPerSecond, 45::Degree)
        val kD by pref(0::FootPerSecond, 45::DegreePerSecondSquared)
        ({ PidGains(kP, kI, kD, Gain(topSpeed, maxTurningSpeed)) })
    }

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        VelocityOutput(
                hardware.offloadedSettings.native(velocityGains), hardware.offloadedSettings.native(0.FootPerSecond)
        ).let { TwoSided(it, it) }
    }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)
    }
}