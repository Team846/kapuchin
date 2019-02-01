package com.lynbrookrobotics.kapuchin.timing.clock

import com.lynbrookrobotics.kapuchin.timing.blockingMutex
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.First
import com.lynbrookrobotics.kapuchin.timing.clock.Clock.ExecutionOrder.Last
import info.kunalsheth.units.generated.Time

/**
 * Represents a source of periodic updates
 *
 * Lets users register functions which will be ran each time this clock ticks
 *
 * @author Kunal
 * @see Ticker
 * @see EventLoop
 * @see Cancel
 */
interface Clock {

    /**
     * collection of the functions to invoke each tick
     */
    val jobsToRun: MutableList<(tickStart: Time) -> Unit>
    val jobsToKill: MutableSet<(tickStart: Time) -> Unit>

    /**
     * Register a function to be run each tick
     *
     * @param order indicate if the function should be run before or after previously registered functions
     * @param run function to invoke on each tick
     * @return `Cancel` to unregister the function, stopping all future updates.
     */
    fun runOnTick(order: ExecutionOrder = First, run: (tickStart: Time) -> Unit): Cancel {
        blockingMutex(jobsToRun) {
            when (order) {
                First -> jobsToRun.add(0, run)
                Last -> jobsToRun += run
            }
        }
        return Cancel { blockingMutex(jobsToKill) { jobsToKill += run } }
    }

    /**
     * Utility enum to specify when a new function should be executed
     */
    enum class ExecutionOrder {
        First, Last
    }

    /**
     * Run all registered jobs
     *
     * @param atTime tick start time
     */
    fun tick(atTime: Time) = blockingMutex(jobsToRun) {
        // avoid deadlocks
        if (jobsToKill.isNotEmpty()) blockingMutex(jobsToKill) {
            jobsToRun -= jobsToKill
            jobsToKill.clear()
        }

        jobsToRun.forEach { it(atTime) }
    }
}