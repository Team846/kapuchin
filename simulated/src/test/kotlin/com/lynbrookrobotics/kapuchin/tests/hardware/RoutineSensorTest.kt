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

class RoutineSensorTest {

    private class RoutineSensorTestSH : TSH<RoutineSensorTestSH, RoutineSensorTestC>("RoutineSensorTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
        val sensorC = sensor { Math.random() stampWith currentTime }
    }

    private class RoutineSensorTestC : TC<RoutineSensorTestC, RoutineSensorTestSH>(RoutineSensorTestSH())

    @Test(timeout = 2 * 1000)
    fun `sensors read on tick are in sync`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors read on tick are in sync"
            RoutineSensorTestC().run {
                startRoutine(name) {
                    val a by hardware.sensorA.readOnTick.withStamps
                    val b by hardware.sensorB.readOnTick.withStamps
                    var runs = 10
                    controller {
                        checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                        currentTime `is greater than?` a.x
                        currentTime `is greater than?` b.x
                        name.takeIf { runs-- > 0 }
                    }
                }
            }
        }
    }

    @Test(timeout = 4 * 1000)
    fun `sensors read on event loop are in sync`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors read on tick are in sync"
            RoutineSensorTestC().run {
                startRoutine(name) {
                    val a by hardware.sensorA.readWithEventLoop.withStamps
                    val b by hardware.sensorB.readWithEventLoop.withStamps
                    var lastStamp = currentTime
                    var runs = 10
                    controller {
                        if (runs % 2 == 0) EventLoop.tick(currentTime)

                        checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                        currentTime `is greater than?` a.x
                        currentTime `is greater than?` b.x

                        val thisStamp = avg(a.x, b.x)
                        if (runs % 2 == 1) lastStamp `is equal to?` thisStamp
                        lastStamp = thisStamp

                        name.takeIf { runs-- > 0 }
                    }
                }
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `sensors read eagerly are eager and efficient`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors read eagerly are eager and efficient"
            RoutineSensorTestC().run {
                startRoutine(name) {
                    val a by hardware.sensorA.readEagerly.withStamps
                    val b by hardware.sensorB.readEagerly.withStamps
                    var runs = 10
                    controller {
                        val a1 = a
                        val b1 = b
                        val a2 = a
                        val b2 = b

                        b2 `is equal to?` b1
                        b1.x `is greater than?` a2.x
                        a2 `is equal to?` a1

                        name.takeIf { runs-- > 0 }
                    }
                }
            }
        }
    }

    @Test(timeout = 1 * 1000)
    fun `sensors are updated once before controller initialization`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors are updated once before controller initialization"
            RoutineSensorTestC().run {
                val start = currentTime
                startRoutine(name) {
                    val a by hardware.sensorA.readEagerly.withStamps
                    val b by hardware.sensorB.readOnTick.withStamps
                    val c by hardware.sensorC.readWithEventLoop.withStamps

                    val a1 = a
                    val b1 = b
                    val c1 = c

                    val end = currentTime
                    controller {
                        a1.x `is within?` start..end
                        b1.x `is within?` start..end
                        c1.x `is within?` start..end
                        null
                    }
                }
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `sensors are read efficiently`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensors are read efficiently"
            RoutineSensorTestC().run {
                startRoutine(name) {
                    val a1 by hardware.sensorA.readOnTick.withStamps
                    val a2 by hardware.sensorA.readEagerly.withStamps
                    val b1 by hardware.sensorB.readOnTick.withStamps
                    val b2 by hardware.sensorB.readEagerly.withStamps
                    var runs = 10
                    controller {
                        a1 `is equal to?` a2
                        b1 `is equal to?` b2
                        name.takeIf { runs-- > 0 }
                    }
                }
            }
        }
    }

    @Test(timeout = 4 * 1000)
    fun `sensor lambdas are released upon routine completion`() = threadDumpOnFailiure {
        runBlocking {
            val name = "sensor lambdas are released upon routine completion"
            RoutineSensorTestC().run {

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

                suspend fun routine() = startRoutine(name) {
                    val a1 by hardware.sensorA.readOnTick.withStamps
                    val a2 by hardware.sensorA.readWithEventLoop.withStamps
                    val a3 by hardware.sensorA.readEagerly.withStamps

                    val a4 by hardware.sensorA.readOnTick.withoutStamps
                    val a5 by hardware.sensorA.readWithEventLoop.withoutStamps
                    val a6 by hardware.sensorA.readEagerly.withoutStamps

                    val b1 by hardware.sensorB.readOnTick.withStamps
                    val b2 by hardware.sensorB.readWithEventLoop.withStamps
                    val b3 by hardware.sensorB.readEagerly.withStamps

                    val b4 by hardware.sensorB.readOnTick.withoutStamps
                    val b5 by hardware.sensorB.readWithEventLoop.withoutStamps
                    val b6 by hardware.sensorB.readEagerly.withoutStamps

                    var runs = 5
                    controller {
                        if(runs % 3 == 0) EventLoop.tick(currentTime)

                        a1.y `is equal to?` a4
                        a2.y `is equal to?` a5
                        a3.y `is equal to?` a6

                        b1.y `is equal to?` b4
                        b2.y `is equal to?` b5
                        b3.y `is equal to?` b6
                        name.takeIf { runs-- > 0 }
                    }
                }

                suspend fun badRoutine() = startRoutine(name) {
                    val a1 by hardware.sensorA.readOnTick.withStamps
                    val a2 by hardware.sensorA.readWithEventLoop.withStamps
                    val a3 by hardware.sensorA.readEagerly.withStamps

                    val a4 by hardware.sensorA.readOnTick.withoutStamps
                    val a5 by hardware.sensorA.readWithEventLoop.withoutStamps
                    val a6 by hardware.sensorA.readEagerly.withoutStamps

                    val b1 by hardware.sensorB.readOnTick.withStamps
                    val b2 by hardware.sensorB.readWithEventLoop.withStamps
                    val b3 by hardware.sensorB.readEagerly.withStamps

                    val b4 by hardware.sensorB.readOnTick.withoutStamps
                    val b5 by hardware.sensorB.readWithEventLoop.withoutStamps
                    val b6 by hardware.sensorB.readEagerly.withoutStamps

                    var runs = 5
                    controller {
                        if(runs % 3 == 0) EventLoop.tick(currentTime)

                        a1.y `is equal to?` a4
                        a2.y `is equal to?` a5
                        a3.y `is equal to?` a6

                        b1.y `is equal to?` b4
                        b2.y `is equal to?` b5
                        b3.y `is equal to?` b6
                        if (runs == 2) error("This routine is broken!")
                        name.takeIf { runs-- > 0 }
                    }
                }

                routine()
                check()

                scope.launch { routine() }
                while (routine == null) delay(1.milli(Second))
                routine!!.cancel()
                check()

                val j2 = scope.launch { routine() }
                while (!j2.isActive) delay(1.milli(Second))
                j2.cancel()
                check()

                badRoutine()
                check()

                scope.launch { badRoutine() }
                check()

                val j5 = scope.launch {
                    routine()
                    routine()
                    routine()
                }
                while (routine == null) delay(1.milli(Second))
                j5.cancel()
                check()


                val j6 = scope.launch {
                    routine()
                    routine()
                    routine()
                }
                while (routine == null) delay(1.milli(Second))
                routine!!.cancel()
                j6.join()
                check()
            }
        }
    }
}