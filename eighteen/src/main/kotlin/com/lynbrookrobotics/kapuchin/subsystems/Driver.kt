package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.`±`
import com.lynbrookrobotics.kapuchin.control.conversion.deadband.horizontalDeadband
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware.JoystickButton.*
import com.lynbrookrobotics.kapuchin.timing.Priority
import edu.wpi.first.wpilibj.Joystick
import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.milli
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

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

    val joystickMapping: (Double) -> Double by pref {
        val exponent by pref(2)
        val deadband by pref(0.02)
        ({
            fun powerWithSign(x: Double) = abs(x).pow(exponent) * x.sign
            val withDeadband = horizontalDeadband(deadband, 1.0)

            fun(x: Double) = powerWithSign(withDeadband(x))
        })
    }

    val manualOverride = s { -joystickMapping(operator.y) }

    // CLIMBER
    val deployHooks = s { operator[RightOne] && operator[RightSix] }
    val deployForks = s { operator[RightThree] }
    val climb = s { driver[LeftOne] }
    val manualClimb = s { operator[RightFour] }

    // DRIVETRAIN
    val accelerator = s { -joystickMapping(driver.y) }
    val steering = s { joystickMapping(wheel.x) }

    // LIFT
    val twistAdjust = s { joystickMapping(operator.y) }
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