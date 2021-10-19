package com.lynbrookrobotics.twenty.subsystems.carousel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import com.revrobotics.ColorSensorV3
import com.revrobotics.ColorSensorV3.*
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val period = 50.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val priority = Priority.High
    override val name = "Carousel"

    private val invert by pref(false)
    val escConfig by escConfigPref(
        defaultNominalOutput = 1.Volt,
        defaultContinuousCurrentLimit = 15.Ampere,
        defaultPeakCurrentLimit = 25.Ampere
    )

    private val escId = 60
    private val hallEffectChannel = 1

    val conversions = CarouselConversions(this)
    var isZeroed = false

    val esc by hardw { CANSparkMax(escId, MotorType.kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
        +it.setIdleMode(IdleMode.kCoast)
    }

    val pidController by hardw { esc.pidController!! }

    val encoder by hardw { esc.encoder!! }.configure {
        it.position = 0.0
    }

    val position = sensor(encoder) {
        conversions.encoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))
        .with(graph("Error off slot", Degree)) { it - it.roundToInt(CarouselSlot).CarouselSlot }

    fun nearestSlot() = position.optimizedRead(currentTime,
        0.Second).y.roundToInt(CarouselSlot).CarouselSlot

    // Sensor is electrically inverted
    private val hallEffect by hardw { DigitalInput(hallEffectChannel) }.configure { dio ->
        dio.requestInterrupts {
            encoder.position = conversions.encoder.native(nearestSlot())
            isZeroed = true
        }
        dio.setUpSourceEdge(false, true)
        dio.enableInterrupts()
    }

    private val alignedToSlot = sensor(hallEffect) { get() stampWith it }
        .with(graph("Aligned to Slot", Each)) { (if (it) 1 else 0).Each }

    private val colorSensor by hardw { ColorSensorV3(Port.kOnboard) }.configure {
        it.configureColorSensor(
            ColorSensorResolution.kColorSensorRes18bit,
            ColorSensorMeasurementRate.kColorRate25ms,
            GainFactor.kGain3x
        )
        it.configureProximitySensor(
            ProximitySensorResolution.kProxRes11bit,
            ProximitySensorMeasurementRate.kProxRate6ms
        )
        it.configureProximitySensorLED(LEDPulseFrequency.kFreq60kHz, LEDCurrent.kPulse125mA, 8)
    }.verify("the color sensor is connected") {
        it.red != 0 && it.green != 0 && it.blue != 0
    }

    val proximity = sensor(colorSensor) { proximity.Each / 2047 stampWith it }
        .with(graph("IR", Percent))

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(alignedToSlot, position, proximity).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}