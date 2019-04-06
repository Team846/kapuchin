package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class RumbleComponent(hardware: RumbleHardware) : Component<RumbleComponent, RumbleHardware, DutyCycle>(hardware, EventLoop) {

    override val fallbackController: RumbleComponent.(Time) -> DutyCycle = { 0.Percent }

    override fun RumbleHardware.output(value: DutyCycle) {
        with(operatorHardware.xbox) {
            setRumble(kLeftRumble, value.Each)
            setRumble(kRightRumble, value.Each)
        }
    }
}

class RumbleHardware(
        val operatorHardware: OperatorHardware
) : SubsystemHardware<RumbleHardware, RumbleComponent>() {
    override val name = "Feedback System"
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
}