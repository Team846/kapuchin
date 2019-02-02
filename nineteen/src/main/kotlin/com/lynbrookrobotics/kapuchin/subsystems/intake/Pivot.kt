package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.AngularOffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.conversion.LinearOffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class PivotComponent(hardware: PivotHardware) : Component<PivotComponent, PivotHardware, OffloadedOutput>(hardware) {

    val collectAngle by pref(-90, Degree)
    val purgeAngle by pref(0, Degree)


    override val fallbackController: PivotComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun PivotHardware.output(value: OffloadedOutput)  = lazyOutput(value)

}

class PivotHardware : SubsystemHardware<PivotHardware, PivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 10.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Pivot"

    val operatingVoltage by pref(11.5, Volt)
    val currentLimit by pref(10, Ampere)
    val maxOutput by pref(70, Percent)

    // SAFETY
    val maxRotation by pref(0, Degree)
    val minRotation by pref(-90, Degree)

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(0) //TODO
        val perFeedbackQuantity by pref(0, Degree) // TODO
        val zeroOffset by pref(0, Degree)

        ({
            AngularOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = nativeFeedbackUnits, perFeedbackQuantity = perFeedbackQuantity,
                    feedbackZero = zeroOffset
            )
        })
    }

    val idx = 0

    val escCanId by pref(20)
    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, FeedbackDevice.Analog)

        val t = 5000

        // SAFETY
        it.configPeakOutputForward(maxOutput.siValue, t)
        it.configPeakOutputReverse(-maxOutput.siValue, t)

        it.configReverseSoftLimitThreshold(offloadedSettings.native(minRotation).toInt(), t)
        it.configReverseSoftLimitEnable(true, t)

        it.configForwardSoftLimitThreshold(offloadedSettings.native(maxRotation).toInt(), t)
        it.configForwardSoftLimitEnable(true, t)
    }
    val lazyOutput = lazyOutput(esc, idx)
    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
            .with(graph("Angle", Degree))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }

}

