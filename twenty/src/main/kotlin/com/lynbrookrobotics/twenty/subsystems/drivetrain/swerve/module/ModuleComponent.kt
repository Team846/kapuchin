package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class ModuleComponent(hardware: ModuleHardware
) :    Component<ModuleComponent, ModuleHardware, TwoSided<OffloadedOutput>>(hardware),
    GenericWheelComponent {
    override val bearingKp: Gain<Velocity, Angle>
        get() = TODO("Not yet implemented")
    override val bearingKd: Gain<Velocity, AngularVelocity>
        get() = TODO("Not yet implemented")
    override val fallbackController: ModuleComponent.(Time) -> TwoSided<OffloadedOutput>
        get() = TODO("Not yet implemented")

    override fun ModuleHardware.output(value: TwoSided<OffloadedOutput>) {
        TODO("Not yet implemented")
    }

}