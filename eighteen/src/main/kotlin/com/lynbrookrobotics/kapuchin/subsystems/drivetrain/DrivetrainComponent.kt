package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.hardware.readWithComponent
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware, driver: DriverHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val maxLeftSpeed by pref(13::FootPerSecond)
    val maxRightSpeed by pref(13.3::FootPerSecond)
    val topSpeed get() = maxLeftSpeed min maxRightSpeed

    val wheelDiameter by pref(6::Inch)

    val encoderToWheelGears by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        ({ GearTrain(encoderGear, wheelGear) })
    }

    val offloadedSettings by pref {
        val nativeOutputUnits by pref(1023)
        val perOutputQuantity by pref(12::Volt)
        val nativeFeedbackUnits by pref(4096)
        val perFeedbackQuantity by pref(1::Turn)
        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = nativeOutputUnits, perOutputQuantity = perOutputQuantity, nativeFeedbackUnits = nativeFeedbackUnits,
                    perFeedbackQuantity = wheelDiameter * PI * encoderToWheelGears.inputToOutput(perFeedbackQuantity).Turn
            )
        })
    }

    val positionGains by pref {
        val kP by pref(12::Volt, 3::Foot)
        val kI by pref(4::Volt, 1::FootSecond)
        val kD by pref(0::Volt, 1::FootPerSecond)
        ({ PidGains(kP, kI, kD) })
    }

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
    val turningPositionGains by pref {
        val kP by pref(6::FootPerSecond, 45::Degree)
        val kI by pref(2::FootPerSecond, 45::DegreeSecond)
        val kD by pref(0::FootPerSecond, 45::DegreePerSecond)
        ({ PidGains(kP, kI, kD) })
    }

    val idx = 0
    val position by readWithComponent {
        TwoSided(
                offloadedSettings.realPosition(hardware.leftMasterEsc.getSelectedSensorPosition(idx)),
                offloadedSettings.realPosition(hardware.rightMasterEsc.getSelectedSensorPosition(idx))
        ) stampWith it
    }
    val velocity by readWithComponent {
        TwoSided(
                offloadedSettings.realVelocity(hardware.leftMasterEsc.getSelectedSensorVelocity(idx)),
                offloadedSettings.realVelocity(hardware.rightMasterEsc.getSelectedSensorVelocity(idx))
        ) stampWith it
    }

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        VelocityOutput(
                offloadedSettings.native(velocityGains), offloadedSettings.native(0.FootPerSecond)
        ).let { TwoSided(it, it) }
    }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)
    }
}