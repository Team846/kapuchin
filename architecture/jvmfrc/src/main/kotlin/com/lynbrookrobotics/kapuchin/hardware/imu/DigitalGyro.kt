package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector

import info.kunalsheth.units.generated.*
import java.util.*

import kotlin.collections.*

/**
 * Calibration, calculation for velocity
 */
abstract class DigitalGyro(updatePeriod: Double) {
    //Tick Period of the robot
    private var currentDrift: UomVector<AngularVelocity>? = null

    // List of velocities used for calibration of IMU
    private val calibrationVelocities: Queue<UomVector<AngularVelocity>> = LinkedList()

    // Whether IMU is calibrating
    private var calibrating: Boolean = true

    /**
     * Gets the current velocity
     * @return UomVector
     */
    abstract fun retrieveVelocity(): UomVector<AngularVelocity>

    /**
     * Gets the current change in angle
     * @return UomVector
     */
    abstract fun retrieveDeltaAngle(): UomVector<Angle>

    /**
     * End the collection of values used to calibrate
     */
    fun endCalibration() {
        if (calibrating) {
            val sum = calibrationVelocities.reduce { acc, it -> acc + it }

            currentDrift = sum * (1.0 / calibrationVelocities.size)

            calibrating = false
        }
    }

    fun getVelocities(): UomVector<AngularVelocity> {
        if (calibrating) {
            calibrationVelocities.add(retrieveVelocity())

            if (calibrationVelocities.size > 200) calibrationVelocities.remove()

            return UomVector(DegreePerSecond(0), DegreePerSecond(0), DegreePerSecond(0))
        } else
        {
            return retrieveVelocity() - currentDrift!!
        }
    }

    fun getDeltaAngles(): UomVector<Angle> {
        if (calibrating) return UomVector(Degree(0), Degree(0), Degree(0)) else return retrieveDeltaAngle()
    }
}