package com.lynbrookrobotics.kapuchin.subsystems.shooter

import com.lynbrookrobotics.kapuchin.*
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
import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*

class FlywheelComponent(hardware: FlywheelHardware) : Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware) {

    val outerEntryAngleLimit by pref(65, Degree) // The magnitude of entry tolerance is 65 Deg. if aiming for the middle
    val hexagonHeight by pref(30, Inch) // "diameter" of outer goal
    val outerInnerDiff by pref(25.25, Inch) // Distance between outer and inner goal
    val boundingCircleRadius by pref(12.252, Inch) // Feasibility circle of outer goal

    val targetHeight by pref(98.25, Inch) // Height from base to center of outer goal
    val shooterHeight by pref(24, Inch) // Turret height


    val maxOmega by pref(5676, Rpm)
    val momentFactor by pref(1.4)
    val ballMass by pref(0.141748, Kilogram)
    val rollerRadius by pref(2, Inch)
    val momentOfInertia by pref(1, PoundFootSquared) // TODO ask Sam P. for the correct value
    val fudgeFactor by pref(100, Percent)




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

    private val motorGear by pref(45)
    private val flywheelGear by pref(24)

    private val armLevel by pref(0.9)
    private val triggerLevel by pref(0.8)

    private var targetOmega  = 1.Rpm // Change this value to the target omega every time you call the choreo

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

    val encoder by hardw {masterEsc.encoder}

    val conversion = GearTrain(motorGear, flywheelGear, 1)

    val omega = sensor {
        conversion.inputToOutput(encoder.velocity * 1.Rpm) stampWith it
    }.with(graph("Speed", Rpm))

    private fun isRpmDipped(current: AngularVelocity, target: AngularVelocity) : Boolean {
        if (current < target * triggerLevel) return true
        else if (current >= target * armLevel) return false
        return false
    }
        
    fun setTarget(target: AngularVelocity) { targetOmega = target } // Set targetOmega every time you launch the choreo

    val rpmDipped = sensor {
        val omega = this.omega.optimizedRead(it, 0.Second).y
        (isRpmDipped(omega, targetOmega)
        stampWith it)
    }

    init {
        Subsystems.uiBaselineTicker.runOnTick { time ->
            setOf(omega).forEach {
                it.optimizedRead(time, .5.Second)
            }
        }
    }
}

