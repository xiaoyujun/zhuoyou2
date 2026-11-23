package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GameType
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.JadeEmerald
import com.example.ui.theme.KyotoSakura
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeOutline
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.LoungeSurfaceElevated
import com.example.ui.theme.MutedText
import com.example.viewmodel.TableBoxUiState

@Composable
fun HallScreen(
    uiState: TableBoxUiState,
    onUpdateHostName: (String) -> Unit,
    onCreateLanRoom: (GameType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(uiState.hostName) { mutableStateOf(uiState.hostName) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF28203B), LoungeObsidian),
                        center = Offset(size.width * 0.2f, size.height * 0.08f),
                        radius = size.maxDimension * 0.85f
                    )
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ImperialGold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "LAN TABLETOP BOX",
                                style = MaterialTheme.typography.labelSmall,
                                color = ImperialGold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "桌游盒子",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "P1 手机开房 · P2–P4 局域网网页免安装即玩",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }

                    Surface(
                        onClick = {
                            draftName = uiState.hostName
                            showNameDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = LoungeSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline),
                        modifier = Modifier.testTag("edit_host_name_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "修改房主昵称",
                                tint = ImperialGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = uiState.hostName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                LanGuideStrip()
            }

            item {
                GameShowcaseCard(
                    gameType = GameType.UNO,
                    bannerRes = R.drawable.img_uno_banner_1795457986236,
                    accentColor = ImperialGold,
                    tags = listOf("2–4 人联机", "局域网网页加入", "支持电脑 AI"),
                    description = "原汁原味 108 张 UNO 规则：功能牌、反转、+2/+4 攻防、大喊 UNO 与抓漏喊罚摸。",
                    createTag = "create_uno_room_button",
                    botTag = "quick_uno_bot_button",
                    onCreateLan = { onCreateLanRoom(GameType.UNO, false) },
                    onQuickBot = { onCreateLanRoom(GameType.UNO, true) }
                )
            }

            item {
                GameShowcaseCard(
                    gameType = GameType.HANAMIKOJI,
                    bannerRes = R.drawable.img_hanamikoji_banner_1795457986114,
                    accentColor = KyotoSakura,
                    tags = listOf("2 人心理对决", "七位京都艺伎", "经典四大行动"),
                    description = "京都花见小路双人策略名作：巧用秘密、弃置、赠礼与竞争，赢得 4 位艺伎或 11 点魅力值。",
                    createTag = "create_hana_room_button",
                    botTag = "quick_hana_bot_button",
                    onCreateLan = { onCreateLanRoom(GameType.HANAMIKOJI, false) },
                    onQuickBot = { onCreateLanRoom(GameType.HANAMIKOJI, true) }
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = LoungeSurface,
            title = { Text("修改房主 (P1) 昵称") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { if (it.length <= 16) draftName = it },
                    label = { Text("玩家昵称") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("host_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateHostName(draftName)
                        showNameDialog = false
                    },
                    modifier = Modifier.testTag("confirm_host_name_button")
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun LanGuideStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LoungeSurface.copy(alpha = 0.92f))
            .border(1.dp, LoungeOutline.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GuideStep(Icons.Default.Wifi, "同 Wi-Fi / 热点", JadeEmerald, Modifier.weight(1f))
        GuideStep(Icons.Default.QrCode2, "P2–P4 扫码入座", ImperialGold, Modifier.weight(1f))
        GuideStep(Icons.Default.Groups, "各自看手牌开玩", KyotoSakura, Modifier.weight(1f))
    }
}

@Composable
private fun GuideStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GameShowcaseCard(
    gameType: GameType,
    bannerRes: Int,
    accentColor: Color,
    tags: List<String>,
    description: String,
    createTag: String,
    botTag: String,
    onCreateLan: () -> Unit,
    onQuickBot: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LoungeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                Image(
                    painter = painterResource(id = bannerRes),
                    contentDescription = "${gameType.title} 封面插画",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    LoungeSurface.copy(alpha = 0.45f),
                                    LoungeSurface
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(LoungeObsidian.copy(alpha = 0.78f))
                        .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gameType.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = gameType.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gameType.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LoungeSurfaceElevated
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCreateLan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = LoungeObsidian
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(48.dp)
                            .testTag(createTag)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "创建局域网房间",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickBot,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag(botTag)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "带 AI 开房",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
