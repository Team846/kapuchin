package com.lynbrookrobotics.kapuchin.subsystems.collector


import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.IntakePneumaticState.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.Solenoid
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class IntakePneumaticComponent(hardware: IntakePneumaticHardware) : Component<IntakePneumaticComponent, IntakePneumaticHardware, IntakePneumaticState>(hardware, Subsystems.pneumaticTicker) {

    override val fallbackController: IntakePneumaticComponent.(Time) -> IntakePneumaticState = { Down }

    override fun IntakePneumaticHardware.output(value: IntakePneumaticState) {
        solenoid.set(value.output)
        solenoid2.set(value.output)
    }
}

class IntakePneumaticHardware : SubsystemHardware<IntakePneumaticHardware, IntakePneumaticComponent>() {
    override val priority: Priority = Priority.Low
    override val period: Time = 100.milli(Second)
    override val syncThreshold: Time = 20.milli(Second)
    override val name: String = "Intake Lift"

    private val solenoidPort = 0
    private val solenoidPort2 = 1
    val solenoid2 by hardw { Solenoid(solenoidPort2) }
    val solenoid by hardw { Solenoid(solenoidPort) }

}

enum class IntakePneumaticState(val output: Boolean) { Up(true), Down(false) }