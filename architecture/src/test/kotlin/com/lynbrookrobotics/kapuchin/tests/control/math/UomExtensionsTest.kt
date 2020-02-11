package com.lynbrookrobotics.kapuchin.tests.control.math

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.tests.*
import info.kunalsheth.units.generated.*
import kotlin.test.Test

class UomExtensionsTest {
    @Test
    fun `Finite subsets are subsets`() {
        anyDouble.map { it.Each }.forEach { a ->
            anyDouble.map { it.Each }.forEach { b ->
                anyDouble.map { it.Each }.forEach { c ->
                    anyDouble.map { it.Each }.filter { setOf(a, b, c, it).size == 4 }.forEach { d ->

                        val l = listOf(a, b, c, d).sorted()
                        val first = l[0]
                        val second = l[1]
                        val third = l[2]
                        val fourth = l[3]
                        (first..third).`⊆`(first..fourth) `is equal to?` true
                        (first..fourth).`⊆`(first..fourth) `is equal to?` true
                        (second..fourth).`⊆`(first..fourth) `is equal to?` true
                        (second..third).`⊆`(first..fourth) `is equal to?` true
                        (first..third).`⊆`(second..fourth) `is equal to?` false
                        (second..fourth).`⊆`(first..third) `is equal to?` false
                        (first..fourth).`⊆`(second..third) `is equal to?` false
                    }
                }
            }
        }
    }
}