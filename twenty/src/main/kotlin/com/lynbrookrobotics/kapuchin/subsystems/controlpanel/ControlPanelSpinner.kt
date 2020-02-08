@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.Colors.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANEncoder
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import com.revrobotics.EncoderType
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C
import info.kunalsheth.units.generated.*

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
    override val priority: Priority = Priority.Low
    override val name: String = "ControlPanel"

    val ticksPerRevolution = 30
    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )


    private val controlWheelEscId by pref(10)
    val spinnerEsc by hardw { CANSparkMax(controlWheelEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
    }
    val spinnerEncoder: CANEncoder by hardw { spinnerEsc.getEncoder(EncoderType.kHallSensor, ticksPerRevolution) }
    val spinnerPidController: CANPIDController by hardw { spinnerEsc.pidController }

    private val leftColorSensorAddress by pref(6)
    val leftColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, leftColorSensorAddress), readColorSensor)

    private val rightColorSensorAddress by pref(7)
    val rightColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, rightColorSensorAddress), readColorSensor)

    val gameData = sensor { DriverStation.getInstance().gameSpecificMessage stampWith it }
//    val rotations = sensor { getControlPanelAngle() stampWith it }
//    val direction = sensor { getDirection() stampWith it }





}


enum class Colors(val r: Double, val g: Double, val b: Double) { Blue(0.3044677734375,0.54288330078125,0.15262451171875 ), Green(0.2068115234375,0.53885498046875,0.2543212890625), Red(0.39801025390625,0.4095703125,0.1924072265625), Yellow(0.3044677734375,0.54288330078125,0.15262451171875) }