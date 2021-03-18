package com.lynbrookrobotics.twenty.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import edu.wpi.first.wpilibj.controller.LinearQuadraticRegulator
import edu.wpi.first.wpilibj.system.NumericalJacobian
import edu.wpi.first.wpiutil.math.Matrix
import edu.wpi.first.wpiutil.math.Nat
import edu.wpi.first.wpiutil.math.Num
import edu.wpi.first.wpiutil.math.numbers.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import edu.wpi.first.math.Drake
import edu.wpi.first.wpilibj.math.Discretization
import edu.wpi.first.wpilibj.system.LinearSystemLoop
import edu.wpi.first.wpiutil.math.Pair
import org.ejml.simple.SimpleMatrix

class DrivetrainState <States:Num, Inputs:Num>  (val rows: Nat<States>,
                                                 val cols: Nat<Inputs>,
                                                 val x_pos: Length,
                           val y_pos: Length,
                           val bearing: Angle,
                           val velocity: Velocity,
                           val omega: AngularVelocity, val leftInput: Volt, val rightInput: Volt){
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

class OptimalGainMatrix<States: Num, Inputs: Num>(Q: Matrix<States, States>, R: Matrix<Inputs, Inputs>){

    private val Q_matrix = Q
    private val R_matrix = R

    fun compute (hardware: DrivetrainHardware,
                rows: Nat<States>, cols: Nat<States>,
                state: DrivetrainState<States, Inputs>,
                desiredState: DrivetrainState<States,Inputs>,
                state_jacobian: Matrix<States, States>,
                input_jacobian: Matrix<States, Inputs>) : Matrix<Inputs, N1>
    {
        val stateError = desiredState.getStateError(state.x)

        val discABPair =
            Discretization.discretizeAB(state_jacobian, input_jacobian, hardware.period.Millisecond)
        val discA = discABPair.first
        val discB = discABPair.second

        val S = Matrix<States,States>(Drake.discreteAlgebraicRiccatiEquation(discA.storage, discB.storage, Q_matrix.storage, R_matrix.storage))

        val temp: Matrix<Inputs, Inputs> = discB.transpose().times(S).times(discB).plus(R_matrix)

        val m_K = temp.solve(discB.transpose().times(S).times(discA))

        return desiredState.u.plus(m_K.times(stateError))


    }

}
class DrivetrainComponent(hardware: DrivetrainHardware) :
    Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware),
    GenericDrivetrainComponent {

    val maxLeftSpeed by pref(11.9, FootPerSecond)
    val maxRightSpeed by pref(12.5, FootPerSecond)
    val maxAcceleration by pref(10, FootPerSecondSquared)
    val percentMaxOmega by pref(75, Percent)

    val speedFactor by pref(50, Percent)
    val maxExtrapolate by pref(40, Inch)

    private val LQR = Named("LQR", this)
    val robotMoment by LQR.pref(67.212, PoundFootSquared)
    //motor constant for the falcon, unit is VoltSeconds
    val k_emf by LQR.pref(0.1508, Volt * Second)
    val k_stall by LQR.pref(6.566, Newton * Metre / Volt)

    override val maxSpeed get() = maxLeftSpeed min maxRightSpeed
    val maxOmega get() = maxSpeed / hardware.conversions.trackLength / 2 * Radian

    val velocityGains by pref {
        val kP by pref(5, Volt, 2, FootPerSecond)
        val kF by pref(110, Percent)
        ({
            val left = OffloadedEscGains(
                kP = hardware.conversions.encoder.left.native(kP),
                kF = hardware.conversions.encoder.left.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxLeftSpeed)
                ) * kF.Each
            )
            val right = OffloadedEscGains(
                kP = hardware.conversions.encoder.right.native(kP),
                kF = hardware.conversions.encoder.right.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxRightSpeed)
                ) * kF.Each
            )
            TwoSided(left, right)
        })
    }

    private val bearingGainsNamed = Named("bearingGains", this)
    override val bearingKp by bearingGainsNamed.pref(5, FootPerSecond, 45, Degree)
    override val bearingKd by bearingGainsNamed.pref(3, FootPerSecond, 360, DegreePerSecond)

    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(PercentOutput(hardware.escConfig, 0.Percent))
    }

    private val leftEscOutputGraph = graph("Left ESC Output", Volt)
    private val rightEscOutputGraph = graph("Right ESC Output", Volt)

    private val leftEscErrorGraph = graph("Left ESC Error", Each)
    private val rightEscErrorGraph = graph("Right ESC Error", Each)

    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        value.left.writeTo(leftMasterEsc)
        value.right.writeTo(rightMasterEsc)

        leftEscOutputGraph(currentTime, leftMasterEsc.motorOutputVoltage.Volt)
        rightEscOutputGraph(currentTime, rightMasterEsc.motorOutputVoltage.Volt)

        leftEscErrorGraph(currentTime, leftMasterEsc.closedLoopError.Each)
        rightEscErrorGraph(currentTime, rightMasterEsc.closedLoopError.Each)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set) { hardware.jitterReadPin.period.Second }
    }
}