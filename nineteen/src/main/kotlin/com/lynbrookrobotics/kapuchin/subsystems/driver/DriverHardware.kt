package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.logging.*
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
    private val lt get() = stick[LeftTrigger]
    private val rt get() = stick[RightTrigger]

    val collectCargo = s { stick[BottomTrigger] && stick[Trigger] }
    val interruptAuto = s { false }
    val lineTracking = s { false }
    val liftDown = s { stick[Trigger] }

    val goToLeftLoadingStation = s { stick[LeftFour] && lt }
    val goToRightLoadingStation = s { stick[LeftFour] && rt }


    val goToLeftCloseCargo = s { stick[LeftFive] && lt }
    val goToRightCloseCargo = s { stick[LeftFive] && rt }

    val goToLeftMiddleCargo = s { stick[LeftSix] && lt }
    val goToRightMiddleCargo = s { stick[LeftSix] && rt }

    val goToLeftFarCargo = s { stick[LeftThree] && lt }
    val goToRightFarCargo = s { stick[LeftThree] && rt }


    val goToLeftCloseRocket = s { stick[LeftOne] && lt }
    val goToRightCloseRocket = s { stick[LeftOne] && rt }

    val goToLeftFarRocket = s { stick[LeftTwo] && lt }
    val goToRightFarRocket = s { stick[LeftTwo] && rt }
}