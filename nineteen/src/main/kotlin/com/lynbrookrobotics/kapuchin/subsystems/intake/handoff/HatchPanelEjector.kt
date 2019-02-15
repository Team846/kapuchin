package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff

import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.EjectorState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class EjectorState {
    In, Out
}

class HatchPanelEjectorComponent(hardware: HatchPanelEjectorHardware) : Component<HatchPanelEjectorComponent, HatchPanelEjectorHardware, EjectorState>(hardware) {
    override val fallbackController: HatchPanelEjectorComponent.(Time) -> EjectorState = { In }
    override fun HatchPanelEjectorHardware.output(value: EjectorState) {
        when(value) {
            In -> hardware.solenoid.set(false)
            Out -> hardware.solenoid.set(true)
        }
    }

}

class HatchPanelEjectorHardware : SubsystemHardware<HatchPanelEjectorHardware, HatchPanelEjectorComponent>() {
    override val priority: Priority  = Priority.Low
    override val period: Time = 30.milli(Second)
    override val syncThreshold: Time = 5.milli(Second)
    override val name: String = "Hatch Panel Ejector"

    val solenoidPort by pref(1)
    val solenoid = Solenoid(solenoidPort)

}

