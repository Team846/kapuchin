package com.lynbrookrobotics.twenty.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.wpilibj.GenericHID.Hand
import edu.wpi.first.wpilibj.XboxController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class OperatorHardware : RobotHardware<OperatorHardware>() {
    override val priority = Priority.High
    override val name = "Operator"

    private val turretMapping by pref {
        val exponent by pref(2)
        val deadband by pref(10, Percent)
        val sensitivity by pref(20, Percent)

        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x) * sensitivity
        })
    }

    private val turretPrecisionMapping by pref {
        val exponent by pref(1)
        val deadband by pref(10, Percent)
        val sensitivity by pref(5, Percent)

        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x) * sensitivity
        })
    }

    private val triggerPressure by pref(30, Percent)

    val xbox by hardw { XboxController(1) }.verify("the operator controller is connected") {
        it.name == "Controller (Xbox One For Windows)"
    }.verify("xbox controller and rumblr are not swapped") {
        it.getTriggerAxis(Hand.kLeft) < 0.1 && it.getTriggerAxis(Hand.kRight) < 0.1
    }

    private fun <Input> s(f: XboxController.() -> Input) = sensor { f(xbox) stampWith it }

    val lt get() = xbox.getTriggerAxis(Hand.kLeft) > triggerPressure.Each
    val rt get() = xbox.getTriggerAxis(Hand.kRight) > triggerPressure.Each
    val lb get() = xbox.getBumper(Hand.kLeft)
    val rb get() = xbox.getBumper(Hand.kRight)

    val shift = s { lb }

    val toggleClimberArms = s { rb }
    val chaChaRealSmooth = s { xbox.backButton && !xbox.startButton }
    val takeItBackNowYall = s { xbox.backButton && xbox.startButton }
//    val setLimit = s { lt }

    val aim = s { lt }
    val shoot = s { rt }

    val shooterPresetAnitez = s { xbox.pov == 270 }
    val shooterPresetLow = s { xbox.pov == 180 }
    val shooterPresetMed = s { xbox.pov == 90 }
    val shooterPresetHigh = s { xbox.pov == 0 }

    val carouselBall0 = s { xbox.yButton }
    val centerTurret = s { xbox.xButton }
    val reindexCarousel = s { xbox.aButton }

    val turretManual = s {
        turretMapping(getX(Hand.kLeft).Each)
    }.with(graph("Turret Manual", Percent))

    val turretPrecisionManual = s {
        turretPrecisionMapping(getX(Hand.kRight).Each)
    }.with(graph("Turret Precsion Manual", Percent))

    init {
        EventLoop.runOnTick { time ->
            setOf(turretManual, turretPrecisionManual).forEach {
                it.optimizedRead(time, 0.1.Second)
            }
        }
    }
}

