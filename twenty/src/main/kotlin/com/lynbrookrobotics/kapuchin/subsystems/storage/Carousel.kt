package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port.kOnboard
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {

    private val carouselRadius by pref(10, Inch)
    private val wheelRadius by pref(1, Inch)

    // TODO position gains
    // TODO native encoder to carousel position conversions
    // TODO "rezero" when hall effect is on

    override val fallbackController: CarouselComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun CarouselHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.High
    override val name: String = "Shooter Carousel"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val escId = 60
    private val hallEffectPort = 0

    private val magazineState = booleanArrayOf(false, false, false, false, false)

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }

    val pidController by hardw { esc.pidController!! }
    val hallEffect by hardw { DigitalInput(hallEffectPort) }

    // TODO position sensor
    val colorSensor = sensor(RevColorSensor(kOnboard, 0x52)) { getCurrentValue() stampWith it }
    val magazine = sensor { magazineState stampWith it }
}