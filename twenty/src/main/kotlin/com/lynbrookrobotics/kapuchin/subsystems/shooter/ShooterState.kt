package com.lynbrookrobotics.kapuchin.subsystems.shooter

import info.kunalsheth.units.generated.*

data class ShooterState(val rpm: AngularVelocity, val state: ShooterHoodState, val turnAngle: Angle)