package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.EjectorState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class EjectorState(val output: Boolean) {
    In(false), Out(true)
}

class HatchPanelEjectorComponent(hardware: HatchPanelEjectorHardware) : Component<HatchPanelEjectorComponent, HatchPanelEjectorHardware, EjectorState>(hardware, Subsystems.pneumaticTicker) {
    override val fallbackController: HatchPanelEjectorComponent.(Time) -> EjectorState = { In }
    override fun HatchPanelEjectorHardware.output(value: EjectorState) {
        solenoid.set(value.output)
    }
}

class HatchPanelEjectorHardware : SubsystemHardware<HatchPanelEjectorHardware, HatchPanelEjectorComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Hatch Panel Ejector"

    val solenoidPort by pref(4)
    val solenoid = Solenoid(solenoidPort)
}

