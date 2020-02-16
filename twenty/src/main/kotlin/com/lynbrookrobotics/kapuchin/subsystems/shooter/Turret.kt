package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANDigitalInput
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, OffloadedOutput>(hardware) {

    // TODO position gains
    // TODO native encoder to position conversions
    // TODO zeroing

    override val fallbackController: TurretComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun TurretHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
    }

}

class TurretHardware : SubsystemHardware<TurretHardware, TurretComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.High
    override val name: String = "Shooter Turret"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val conversions = TurretConversions(this)

    private val escId = 52

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }

    val pidController by hardw { esc.pidController!! }

    private val limitSwitch: CANDigitalInput by hardw { esc.getForwardLimitSwitch(kNormallyOpen) }.configure {
        +it.enableLimitSwitch(true)
    }
    val atZero = sensor(limitSwitch) { get() stampWith it }
            .with(graph("At Zero", Each)) { (if (it) 1 else 0).Each }

    val encoder by hardw { esc.encoder }
    val position = sensor(encoder) { position stampWith it }
            .with(graph("Angle", Degree))

    var isZeroed = false
        private set

    private val limitSwitchPos by pref(45, Degree)

    fun zero() = with(conversions) {
        encoder.position = turretToMotor(limitSwitchPos).Turn
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
