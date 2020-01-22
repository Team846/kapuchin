package com.lynbrookrobotics.kapuchin.control.math.drivetrain
//
//import com.lynbrookrobotics.kapuchin.control.data.*
//import com.lynbrookrobotics.kapuchin.timing.*
//import info.kunalsheth.units.generated.*
//
///**
// * Given a differential drivetrain and a [Trajectory], calculate the left and right velocities at a specific time.
// *
// * @author Andy
// *
// * @property drivetrain a tank drive drivetrain component.
// * @param trajectory the trajectory to follow.
// * @param origin the starting position of the robot.
// */
//class TrajectoryFollower(
//        val drivetrain: GenericDrivetrainComponent,
//        trajectory: Trajectory,
//        origin: Position = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y
//) {
//
//    private val matrix = RotationMatrix(origin.bearing)
//
//    // Make segment waypoints relative to origin
//    private val segments = trajectory
//            .map { it.copy(waypt = matrix.rotate(it.waypt) + origin.vector) }
//            .iterator()
//
//    private var totalTime = 0.Second
//    private var previousTime: Time? = null
//
//    private var currentSegment = segments.next()
//    private var velocity = 0.Foot / Second
//    private var omega = 0.Radian / Second
//    private var done = false
//
//    fun simpleOutput(time: Time): TwoSided<Velocity>? {
//        val dt = time - (previousTime ?: time)
//        previousTime = time
//        totalTime += dt
//
//        if (totalTime > currentSegment.time) {
//            if (!segments.hasNext()) {
//                done = true
//            } else {
//                currentSegment = segments.next()
//            }
//        }
//
//        velocity += currentSegment.acceleration * dt
//        omega += currentSegment.alpha * dt
//
//        //http://faculty.salina.k-state.edu/tim/robotics_sg/Control/kinematics/unicycle.html
//        with(drivetrain.hardware.conversions) {
//            val velocityL = velocity - ((omega / Radian) * trackLength) / 2
//            val velocityR = velocity + ((omega / Radian) * trackLength) / 2
//            return TwoSided(velocityL, velocityR).takeIf { !done }
//        }
//    }
//}