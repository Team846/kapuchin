package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.AngularVelocity
import info.kunalsheth.units.generated.DegreePerSecond
import info.kunalsheth.units.generated.Degree

import java.util.*

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
            return UomVector(0.DegreePerSecond, 0.DegreePerSecond, 0.DegreePerSecond)
        } else {
            return retrieveVelocity() - currentDrift!!
        }
    }

    fun getDeltaAngles(): UomVector<Angle> {
        if (calibrating) return UomVector(0.Degree, 0.Degree, 0.Degree) else return retrieveDeltaAngle()
    }
}