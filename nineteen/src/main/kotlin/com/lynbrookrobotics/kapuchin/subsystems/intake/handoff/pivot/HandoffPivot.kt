package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration
import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.system.measureTimeMillis

class HandoffPivotComponent(hardware: HandoffPivotHardware) : Component<HandoffPivotComponent, HandoffPivotHardware, OffloadedOutput>(hardware, EventLoop) {

    val collectPosition by pref(10, Degree)
    val handoffPosition by pref(60, Degree)

    val kP by pref(5, Volt, 30, Degree)
    val kD by pref(0, Volt, 360, DegreePerSecond)

    override val fallbackController: HandoffPivotComponent.(Time) -> OffloadedOutput = { PercentOutput(0.Percent) }

    private var lastReverseSoftLimit = Integer.MAX_VALUE
    private var lastForwardSoftLimit = Integer.MIN_VALUE
    override fun HandoffPivotHardware.output(value: OffloadedOutput) {
//        val current = position.optimizedRead(currentTime, 0.Second).y
//
//        val range = unionizeAndFindClosestRange(HandoffPivotState.legalRanges(), current, (Int.MIN_VALUE + 1).Degree)
//
//        if (range.start - range.endInclusive != 0.Degree) {
//            val reverseSoftLimit = conversions.native.native(range.start).toInt()
//            if (reverseSoftLimit != lastReverseSoftLimit) {
//                lastReverseSoftLimit = reverseSoftLimit
//                esc.configReverseSoftLimitThreshold(reverseSoftLimit)
//                println("setting reverse soft limit to: ${range.start.Degree}")
//            }
//
//            val forwardSoftLimit = conversions.native.native(range.endInclusive).toInt()
//            if (forwardSoftLimit != lastForwardSoftLimit) {
//                lastForwardSoftLimit = forwardSoftLimit
//                esc.configForwardSoftLimitThreshold(forwardSoftLimit)
//                println("setting forward soft limit to: ${range.endInclusive.Degree}")
//            }
        lazyOutput(value)
//        } else if (Safeties.log) {
//            log(Warning) { "No legal states found" }
//        }
    }
}

class HandoffPivotHardware : SubsystemHardware<HandoffPivotHardware, HandoffPivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 15.milli(Second)
    override val name: String = "Handoff Pivot"

    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(10, Ampere)
    val startupFrictionCompensation by pref(0.5, Volt)
    val maxOutput by pref(10, Percent)

    val invert by pref(true)
    val invertSensor by pref(false)

    private val idx = 0

    val conversions = HandoffPivotConversions(this)

    val escCanId by pref(30)
    val esc by hardw { TalonSRX(escCanId) }.configure {
        configMaster(it, operatingVoltage, currentLimit, startupFrictionCompensation, FeedbackDevice.Analog)

        it.inverted = invert
        it.setSensorPhase(invertSensor)

        // SAFETY
        +it.configPeakOutputForward(maxOutput.siValue, configTimeout)
        +it.configPeakOutputReverse(-maxOutput.siValue, configTimeout)

        with(conversions) {
            +it.configReverseSoftLimitThreshold(native.native(minPt.first).toInt(), configTimeout)
            +it.configReverseSoftLimitEnable(true, configTimeout)

            +it.configForwardSoftLimitThreshold(native.native(maxPt.first).toInt(), configTimeout)
            +it.configForwardSoftLimitEnable(true, configTimeout)
        }
    }.verify("soft-limits are set correctly") {
        val configs = TalonSRXConfiguration()
        it.getAllConfigs(configs, configTimeout)
        configs.reverseSoftLimitThreshold == conversions.minPt.second &&
                configs.forwardSoftLimitThreshold == conversions.maxPt.second
    }

    val lazyOutput = lazyOutput(esc, idx)

    val nativeGrapher = graph("Native", Each)
    val position = sensor(esc) { t ->
        conversions.native.realPosition(
                getSelectedSensorPosition(idx).also { nativeGrapher(t, it.Each) }
        ) stampWith t
    }
            .with(graph("Angle", Degree))

    init {
        uiBaselineTicker.runOnTick { position.optimizedRead(it, .5.Second) }
    }
}
