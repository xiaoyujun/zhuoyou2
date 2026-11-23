package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.uno.UnoCard
import com.example.game.uno.UnoColor
import com.example.game.uno.UnoEngine
import com.example.game.uno.UnoState
import com.example.model.PlayerSeat
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.JadeEmerald
import com.example.ui.theme.LanternVermilion
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeOutline
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.LoungeSurfaceElevated
import com.example.ui.theme.MutedText
import com.example.ui.theme.UnoBlue
import com.example.ui.theme.UnoBlueDark
import com.example.ui.theme.UnoGreen
import com.example.ui.theme.UnoGreenDark
import com.example.ui.theme.UnoRed
import com.example.ui.theme.UnoRedDark
import com.example.ui.theme.UnoWildDark
import com.example.ui.theme.UnoYellow
import com.example.ui.theme.UnoYellowDark

@Composable
fun UnoGameScreen(
    unoState: UnoState,
    seats: List<PlayerSeat>,
    statusMessage: String?,
    onPlayCard: (String, UnoColor?) -> Unit,
    onDrawCard: () -> Unit,
    onPassTurn: () -> Unit,
    onCallUno: () -> Unit,
    onCatchUno: (Int) -> Unit,
    onNextRound: () -> Unit,
    onRestartFresh: () -> Unit,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToLobby() }

    var pendingWildCard by remember { mutableStateOf<UnoCard?>(null) }
    val seatMap = remember(seats) { seats.associateBy { it.seatIndex } }
    val activeAccent = unoColorPrimary(unoState.activeColor)
    val myHand = unoState.hands[0].orEmpty()
    val isMyTurn = unoState.currentTurnSeat == 0 && !unoState.isFinished
    val canCallUno = !unoState.isFinished && myHand.size in 1..2 && 0 !in unoState.unoCalledSeats

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(LoungeObsidian)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(activeAccent.copy(alpha = 0.24f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.44f),
                        radius = size.minDimension * 0.72f
                    ),
                    radius = size.minDimension * 0.72f,
                    center = Offset(size.width * 0.5f, size.height * 0.44f)
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
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar + Event + Opponents
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToLobby,
                        modifier = Modifier.testTag("uno_back_lobby_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回局域网房间",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "UNO · 第 ${unoState.roundNumber} 局",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (unoState.direction > 0) "出牌方向：顺时针 ↻" else "出牌方向：逆时针 ↺",
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedText
                        )
                    }
                    IconButton(
                        onClick = onRestartFresh,
                        modifier = Modifier.testTag("uno_restart_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重新发牌",
                            tint = ImperialGold
                        )
                    }
                }

                // Event ticker
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LoungeSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: unoState.lastEvent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }

                // Opponents strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opponents = unoState.seatOrder.filter { it != 0 }
                    opponents.forEach { seatIdx ->
                        val seat = seatMap[seatIdx]
                        val count = unoState.hands[seatIdx]?.size ?: 0
                        val isTurn = unoState.currentTurnSeat == seatIdx && !unoState.isFinished
                        val calledUno = seatIdx in unoState.unoCalledSeats
                        val canCatch = count == 1 && !calledUno && !unoState.isFinished

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isTurn) LoungeSurfaceElevated else LoungeSurface,
                            border = BorderStroke(
                                width = if (isTurn) 2.dp else 1.dp,
                                color = if (isTurn) ImperialGold else LoungeOutline
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${seat?.seatTag ?: "P${seatIdx + 1}"} ${seat?.name ?: ""}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isTurn) ImperialGold else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$count 张牌 · ${unoState.scores[seatIdx] ?: 0}分",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MutedText
                                )
                                if (calledUno && count == 1) {
                                    Text(
                                        text = "已喊 UNO!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JadeEmerald
                                    )
                                } else if (canCatch) {
                                    Button(
                                        onClick = { onCatchUno(seatIdx) },
                                        colors = ButtonDefaults.buttonColors(containerColor = LanternVermilion),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("catch_uno_$seatIdx")
                                    ) {
                                        Text("抓 UNO!", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Center Table (Draw Pile + Discard Pile + Active Color)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Draw Pile
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(width = 88.dp, height = 132.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF322B4A), UnoWildDark))
                                )
                                .border(
                                    width = if (isMyTurn && !unoState.hasDrawnThisTurn) 2.5.dp else 1.5.dp,
                                    color = if (isMyTurn && !unoState.hasDrawnThisTurn) ImperialGold else Color.White.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = isMyTurn && !unoState.hasDrawnThisTurn) { onDrawCard() }
                                .testTag("uno_draw_pile"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Style,
                                    contentDescription = "牌堆摸牌",
                                    tint = ImperialGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${unoState.drawPile.size}",
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isMyTurn && !unoState.hasDrawnThisTurn) "点此摸牌" else "剩余牌堆",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedText
                                )
                            }
                        }
                    }

                    // Active Top Discard Card
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UnoCardView(
                            card = unoState.topCard,
                            playable = false,
                            dimmed = false,
                            large = true,
                            onClick = {}
                        )
                    }
                }

                // Active Color + Turn indicator pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = activeAccent.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, activeAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeAccent)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "当前花色：${unoState.activeColor.label}色",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isMyTurn) ImperialGold else LoungeSurfaceElevated
                    ) {
                        val turnSeat = seatMap[unoState.currentTurnSeat]
                        Text(
                            text = if (isMyTurn) "轮到你出牌！" else "等待 ${turnSeat?.name ?: "对手"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isMyTurn) LoungeObsidian else MutedText,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom: Controls + P1 Hand
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMyTurn && !unoState.hasDrawnThisTurn) {
                        OutlinedButton(
                            onClick = onDrawCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("uno_draw_button")
                        ) {
                            Text("摸一张牌")
                        }
                    }
                    if (isMyTurn && unoState.hasDrawnThisTurn) {
                        OutlinedButton(
                            onClick = onPassTurn,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("uno_pass_button")
                        ) {
                            Text("跳过出牌")
                        }
                    }
                    if (canCallUno) {
                        Button(
                            onClick = onCallUno,
                            colors = ButtonDefaults.buttonColors(containerColor = LanternVermilion),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("uno_call_button")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("大喊 UNO!", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // My Hand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "我的手牌 (P1 · ${myHand.size} 张)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "我的总分：${unoState.scores[0] ?: 0}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ImperialGold
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(myHand, key = { it.id }) { card ->
                        val playable = UnoEngine.canPlayCard(unoState, 0, card)
                        UnoCardView(
                            card = card,
                            playable = playable,
                            dimmed = isMyTurn && !playable,
                            large = false,
                            onClick = {
                                if (playable) {
                                    if (card.value.isWild) {
                                        pendingWildCard = card
                                    } else {
                                        onPlayCard(card.id, null)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Wild Color Picker Dialog
        if (pendingWildCard != null) {
            AlertDialog(
                onDismissRequest = { pendingWildCard = null },
                containerColor = LoungeSurface,
                title = { Text("指定万能牌颜色") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WildColorChoiceButton("红色", UnoRed, Color.White, Modifier.weight(1f)) {
                                val cid = pendingWildCard?.id ?: return@WildColorChoiceButton
                                pendingWildCard = null
                                onPlayCard(cid, UnoColor.RED)
                            }
                            WildColorChoiceButton("黄色", UnoYellow, LoungeObsidian, Modifier.weight(1f)) {
                                val cid = pendingWildCard?.id ?: return@WildColorChoiceButton
                                pendingWildCard = null
                                onPlayCard(cid, UnoColor.YELLOW)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WildColorChoiceButton("绿色", UnoGreen, Color.White, Modifier.weight(1f)) {
                                val cid = pendingWildCard?.id ?: return@WildColorChoiceButton
                                pendingWildCard = null
                                onPlayCard(cid, UnoColor.GREEN)
                            }
                            WildColorChoiceButton("蓝色", UnoBlue, Color.White, Modifier.weight(1f)) {
                                val cid = pendingWildCard?.id ?: return@WildColorChoiceButton
                                pendingWildCard = null
                                onPlayCard(cid, UnoColor.BLUE)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { pendingWildCard = null }) { Text("取消") }
                }
            )
        }

        // Winner Overlay Dialog
        if (unoState.isFinished) {
            val winSeat = seatMap[unoState.winnerSeat]
            AlertDialog(
                onDismissRequest = {},
                containerColor = LoungeSurface,
                title = {
                    Text(
                        text = "本局胜者：${winSeat?.name ?: "玩家"}",
                        color = ImperialGold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("本局赢得 ${unoState.lastRoundPoints} 分！累计积分榜：")
                        unoState.seatOrder.forEach { idx ->
                            val s = seatMap[idx]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${s?.seatTag ?: "P${idx + 1}"} ${s?.name ?: ""}")
                                Text(
                                    text = "${unoState.scores[idx] ?: 0} 分",
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGold
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onNextRound,
                        colors = ButtonDefaults.buttonColors(containerColor = ImperialGold, contentColor = LoungeObsidian),
                        modifier = Modifier.testTag("uno_next_round_button")
                    ) {
                        Text("下一局")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBackToLobby) {
                        Text("返回房间")
                    }
                }
            )
        }
    }
}

@Composable
private fun WildColorChoiceButton(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("wild_color_$label")
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UnoCardView(
    card: UnoCard,
    playable: Boolean,
    dimmed: Boolean,
    large: Boolean,
    onClick: () -> Unit
) {
    val lift by animateDpAsState(
        targetValue = if (playable) (-8).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "uno_card_lift"
    )
    val (primary, secondary) = unoColorPair(card.color)
    val textColor = if (card.color == UnoColor.YELLOW) Color(0xFF1F1604) else Color.White
    val cardWidth = if (large) 92.dp else 76.dp
    val cardHeight = if (large) 136.dp else 114.dp

    Card(
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(
            width = if (playable) 2.5.dp else 1.5.dp,
            color = if (playable) ImperialGold else Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (playable) 10.dp else 4.dp),
        modifier = Modifier
            .offset(y = lift)
            .size(width = cardWidth, height = cardHeight)
            .alpha(if (dimmed) 0.42f else 1f)
            .clickable(enabled = playable) { onClick() }
            .testTag("uno_card_${card.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(primary, secondary)))
                .padding(7.dp)
        ) {
            // Slanted inner oval motif
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = cardWidth * 0.72f, height = cardHeight * 0.56f)
                    .rotate(-22f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (card.color == UnoColor.YELLOW) 0.35f else 0.16f))
            )

            Text(
                text = card.value.symbol,
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (large) 14.sp else 12.sp,
                color = textColor,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = card.value.symbol,
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (large) 34.sp else 27.sp,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = card.value.label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

private fun unoColorPrimary(color: UnoColor): Color = when (color) {
    UnoColor.RED -> UnoRed
    UnoColor.YELLOW -> UnoYellow
    UnoColor.GREEN -> UnoGreen
    UnoColor.BLUE -> UnoBlue
    UnoColor.WILD -> ImperialGold
}

private fun unoColorPair(color: UnoColor): Pair<Color, Color> = when (color) {
    UnoColor.RED -> UnoRed to UnoRedDark
    UnoColor.YELLOW -> UnoYellow to UnoYellowDark
    UnoColor.GREEN -> UnoGreen to UnoGreenDark
    UnoColor.BLUE -> UnoBlue to UnoBlueDark
    UnoColor.WILD -> Color(0xFF342C4F) to UnoWildDark
}
