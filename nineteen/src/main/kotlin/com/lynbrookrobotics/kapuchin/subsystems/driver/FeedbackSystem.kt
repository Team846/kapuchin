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
        val driverRumble: DutyCycle = 0.Percent,
        val operatorRumble: DutyCycle = 0.Percent,
        val ledColor: Color = Color.BLACK
        //Color(Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 0.3f))
) {

    fun fullRumble() = Feedback(
            driverRumble = 100.Percent,
            operatorRumble = 100.Percent,
            ledColor = this.ledColor
    )

    companion object {
        val red = Feedback(ledColor = Color.RED)
        val green = Feedback(ledColor = Color.GREEN)
        val blue = Feedback(ledColor = Color.BLUE)
        val black = Feedback(ledColor = Color.BLACK)
        val white = Feedback(ledColor = Color.WHITE)

        operator fun invoke(r: Double, g: Double, b: Double) = Feedback(
                ledColor = Color(r.toFloat(), g.toFloat(), b.toFloat())
        )
    }
}

class FeedbackSystemComponent(hardware: FeedbackSystemHardware) : Component<FeedbackSystemComponent, FeedbackSystemHardware, Feedback>(hardware, EventLoop) {

    override val fallbackController: FeedbackSystemComponent.(Time) -> Feedback = { Feedback.black }

    override fun FeedbackSystemHardware.output(value: Feedback) = value.run {
        ledHardware?.channels?.invoke(value.ledColor)

        with(operatorHardware.xbox) {
            setRumble(kLeftRumble, operatorRumble.Each)
            setRumble(kRightRumble, operatorRumble.Each)
        }

        with(driverHardware.wheel) {
            setRumble(kLeftRumble, driverRumble.Each)
            setRumble(kRightRumble, driverRumble.Each)
        }

        with(driverHardware.stick) {
            setRumble(kLeftRumble, driverRumble.Each)
            setRumble(kRightRumble, driverRumble.Each)
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