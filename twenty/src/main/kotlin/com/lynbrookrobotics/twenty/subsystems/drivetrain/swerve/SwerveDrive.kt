package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve
//
//import com.kauailabs.navx.frc.AHRS
//import com.lynbrookrobotics.kapuchin.control.data.*
//import com.lynbrookrobotics.kapuchin.control.math.*
//import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
//import com.lynbrookrobotics.kapuchin.hardware.*
//import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
//import com.lynbrookrobotics.kapuchin.logging.*
//import com.lynbrookrobotics.kapuchin.preferences.*
//import com.lynbrookrobotics.kapuchin.timing.*
//import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module.ModuleComponent
//import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.module.ModuleHardware
//import edu.wpi.first.wpilibj.SerialPort.Port.kUSB
//import info.kunalsheth.units.generated.*
//
//class SwerveDrive: GenericDriveComponent, GenericDriveConversions {
//    override val trackLength: Length by pref(2, Foot)
//    override val trackWidth: Length by pref(2, Foot)
//    override val maxSpeed: Velocity by pref(12, FootPerSecond)
//
//    private val bearingGainsNamed = Named("bearingGains", this)
//    override val bearingKp by bearingGainsNamed.pref(5, FootPerSecond, 45, Degree)
//    override val bearingKd by bearingGainsNamed.pref(3, FootPerSecond, 360, DegreePerSecond)
//
//    override val name: String = "Swerve Drive"
//
//
//    val m1Hardware = ModuleHardware(1,2,3,4)
//    val m2Hardware = ModuleHardware(1,2,3,4)
//    val m3Hardware = ModuleHardware(1,2,3,4)
//    val m4Hardware = ModuleHardware(1,2,3,4)
//
//    val m1Comp = ModuleComponent(m1Hardware)
//    val m2Comp = ModuleComponent(m2Hardware)
//    val m3Comp = ModuleComponent(m3Hardware)
//    val m4Comp = ModuleComponent(m4Hardware)
//
//    val modules = mutableListOf<ModuleComponent>(m1Comp, m2Comp, m3Comp, m4Comp)
//
//    var position = Position(0.Foot,0.Foot,0.Radian)
//
//    fun SwerveHardware.output(value: List<Pair<OffloadedOutput, OffloadedOutput>>) {
//        for(i in 0 until modules.size){
//            with(modules[i]){
//                hardware.output(value[i].first, value[i].second)
//            }
//        }
//
//    }
//
//    val tracking = SwerveOdometry(position, trackWidth/2, trackLength/2)
//
//    fun odometry(modulesMovements: Array<Pair<Length, Angle>>){
//        tracking.updatePosition(modulesMovements)
//    }
//
//    val conversions = SwerveConversions(this)
//
//    private val driftTolerance by pref(0.2, DegreePerSecond)
//
//    private val gyro by hardw { AHRS(kUSB) }.configure {
//        blockUntil() { it.isConnected }
//        blockUntil() { !it.isCalibrating }
//        it.zeroYaw()
//    }.verify("NavX should be connected") {
//        it.isConnected
//    }.verify("NavX should be finished calibrating on startup") {
//        !it.isCalibrating
//    }.verify("NavX yaw should not drift after calibration") {
//        it.rate.DegreePerSecond in `±`(driftTolerance)
//    }
//
//
//}