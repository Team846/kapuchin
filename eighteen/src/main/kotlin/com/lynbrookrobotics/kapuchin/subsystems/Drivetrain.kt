package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.TwoSided
import com.lynbrookrobotics.kapuchin.control.conversion.TalonNativeConversion
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidConfig
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedPidConfig
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val positionGains by pref {
        val kP by pref(12::Volt, 3::Foot)
        val kI by pref(0::Volt, 1::FootSecond)
        val kD by pref(12::Volt, 13::FootPerSecond)
        ({ PidConfig(kP, kI, kD) })
    }

    val wheelDiameter by pref(6::Inch)
    val gearRatio by pref {
        val driver by pref(18::Tick)
        val driven by pref(74::Tick)
        ({ Gear })
    }

    val offloadedSettings by pref {
        val nativeUnits by pref(4096::Tick)
        val perLength
        TalonNativeConversion
    }

    fun positionGainsToNative() = OffloadedPidConfig(
            kp =
    )

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> =
            { TwoSided(VelocityOutput(), VelocityOutput()) }

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {

    }
}

class DrivetrainHardware : Hardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(::Second)
    override val syncThreshold = 1.milli(::Second)
    override val name = "Drivetrain"
}