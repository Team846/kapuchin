package com.lynbrookrobotics.twenty.subsystems.climber

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ClimberPivotState(val output: Boolean) { Down(true), Up(false) }

class ClimberPivotComponent(hardware: ClimberPivotHardware) :
    Component<ClimberPivotComponent, ClimberPivotHardware, ClimberPivotState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: ClimberPivotComponent.(Time) -> ClimberPivotState = { ClimberPivotState.Up }

    override fun ClimberPivotHardware.output(value: ClimberPivotState) {
        pivotSolenoid.set(value.output)
    }
}

class ClimberPivotHardware : SubsystemHardware<ClimberPivotHardware, ClimberPivotComponent>() {
    override val period = Subsystems.pneumaticTicker.period
    override val syncThreshold = 100.milli(Second)
    override val priority = Priority.Low
    override val name = "Climber Pivot"

    private val pivotSolenoidChannel = 1

    val pivotSolenoid by hardw { Solenoid(pivotSolenoidChannel) }
}