package com.lynbrookrobotics.twenty.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*

class FlywheelComponent(hardware: FlywheelHardware) :
    Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware, Subsystems.shooterTicker) {

    val presetAnitez by pref(3000, Rpm)
    val presetLow by pref(4500, Rpm)
    val presetMed by pref(5000, Rpm)
    val presetHigh by pref(6000, Rpm)

    private val maxSpeed by pref(9632, Rpm)
    val coefA by pref (10)
    val coeffB by pref (10)
    val coeffC by pref (10)
    val tolerance by pref(10, Rpm)

    val velocityGains by pref {
        val kP by pref(10, Volt, 100, Rpm)
        val kD by pref(0, Volt, 100, RpmPerSecond)
        val kF by pref(110, Percent)
        ({
            OffloadedEscGains(
                kP = hardware.conversions.encoder.native(kP),
                kD = hardware.conversions.encoder.native(kD),
                kF = hardware.conversions.encoder.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxSpeed)
                ) * kF.Each
            )
        })
    }

    private val idleOutput by pref(50, Percent)

    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, idleOutput)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
    }
}



