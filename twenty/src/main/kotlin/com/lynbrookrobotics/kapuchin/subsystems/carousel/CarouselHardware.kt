package com.lynbrookrobotics.kapuchin.subsystems.carousel

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.ColorSensorV3
import com.revrobotics.ColorSensorV3.*
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port.kOnboard
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
        set(value) {
            log(Debug) { "Setting isZeroed to $value" }
            field = value
        }

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
        +it.setIdleMode(IdleMode.kCoast)
    }
    val pidController by hardw { esc.pidController }

    val encoder by hardw { esc.encoder }
    val position = sensor(encoder) {
        conversions.encoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    // Sensor is electrically inverted
    private val hallEffect by hardw { DigitalInput(hallEffectChannel) }.configure { dio ->
        dio.requestInterrupts {
            encoder.position = conversions.encoder.native(position.optimizedRead(
                    dio.readFallingTimestamp().Second, syncThreshold
            ).y.roundToInt(CarouselSlot))
//          log(Debug) { "Running hall effect ISR" }
            isZeroed = true
        }
        dio.setUpSourceEdge(false, true)
        dio.enableInterrupts()
    }
    val alignedToSlot = sensor(hallEffect) { get() stampWith it }
            .with(graph("Aligned to Slot", Each)) { (if (it) 1 else 0).Each }

    private val colorSensor by hardw { ColorSensorV3(kOnboard) }.configure {
        it.configureColorSensor(ColorSensorResolution.kColorSensorRes18bit, ColorSensorMeasurementRate.kColorRate25ms, GainFactor.kGain3x)
        it.configureProximitySensor(ProximitySensorResolution.kProxRes11bit, ProximitySensorMeasurementRate.kProxRate6ms)
        it.configureProximitySensorLED(LEDPulseFrequency.kFreq60kHz, LEDCurrent.kPulse125mA, 8)
    }.verify("the color sensor is connected") {
        it.proximity.Each / 2047 > 50.Percent
    }
    private val colorNamed = Named("Color Sensor", this)
    val color = sensor(colorSensor) { color stampWith it }
            .with(graph("R", Percent, colorNamed)) { it.red.Each }
            .with(graph("G", Percent, colorNamed)) { it.green.Each }
            .with(graph("B", Percent, colorNamed)) { it.blue.Each }
            .with(graph("Similarity", Each, colorNamed)) { conversions.similarity(it).Each }

    val proximity = sensor(colorSensor) { proximity.Each / 2047 stampWith it }
            .with(graph("IR", Percent, colorNamed))

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(alignedToSlot, position, color, proximity).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}