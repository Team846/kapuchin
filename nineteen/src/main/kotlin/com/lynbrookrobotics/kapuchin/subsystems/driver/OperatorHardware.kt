package com.lynbrookrobotics.kapuchin.subsystems.driver

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
    private val rt get() = xbox.getTriggerAxis(kLeft) > triggerPressure.Each

    val groundHeight = s { aButton }
    val collectGroundPanel = s { aButton && lt }

    val lowPanelHeight = s { yButton }
    val lowCargoHeight = s { yButton && lt }

    val midPanelHeight = s { xButton }
    val midCargoHeight = s { xButton && lt }

    val highPanelHeight = s { bButton }
    val highCargoHeight = s { bButton && lt }

    private val povMush by pref(15, Degree)
    val liftPrecision = s {
        val pov = xbox.pov.Degree
        (when (pov) {
            in 0.Turn `±` povMush,
            in 1.Turn `±` povMush -> 1
            in 0.5.Turn `±` povMush -> -1
            else -> 0
        }).Each
    }
    val sliderPrecision = s {
        val pov = xbox.pov.Degree
        (when (pov) {
            in 0.25.Turn `±` povMush -> 1
            in 0.75.Turn `±` povMush -> -1
            else -> 0
        }).Each
    }

    val deployPanel = s { rt }
    val centerCargo = s { startButton }
    val deployCargo = s { lt && rt }

    val pushPanel = s { getBumper(kLeft) }
    val unleashTheCobra = s { lt && getBumper(kLeft) && getBumper(kRight) }
}