package com.lynbrookrobotics.kapuchin.subsystems.Handoff.handoff

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class HandoffPivotComponent(hardware: HandoffPivotHardware) : Component<HandoffPivotComponent, HandoffPivotHardware, OffloadedOutput>(hardware) {


    //The `up` position is 90 Degrees. You can only be in [0 Degrees, 90 Degrees].
    val downPosition by pref(0, Degree)
    val collectPosition by pref(10, Degree)
    val plateHandoffPosition by pref(60, Degree)
    val upPosition by pref(90, Degree)

    val positionGains by pref {
        val kP by pref(10, Volt, 2, Degree)
        ({
            OffloadedPidGains(
                    hardware.offloadedSettings.native(kP),
                    0.0, 0.0,
                    hardware.offloadedSettings.native(
                            Gain(hardware.operatingVoltage, downPosition)
                    )
            )
        })
    }

    override val fallbackController: HandoffPivotComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    override fun HandoffPivotHardware.output(value: OffloadedOutput) = lazyOutput(value)

}

class HandoffPivotHardware : SubsystemHardware<HandoffPivotHardware, HandoffPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Handoff Pivot"

    val operatingVoltage by pref(11.5, Volt)
    val currentLimit by pref(10, Ampere)
    val startupFrictionCompensation by pref(1.4, Volt)

    // SAFETY
    val min by pref {
        val real by pref(0, Degree)
        val native by pref(0)
        ({ Pair(real, native) })
    }

    val max by pref {
        val real by pref(90, Degree)
        val native by pref(1023)
        ({ Pair(real, native) })
    }

    val conversion by pref {
        { Pair(max.first - min.first, max.second - min.second) }
    }

    val offloadedSettings by pref {

        val zeroOffset by pref(90, Degree)

        ({
            AngularOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = conversion.second, perFeedbackQuantity = conversion.first,
                    feedbackZero = zeroOffset
            )
        })
    }

    val idx = 0


    val escCanId by pref(20)
    val maxOutput by pref(30, Percent)

    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, FeedbackDevice.Analog)

        // SAFETY
        +it.configPeakOutputForward(maxOutput.siValue, configTimeout)
        +it.configPeakOutputReverse(-maxOutput.siValue, configTimeout)

        +it.configReverseSoftLimitThreshold(offloadedSettings.native(min.first).toInt(), configTimeout)
        +it.configReverseSoftLimitEnable(true, configTimeout)

        +it.configForwardSoftLimitThreshold(offloadedSettings.native(max.first).toInt(), configTimeout)
        +it.configForwardSoftLimitEnable(true, configTimeout)
    }
    val lazyOutput = lazyOutput(esc, idx)
    val position = sensor { offloadedSettings.realPosition(esc.getSelectedSensorPosition(idx)) stampWith it }
            .with(graph("Angle", Degree))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }

}

