package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class IntakeRollersComponent(hardware: IntakeRollersHardware) : Component<IntakeRollersComponent, IntakeRollersHardware, OffloadedOutput>(hardware) {
    val collectSpeed by pref(50, Percent)

    override val fallbackController: IntakeRollersComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun IntakeRollersHardware.output(value: OffloadedOutput) {
        value.writeTo(rollersEsc)
    }
}

class IntakeRollersHardware : SubsystemHardware<IntakeRollersHardware, IntakeRollersComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Intake Rollers"

    private val invertRollers by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 25.Ampere,
            defaultPeakCurrentLimit = 35.Ampere
    )

    private val rollersEscId by pref(0)
    val rollersEsc by hardw { TalonSRX(rollersEscId) }.configure {
        it.inverted = invertRollers
    }
}
