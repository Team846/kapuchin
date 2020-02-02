package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {
    val carouselSpeed by pref(6, Percent)

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(carouselEsc)
    }
}


class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Carousel"


    val rotationHallEffect by hardw { DigitalInput(2) }


    val proximity by hardw {
        DigitalInput(1)
    }

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val carouselEscId by pref(10)
    val carouselEsc by hardw { CANSparkMax(carouselEscId, kBrushless) }
}