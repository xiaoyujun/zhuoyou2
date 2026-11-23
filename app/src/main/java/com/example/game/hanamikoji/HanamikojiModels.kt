package com.example.game.hanamikoji

enum class Geisha(
    val id: Int,
    val title: String,
    val itemName: String,
    val badgeChar: String,
    val charm: Int,
    val accentHex: String
) {
    PLUM(0, "红梅", "折扇", "扇", 2, "#E05263"),
    WISTERIA(1, "紫藤", "竹笛", "笛", 2, "#9B72CF"),
    BAMBOO(2, "翠竹", "诗卷", "卷", 2, "#38A37F"),
    CHRYSANTHEMUM(3, "金菊", "油纸伞", "伞", 3, "#E9A825"),
    SAKURA(4, "樱雪", "三味线", "琴", 3, "#F2849E"),
    MAGNOLIA(5, "玉兰", "琵琶", "琶", 4, "#4D9DE0"),
    CRIMSON_MOON(6, "绯月", "华服", "服", 5, "#D62839");

    companion object {
        fun byId(id: Int): Geisha = entries.first { it.id == id }
    }
}

data class HanaCard(
    val id: String,
    val geishaId: Int
) {
    val geisha: Geisha get() = Geisha.byId(geishaId)
}

enum class HanaActionType(
    val code: String,
    val title: String,
    val shortRule: String,
    val requiredCards: Int
) {
    SECRET("SECRET", "秘密", "选 1 张暗置保留，回合结束时结算", 1),
    DISCARD("DISCARD", "弃置", "选 2 张暗置移除，本轮不再参与结算", 2),
    GIFT("GIFT", "赠礼", "选 3 张展示，对手挑 1 张，其余 2 张归你", 3),
    COMPETITION("COMPETITION", "竞争", "选 4 张分两组(各2张)，对手选 1 组，另 1 组归你", 4);

    companion object {
        fun fromCode(code: String?): HanaActionType? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}

enum class HanaPhase(val code: String) {
    CHOOSE_ACTION("CHOOSE_ACTION"),
    WAITING_OPPONENT_CHOICE("WAITING_OPPONENT_CHOICE"),
    ROUND_SUMMARY("ROUND_SUMMARY"),
    GAME_OVER("GAME_OVER")
}

sealed class HanaPendingOffer {
    abstract val actorSeat: Int
    abstract val chooserSeat: Int

    data class GiftOffer(
        override val actorSeat: Int,
        override val chooserSeat: Int,
        val cards: List<HanaCard>
    ) : HanaPendingOffer()

    data class CompetitionOffer(
        override val actorSeat: Int,
        override val chooserSeat: Int,
        val groupA: List<HanaCard>,
        val groupB: List<HanaCard>
    ) : HanaPendingOffer()
}

data class HanaRoundReveal(
    val roundNumber: Int,
    val secretCards: Map<Int, HanaCard?>,
    val removedCard: HanaCard?,
    val geishaCountsP0: Int,
    val geishaCountsP1: Int,
    val charmPointsP0: Int,
    val charmPointsP1: Int
)

data class HanamikojiState(
    val roundNumber: Int = 1,
    val firstPlayerSeat: Int = 0,
    val currentTurnSeat: Int = 0,
    val phase: HanaPhase = HanaPhase.CHOOSE_ACTION,
    val deck: List<HanaCard> = emptyList(),
    val removedCard: HanaCard? = null,
    val hands: Map<Int, List<HanaCard>> = emptyMap(),
    val playedCards: Map<Int, Map<Int, List<HanaCard>>> = emptyMap(),
    val secretCards: Map<Int, HanaCard?> = emptyMap(),
    val discardedCards: Map<Int, List<HanaCard>> = emptyMap(),
    val usedActions: Map<Int, Set<HanaActionType>> = emptyMap(),
    val geishaFavor: List<Int> = List(7) { -1 }, // -1 neutral, 0 = P1, 1 = P2
    val pendingOffer: HanaPendingOffer? = null,
    val lastRoundReveal: HanaRoundReveal? = null,
    val winnerSeat: Int? = null,
    val winReason: String = "",
    val lastEvent: String = "花间小路对局开始！"
) {
    fun geishaCountFor(seat: Int): Int = geishaFavor.count { it == seat }
    fun charmPointsFor(seat: Int): Int = Geisha.entries.sumOf { g ->
        if (geishaFavor[g.id] == seat) g.charm else 0
    }
}

sealed class HanaMoveResult {
    data class Success(val state: HanamikojiState) : HanaMoveResult()
    data class Failure(val message: String) : HanaMoveResult()
}

sealed class HanaBotDecision {
    data class Secret(val cardId: String) : HanaBotDecision()
    data class Discard(val cardIds: List<String>) : HanaBotDecision()
    data class Gift(val cardIds: List<String>) : HanaBotDecision()
    data class Competition(val groupA: List<String>, val groupB: List<String>) : HanaBotDecision()
    data class ResolveGift(val chosenCardId: String) : HanaBotDecision()
    data class ResolveCompetition(val chosenGroup: Int) : HanaBotDecision()
}
