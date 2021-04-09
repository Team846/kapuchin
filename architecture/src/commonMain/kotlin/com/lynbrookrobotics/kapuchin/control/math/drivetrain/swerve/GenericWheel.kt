package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*

interface GenericWheelConversions {
    val wheelRadius: Length
}

interface GenericWheelHardware {
    val angle: Sensor<Angle>
    var position: Sensor<Time>
    val conversions: GenericWheelConversions
}

interface GenericWheelComponent: Named {
    val hardware: GenericWheelHardware
    val bearingKp: Gain<Velocity, Angle>
    val bearingKd: Gain<Velocity, AngularVelocity>
}

