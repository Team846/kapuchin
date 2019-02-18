package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.lynbrookrobotics.kapuchin.Subsystems.uiBaselineTicker
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorSliderComponent(hardware: CollectorSliderHardware) : Component<CollectorSliderComponent, CollectorSliderHardware, DutyCycle>(hardware) {

    val kP by pref(3, Volt, 1, Inch)

    override val fallbackController: CollectorSliderComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun CollectorSliderHardware.output(value: DutyCycle) {
        hardware.esc.set(value.Each)
    }
}

class CollectorSliderHardware : SubsystemHardware<CollectorSliderHardware, CollectorSliderComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 15.milli(Second)
    override val name: String = "Collector Slider"

    val operatingVoltage by pref(11, Volt)
    val currentLimit by pref(20, Ampere)
    val startupFrictionCompensation by pref(0.5, Volt)

    val escCanId by pref(20)
    val limitSwitchChannel by pref(3)

    val esc by hardw { CANSparkMax(escCanId, MotorType.kBrushless) }.configure {
        it.setSmartCurrentLimit(currentLimit.Ampere.toInt())
    }
    val encoder by hardw { esc.encoder }
    val limitSwitch by hardw { DigitalInput(limitSwitchChannel) }

    val conversion by pref {
        val encoderRotations by pref(10, Turn)
        val perSliderDistance by pref(1, Inch)

        val resolution by pref(42)
        ({
            val enc = EncoderConversion(resolution, 1.Turn)
            fun(ticks: Double) = enc.angle(ticks - zeroOffset) / encoderRotations * perSliderDistance
        })
    }

    val position = sensor(encoder) { conversion(position) stampWith it }
    val outOfRange = sensor { limitSwitch.get() stampWith it }

    private var zeroOffset = 0.0
    fun zero() { zeroOffset = encoder.position }

    init {
        uiBaselineTicker.runOnTick { position.optimizedRead(it, 1.Second) }
    }
}