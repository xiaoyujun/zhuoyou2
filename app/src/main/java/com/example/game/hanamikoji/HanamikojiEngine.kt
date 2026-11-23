package com.example.game.hanamikoji

import com.example.model.PlayerSeat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

object HanamikojiEngine {

    fun createDeck(random: Random = Random.Default): List<HanaCard> {
        val list = mutableListOf<HanaCard>()
        var seq = 0
        for (geisha in Geisha.entries) {
            repeat(geisha.charm) {
                list += HanaCard(id = "h_${seq++}", geishaId = geisha.id)
            }
        }
        return list.shuffled(random)
    }

    fun newMatch(
        firstPlayerSeat: Int = 0,
        seatNames: Map<Int, String> = emptyMap(),
        random: Random = Random.Default
    ): HanamikojiState {
        return dealRound(
            roundNumber = 1,
            firstPlayerSeat = firstPlayerSeat,
            geishaFavor = List(7) { -1 },
            seatNames = seatNames,
            random = random
        )
    }

    fun startNextRound(
        state: HanamikojiState,
        seatNames: Map<Int, String> = emptyMap(),
        random: Random = Random.Default
    ): HanaMoveResult {
        if (state.phase != HanaPhase.ROUND_SUMMARY) {
            return HanaMoveResult.Failure("当前不在回合结算阶段")
        }
        val nextStarter = 1 - state.firstPlayerSeat
        return HanaMoveResult.Success(
            dealRound(
                roundNumber = state.roundNumber + 1,
                firstPlayerSeat = nextStarter,
                geishaFavor = state.geishaFavor,
                seatNames = seatNames,
                random = random
            )
        )
    }

    private fun dealRound(
        roundNumber: Int,
        firstPlayerSeat: Int,
        geishaFavor: List<Int>,
        seatNames: Map<Int, String>,
        random: Random
    ): HanamikojiState {
        val deck = createDeck(random).toMutableList()
        val removed = deck.removeAt(0)
        val p0Hand = MutableList(6) { deck.removeAt(0) }
        val p1Hand = MutableList(6) { deck.removeAt(0) }

        // First player draws 1 card immediately to start Turn 1
        val firstDraw = deck.removeAt(0)
        if (firstPlayerSeat == 0) p0Hand.add(firstDraw) else p1Hand.add(firstDraw)

        val emptyBoard = Geisha.entries.associate { it.id to emptyList<HanaCard>() }
        val starterName = seatNames[firstPlayerSeat] ?: "P${firstPlayerSeat + 1}"

        return HanamikojiState(
            roundNumber = roundNumber,
            firstPlayerSeat = firstPlayerSeat,
            currentTurnSeat = firstPlayerSeat,
            phase = HanaPhase.CHOOSE_ACTION,
            deck = deck,
            removedCard = removed,
            hands = mapOf(
                0 to sortHand(p0Hand),
                1 to sortHand(p1Hand)
            ),
            playedCards = mapOf(0 to emptyBoard, 1 to emptyBoard),
            secretCards = mapOf(0 to null, 1 to null),
            discardedCards = mapOf(0 to emptyList(), 1 to emptyList()),
            usedActions = mapOf(0 to emptySet(), 1 to emptySet()),
            geishaFavor = geishaFavor,
            pendingOffer = null,
            lastRoundReveal = null,
            winnerSeat = null,
            winReason = "",
            lastEvent = "第 $roundNumber 轮开始！已暗置移除 1 张物品牌，轮到 $starterName 行动。"
        )
    }

    fun sortHand(cards: List<HanaCard>): List<HanaCard> =
        cards.sortedWith(compareBy<HanaCard> { it.geishaId }.thenBy { it.id })

