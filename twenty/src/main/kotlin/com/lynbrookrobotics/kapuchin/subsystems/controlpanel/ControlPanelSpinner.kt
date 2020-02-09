@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C.Port
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.lang.Integer.signum

enum class Colors(val color: String) { Blue("blue"), Green("green"), Red("red"), Yellow("yellow") }

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) : Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, OffloadedOutput>(hardware) {

    val spinSpeed by pref(6, Volt)

    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun ControlPanelSpinnerHardware.output(value: OffloadedOutput) {
        value.writeTo(spinnerEsc, spinnerPidController)
    }
}

class ControlPanelSpinnerHardware : SubsystemHardware<ControlPanelSpinnerHardware, ControlPanelSpinnerComponent>() {
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 30.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Control Panel"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val escId = 20
    private val colorSensorAddress = 6

    val spinnerEsc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invert
    }

    val spinnerPidController by hardw { spinnerEsc.pidController!! }

    private val revColorSensor by hardw { RevColorSensor(Port.kOnboard, colorSensorAddress) }

    val currentColor = sensor { Colors.valueOf(revColorSensor.getCurrentValue()) stampWith it }

    val gameData = sensor { DriverStation.getInstance().gameSpecificMessage stampWith it }
    val controlPanelAngle = sensor { getControlPanelAngle(currentColor.optimizedRead(it, 0.Second).y) stampWith it }

    var lastColorOrdinal: Int? = null
    private var controlPanelSpinnerAngle: Angle = 0.Degree

    private var lastDirectionSignum = 0
    private fun getControlPanelAngle(color: Colors): Angle {
        val currentColorOrdinal = Colors.valueOf(color.color).ordinal

        lastColorOrdinal?.also {
            controlPanelSpinnerAngle += (((it - currentColorOrdinal) % 2) * 45).Degree
            lastDirectionSignum = signum((it - currentColorOrdinal) % 2)
        }

        lastColorOrdinal = currentColorOrdinal

        return controlPanelSpinnerAngle
    }
}