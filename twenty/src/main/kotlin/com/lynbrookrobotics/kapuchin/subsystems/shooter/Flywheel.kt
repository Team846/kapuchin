package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType.kBrushless
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware) {

    val outerGoalEntryTolerance by pref(65, Degree) // The magnitude of entry tolerance is 65 Deg. if aiming for the middle

    val hexagonHeight by pref(30, Inch) // "diameter" of outer goal
    val outerInnerDiff by pref(25.25, Inch) // Distance between outer and inner goal

    val boundingCircleRadius by pref(12.252, Inch) // Feasibility circle of outer goal
    val slippage by pref(0, Rpm)

    val height by pref(24, Inch) // Turret height
    val maxOmega by pref(5676, Rpm)
    val momentFactor by pref(1.4)
    val ballMass by pref(0.141748, Kilogram)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1, PoundFootSquared) // TODO
    val targetHeight by pref(98.25, Inch) // Height from base to center of outer goal

    // TODO velocity gains
    // TODO native encoder units to flywheel omega conversion

    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
    }
}

class FlywheelHardware : SubsystemHardware<FlywheelHardware, FlywheelComponent>() {
    override val period = 50.milli(Second)
    override val syncThreshold = 20.milli(Second)
    override val priority = Priority.High
    override val name = "Shooter Flywheel"

    private val invertMaster by pref(false)
    private val invertSlave by pref(false)

    val escConfig by escConfigPref(
            defaultContinuousCurrentLimit = 30.Ampere,
            defaultPeakCurrentLimit = 60.Ampere
    )

    private val masterEscId = 50
    private val slaveEscId = 51

    val masterEsc by hardw { CANSparkMax(masterEscId, kBrushless) }.configure {
        setupMaster(it, escConfig, false)
        it.inverted = invertMaster
    }

    val slaveEsc by hardw { CANSparkMax(slaveEscId, kBrushless) }.configure {
        generalSetup(it, escConfig)
        it.follow(masterEsc, invertMaster != invertSlave)
    }

    val pidController by hardw { masterEsc.pidController!! }

    // TODO current omega sensor
    val encoder by hardw {masterEsc.getEncoder()}

    val omega = sensor {
        {
            Velocity(encoder.velocity)
        } stampWith it
    }

}

