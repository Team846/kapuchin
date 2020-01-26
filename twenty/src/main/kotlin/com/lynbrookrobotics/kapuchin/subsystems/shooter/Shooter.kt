package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

class ShooterComponent(hardware: ShooterHardware) : Component<ShooterComponent, ShooterHardware, TwoSided<DutyCycle>>(hardware) {

    private val shooterHeight by pref(24, Inch)
    private val lowLaunchAngle by pref(1,Degree)
    private val highLaunchAngle by pref(2, Degree)
    private val maxRPM by pref(5676, Rpm)
    private val momentFactor by pref (1.4, Each)
    private val rollerRadius by pref (2,Inch)
    private val ballMass by pref(1, Kilogram)
    private val rollerInertia by pref(1, Each)

    override val fallbackController: ShooterComponent.(Time) -> TwoSided<DutyCycle>
        get() = { TwoSided(0.Percent, 0.Percent) }

    override fun ShooterHardware.output(value: TwoSided<DutyCycle>) {
        if (ballLimitSwitch.get()) {
            leftFlywheelEsc.set(value.left.Each)
            rightFlywheelEsc.set(value.right.Each)
        }
    }
    
    private fun shooterState(target: DetectedTarget) {
        val dist = sqrt(((target.estimate.x * target.estimate.x) + (target.estimate.y * target.estimate.y)).siValue) * 1.Foot

    }
    private fun ballVel(target: DetectedTarget, shooterAngle: Angle): `L⋅T⁻¹` // This needs to toggle between states, not angles
    {
        val straightDist = sqrt((target.estimate.x.Foot *
                target.estimate.x + target.estimate.y.Foot * target.estimate.y).Foot.Each).Each.Foot
        val v_ball = 1.FootPerSecond * sqrt(1.0/2.0) * sqrt((straightDist * straightDist * 32.2) / ((straightDist * tan(shooterAngle) - shooterHeight) * cos(shooterAngle))/1.Foot)
        return v_ball
    }

    private fun rollerVel(target: DetectedTarget, shooterAngle: Angle): `T⁻¹` { // This also needs to toggle between states
        val ballVel = this.ballVel(target, shooterAngle)
        val w = ballVel*rollerRadius*(momentFactor*ballMass+(2*MomentOfInertia(rollerInertia.Each))/(rollerRadius*rollerRadius))/MomentOfInertia(rollerInertia.Each)
        return w
    }

}

class ShooterHardware : SubsystemHardware<ShooterHardware, ShooterComponent>() {
    override val period: Time
        get() = 50.Millisecond
    override val syncThreshold: Time
        get() = 20.Millisecond
    override val priority: Priority
        get() = Priority.High
    override val name: String
        get() = "Shooter"

    private val leftFlywheelId by pref(10)
    private val rightFlywheelId by pref(11)

    private val ballLimitSwitchId by pref(12)

    val ballLimitSwitch by hardw { DigitalInput(ballLimitSwitchId) }

    /**
     * This motor is a NEO 550 controlling the rotation of the shooter's wheels
     */
    val leftFlywheelEsc by hardw { CANSparkMax(leftFlywheelId, kBrushless) }
    val rightFlywheelEsc by hardw { CANSparkMax(rightFlywheelId, kBrushless) }
}