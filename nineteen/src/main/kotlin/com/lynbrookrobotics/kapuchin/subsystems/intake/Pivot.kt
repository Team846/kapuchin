package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.AngularOffloadedNativeConversion
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.hardware.unaryPlus
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.milli

class PivotComponent(hardware: PivotHardware) : Component<PivotComponent, PivotHardware, OffloadedOutput>(hardware) {


    //The `up` position is 90 Degrees. You can only be in [0 Degrees, 90 Degrees].
    val downPosition by pref(0, Degree)
    val collectPosition by pref (10, Degree)
    val plateHandoffPosition by pref (60, Degree)
    val upPosition by pref(90, Degree)


    override val fallbackController: PivotComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun PivotHardware.output(value: OffloadedOutput)  = lazyOutput(value)

}

class PivotHardware : SubsystemHardware<PivotHardware, PivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Pivot"

    val operatingVoltage by pref(11.5, Volt)
    val currentLimit by pref(10, Ampere)
    val maxOutput by pref(30, Percent)

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

        // SAFETY
        +it.configPeakOutputForward(maxOutput.siValue, timeout)
        +it.configPeakOutputReverse(-maxOutput.siValue, timeout)

        +it.configReverseSoftLimitThreshold(offloadedSettings.native(minRotation).toInt(), timeout)
        +it.configReverseSoftLimitEnable(true, timeout)

        +it.configForwardSoftLimitThreshold(offloadedSettings.native(maxRotation).toInt(), timeout)
        +it.configForwardSoftLimitEnable(true, timeout)
    }
    val lazyOutput = lazyOutput(esc, idx)
    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
            .with(graph("Angle", Degree))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }

}

