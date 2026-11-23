package com.example.game.uno

import com.example.model.PlayerSeat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

object UnoEngine {

    fun createDeck(random: Random = Random.Default): List<UnoCard> {
        val cards = mutableListOf<UnoCard>()
        var seq = 0
        for (color in UnoColor.PLAYABLE_COLORS) {
            cards += UnoCard("u_${seq++}", color, UnoValue.ZERO)
            val duplicated = listOf(
                UnoValue.ONE, UnoValue.TWO, UnoValue.THREE, UnoValue.FOUR,
                UnoValue.FIVE, UnoValue.SIX, UnoValue.SEVEN, UnoValue.EIGHT,
                UnoValue.NINE, UnoValue.SKIP, UnoValue.REVERSE, UnoValue.DRAW_TWO
            )
            for (value in duplicated) {
                cards += UnoCard("u_${seq++}", color, value)
                cards += UnoCard("u_${seq++}", color, value)
            }
        }
        repeat(4) {
            cards += UnoCard("u_${seq++}", UnoColor.WILD, UnoValue.WILD)
            cards += UnoCard("u_${seq++}", UnoColor.WILD, UnoValue.WILD_DRAW_FOUR)
        }
        return cards.shuffled(random)
    }

    fun newGame(
        seats: List<PlayerSeat>,
        previousScores: Map<Int, Int> = emptyMap(),
        roundNumber: Int = 1,
        random: Random = Random.Default
    ): UnoState {
        val order = seats.sortedBy { it.seatIndex }.map { it.seatIndex }
        val deck = createDeck(random).toMutableList()
        val hands = mutableMapOf<Int, List<UnoCard>>()
        for (seatIndex in order) {
            val dealt = MutableList(7) { deck.removeAt(0) }
            hands[seatIndex] = sortHand(dealt)
        }
        // Pick a starting non-wild number card for clean opening
        val starterIndex = deck.indexOfFirst { !it.value.isWild && it.value.points < 20 }.takeIf { it >= 0 } ?: 0
        val starter = deck.removeAt(starterIndex)
        val firstSeat = order[(roundNumber - 1) % order.size]
        val initialScores = order.associateWith { previousScores[it] ?: 0 }
        val starterSeatName = seats.firstOrNull { it.seatIndex == firstSeat }?.name ?: "P${firstSeat + 1}"

        return UnoState(
            seatOrder = order,
            hands = hands,
            drawPile = deck,
            discardPile = listOf(starter),
            activeColor = starter.color,
            currentTurnSeat = firstSeat,
            direction = 1,
            scores = initialScores,
            roundNumber = roundNumber,
            lastEvent = "第 $roundNumber 局开始！起始牌 ${starter.displayName()}，轮到 $starterSeatName 出牌。"
        )
    }

    fun sortHand(cards: List<UnoCard>): List<UnoCard> {
        val colorOrder = mapOf(
            UnoColor.RED to 0,
            UnoColor.YELLOW to 1,
            UnoColor.GREEN to 2,
            UnoColor.BLUE to 3,
            UnoColor.WILD to 4
        )
        return cards.sortedWith(compareBy<UnoCard> { colorOrder[it.color] ?: 9 }.thenBy { it.value.ordinal })
    }

    fun canPlayCard(state: UnoState, seatIndex: Int, card: UnoCard): Boolean {
        if (state.isFinished || state.currentTurnSeat != seatIndex) return false
        if (state.hasDrawnThisTurn && state.drawnCardId != null && card.id != state.drawnCardId) {
            return false
        }
        if (card.value.isWild) return true
        val top = state.topCard
        return card.color == state.activeColor || card.value == top.value
    }

