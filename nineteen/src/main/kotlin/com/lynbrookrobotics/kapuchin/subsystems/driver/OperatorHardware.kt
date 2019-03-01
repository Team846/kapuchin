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
    override val name = "Operator"
    override val priority = Priority.RealTime

    val xbox by hardw { XboxController(1) }.verify("the operator controller is connected") {
        //        it.name == "T.16000M" TODO: Get Controller Device Name
        true
    }

    private fun <Input> s(f: XboxController.() -> Input) = sensor { f(xbox) stampWith it }

    private val triggerPressure by pref(50, Percent)
    private val lt get() = xbox.getTriggerAxis(kLeft) > triggerPressure.Each
    private val rt get() = xbox.getTriggerAxis(kRight) > triggerPressure.Each
    private val lb get() = xbox.getBumper(kLeft)
    private val rb get() = xbox.getBumper(kRight)

    private val start get() = xbox.startButton

    private val lStickY get() = xbox.getY(kLeft)
    private val rStickX get() = xbox.getX(kRight)

    val joystickMapping by pref {
        val exponent by pref(2)
        val deadband by pref(10, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }

    val lowPanelHeight = s { aButton && !lt }
    val lowCargoHeight = s { aButton && lt }

    val midPanelHeight = s { bButton && !lt }
    val midCargoHeight = s { bButton && lt }

    val highPanelHeight = s { yButton && !lt }
    val highCargoHeight = s { yButton && lt }

    val deployPanel = s { rb && !lt }
    val deployCargo = s { rb && lt }

    val collectPanel = s { lb && !lt }
    val collectCargo = s { lb && lt }

    val lineTracking = s { rt && !lt && !start }
    val centerSlider = s { xButton && lt }

    val centerCargo = s { xButton && !lt }

    val liftPrecision = s { joystickMapping(lStickY.Each).Each }
    val sliderPrecision = s { joystickMapping(rStickX.Each).Each }

    val unleashTheCobra = s { start && lt && rt }
}
