package me.kosert.youtubeplayer.util

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import me.kosert.youtubeplayer.App

@ColorInt
fun Int.toColor(ctx: Context = App.get()) = ContextCompat.getColor(ctx, this)