package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module.*
import info.kunalsheth.units.generated.*

class SwerveComponent(hardware: SwerveHardware,
                      override val fallbackController: SwerveComponent.(Time) -> FourSided<OffloadedOutput>,
                      override val maxSpeed: Velocity,
                      override val bearingKp: Gain<Velocity, Angle>,
                      override val bearingKd: Gain<Velocity, AngularVelocity>
) :
    Component<SwerveComponent, SwerveHardware, FourSided<OffloadedOutput>>(hardware),
    GenericDriveComponent {



    override fun SwerveHardware.output(value: FourSided<OffloadedOutput>) {
        TODO("Not yet implemented")
    }



    fun SwerveHardware.output(value: List<Pair<OffloadedOutput, OffloadedOutput>>) {
        for(i in 0 until modules.size){
            with(modules[i]){
                hardware.output(value[i].first, value[i].second)
            }
        }

    }

    init {
//        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set) { hardware.jitterReadPin.period.Second }
    }

}