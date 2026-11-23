package com.example.game.uno

enum class UnoColor(val code: String, val label: String) {
    RED("RED", "红"),
    YELLOW("YELLOW", "黄"),
    GREEN("GREEN", "绿"),
    BLUE("BLUE", "蓝"),
    WILD("WILD", "万能");

    companion object {
        val PLAYABLE_COLORS = listOf(RED, YELLOW, GREEN, BLUE)
        fun fromCode(code: String?): UnoColor? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

enum class UnoValue(val code: String, val symbol: String, val label: String, val points: Int) {
    ZERO("0", "0", "0", 0),
    ONE("1", "1", "1", 1),
    TWO("2", "2", "2", 2),
    THREE("3", "3", "3", 3),
    FOUR("4", "4", "4", 4),
    FIVE("5", "5", "5", 5),
    SIX("6", "6", "6", 6),
    SEVEN("7", "7", "7", 7),
    EIGHT("8", "8", "8", 8),
    NINE("9", "9", "9", 9),
    SKIP("SKIP", "⊘", "跳过", 20),
    REVERSE("REVERSE", "⇄", "反转", 20),
    DRAW_TWO("DRAW_TWO", "+2", "+2", 20),
    WILD("WILD", "✦", "变色", 50),
    WILD_DRAW_FOUR("WILD_FOUR", "+4", "万能+4", 50);

    val isWild: Boolean get() = this == WILD || this == WILD_DRAW_FOUR
}

data class UnoCard(
    val id: String,
    val color: UnoColor,
    val value: UnoValue
) {
    fun displayName(): String = if (value.isWild) value.label else "${color.label}${value.label}"
}

data class UnoState(
    val seatOrder: List<Int>,
    val hands: Map<Int, List<UnoCard>>,
    val drawPile: List<UnoCard>,
    val discardPile: List<UnoCard>,
    val activeColor: UnoColor,
    val currentTurnSeat: Int,
    val direction: Int, // 1 = clockwise, -1 = counter-clockwise
    val unoCalledSeats: Set<Int> = emptySet(),
    val hasDrawnThisTurn: Boolean = false,
    val drawnCardId: String? = null,
    val winnerSeat: Int? = null,
    val lastRoundPoints: Int = 0,
    val scores: Map<Int, Int> = emptyMap(),
    val roundNumber: Int = 1,
    val lastEvent: String = "游戏开始！"
) {
    val topCard: UnoCard get() = discardPile.last()
    val isFinished: Boolean get() = winnerSeat != null
}

sealed class UnoMoveResult {
    data class Success(val state: UnoState) : UnoMoveResult()
    data class Failure(val message: String) : UnoMoveResult()
}

sealed class UnoBotDecision {
    data class Play(val cardId: String, val chosenColor: UnoColor?) : UnoBotDecision()
    data object Draw : UnoBotDecision()
    data object Pass : UnoBotDecision()
}
