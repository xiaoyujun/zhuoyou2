package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.hanamikoji.Geisha
import com.example.game.hanamikoji.HanaActionType
import com.example.game.hanamikoji.HanaCard
import com.example.game.hanamikoji.HanaPendingOffer
import com.example.game.hanamikoji.HanaPhase
import com.example.game.hanamikoji.HanamikojiState
import com.example.model.PlayerSeat
import com.example.ui.theme.GeishaBamboo
import com.example.ui.theme.GeishaChrys
import com.example.ui.theme.GeishaCrimson
import com.example.ui.theme.GeishaMagnolia
import com.example.ui.theme.GeishaPlum
import com.example.ui.theme.GeishaSakura
import com.example.ui.theme.GeishaWisteria
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.JadeEmerald
import com.example.ui.theme.KyotoSakura
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeOutline
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.LoungeSurfaceElevated
import com.example.ui.theme.MutedText

@Composable
fun HanamikojiGameScreen(
    hanaState: HanamikojiState,
    seats: List<PlayerSeat>,
    statusMessage: String?,
    onSecret: (String) -> Unit,
    onDiscard: (List<String>) -> Unit,
    onGift: (List<String>) -> Unit,
    onCompetition: (List<String>, List<String>) -> Unit,
    onResolveGift: (String) -> Unit,
    onResolveCompetition: (Int) -> Unit,
    onNextRound: () -> Unit,
    onRestartMatch: () -> Unit,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToLobby() }

    val seatMap = remember(seats) { seats.associateBy { it.seatIndex } }
    val p1Name = seatMap[0]?.name ?: "房主 P1"
    val p2Name = seatMap[1]?.name ?: "玩家 P2"
    val isMyTurn = hanaState.phase == HanaPhase.CHOOSE_ACTION && hanaState.currentTurnSeat == 0
    val myHand = hanaState.hands[0].orEmpty()
    val myUsedActions = hanaState.usedActions[0].orEmpty()

    var selectedAction by remember { mutableStateOf<HanaActionType?>(null) }
    var pickA by remember { mutableStateOf<List<String>>(emptyList()) }
    var pickB by remember { mutableStateOf<List<String>>(emptyList()) }
    var compTargetIsA by remember { mutableStateOf(true) }

    // Reset selection whenever turn or round changes
    LaunchedEffect(hanaState.currentTurnSeat, hanaState.phase, hanaState.roundNumber, myUsedActions.size) {
        selectedAction = null
        pickA = emptyList()
        pickB = emptyList()
        compTargetIsA = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2D1B2E), LoungeObsidian),
                        center = Offset(size.width * 0.8f, size.height * 0.12f),
                        radius = size.maxDimension * 0.9f
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLobby,
                    modifier = Modifier.testTag("hana_back_lobby_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回房间",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "花间小路 · 第 ${hanaState.roundNumber} 轮",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "牌堆剩余 ${hanaState.deck.size} 张 · 对手手牌 ${hanaState.hands[1]?.size ?: 0} 张",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }
                IconButton(
                    onClick = onRestartMatch,
                    modifier = Modifier.testTag("hana_restart_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重新开始",
                        tint = KyotoSakura
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Event Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LoungeSurface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, KyotoSakura.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage ?: hanaState.lastEvent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }

                // Dual Scoreboard
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HanaScoreCard(
                            title = "我 · $p1Name",
                            geishas = hanaState.geishaCountFor(0),
                            charm = hanaState.charmPointsFor(0),
                            accent = JadeEmerald,
                            isActive = hanaState.currentTurnSeat == 0 && hanaState.phase == HanaPhase.CHOOSE_ACTION,
                            modifier = Modifier.weight(1f)
                        )
                        HanaScoreCard(
                            title = "对手 · $p2Name",
                            geishas = hanaState.geishaCountFor(1),
                            charm = hanaState.charmPointsFor(1),
                            accent = KyotoSakura,
                            isActive = hanaState.currentTurnSeat == 1 && hanaState.phase == HanaPhase.CHOOSE_ACTION,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Round Summary or Game Over Banner
                if (hanaState.phase == HanaPhase.ROUND_SUMMARY || hanaState.phase == HanaPhase.GAME_OVER) {
                    item {
                        RoundResultCard(
                            hanaState = hanaState,
                            p1Name = p1Name,
                            p2Name = p2Name,
                            onNextRound = onNextRound,
                            onRestartMatch = onRestartMatch,
                            onBackToLobby = onBackToLobby
                        )
                    }
                }

                // Pending Offer Panel (Gift / Competition response)
                hanaState.pendingOffer?.let { offer ->
                    item {
                        PendingOfferCard(
                            offer = offer,
                            p2Name = p2Name,
                            onResolveGift = onResolveGift,
                            onResolveCompetition = onResolveCompetition
                        )
                    }
                }

                // 7 Geishas Board (paired rows for scannable mobile layout)
                item {
                    Text(
                        text = "七位艺伎与好感归属",
                        style = MaterialTheme.typography.titleSmall,
                        color = MutedText
                    )
                }

                val geishaPairs = Geisha.entries.chunked(2)
                items(geishaPairs) { rowGeishas ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowGeishas.forEach { geisha ->
                            GeishaBoardCard(
                                geisha = geisha,
                                myCount = hanaState.playedCards[0]?.get(geisha.id)?.size ?: 0,
                                oppCount = hanaState.playedCards[1]?.get(geisha.id)?.size ?: 0,
                                favorSeat = hanaState.geishaFavor[geisha.id],
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowGeishas.size == 1) {
                            // 7th Geisha gets extra breathing room alongside secret card reminder
                            MySecretInfoCard(
                                secretCard = hanaState.secretCards[0],
                                discardedCount = hanaState.discardedCards[0]?.size ?: 0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Action Tokens & Hand Selection Area
                if (hanaState.phase != HanaPhase.ROUND_SUMMARY && hanaState.phase != HanaPhase.GAME_OVER) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = LoungeSurface),
                            border = BorderStroke(1.dp, if (isMyTurn) ImperialGold.copy(alpha = 0.7f) else LoungeOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isMyTurn) "轮到你行动：选择 1 个行动标记" else "等待对手行动...",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isMyTurn) ImperialGold else MutedText
                                )

                                // 2x2 Action Tokens
                                val actionPairs = HanaActionType.entries.chunked(2)
                                actionPairs.forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { action ->
                                            val used = action in myUsedActions
                                            val isSelected = selectedAction == action
                                            ActionTokenButton(
                                                action = action,
                                                used = used,
                                                selected = isSelected,
                                                enabled = isMyTurn && !used && myHand.size >= action.requiredCards,
                                                onClick = {
                                                    selectedAction = action
                                                    pickA = emptyList()
                                                    pickB = emptyList()
                                                    compTargetIsA = true
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Competition A/B group switcher
                                if (isMyTurn && selectedAction == HanaActionType.COMPETITION) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { compTargetIsA = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (compTargetIsA) ImperialGold else LoungeSurfaceElevated,
                                                contentColor = if (compTargetIsA) LoungeObsidian else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("选择 A 组 (${pickA.size}/2)")
                                        }
                                        Button(
                                            onClick = { compTargetIsA = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (!compTargetIsA) KyotoSakura else LoungeSurfaceElevated,
                                                contentColor = if (!compTargetIsA) LoungeObsidian else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                        ) {
                                            Text("选择 B 组 (${pickB.size}/2)")
                                        }
                                    }
                                }

                                // P1 Hand Row
                                Text(
                                    text = "我的手牌 (${myHand.size} 张)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MutedText
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(myHand, key = { it.id }) { card ->
                                        val inA = card.id in pickA
                                        val inB = card.id in pickB
                                        val badgeLabel = when {
                                            selectedAction == HanaActionType.COMPETITION && inA -> "A 组"
                                            selectedAction == HanaActionType.COMPETITION && inB -> "B 组"
                                            inA -> "已选"
                                            else -> null
                                        }
                                        HanaHandCardView(
                                            card = card,
                                            selectedLabel = badgeLabel,
                                            highlightColor = if (inB) KyotoSakura else ImperialGold,
                                            onClick = {
                                                val act = selectedAction ?: return@HanaHandCardView
                                                if (!isMyTurn) return@HanaHandCardView
                                                if (act == HanaActionType.COMPETITION) {
                                                    when {
                                                        inA -> pickA = pickA - card.id
                                                        inB -> pickB = pickB - card.id
                                                        compTargetIsA && pickA.size < 2 -> {
                                                            pickA = pickA + card.id
                                                            if (pickA.size == 2 && pickB.size < 2) compTargetIsA = false
                                                        }
                                                        !compTargetIsA && pickB.size < 2 -> {
                                                            pickB = pickB + card.id
                                                            if (pickB.size == 2 && pickA.size < 2) compTargetIsA = true
                                                        }
                                                    }
                                                } else {
                                                    pickA = if (inA) {
                                                        pickA - card.id
                                                    } else if (pickA.size < act.requiredCards) {
                                                        pickA + card.id
                                                    } else {
                                                        pickA
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                                // Confirm Action Button
                                val activeAct = selectedAction
                                if (isMyTurn && activeAct != null) {
                                    val isReady = when (activeAct) {
                                        HanaActionType.SECRET -> pickA.size == 1
                                        HanaActionType.DISCARD -> pickA.size == 2
                                        HanaActionType.GIFT -> pickA.size == 3
                                        HanaActionType.COMPETITION -> pickA.size == 2 && pickB.size == 2
                                    }
                                    Button(
                                        onClick = {
                                            when (activeAct) {
                                                HanaActionType.SECRET -> onSecret(pickA.first())
                                                HanaActionType.DISCARD -> onDiscard(pickA)
                                                HanaActionType.GIFT -> onGift(pickA)
                                                HanaActionType.COMPETITION -> onCompetition(pickA, pickB)
                                            }
                                        },
                                        enabled = isReady,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = JadeEmerald,
                                            contentColor = LoungeObsidian
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("hana_confirm_action_button")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "确认执行【${activeAct.title}】",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
private fun HanaScoreCard(
    title: String,
    geishas: Int,
    charm: Int,
    accent: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) LoungeSurfaceElevated else LoungeSurface,
        border = BorderStroke(if (isActive) 2.dp else 1.dp, if (isActive) accent else LoungeOutline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "艺伎 $geishas/4 · 魅力 $charm/11",
                style = MaterialTheme.typography.labelMedium,
                color = MutedText
            )
            LinearProgressIndicator(
                progress = { (charm / 11f).coerceIn(0f, 1f) },
                color = accent,
                trackColor = LoungeObsidian,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun GeishaBoardCard(
    geisha: Geisha,
    myCount: Int,
    oppCount: Int,
    favorSeat: Int,
    modifier: Modifier = Modifier
) {
    val gColor = geishaColor(geisha)
    val favorText = when (favorSeat) {
        0 -> "好感归我方"
        1 -> "好感归对手"
        else -> "好感中立"
    }
    val favorBg = when (favorSeat) {
        0 -> JadeEmerald.copy(alpha = 0.2f)
        1 -> KyotoSakura.copy(alpha = 0.2f)
        else -> LoungeObsidian.copy(alpha = 0.7f)
    }
    val favorFg = when (favorSeat) {
        0 -> JadeEmerald
        1 -> KyotoSakura
        else -> MutedText
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = LoungeSurface,
        border = BorderStroke(1.dp, gColor.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(gColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = geisha.badgeChar,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = geisha.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = geisha.itemName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText
                        )
                    }
                }
                Text(
                    text = "${geisha.charm}分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = gColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("对手 $oppCount", style = MaterialTheme.typography.labelMedium, color = KyotoSakura)
                Text("我方 $myCount", style = MaterialTheme.typography.labelMedium, color = JadeEmerald)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(favorBg)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = favorText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = favorFg
                )
            }
        }
    }
}

@Composable
private fun MySecretInfoCard(
    secretCard: HanaCard?,
    discardedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = LoungeSurfaceElevated.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, LoungeOutline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "我方暗置区",
                style = MaterialTheme.typography.titleSmall,
                color = ImperialGold
            )
            Text(
                text = if (secretCard != null) {
                    "秘密牌：${secretCard.geisha.title} (${secretCard.geisha.charm}分)"
                } else {
                    "秘密牌：尚未放置"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "已弃置：$discardedCount 张",
                style = MaterialTheme.typography.labelMedium,
                color = MutedText
            )
        }
    }
}

@Composable
private fun ActionTokenButton(
    action: HanaActionType,
    used: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = when {
            selected -> ImperialGold.copy(alpha = 0.18f)
            used -> LoungeObsidian.copy(alpha = 0.5f)
            else -> LoungeSurfaceElevated
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ImperialGold else LoungeOutline
        ),
        modifier = modifier
            .alpha(if (used) 0.42f else 1f)
            .testTag("hana_action_${action.code.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "${action.title} · 选${action.requiredCards}张",
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) ImperialGold else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (used) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = action.shortRule,
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HanaHandCardView(
    card: HanaCard,
    selectedLabel: String?,
    highlightColor: Color,
    onClick: () -> Unit
) {
    val g = card.geisha
    val gColor = geishaColor(g)
    val isSelected = selectedLabel != null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) highlightColor.copy(alpha = 0.2f) else LoungeSurfaceElevated,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) highlightColor else gColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .width(86.dp)
            .clickable { onClick() }
            .testTag("hana_card_${card.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(gColor),
                contentAlignment = Alignment.Center
            ) {
                Text(g.badgeChar, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                text = g.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${g.charm}分 · ${g.itemName}",
                style = MaterialTheme.typography.labelSmall,
                color = MutedText
            )
            if (selectedLabel != null) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = highlightColor
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = LoungeObsidian,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingOfferCard(
    offer: HanaPendingOffer,
    p2Name: String,
    onResolveGift: (String) -> Unit,
    onResolveCompetition: (Int) -> Unit
) {
    val isForMe = offer.chooserSeat == 0
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LoungeSurfaceElevated),
        border = BorderStroke(2.dp, ImperialGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (offer) {
                is HanaPendingOffer.GiftOffer -> {
                    Text(
                        text = if (isForMe) "$p2Name 发来【赠礼】：请点击挑选 1 张" else "已向 $p2Name 发出【赠礼】，等待对方挑选...",
                        style = MaterialTheme.typography.titleSmall,
                        color = ImperialGold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(offer.cards, key = { it.id }) { card ->
                            HanaHandCardView(
                                card = card,
                                selectedLabel = if (isForMe) "点击拿走" else null,
                                highlightColor = JadeEmerald,
                                onClick = { if (isForMe) onResolveGift(card.id) }
                            )
                        }
                    }
                }
                is HanaPendingOffer.CompetitionOffer -> {
                    Text(
                        text = if (isForMe) "$p2Name 发来【竞争】：请选择 A 组或 B 组" else "已向 $p2Name 发出【竞争】，等待对方挑选分组...",
                        style = MaterialTheme.typography.titleSmall,
                        color = ImperialGold
                    )
                    listOf(0 to offer.groupA, 1 to offer.groupB).forEach { (idx, groupCards) ->
                        val grpName = if (idx == 0) "A 组" else "B 组"
                        val summary = groupCards.joinToString(" + ") { "${it.geisha.title}(${it.geisha.charm}分)" }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LoungeSurface)
                                .border(1.dp, LoungeOutline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$grpName：$summary", style = MaterialTheme.typography.bodyMedium)
                            if (isForMe) {
                                Button(
                                    onClick = { onResolveCompetition(idx) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ImperialGold, contentColor = LoungeObsidian),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("hana_pick_group_$idx")
                                ) {
                                    Text("选 $grpName", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundResultCard(
    hanaState: HanamikojiState,
    p1Name: String,
    p2Name: String,
    onNextRound: () -> Unit,
    onRestartMatch: () -> Unit,
    onBackToLobby: () -> Unit
) {
    val reveal = hanaState.lastRoundReveal
    val isGameOver = hanaState.phase == HanaPhase.GAME_OVER
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LoungeSurfaceElevated),
        border = BorderStroke(2.dp, if (isGameOver) ImperialGold else KyotoSakura),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isGameOver) {
                    "对局结束 · ${if (hanaState.winnerSeat == 0) p1Name else p2Name} 获胜！"
                } else {
                    "第 ${hanaState.roundNumber} 轮结算"
                },
                style = MaterialTheme.typography.titleLarge,
                color = ImperialGold
            )
            Text(text = hanaState.winReason, style = MaterialTheme.typography.bodyMedium)
            if (reveal != null) {
                val p1Sec = reveal.secretCards[0]?.let { "${it.geisha.title}(${it.geisha.charm}分)" } ?: "无"
                val p2Sec = reveal.secretCards[1]?.let { "${it.geisha.title}(${it.geisha.charm}分)" } ?: "无"
                val rem = reveal.removedCard?.let { "${it.geisha.title}(${it.geisha.charm}分)" } ?: "无"
                Text(
                    text = "秘密牌揭晓：$p1Name=$p1Sec | $p2Name=$p2Sec\n本轮移出牌：$rem",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isGameOver) {
                    Button(
                        onClick = onNextRound,
                        colors = ButtonDefaults.buttonColors(containerColor = KyotoSakura, contentColor = LoungeObsidian),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hana_next_round_button")
                    ) {
                        Text("进入第 ${hanaState.roundNumber + 1} 轮", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onRestartMatch,
                        colors = ButtonDefaults.buttonColors(containerColor = ImperialGold, contentColor = LoungeObsidian),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hana_play_again_button")
                    ) {
                        Text("再来一局", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onBackToLobby,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("返回房间")
                }
            }
        }
    }
}

private fun geishaColor(geisha: Geisha): Color = when (geisha) {
    Geisha.PLUM -> GeishaPlum
    Geisha.WISTERIA -> GeishaWisteria
    Geisha.BAMBOO -> GeishaBamboo
    Geisha.CHRYSANTHEMUM -> GeishaChrys
    Geisha.SAKURA -> GeishaSakura
    Geisha.MAGNOLIA -> GeishaMagnolia
    Geisha.CRIMSON_MOON -> GeishaCrimson
}
