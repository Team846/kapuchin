package com.lynbrookrobotics.twenty.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class RumbleComponent(hardware: RumbleHardware) :
    Component<RumbleComponent, RumbleHardware, DutyCycle>(hardware, EventLoop) {

    override val fallbackController: RumbleComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun RumbleHardware.output(value: DutyCycle) {
        operator.xbox.apply {
            setRumble(RumbleType.kLeftRumble, value.Each)
            setRumble(RumbleType.kRightRumble, value.Each)
        }
    }
}

class RumbleHardware(
    val operator: OperatorHardware,
) : SubsystemHardware<RumbleHardware, RumbleComponent>() {
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Rumble"
}