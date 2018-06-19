package com.lynbrookrobotics.kapuchin.control.loops

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.div

data class Gain<Input, Output>(
        val compensation: Output,
        val forError: Input
)
        where Output : Quan<Output>,
              Input : Quan<Input> {

    operator fun times(input: Input) = compensation * (input / forError)
}