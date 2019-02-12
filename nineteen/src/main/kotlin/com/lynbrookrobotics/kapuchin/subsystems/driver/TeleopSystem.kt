package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

typealias Rumble = TwoSided<DutyCycle>
class TeleopComponent(hardware: TeleopHardware) : Component<TeleopComponent, TeleopHardware, Pair<Rumble, Color>>(hardware, EventLoop) {
    override val fallbackController: TeleopComponent.(Time) -> Pair<Rumble, Color> = {
        TwoSided(0.Percent) to Color.ORANGE
    }

    override fun TeleopHardware.output(value: Pair<Rumble, Color>) {
        // TODO: Output to LEDs

        with(operatorHardware.xbox) {
            value.first.let { (l, r) ->
                setRumble(RumbleType.kLeftRumble, l.abs.Each)
                setRumble(RumbleType.kRightRumble, r.abs.Each)
            }
        }
    }
}

class TeleopHardware(
//      private val ledHardware: LedHardware,
        val driverHardware: DriverHardware,
        val operatorHardware: OperatorHardware
) : SubsystemHardware<TeleopHardware, TeleopComponent>() {
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Teleop Interface"
}