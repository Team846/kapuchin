package com.lynbrookrobotics.twenty.subsystems.intake

import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class IntakeRollersComponent(hardware: IntakeRollersHardware) :
    Component<IntakeRollersComponent, IntakeRollersHardware, OffloadedOutput>(hardware, Subsystems.pneumaticTicker) {

    val eatSpeed by pref(100, Percent)
    val pauseSpeed by pref(-10, Percent)

    override val fallbackController: IntakeRollersComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun IntakeRollersHardware.output(value: OffloadedOutput) {
        value.writeTo(esc)
    }
}

class IntakeRollersHardware : SubsystemHardware<IntakeRollersHardware, IntakeRollersComponent>() {
    override val period = Subsystems.pneumaticTicker.period
    override val syncThreshold = 20.milli(Second)
    override val priority = Priority.Low
    override val name = "Intake Rollers"

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
