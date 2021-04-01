package com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainConversions
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

class SwerveHardware(
) : SubsystemHardware<SwerveHardware, SwerveComponent>(), GenericDriveHardware {
    override val priority = Priority.RealTime
    override val period = 30.milli(Second)
    override val syncThreshold = 5.milli(Second)
    override val name = "Drivetrain"

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)

    private val TREscInversion by pref(false)
    private val TLEscInversion by pref(true)
    private val BRtEscInversion by pref(true)
    private val BLtEscInversion by pref(true)

    private val TRSensorInversion by pref(true)
    private val TLSensorInversion by pref(false)
    private val BRSensorInversion by pref(false)
    private val BLSensorInversion by pref(false)

    private val driftTolerance by pref(0.2, DegreePerSecond)

    val escConfig by escConfigPref(
        defaultNominalOutput = 1.5.Volt,
        defaultContinuousCurrentLimit = 25.Ampere,
        defaultPeakCurrentLimit = 35.Ampere
    )

    private val idx = 0
    private val TR = 30
    private val TL = 32
    private val BR = 33
    private val BL = 31

    override val conversions = SwerveConversions(this)

}