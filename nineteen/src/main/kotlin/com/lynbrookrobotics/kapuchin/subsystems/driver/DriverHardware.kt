package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class DriverHardware : RobotHardware<DriverHardware>() {
    override val name = "Driver"
    override val priority = Priority.RealTime

    val station by hardw { DriverStation.getInstance() }

    val stick by hardw { Joystick(0) }.verify("the driver joystick is connected") {
        it.name == "T.16000M"
    }
    val wheel by hardw { Joystick(2) }.verify("the driver wheel is connected") {
        it.name == "FGT Rumble 3-in-1"
    }
    val absoluteWheel by hardw { Joystick(3) }.verify("the absolute wheel is connected") {
        it.name == "Kunals Absolute Steering Wheel"
    }

    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it }

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

    val accelerator = s { -joystickMapping(stick.y.Each) }
            .with(graph("Accelerator", Percent))
    val steering = s { wheelMapping(wheel.x.Each) }
            .with(graph("Steering", Percent))

    val absSteering = s {
        (-(absoluteWheel.x / 2 + 1) * 1023 / 1000).Turn
    }
            .with(graph("Absolute Steering", Degree))

    // buttons
    val collectCargo = s { stick[RightTrigger] }
    val collectWallPanel = s { stick[BottomTrigger] }
//    val collectGroundPanel = s { stick[LeftTrigger] }
    val visionAlign = s { stick[Trigger] }
}