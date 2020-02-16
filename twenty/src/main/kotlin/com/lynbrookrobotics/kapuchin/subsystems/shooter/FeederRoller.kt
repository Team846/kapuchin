package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FeederRollerComponent(hardware: FeederRollerHardware) : Component<FeederRollerComponent, FeederRollerHardware, OffloadedOutput>(hardware, pneumaticTicker) {

    val maxSpeed by pref(5676, Rpm)

    val feedSpeed by pref(1000, Rpm)
    val tolerance by pref(10, Rpm)

    val velocityGains by pref {
        val kP by pref(10, Volt, 100, Rpm)
        val kF by pref(110, Percent)
        ({
            OffloadedEscGains(
                    syncThreshold = hardware.syncThreshold,
                    kP = hardware.conversions.native(kP),
                    kF = hardware.conversions.native(
                            Gain(12.Volt, maxSpeed)
                    ) * kF.Each
            )
        })
    }

    override val fallbackController: FeederRollerComponent.(Time) -> OffloadedOutput = {
        VelocityOutput(hardware.escConfig, velocityGains, 0.0)
    }

    override fun FeederRollerHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
    }
}

class FeederRollerHardware : SubsystemHardware<FeederRollerHardware, FeederRollerComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Medium
    override val name = "Feeder Roller"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,
            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere,
            defaultVoltageCompSaturation = 11.Volt
    )

    private val escId = 61
    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invert
    }
    val encoder by hardw { esc.encoder }
    val pidController by hardw { esc.pidController }

    val velocity = sensor(encoder) { conversions.realVelocity(encoder.velocity) stampWith it }

    val conversions = AngularOffloadedNativeConversion(::p, ::p, ::p, ::p,
            nativeOutputUnits = 1, perOutputQuantity = escConfig.voltageCompSaturation,
            nativeFeedbackUnits = 1, perFeedbackQuantity = 1.Turn,
            nativeTimeUnit = 1.Minute, nativeRateUnit = 1.milli(Second)
    )
}
