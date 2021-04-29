package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module

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
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import info.kunalsheth.units.generated.*

class ModuleComponent(hardware: ModuleHardware
) :    Component<ModuleComponent, ModuleHardware, OffloadedOutput>(hardware),
    GenericWheelComponent {

    val maxSpeed by pref(10, Foot)
    val maxAcceleration by pref(5,FootPerSecondSquared)
    val percentMaxOmega by pref(75, Percent)

    val speedFactor by pref(50, Percent)

//    override val bearingKp: Gain<Velocity, Angle>
//        get() = TODO("Not yet implemented")
//    override val bearingKd: Gain<Velocity, AngularVelocity>
//        get() = TODO("Not yet implemented")
//    override val fallbackController: ModuleComponent.(Time) -> TwoSided<OffloadedOutput>
//        get() = TODO("Not yet implemented")


    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val wheel = OffloadedEscGains(
                kP = hardware.conversions.wheelEncoder.native(kP),
                kF = hardware.conversions.wheelEncoder.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxSpeed)
                ) * kF.Each
            )
        })
    }

    private val bearingGainsNamed = Named("bearingGains", this)
    override val bearingKp by bearingGainsNamed.pref(5, FootPerSecond, 45, Degree)
    override val bearingKd by bearingGainsNamed.pref(3, FootPerSecond, 360, DegreePerSecond)

    override val fallbackController: ModuleComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    private val wheelEscOutputGraph = graph("Wheel ESC Output", Volt)
    //private val angleEscOutputGraph = graph("Angle ESC Output", Volt)

    private val wheelEscErrorGraph = graph("Wheel ESC Error", Each)
    //private val AngleEscErrorGraph = graph("Angle ESC Error", Each)

    fun ModuleHardware.output(wheelValue: OffloadedOutput, angleValue: OffloadedOutput) {
        wheelValue.writeTo(wheelEsc)
        angleValue.writeTo(angleEsc, pidController)

        wheelEscOutputGraph(currentTime, wheelEsc.motorOutputVoltage.Volt)
        //angleEscOutputGraph(currentTime, angleEsc.motorOutputVoltage.Volt)

        wheelEscErrorGraph(currentTime, wheelEsc.closedLoopError.Each)
        //angleEscErrorGraph(currentTime, angleEsc.closedLoopError.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set) { hardware.jitterReadPin.period.Second }
    }

    override fun ModuleHardware.output(value: OffloadedOutput) {
        TODO("Not yet implemented")
    }

}