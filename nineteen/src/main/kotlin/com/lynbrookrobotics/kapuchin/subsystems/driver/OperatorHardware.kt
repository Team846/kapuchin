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
    private val rt get() = xbox.getTriggerAxis(kRight) > triggerPressure.Each
    private val lb get() = xbox.getBumper(kLeft)
    private val rb get() = xbox.getBumper(kRight)

    private val start get() = xbox.startButton
    private val back get() = xbox.backButton

    val liftSensitivity by pref(75, Percent)
    val sliderSensitivity by pref(100, Percent)

    val lowPanelHeight = s { aButton && !lt }
    val lowCargoHeight = s { aButton && lt }

    val midPanelHeight = s { bButton && !lt }
    val midCargoHeight = s { bButton && lt }

    val highPanelHeight = s { yButton && !lt }
    val highCargoHeight = s { yButton && lt }

    val deployCargo = s { rb && lt }
    val softDeployCargo = s { rb && !lt }

    val deployPanel = s { lb && lt }
    val collectPanel = s { lb && !lt }

    val cargoShipCargoHeight = s { xButton && !lt }
    val pivotDown = s { xButton && lt }
    val centerAll = s { rt && !lt }
    val lineTracking = s { rt && lt }

    val liftPrecision = s {
        when (pov) {
            0 -> liftSensitivity
            180 -> -liftSensitivity
            else -> 0.Percent
        }
    }
    val sliderPrecision = s {
        when (pov) {
            90 -> sliderSensitivity
            270 -> -sliderSensitivity
            else -> 0.Percent
        }
    }

    val unleashTheCobra = s { lt && start }
    val oShitSnekGoBack = s { lt && back }
}
