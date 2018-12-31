package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.AngularVelocity

/**
 * Constructs a single data point from the IMU.
 */
data class IMUValue(val gyro: UomVector<AngularVelocity>?, val dAngle: UomVector<Angle>?)