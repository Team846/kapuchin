package com.lynbrookrobotics.kapuchin.subsystems.intake

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.IntakePivotState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class IntakePivotState(val output: Boolean) { Out(true), In(false) }

class IntakePivotComponent(hardware: IntakePivotHardware) : Component<IntakePivotComponent, IntakePivotHardware, IntakePivotState>(hardware, pneumaticTicker) {

    override val fallbackController: IntakePivotComponent.(Time) -> IntakePivotState = { In }

    override fun IntakePivotHardware.output(value: IntakePivotState) {
        solenoid.set(value.output)
    }
}

class IntakePivotHardware : SubsystemHardware<IntakePivotHardware, IntakePivotComponent>() {
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Intake Pivot"

    private val solenoidPort = 3

    val solenoid by hardw { Solenoid(solenoidPort) }
}