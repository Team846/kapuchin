package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.TwoSided
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.conversion.TalonNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidGains
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VoltageOutput
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*
import kotlin.math.PI

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val wheelDiameter by pref(6::Inch)
    val wheelCircumference get() = wheelDiameter * PI

    val encoderToWheelGears by pref {
        val driver by pref(18)
        val driven by pref(74)
        ({ GearTrain(driver, driven) })
    }

    val encoderSettings by pref {
        val nativeUnits by pref(4096::Tick)
        val perEncoderAngle by pref(360::Degree)
        ({
            TalonNativeConversion(
                    nativeUnits = nativeUnits,
                    perQuantity = wheelCircumference * encoderToWheelGears.inputToOutput(perEncoderAngle).Turn
            )
        })
    }

    val positionGains by pref {
        val kP by pref(12::Volt, 3::Foot)
        val kI by pref(0::Volt, 1::FootSecond)
        val kD by pref(12::Volt, 13::FootPerSecond)
        ({ PidGains(kP, kI, kD) })
    }

    val nativePositionGains() {

    }

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> =
            { TwoSided(VoltageOutput(12.Volt), VoltageOutput((12.Volt))) }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {

    }
}

class DrivetrainHardware : Hardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(::Second)
    override val syncThreshold = 1.milli(::Second)
    override val name = "Drivetrain"
}