    fun playCard(
        state: UnoState,
        seatIndex: Int,
        cardId: String,
        chosenColor: UnoColor?,
        seatNames: Map<Int, String> = emptyMap(),
        random: Random = Random.Default
    ): UnoMoveResult {
        if (state.isFinished) return UnoMoveResult.Failure("本局已经结束")
        if (state.currentTurnSeat != seatIndex) return UnoMoveResult.Failure("还没轮到你的回合")

        val hand = state.hands[seatIndex] ?: return UnoMoveResult.Failure("无效座位")
        val card = hand.firstOrNull { it.id == cardId } ?: return UnoMoveResult.Failure("手牌中找不到该卡牌")
        if (!canPlayCard(state, seatIndex, card)) {
            return UnoMoveResult.Failure("当前无法打出 ${card.displayName()}")
        }

        val resolvedColor = if (card.value.isWild) {
            if (chosenColor == null || chosenColor == UnoColor.WILD) {
                return UnoMoveResult.Failure("请为万能牌指定颜色")
            }
            chosenColor
        } else {
            card.color
        }

        val updatedHand = hand.filterNot { it.id == cardId }
        val newHands = state.hands.toMutableMap()
        newHands[seatIndex] = updatedHand

        var drawPile = state.drawPile.toMutableList()
        val discardPile = (state.discardPile + card).toMutableList()
        val actorName = seatNames[seatIndex] ?: "P${seatIndex + 1}"

        // Update UNO call status
        val newUnoCalled = state.unoCalledSeats.toMutableSet()
        if (updatedHand.size != 1) {
            newUnoCalled.remove(seatIndex)
        }

        // Check win
        if (updatedHand.isEmpty()) {
            val earnedPoints = newHands
                .filterKeys { it != seatIndex }
                .values
                .flatten()
                .sumOf { it.value.points }
            val updatedScores = state.scores.toMutableMap()
            updatedScores[seatIndex] = (updatedScores[seatIndex] ?: 0) + earnedPoints

            return UnoMoveResult.Success(
                state.copy(
                    hands = newHands,
                    drawPile = drawPile,
                    discardPile = discardPile,
                    activeColor = resolvedColor,
                    unoCalledSeats = newUnoCalled,
                    hasDrawnThisTurn = false,
                    drawnCardId = null,
                    winnerSeat = seatIndex,
                    lastRoundPoints = earnedPoints,
                    scores = updatedScores,
                    lastEvent = "$actorName 打出 ${card.displayName()} 清空手牌获胜！赢得 $earnedPoints 分。"
                )
            )
        }

        var direction = state.direction
        var steps = 1
        var effectSuffix = ""

        when (card.value) {
            UnoValue.REVERSE -> {
                if (state.seatOrder.size == 2) {
                    steps = 2
                    effectSuffix = "，2人局反转等同于跳过对手！"
                } else {
                    direction = -direction
                    effectSuffix = if (direction > 0) "，出牌方向变为顺时针！" else "，出牌方向变为逆时针！"
                }
            }
            UnoValue.SKIP -> {
                steps = 2
                val skippedSeat = nextSeat(state.seatOrder, seatIndex, direction, 1)
                val skippedName = seatNames[skippedSeat] ?: "P${skippedSeat + 1}"
                effectSuffix = "，跳过 $skippedName！"
            }
            UnoValue.DRAW_TWO -> {
                val targetSeat = nextSeat(state.seatOrder, seatIndex, direction, 1)
                val drawn = drawCardsFromPile(drawPile, discardPile, 2, random)
                drawPile = drawn.remainingDrawPile
                newHands[targetSeat] = sortHand((newHands[targetSeat] ?: emptyList()) + drawn.cards)
                newUnoCalled.remove(targetSeat)
                steps = 2
                val targetName = seatNames[targetSeat] ?: "P${targetSeat + 1}"
                effectSuffix = "，$targetName 罚摸 2 张并跳过！"
            }
            UnoValue.WILD_DRAW_FOUR -> {
                val targetSeat = nextSeat(state.seatOrder, seatIndex, direction, 1)
                val drawn = drawCardsFromPile(drawPile, discardPile, 4, random)
                drawPile = drawn.remainingDrawPile
                newHands[targetSeat] = sortHand((newHands[targetSeat] ?: emptyList()) + drawn.cards)
                newUnoCalled.remove(targetSeat)
                steps = 2
                val targetName = seatNames[targetSeat] ?: "P${targetSeat + 1}"
                effectSuffix = "，指定${resolvedColor.label}色，$targetName 罚摸 4 张并跳过！"
            }
            UnoValue.WILD -> {
                effectSuffix = "，指定颜色为${resolvedColor.label}色！"
            }
            else -> Unit
        }

        val nextTurn = nextSeat(state.seatOrder, seatIndex, direction, steps)
        val unoReminder = if (updatedHand.size == 1 && seatIndex in newUnoCalled) "（已喊 UNO!）" else ""

        return UnoMoveResult.Success(
            state.copy(
                hands = newHands,
                drawPile = drawPile,
                discardPile = discardPile,
                activeColor = resolvedColor,
                currentTurnSeat = nextTurn,
                direction = direction,
                unoCalledSeats = newUnoCalled,
                hasDrawnThisTurn = false,
                drawnCardId = null,
                lastEvent = "$actorName 打出 ${card.displayName()}$unoReminder$effectSuffix"
            )
        )
    }

