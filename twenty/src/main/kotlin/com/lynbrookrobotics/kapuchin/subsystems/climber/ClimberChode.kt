package com.lynbrookrobotics.kapuchin.subsystems.climber

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.pneumaticTicker
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.ClimberChodeState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

enum class ClimberChodeState(val output: Boolean) { On(true), Off(false) }

class ClimberChodeComponent(hardware: ClimberChodeHardware) : Component<ClimberChodeComponent, ClimberChodeHardware, ClimberChodeState>(hardware, pneumaticTicker) {

    override val fallbackController: ClimberChodeComponent.(Time) -> ClimberChodeState = { On }

    override fun ClimberChodeHardware.output(value: ClimberChodeState) {
        solenoid.set(value.output)
    }
}

class ClimberChodeHardware : SubsystemHardware<ClimberChodeHardware, ClimberChodeComponent>() {
    override val period: Time = 50.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val priority: Priority = Priority.Low
    override val name: String = "Climber Chode (brake)"

    private val solenoidId = 0

    val solenoid by hardw { Solenoid(solenoidId) }
}