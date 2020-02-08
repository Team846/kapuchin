package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakePivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class IntakePivotComponent(hardware: IntakePivotHardware) : Component<IntakePivotComponent, IntakePivotHardware, IntakePivotState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: IntakePivotComponent.(Time) -> IntakePivotState = { Down }

    override fun IntakePivotHardware.output(value: IntakePivotState) {
        solenoid.set(value.output)
    }
}

class IntakePivotHardware : SubsystemHardware<IntakePivotHardware, IntakePivotComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Intake Pivot"

    private val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }

}

enum class IntakePivotState(val output: Boolean) { Up(true), Down(false) }