package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import info.kunalsheth.units.generated.*

suspend fun RumbleComponent.set(rumble: Rumble) = startRoutine("Set") {
    controller { rumble }
}

