package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.FeedbackSystemComponent.*

suspend fun FeedbackSystemComponent.set(feedback: Feedback) = startRoutine("Set") {
    controller { feedback }
}