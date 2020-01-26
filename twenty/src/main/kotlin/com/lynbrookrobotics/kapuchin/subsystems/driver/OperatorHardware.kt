package com.lynbrookrobotics.kapuchin.subsystems.driver

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj.GenericHID.Hand.kLeft
import edu.wpi.first.wpilibj.GenericHID.Hand.kRight
import info.kunalsheth.units.generated.*

class OperatorHardware : RobotHardware<OperatorHardware>() {
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

    val shoot = s { xButton && !lt}
    val turret = s { xButton && lt}
    val wacker = s { yButton && !lt}


}
