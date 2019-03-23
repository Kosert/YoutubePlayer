package me.kosert.youtubeplayer.util

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@JvmOverloads
fun timerJob(
        period: Long = 1000L, coroutineContext: CoroutineContext = Dispatchers.Default,
        onTick: suspend (Int) -> Unit
): Job {
    return CoroutineScope(Job() + Dispatchers.Default).launch {

        var counter = 0

        while (isActive) {
            delay(period)
            counter++
            withContext(coroutineContext) { onTick(counter) }
        }
    }
}