    fun performSecret(
        state: HanamikojiState,
        seat: Int,
        cardId: String,
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        val validation = validateActionStart(state, seat, HanaActionType.SECRET)
        if (validation != null) return HanaMoveResult.Failure(validation)

        val hand = state.hands[seat].orEmpty()
        val card = hand.firstOrNull { it.id == cardId }
            ?: return HanaMoveResult.Failure("手牌中找不到该物品牌")

        val newHands = state.hands.toMutableMap()
        newHands[seat] = hand.filterNot { it.id == cardId }
        val newSecrets = state.secretCards.toMutableMap()
        newSecrets[seat] = card
        val newUsed = markActionUsed(state.usedActions, seat, HanaActionType.SECRET)
        val actorName = seatNames[seat] ?: "P${seat + 1}"

        return HanaMoveResult.Success(
            advanceAfterActionResolved(
                state.copy(
                    hands = newHands,
                    secretCards = newSecrets,
                    usedActions = newUsed
                ),
                eventMessage = "$actorName 使用了【秘密】，暗置保留 1 张牌。",
                seatNames = seatNames
            )
        )
    }

    fun performDiscard(
        state: HanamikojiState,
        seat: Int,
        cardIds: List<String>,
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        val validation = validateActionStart(state, seat, HanaActionType.DISCARD)
        if (validation != null) return HanaMoveResult.Failure(validation)

        val uniqueIds = cardIds.distinct()
        if (uniqueIds.size != 2) return HanaMoveResult.Failure("【弃置】必须选择 2 张不同的卡牌")

        val hand = state.hands[seat].orEmpty()
        val chosen = uniqueIds.mapNotNull { id -> hand.firstOrNull { it.id == id } }
        if (chosen.size != 2) return HanaMoveResult.Failure("所选卡牌不在手牌中")

        val newHands = state.hands.toMutableMap()
        newHands[seat] = hand.filterNot { it.id in uniqueIds }
        val newDiscards = state.discardedCards.toMutableMap()
        newDiscards[seat] = chosen
        val newUsed = markActionUsed(state.usedActions, seat, HanaActionType.DISCARD)
        val actorName = seatNames[seat] ?: "P${seat + 1}"

        return HanaMoveResult.Success(
            advanceAfterActionResolved(
                state.copy(
                    hands = newHands,
                    discardedCards = newDiscards,
                    usedActions = newUsed
                ),
                eventMessage = "$actorName 使用了【弃置】，暗置移除 2 张牌。",
                seatNames = seatNames
            )
        )
    }

    fun performGift(
        state: HanamikojiState,
        seat: Int,
        cardIds: List<String>,
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        val validation = validateActionStart(state, seat, HanaActionType.GIFT)
        if (validation != null) return HanaMoveResult.Failure(validation)

        val uniqueIds = cardIds.distinct()
        if (uniqueIds.size != 3) return HanaMoveResult.Failure("【赠礼】必须选择 3 张不同的卡牌")

        val hand = state.hands[seat].orEmpty()
        val chosen = uniqueIds.mapNotNull { id -> hand.firstOrNull { it.id == id } }
        if (chosen.size != 3) return HanaMoveResult.Failure("所选卡牌不在手牌中")

        val newHands = state.hands.toMutableMap()
        newHands[seat] = hand.filterNot { it.id in uniqueIds }
        val newUsed = markActionUsed(state.usedActions, seat, HanaActionType.GIFT)
        val opponent = 1 - seat
        val actorName = seatNames[seat] ?: "P${seat + 1}"
        val oppName = seatNames[opponent] ?: "P${opponent + 1}"

        return HanaMoveResult.Success(
            state.copy(
                hands = newHands,
                usedActions = newUsed,
                phase = HanaPhase.WAITING_OPPONENT_CHOICE,
                pendingOffer = HanaPendingOffer.GiftOffer(
                    actorSeat = seat,
                    chooserSeat = opponent,
                    cards = sortHand(chosen)
                ),
                lastEvent = "$actorName 发起【赠礼】展示了 3 张牌，等待 $oppName 挑选 1 张。"
            )
        )
    }

