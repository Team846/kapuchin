package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kLeftRumble
import edu.wpi.first.wpilibj.GenericHID.RumbleType.kRightRumble
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

data class Feedback(
        val wheelRumble: DutyCycle = 0.Percent,
        val stickRumble: DutyCycle = 0.Percent,
        val xboxLeftRumble: DutyCycle = 0.Percent,
        val xboxRightRumble: DutyCycle = xboxLeftRumble,
        val ledColor: Color = Color(
                Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 1f)
        )
) {

    val fullRumble = Feedback(
            wheelRumble = 100.Percent,
            stickRumble = 100.Percent,
            xboxLeftRumble = 100.Percent, xboxRightRumble = 100.Percent,
            ledColor = this.ledColor
    )

    fun withRumble(rumble: DutyCycle) = Feedback(
            wheelRumble = rumble,
            stickRumble = rumble,
            xboxLeftRumble = rumble, xboxRightRumble = rumble,
            ledColor = this.ledColor
    )

    companion object {
        val RED = Feedback(ledColor = Color.RED)
        val GREEN = Feedback(ledColor = Color.GREEN)
        val BLUE = Feedback(ledColor = Color.BLUE)

        operator fun invoke(r: Double, g: Double, b: Double) = Feedback(
                ledColor = Color(r.toFloat(), g.toFloat(), b.toFloat())
        )
    }
}

class FeedbackSystemComponent(hardware: FeedbackSystemHardware) : Component<FeedbackSystemComponent, FeedbackSystemHardware, Feedback>(hardware, EventLoop) {

    override val fallbackController: FeedbackSystemComponent.(Time) -> Feedback = { Feedback() }

    override fun FeedbackSystemHardware.output(value: Feedback) = value.run {
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

class FeedbackSystemHardware(
        val driverHardware: DriverHardware,
        val operatorHardware: OperatorHardware,
        val ledHardware: LedHardware?
) : SubsystemHardware<FeedbackSystemHardware, FeedbackSystemComponent>() {
    override val name = "Feedback System"
    override val period = 20.milli(Second)
    override val syncThreshold = 10.milli(Second)
    override val priority = Priority.High
}