package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.AlpsRdc80
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware.JoystickButton.*
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.*
import kotlin.math.sign

class DriverHardware : SubsystemHardware<DriverHardware, Nothing>() {
    override val subsystemName = "Driver"
    override val priority = Priority.RealTime
    override val period = 20.milli(Second)
    override val syncThreshold = 3.milli(Second)

    val operator by hardw { Joystick(1) }.verify("the operator joystick is connected") {
        it.name == "T.16000M"
    }
    val driver by hardw { Joystick(0) }.verify("the driver joystick is connected") {
        it.name == "T.16000M"
    }
    val wheel by hardw { Joystick(2) }.verify("the driver wheel is connected") {
        it.name == "FGT Rumble 3-in-1"
    }
    val absoluteWheel by hardw { Joystick(3) }.verify("the driver absolute wheel is connected"){
        it.name == "BU0836A Interface"
    }

    enum class JoystickButton(val raw: Int) {
        Trigger(1), BottomTrigger(2), LeftTrigger(3), RightTrigger(4),
        LeftOne(5), LeftTwo(6), LeftThree(7), LeftFour(10), LeftFive(9), LeftSix(8),
        RightOne(11), RightTwo(12), RightThree(13), RightFour(16), RightFive(15), RightSix(14),
    }

    operator fun Joystick.get(button: JoystickButton) = getRawButton(button.raw)
    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it }

    val activationTolerance by pref(0.03)
    val inactiveRange = 0 withToleranceOf activationTolerance
    val manualOverride = s { -(operator.y.takeUnless { it in inactiveRange } ?: 0.0) }

    // CLIMBER
    val deployHooks = s { operator[RightOne] && operator[RightSix] }
    val deployForks = s { operator[RightThree] }
    val climb = s { driver[LeftOne] }
    val manualClimb = s { operator[RightFour] }

    // DRIVETRAIN
    fun sqrWithSign(x: Double) = x * x * x.sign

    val accelerator = s { sqrWithSign(-(driver.y.takeUnless { it in inactiveRange } ?: 0.0)) }
    val steering = s { sqrWithSign(wheel.x.takeUnless { it in inactiveRange } ?: 0.0) }

    val absoluteSteeringPhaseCalibration by pref(180 + 12.9, Degree)
    val absSteerConversion = AlpsRdc80(absoluteSteeringPhaseCalibration)
    val absoluteSteering = s { absSteerConversion(absoluteWheel.x, absoluteWheel.y) }

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