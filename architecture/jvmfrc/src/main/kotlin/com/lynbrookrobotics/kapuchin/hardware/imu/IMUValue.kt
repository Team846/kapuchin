package com.lynbrookrobotics.kapuchin.hardware.imu

import com.lynbrookrobotics.kapuchin.control.data.UomVector
import info.kunalsheth.units.generated.Angle
import info.kunalsheth.units.generated.AngularVelocity

/**
 * Constructs a single data point from the IMU.
 *
 * @author Nikash Walia
 *
 * @param angVel angular velocity read from gyro
 * @param dAngle change in angle--read from gyro's on-board integration
 */
data class IMUValue(val angVel: UomVector<AngularVelocity>?, val dAngle: UomVector<Angle>?)