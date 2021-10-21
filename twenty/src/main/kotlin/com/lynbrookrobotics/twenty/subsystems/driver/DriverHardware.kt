package com.lynbrookrobotics.twenty.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

@Suppress("unused")
enum class ThrustmasterButtons(val raw: Int) {
    Trigger(1), LeftTrigger(3), BottomTrigger(2), RightTrigger(4),
    LeftOne(5), LeftTwo(6), LeftThree(7), LeftFour(10), LeftFive(9), LeftSix(8),
    RightThree(13), RightTwo(12), RightOne(11), RightSix(14), RightFive(15), RightFour(16)
}

operator fun Joystick.get(button: ThrustmasterButtons) = getRawButton(button.raw)

class DriverHardware : RobotHardware<DriverHardware>() {
    override val priority = Priority.RealTime
    override val name = "Driver"

    val joystickMapping by pref {
        val exponent by pref(2)
        val deadband by pref(5, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }

    val wheelMapping by pref {
        val exponent by pref(2)
        val deadband by pref(5, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }

    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it }

    private val stick by hardw { Joystick(0) }.verify("the driver joystick is connected") {
        it.name == "T.16000M"
    }

    private val wheel by hardw { Joystick(2) }.verify("the driver wheel is connected") {
        it.name == "FGT Rumble 3-in-1"
    }

    val accelerator = s { joystickMapping(-stick.y.Each) }
        .with(graph("Accelerator", Percent))

    val steering = s { wheelMapping(wheel.x.Each) }
        .with(graph("Steering", Percent))

    val stopDrivetrain = s { stick[ThrustmasterButtons.LeftTrigger] }

    val eatBalls = s { stick[ThrustmasterButtons.Trigger] && !stick[ThrustmasterButtons.BottomTrigger] }
    val pukeBallsIntakeIn = s { stick[ThrustmasterButtons.BottomTrigger] && !stick[ThrustmasterButtons.Trigger] }
    val pukeBallsIntakeOut = s { stick[ThrustmasterButtons.BottomTrigger] && stick[ThrustmasterButtons.Trigger] }

    val carouselBall0 = s { stick[ThrustmasterButtons.RightTrigger] }
    val carouselLeft = s { stick.pov in (270 - 45)..(270 + 45) }
    val carouselRight = s { stick.pov in (90 - 45)..(90 + 45) }

    init {
        Subsystems.uiTicker.runOnTick { time ->
            setOf(accelerator, steering).forEach {
                it.optimizedRead(time, Subsystems.uiTicker.period)
            }
        }
    }
}