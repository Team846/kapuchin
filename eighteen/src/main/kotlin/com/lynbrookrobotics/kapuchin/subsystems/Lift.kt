package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.FeedbackDevice.Analog
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.OffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {
    val positionGains by pref {
        val kP by pref(12, `To Volt`, 8, `To Inch`)
        val kI by pref(0, `To Volt`, 1, `To FootSecond`)
        val kD by pref(0, `To Volt`, 1, `To FootPerSecond`)
        ({ PidGains(kP, kI, kD) })
    }

    val collectHeight by pref(0, `To Inch`)
    val exchangeHeight by pref(4, `To Inch`)
    val switchHeight by pref(25, `To Inch`)
    val lowScaleHeight by pref(53, `To Inch`)
    val highScaleHeight by pref(75, `To Inch`)
    val positionTolerance by pref(2, `To Inch`)

    val twistAdjustRange by pref(10, `To Inch`)

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }
    override fun LiftHardware.output(value: OffloadedOutput) = lazyOutput(value)
}

class LiftHardware : SubsystemHardware<LiftHardware, LiftComponent>() {
    override val priority = Priority.Low
    override val period = 20.milli(::Second)
    override val syncThreshold = 5.milli(::Second)
    override val subsystemName = "Lift"

    val operatingVoltage by pref(12, `To Volt`)
    val currentLimit by pref(30, `To Ampere`)

    // SAFETY
    val maxHeight by pref(80, `To Inch`)
    val minHeight by pref(0, `To Inch`)

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(615)
        val perFeedbackQuantity by pref(80.25, `To Inch`)
        val zeroOffset by pref(11.2, `To Inch`)

        ({
            OffloadedNativeConversion(
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = nativeFeedbackUnits, perFeedbackQuantity = perFeedbackQuantity,
                    feedbackZero = zeroOffset
            )
        })
    }

    val escCanId by pref(20)
    val maxOutput by pref(70, `To Percent`)
    val idx = 0
    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, Analog)

        val t = 5000

        // SAFETY
        it.configPeakOutputForward(maxOutput.siValue, t)
        it.configPeakOutputReverse(-maxOutput.siValue, t)

        it.configReverseSoftLimitThreshold(offloadedSettings.native(minHeight).toInt(), t)
        it.configReverseSoftLimitEnable(true, t)

        it.configForwardSoftLimitThreshold(offloadedSettings.native(maxHeight).toInt(), t)
        it.configForwardSoftLimitEnable(true, t)
    }
    val lazyOutput = lazyOutput(esc, idx)
    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
}