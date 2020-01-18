package com.lynbrookrobotics.kapuchin.subsystems.controlpanel

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*

class ControlWheelComponent (hardware: ControlWheelHardware) : Component<ControlWheelComponent, ControlWheelHardware, DutyCycle>(hardware) {
    override val fallbackController: ControlWheelComponent.(Time) -> DutyCycle ={0.Percent}

    override fun ControlWheelHardware.output(value: DutyCycle) {
        controlwheelEsc.set(value.Each)




    }



}
class ControlWheelHardware : SubsystemHardware<ControlWheelHardware, ControlWheelComponent>() {
    override val period: Time = 30.Millisecond
    override val syncThreshold: Time = 30.Millisecond
    override val priority: Priority = Priority.Low
    override val name: String = "ControlPanel"

    val controlwheelEscId by pref(10)
    val controlwheelEscInversion by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,
            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    val controlwheelEsc by hardw { CANSparkMax(controlwheelEscId, kBrushless) }.configure {

    }
}