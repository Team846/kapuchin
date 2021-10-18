package com.lynbrookrobotics.twenty.subsystems.shooter.turret

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*

class TurretComponent(hardware: TurretHardware) :
    Component<TurretComponent, TurretHardware, OffloadedOutput>(hardware, Subsystems.shooterTicker) {

    val positionGains by pref {
        val kP by pref(12, Volt, 45, Degree)
        val kI by pref(1, Volt, 2, DegreeSecond)
        val kD by pref(0, Volt, 60, DegreePerSecond)
        ({
            OffloadedEscGains(
                kP = hardware.conversions.encoder.native(kP),
                kI = hardware.conversions.encoder.native(kI),
                kD = hardware.conversions.encoder.native(kD)
            )
        })
    }

    override val fallbackController: TurretComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun TurretHardware.output(value: OffloadedOutput) = with(hardware.conversions) {
        if (atZero.optimizedRead(currentTime, 0.Second).y) zero()

        val safeValue = value.with(
            OffloadedEscSafeties(
                min = encoder.native(min),
                max = encoder.native(max)
            )
        )

        safeValue.writeTo(esc, pidController)
    }
}