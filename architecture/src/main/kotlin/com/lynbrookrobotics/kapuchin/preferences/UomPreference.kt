package com.lynbrookrobotics.kapuchin.preferences

import com.lynbrookrobotics.kapuchin.control.Quan
import com.lynbrookrobotics.kapuchin.logging.Named
import kotlin.reflect.KProperty0

expect fun <Q : Quan<Q>> Named.pref(fallback: KProperty0<Q>): UomPreference<Q>

class UomPreference<Value>(
        conversion: (Double) -> Value, uomName: String,
        parent: Named, fallback: Double, get: (String, Double) -> Double
) : Preference<Value>(
        parent = parent,
        fallback = conversion(fallback),
        get = { name, _ -> conversion(get(name, fallback)) },
        nameSuffix = " ($uomName)"
)
        where Value : Quan<Value>
