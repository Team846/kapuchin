package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class IntakeRollersComponent(hardware: IntakeRollersHardware) : Component<IntakeRollersComponent, IntakeRollersHardware, OffloadedOutput>(hardware) {

    val intakeSpeed by pref(50, Percent)
    val unjamSpeed by pref(50, Percent)

    override val fallbackController: IntakeRollersComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun IntakeRollersHardware.output(value: OffloadedOutput) {
        value.writeTo(esc)
    }
}

class IntakeRollersHardware : SubsystemHardware<IntakeRollersHardware, IntakeRollersComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Medium
    override val name: String = "Intake Rollers"

    private val invert by pref(false)

    val escConfig by escConfigPref(
            defaultNominalOutput = 0.5.Volt,

            defaultContinuousCurrentLimit = 10.Ampere,
            defaultPeakCurrentLimit = 25.Ampere
    )

    private val escId = 40

    val esc by hardw { VictorSPX(escId) }.configure {
        generalSetup(it, escConfig)
        it.inverted = invert
    }
}