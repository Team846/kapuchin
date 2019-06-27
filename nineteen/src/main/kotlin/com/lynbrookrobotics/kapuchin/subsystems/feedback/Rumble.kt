package com.lynbrookrobotics.kapuchin.subsystems.feedback

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.control.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class Rumble(hardware: RumbleHardware) : Component<Rumble, RumbleHardware, TwoSided<DutyCycle>>(hardware, EventLoop) {

    override val fallbackController: Rumble.(Time) -> TwoSided<DutyCycle> = { TwoSided(0.Percent) }

    override fun RumbleHardware.output(value: TwoSided<DutyCycle>) {
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
        val driver: Driver,
        val operator: Operator
) : SubsystemHardware<RumbleHardware, Rumble>() {
    override val priority = Priority.High
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val name = "Rumble"
}