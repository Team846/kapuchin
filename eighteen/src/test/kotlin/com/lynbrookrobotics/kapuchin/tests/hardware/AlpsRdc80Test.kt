package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.hardware.AlpsRdc80
import info.kunalsheth.units.generated.Degree
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`

class AlpsRdc80Test {

    fun `The Alps RDC80 does xyz`() {
        val a = AlpsRdc80(181.Degree)

        val example = 3.14

        example `is equal to?` 3.14
        a(0.0, Math.random()) `is equal to?` 3.14
    }
}