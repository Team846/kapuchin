package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.FeedbackDevice.Analog
import com.ctre.phoenix.motorcontrol.NeutralMode.Brake
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.hardw
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware) {

    override val fallbackController: LiftComponent.(Time) -> PositionOutput
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun LiftHardware.output(value: OffloadedOutput) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class LiftHardware : SubsystemHardware<LiftHardware, LiftComponent>() {
    override val priority = Priority.Medium
    override val period = 20.milli(::Second)
    override val syncThreshold = 1.milli(::Second)
    override val subsystemName = "Lift"

    val operatingVoltage by pref(12::Volt)
    val currentLimit by pref(30::Ampere)

    // SAFETY
    val maxHeight by pref(60::Inch)
    val minHeight by pref(20::Inch)

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(615)
        val perFeedbackQuantity by pref(80.25::Inch)
        val zeroOffset by pref(11.2::Inch)

        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = nativeFeedbackUnits, perFeedbackQuantity = perFeedbackQuantity,
                    feedbackZero = zeroOffset
            )
        })
    }

    val escCanId by pref(20)
    val esc = hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, Analog)
        it.setNeutralMode(Brake)

        val t = 100

        // SAFETY
        it.configPeakOutputForward(0.2, t)
        it.configPeakOutputReverse(0.2, t)

        it.configReverseSoftLimitThreshold(offloadedSettings.native(minHeight).toInt(), t)
        it.configReverseSoftLimitEnable(true, t)

        it.configForwardSoftLimitThreshold(offloadedSettings.native(maxHeight).toInt(), t)
        it.configForwardSoftLimitEnable(true, t)
    }
}