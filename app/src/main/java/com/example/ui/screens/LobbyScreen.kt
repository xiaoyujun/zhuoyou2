package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GameType
import com.example.model.PlayerSeat
import com.example.model.PlayerType
import com.example.network.QrCodeGenerator
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.JadeEmerald
import com.example.ui.theme.KyotoSakura
import com.example.ui.theme.LanternVermilion
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeOutline
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.LoungeSurfaceElevated
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.MutedText
import com.example.viewmodel.TableBoxUiState

@Composable
fun LobbyScreen(
    uiState: TableBoxUiState,
    onRefreshIp: () -> Unit,
    onSelectIp: (Int) -> Unit,
    onAddBot: () -> Unit,
    onRemoveSeat: (Int) -> Unit,
    onStartGame: () -> Unit,
    onCloseRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onCloseRoom() }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }

    val game = uiState.activeGameType ?: GameType.UNO
    val accent = if (game == GameType.UNO) ImperialGold else KyotoSakura
    val qrBitmap = remember(uiState.qrJoinUrl) {
        QrCodeGenerator.generateQrBitmap(uiState.qrJoinUrl, 512)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LoungeObsidian)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCloseRoom,
                modifier = Modifier.testTag("close_room_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "解散房间并返回大厅",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${game.title} · 局域网房间",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.serverRunning) JadeEmerald else LanternVermilion)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (uiState.serverRunning) "局域网网页服务运行中 (:${uiState.serverPort})" else "服务未启动",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.serverRunning) JadeEmerald else LanternVermilion
                    )
                }
            }
            IconButton(
                onClick = onRefreshIp,
                modifier = Modifier.testTag("refresh_ip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新局域网 IP",
                    tint = accent
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!uiState.statusMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accent.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // QR & LAN Connection Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LoungeSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Scannable QR Code
                            Box(
                                modifier = Modifier
                                    .size(138.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF6F1E7))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "局域网加入二维码",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "P2–P${game.maxPlayers} 手机浏览器扫码加入",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = accent
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = LoungeObsidian,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                        Text(
                                            text = "局域网网址",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedText
                                        )
                                        Text(
                                            text = uiState.joinUrl,
                                            fontFamily = MonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accent.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "房间口令",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accent
                                        )
                                        Text(
                                            text = uiState.roomCode,
                                            fontFamily = MonoFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = accent,
                                            modifier = Modifier.testTag("room_pin_text")
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.availableAddresses.size > 1) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(uiState.availableAddresses) { idx, option ->
                                    FilterChip(
                                        selected = idx == uiState.selectedAddressIndex,
                                        onClick = { onSelectIp(idx) },
                                        label = { Text("${option.displayLabel}: ${option.ipAddress}") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Wifi,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(uiState.qrJoinUrl))
                                    copiedFeedback = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("copy_lan_url_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (copiedFeedback) "已复制链接" else "复制加入链接")
                            }

                            OutlinedButton(
                                onClick = {
                                    val url = uiState.localPreviewUrl.ifEmpty { uiState.qrJoinUrl }
                                    if (url.isNotEmpty()) {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (_: Exception) {
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_web_preview_button")
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("本机网页预览")
                            }
                        }
                    }
                }
            }

            // Player Seats Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "玩家座位 (${uiState.seats.size}/${game.maxPlayers})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.seats.size < game.maxPlayers) {
                        OutlinedButton(
                            onClick = onAddBot,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("add_bot_button")
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("添加电脑 AI")
                        }
                    }
                }
            }

            items(game.maxPlayers) { slot ->
                val seat = uiState.seats.firstOrNull { it.seatIndex == slot }
                SeatRowCard(
                    seatIndex = slot,
                    seat = seat,
                    accent = accent,
                    onRemove = { onRemoveSeat(slot) }
                )
            }
        }

        // Bottom Primary Start CTA
        Surface(
            color = LoungeSurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onStartGame,
                    enabled = uiState.canStartGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = LoungeObsidian
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 608.dp)
                        .height(52.dp)
                        .testTag("start_game_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.canStartGame) {
                            "开始 ${game.title} (${uiState.seats.size}人)"
                        } else {
                            "至少需要 ${game.minPlayers} 名玩家 (可扫码或添加 AI)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatRowCard(
    seatIndex: Int,
    seat: PlayerSeat?,
    accent: Color,
    onRemove: () -> Unit
) {
    val seatTag = "P${seatIndex + 1}"
    val isOccupied = seat != null
    val roleLabel = when (seat?.type) {
        PlayerType.HOST -> "房主 · 本机安卓"
        PlayerType.WEB_CLIENT -> "局域网网页已连接"
        PlayerType.BOT -> "智能电脑 AI"
        null -> "等待局域网玩家扫码加入..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isOccupied) LoungeSurfaceElevated else LoungeSurface.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = if (seat?.type == PlayerType.WEB_CLIENT) JadeEmerald.copy(alpha = 0.7f) else LoungeOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isOccupied) accent.copy(alpha = 0.2f) else LoungeObsidian),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seatTag,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOccupied) accent else MutedText
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = seat?.name ?: "空座位",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isOccupied) MaterialTheme.colorScheme.onSurface else MutedText
                    )
                    if (seat?.type == PlayerType.WEB_CLIENT) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(JadeEmerald)
                        )
                    }
                }
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (seat?.type == PlayerType.WEB_CLIENT) JadeEmerald else MutedText
                )
            }
        }

        if (seat != null && seat.seatIndex > 0) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("remove_seat_${seat.seatIndex}_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移出 $seatTag",
                    tint = MutedText
                )
            }
        } else if (seat == null) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = LoungeOutline,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
