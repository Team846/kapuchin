@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C.Port.kOnboard
import info.kunalsheth.units.generated.*
import java.lang.Integer.signum

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) : Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, OffloadedOutput>(hardware) {
    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    val spinSpeed by pref(6, Volt)

    override fun ControlPanelSpinnerHardware.output(value: OffloadedOutput) {
        value.writeTo(spinnerEsc, spinnerPidController)
    }
}

val readColorSensor: RevColorSensor.(Time) -> TimeStamped<String> = {
    getCurrentValue() stampWith currentTime
}

class ControlPanelSpinnerHardware : SubsystemHardware<ControlPanelSpinnerHardware, ControlPanelSpinnerComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Low
    override val name: String = "ControlPanel"


    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )


    private val controlWheelEscId by pref(10)
    val spinnerEsc by hardw { CANSparkMax(controlWheelEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
    }

    val spinnerPidController: CANPIDController by hardw { spinnerEsc.pidController }

    private val colorSensorAddress by pref(6)
    private val revColorSensor by hardw { RevColorSensor(kOnboard, colorSensorAddress) }
    val currentColor = sensor { Colors.valueOf(revColorSensor.getCurrentValue()) stampWith it }

    val gameData = sensor { DriverStation.getInstance().gameSpecificMessage stampWith it }
    val controlPanelAngle = sensor { getControlPanelAngle(currentColor.optimizedRead(it, 0.Second).y) stampWith it }
    val direction = sensor { lastDirectionSignum stampWith it }

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


enum class Colors(val color: String) { Blue("blue"), Green("green"), Red("red"), Yellow("yellow") }

