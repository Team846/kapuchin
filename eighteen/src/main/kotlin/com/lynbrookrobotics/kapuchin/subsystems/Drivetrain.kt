package com.lynbrookrobotics.kapuchin.subsystems

import com.lynbrookrobotics.kapuchin.control.TwoSided
import com.lynbrookrobotics.kapuchin.control.loops.pid.PidConfig
import com.lynbrookrobotics.kapuchin.delegates.preferences.pref
import com.lynbrookrobotics.kapuchin.delegates.preferences.prefGain
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.timing.Priority
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val positionGains by pref {
        val kP by prefGain(12::Volt, 3::Foot)
        val kI by prefGain(0::Volt, 1::Foot)
        val kD by prefGain(12::Volt, 13::FootPerSecond)
        ({ PidConfig(kP, kI, kD) })
    }

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