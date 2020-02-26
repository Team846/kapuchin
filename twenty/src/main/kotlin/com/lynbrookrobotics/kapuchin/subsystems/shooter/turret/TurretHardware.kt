package com.lynbrookrobotics.kapuchin.subsystems.shooter.turret

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 15.milli(Second)
    override val priority = Priority.High
    override val name = "Shooter Turret"

    private val invert by pref(false)
    private val limitSwitchOffset by pref(116 + 78.8, Degree)
    val escConfig by escConfigPref(
            defaultNominalOutput = 1.Volt,
            defaultContinuousCurrentLimit = 15.Ampere,
            defaultPeakCurrentLimit = 25.Ampere
    )

    val conversions = TurretConversions(this)
    var isZeroed = false
        set(value) {
            log(Debug) { "Setting isZeroed to $value" }
            field = value
        }

    private val escId = 52

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }
    val pidController by hardw { esc.pidController }
    val encoder by hardw { esc.encoder }

    private val limitSwitch by hardw { esc.getForwardLimitSwitch(kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }

    val position = sensor(encoder) {
        conversions.encoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    val atZero = sensor(limitSwitch) { get() stampWith it }
            .with(graph("At Zero", Each)) { (if (it) 1 else 0).Each }

    fun zero() {
        if (isZeroed) log(Error) { "Already zeroed!" }
        else {
            val originalPosition = encoder.position
            val zeroedPosition = conversions.encoder.native(limitSwitchOffset)
            log(Warning) { "Zeroing ESC position from ${originalPosition withDecimals 2} to ${zeroedPosition withDecimals 2}" }
            encoder.position = zeroedPosition
            isZeroed = true
        }
    }

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(atZero, position).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}