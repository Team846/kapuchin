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

    val m1Hardware = ModuleHardware(1,2,3,4)
    val m2Hardware = ModuleHardware(1,2,3,4)
    val m3Hardware = ModuleHardware(1,2,3,4)
    val m4Hardware = ModuleHardware(1,2,3,4)

    val m1Comp = ModuleComponent(m1Hardware)
    val m2Comp = ModuleComponent(m1Hardware)
    val m3Comp = ModuleComponent(m1Hardware)
    val m4Comp = ModuleComponent(m1Hardware)

    val modules = mutableListOf<ModuleComponent>(m1Comp, m2Comp, m3Comp, m4Comp)

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