    fun resolveGift(
        state: HanamikojiState,
        chooserSeat: Int,
        chosenCardId: String,
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        if (state.phase != HanaPhase.WAITING_OPPONENT_CHOICE) {
            return HanaMoveResult.Failure("当前无需回应赠礼")
        }
        val offer = state.pendingOffer as? HanaPendingOffer.GiftOffer
            ?: return HanaMoveResult.Failure("当前不是赠礼阶段")
        if (offer.chooserSeat != chooserSeat) {
            return HanaMoveResult.Failure("等待对手做出选择")
        }

        val picked = offer.cards.firstOrNull { it.id == chosenCardId }
            ?: return HanaMoveResult.Failure("请选择展示中的卡牌")
        val remaining = offer.cards.filterNot { it.id == chosenCardId }

        var board = addCardsToBoard(state.playedCards, chooserSeat, listOf(picked))
        board = addCardsToBoard(board, offer.actorSeat, remaining)

        val chooserName = seatNames[chooserSeat] ?: "P${chooserSeat + 1}"
        val actorName = seatNames[offer.actorSeat] ?: "P${offer.actorSeat + 1}"

        return HanaMoveResult.Success(
            advanceAfterActionResolved(
                state.copy(
                    playedCards = board,
                    pendingOffer = null
                ),
                eventMessage = "$chooserName 收下了【${picked.geisha.title}】，其余 2 张归 $actorName。",
                seatNames = seatNames
            )
        )
    }

    fun performCompetition(
        state: HanamikojiState,
        seat: Int,
        groupAIds: List<String>,
        groupBIds: List<String>,
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        val validation = validateActionStart(state, seat, HanaActionType.COMPETITION)
        if (validation != null) return HanaMoveResult.Failure(validation)

        val aUnique = groupAIds.distinct()
        val bUnique = groupBIds.distinct()
        if (aUnique.size != 2 || bUnique.size != 2 || (aUnique + bUnique).distinct().size != 4) {
            return HanaMoveResult.Failure("【竞争】必须选择 4 张不同的牌并平分为两组(各 2 张)")
        }

        val hand = state.hands[seat].orEmpty()
        val groupA = aUnique.mapNotNull { id -> hand.firstOrNull { it.id == id } }
        val groupB = bUnique.mapNotNull { id -> hand.firstOrNull { it.id == id } }
        if (groupA.size != 2 || groupB.size != 2) {
            return HanaMoveResult.Failure("所选卡牌不在手牌中")
        }

        val allIds = (aUnique + bUnique).toSet()
        val newHands = state.hands.toMutableMap()
        newHands[seat] = hand.filterNot { it.id in allIds }
        val newUsed = markActionUsed(state.usedActions, seat, HanaActionType.COMPETITION)
        val opponent = 1 - seat
        val actorName = seatNames[seat] ?: "P${seat + 1}"
        val oppName = seatNames[opponent] ?: "P${opponent + 1}"

        return HanaMoveResult.Success(
            state.copy(
                hands = newHands,
                usedActions = newUsed,
                phase = HanaPhase.WAITING_OPPONENT_CHOICE,
                pendingOffer = HanaPendingOffer.CompetitionOffer(
                    actorSeat = seat,
                    chooserSeat = opponent,
                    groupA = sortHand(groupA),
                    groupB = sortHand(groupB)
                ),
                lastEvent = "$actorName 发起【竞争】将 4 张牌分为 A/B 两组，等待 $oppName 选择。"
            )
        )
    }

