package com.lynbrookrobotics.kapuchin.subsystems.intake.collector

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.wpilibj.Encoder
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CollectorSliderComponent(hardware: CollectorSliderHardware) : Component<CollectorSliderComponent, CollectorSliderHardware, DutyCycle>(hardware, EventLoop) {

    //positive is to the robot's right
    //negative is to the robot's left

    val defaultPosition by pref(0, Inch)

    val kP = 10.Percent / 2.Inch

    override val fallbackController: CollectorSliderComponent.(Time) -> DutyCycle = {
        kP * (defaultPosition - hardware.position.optimizedRead(it, 5.milli(Second)).y)
    }

    override fun CollectorSliderHardware.output(value: DutyCycle) {
        hardware.esc.set(value.Each)
    }

}

class CollectorSliderHardware : SubsystemHardware<CollectorSliderHardware, CollectorSliderComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Collector Slider"

    val operatingVoltage by pref(12, Volt)
    val currentLimit by pref(30, Ampere)
    val startupFrictionCompensation by pref(1.4, Volt)

    val range by pref(3)

    val escCanId by pref(20)
    val esc by hardw { CANSparkMax(escCanId, MotorType.kBrushless) }

    val chA by pref(10)
    val chB by pref(11)
    val resolution by pref(42)

    val distance by pref(16, Inch)
    val perDegree by pref(1080, Degree)

    val encoder = Encoder(chA, chB)

    val position = sensor {
        distance * (EncoderConversion(resolution, 360.Degree).angle(encoder.raw.toDouble()) / perDegree) stampWith it
    }

    init {
        EventLoop.runOnTick { position.optimizedRead(it, syncThreshold) }
    }
}