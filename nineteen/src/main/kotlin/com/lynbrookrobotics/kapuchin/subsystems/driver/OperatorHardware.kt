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

    val highPanelHeight = s { aButton }
    val highCargoHeight = s { aButton && lt }

    val liftPrecision = s { 0.0.Each.takeIf { false } } // todo: Figure out d-pad
    val sliderPrecision = s { 0.0.Each.takeIf { false } } // todo: Figure out d-pad

    val deployPanel = s { rt }
    val deployCargo = s { lt && rt }

    val pushPanel = s { getBumper(kLeft) }
    val unleashTheCobra = s { lt && getBumper(kLeft) && getBumper(kRight) }
}