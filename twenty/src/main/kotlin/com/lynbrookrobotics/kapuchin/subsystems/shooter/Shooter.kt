package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, OffloadedOutput>(hardware) {

    private val shooterHeight by pref(24, Inch)
    private val maximumAngle by pref(33, Degree) // Maximum entry angle
    private val minimumAngle by pref(17, Degree) // Minimum entry angle
    private val lowLaunchAngle by pref(1, Degree)
    private val highLaunchAngle by pref(2, Degree)
    private var launchAngle = 0.Degree // Set to either low or high based on vision data
    private val maxRPM by pref(5676, Rpm)
    private val momentFactor by pref(1.4, Each)
    private val rollerRadius by pref(2, Inch)
    private val ballMass by pref(1, Kilogram)
    private val rollerInertia by pref(1, Each)

    override val fallbackController: ShooterComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun ShooterHardware.output(value: OffloadedOutput) {
        if (ballLimitSwitch.get()) {
            value.writeTo(masterFlywheelEsc, flywheelPidController)
        }
    }

    private fun distance(target: DetectedTarget): L { // Returns distance to target
        TODO("Return the distance to the target")
    }

    private fun shooterState(target: DetectedTarget) { // Sets the desired launch angle
        val state1 = this.slope(target, lowLaunchAngle)
        val state2 = this.slope(target, highLaunchAngle)
        when {
            state1 < maximumAngle && state1 > minimumAngle -> {
                this.launchAngle = lowLaunchAngle
            }
            state2 > -1 * maximumAngle && state2 < -1 * minimumAngle -> { // Might need to double check the math on this condition
                this.launchAngle = highLaunchAngle
            }
        }
    }

    private fun slope(target: DetectedTarget, launch: Angle): `∠` { // Returns entry slope of ball
        val dist = this.distance(target)
        val slope = (dist * 32.2 * 1.FootPerSecondSquared) / (this.ballVel(target) * this.ballVel(target) * cos(launch) * cos(launch)) // Slope formula
        return atan(slope)
    }

    private fun ballVel(target: DetectedTarget): `L⋅T⁻¹` // Returns required ball velocity
    {
        this.shooterState(target)
        val shooterAngle = launchAngle
        val straightDist = this.distance(target)
        return 1.FootPerSecond * sqrt(1.0 / 2.0) * sqrt((straightDist * straightDist * 32.2) / ((straightDist * tan(shooterAngle) - shooterHeight) * cos(shooterAngle)) / 1.Foot)
    }

    private fun rollerVel(target: DetectedTarget): `∠⋅T⁻¹` { // Returns required rpm
        val ballVel = this.ballVel(target)
        return 1.Rpm * ((ballVel * rollerRadius * (momentFactor * ballMass + (2 * MomentOfInertia(rollerInertia.Each)) / (rollerRadius * rollerRadius)) / MomentOfInertia(rollerInertia.Each)).siValue)
    }

}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val period = 50.Millisecond
    override val syncThreshold = 20.Millisecond
    override val priority = High
    override val name = "Shooter"

    val escConfig by escConfigPref()

    private val leftFlywheelId by pref(10)
    private val rightFlywheelId by pref(11)

    private val ballLimitSwitchId by pref(12)

    val ballLimitSwitch by hardw { DigitalInput(ballLimitSwitchId) }

    /**
     * This motor is a NEO 550 controlling the rotation of the shooter's wheels
     */
    val masterFlywheelEsc by hardw { CANSparkMax(leftFlywheelId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
    }

    val flywheelPidController: CANPIDController by hardw { masterFlywheelEsc.pidController }

    val slaveFlywheelEsc by hardw { CANSparkMax(rightFlywheelId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.follow(masterFlywheelEsc)
    }
}