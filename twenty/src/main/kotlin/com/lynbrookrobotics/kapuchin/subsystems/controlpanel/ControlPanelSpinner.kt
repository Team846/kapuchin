package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
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
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.ColorSensorV3
import edu.wpi.first.wpilibj.I2C.Port
import info.kunalsheth.units.generated.*

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) : Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, DutyCycle>(hardware, pneumaticTicker) {
    val kP by pref(10, Volt, 1, Turn)

    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun ControlPanelSpinnerHardware.output(value: DutyCycle) {
        spinnerEsc.set(value.Each)
    }
}

class ControlPanelSpinnerHardware(driver: DriverHardware) : SubsystemHardware<ControlPanelSpinnerHardware, ControlPanelSpinnerComponent>() {
    override val period = sharedTickerTiming()
    override val syncThreshold = sharedTickerTiming()
    override val priority = Priority.Medium
    override val name = "Control Panel"

    val escConfig by escConfigPref()
    private val invert by pref(false)

    private val escId = 20
    val spinnerEsc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.idleMode = CANSparkMax.IdleMode.kCoast
        it.inverted = invert
    }

    val colorSensor by hardw { ColorSensorV3(Port.kOnboard) }.configure {
        // TODO: Wesley, what color and proximity settings should we use here?
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

    val conversions = ControlPanelConversions(this)
    val targetColor = sensor {
        when (driver.station.gameSpecificMessage.trim()) {
            "" -> null
            "B" -> conversions.blue
            "G" -> conversions.green
            "R" -> conversions.red
            "Y" -> conversions.yellow
        } stampWith it
    }
}