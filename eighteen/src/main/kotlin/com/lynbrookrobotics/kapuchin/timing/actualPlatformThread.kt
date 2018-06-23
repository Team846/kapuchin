package com.lynbrookrobotics.kapuchin.timing

import com.lynbrookrobotics.kapuchin.logging.Level
import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import edu.wpi.first.wpilibj.Threads
import kotlin.concurrent.thread

actual class PlatformThread actual constructor(parent: Named, name: String, priority: Priority, run: () -> Unit) {
    private val thread: Thread

    init {
        val (jvmPriority, roboRioPriority) = when (priority) {
            Lowest -> 1 to 99 // Thread.MIN_PRIORITY
            Low -> 2 to 75
            Medium -> 5 to 50 // Thread.NORM_PRIORITY
            High -> 7 to 25
            Highest, RealTime -> 10 to 1 // Thread.MAX_PRIORITY
        }


        val formattedName = "${parent.name} $name Thread"
        thread = parent.run {
            thread(name = formattedName, priority = jvmPriority) {
                log(Level.Debug) { "Starting $formattedName Thread" }
                Threads.setCurrentThreadPriority(priority == RealTime, roboRioPriority)
                run()
            }.apply {
                uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
                    log(Level.Error, e.stackTrace) { e.message!! }
                    start()
                }
            }
        }
    }
}