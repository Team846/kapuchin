package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

interface GenericDriveConversions {
    val trackLength: Length
    val botRadius: Length
}

interface GenericDriveHardware {
    val modules: Array<GenericWheelComponent> //size of 4
    val position: Sensor<Position>
    val conversions: GenericDriveConversions

    fun positionDelegate(scope: BoundSensorScope): DelegateProvider<Any?, TimeStamped<Position>> = with(scope) {
        position.readOnTick.withStamps
    }

//    fun angleDelegate(scope: BoundSensorScope): DelegateProvider<Any?, TimeStamped<Array<Angle>>> = with(scope) {
//    }
}

interface GenericDriveComponent: Named {
    val hardware: GenericDriveHardware
    val maxSpeed: Velocity
    val bearingKp: Gain<Velocity, Angle>
    val bearingKd: Gain<Velocity, AngularVelocity>
//    val kP: Gain<Velocity, Length>
//    val kD: Gain<Velocity, Velocity>
    // not sure if there should be positionKp and positionKd
}