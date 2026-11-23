package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.JadeEmerald
import com.example.ui.theme.KyotoSakura
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeOutline
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.MutedText

@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoungeObsidian),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "规则与联机指南",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "无需联网服务器，局域网网页即扫即玩",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
            }

            item {
                RuleSectionCard(
                    icon = Icons.Default.Wifi,
                    title = "局域网网页联机方法",
                    accent = JadeEmerald,
                    bullets = listOf(
                        "1. 确保房主手机与 P2/P3/P4 手机连接同一个 Wi-Fi，或直接开启房主手机热点让朋友连接。",
                        "2. 房主在大厅点击【创建局域网房间】，屏幕会自动生成局域网网址、二维码与 4 位房间口令。",
                        "3. 朋友使用微信/相机扫码或在浏览器输入网址，输入昵称即可加入，各自手牌完全私密隔离。",
                        "4. 也可在座位中随时添加电脑 AI 补位，单人也能畅玩两款桌游。"
                    )
                )
            }

            item {
                RuleSectionCard(
                    icon = Icons.Default.Casino,
                    title = "UNO 经典纸牌 (2–4人)",
                    accent = ImperialGold,
                    bullets = listOf(
                        "• 起始每人 7 张牌，出牌必须与牌堆顶部的颜色或数字/符号相同，万能牌可随时打出并指定新颜色。",
                        "• 功能牌：跳过(⊘)令下家跳过；反转(⇄)改变出牌方向（2人局等同于跳过）；+2 与万能 +4 让下家罚摸并跳过。",
                        "• 无牌可出时点【摸牌】：若摸到的牌恰好可出，可立即打出或点【跳过出牌】。",
                        "• 喊 UNO：手牌剩 1–2 张时记得点【喊 UNO!】，否则其他玩家可点击【抓没喊 UNO】罚你摸 2 张！"
                    )
                )
            }

            item {
                RuleSectionCard(
                    icon = Icons.Default.FilterVintage,
                    title = "花间小路 Hanamikoji (2人)",
                    accent = KyotoSakura,
                    bullets = listOf(
                        "• 桌面上有 7 位艺伎，魅力值分别为 2、2、2、3、3、4、5，对应 21 张物品牌。每轮开始暗置移除 1 张，每人发 6 张。",
                        "• 轮到你时先自动摸 1 张牌，然后从 4 个行动标记中选 1 个执行（每轮各只能用 1 次）：",
                        "  ① 秘密：选 1 张暗置保留，轮末亮出计入己方。",
                        "  ② 弃置：选 2 张暗置移除，本轮作废。",
                        "  ③ 赠礼：展示 3 张牌，对手挑走 1 张，其余 2 张归你。",
                        "  ④ 竞争：选 4 张平分为 A/B 两组，对手挑走 1 组，另 1 组归你。",
                        "• 获胜条件：每轮末按每位艺伎两侧物品数决定好感归属（平局维持原状）。率先赢得 ≥4 位艺伎 或 ≥11 点总魅力值即胜！"
                    )
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RuleSectionCard(
    icon: ImageVector,
    title: String,
    accent: Color,
    bullets: List<String>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LoungeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LoungeOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accent.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            bullets.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
    }
}
