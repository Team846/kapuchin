package com.lynbrookrobotics.kapuchin.subsystems.shooter.turret

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val priority = Priority.High
    override val name = "Shooter Turret"

    val escConfig by escConfigPref(
            defaultNominalOutput = 1.Volt,
            defaultContinuousCurrentLimit = 15.Ampere,
            defaultPeakCurrentLimit = 25.Ampere
    )

    private val escId = 52
    private val invert by pref(false)
    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }
    val pidController by hardw { esc.pidController }

    val conversions = TurretConversions(this)

    val encoder by hardw { esc.encoder }
    val position = sensor(encoder) {
        conversions.encoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    private val limitSwitch by hardw { esc.getForwardLimitSwitch(kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }
    val atZero = sensor(limitSwitch) { get() stampWith it }
            .with(graph("At Zero", Each)) { (if (it) 1 else 0).Each }

    var isZeroed = false
        private set

    private val limitSwitchMount by pref(90, Degree)
    fun zero() {
        encoder.position = conversions.encoder.native(limitSwitchMount)
        isZeroed = true
    }

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(atZero, position).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}