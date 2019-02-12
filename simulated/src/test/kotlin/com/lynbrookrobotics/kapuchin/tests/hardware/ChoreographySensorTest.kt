package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.ranges.rangeTo

class ChoreographySensorTest {

    private class ChoreographySensorTestSH : TSH<ChoreographySensorTestSH, ChoreographySensorTestC>("ChoreographySensorTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
        val sensorC = sensor { Math.random() stampWith currentTime }
    }

    private class ChoreographySensorTestC : TC<ChoreographySensorTestC, ChoreographySensorTestSH>(ChoreographySensorTestSH())

    @Test(timeout = 8 * 1000)
    fun `sensors getting old are in sync`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors getting old are in sync"
            ChoreographySensorTestC().run {
                startChoreo(name) {
                    val a by hardware.sensorA.getOld.withStamps
                    val b by hardware.sensorB.getOld.withStamps

                    val initA = a
                    val initB = b

                    choreography {
                        for (i in 1..10) {
                            countTo(i)
                            checkCount(i, i)

                            checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                            currentTime `is greater than?` a.x
                            currentTime `is greater than?` b.x

                            a `is equal to?` initA
                            b `is equal to?` initB
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `sensors read on event loop are in sync`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors read on event loop are in sync"
            ChoreographySensorTestC().run {
                startChoreo(name) {
                    val st = hardware.syncThreshold
                    val a by hardware.sensorA.readWithEventLoop(st).withStamps
                    val b by hardware.sensorB.readWithEventLoop(st).withStamps
                    var lastStamp = currentTime
                    choreography {
                        for (i in 1..10) {
                            countTo(i)
                            checkCount(i, i)

                            if (i % 2 == 0) EventLoop.tick(currentTime)

                            checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                            currentTime `is greater than?` a.x
                            currentTime `is greater than?` b.x

                            val thisStamp = avg(a.x, b.x)
                            if (i % 2 == 1) lastStamp `is equal to?` thisStamp
                            lastStamp = thisStamp
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 8 * 1000)
    fun `sensors read eagerly are eager and efficient`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors read eagerly are eager and efficient"
            ChoreographySensorTestC().run {
                startChoreo(name) {
                    val st = hardware.syncThreshold
                    val a by hardware.sensorA.readEagerly(st).withStamps
                    val b by hardware.sensorB.readEagerly(st).withStamps
                    choreography {
                        for (i in 1..10) {
                            countTo(i)
                            checkCount(i, i)

                            val a1 = a
                            val b1 = b
                            val a2 = a
                            val b2 = b

                            b2 `is equal to?` b1
                            b1.x `is greater than?` a2.x
                            a2 `is equal to?` a1
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 1 * 1000)
    fun `sensors are updated once before controller initialization`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors are updated once before controller initialization"
            ChoreographySensorTestC().run {
                val start = currentTime
                startChoreo(name) {
                    val st = hardware.syncThreshold
                    val a by hardware.sensorA.getOld.withStamps
                    val b by hardware.sensorB.readEagerly(st).withStamps
                    val c by hardware.sensorC.readWithEventLoop(st).withStamps

                    val a1 = a
                    val b1 = b
                    val c1 = c

                    val end = currentTime
                    choreography {
                        a1.x `is within?` start..end
                        b1.x `is within?` start..end
                        c1.x `is within?` start..end
                    }
                }
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `sensors are read efficiently`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors are read efficiently"
            ChoreographySensorTestC().run {
                startChoreo(name) {
                    val st = hardware.syncThreshold

                    val a1 by hardware.sensorA.readWithEventLoop(st).withStamps
                    val a2 by hardware.sensorA.readEagerly(st).withStamps
                    val b1 by hardware.sensorB.readWithEventLoop(st).withStamps
                    val b2 by hardware.sensorB.readEagerly(st).withStamps
                    choreography {
                        for (i in 1..10) {
                            countTo(i)
                            checkCount(i, i)

                            a1 `is equal to?` a2
                            b1 `is equal to?` b2
                        }

                        EventLoop.tick(currentTime)
                        val a1x = a1.x
                        val a2x = a2.x
                        val b1x = b1.x
                        val b2x = b2.x
                        EventLoop.tick(currentTime)
                        a1x `is equal to?` a1.x
                        a2x `is equal to?` a2.x
                        b1x `is equal to?` b1.x
                        b2x `is equal to?` b2.x
                        delay(st)
                        EventLoop.tick(currentTime)
                        a1.x `is greater than?` a1x
                        b1.x `is greater than?` b1x
                        a1 `is equal to?` a2
                        b1 `is equal to?` b2
                    }
                }
            }
        }
    }

    @Test(timeout = 4 * 1000)
    fun `sensor lambdas are released upon choreography completion`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensor lambdas are released upon choreography completion"
            ChoreographySensorTestC().run {

                val ogClockJobs = clock.jobsToRun.size
                val ogElJobs = EventLoop.jobsToRun.size
                suspend fun check() {
                    while (clock.jobsToRun.size > ogClockJobs) {
                        delay(1.milli(Second))
                    }
                    while (EventLoop.jobsToRun.size > ogElJobs) {
                        EventLoop.tick(currentTime)
                        delay(1.milli(Second))
                    }
                }

                suspend fun choreography() = startChoreo(name) {
                    val st = hardware.syncThreshold

                    val a1 by hardware.sensorA.getOld.withStamps
                    val a2 by hardware.sensorA.readWithEventLoop(st).withStamps
                    val a3 by hardware.sensorA.readEagerly(st).withStamps

                    val a4 by hardware.sensorA.getOld.withoutStamps
                    val a5 by hardware.sensorA.readWithEventLoop(st).withoutStamps
                    val a6 by hardware.sensorA.readEagerly(st).withoutStamps

                    val b1 by hardware.sensorB.getOld.withStamps
                    val b2 by hardware.sensorB.readWithEventLoop(st).withStamps
                    val b3 by hardware.sensorB.readEagerly(st).withStamps

                    val b4 by hardware.sensorB.getOld.withoutStamps
                    val b5 by hardware.sensorB.readWithEventLoop(st).withoutStamps
                    val b6 by hardware.sensorB.readEagerly(st).withoutStamps

                    choreography {
                        for (i in 1..5) {
                            countTo(i)
                            checkCount(i, i)

                            a1.y `is equal to?` a4
                            a2.y `is equal to?` a5
                            a3.y `is equal to?` a6

                            b1.y `is equal to?` b4
                            b2.y `is equal to?` b5
                            b3.y `is equal to?` b6
                        }
                    }
                }

                suspend fun badChoreography() = startChoreo(name) {
                    val st = hardware.syncThreshold

                    val a1 by hardware.sensorA.getOld.withStamps
                    val a2 by hardware.sensorA.readWithEventLoop(st).withStamps
                    val a3 by hardware.sensorA.readEagerly(st).withStamps

                    val a4 by hardware.sensorA.getOld.withoutStamps
                    val a5 by hardware.sensorA.readWithEventLoop(st).withoutStamps
                    val a6 by hardware.sensorA.readEagerly(st).withoutStamps

                    val b1 by hardware.sensorB.getOld.withStamps
                    val b2 by hardware.sensorB.readWithEventLoop(st).withStamps
                    val b3 by hardware.sensorB.readEagerly(st).withStamps

                    val b4 by hardware.sensorB.getOld.withoutStamps
                    val b5 by hardware.sensorB.readWithEventLoop(st).withoutStamps
                    val b6 by hardware.sensorB.readEagerly(st).withoutStamps

                    choreography {
                        for (i in 1..5) {
                            countTo(i)
                            checkCount(i, i)

                            a1.y `is equal to?` a4
                            a2.y `is equal to?` a5
                            a3.y `is equal to?` a6

                            b1.y `is equal to?` b4
                            b2.y `is equal to?` b5
                            b3.y `is equal to?` b6
                            if (i == 2) error("This choreography is broken!")
                        }
                    }
                }

                choreography()
                check()

                val j2 = scope.launch {
                    delay(10.milli(Second))
                    choreography()
                }
                while (!j2.isActive) delay(1.milli(Second))
                j2.cancel()
                check()

                badChoreography()
                check()

                scope.launch { badChoreography() }
                check()

                val j5 = scope.launch {
                    delay(10.milli(Second))
                    choreography()
                    choreography()
                    choreography()
                }
                while (!j5.isActive) delay(1.milli(Second))
                j5.cancel()
                check()

                scope.launch {
                    choreography()
                    choreography()
                    badChoreography()
                }.join()
                check()
            }
        }
    }
}