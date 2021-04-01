package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

/**
 * Helper class for the swerve odometry algorithm
 * @param pos --> initial position of the robot (most likely 0,0,0)
 * @param radius --> axle length / 2 or just the radius of the robot
 * @author Kaustubh
 */
class SwerveOdometry(var pos: Position, private val radius: Length, private val lenBy2: Length) {

    /**
     * Updates the current `Position` of the swerve drive given the angle and how far each module has travelled
     * @param modules --> array of size four, contains `Pair<Length, Angle>` where Length
     * is distance traveled by the wheel and Angle is the current angle of the wheel
     * @return Position --> new position of the robot given starting position and `modules`
     */
    fun updatePosition(modules: Array<Pair<Vector<Length>, Angle>>): Position? {
        val x_top =
            (modules[0].first.x * cos(modules[0].second)) avg (modules[1].first.x * cos(modules[1].second)) // may need to be something other than avg
        val x_bottom = (modules[2].first.x * cos(modules[2].second)) avg (modules[3].first.x * cos(modules[3].second))

        val y_left = (modules[0].first.y * sin(modules[0].second)) avg (modules[2].first.y * sin(modules[2].second))
        val y_right = (modules[1].first.y * sin(modules[1].second)) avg (modules[3].first.y * sin(modules[3].second))

        return null //TODO(Fix this)
    }

    fun getPoint(dLeft: Length, dRight: Length) {
        val dCenter = dLeft avg dRight
    }
}

private infix fun L1A0M0T0I0Theta0N0J0.avg(that: Length) = (this + that) / 2
