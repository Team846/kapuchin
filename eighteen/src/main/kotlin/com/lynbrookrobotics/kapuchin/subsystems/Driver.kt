package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.conversion.deadband.horizontalDeadband
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware.JoystickButton.*
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Dimensionless
import info.kunalsheth.units.generated.Each
import info.kunalsheth.units.generated.Percent
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.math.milli
import info.kunalsheth.units.math.pow
import info.kunalsheth.units.math.withSign

class DriverHardware : SubsystemHardware<DriverHardware, Nothing>() {
    override val name = "Driver"
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

    enum class JoystickButton(val raw: Int) {
        Trigger(1), BottomTrigger(2), LeftTrigger(3), RightTrigger(4),
        LeftOne(5), LeftTwo(6), LeftThree(7), LeftFour(10), LeftFive(9), LeftSix(8),
        RightOne(11), RightTwo(12), RightThree(13), RightFour(16), RightFive(15), RightSix(14),
    }

    operator fun Joystick.get(button: JoystickButton) = getRawButton(button.raw)
    private fun <Input> s(f: () -> Input) = sensor { f() stampWith it }

    val joystickMapping by pref {
        val exponent by pref(2)
        val deadband by pref(2, Percent)
        ({
            val db = horizontalDeadband(deadband, 100.Percent)
            fun(x: Dimensionless) = db(x).abs.pow(exponent.Each).withSign(x)
        })
    }

    val manualOverride = s { -joystickMapping(operator.y.Each) }

    // CLIMBER
    val deployHooks = s { operator[RightOne] && operator[RightSix] }
    val deployForks = s { operator[RightThree] }
    val climb = s { driver[LeftOne] }
    val manualClimb = s { operator[RightFour] }

    // DRIVETRAIN
    val accelerator = s { -joystickMapping(driver.y.Each) }
    val steering = s { joystickMapping(wheel.x.Each) }

    // LIFT
    val twistAdjust = s { joystickMapping(operator.y.Each) }
    val collect = s { driver[Trigger] }
    val exchange = s { operator[BottomTrigger] }
    val switch = s { operator[LeftTrigger] }
    val lowScale = s { operator[LeftFour] }
    val highScale = s { operator[RightTrigger] }
    val maxHeight = s { operator[LeftOne] }
    val manualLift = s { operator[LeftFive] }

    val upCubeStack = s { operator.getRawButtonPressed(RightTrigger.raw) }
    val downCubeStack = s { operator.getRawButtonPressed(LeftTrigger.raw) }
    val zeroCubeStack = s { operator.getRawButtonPressed(BottomTrigger.raw) }

    // COLLECTOR
    val purge = s { driver[LeftTrigger] || operator[Trigger] }
    val pivotDown = s { driver[Trigger] || driver[BottomTrigger] || operator[LeftTwo] }
    val openClamp = s { driver[BottomTrigger] || operator[LeftThree] }
}