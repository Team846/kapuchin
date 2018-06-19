package com.lynbrookrobotics.kapuchin.control.loops

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.div

data class Gain<Compensation, Error>(
        val compensation: Compensation,
        val forError: Error
)
        where Compensation : Quan<Compensation>,
              Error : Quan<Error> {

    operator fun times(input: Error) = compensation * (input / forError)
}