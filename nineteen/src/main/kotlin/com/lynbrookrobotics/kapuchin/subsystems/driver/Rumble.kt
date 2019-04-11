package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

typealias Rumble = Pair<DutyCycle, DutyCycle>

class RumbleComponent(hardware: RumbleHardware) : Component<RumbleComponent, RumbleHardware, Rumble>(hardware, EventLoop) {

    override val fallbackController: RumbleComponent.(Time) -> Rumble = { 0.Percent to 0.Percent }

    override fun RumbleHardware.output(value: Rumble) {
        //TODO driver feedback with(driverHardware) {}

        with(operatorHardware.xbox) {
            setRumble(kLeftRumble, value.second.Each)
            setRumble(kRightRumble, value.second.Each)
        }
    }
}

class RumbleHardware(
        val driverHardware: DriverHardware,
        val operatorHardware: OperatorHardware
) : SubsystemHardware<RumbleHardware, RumbleComponent>() {
    override val name = "Feedback System"
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
}