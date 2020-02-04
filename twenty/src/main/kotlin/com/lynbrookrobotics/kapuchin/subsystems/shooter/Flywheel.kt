package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import edu.wpi.first.wpilibj.DigitalInput
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware) {

    val outerGoalEntryTolerance by pref(65, Degree) // The magnitude of entry tolerance is 65 Deg. if aiming for the middle


    val hexagonHeight by pref(30, Inch) // "height" of outer goal
    val outerInnerDiff by pref(25.25, Inch) // Distance between outer and inner goal

    val boundingCircleRadius by pref(12.252, Inch) // Feasibility circle of outer goal

    val height by pref(24, Inch)
    val maxOmega by pref(5676, Rpm)
    val momentFactor by pref(1.4)
    val ballMass by pref(0.141748, Kilogram)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1, PoundFootSquared) // TODO
    val targetHeight by pref(98.25, Inch) // TODO

    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        println(value.value)
        // TODO output offloaded sparkmax output
    }

}

class FlywheelHardware : SubsystemHardware<FlywheelHardware, FlywheelComponent>() {
    override val period = 50.milli(Second)
    override val syncThreshold = 20.milli(Second)
    override val priority = Priority.High
    override val name = "Shooter Flywheel"

    private val leftFlywheelId = 10
    private val rightFlywheelId = 11
    private val ballLimitSwitchId = 12

    val escConfig by escConfigPref() //TODO

    val ballLimitSwitch by hardw { DigitalInput(ballLimitSwitchId) }
    val leftFlywheelEsc by hardw { CANSparkMax(leftFlywheelId, kBrushless) } // NEO
    val rightFlywheelEsc by hardw { CANSparkMax(rightFlywheelId, kBrushless) } // NEO
}