package com.lynbrookrobotics.kapuchin.logging

import info.kunalsheth.units.generated.Time
import kotlinx.coroutines.experimental.Job

expect class Grapher(parent: Named, name: String) : Named, (Time, Double) -> Job {
    override fun invoke(stamp: Time, value: Double): Job
}