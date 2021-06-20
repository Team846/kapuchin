package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.PI

/**
 * Helper class for the swerve odometry algorithm
 * @param pos --> initial position of the robot (most likely 0,0,0)
 * @param radius --> axle length / 2 or just the radius of the robot
 * @author Kaustubh
 */
class SwerveOdometry(var pos: Position, private val radius: Length, private val lenBy2: Length) {

    /**
     * Updates the current `Position` of the swerve drive given the angle and how far each module has travelled
     * Models swerve as a 2 differential drive trains
     * @param modules --> array of size four, contains `Pair<Length, Angle>` where Length
     * is distance traveled by the wheel and Angle is the current angle of the wheel
     * @return Position --> new position of the robot given starting position and `modules`
     */
    fun updatePosition(modules: Array<Pair<Length, Angle>>): Position? {
//        val xTop =
//            (modules[0].first * cos(modules[0].second)) avg (modules[1].first * cos(modules[1].second)) // may need to be something other than avg
//        val xBottom = (modules[2].first * cos(modules[2].second)) avg (modules[3].first * cos(modules[3].second))
//
//        val yLeft = (modules[0].first * sin(modules[0].second)) avg (modules[2].first * sin(modules[2].second))
//        val yRight = (modules[1].first * sin(modules[1].second)) avg (modules[3].first * sin(modules[3].second))
//
//        val xDisplacement = getPoint(xTop, xBottom, r=radius)
//        val yDisplacement = getPoint(yLeft, yRight, pos.bearing + (PI / 2) * Radian, lenBy2)
//
//        pos += xDisplacement + yDisplacement
//        return pos

        val xyComps = modules.map {
            sin(it.second) * it.first to cos(it.second) * it.first;
        }

        val top = xyComps[0].first avg xyComps[1].first
        val bottom = xyComps[3].first avg xyComps[2].first
        val left = xyComps[0].second avg xyComps[3].second
        val right = xyComps[1].second avg xyComps[2].second

        val omega1 = (top - bottom) / radius
        val omega2 = (left - right) / lenBy2
        val omega = (omega1 + omega2) / 2

        val upDown = left + right
        val leftRight = top + bottom

        val b1 = pos.bearing
        val b2 = pos.bearing + (PI / 2) * Radian

//        pos.x += ((upDown * cos(b1) + leftRight * cos(b2)) / 2) * Foot
//        pos.y += (upDown * sin(b1) + leftRight * sin(b2)) / 2
//        pos.bearing += omega

        pos += Position(((upDown * cos(b1) + leftRight * cos(b2)) / 2), (upDown * sin(b1) + leftRight * sin(b2)) / 2, omega * Radian)

//        println("BEARING IS: ${pos.bearing}")


        return pos



    }

     fun setPosition(position: Position){
        pos = position
    }

    private fun getPoint(dLeft: Length, dRight: Length, theta: Angle = pos.bearing, r: Length): Position {
        val dCenter = dLeft avg dRight
        // Intermediate math stuff
//        val phi = ((dLeft - dRight) / (2*radius)) * Radian
//        val xPrime = pos.x + dCenter * cos(theta)
//        val yPrime = pos.y + dCenter * sin(theta)
//        val thetaPrime = theta + phi
        return Position(
            pos.x + dCenter * cos(theta),
            pos.y + dCenter * sin(theta),
            theta + ((dLeft - dRight) / (2 * r)) * Radian
        ) - pos
    }
}

private infix fun L1A0M0T0I0Theta0N0J0.avg(that: Length) = (this + that) / 2
