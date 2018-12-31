package com.lynbrookrobotics.kapuchin.timing

import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.Time
import info.kunalsheth.units.math.nano

actual val currentTime: Time get() = System.nanoTime().nano(Second)