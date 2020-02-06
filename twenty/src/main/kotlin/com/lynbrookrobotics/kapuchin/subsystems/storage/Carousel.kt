package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANEncoder
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.EncoderType.kHallSensor
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {
    val carouselSpeed by pref(6, Percent)

    val slotDetectTolerance by pref(2, Degree)

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(carouselEsc, carouselEscPidController)
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

    val positionGains by pref {
        val kP by pref(1.0)
        val kI by pref(1.0)
        val kD by pref(1.0)
        ({ OffloadedEscGains(30.Millisecond, kP, kI, kD) })
    }

    private val ticksPerRevolution = 5

    private val carouselEscId = 10
    val carouselEsc by hardw { CANSparkMax(carouselEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
    }
    val carouselEscPidController: CANPIDController by hardw { carouselEsc.pidController }
    private val carouselEncoder: CANEncoder by hardw { carouselEsc.getEncoder(kHallSensor, ticksPerRevolution) }.configure {
        it.positionConversionFactor = 360.0
    }

    private val magazineState = booleanArrayOf(false, false, false, false, false)
    val magazine = sensor { magazineState stampWith it }

    val angle = sensor { carouselEncoder.position.Degree stampWith it }
}