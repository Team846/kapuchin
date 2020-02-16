package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.FieldColors.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.ColorMatch
import com.revrobotics.ColorSensorV3
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C.Port
import edu.wpi.first.wpilibj.util.Color
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class FieldColors(val color: Color) {
    Blue(Color(0.3044677734375, 0.54288330078125, 0.15262451171875)),
    Green(Color(0.2068115234375, 0.53885498046875, 0.2543212890625)),
    Red(Color(0.39801025390625, 0.4095703125, 0.1924072265625)),
    Yellow(Color(0.3044677734375, 0.54288330078125, 0.15262451171875)),
    BallYellow(Color(0.3006591796875, 0.53218994140625, 0.16715087809625))
}

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) : Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, DutyCycle>(hardware) {
    val kP by pref(10, Volt, 1440, Degree)
    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> DutyCycle = { 0.Percent }
    override fun ControlPanelSpinnerHardware.output(value: DutyCycle) {
        spinnerEsc.set(value.Each)
    }
}

class ControlPanelSpinnerHardware : SubsystemHardware<ControlPanelSpinnerHardware, ControlPanelSpinnerComponent>() {
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 30.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Control Panel"

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,
            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )
    private val invert by pref(false)
    private val escId = 20
    val spinnerEsc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }

    private val colorSensorV3 by hardw { ColorSensorV3(Port.kOnboard) }
    private val colorMatcher = ColorMatch().apply {
        addColorMatch(Blue.color)
        addColorMatch(Green.color)
        addColorMatch(Red.color)
        addColorMatch(Yellow.color)
    }

    private val gameData = sensor { DriverStation.getInstance().gameSpecificMessage stampWith it }
    val targetColorOrdinal = sensor { convertGameMessage() stampWith it }
    val currentColorOrdinal = sensor { currentColorOrdinal(colorMatcher.matchClosestColor(colorSensorV3.color).color) stampWith it }
    val controlPanelAngle = sensor { getControlPanelAngle(colorMatcher.matchClosestColor(colorSensorV3.color).color) stampWith it }
    var lastColorOrdinal: Int? = null
    private var controlPanelSpinnerAngle: Angle = 0.Degree
    private var lastDirectionSignum = 0
    private var gameDataOrdinal: Int? = null
    private fun convertGameMessage(): Int? {
        gameDataOrdinal = when (gameData.toString()) {
            "B" -> (Blue.ordinal + 2) % 4
            "G" -> (Green.ordinal + 2) % 4
            "R" -> (Red.ordinal + 2) % 4
            "Y" -> (Yellow.ordinal + 2) % 4
            else -> null
        }
        return gameDataOrdinal
    }

    private fun currentColorOrdinal(color: Color): Int? {
        return when (color) {
            Blue.color -> Blue.ordinal
            Green.color -> Green.ordinal
            Red.color -> Red.ordinal
            Yellow.color -> Yellow.ordinal
            else -> null
        }
    }

    private fun getControlPanelAngle(color: Color): Angle {
        val currentColor = currentColorOrdinal(color)
        if (currentColor == null || lastColorOrdinal == null) {
            controlPanelSpinnerAngle = 0.Degree
        } else {
            lastColorOrdinal?.also {
                controlPanelSpinnerAngle += (((it - currentColor) % 2) * 45).Degree
//                lastDirectionSignum = signum((it - currentColor) % 2)
            }
        }
        lastColorOrdinal = currentColor
        return controlPanelSpinnerAngle
    }
}