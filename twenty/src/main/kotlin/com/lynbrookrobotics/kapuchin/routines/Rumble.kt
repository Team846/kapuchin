package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*

suspend fun RumbleComponent.set(rumble: Rumble) = startRoutine("Set") {
    controller { rumble }
}

