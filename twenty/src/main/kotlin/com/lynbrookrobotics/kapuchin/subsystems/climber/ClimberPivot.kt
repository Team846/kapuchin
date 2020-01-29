package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*

class ClimberPivotHardware : SubsystemHardware<ClimberPivotHardware, ClimberPivotComponent>() {
    override val period: Time = 50.Millisecond
    override val name: String = "Climber Pivot"
    override val syncThreshold: Time = 20.Millisecond
    override val priority: Priority = Low

    private val solenoidId = 12
    val solenoid by hardw { Solenoid(solenoidId) }
}

class ClimberPivotComponent(hardware: ClimberPivotHardware) : Component<ClimberPivotComponent, ClimberPivotHardware, ClimberPivotState>(hardware) {
    override val fallbackController: ClimberPivotComponent.(Time) -> ClimberPivotState
        get() = {Stowed}

    override fun ClimberPivotHardware.output(value: ClimberPivotState) {
        solenoid.set(value.output)
    }
}

enum class ClimberPivotState(val output: Boolean) {
    Stowed(false), Deployed(true)
}