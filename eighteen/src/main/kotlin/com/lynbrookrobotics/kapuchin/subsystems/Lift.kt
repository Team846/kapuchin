package com.lynbrookrobotics.kapuchin.subsystems

import com.ctre.phoenix.motorcontrol.FeedbackDevice.Analog
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.loops.pid.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {
    val positionGains by pref {
        val kP by pref(12, Volt, 8, Inch)
        val kI by pref(0, Volt, 1, FootSecond)
        val kD by pref(0, Volt, 1, FootPerSecond)
        ({ PidGains(kP, kI, kD) })
    }

    val cubeHeight by pref(13, Inch)

    val collectHeight by pref(0, Inch)
    val exchangeHeight by pref(4, Inch)
    val switchHeight by pref(25, Inch)
    val lowScaleHeight by pref(53, Inch)
    val highScaleHeight by pref(75, Inch)
    val positionTolerance by pref(2, Inch)

    val twistAdjustRange by pref(10, Inch)

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }
    override fun LiftHardware.output(value: OffloadedOutput) = lazyOutput(value)

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}

class LiftHardware : SubsystemHardware<LiftHardware, LiftComponent>() {
    override val priority = Priority.Low
    override val period = 20.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "Lift"

    val operatingVoltage by pref(12, Volt)
    val currentLimit by pref(30, Ampere)

    val jitterPulsePinNumber by pref(6)
    val jitterReadPinNumber by pref(7)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    // SAFETY
    val maxHeight by pref(80, Inch)
    val minHeight by pref(0, Inch)

    val offloadedSettings by pref {
        val nativeFeedbackUnits by pref(615)
        val perFeedbackQuantity by pref(80.25, Inch)
        val zeroOffset by pref(11.2, Inch)

        ({
            LinearOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = operatingVoltage,
                    nativeFeedbackUnits = nativeFeedbackUnits, perFeedbackQuantity = perFeedbackQuantity,
                    feedbackZero = zeroOffset
            )
        })
    }

    val escCanId by pref(20)
    val maxOutput by pref(70, Percent)
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
            .with(graph("Height", Inch))

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }
}