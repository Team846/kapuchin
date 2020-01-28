package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {
    choreography {
        runWhenever(

        )
    }
}
