package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level.Debug
import com.lynbrookrobotics.kapuchin.logging.Level.Error
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import edu.wpi.first.wpilibj.Threads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.concurrent.thread

actual class PlatformThread private actual constructor(parent: Named, name: String, priority: Priority, run: () -> Unit) {
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

    actual companion object {
        actual fun Named.platformThread(name: String, priority: Priority, run: () -> Unit) = PlatformThread(this, name, priority, run)
    }
}

actual val scope: CoroutineScope = GlobalScope
actual inline fun <R> blockingMutex(lock: Any, block: () -> R) = kotlin.synchronized(lock, block)