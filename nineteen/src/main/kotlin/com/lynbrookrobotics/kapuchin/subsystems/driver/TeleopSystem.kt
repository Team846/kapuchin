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
        TwoSided(0.Percent) to Color(Color.HSBtoRGB(((currentTime.Second / 3 % 1.0)).toFloat(), 1f, 1f))
    }

    override fun TeleopHardware.output(value: Pair<Rumble, Color>) {
        ledHardware?.channels?.invoke(value.second)

        with(operatorHardware.xbox) {
            value.first.let { (l, r) ->
                setRumble(RumbleType.kLeftRumble, l.abs.Each)
                setRumble(RumbleType.kRightRumble, r.abs.Each)
            }
        }
    }
}

class TeleopHardware(
        val driverHardware: DriverHardware,
        val operatorHardware: OperatorHardware,
        val ledHardware: LedHardware?
) : SubsystemHardware<TeleopHardware, TeleopComponent>() {
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
    override val name = "Teleop Interface"
}