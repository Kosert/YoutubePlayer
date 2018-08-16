package me.kosert.youtubeplayer.service

class ControlEvent(
        val type: OperationType
)

enum class OperationType {
    PLAY,
    PAUSE
}