    fun drawCard(
        state: UnoState,
        seatIndex: Int,
        seatNames: Map<Int, String> = emptyMap(),
        random: Random = Random.Default
    ): UnoMoveResult {
        if (state.isFinished) return UnoMoveResult.Failure("本局已经结束")
        if (state.currentTurnSeat != seatIndex) return UnoMoveResult.Failure("还没轮到你的回合")
        if (state.hasDrawnThisTurn) return UnoMoveResult.Failure("本回合已经摸过牌，请出牌或跳过")

        val drawPile = state.drawPile.toMutableList()
        val discardPile = state.discardPile.toMutableList()
        val drawnResult = drawCardsFromPile(drawPile, discardPile, 1, random)
        val drawnCard = drawnResult.cards.firstOrNull()
        val actorName = seatNames[seatIndex] ?: "P${seatIndex + 1}"

        if (drawnCard == null) {
            val nextTurn = nextSeat(state.seatOrder, seatIndex, state.direction, 1)
            return UnoMoveResult.Success(
                state.copy(
                    currentTurnSeat = nextTurn,
                    hasDrawnThisTurn = false,
                    drawnCardId = null,
                    lastEvent = "牌堆已空，$actorName 跳过回合。"
                )
            )
        }

        val newHands = state.hands.toMutableMap()
        newHands[seatIndex] = sortHand((newHands[seatIndex] ?: emptyList()) + drawnCard)
        val newUnoCalled = state.unoCalledSeats - seatIndex

        val tempState = state.copy(
            hands = newHands,
            drawPile = drawnResult.remainingDrawPile,
            discardPile = drawnResult.remainingDiscardPile,
            unoCalledSeats = newUnoCalled,
            hasDrawnThisTurn = true,
            drawnCardId = drawnCard.id
        )

        val playable = canPlayCard(tempState, seatIndex, drawnCard)
        return if (playable) {
            UnoMoveResult.Success(
                tempState.copy(
                    lastEvent = "$actorName 摸了一张牌，可立即打出或选择跳过。"
                )
            )
        } else {
            val nextTurn = nextSeat(state.seatOrder, seatIndex, state.direction, 1)
            UnoMoveResult.Success(
                tempState.copy(
                    currentTurnSeat = nextTurn,
                    hasDrawnThisTurn = false,
                    drawnCardId = null,
                    lastEvent = "$actorName 摸了一张牌（无匹配牌），回合结束。"
                )
            )
        }
    }

