package com.lynbrookrobotics.kapuchin.control.conversion

import info.kunalsheth.units.generated.*

internal typealias t = Second
typealias LinearOffloadedNativeConversion = OffloadedNativeConversion<V, Absement, L, Velocity, Acceleration>
typealias AngularOffloadedNativeConversion = OffloadedNativeConversion<V, AngularAbsement, Angle, AngularVelocity, AngularAcceleration>