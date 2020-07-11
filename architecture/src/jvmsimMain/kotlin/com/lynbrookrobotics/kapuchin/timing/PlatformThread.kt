package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.math.roundToLong

actual class PlatformThread internal actual constructor(
    parent: Named,
    name: String,
    priority: Priority,
    run: () -> Unit
) {
    private val thread = thread(name = "${parent.name} $name Thread", block = run)
}

actual inline fun <R> blockingMutex(lock: Any, block: () -> R) = kotlin.synchronized(lock, block)
actual fun blockingDelay(time: Time) {
    val millis = floor(time, Millisecond)
    val millisLong = millis.milli(Second).roundToLong()
    val nanosLong = (time - millis).nano(Second).roundToInt()
    Thread.sleep(millisLong, nanosLong)
}

val coroutineNamed = Named("Kapuchin Coroutines")
private var numThreads = 0
private val pool = Executors.newFixedThreadPool(4) {
    Thread(it).apply {
        priority = Thread.MIN_PRIORITY
        isDaemon = true
        name = coroutineNamed.name + numThreads++
    }
}
actual val scope = CoroutineScope(pool.asCoroutineDispatcher() +
        CoroutineName("Kapuchin Coroutine Scope") +
        CoroutineExceptionHandler { _, throwable: Throwable ->
            coroutineNamed.log(Level.Error, throwable) { "Exception thrown from coroutine" }
        }
)