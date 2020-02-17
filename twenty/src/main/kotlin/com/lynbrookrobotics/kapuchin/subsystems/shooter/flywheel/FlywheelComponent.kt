package com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware, shooterTicker) {

    private val fieldConstants = Named("fieldConstants", this)

    val targetDiameter by fieldConstants.pref(30, Inch) // "diameter" of the inscribed circle of the outer goal
    val innerGoalDepth by fieldConstants.pref(25.25, Inch) // Distance between outer and inner goal
    val targetHeight by fieldConstants.pref(98.25, Inch) // height from floor to center of outer goal
    val ballMass by fieldConstants.pref(0.141748, Kilogram)
    val ballDiameter by fieldConstants.pref(7, Inch)

    val outerEntryAngleLimit get() = 90.Degree - atan2(ballDiameter, targetDiameter / 2)
    val boundingCircleRadius get() = (targetDiameter / 2) - (ballDiameter / 2)

    val maxSpeed by pref(5676, Rpm)
    val momentFactor by pref(1.4)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1.2, PoundFootSquared)
    val fudgeFactor by pref(100, Percent)
    val shooterHeight by pref(24, Inch) // shooter height from the floor

    val idleOutput by pref(50, Percent)

    val velocityGains by pref {
        val kP by pref(10, Volt, 100, Rpm)
        val kF by pref(110, Percent)
        ({
            OffloadedEscGains(
                    syncThreshold = hardware.syncThreshold,
                    kP = hardware.conversions.encoder.native(kP),
                    kF = hardware.conversions.encoder.native(
                            Gain(12.Volt, maxSpeed)
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



