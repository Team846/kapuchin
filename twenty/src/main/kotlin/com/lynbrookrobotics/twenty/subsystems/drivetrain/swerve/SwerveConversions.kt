package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveConversions(val hardware: SwerveHardware) : Named by Named("Conversions", hardware),
    GenericDriveConversions {

    override val trackLength by pref(2, Foot)
    override val trackWidth by pref(2, Foot)
    val position = Position(0.Foot,0.Foot,0.Radian)

//    val tracking = SwerveOdometry(Position(0.Foot, 0.Foot, 0.Radians), radius, trackLength)
//
//    fun odometry(modules: Array<Pair<Length, Angle>>){
//        if(noTicksFR && modules[0].first != 0.Foot) log(Level.Debug) {
//            "Received first front right tick at ${currentTime withDecimals 2}"
//        }.also { noTicksFR = false }
//        if(noTicksFL && modules[1].first != 0.Foot) log(Level.Debug) {
//            "Received first front left tick at ${currentTime withDecimals 2}"
//        }.also { noTicksBR = false }
//        if(noTicksFR && modules[2].first != 0.Foot) log(Level.Debug) {
//            "Received first back right tick at ${currentTime withDecimals 2}"
//        }.also { noTicksBL = false }
//        if(noTicksFR && modules[3].first != 0.Foot) log(Level.Debug) {
//            "Received first back left tick at ${currentTime withDecimals 2}"
//        }.also { noTicksFR = false }
//
//        tracking.updatePosition(modules)
//
//        lastFrontRight = modules[0].first
//        lastFrontLeft = modules[1].first
//        lastBackRight = modules[2].first
//        lastBackLeft = modules[3].first
//    }

    val tracking = SwerveOdometry(position, trackWidth/2, trackLength/2)

    fun odometry(modulesMovements: Array<Pair<Length, Angle>>){
        tracking.updatePosition(modulesMovements)
    }
}