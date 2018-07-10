package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    private val maxLeftSpeed by pref(13, `To FootPerSecond`)
    private val maxRightSpeed by pref(13.3, `To FootPerSecond`)
    val topSpeed get() = maxLeftSpeed min maxRightSpeed

    val positionGains by pref {
        val kP by pref(12, `To Volt`, 3, `To Foot`)
        val kI by pref(4, `To Volt`, 1, `To FootSecond`)
        val kD by pref(0, `To Volt`, 1, `To FootPerSecond`)
        ({ PidGains(kP, kI, kD) })
    }

    private val velocityGains by pref {
        val kP by pref(0, `To Volt`, 3, `To FootPerSecond`)
        val kI by pref(0, `To Volt`, 1, `To Foot`)
        val kD by pref(0, `To Volt`, 1, `To FootPerSecondSquared`)
        ({ PidGains(kP, kI, kD) })
    }
    val leftVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxLeftSpeed))
    val rightVelocityGains get() = velocityGains.copy(kF = Gain(hardware.operatingVoltage, maxRightSpeed))

    val trackSize by pref(2, `To Foot`)
    val maxTurningSpeed get() = topSpeed / (trackSize / 2)
    val turningPositionGains by pref {
        val kP by pref(2, `To FootPerSecond`, 45, `To Degree`)
        val kI by pref(0, `To FootPerSecond`, 45, `To DegreeSecond`)
        val kD by pref(0, `To FootPerSecond`, 45, `To DegreePerSecond`)
        ({ PidGains(kP, kI, kD) })
    }

    val maxAccelerationWithLiftUp by pref(3, `To FootPerSecondSquared`)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(VelocityOutput(
                hardware.offloadedSettings.native(velocityGains), hardware.offloadedSettings.native(0.FootPerSecond)
        ))
    }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)
    }
}