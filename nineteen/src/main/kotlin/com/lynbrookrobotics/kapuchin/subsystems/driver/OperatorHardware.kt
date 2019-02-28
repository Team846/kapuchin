package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
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

    //TODO collector slider right joystick
    //TODO lift left joystick
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

    val lowPanelHeight = s { aButton && !lt}
    val lowCargoHeight = s { aButton && lt }

    val midPanelHeight = s { bButton && !lt}
    val midCargoHeight = s { bButton && lt }

    val highPanelHeight = s { yButton && !lt}
    val highCargoHeight = s { yButton && lt }

    val deployPanel = s { rb && !lt }
    val deployCargo = s { rb && lt }

    val collectPanel = s { lb && !lt}
    val collectCargo = s { lb && lt }

    val visionAlign = s { rt && !lt && !start}
    val centerSlider = s { xButton && lt}
    
    val centerCargo = s { xButton && !lt}

    val unleashTheCobra = s { start && lt && rt }
}