    fun passTurn(
        state: UnoState,
        seatIndex: Int,
        seatNames: Map<Int, String> = emptyMap()
    ): UnoMoveResult {
        if (state.isFinished) return UnoMoveResult.Failure("本局已经结束")
        if (state.currentTurnSeat != seatIndex) return UnoMoveResult.Failure("还没轮到你的回合")
        if (!state.hasDrawnThisTurn) return UnoMoveResult.Failure("必须先摸一张牌才能跳过")

        val nextTurn = nextSeat(state.seatOrder, seatIndex, state.direction, 1)
        val actorName = seatNames[seatIndex] ?: "P${seatIndex + 1}"
        return UnoMoveResult.Success(
            state.copy(
                currentTurnSeat = nextTurn,
                hasDrawnThisTurn = false,
                drawnCardId = null,
                lastEvent = "$actorName 保留了刚摸的牌并结束回合。"
            )
        )
    }

    fun callUno(
        state: UnoState,
        seatIndex: Int,
        seatNames: Map<Int, String> = emptyMap()
    ): UnoMoveResult {
        if (state.isFinished) return UnoMoveResult.Failure("本局已经结束")
        val handSize = state.hands[seatIndex]?.size ?: 0
        if (handSize !in 1..2) {
            return UnoMoveResult.Failure("仅在剩余 1–2 张牌时可以喊 UNO")
        }
        val actorName = seatNames[seatIndex] ?: "P${seatIndex + 1}"
        return UnoMoveResult.Success(
            state.copy(
                unoCalledSeats = state.unoCalledSeats + seatIndex,
                lastEvent = "$actorName 大喊一声：UNO！"
            )
        )
    }

    fun catchUno(
        state: UnoState,
        catcherSeat: Int,
        targetSeat: Int,
        seatNames: Map<Int, String> = emptyMap(),
        random: Random = Random.Default
    ): UnoMoveResult {
        if (state.isFinished) return UnoMoveResult.Failure("本局已经结束")
        if (catcherSeat == targetSeat) return UnoMoveResult.Failure("不能质疑自己")
        val targetHand = state.hands[targetSeat] ?: return UnoMoveResult.Failure("无效目标玩家")
        if (targetHand.size != 1 || targetSeat in state.unoCalledSeats) {
            return UnoMoveResult.Failure("对方无需被罚摸")
        }
        val drawn = drawCardsFromPile(state.drawPile.toMutableList(), state.discardPile.toMutableList(), 2, random)
        val newHands = state.hands.toMutableMap()
        newHands[targetSeat] = sortHand(targetHand + drawn.cards)
        val catcherName = seatNames[catcherSeat] ?: "P${catcherSeat + 1}"
        val targetName = seatNames[targetSeat] ?: "P${targetSeat + 1}"
        return UnoMoveResult.Success(
            state.copy(
                hands = newHands,
                drawPile = drawn.remainingDrawPile,
                discardPile = drawn.remainingDiscardPile,
                lastEvent = "$catcherName 抓住 $targetName 忘记喊 UNO！$targetName 罚摸 2 张。"
            )
        )
    }

    fun computeBotMove(state: UnoState, seatIndex: Int): UnoBotDecision {
        val hand = state.hands[seatIndex].orEmpty()
        val playable = hand.filter { canPlayCard(state, seatIndex, it) }
        if (playable.isEmpty()) {
            return if (state.hasDrawnThisTurn) UnoBotDecision.Pass else UnoBotDecision.Draw
        }
        // Prefer non-wild cards first, especially action cards or highest value cards
        val preferred = playable.sortedWith(
            compareBy<UnoCard> { if (it.value.isWild) 1 else 0 }
                .thenByDescending { it.value.points }
        ).first()

        val chosenColor = if (preferred.value.isWild) {
            val colorCounts = hand.filter { !it.value.isWild }
                .groupingBy { it.color }
                .eachCount()
            colorCounts.maxByOrNull { it.value }?.key ?: UnoColor.PLAYABLE_COLORS.random()
        } else null

        return UnoBotDecision.Play(preferred.id, chosenColor)
    }

    private data class DrawBatch(
        val cards: List<UnoCard>,
        val remainingDrawPile: MutableList<UnoCard>,
        val remainingDiscardPile: MutableList<UnoCard>
    )

