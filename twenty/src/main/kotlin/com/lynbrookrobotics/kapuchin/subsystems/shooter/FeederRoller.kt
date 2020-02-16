package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FeederRollerComponent(hardware: FeederRollerHardware) : Component<FeederRollerComponent, FeederRollerHardware, OffloadedOutput>(hardware) {

    override val fallbackController: FeederRollerComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun FeederRollerHardware.output(value: OffloadedOutput) {
        value.writeTo(esc, pidController)
    }
}

class FeederRollerHardware : SubsystemHardware<FeederRollerHardware, FeederRollerComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Medium
    override val name: String = "Feeder Roller"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val escId = 61

    val esc by hardw { CANSparkMax(escId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invert
    }

    val pidController by hardw { esc.pidController!! }
}
