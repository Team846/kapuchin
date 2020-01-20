package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberStowState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class ClimberStowComponent(hardware: ClimberStowHardware) : Component<ClimberStowComponent, ClimberStowHardware, ClimberStowState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: ClimberStowComponent.(Time) -> ClimberStowState = { Down }

    override fun ClimberStowHardware.output(value: ClimberStowState) {
        solenoid.set(value.output)
    }
}

class ClimberStowHardware : SubsystemHardware<ClimberStowHardware, ClimberStowComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Climber Stow"

    private val solenoidPort = 0
    val solenoid by hardw { Solenoid(solenoidPort) }
}

enum class ClimberStowState(val output: Boolean) { Up(true), Down(false) }