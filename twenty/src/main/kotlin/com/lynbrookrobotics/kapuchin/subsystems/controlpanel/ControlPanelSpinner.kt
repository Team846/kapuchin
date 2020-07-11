package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.Subsystems.Companion.sharedTickerTiming
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMax.IdleMode
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.ColorSensorV3
import com.revrobotics.ColorSensorV3.*
import edu.wpi.first.wpilibj.I2C.Port
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) :
    Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, DutyCycle>(hardware) {

    val kP by pref(10, Volt, 1, Turn)
    val kD by pref(10, Volt, 60, Rpm)

    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun ControlPanelSpinnerHardware.output(value: DutyCycle) {
        spinnerEsc.set(value.Each)
    }
}

class ControlPanelSpinnerHardware(driver: DriverHardware) :
    SubsystemHardware<ControlPanelSpinnerHardware, ControlPanelSpinnerComponent>() {
    override val period by sharedTickerTiming
    override val syncThreshold = 20.milli(Second)
    override val priority = Priority.High
    override val name = "Control Panel"

    private val invert by pref(false)
    val escConfig by escConfigPref()

    val conversions = ControlPanelConversions(this)

    private val escId = 20

    val spinnerEsc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        +it.setIdleMode(IdleMode.kCoast)
        it.inverted = invert
    }
    val encoder by hardw { spinnerEsc.encoder }

    val encoderPosition = sensor(encoder) { conversions.encoderPositionDelta(position.Turn) stampWith it }

    val colorSensor by hardw { ColorSensorV3(Port.kOnboard) }.configure {
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
        // TODO: Must test empirically
        true
    }

    private val colorNamed = Named("Color Sensor", this)
    val color = sensor(colorSensor) { color stampWith it }
        .with(graph("R", Percent, colorNamed)) { it.red.Each }
        .with(graph("G", Percent, colorNamed)) { it.green.Each }
        .with(graph("B", Percent, colorNamed)) { it.blue.Each }

    val proximity = sensor(colorSensor) { proximity.Each / 2047 stampWith it }
        .with(graph("IR", Percent, colorNamed))

    val targetColor = sensor {
        when (driver.station.gameSpecificMessage.trim()) {
            "" -> null
            "B" -> conversions.blue
            "G" -> conversions.green
            "R" -> conversions.red
            "Y" -> conversions.yellow
            else -> null
        } stampWith it
    }
        .with(graph("Target Color", Each)) { (it?.run(conversions::indexColor) ?: -1).Each }

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(color, proximity, targetColor).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}