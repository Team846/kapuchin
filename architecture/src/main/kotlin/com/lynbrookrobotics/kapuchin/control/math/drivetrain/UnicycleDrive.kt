package com.lynbrookrobotics.kapuchin.control.math.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*

/**
 * Control a drivetrain by treating it as a "unicycle".
 *
 * Instead of directly controlling the left and right side, we input a linear velocity and an angle.
 *
 * @preropty drivetrain a tank drive drivetrain component.
 * @param scope sensor scope of the routine.
 */
class UnicycleDrive(private val drivetrain: GenericDrivetrainComponent, scope: BoundSensorScope) {
    val position by with(scope) { drivetrain.hardware.position.readOnTick.withStamps }
    val dadt = differentiator(::p, position.x, position.y.bearing)

    val errorGraph = drivetrain.graph("Error Angle", Degree)
    val targetGraph = drivetrain.graph("Target Angle", Degree)
    val speedGraph = drivetrain.graph("Target Speed", FootPerSecond)

    fun speedAngleTarget(speed: Velocity, angle: Angle): Pair<TwoSided<Velocity>, Angle> {
        val error = (angle `coterminal -` position.y.bearing)
        return speedTargetAngleError(speed, error) to error
    }

    fun speedTargetAngleError(speed: Velocity, error: Angle) = with(drivetrain) {
        val (t, p) = position

        val angularVelocity = dadt(t, p.bearing)

        val pA = bearingKp * error - bearingKd * angularVelocity

        val targetL = speed + pA
        val targetR = speed - pA

        TwoSided(targetL, targetR).also {
            speedGraph(t, speed)
            targetGraph(t, error `coterminal +` p.bearing)
            errorGraph(t, error)
        }
    }
}