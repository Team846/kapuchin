package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, DutyCycle>(hardware) {
    val carouselspeed by pref(6, Volt)
    override val fallbackController: CarouselComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun CarouselHardware.output(value: DutyCycle) {
        carouselEsc.set(value.Each)
    }
}


class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "StorageBelt"

    private val carouselEscId by pref(10)
    private val carouselEscInversion by pref(false)


    val halleffect by hardw { DigitalInput(2) }

    val proximity by hardw {
        DigitalInput(1)
    }

    val carouselEsc by hardw { CANSparkMax(carouselEscId, kBrushless) }.configure {
        it.inverted = carouselEscInversion
    }


}