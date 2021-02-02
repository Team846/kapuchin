package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

interface GenericDrivetrainConversions {
    val trackLength: Length
}

interface GenericDrivetrainHardware {
    val position: Sensor<Position>
    val conversions: GenericDrivetrainConversions

    /**
     * Jank workaround to access the drivetrain position sensor delegate from TrajectoryFollower.
     *
     * This used to work
     * ```
     * private val position by with(scope) { drivetrain.hardware.position.readOnTick.withoutStamps }
     * ```
     *
     * But now use this function
     * ```
     * private val position by drivetrain.hardware.positionDelegate(scope)
     * ```
     *
     * @param scope the [BoundSensorScope]
     *
     * @author Andy
     */
    fun positionDelegate(
        scope: BoundSensorScope,
    ): DelegateProvider<Any?, TimeStamped<Position>> = with(scope) {
        position.readOnTick.withStamps
    }
}

interface GenericDrivetrainComponent : Named {
    val hardware: GenericDrivetrainHardware
    val bearingKp: Gain<Velocity, Angle>
    val bearingKd: Gain<Velocity, AngularVelocity>
}