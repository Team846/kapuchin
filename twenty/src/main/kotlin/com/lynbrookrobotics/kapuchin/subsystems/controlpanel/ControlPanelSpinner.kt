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
import edu.wpi.first.wpilibj.I2C
import info.kunalsheth.units.generated.*

class ControlPanelSpinnerComponent(hardware: ControlPanelSpinnerHardware) : Component<ControlPanelSpinnerComponent, ControlPanelSpinnerHardware, OffloadedOutput>(hardware) {
    override val fallbackController: ControlPanelSpinnerComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent) }

    val spinSpeed by pref(6, Volt)

    override fun ControlPanelSpinnerHardware.output(value: OffloadedOutput) {
        value.writeTo(spinnerEsc)
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

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val controlWheelEscId by pref(10)
    val spinnerEsc by hardw { CANSparkMax(controlWheelEscId, kBrushless) }

    private val leftColorSensorAddress by pref(6)
    val leftColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, leftColorSensorAddress), readColorSensor)

    private val rightColorSensorAddress by pref(7)
    val rightColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, rightColorSensorAddress), readColorSensor)


}