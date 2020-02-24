package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.GenericHID.Hand.kLeft
import edu.wpi.first.wpilibj.GenericHID.Hand.kRight
import edu.wpi.first.wpilibj.XboxController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.atan2

class OperatorHardware : RobotHardware<OperatorHardware>() {
    override val priority = Priority.High
    override val name = "Operator"

    private val flywheelMappingNamed = Named("Flywheel Mapping", this)
    private val deadband by flywheelMappingNamed.pref(80, Percent)
    private val min by flywheelMappingNamed.pref(20, Percent)
    private val max by flywheelMappingNamed.pref(100, Percent)
    private val minAngle by flywheelMappingNamed.pref(-90, Degree)
    private val maxAngle by flywheelMappingNamed.pref(90, Degree)
    private val minSetpointAngle by flywheelMappingNamed.pref(-135, Degree)
    private val maxSetpointAngle by flywheelMappingNamed.pref(135, Degree)

    private val db = horizontalDeadband(deadband, 100.Percent)
    private fun flywheelMapping(x: Dimensionless, y: Dimensionless): Angle? {
        if (db(x) == 0.Percent && db(y) == 0.Percent) return null
        return atan2(db(x).Each, db(y).Each).Radian
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

    private val triggerPressure by pref(30, Percent)
    private val lt get() = xbox.getTriggerAxis(kLeft) > triggerPressure.Each
    private val rt get() = xbox.getTriggerAxis(kRight) > triggerPressure.Each
    private val lb get() = xbox.getBumper(kLeft)
    private val rb get() = xbox.getBumper(kRight)
    private val start get() = xbox.startButton
    private val back get() = xbox.backButton

    val aim = s { /*lt*/false }
    val aimPreset = s {
        val angle = flywheelMapping(getX(kLeft).Each, getY(kLeft).Each)
        /*(angle?.let { it in minSetpointAngle..-180.Degree || it in 180.Degree..maxSetpointAngle } ?: false) || */yButton //TODO FOR TESTINGGLKWEJGLKEWFJLEW -ANDY
    }
    val shoot = s { rt }
    val hoodUp = s { lb }

    val flywheelManual = s {
        val angle = flywheelMapping(getX(kLeft).Each, getY(kLeft).Each)
        angle?.let { a ->
            val percent = ((a - minAngle) / (maxAngle - minAngle))
                    .takeIf { a in minAngle..0.Degree || a in 0.Degree..maxAngle }
                    ?: return@s 0.Percent

            (max - min) * percent - min
        } ?: 0.Percent
        0.Percent
    }
    val turretManual = s { turretMapping.first(getX(kRight).Each) * turretMapping.second }

    val rezeroTurret = s { pov == 90 }
    val reindexCarousel = s { pov == 270 }

    val extendClimber = s { back }
    val retractClimber = s { start }

    val extendControlPanel = s { pov == 0 }
    val controlPanelStage2 = s { pov == 0 && aButton }
    val controlPanelStage3 = s { pov == 0 && bButton }
}
