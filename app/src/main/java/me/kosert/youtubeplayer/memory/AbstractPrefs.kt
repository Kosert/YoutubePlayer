package me.kosert.youtubeplayer.memory

import android.content.Context
import me.kosert.youtubeplayer.App
import me.kosert.youtubeplayer.GlobalProvider

abstract class AbstractPrefs<B : IBooleanPrefType, I : IIntegerPrefType, S : IStringPrefType, A: IAnyPrefType> {

    protected abstract val keyResourceId: Int

    private val prefs by lazy {
        val key = App.get().getString(keyResourceId)
        App.get().getSharedPreferences(key, Context.MODE_PRIVATE)
    }

    fun getBoolean(pref: B): Boolean {
        return prefs.getBoolean(pref.prefKey, pref.default)
    }

    fun getInt(pref: I): Int {
        return prefs.getInt(pref.prefKey, pref.default)
    }

    fun getString(pref: S): String {
        return prefs.getString(pref.prefKey, pref.default)
    }

    fun getAny(pref: A): Any {
        val json = prefs.getString(pref.prefKey, null)
        return if (json == null)
            pref.default
        else
            GlobalProvider.gson.fromJson(json, pref.default::class.java)
    }

    fun setBoolean(pref: B, value: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(pref.prefKey, value)
        editor.apply()
    }

    fun setInt(pref: I, value: Int) {
        val editor = prefs.edit()
        editor.putInt(pref.prefKey, value)
        editor.apply()
    }

    fun setString(pref: S, value: String) {
        val editor = prefs.edit()
        editor.putString(pref.prefKey, value)
        editor.apply()
    }

    fun setAny(pref: A, value: Any) {
        val json = GlobalProvider.gson.toJson(value)
        val editor = prefs.edit()
        editor.putString(pref.prefKey, json)
        editor.apply()
    }
}