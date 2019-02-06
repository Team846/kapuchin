package com.lynbrookrobotics.kapuchin.timing

import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

actual val currentTime: Time get() = System.nanoTime().nano(Second)