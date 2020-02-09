package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberPivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ClimberPivotState(val output: Boolean) { Stowed(false), Deployed(true) }

class ClimberPivotComponent(hardware: ClimberPivotHardware) : Component<ClimberPivotComponent, ClimberPivotHardware, ClimberPivotState>(hardware, pneumaticTicker) {

    override val fallbackController: ClimberPivotComponent.(Time) -> ClimberPivotState = { Stowed }

    override fun ClimberPivotHardware.output(value: ClimberPivotState) {
        solenoid.set(value.output)
    }
}

class ClimberPivotHardware : SubsystemHardware<ClimberPivotHardware, ClimberPivotComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Climber Pivot"

    private val solenoidId = 1

    val solenoid by hardw { Solenoid(solenoidId) }
}