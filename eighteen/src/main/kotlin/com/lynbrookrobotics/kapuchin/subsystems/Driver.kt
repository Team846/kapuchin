package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware.JoystickButton.*
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.milli

class DriverHardware : SubsystemHardware<DriverHardware, Nothing>() {
    override val subsystemName = "Driver"
    override val priority = Priority.RealTime
    override val period = 20.milli(::Second)
    override val syncThreshold = 3.milli(::Second)

    val operatorStick by hardw { Joystick(1) }.verify("the operator joystick is connected") {
        log(Debug) { it.name } // TESTING
        true
    }
    val driverStick by hardw { Joystick(0) }.verify("the driver joystick is connected") {
        log(Debug) { it.name } // TESTING
        true
    }
    val driverWheel by hardw { Joystick(2) }.verify("the driver wheel is connected") {
        log(Debug) { it.name } // TESTING
        true
    }

    enum class JoystickButton(val raw: Int) {
        Trigger(1), BottomTrigger(2), LeftTrigger(3), RightTrigger(4),
        LeftOne(5), LeftTwo(6), LeftThree(7), LeftFour(10), LeftFive(9), LeftSix(8),
        RightOne(11), RightTwo(12), RightThree(13), RightFour(16), RightFive(15), RightSix(14),
    }

    operator fun Joystick.get(button: JoystickButton) = getRawButton(button.raw)
    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it }

    val manualOverride = s { -operatorStick.y }

    // CLIMBER HOOKS
    val deployHooks = s { operatorStick[RightOne] && operatorStick[RightSix] }

    // CLIMBER WINCH
    val climb = s { driverStick[LeftOne] }

    // CLIMBER FORKS

    // DRIVETRAIN
    val activationTolerance by pref(0.05)
    val inactiveRange = 0 withToleranceOf activationTolerance
    val accelerator = s { -(driverStick.y.takeUnless { it in inactiveRange } ?: 0.0) }
    val steering = s { driverWheel.x.takeUnless { it in inactiveRange } ?: 0.0 }

    // LIFT
    val twistAdjust = s { operatorStick.z }
    val collect = s { driverStick[Trigger] }
    val exchange = s { operatorStick[BottomTrigger] }
    val switch = s { operatorStick[LeftTrigger] }
    val lowScale = s { operatorStick[LeftFour] }
    val highScale = s { operatorStick[RightTrigger] }
    val maxHeight = s { operatorStick[LeftOne] }
    val manualLift = s { operatorStick[LeftFive] }

    // COLLECTOR ROLLERS
    val purge = s { driverStick[LeftTrigger] || operatorStick[Trigger] }
}