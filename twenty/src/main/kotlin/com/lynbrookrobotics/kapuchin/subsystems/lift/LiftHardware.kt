package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class LiftHardware : SubsystemHardware<LiftHardware, LiftComponent>() {
    override val name: String = "Lift"
    override val period: Time = 30.milli(Second)
    override val priority: Priority = Priority.Low
    override val syncThreshold: Time = 5.milli(Second)

    val escCanId = 40
    val idx = 0

    val invert by pref(false)
    val invertSensor by pref(false)

    val conversions = LiftConversions(this)

    val escConfig by escConfigPref(
            defaultNominalOutput = 1.Volt,

            defaultContinuousCurrentLimit = 20.Ampere,
            defaultPeakCurrentLimit = 35.Ampere,
            defaultPeakCurrentDuration = 0.5.Second
    )

    val esc by hardw { TalonSRX(escCanId) }.configure {
        setupMaster(it, escConfig, FeedbackDevice.Analog, false)

        it.inverted = invert
        it.setSensorPhase(invertSensor)
    }.verify("soft-limits are set correctly") {
        PercentOutput(escConfig, 0.Percent, conversions.safeties).writeTo(it)

        val configs = TalonSRXConfiguration()
        it.getAllConfigs(configs, configTimeout)

        configs.reverseSoftLimitThreshold ==
                conversions.safeties.min?.roundToInt() &&
                configs.forwardSoftLimitThreshold ==
                conversions.safeties.max?.roundToInt()
    }

    val nativeGrapher = graph("Native", Each)
    val position = sensor { t ->
        conversions.native.realPosition(
                esc.getSelectedSensorPosition(idx).also { nativeGrapher(t, it.Each) }
        ) stampWith t
    }
            .with(graph("Height", Inch))

    init {
        uiBaselineTicker.runOnTick { position.optimizedRead(it, .5.Second) }
    }

}