    private fun drawCardsFromPile(
        drawPile: MutableList<UnoCard>,
        discardPile: MutableList<UnoCard>,
        count: Int,
        random: Random
    ): DrawBatch {
        val drawn = mutableListOf<UnoCard>()
        repeat(count) {
            if (drawPile.isEmpty() && discardPile.size > 1) {
                val top = discardPile.removeAt(discardPile.lastIndex)
                drawPile.addAll(discardPile.shuffled(random))
                discardPile.clear()
                discardPile.add(top)
            }
            if (drawPile.isNotEmpty()) {
                drawn.add(drawPile.removeAt(0))
            }
        }
        return DrawBatch(drawn, drawPile, discardPile)
    }

    private fun nextSeat(order: List<Int>, currentSeat: Int, direction: Int, steps: Int): Int {
        val idx = order.indexOf(currentSeat).coerceAtLeast(0)
        val n = order.size
        val target = ((idx + direction * steps) % n + n) % n
        return order[target]
    }

    fun toPlayerJson(state: UnoState, viewerSeat: Int, seats: List<PlayerSeat>): JSONObject {
        val seatMap = seats.associateBy { it.seatIndex }
        val playersJson = JSONArray()
        for (seatIndex in state.seatOrder) {
            val seat = seatMap[seatIndex]
            val handSize = state.hands[seatIndex]?.size ?: 0
            val unoSafe = seatIndex in state.unoCalledSeats
            playersJson.put(
                JSONObject().apply {
                    put("seatIndex", seatIndex)
                    put("seatTag", "P${seatIndex + 1}")
                    put("name", seat?.name ?: "P${seatIndex + 1}")
                    put("isBot", seat?.type == com.example.model.PlayerType.BOT)
                    put("cardCount", handSize)
                    put("unoCalled", unoSafe)
                    put("canBeCaught", handSize == 1 && !unoSafe && seatIndex != viewerSeat && !state.isFinished)
                    put("score", state.scores[seatIndex] ?: 0)
                    put("isCurrentTurn", state.currentTurnSeat == seatIndex && !state.isFinished)
                }
            )
        }

        val myHandJson = JSONArray()
        val myHand = state.hands[viewerSeat].orEmpty()
        for (card in myHand) {
            myHandJson.put(
                JSONObject().apply {
                    put("id", card.id)
                    put("color", card.color.code)
                    put("value", card.value.code)
                    put("symbol", card.value.symbol)
                    put("label", card.value.label)
                    put("isWild", card.value.isWild)
                    put("playable", canPlayCard(state, viewerSeat, card))
                }
            )
        }

        val topCardJson = JSONObject().apply {
            put("id", state.topCard.id)
            put("color", state.topCard.color.code)
            put("value", state.topCard.value.code)
            put("symbol", state.topCard.value.symbol)
            put("label", state.topCard.value.label)
            put("isWild", state.topCard.value.isWild)
        }

        return JSONObject().apply {
            put("gameType", "uno")
            put("roundNumber", state.roundNumber)
            put("activeColor", state.activeColor.code)
            put("activeColorLabel", state.activeColor.label)
            put("direction", state.direction)
            put("currentTurnSeat", state.currentTurnSeat)
            put("isMyTurn", state.currentTurnSeat == viewerSeat && !state.isFinished)
            put("hasDrawnThisTurn", state.hasDrawnThisTurn && state.currentTurnSeat == viewerSeat)
            put("canCallUno", !state.isFinished && myHand.size in 1..2 && viewerSeat !in state.unoCalledSeats)
            put("unoCalled", viewerSeat in state.unoCalledSeats)
            put("drawPileCount", state.drawPile.size)
            put("topCard", topCardJson)
            put("players", playersJson)
            put("myHand", myHandJson)
            put("winnerSeat", state.winnerSeat ?: JSONObject.NULL)
            put("winnerName", state.winnerSeat?.let { seatMap[it]?.name ?: "P${it + 1}" } ?: JSONObject.NULL)
            put("lastRoundPoints", state.lastRoundPoints)
            put("lastEvent", state.lastEvent)
        }
    }
}
