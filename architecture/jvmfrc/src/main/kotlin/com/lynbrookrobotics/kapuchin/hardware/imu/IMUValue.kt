package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector

import info.kunalsheth.units.generated.*

/**
 * Constructs a single datapoint from the IMU.
 */
data class IMUValue(
                    val gyro: UomVector<AngularVelocity>?,
                    val accel: UomVector<Acceleration>?,
                    val magneto: UomVector<MagneticFlux>?
                )