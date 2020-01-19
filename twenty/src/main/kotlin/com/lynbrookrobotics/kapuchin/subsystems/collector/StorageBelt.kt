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

class StorageBeltComponent(hardware: StorageBeltHardware) : Component<StorageBeltComponent, StorageBeltHardware, TwoSided<DutyCycle>>(hardware) {


    override val fallbackController: StorageBeltComponent.(Time) -> TwoSided<DutyCycle>
        get() = { TwoSided(0.Percent, 0.Percent) }

    override fun StorageBeltHardware.output(value: TwoSided<DutyCycle>) {
        topEsc.set(value.left.Each)
        bottomEsc.set(value.right.Each)
    }
}

class StorageBeltHardware : SubsystemHardware<StorageBeltHardware, StorageBeltComponent>() {
    override val priority: Priority = Priority.Medium
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "StorageBelt"

    val topEscId by pref(10)
    val bottomEscId by pref(11)
    val topEscInversion by pref(false)
    val bottomEscInversion by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,
            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )
    val topCanId = 51
    val topEsc by hardw { CANSparkMax(topEscId, kBrushless) }.configure {

    }

    val bottomCanId = 50
    val bottomEsc by hardw { CANSparkMax(bottomEscId, kBrushless) }.configure {

    }
}