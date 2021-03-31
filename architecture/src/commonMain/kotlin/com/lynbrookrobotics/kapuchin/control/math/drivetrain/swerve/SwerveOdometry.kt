package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.log

class SwerveOdometry(val startPos: Position, private val radius: Length) {
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
        return startPos + Position(xDist, yDist, thetaDist)
    }
}