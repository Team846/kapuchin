package com.lynbrookrobotics.kapuchin.subsystems.collector

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class StorageBeltComponent(hardware: StorageBeltHardware) : Component<StorageBeltComponent, StorageBeltHardware, DutyCycle>(hardware) {
    override val fallbackController: StorageBeltComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun StorageBeltHardware.output(value: DutyCycle) {
        topEsc.set(value.Each)
    }
}

class StorageBeltHardware : SubsystemHardware<StorageBeltHardware, StorageBeltComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "StorageBelt"

    private val topEscId by pref(10)
    private val topEscInversion by pref(false)


    val topEsc by hardw { CANSparkMax(topEscId, kBrushless) }.configure {
        it.inverted = topEscInversion
    }


}