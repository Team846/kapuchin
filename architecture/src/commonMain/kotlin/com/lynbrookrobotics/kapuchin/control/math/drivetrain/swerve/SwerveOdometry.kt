package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.log

/**
 * Helper class for the swerve odometry algorithm
 * @param pos --> initial position of the robot (most likely 0,0,0)
 * @param radius --> axle length / 2 or just the radius of the robot
 * @author Kaustubh
 */
class SwerveOdometry(var pos: Position, private val radius: Length) {

    /**
     * Updates the current `Position` of the swerve drive given the angle and how far each module has travelled
     * @param modules --> array of size four, contains `Pair<Length, Angle>` where Length
     * is distance traveled by the wheel and Angle is the current angle of the wheel
     * @return Position --> new position of the robot given starting position and `modules`
     */
    fun updatePosition(modules: Array<Pair<Length, Angle>>): Position?{
        if(modules.size != 4) return null

        var xDist: Length = 0.Inch
        var yDist: Length = 0.Inch
        var thetaDist: Angle = 0.Radian
        modules.forEach{
            xDist += it.first * cos(it.second)
            yDist += it.first * cos(it.second)
            thetaDist += (it.first / radius) * Radian
        }
        pos += Position(xDist, yDist, thetaDist)
        return pos
    }
}