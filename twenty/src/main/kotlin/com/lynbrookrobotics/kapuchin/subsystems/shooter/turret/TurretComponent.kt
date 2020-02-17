package com.lynbrookrobotics.kapuchin.subsystems.shooter.turret

import com.lynbrookrobotics.kapuchin.Subsystems.Companion.shooterTicker
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*

class TurretComponent(hardware: TurretHardware) : Component<TurretComponent, TurretHardware, OffloadedOutput>(hardware, shooterTicker) {

    private val safeSpeed by pref(3, Volt)

    val positionGains by pref {
        val kP by pref(12, Volt, 45, Degree)
        val kD by pref(0, Volt, 60, DegreePerSecond)
        ({
            OffloadedEscGains(
                    syncThreshold = hardware.syncThreshold,
                    kP = hardware.conversions.encoder.native(kP),
                    kD = hardware.conversions.encoder.native(kD)
            )
        })
    }

    override val fallbackController: TurretComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun TurretHardware.output(value: OffloadedOutput) = with(hardware.conversions) {
        val safeValue = if (!isZeroed) value.with(value.config.copy(
                peakOutputForward = safeSpeed,
                peakOutputReverse = -safeSpeed
        ))
        else value.with(OffloadedEscSafeties(hardware.syncThreshold,
                min = encoder.native(min),
                max = encoder.native(max)
        ))

        safeValue.writeTo(esc, pidController)
    }
}