    fun resolveCompetition(
        state: HanamikojiState,
        chooserSeat: Int,
        chosenGroup: Int, // 0 = Group A, 1 = Group B
        seatNames: Map<Int, String> = emptyMap()
    ): HanaMoveResult {
        if (state.phase != HanaPhase.WAITING_OPPONENT_CHOICE) {
            return HanaMoveResult.Failure("当前无需回应竞争")
        }
        val offer = state.pendingOffer as? HanaPendingOffer.CompetitionOffer
            ?: return HanaMoveResult.Failure("当前不是竞争阶段")
        if (offer.chooserSeat != chooserSeat) {
            return HanaMoveResult.Failure("等待对手做出选择")
        }
        if (chosenGroup !in 0..1) {
            return HanaMoveResult.Failure("请选择 A 组或 B 组")
        }

        val chooserCards = if (chosenGroup == 0) offer.groupA else offer.groupB
        val actorCards = if (chosenGroup == 0) offer.groupB else offer.groupA

        var board = addCardsToBoard(state.playedCards, chooserSeat, chooserCards)
        board = addCardsToBoard(board, offer.actorSeat, actorCards)

        val chooserName = seatNames[chooserSeat] ?: "P${chooserSeat + 1}"
        val actorName = seatNames[offer.actorSeat] ?: "P${offer.actorSeat + 1}"
        val groupLabel = if (chosenGroup == 0) "A 组" else "B 组"

        return HanaMoveResult.Success(
            advanceAfterActionResolved(
                state.copy(
                    playedCards = board,
                    pendingOffer = null
                ),
                eventMessage = "$chooserName 选择了 $groupLabel，另一组归 $actorName。",
                seatNames = seatNames
            )
        )
    }

    private fun validateActionStart(
        state: HanamikojiState,
        seat: Int,
        action: HanaActionType
    ): String? {
        if (state.phase != HanaPhase.CHOOSE_ACTION) return "当前阶段无法执行此行动"
        if (state.currentTurnSeat != seat) return "还没轮到你的回合"
        if (action in state.usedActions[seat].orEmpty()) return "本轮已使用过【${action.title}】"
        val handSize = state.hands[seat]?.size ?: 0
        if (handSize < action.requiredCards) return "手牌数量不足以执行【${action.title}】"
        return null
    }

    private fun markActionUsed(
        current: Map<Int, Set<HanaActionType>>,
        seat: Int,
        action: HanaActionType
    ): Map<Int, Set<HanaActionType>> {
        val map = current.toMutableMap()
        map[seat] = (map[seat] ?: emptySet()) + action
        return map
    }

    private fun addCardsToBoard(
        board: Map<Int, Map<Int, List<HanaCard>>>,
        seat: Int,
        cards: List<HanaCard>
    ): Map<Int, Map<Int, List<HanaCard>>> {
        val outer = board.toMutableMap()
        val inner = (outer[seat] ?: emptyMap()).toMutableMap()
        for (card in cards) {
            inner[card.geishaId] = (inner[card.geishaId] ?: emptyList()) + card
        }
        outer[seat] = inner
        return outer
    }

    private fun advanceAfterActionResolved(
        state: HanamikojiState,
        eventMessage: String,
        seatNames: Map<Int, String>
    ): HanamikojiState {
        val p0Done = (state.usedActions[0]?.size ?: 0) >= 4
        val p1Done = (state.usedActions[1]?.size ?: 0) >= 4
        if (p0Done && p1Done) {
            return resolveRoundEnd(state, seatNames)
        }

        val nextSeat = 1 - state.currentTurnSeat
        val remainingDeck = state.deck.toMutableList()
        val newHands = state.hands.toMutableMap()
        if (remainingDeck.isNotEmpty()) {
            val drawn = remainingDeck.removeAt(0)
            newHands[nextSeat] = sortHand((newHands[nextSeat] ?: emptyList()) + drawn)
        }

        val nextName = seatNames[nextSeat] ?: "P${nextSeat + 1}"
        return state.copy(
            currentTurnSeat = nextSeat,
            phase = HanaPhase.CHOOSE_ACTION,
            deck = remainingDeck,
            hands = newHands,
            pendingOffer = null,
            lastEvent = "$eventMessage 轮到 $nextName 行动。"
        )
    }

