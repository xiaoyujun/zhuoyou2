package com.example

import com.example.game.hanamikoji.HanaBotDecision
import com.example.game.hanamikoji.HanaMoveResult
import com.example.game.hanamikoji.HanaPhase
import com.example.game.hanamikoji.HanamikojiEngine
import com.example.game.uno.UnoBotDecision
import com.example.game.uno.UnoEngine
import com.example.game.uno.UnoMoveResult
import com.example.model.PlayerSeat
import com.example.model.PlayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TableBoxEngineTest {

    @Test
    fun unoFullMatchCompletesValidlyWithFourPlayers() {
        val deck = UnoEngine.createDeck(Random(42))
        assertEquals(108, deck.size)

        val seats = listOf(
            PlayerSeat(0, "p1", "房主", PlayerType.HOST),
            PlayerSeat(1, "p2", "玩家2", PlayerType.WEB_CLIENT),
            PlayerSeat(2, "p3", "玩家3", PlayerType.WEB_CLIENT),
            PlayerSeat(3, "p4", "玩家4", PlayerType.WEB_CLIENT)
        )
        val names = seats.associate { it.seatIndex to it.name }
        var state = UnoEngine.newGame(seats, random = Random(42))
        assertEquals(7, state.hands[0]?.size)
        assertFalse(state.topCard.value.isWild)

        var turns = 0
        while (!state.isFinished && turns < 400) {
            val actor = state.currentTurnSeat
            val res = when (val move = UnoEngine.computeBotMove(state, actor)) {
                is UnoBotDecision.Play -> UnoEngine.playCard(state, actor, move.cardId, move.chosenColor, names, Random(42 + turns))
                UnoBotDecision.Draw -> UnoEngine.drawCard(state, actor, names, Random(42 + turns))
                UnoBotDecision.Pass -> UnoEngine.passTurn(state, actor, names)
            }
            assertTrue("UNO move failed: $res", res is UnoMoveResult.Success)
            state = (res as UnoMoveResult.Success).state
            turns++
        }

        assertTrue("UNO game should finish within 400 turns", state.isFinished)
        assertNotNull(state.winnerSeat)
        assertTrue((state.scores[state.winnerSeat!!] ?: 0) >= 0)
    }

    @Test
    fun hanamikojiFullRoundResolvesAndPreservesFavor() {
        val names = mapOf(0 to "P1", 1 to "P2")
        var state = HanamikojiEngine.newMatch(firstPlayerSeat = 0, seatNames = names, random = Random(99))
        assertEquals(21, HanamikojiEngine.createDeck(Random(99)).size)
        assertEquals(7, state.hands[0]?.size)
        assertEquals(6, state.hands[1]?.size)

        var steps = 0
        while (state.phase != HanaPhase.ROUND_SUMMARY && state.phase != HanaPhase.GAME_OVER && steps < 24) {
            val actor = when (state.phase) {
                HanaPhase.CHOOSE_ACTION -> state.currentTurnSeat
                HanaPhase.WAITING_OPPONENT_CHOICE -> state.pendingOffer!!.chooserSeat
                HanaPhase.ROUND_SUMMARY, HanaPhase.GAME_OVER -> break
            }
            val move = HanamikojiEngine.computeBotAction(state, actor)
            assertNotNull("Expected valid bot move at step $steps", move)
            val res = when (move!!) {
                is HanaBotDecision.Secret -> HanamikojiEngine.performSecret(state, actor, move.cardId, names)
                is HanaBotDecision.Discard -> HanamikojiEngine.performDiscard(state, actor, move.cardIds, names)
                is HanaBotDecision.Gift -> HanamikojiEngine.performGift(state, actor, move.cardIds, names)
                is HanaBotDecision.Competition -> HanamikojiEngine.performCompetition(state, actor, move.groupA, move.groupB, names)
                is HanaBotDecision.ResolveGift -> HanamikojiEngine.resolveGift(state, actor, move.chosenCardId, names)
                is HanaBotDecision.ResolveCompetition -> HanamikojiEngine.resolveCompetition(state, actor, move.chosenGroup, names)
            }
            assertTrue("Hanamikoji move failed: $res", res is HanaMoveResult.Success)
            state = (res as HanaMoveResult.Success).state
            steps++
        }

        assertTrue(state.phase == HanaPhase.ROUND_SUMMARY || state.phase == HanaPhase.GAME_OVER)
        assertNotNull(state.lastRoundReveal)
        assertEquals(4, state.usedActions[0]?.size)
        assertEquals(4, state.usedActions[1]?.size)

        if (state.phase == HanaPhase.ROUND_SUMMARY) {
            val next = HanamikojiEngine.startNextRound(state, names, Random(101))
            assertTrue(next is HanaMoveResult.Success)
            val nextState = (next as HanaMoveResult.Success).state
            assertEquals(2, nextState.roundNumber)
            assertEquals(state.geishaFavor, nextState.geishaFavor)
        }
    }
}
