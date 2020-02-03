package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANEncoder
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.EncoderType.kHallSensor
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.util.LinkedList


class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {
    val carouselSpeed by pref(6, Percent)

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(carouselEsc)
    }
}


@Suppress("EXPERIMENTAL_API_USAGE")
class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Carousel"


    val rotationHallEffect by hardw { DigitalInput(2) }

    val colorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, 0x52)) { getCurrentValue() stampWith it }

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val ticksPerRevolution = 5

    private val carouselEscId = 10
    val carouselEsc by hardw { CANSparkMax(carouselEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
    }
    val carouselEncoder: CANEncoder by hardw { carouselEsc.getEncoder(kHallSensor, ticksPerRevolution) }


    private val magazineState = LinkedList<Boolean>(
            listOf(false, false, false, false, false)
    )
    val magazine = sensor { magazineState stampWith it }

    private val angleBySensor = sensor { carouselEncoder.position * 360.Degree stampWith it }
}