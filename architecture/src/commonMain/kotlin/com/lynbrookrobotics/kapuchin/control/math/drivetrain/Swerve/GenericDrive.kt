package com.lynbrookrobotics.kapuchin.control.math.drivetrain.Swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

interface GenericDriveConversions {
    val trackLength: Length
    val botRadius: Length
}

interface GenericDriveHardware {
    val modules: Array<GenericWheelComponent> //size of 4
    val position: Sensor<Position>
    val conversions: GenericDriveConversions
}

interface GenericDriveComponent: Named {
    val hardware: GenericDriveHardware
    val maxSpeed: Velocity
    val bearingKp: Gain<Velocity, Angle>
    val bearingKd: Gain<Velocity, AngularVelocity>
    // not sure if there should be positionKp and positionKd
}