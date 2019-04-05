package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.TeleopComponent.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

class TeleopComponent(hardware: TeleopHardware) : Component<TeleopComponent, TeleopHardware, TeleopFeedback>(hardware, EventLoop) {

    data class TeleopFeedback(
            val wheelRumble: DutyCycle = 0.Percent,
            val stickRumble: DutyCycle = 0.Percent,
            val xboxLeftRumble: DutyCycle = 0.Percent,
            val xboxRightRumble: DutyCycle = xboxLeftRumble,
            val ledColor: Color = Color(
                    Color.HSBtoRGB(((currentTime.Second / 3 % 1.0)).toFloat(), 1f, 1f)
            )
    )

    override val fallbackController: TeleopComponent.(Time) -> TeleopFeedback = { TeleopFeedback() }

    override fun TeleopHardware.output(value: TeleopFeedback) = value.run {
        ledHardware?.channels?.invoke(value.ledColor)

        with(operatorHardware.xbox) {
            setRumble(kLeftRumble, xboxLeftRumble.Each)
            setRumble(kRightRumble, xboxRightRumble.Each)
        }

        with(driverHardware.wheel) {
            setRumble(kLeftRumble, wheelRumble.Each)
            setRumble(kRightRumble, wheelRumble.Each)
        }

        with(driverHardware.stick) {
            setRumble(kLeftRumble, stickRumble.Each)
            setRumble(kRightRumble, stickRumble.Each)
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