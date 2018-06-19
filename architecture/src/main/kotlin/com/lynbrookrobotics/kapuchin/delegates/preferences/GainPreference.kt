package com.lynbrookrobotics.kapuchin.delegates.preferences

import com.lynbrookrobotics.kapuchin.Quan
import com.lynbrookrobotics.kapuchin.control.loops.Gain
import com.lynbrookrobotics.kapuchin.delegates.DelegateProvider
import com.lynbrookrobotics.kapuchin.delegates.WithEventLoop
import com.lynbrookrobotics.kapuchin.subsystems.Named
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class GainPreference<Compensation, Error>(
        private val fallbackComp: Double, private val compUomName: String, private val compConversion: (Double) -> Compensation,
        private val fallbackError: Double, private val errorUomName: String, private val errorConversion: (Double) -> Error,
        private val get: (String, Double) -> Double
) : WithEventLoop, DelegateProvider<Named, Gain<Compensation, Error>>
        where Compensation : Quan<Compensation>,
              Error : Quan<Error> {

    private lateinit var errorName: String
    private lateinit var compName: String

    private var value: Gain<Compensation, Error>? = null

    override fun update() {
        if (this::errorName.isInitialized && this::compName.isInitialized) {
            value = Gain(
                    compConversion(get(compName, fallbackComp)),
                    errorConversion(get(errorName, fallbackError))
            )
        }
    }

    override fun provideDelegate(thisRef: Named, prop: KProperty<*>): ReadOnlyProperty<Named, Gain<Compensation, Error>> {
        val baseName = namePreference(thisRef, prop)
        errorName = "$baseName (error, $errorUomName)"
        compName = "$baseName (compensation, $compUomName)"

        return object : ReadOnlyProperty<Named, Gain<Compensation, Error>> {
            override fun getValue(thisRef: Named, property: KProperty<*>) = value ?: update().let { value!! }
        }
    }
}