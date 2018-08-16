package me.kosert.youtubeplayer.memory

import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.memory.AbstractPrefs

object Prefs : AbstractPrefs<Prefs.BooleanType, Prefs.IntType, Prefs.StringType, Prefs.AnyType>() {

    override val keyResourceId = R.string.user_prefs_key

    enum class BooleanType(
            override val prefKey: String,
            override val default: Boolean
    ) : IBooleanPrefType {

    }

    enum class IntType(
            override val prefKey: String,
            override val default: Int
    ) : IIntegerPrefType {

    }

    enum class StringType(
            override val prefKey: String,
            override val default: String
    ) : IStringPrefType {

    }

    enum class AnyType(
            override val prefKey: String,
            override val default: Any
    ) : IAnyPrefType {

    }
}