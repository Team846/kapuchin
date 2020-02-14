package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ComponentTest {

    private class ComponentTestElc : TC<ComponentTestElc, ComponentTestHardw>(ComponentTestHardw(), EventLoop)
    private class ComponentTestHardw : TSH<ComponentTestHardw, ComponentTestElc>("ComponentTest Hardware")

    @Test(timeout = 2 * 1000)
    fun `event loop components only update on event loop ticks`() = threadDumpOnFailure {
        val c = ComponentTestElc()
        blockingDelay(1.Second)

        val j = scope.launch { c.countTo(10) }
        while (c.routine == null) Thread.sleep(1)
        c.checkCount(10, 0)
        EventLoop.tick(currentTime)
        c.checkCount(10, 1)
        EventLoop.tick(currentTime)
        EventLoop.tick(currentTime)
        EventLoop.tick(currentTime)
        c.checkCount(10, 4)
        repeat(10) { EventLoop.tick(currentTime) }
        c.checkCount(10, 10)
        runBlocking { j.join() }
    }
}