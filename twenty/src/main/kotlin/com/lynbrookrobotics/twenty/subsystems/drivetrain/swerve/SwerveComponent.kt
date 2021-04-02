package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware,
                      override val fallbackController: SwerveComponent.(Time) -> FourSided<OffloadedOutput>
) :
    Component<SwerveComponent, SwerveHardware, FourSided<OffloadedOutput>>(hardware),
    GenericDriveComponent {

    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val TR = OffloadedEscGains(
                kP = hardware.conversions.encoder.topRight.native(kP),
                kF = hardware.conversions.encoder.topRight.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxLeftSpeed)
                ) * kF.Each
            )
            val TL = OffloadedEscGains(
                kP = hardware.conversions.encoder.topLeft.native(kP),
                kF = hardware.conversions.encoder.topLeft.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxRightSpeed)
                ) * kF.Each
            )
//            val BR = OffloadedEscGains(
//                kP = hardware.conversions.encoder.BottomRight.native(kP),
//                kF = har
//            )
//            TwoSided(left, right)
        })
    }

    override fun SwerveHardware.output(value: FourSided<OffloadedOutput>) {
        TODO("Not yet implemented")
    }

}