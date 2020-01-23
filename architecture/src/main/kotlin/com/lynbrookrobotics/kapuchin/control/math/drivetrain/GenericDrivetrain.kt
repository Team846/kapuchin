package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

interface GenericDrivetrainConversions {
    val trackLength: Length
}

interface GenericDrivetrainHardware {
    val position: Sensor<Position>
    val conversions: GenericDrivetrainConversions
}

interface GenericDrivetrainComponent : Named {
    val hardware: GenericDrivetrainHardware
    val bearingKp: Gain<Velocity, Angle>
    val bearingKf: Gain<Velocity, AngularVelocity>
}
