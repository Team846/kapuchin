package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*

suspend fun RumbleComponent.set(target: Rumble) = startRoutine("Set") {
    controller { target }
}
