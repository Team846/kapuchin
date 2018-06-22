package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.TwoSided
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val wheelDiameter by pref(6::Inch)

    val encoderToWheelGears by pref {
        val driver by pref(18)
        val driven by pref(74)
        ({ GearTrain(driver, driven) })
    }

    val offloadedSettings by pref {
        val nativeOutputUnits by pref(1023::Tick)
        val perOutputQuantity by pref(12::Volt)
        val nativeFeedbackUnits by pref(4096::Tick)
        val perFeedbackQuantity by pref(360::Degree)
        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = nativeOutputUnits, perOutputQuantity = perOutputQuantity, nativeFeedbackUnits = nativeFeedbackUnits,
                    perFeedbackQuantity = wheelDiameter * PI * encoderToWheelGears.inputToOutput(perFeedbackQuantity).Turn
            )
        })
    }

    val velocityGains by pref {
        val kP by pref(12::Volt, 3::FootPerSecond)
        val kI by pref(4::Volt, 1::Foot)
        val kD by pref(0::Volt, 1::FootPerSecondSquared)
        ({ PidGains(kP, kI, kD) })
    }

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        VelocityOutput(
                offloadedSettings.native(velocityGains),
                offloadedSettings.native(0.FootPerSecond)
        ).let { TwoSided(it, it) }
    }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)
    }
}

class DrivetrainHardware : Hardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(::Second)
    override val syncThreshold = 1.milli(::Second)
    override val subsystemName = "Drivetrain"

    val leftSlaveEscId by pref(14)
    val rightSlaveEscId by pref(13)
    val rightMasterEscId by pref(11)
    val leftMasterEscId by pref(12)

    val escCanTimeout by pref(0.001::Second)

    val leftMasterEsc = TalonSRX(leftMasterEscId)
    val leftLazyOutput = lazyOutput(leftMasterEsc, escCanTimeout)
    val leftSlaveEsc = TalonSRX(leftSlaveEscId)

    val rightMasterEsc = TalonSRX(rightMasterEscId)
    val rightLazyOutput = lazyOutput(rightMasterEsc, escCanTimeout)
    val rightSlaveEsc = TalonSRX(rightSlaveEscId)
}