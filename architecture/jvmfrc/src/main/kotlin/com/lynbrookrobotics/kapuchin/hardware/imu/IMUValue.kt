package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector
import info.kunalsheth.units.generated.Acceleration
import info.kunalsheth.units.generated.AngularVelocity
import info.kunalsheth.units.generated.MagneticFlux

/**
 * Constructs a single datapoint from the IMU.
 */
data class IMUValue(
        val gyro: UomVector<AngularVelocity>?,
        val accel: UomVector<Acceleration>?,
        val magneto: UomVector<MagneticFlux>?
)