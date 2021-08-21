package com.lynbrookrobotics.twenty.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.GenericHID.RumbleType
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

typealias Rumble = TwoSided<DutyCycle>

class RumbleComponent(hardware: RumbleHardware) :
    Component<RumbleComponent, RumbleHardware, Rumble>(hardware, EventLoop) {

    override val fallbackController: RumbleComponent.(Time) -> Rumble = { Rumble(0.Percent) }

    override fun RumbleHardware.output(value: Rumble) {
        driver.rumble?.apply {
            setRumble(RumbleType.kLeftRumble, value.left.Each)
            setRumble(RumbleType.kRightRumble, value.left.Each)
        }

        operator.xbox.apply {
            setRumble(RumbleType.kLeftRumble, value.right.Each)
            setRumble(RumbleType.kRightRumble, value.right.Each)
        }
    }
}

class RumbleHardware(
    val driver: DriverHardware,
    val operator: OperatorHardware,
) : SubsystemHardware<RumbleHardware, RumbleComponent>() {
    override val period by Subsystems.sharedTickerTiming
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Rumble"
}