    private fun resolveRoundEnd(
        state: HanamikojiState,
        seatNames: Map<Int, String>
    ): HanamikojiState {
        var board = state.playedCards
        val secret0 = state.secretCards[0]
        val secret1 = state.secretCards[1]
        if (secret0 != null) board = addCardsToBoard(board, 0, listOf(secret0))
        if (secret1 != null) board = addCardsToBoard(board, 1, listOf(secret1))

        val newFavor = state.geishaFavor.toMutableList()
        for (geisha in Geisha.entries) {
            val c0 = board[0]?.get(geisha.id)?.size ?: 0
            val c1 = board[1]?.get(geisha.id)?.size ?: 0
            if (c0 > c1) newFavor[geisha.id] = 0
            else if (c1 > c0) newFavor[geisha.id] = 1
            // ties keep existing favor marker!
        }

        val g0 = newFavor.count { it == 0 }
        val g1 = newFavor.count { it == 1 }
        val pts0 = Geisha.entries.sumOf { if (newFavor[it.id] == 0) it.charm else 0 }
        val pts1 = Geisha.entries.sumOf { if (newFavor[it.id] == 1) it.charm else 0 }

        val reveal = HanaRoundReveal(
            roundNumber = state.roundNumber,
            secretCards = mapOf(0 to secret0, 1 to secret1),
            removedCard = state.removedCard,
            geishaCountsP0 = g0,
            geishaCountsP1 = g1,
            charmPointsP0 = pts0,
            charmPointsP1 = pts1
        )

        val p0Name = seatNames[0] ?: "P1"
        val p1Name = seatNames[1] ?: "P2"

        // Official Hanamikoji victory check: >= 11 charm points takes priority over >= 4 geishas
        val (winner, reason) = when {
            pts0 >= 11 -> 0 to "$p0Name 赢得 $pts0 点魅力值（≥11分）获得胜利！"
            pts1 >= 11 -> 1 to "$p1Name 赢得 $pts1 点魅力值（≥11分）获得胜利！"
            g0 >= 4 -> 0 to "$p0Name 赢得 $g0 位艺伎青睐（≥4位）获得胜利！"
            g1 >= 4 -> 1 to "$p1Name 赢得 $g1 位艺伎青睐（≥4位）获得胜利！"
            else -> null to "双方均未达到获胜条件（4位艺伎或11点魅力），保留好感标记进入下一轮！"
        }

        return state.copy(
            playedCards = board,
            geishaFavor = newFavor,
            phase = if (winner != null) HanaPhase.GAME_OVER else HanaPhase.ROUND_SUMMARY,
            pendingOffer = null,
            lastRoundReveal = reveal,
            winnerSeat = winner,
            winReason = reason,
            lastEvent = "第 ${state.roundNumber} 轮结算完成：$reason"
        )
    }

    fun computeBotAction(state: HanamikojiState, botSeat: Int): HanaBotDecision? {
        if (state.phase == HanaPhase.WAITING_OPPONENT_CHOICE) {
            return when (val offer = state.pendingOffer) {
                is HanaPendingOffer.GiftOffer -> {
                    if (offer.chooserSeat != botSeat) return null
                    val best = offer.cards.maxByOrNull { cardValueScore(state, botSeat, it) } ?: offer.cards.first()
                    HanaBotDecision.ResolveGift(best.id)
                }
                is HanaPendingOffer.CompetitionOffer -> {
                    if (offer.chooserSeat != botSeat) return null
                    val scoreA = offer.groupA.sumOf { cardValueScore(state, botSeat, it) }
                    val scoreB = offer.groupB.sumOf { cardValueScore(state, botSeat, it) }
                    HanaBotDecision.ResolveCompetition(if (scoreA >= scoreB) 0 else 1)
                }
                null -> null
            }
        }

        if (state.phase != HanaPhase.CHOOSE_ACTION || state.currentTurnSeat != botSeat) return null
        val hand = state.hands[botSeat].orEmpty()
        val remaining = HanaActionType.entries.filter { it !in state.usedActions[botSeat].orEmpty() }
        if (remaining.isEmpty()) return null

        val scoredHand = hand.sortedByDescending { cardValueScore(state, botSeat, it) }

        // Prefer early SECRET / DISCARD when hand is full, then COMPETITION / GIFT
        val chosenAction = when {
            HanaActionType.SECRET in remaining && hand.size >= 4 -> HanaActionType.SECRET
            HanaActionType.DISCARD in remaining && hand.size >= 5 -> HanaActionType.DISCARD
            HanaActionType.COMPETITION in remaining && hand.size >= 4 -> HanaActionType.COMPETITION
            HanaActionType.GIFT in remaining && hand.size >= 3 -> HanaActionType.GIFT
            else -> remaining.firstOrNull { hand.size >= it.requiredCards } ?: return null
        }

        return when (chosenAction) {
            HanaActionType.SECRET -> HanaBotDecision.Secret(scoredHand.first().id)
            HanaActionType.DISCARD -> HanaBotDecision.Discard(scoredHand.takeLast(2).map { it.id })
            HanaActionType.GIFT -> {
                // Offer 3 balanced middle-to-high cards so remaining 2 are strong
                val picks = if (scoredHand.size >= 3) listOf(scoredHand[0], scoredHand[1], scoredHand.last()) else scoredHand.take(3)
                HanaBotDecision.Gift(picks.map { it.id })
            }
            HanaActionType.COMPETITION -> {
                // Split 4 cards into 2 relatively balanced groups (best+worst vs mid+mid)
                val top4 = scoredHand.take(4)
                val groupA = listOf(top4[0].id, top4[3].id)
                val groupB = listOf(top4[1].id, top4[2].id)
                HanaBotDecision.Competition(groupA, groupB)
            }
        }
    }

