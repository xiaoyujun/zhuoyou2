package com.example.model

enum class GameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val durationLabel: String
) {
    UNO(
        id = "uno",
        title = "UNO 经典纸牌",
        subtitle = "2–4人 · 策略、反转与大逆转",
        minPlayers = 2,
        maxPlayers = 4,
        durationLabel = "10–15 分钟"
    ),
    HANAMIKOJI(
        id = "hanamikoji",
        title = "花间小路",
        subtitle = "2人对决 · 七位艺伎与心理博弈",
        minPlayers = 2,
        maxPlayers = 2,
        durationLabel = "15 分钟"
    )
}

enum class PlayerType {
    HOST,
    WEB_CLIENT,
    BOT
}

data class PlayerSeat(
    val seatIndex: Int, // 0 = P1, 1 = P2, 2 = P3, 3 = P4
    val id: String,
    val name: String,
    val type: PlayerType,
    val token: String? = null,
    val lastSeenMs: Long = System.currentTimeMillis()
) {
    val seatTag: String get() = "P${seatIndex + 1}"
}

enum class RoomPhase {
    LOBBY,
    PLAYING
}

data class NetworkAddressOption(
    val interfaceName: String,
    val ipAddress: String,
    val displayLabel: String
)

enum class AppScreen {
    HALL,
    RULES,
    LOBBY,
    GAME_UNO,
    GAME_HANAMIKOJI
}
