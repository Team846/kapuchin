package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.hardware.AlpsRdc80
import com.lynbrookrobotics.kapuchin.tests.*
import com.lynbrookrobotics.kapuchin.tests.anyDouble
import info.kunalsheth.units.generated.Degree
import kotlin.test.Test

class AlpsRdc80Test {

    // to run your tests:
    // ./gradlew check


    // occasionally, tests from other packages will not pass
    // I need to fix this, but for now, just run `gradle check` again and it should go away

    @Test
    fun `The Alps RDC80 does xyz`() { // you can write as many test methods as you want in this class
        val a = AlpsRdc80(181.Degree)

        val example = 3.14

        example `is equal to?` 3.14
        // a(0.0, Math.random()) `is equal to?` 3.14.Degree // fails
        a(8.4, 6.0) `is within?` -8.46.Degree..-7.5.Degree

        anyDouble.forEach {
            it * 0 `is equal to?` 0.0
        }

        anyInt.forEach {
            it * 1 `is equal to?` it / 1
        }

        Math.PI `is greater than?` Math.E
        Math.PI `is greater than or equal to?` Math.E

        assert(1 + 1 == 2) { "One plus one equals two!!" }
    }
}