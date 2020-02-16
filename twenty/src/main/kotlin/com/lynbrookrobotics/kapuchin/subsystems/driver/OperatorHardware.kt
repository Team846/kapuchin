package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.GenericHID.Hand.kLeft
import edu.wpi.first.wpilibj.GenericHID.Hand.kRight
import edu.wpi.first.wpilibj.XboxController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class OperatorHardware : RobotHardware<OperatorHardware>() {
    override val priority = Priority.High
    override val name = "Operator"

    private val flywheelMapping by pref {
        val exponent by pref(1)
        val deadband by pref(10, Percent)
        val sensitivity by pref(100, Percent)

        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            (fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)) to sensitivity
        })
    }

    private val turretMapping by pref {
        val exponent by pref(1)
        val deadband by pref(10, Percent)
        val sensitivity by pref(100, Percent)

        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            (fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)) to sensitivity
        })
    }

    val xbox by hardw { XboxController(1) }.verify("the operator controller is connected") {
        it.name == "Controller (Xbox One For Windows)"
    }.verify("xbox controller and rumblr are not swapped") {
        it.getTriggerAxis(kLeft) < 0.1 && it.getTriggerAxis(kRight) < 0.1
    }

    private fun <Input> s(f: XboxController.() -> Input) = sensor { f(xbox) stampWith it }

    private val triggerPressure by pref(50, Percent)
    private val lt get() = xbox.getTriggerAxis(kLeft) > triggerPressure.Each
    private val rt get() = xbox.getTriggerAxis(kRight) > triggerPressure.Each
    private val lb get() = xbox.getBumper(kLeft)
    private val rb get() = xbox.getBumper(kRight)
    private val start get() = xbox.startButton
    private val back get() = xbox.backButton

    val aim = s { lt }
    val aimPreset = s { lb }
    val shoot = s { rt }
    val shootOverride = s { rb }

    val flywheelManual = s { flywheelMapping.first(getY(kLeft).Each) * flywheelMapping.second }
    val turretManual = s { turretMapping.first(getX(kRight).Each) * turretMapping.second }
    val shooterHoodManual = s { pov == 0 }

    val extendClimber = s { back }
    val retractClimber = s { start }

    val extendControlPanelPivot = s { pov == 180 }
    val controlPanelStage2 = s { pov == 180 && aButton }
    val controlPanelStage3 = s { pov == 180 && bButton }
}
