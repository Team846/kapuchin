package com.lynbrookrobotics.twenty.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.Subsystems
import com.lynbrookrobotics.twenty.choreos.spinUpShooter
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*


val Length.squared: `L²` get() = this * this

suspend fun FlywheelComponent.interpolatedRPMCalc(
    distance: Length,

    ):  {
    return (30.times(distance.squared)/(1.Foot.squared) + 40.times(distance)/1.Foot + 1000)
}