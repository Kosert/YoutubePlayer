package me.kosert.youtubeplayer.service

class ControlEvent(
        val type: OperationType,
        val index: Int = -1
)

enum class OperationType {
    PLAY,
    PAUSE,
    STOP,
    NEXT,
    SELECTED,
    PLAYLIST_SWAP
}