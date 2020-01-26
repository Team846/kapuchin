package com.lynbrookrobotics.kapuchin.subsystems.collector


import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


class OmniWheelComponent(hardware: OmniWheelHardware) : Component<OmniWheelComponent, OmniWheelHardware, DutyCycle>(hardware) {
    override val fallbackController: OmniWheelComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun OmniWheelHardware.output(value: DutyCycle) {
        omniwheelEsc.set(value.Each)
    }
}


class OmniWheelHardware : SubsystemHardware<OmniWheelHardware, OmniWheelComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "StorageBelt"
    private val omniwheelEscId by pref(10)
    private val omniwheelEscInversion by pref(false)


    val halleffect by hardw { DigitalInput(2) }

    val proximity by hardw {
        DigitalInput(1)
    }

    val omniwheelEsc by hardw { CANSparkMax(omniwheelEscId, kBrushless) }.configure {
        it.inverted = omniwheelEscInversion
    }


}