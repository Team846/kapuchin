package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import edu.wpi.first.wpilibj.Threads
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.concurrent.thread

actual class PlatformThread internal actual constructor(parent: Named, name: String, priority: Priority, run: () -> Unit) {
    private val thread: Thread

    init {
        val (jvmPriority, roboRioPriority) = when (priority) {
            Priority.Lowest -> 1 to 99 // Thread.MIN_PRIORITY
            Priority.Low -> 2 to 75
            Priority.Medium -> 5 to 50 // Thread.NORM_PRIORITY
            Priority.High -> 7 to 25
            Priority.Highest, Priority.RealTime -> 10 to 1 // Thread.MAX_PRIORITY
        }


        val formattedName = "${parent.name} $name Thread"
        thread = parent.run {
            thread(name = formattedName, priority = jvmPriority) {
                log(Debug) { "Starting $formattedName" }

                Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
                    log(Error, e) { "$e thrown from $formattedName" }
                }

                Threads.setCurrentThreadPriority(priority == Priority.RealTime, roboRioPriority)

                run()

                log(Debug) { "$formattedName Exiting" }
            }
        }
    }
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

actual inline fun <R> blockingMutex(lock: Any, block: () -> R) = kotlin.synchronized(lock, block)