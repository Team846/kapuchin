package com.lynbrookrobotics.twenty.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import edu.wpi.first.math.Drake
import edu.wpi.first.wpilibj.math.Discretization
import edu.wpi.first.wpiutil.math.Matrix
import edu.wpi.first.wpiutil.math.Num
import edu.wpi.first.wpiutil.math.numbers.N1
import info.kunalsheth.units.generated.*

class LQRWaypoint<States: Num, Inputs: Num> (desiredState: DrivetrainState<States, Inputs>, input_jacobian: Matrix<States, Inputs>, state_jacobian: Matrix<States, States>){
    val desiredState = desiredState
    val input_jacobian = input_jacobian
    val state_jacobian = state_jacobian
}

typealias LQRTrajectory<States, Inputs> = List<LQRWaypoint<States, Inputs>>

class LQR<States: Num, Inputs: Num>(Q: Matrix<States, States>, R: Matrix<Inputs, Inputs>){

    private val Q_matrix = Q
    private val R_matrix = R

    fun compute(hardware: SubsystemHardware<*, *>,
                state: DrivetrainState<States, Inputs>,
                waypoint: LQRWaypoint<States, Inputs>) : Matrix<Inputs, N1>
    {
        val stateError = waypoint.desiredState.getStateError(state.x)

        val discABPair =
            Discretization.discretizeAB(waypoint.state_jacobian, waypoint.input_jacobian, hardware.period.Millisecond)
        val discA = discABPair.first
        val discB = discABPair.second

        //the solution to our DARE
        val S = Matrix<States,States>(Drake.discreteAlgebraicRiccatiEquation(discA.storage, discB.storage, Q_matrix.storage, R_matrix.storage))

        val temp: Matrix<Inputs, Inputs> = discB.transpose().times(S).times(discB).plus(R_matrix)

        //the optimal gain matrix
        val m_K = temp.solve(discB.transpose().times(S).times(discA))

        return waypoint.desiredState.u.minus(m_K.times(stateError))


    }

}