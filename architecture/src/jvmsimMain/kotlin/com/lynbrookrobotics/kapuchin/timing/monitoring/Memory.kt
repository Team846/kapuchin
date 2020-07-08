package com.lynbrookrobotics.kapuchin.timing.monitoring

private val r = Runtime.getRuntime()
actual val usedMemory get() = r.totalMemory() - r.freeMemory()
actual val maxMemory get() = r.maxMemory()