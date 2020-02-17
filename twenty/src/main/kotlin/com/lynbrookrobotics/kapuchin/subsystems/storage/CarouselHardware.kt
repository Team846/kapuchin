package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.ColorSensorV3
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.I2C.Port.kMXP
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class CarouselHardware : SubsystemHardware<CarouselHardware, CarouselComponent>() {
    override val period = 50.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Carousel"

    private val invert by pref(false)
    val escConfig by escConfigPref(
            defaultNominalOutput = 1.Volt,
            defaultContinuousCurrentLimit = 15.Ampere,
            defaultPeakCurrentLimit = 25.Ampere
    )

    private val escId = 60
    private val hallEffectChannel = 0

    val conversions = CarouselConversions(this)
    var isZeroed = false
        private set

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
        it.idleMode = CANSparkMax.IdleMode.kCoast
    }
    val pidController by hardw { esc.pidController }

    val encoder by hardw { esc.encoder }
    val position = sensor(encoder) {
        conversions.encoder.realPosition(position) stampWith it
    }.with(graph("Angle", Degree))

    private val hallEffect by hardw { DigitalInput(hallEffectChannel) }.configure { dio ->
        dio.setUpSourceEdge(true, false)
        dio.requestInterrupts {
            encoder.position = position.optimizedRead(
                    dio.readRisingTimestamp().Second, syncThreshold
            ).y.roundToInt(CarouselSlot).let(conversions.encoder::native)
            isZeroed = true
        }
        dio.enableInterrupts()
    }
    val alignedToSlot = sensor(hallEffect) { get() stampWith it }
            .with(graph("Aligned to Slot", Each)) { (if (it) 1 else 0).Each }

    private val colorSensor by hardw { ColorSensorV3(kMXP) }
    private val colorNamed = Named("Color Sensor", this)
    val color = sensor(colorSensor) { color stampWith it }
            .with(graph("R", Percent, colorNamed)) { it.red.Each }
            .with(graph("G", Percent, colorNamed)) { it.green.Each }
            .with(graph("B", Percent, colorNamed)) { it.blue.Each }
            .with(graph("Accuracy", Each, colorNamed)) { conversions.accuracy(it).Each }

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