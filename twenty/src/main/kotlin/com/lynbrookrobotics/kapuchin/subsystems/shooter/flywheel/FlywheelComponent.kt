package com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware) {

    val outerEntryAngleLimit by pref(65, Degree) // The magnitude of entry tolerance is 65 Deg. if aiming for the middle
    val targetDiameter by pref(30, Inch) // "diameter" of outer goal
    val innerGoalDepth by pref(25.25, Inch) // Distance between outer and inner goal
    val boundingCircleRadius by pref(12.252, Inch) // Feasibility circle of outer goal

    val targetHeight by pref(98.25, Inch) // Height from base to center of outer goal
    val shooterHeight by pref(24, Inch) // Turret height

    val maxSpeed by pref(5676, Rpm)
    val momentFactor by pref(1.4)
    val ballMass by pref(0.141748, Kilogram)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1, PoundFootSquared) // TODO ask Sam P. for the correct value
    val fudgeFactor by pref(100, Percent)


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

    val shotDetectionThreshold by pref(10, Percent)

    val idleOutput by pref(50, Percent)
    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, idleOutput)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
    }
}



