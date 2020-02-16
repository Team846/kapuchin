package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.*
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.EncoderType.kHallSensor
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.roundToInt

class CarouselComponent(hardware: CarouselHardware) : Component<CarouselComponent, CarouselHardware, OffloadedOutput>(hardware) {

    // TODO position gains

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

    private val carouselRadius by pref(10, Inch)
    private val wheelRadius by pref(1, Inch)

    private val escId = 60
    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }
    val encoder: CANEncoder by hardw { esc.getEncoder(kHallSensor, 5) }.configure { encoder ->
        encoder.positionConversionFactor = (wheelRadius / carouselRadius).Each // TODO check if this is correct
    }
    val pidController: CANPIDController by hardw { esc.pidController }

    private val hallEffectPort = 0
    private val hallEffect by hardw { DigitalInput(hallEffectPort) }
    val isHallEffect = sensor(hallEffect) { get() stampWith it }

    val position = sensor(encoder) { encoder.position.Turn stampWith it }
    val slotAtCollect = sensor {
        (encoder.position * 5).roundToInt() stampWith it
    }

    private val colorMatcher = ColorMatch().apply {
        addColorMatch(FieldColors.BallYellow.color)
    }

    private val colorSensorV3 by hardw { ColorSensorV3(Port.kMXP) }
    val isBallInCollect = sensor { isBall(colorMatcher.matchClosestColor(colorSensorV3.color).color) stampWith it }

    private fun isBall(color: Color): Boolean {
        return color.red == FieldColors.BallYellow.color.red &&
                color.green == FieldColors.BallYellow.color.green &&
                color.blue == FieldColors.BallYellow.color.blue;
    }

    val magazineState = booleanArrayOf(false, false, false, false, false)
    val magazine = sensor { magazineState stampWith it }

}
