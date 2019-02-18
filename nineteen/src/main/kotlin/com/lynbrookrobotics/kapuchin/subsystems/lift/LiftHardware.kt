package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LiftHardware : SubsystemHardware<LiftHardware, LiftComponent>() {
    override val name: String = "Lift"
    override val period: Time = 30.milli(Second)
    override val priority: Priority = Priority.Low
    override val syncThreshold: Time = 5.milli(Second)

    val operatingVoltage by pref(12, Volt)
    val currentLimit by pref(30, Ampere)
    val startupFrictionCompensation by pref(1.4, Volt)

    val jitterPulsePinNumber by pref(6)
    val jitterReadPinNumber by pref(7)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    // SAFETY
    val min by pref {
        val real by pref(0, Inch)
        val native by pref(0)
        ({ Pair(real, native) })
    }

    val max by pref {
        val real by pref(80, Inch)
        val native by pref(1023)
        ({ Pair(real, native) })
    }

    val conversion by pref {
        { Pair(max.first - min.first, max.second - min.second) }
    }

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(615)
        val perFeedbackQuantity by pref(80.25, Inch)
        val zeroOffset by pref(11.2, Inch)

        ({
            LinearOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = conversion.second, perFeedbackQuantity = conversion.first,
                    feedbackZero = zeroOffset
            )
        })
    }

    val escCanId by pref(20) //TODO
    val maxOutput by pref(70, Percent)
    val idx = 0
    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, FeedbackDevice.Analog)

        val t = 5000

        // SAFETY
        it.configPeakOutputForward(maxOutput.siValue, t)
        it.configPeakOutputReverse(-maxOutput.siValue, t)

        it.configReverseSoftLimitThreshold(offloadedSettings.native(min.first).toInt(), t)
        it.configReverseSoftLimitEnable(true, t)

        it.configForwardSoftLimitThreshold(offloadedSettings.native(max.first).toInt(), t)
        it.configForwardSoftLimitEnable(true, t)
    }

    val lazyOutput = lazyOutput(esc, idx)

    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
            .with(graph("Height", Inch))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }

}