package com.lynbrookrobotics.kapuchin.delegates

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface DelegateProvider<in ThisRef, out Value> {
    operator fun provideDelegate(
        thisRef: ThisRef,
        prop: KProperty<*>
    ): ReadOnlyProperty<ThisRef, Value>
}