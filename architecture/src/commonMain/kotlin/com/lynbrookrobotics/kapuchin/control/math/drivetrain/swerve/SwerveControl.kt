package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt
import kotlin.math.atan2

class SwerveControl(private val radius: Length, private var lengthBy2: Length) {
    fun swerveModuleAngles(targetVelocity: UomVector<Velocity>, targetAngularVelocity: AngularVelocity, pos: Position){

        var radiusVecs = mutableListOf(UomVector(radius,lengthBy2), UomVector(radius,-lengthBy2), UomVector(-radius,-lengthBy2), UomVector(-radius,lengthBy2))
        val wheelVecs = Array(4) {Pair(0.Inch / 0.Second, 0.Radian)}

        val v1x = targetVelocity.x + targetAngularVelocity * radiusVecs[0].x / Radian
        val v1y = targetVelocity.y + targetAngularVelocity * radiusVecs[0].y / Radian
        val vec = UomVector(v1x, v1y)

        val mag = vec.magnitude()
    }

    private fun UomVector<Velocity>.magnitude() = this.x.times(this.x) + this.y.times(this.y) //Returns square of what it actually should
}


