package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.Subsystems.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
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

    val escCanId by pref(40)
    val maxOutput by pref(30, Percent)
    val idx = 0

    val conversions = LiftConversions(this)

    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, FeedbackDevice.Analog)

        +it.configPeakOutputForward(maxOutput.siValue, configTimeout)
        +it.configPeakOutputReverse(-maxOutput.siValue, configTimeout)

        with(conversions) {
            +it.configReverseSoftLimitThreshold(native.native(minPt.first).toInt(), configTimeout)
            +it.configReverseSoftLimitEnable(true, configTimeout)

            +it.configForwardSoftLimitThreshold(native.native(maxPt.first).toInt(), configTimeout)
            +it.configForwardSoftLimitEnable(true, configTimeout)
        }
    }

    val lazyOutput = lazyOutput(esc, idx)

    val position = sensor {
        conversions.native.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it
    }
            .with(graph("Height", Inch))

    init {
        uiBaselineTicker.runOnTick { position.optimizedRead(it, 1.Second) }
    }

}