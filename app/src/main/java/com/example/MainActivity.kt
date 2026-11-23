package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.ui.screens.HallScreen
import com.example.ui.screens.HanamikojiGameScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.UnoGameScreen
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LoungeObsidian
import com.example.ui.theme.LoungeSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.TableBoxTheme
import com.example.viewmodel.TableBoxViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TableBoxTheme {
                TableBoxApp()
            }
        }
    }
}

@Composable
fun TableBoxApp(
    viewModel: TableBoxViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.screen) {
        AppScreen.HALL, AppScreen.RULES -> {
            Scaffold(
                containerColor = LoungeObsidian,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar(
                        containerColor = LoungeSurface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val onHall = uiState.screen == AppScreen.HALL
                        NavigationBarItem(
                            selected = onHall,
                            onClick = { viewModel.navigateTo(AppScreen.HALL) },
                            icon = {
                                Icon(
                                    imageVector = if (onHall) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                                    contentDescription = "游戏大厅"
                                )
                            },
                            label = { Text("游戏大厅") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LoungeObsidian,
                                selectedTextColor = ImperialGold,
                                indicatorColor = ImperialGold,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_hall_tab")
                        )

                        val onRules = uiState.screen == AppScreen.RULES
                        NavigationBarItem(
                            selected = onRules,
                            onClick = { viewModel.navigateTo(AppScreen.RULES) },
                            icon = {
                                Icon(
                                    imageVector = if (onRules) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = "规则手册"
                                )
                            },
                            label = { Text("规则与联机") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LoungeObsidian,
                                selectedTextColor = ImperialGold,
                                indicatorColor = ImperialGold,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_rules_tab")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (uiState.screen == AppScreen.HALL) {
                        HallScreen(
                            uiState = uiState,
                            onUpdateHostName = viewModel::updateHostName,
                            onCreateLanRoom = viewModel::createLanRoom
                        )
                    } else {
                        RulesScreen()
                    }
                }
            }
        }

        AppScreen.LOBBY -> {
            LobbyScreen(
                uiState = uiState,
                onRefreshIp = viewModel::refreshNetworkAddresses,
                onSelectIp = viewModel::selectAddressIndex,
                onAddBot = viewModel::addBotPlayer,
                onRemoveSeat = viewModel::removeSeat,
                onStartGame = viewModel::startGame,
                onCloseRoom = viewModel::closeRoom
            )
        }

        AppScreen.GAME_UNO -> {
            val unoState = uiState.unoState
            if (unoState != null) {
                UnoGameScreen(
                    unoState = unoState,
                    seats = uiState.seats,
                    statusMessage = uiState.statusMessage,
                    onPlayCard = viewModel::hostPlayUnoCard,
                    onDrawCard = viewModel::hostDrawUnoCard,
                    onPassTurn = viewModel::hostPassUnoTurn,
                    onCallUno = viewModel::hostCallUno,
                    onCatchUno = viewModel::hostCatchUno,
                    onNextRound = { viewModel.restartCurrentGame(nextUnoRound = true) },
                    onRestartFresh = { viewModel.restartCurrentGame(nextUnoRound = false) },
                    onBackToLobby = viewModel::returnToLobby
                )
            } else {
                viewModel.returnToLobby()
            }
        }

        AppScreen.GAME_HANAMIKOJI -> {
            val hanaState = uiState.hanamikojiState
            if (hanaState != null) {
                HanamikojiGameScreen(
                    hanaState = hanaState,
                    seats = uiState.seats,
                    statusMessage = uiState.statusMessage,
                    onSecret = viewModel::hostHanaSecret,
                    onDiscard = viewModel::hostHanaDiscard,
                    onGift = viewModel::hostHanaGift,
                    onCompetition = viewModel::hostHanaCompetition,
                    onResolveGift = viewModel::hostHanaResolveGift,
                    onResolveCompetition = viewModel::hostHanaResolveCompetition,
                    onNextRound = viewModel::advanceHanamikojiRound,
                    onRestartMatch = { viewModel.restartCurrentGame(nextUnoRound = false) },
                    onBackToLobby = viewModel::returnToLobby
                )
            } else {
                viewModel.returnToLobby()
            }
        }
    }
}
