package me.kosert.youtubeplayer.memory

import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.memory.AbstractPrefs

object AppData : AbstractPrefs<AppData.BooleanType, AppData.IntType, AppData.StringType, AppData.AnyType>() {

    override val keyResourceId = R.string.app_data_key

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
        LOGIN("LOGIN", ""),

    }

    enum class AnyType(
            override val prefKey: String,
            override val default: Any
    ) : IAnyPrefType {

    }
}