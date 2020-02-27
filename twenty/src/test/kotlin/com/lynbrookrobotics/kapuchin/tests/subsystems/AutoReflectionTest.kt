package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

typealias Block = CoroutineScope.() -> Unit

class AutoReflectionTest : Named by Named("Auto Reflection") {

    fun choreography(block: Block) = block

    fun f1() = println("f1")
    suspend fun f2() {
        print("f")
        coroutineScope { delay(10) }
        println(2)
    }

    val funs = listOf(::f1, ::f2)

    @Test
    fun `calling kfunction works`() {
        funs[0].call()
    }

    @Test
    fun `calling ksuspendfunction works`() = runBlocking {
        coroutineScope {
            funs[1].call(this)
        }
    }
}