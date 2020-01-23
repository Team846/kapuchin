package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Given a differential drivetrain and a [Trajectory], calculate the left and right velocities at a specific time.
 *
 * @author Andy, Alvyn
 *
 * @property drivetrain a tank drive drivetrain component.
 * @param trajectory the trajectory to follow.
 * @param origin the starting position of the robot.
 */
class TrajectoryFollower(
        val drivetrain: GenericDrivetrainComponent,
        trajectory: Trajectory,
        origin: Position = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y
) {
    private val matrix = RotationMatrix(origin.bearing)

    // Make segment waypoints relative to origin
    private val segments = trajectory
            .map { it.copy(waypt = matrix.rotate(it.waypt) + origin.vector) }
            .iterator()

    private var totalTime = 0.Second
    private var previousTime: Time? = null

    private var currentSegment = segments.next()
    private var currentSegmentEndingTime = distance(currentSegment.waypt, origin.vector) / currentSegment.velocity
    private var done = false

    fun simpleOutput(time: Time, currPosition: Position): TwoSided<Velocity>? {

        val vector = currPosition.vector

        val dt = time - (previousTime ?: time)
        previousTime = time
        totalTime += dt

        if (totalTime > currentSegmentEndingTime) {
            if (!segments.hasNext()) {
                done = true
            } else {
                val prevSegment = currentSegment
                currentSegment = segments.next()
                currentSegmentEndingTime += distance(currentSegment.waypt, prevSegment.waypt) / currentSegment.velocity
            }
        }

        val angleError =  atan2(currentSegment.waypt.x - vector.x, currentSegment.waypt.y - vector.y)

        //http://faculty.salina.k-state.edu/tim/robotics_sg/Control/kinematics/unicycle.html
        with(drivetrain.hardware.conversions) {
            val velocityL = currentSegment.velocity - ((currentSegment.omega / Radian) * trackLength) / 2
            val velocityR = currentSegment.velocity + ((currentSegment.omega / Radian) * trackLength) / 2
            val velocityFeedback = drivetrain.bearingKp * angleError + drivetrain.bearingKf * currentSegment.omega
            return TwoSided(velocityL + velocityFeedback, velocityR - velocityFeedback).takeIf { !done }
        }
    }
}