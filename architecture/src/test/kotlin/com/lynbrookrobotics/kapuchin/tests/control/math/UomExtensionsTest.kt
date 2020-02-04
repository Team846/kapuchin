package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class UomExtensionsTest {
    @Test
    fun `Finite subsets are subsets`() {
        anyDouble.map { it.Foot }.forEach { a ->
            anyDouble.map { it.Foot }.forEach { b ->
                anyDouble.map { it.Foot }.forEach { c ->
                    anyDouble.map { it.Foot }.forEach { d ->
                        val l = listOf(a, b, c, d).sorted()
                        b..c `⊆` a..d `is equal to?` true
                        a..d `⊆` a..d `is equal to?` true
                        a..c `⊆` b..d `is equal to?` false
                        b..d `⊆` a..c `is equal to?` false
                        a..d `⊆` b..c `is equal to?` false

                    }
                }
            }
        }
    }
}