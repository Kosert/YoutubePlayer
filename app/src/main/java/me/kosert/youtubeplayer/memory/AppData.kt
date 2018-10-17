package me.kosert.youtubeplayer.memory

import me.kosert.youtubeplayer.R
import me.kosert.youtubeplayer.service.Song

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
        CURRENT_POSITION("CURRENT_POSITION", 0)
    }

    enum class StringType(
            override val prefKey: String,
            override val default: String
    ) : IStringPrefType {
        USER_PLAYLIST_NAME("USER_PLAYLIST_NAME", "Default playlist"),

        SAVED_NAME_1("SAVED_NAME_1", "Playlist 1"),
        SAVED_NAME_2("SAVED_NAME_2", "Playlist 2"),
        SAVED_NAME_3("SAVED_NAME_3", "Playlist 3")
    }

    enum class AnyType(
            override val prefKey: String,
            override val default: Any
    ) : IAnyPrefType {
        USER_PLAYLIST("USER_PLAYLIST", arrayOf<Song>()),

        SAVED_1("SAVED_PLAYLIST1", arrayOf<Song>()),
        SAVED_2("SAVED_PLAYLIST2", arrayOf<Song>()),
        SAVED_3("SAVED_PLAYLIST3", arrayOf<Song>())
    }
}