    private fun cardValueScore(state: HanamikojiState, seat: Int, card: HanaCard): Int {
        val g = card.geisha
        val myCount = state.playedCards[seat]?.get(g.id)?.size ?: 0
        val oppCount = state.playedCards[1 - seat]?.get(g.id)?.size ?: 0
        val favorBonus = if (state.geishaFavor[g.id] == seat) 1 else 2
        val contestBonus = if (myCount == oppCount || myCount + 1 == oppCount) 3 else 0
        return g.charm * 2 + favorBonus + contestBonus
    }

    fun toPlayerJson(state: HanamikojiState, viewerSeat: Int, seats: List<PlayerSeat>): JSONObject {
        val opponentSeat = 1 - viewerSeat
        val seatMap = seats.associateBy { it.seatIndex }
        val geishasJson = JSONArray()

        for (geisha in Geisha.entries) {
            val myCount = state.playedCards[viewerSeat]?.get(geisha.id)?.size ?: 0
            val oppCount = state.playedCards[opponentSeat]?.get(geisha.id)?.size ?: 0
            val favor = state.geishaFavor[geisha.id]
            val favorSide = when (favor) {
                viewerSeat -> "ME"
                opponentSeat -> "OPPONENT"
                else -> "NEUTRAL"
            }
            geishasJson.put(
                JSONObject().apply {
                    put("id", geisha.id)
                    put("title", geisha.title)
                    put("itemName", geisha.itemName)
                    put("badgeChar", geisha.badgeChar)
                    put("charm", geisha.charm)
                    put("accentHex", geisha.accentHex)
                    put("myCards", myCount)
                    put("opponentCards", oppCount)
                    put("favorSide", favorSide)
                }
            )
        }

        val myHandJson = JSONArray()
        for (card in state.hands[viewerSeat].orEmpty()) {
            myHandJson.put(cardToJson(card))
        }

        val myActionsJson = JSONArray()
        val oppActionsJson = JSONArray()
        val myUsed = state.usedActions[viewerSeat].orEmpty()
        val oppUsed = state.usedActions[opponentSeat].orEmpty()
        for (action in HanaActionType.entries) {
            myActionsJson.put(
                JSONObject().apply {
                    put("code", action.code)
                    put("title", action.title)
                    put("shortRule", action.shortRule)
                    put("requiredCards", action.requiredCards)
                    put("used", action in myUsed)
                }
            )
            oppActionsJson.put(
                JSONObject().apply {
                    put("code", action.code)
                    put("title", action.title)
                    put("used", action in oppUsed)
                }
            )
        }

        val offerJson = when (val offer = state.pendingOffer) {
            is HanaPendingOffer.GiftOffer -> JSONObject().apply {
                put("type", "GIFT")
                put("isForMe", offer.chooserSeat == viewerSeat)
                put("actorName", seatMap[offer.actorSeat]?.name ?: "P${offer.actorSeat + 1}")
                put("cards", JSONArray().apply { offer.cards.forEach { put(cardToJson(it)) } })
            }
            is HanaPendingOffer.CompetitionOffer -> JSONObject().apply {
                put("type", "COMPETITION")
                put("isForMe", offer.chooserSeat == viewerSeat)
                put("actorName", seatMap[offer.actorSeat]?.name ?: "P${offer.actorSeat + 1}")
                put("groupA", JSONArray().apply { offer.groupA.forEach { put(cardToJson(it)) } })
                put("groupB", JSONArray().apply { offer.groupB.forEach { put(cardToJson(it)) } })
            }
            null -> JSONObject.NULL
        }

        val revealJson = state.lastRoundReveal?.let { rev ->
            JSONObject().apply {
                put("roundNumber", rev.roundNumber)
                put("mySecret", rev.secretCards[viewerSeat]?.let { cardToJson(it) } ?: JSONObject.NULL)
                put("oppSecret", rev.secretCards[opponentSeat]?.let { cardToJson(it) } ?: JSONObject.NULL)
                put("removedCard", rev.removedCard?.let { cardToJson(it) } ?: JSONObject.NULL)
                put("myGeishas", if (viewerSeat == 0) rev.geishaCountsP0 else rev.geishaCountsP1)
                put("oppGeishas", if (viewerSeat == 0) rev.geishaCountsP1 else rev.geishaCountsP0)
                put("myCharm", if (viewerSeat == 0) rev.charmPointsP0 else rev.charmPointsP1)
                put("oppCharm", if (viewerSeat == 0) rev.charmPointsP1 else rev.charmPointsP0)
            }
        } ?: JSONObject.NULL

        return JSONObject().apply {
            put("gameType", "hanamikoji")
            put("roundNumber", state.roundNumber)
            put("phase", state.phase.code)
            put("deckCount", state.deck.size)
            put("mySeat", viewerSeat)
            put("myName", seatMap[viewerSeat]?.name ?: "P${viewerSeat + 1}")
            put("opponentName", seatMap[opponentSeat]?.name ?: "P${opponentSeat + 1}")
            put("opponentHandCount", state.hands[opponentSeat]?.size ?: 0)
            put("isMyTurn", state.phase == HanaPhase.CHOOSE_ACTION && state.currentTurnSeat == viewerSeat)
            put("needsMyChoice", state.phase == HanaPhase.WAITING_OPPONENT_CHOICE && state.pendingOffer?.chooserSeat == viewerSeat)
            put("myGeishas", state.geishaCountFor(viewerSeat))
            put("opponentGeishas", state.geishaCountFor(opponentSeat))
            put("myCharm", state.charmPointsFor(viewerSeat))
            put("opponentCharm", state.charmPointsFor(opponentSeat))
            put("mySecretCard", state.secretCards[viewerSeat]?.let { cardToJson(it) } ?: JSONObject.NULL)
            put("myHand", myHandJson)
            put("geishas", geishasJson)
            put("myActions", myActionsJson)
            put("opponentActions", oppActionsJson)
            put("pendingOffer", offerJson)
            put("roundReveal", revealJson)
            put("winnerSeat", state.winnerSeat ?: JSONObject.NULL)
            put("winnerName", state.winnerSeat?.let { seatMap[it]?.name ?: "P${it + 1}" } ?: JSONObject.NULL)
            put("winReason", state.winReason)
            put("lastEvent", state.lastEvent)
        }
    }

    private fun cardToJson(card: HanaCard): JSONObject = JSONObject().apply {
        val g = card.geisha
        put("id", card.id)
        put("geishaId", g.id)
        put("title", g.title)
        put("itemName", g.itemName)
        put("badgeChar", g.badgeChar)
        put("charm", g.charm)
        put("accentHex", g.accentHex)
    }
}
