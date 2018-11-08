package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.scope
import com.lynbrookrobotics.kapuchin.timing.currentTime
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ComponentTest {

    private object ComponentTestElc : TC<ComponentTestElc, ComponentTestHardw>(ComponentTestHardw, EventLoop)
    private object ComponentTestHardw : TSH<ComponentTestHardw, ComponentTestElc>("ComponentTest Hardware")

    @Test(timeout = 1 * 1000)
    fun `event loop components only update on event loop ticks`() {
        val j = scope.launch { ComponentTestElc.countTo(10) }
        while (ComponentTestElc.routine == null) Thread.sleep(1)
        ComponentTestElc.checkCount(10, 0)
        EventLoop.tick(currentTime)
        ComponentTestElc.checkCount(10, 1)
        EventLoop.tick(currentTime)
        EventLoop.tick(currentTime)
        EventLoop.tick(currentTime)
        ComponentTestElc.checkCount(10, 4)
        repeat(10) { EventLoop.tick(currentTime) }
        ComponentTestElc.checkCount(10, 10)
        runBlocking { j.join() }
    }
}