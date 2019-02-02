package com.lynbrookrobotics.kapuchin.subsystems.slider

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class SliderHardware : SubsystemHardware<SliderHardware, SliderComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 5.milli(Second)
    override val syncThreshold: Time = 1.milli(Second)
    override val name: String = "Slider"

    val operatingVoltage by pref(12, Volt)
    val currentLimit by pref(30, Ampere)

    // SAFETY
    val leftLimit by pref(-3, Inch)
    val rightLimit by pref(3, Inch)

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(615) //TODO
        val perFeedbackQuantity by pref(80.25, Inch) //TODO
        val zeroOffset by pref(0, Inch) //TODO

        ({
            LinearOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = nativeFeedbackUnits, perFeedbackQuantity = perFeedbackQuantity,
                    feedbackZero = zeroOffset
            )
        })
    }

    val escCanId by pref(20) //TODO
    val maxOutput by pref(70, Percent)
    val idx = 0
    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, FeedbackDevice.Analog)

        val t = 5000

        // SAFETY
        it.configPeakOutputForward(maxOutput.siValue, t)
        it.configPeakOutputReverse(-maxOutput.siValue, t)

        it.configReverseSoftLimitThreshold(offloadedSettings.native(leftLimit).toInt(), t)
        it.configReverseSoftLimitEnable(true, t)

        it.configForwardSoftLimitThreshold(offloadedSettings.native(rightLimit).toInt(), t)
        it.configForwardSoftLimitEnable(true, t)
    }

    val lazyOutput = lazyOutput(esc, idx)

    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
            .with(graph("Height", Inch))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }


}