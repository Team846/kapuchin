package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.GenericHID.Hand.kLeft
import edu.wpi.first.wpilibj.GenericHID.Hand.kRight
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.XboxController
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
    val rumble by hardw<XboxController?> { XboxController(4) }.verify("the rumblr is connected") {
        it!!.name == "Controller (XBOX 360 For Windows)"
    }.verify("xbox controller and rumblr are not swapped") {
        it!!.getTriggerAxis(kLeft) > 0.1 && it.getTriggerAxis(kRight) > 0.1
    }
            .otherwise(hardw { null })

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

    val absSteering = s { -180.Degree * absoluteWheel.x }
            .with(graph("Absolute Steering", Degree))

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(accelerator, steering, absSteering).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }

    // buttons
    val collectCargo = s { stick[Trigger] && stick[BottomTrigger] }
    val liftDown = s { stick[Trigger] && !stick[BottomTrigger] }
    val interruptAuto = s { stick[LeftTrigger] }
    val autoAlign = s { stick[RightTrigger] }
}