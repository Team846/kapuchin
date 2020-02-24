package com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.shooterTicker
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware, shooterTicker) {

    val maxSpeed by pref(9632, Rpm)
    val momentFactor by pref(1.4)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1.2, PoundFootSquared)
    val fudgeFactor by pref(100, Percent)
    val shooterHeight by pref(24, Inch) // shooter height from the floor

    val idleOutput by pref(50, Percent)
    val preset by pref(3000, Rpm)
    val tolerance by pref(10, Rpm)
    val ballThreshold by pref(-1000, RpmPerSecond)

    val velocityGains by pref {
        val kP by pref(10, Volt, 100, Rpm)
        val kF by pref(110, Percent)
        ({
            OffloadedEscGains(
                    kP = hardware.conversions.encoder.native(kP),
                    kF = hardware.conversions.encoder.native(
                            Gain(hardware.escConfig.voltageCompSaturation, maxSpeed)
                    ) * kF.Each
            )
        })
    }

    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, idleOutput)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
    }
}



