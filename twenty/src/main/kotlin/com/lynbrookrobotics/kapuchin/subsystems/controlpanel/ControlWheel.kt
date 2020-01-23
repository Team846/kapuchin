@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.I2C
import info.kunalsheth.units.generated.*

class ControlWheelComponent(hardware: ControlWheelHardware) : Component<ControlWheelComponent, ControlWheelHardware, DutyCycle>(hardware) {
    override val fallbackController: ControlWheelComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun ControlWheelHardware.output(value: DutyCycle) {
        spinnerEsc.set(value.Each)
    }
}

val readColorSensor: RevColorSensor.(Time) -> TimeStamped<String> = {
    getCurrentValue() stampWith currentTime
}

class ControlWheelHardware : SubsystemHardware<ControlWheelHardware, ControlWheelComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Priority.Low
    override val name: String = "ControlPanel"

    private val controlWheelEscId by pref(10)

    private val leftColorSensorAddress by pref(6)
    private val rightColorSensorAddress by pref(7)

    val leftColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, leftColorSensorAddress), readColorSensor)

    val rightColorSensor = sensor(RevColorSensor(I2C.Port.kOnboard, rightColorSensorAddress), readColorSensor)

    val spinnerEsc by hardw { CANSparkMax(controlWheelEscId, kBrushless) }
}
