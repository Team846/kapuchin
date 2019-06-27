package com.lynbrookrobotics.kapuchin.subsystems.control

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

class Operator : RobotHardware<Operator>() {
    override val name = "Operator"
    override val priority = Priority.RealTime

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

    val liftSensitivity by pref(75, Percent)
    val sliderSensitivity by pref(100, Percent)

    val cargoShipCargoHeight = s { xButton }
    val pivotDown = s { /*xButton && lt*/ false }

    val lowPanelHeight = s { aButton && !lt }
    val lowCargoHeight = s { aButton && lt }

    val midPanelHeight = s { bButton && !lt }
    val midCargoHeight = s { bButton && lt }

    val highPanelHeight = s { yButton && !lt }
    val highCargoHeight = s { yButton && lt }

    val lineTracking = s { rt }

    val centerSlider = s { pov == 0 }
    val centerCargoLeft = s { pov == 90 || pov == 45 }
    val centerCargoRight = s { pov == 270 || pov == 315 }
    val reZero = s { pov == 180 }

    val deployPanel = s { lb && !lt }
    val lilDicky = s { lb && lt }

    val softDeployCargo = s { rb && !lt }
    val deployCargo = s { rb && lt }

    val unleashTheCobra = s { lt && start }
    val oShitSnekGoBack = s { lt && back }

    val liftJoystickMapping by pref {
        val exponent by pref(2)
        val deadband by pref(10, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }
    val sliderJoystickMapping by pref {
        val exponent by pref(1)
        val deadband by pref(10, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }
    val liftPrecision = s {
        -liftJoystickMapping(getY(kLeft).Each) * liftSensitivity
    }
    val sliderPrecision = s {
        sliderJoystickMapping(getX(kRight).Each) * sliderSensitivity
    }
}
