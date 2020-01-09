package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

typealias Rumble = TwoSided<DutyCycle>

class RumbleComponent(hardware: RumbleHardware) : Component<RumbleComponent, RumbleHardware, Rumble>(hardware, EventLoop) {

    override val fallbackController: RumbleComponent.(Time) -> Rumble = { TwoSided(0.Percent) }

    override fun RumbleHardware.output(value: Rumble) {
        driver.rumble?.apply {
            setRumble(kLeftRumble, value.left.Each)
            setRumble(kRightRumble, value.left.Each)
        }

        operator.xbox.apply {
            setRumble(kLeftRumble, value.right.Each)
            setRumble(kRightRumble, value.right.Each)
        }
    }
}

class RumbleHardware(
        val driver: DriverHardware,
        val operator: OperatorHardware
) : SubsystemHardware<RumbleHardware, RumbleComponent>() {
    override val name = "Feedback System"
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
}