package com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.pow

/**
 * Helper class for the swerve control algorithm
 * @param radius width of the robot / 2
 * @param lengthBy2 length of the robot / 2
 * @see SwerveOdometry
 * @author Kaustubh
 */
class SwerveControl(private val radius: Length, private var lengthBy2: Length) {

    //variables stored in order of counter clockwise starting from top right swerve module
    private val radiusVecs = mutableListOf(UomVector(lengthBy2,-lengthBy2), UomVector(lengthBy2,lengthBy2),
        UomVector(-lengthBy2,lengthBy2), UomVector(-lengthBy2,-lengthBy2))


    /**
     * Algorithm that when a `targetVelocity` and `targetAngularVelocity` are provided, it returns the ideal angles
     * each wheel should be moved to. This is done through representing each of the angles as a combination of the
     * velocity components and the angular velocity. Note: the wheelVecs is ordered in counter clockwise order from top
     * view and starts from the top right module.
     *
     * @param targetVelocity is a vector of the x and y components of desired velocity l/s
     * @param targetAngularVelocity is a value of the desired angular velocity in r/s
     * @param pos current robot position (not being used currently but can be for odometry and stuff)
     * @return wheelVecs an array of the target speed and angle to move each of the wheel modules to
     */
    fun swerveModuleAngles(targetVelocity: UomVector<Velocity>, targetAngularVelocity: AngularVelocity, pos: Position): Array<Pair<`L⋅T⁻¹`, Angle>> {
            val wheelVecs = Array(4) {Pair(0.Inch / Second, 0.Radian)}

            wheelVecs.forEachIndexed { index, _ ->
                val vec = UomVector(targetVelocity.x + targetAngularVelocity * radiusVecs[index].x / Radian,
                    targetVelocity.y + targetAngularVelocity * radiusVecs[index].y / Radian)

                wheelVecs[index] = Pair(vec.magnitude() * Foot / Second, vec.bearing() * Radian)
            }

        return wheelVecs
    }

    private fun UomVector<Velocity>.magnitude() = sqrt(this.x.siValue.pow(2) + this.y.siValue.pow(2))

    private fun UomVector<Velocity>.bearing() = atan2(this.x.siValue, this.y.siValue)
}


