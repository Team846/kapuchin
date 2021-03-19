package com.lynbrookrobotics.kapuchin.drivetrain

import edu.wpi.first.wpiutil.math.Matrix
import edu.wpi.first.wpiutil.math.Nat
import edu.wpi.first.wpiutil.math.Num
import edu.wpi.first.wpiutil.math.numbers.N1
import info.kunalsheth.units.generated.*

class DrivetrainState <States: Num, Inputs: Num>(
    val rows: Nat<States>,
    val cols: Nat<Inputs>,
    val x_pos: Length,
    val y_pos: Length,
    val bearing: Angle,
    val velocity: Velocity,
    val omega: AngularVelocity,
    val leftInput: `L²⋅M⋅T⁻³⋅I⁻¹`, //Volt
    val rightInput: `L²⋅M⋅T⁻³⋅I⁻¹` //Volt
){
    // state vector
    val x: Matrix<States, N1>
    //input vector
    val u: Matrix<Inputs, N1>
    init {
        x = Matrix.mat(rows, Nat.N1()).fill(
            x_pos.Foot,
            y_pos.Foot,
            bearing.Degree,
            velocity.FootPerSecond,
            omega.DegreePerSecond
        )
        u = Matrix.mat(cols, Nat.N1()).fill(
            leftInput.siValue,
            rightInput.siValue
        )
    }
    fun getStateError (desiredState: Matrix<States, N1>): Matrix<States, N1>? {
        return desiredState.minus(x)
    }
    fun getInputError (desiredInput: Matrix<Inputs, N1>): Matrix<Inputs, N1>? {
        return desiredInput.minus(u)
    }
}