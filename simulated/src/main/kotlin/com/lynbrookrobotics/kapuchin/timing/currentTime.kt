package com.lynbrookrobotics.kapuchin.timing

import info.kunalsheth.units.generated.Second
import info.kunalsheth.units.generated.nano

actual val currentTime = System.nanoTime().nano(::Second)