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


class StorageComponent(hardware: StorageHardware) : Component<StorageComponent, StorageHardware, DutyCycle>(hardware) {
    override val fallbackController: StorageComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun StorageHardware.output(value: DutyCycle) {
        storageEsc.set(value.Each)
    }
}


class StorageHardware : SubsystemHardware<StorageHardware, StorageComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "StorageBelt"

    private val storageEscId by pref(10)
    private val storageEscInversion by pref(false)


    val halleffect by hardw { DigitalInput(2) }

    val proximity by hardw {
        DigitalInput(1)
    }

    val storageEsc by hardw { CANSparkMax(storageEscId, kBrushless) }.configure {
        it.inverted = storageEscInversion
    }


}