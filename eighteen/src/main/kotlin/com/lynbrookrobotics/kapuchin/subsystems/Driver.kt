package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.`±`
import com.lynbrookrobotics.kapuchin.control.data.stampWith
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
import kotlin.math.sign

class DriverHardware : SubsystemHardware<DriverHardware, Nothing>() {
    override val name = "Driver"
    override val priority = Priority.RealTime
    override val period = 20.milli(Second)
    override val syncThreshold = 3.milli(Second)

    val operator by hardw { Joystick(1) }.verify("the operator joystick is connected") {
        log(Debug) { it.name } // TESTING
        true
    }
    val driver by hardw { Joystick(0) }.verify("the driver joystick is connected") {
        log(Debug) { it.name } // TESTING
        true
    }
    val wheel by hardw { Joystick(2) }.verify("the driver wheel is connected") {
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

    val activationTolerance by pref(0.01)
    val inactiveRange = 0 `±` activationTolerance
    val manualOverride = s { -(operator.y.takeUnless { it in inactiveRange } ?: 0.0) }

    // CLIMBER
    val deployHooks = s { operator[RightOne] && operator[RightSix] }
    val deployForks = s { operator[RightThree] }
    val climb = s { driver[LeftOne] }
    val manualClimb = s { operator[RightFour] }

    // DRIVETRAIN
    fun sqrWithSign(x: Double) = x * x * x.sign

    val accelerator = s { sqrWithSign(-(driver.y.takeUnless { it in inactiveRange } ?: 0.0)) }
    val steering = s { wheel.x.takeUnless { it in inactiveRange } ?: 0.0 }

    // LIFT
    val twistAdjust = s { operator.z }
    val collect = s { driver[Trigger] }
    val exchange = s { operator[BottomTrigger] }
    val switch = s { operator[LeftTrigger] }
    val lowScale = s { operator[LeftFour] }
    val highScale = s { operator[RightTrigger] }
    val maxHeight = s { operator[LeftOne] }
    val manualLift = s { operator[LeftFive] }

    // COLLECTOR
    val purge = s { driver[LeftTrigger] || operator[Trigger] }
    val pivotDown = s { driver[Trigger] || driver[BottomTrigger] || operator[LeftTwo] }
    val openClamp = s { driver[BottomTrigger] || operator[LeftThree] }
}