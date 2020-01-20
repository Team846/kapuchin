package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*

class ControlWheelComponent(hardware: ControlWheelHardware) : Component<ControlWheelComponent, ControlWheelHardware, DutyCycle>(hardware) {
    override val fallbackController: ControlWheelComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun ControlWheelHardware.output(value: DutyCycle) {
        spinnerEsc.set(value.Each)
    }
}

class ControlWheelHardware : SubsystemHardware<ControlWheelHardware, ControlWheelComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Priority.Low
    override val name: String = "ControlPanel"

    private val controlWheelEscId by pref(10)

    val spinnerEsc by hardw { CANSparkMax(controlWheelEscId, kBrushless) }
}
