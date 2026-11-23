package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.hanamikoji.HanaBotDecision
import com.example.game.hanamikoji.HanaMoveResult
import com.example.game.hanamikoji.HanaPhase
import com.example.game.hanamikoji.HanamikojiEngine
import com.example.game.hanamikoji.HanamikojiState
import com.example.game.uno.UnoBotDecision
import com.example.game.uno.UnoColor
import com.example.game.uno.UnoEngine
import com.example.game.uno.UnoMoveResult
import com.example.game.uno.UnoState
import com.example.model.AppScreen
import com.example.model.GameType
import com.example.model.NetworkAddressOption
import com.example.model.PlayerSeat
import com.example.model.PlayerType
import com.example.model.RoomPhase
import com.example.network.ApiResponse
import com.example.network.LanGameServer
import com.example.network.LanRequestHandler
import com.example.network.LanUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

data class TableBoxUiState(
    val screen: AppScreen = AppScreen.HALL,
    val hostName: String = "房主",
    val activeGameType: GameType? = null,
    val roomPhase: RoomPhase = RoomPhase.LOBBY,
    val roomCode: String = "",
    val serverRunning: Boolean = false,
    val serverPort: Int = 0,
    val availableAddresses: List<NetworkAddressOption> = emptyList(),
    val selectedAddressIndex: Int = 0,
    val seats: List<PlayerSeat> = emptyList(),
    val unoState: UnoState? = null,
    val hanamikojiState: HanamikojiState? = null,
    val statusMessage: String? = null
) {
    val selectedAddress: NetworkAddressOption?
        get() = availableAddresses.getOrNull(selectedAddressIndex) ?: availableAddresses.firstOrNull()

    val joinUrl: String
        get() {
            val ip = selectedAddress?.ipAddress ?: "127.0.0.1"
            return if (serverPort > 0) "http://$ip:$serverPort" else ""
        }

    val qrJoinUrl: String
        get() = if (joinUrl.isNotEmpty() && roomCode.isNotEmpty()) "$joinUrl/?pin=$roomCode" else joinUrl

    val localPreviewUrl: String
        get() = if (serverPort > 0 && roomCode.isNotEmpty()) "http://127.0.0.1:$serverPort/?pin=$roomCode" else ""

    val canStartGame: Boolean
        get() {
            val game = activeGameType ?: return false
            return seats.size in game.minPlayers..game.maxPlayers
        }
}

class TableBoxViewModel : ViewModel(), LanRequestHandler {

    private val lock = Any()
    private val secureRandom = SecureRandom()
    private val lanServer = LanGameServer(this)
    private var botJob: Job? = null

    private val _uiState = MutableStateFlow(
        TableBoxUiState(availableAddresses = LanUtils.discoverLanAddresses())
    )
    val uiState: StateFlow<TableBoxUiState> = _uiState.asStateFlow()

    fun updateHostName(rawName: String) {
        val clean = sanitizeNickname(rawName, fallback = "房主")
        synchronized(lock) {
            _uiState.update { st ->
                val updatedSeats = st.seats.map {
                    if (it.seatIndex == 0) it.copy(name = clean) else it
                }
                st.copy(hostName = clean, seats = updatedSeats)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(screen = screen, statusMessage = null) }
    }

    fun refreshNetworkAddresses() {
        val addresses = LanUtils.discoverLanAddresses()
        _uiState.update { st ->
            st.copy(
                availableAddresses = addresses,
                selectedAddressIndex = st.selectedAddressIndex.coerceIn(0, (addresses.size - 1).coerceAtLeast(0))
            )
        }
    }

    fun selectAddressIndex(index: Int) {
        _uiState.update { st ->
            if (index in st.availableAddresses.indices) st.copy(selectedAddressIndex = index) else st
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun createLanRoom(gameType: GameType, autoAddBot: Boolean = false) {
        synchronized(lock) {
            cancelBotLoop()
            val port = try {
                lanServer.start(8088)
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = e.message ?: "局域网服务启动失败") }
                return
            }

            val addresses = LanUtils.discoverLanAddresses()
            val pin = generateRoomCode()
            val hostName = sanitizeNickname(_uiState.value.hostName, "房主")
            val initialSeats = mutableListOf(
                PlayerSeat(
                    seatIndex = 0,
                    id = "host_p1",
                    name = hostName,
                    type = PlayerType.HOST
                )
            )
            if (autoAddBot) {
                initialSeats += PlayerSeat(
                    seatIndex = 1,
                    id = "bot_1",
                    name = "电脑 P2",
                    type = PlayerType.BOT
                )
            }

            _uiState.update {
                it.copy(
                    screen = AppScreen.LOBBY,
                    activeGameType = gameType,
                    roomPhase = RoomPhase.LOBBY,
                    roomCode = pin,
                    serverRunning = true,
                    serverPort = port,
                    availableAddresses = addresses,
                    selectedAddressIndex = 0,
                    seats = initialSeats,
                    unoState = null,
                    hanamikojiState = null,
                    statusMessage = "已创建 ${gameType.title} 局域网房间，端口 $port"
                )
            }
        }
    }

    fun addBotPlayer() {
        synchronized(lock) {
            val st = _uiState.value
            val game = st.activeGameType ?: return
            if (st.roomPhase != RoomPhase.LOBBY) return
            val nextIndex = (1 until game.maxPlayers).firstOrNull { idx ->
                st.seats.none { it.seatIndex == idx }
            } ?: return

            val botNames = listOf("京都棋手", "纸牌大师", "夜樱智囊")
            val botName = "${botNames[(nextIndex - 1) % botNames.size]} (AI)"
            val newSeat = PlayerSeat(
                seatIndex = nextIndex,
                id = "bot_$nextIndex",
                name = botName,
                type = PlayerType.BOT
            )
            _uiState.update { cur ->
                cur.copy(seats = (cur.seats + newSeat).sortedBy { it.seatIndex })
            }
        }
    }

    fun removeSeat(seatIndex: Int) {
        if (seatIndex == 0) return
        synchronized(lock) {
            val st = _uiState.value
            if (st.roomPhase != RoomPhase.LOBBY) return
            _uiState.update { cur ->
                cur.copy(seats = cur.seats.filterNot { it.seatIndex == seatIndex })
            }
        }
    }

    fun startGame() {
        synchronized(lock) {
            val st = _uiState.value
            val game = st.activeGameType ?: return
            if (st.seats.size !in game.minPlayers..game.maxPlayers) {
                _uiState.update { it.copy(statusMessage = "人数需在 ${game.minPlayers}–${game.maxPlayers} 人之间") }
                return
            }

            when (game) {
                GameType.UNO -> {
                    val uno = UnoEngine.newGame(seats = st.seats)
                    _uiState.update {
                        it.copy(
                            roomPhase = RoomPhase.PLAYING,
                            screen = AppScreen.GAME_UNO,
                            unoState = uno,
                            hanamikojiState = null,
                            statusMessage = null
                        )
                    }
                }
                GameType.HANAMIKOJI -> {
                    val names = st.seats.associate { it.seatIndex to it.name }
                    val hana = HanamikojiEngine.newMatch(firstPlayerSeat = 0, seatNames = names)
                    _uiState.update {
                        it.copy(
                            roomPhase = RoomPhase.PLAYING,
                            screen = AppScreen.GAME_HANAMIKOJI,
                            hanamikojiState = hana,
                            unoState = null,
                            statusMessage = null
                        )
                    }
                }
            }
            scheduleBotIfNeeded()
        }
    }

    fun restartCurrentGame(nextUnoRound: Boolean = false) {
        synchronized(lock) {
            val st = _uiState.value
            val game = st.activeGameType ?: return
            when (game) {
                GameType.UNO -> {
                    val prev = st.unoState
                    val uno = if (nextUnoRound && prev != null) {
                        UnoEngine.newGame(
                            seats = st.seats,
                            previousScores = prev.scores,
                            roundNumber = prev.roundNumber + 1
                        )
                    } else {
                        UnoEngine.newGame(seats = st.seats)
                    }
                    _uiState.update { it.copy(unoState = uno, statusMessage = null) }
                }
                GameType.HANAMIKOJI -> {
                    val names = st.seats.associate { it.seatIndex to it.name }
                    val hana = HanamikojiEngine.newMatch(firstPlayerSeat = 0, seatNames = names)
                    _uiState.update { it.copy(hanamikojiState = hana, statusMessage = null) }
                }
            }
            scheduleBotIfNeeded()
        }
    }

    fun advanceHanamikojiRound() {
        synchronized(lock) {
            val st = _uiState.value
            val hana = st.hanamikojiState ?: return
            val names = st.seats.associate { it.seatIndex to it.name }
            when (val res = HanamikojiEngine.startNextRound(hana, names)) {
                is HanaMoveResult.Success -> {
                    _uiState.update { it.copy(hanamikojiState = res.state, statusMessage = null) }
                    scheduleBotIfNeeded()
                }
                is HanaMoveResult.Failure -> {
                    _uiState.update { it.copy(statusMessage = res.message) }
                }
            }
        }
    }

    fun returnToLobby() {
        synchronized(lock) {
            cancelBotLoop()
            _uiState.update {
                it.copy(
                    roomPhase = RoomPhase.LOBBY,
                    screen = AppScreen.LOBBY,
                    unoState = null,
                    hanamikojiState = null,
                    statusMessage = null
                )
            }
        }
    }

    fun closeRoom() {
        synchronized(lock) {
            cancelBotLoop()
            lanServer.stop()
            _uiState.update {
                it.copy(
                    screen = AppScreen.HALL,
                    activeGameType = null,
                    roomPhase = RoomPhase.LOBBY,
                    roomCode = "",
                    serverRunning = false,
                    serverPort = 0,
                    seats = emptyList(),
                    unoState = null,
                    hanamikojiState = null,
                    statusMessage = null
                )
            }
        }
    }

    // --- Host (P1) UNO actions ---
    fun hostPlayUnoCard(cardId: String, chosenColor: UnoColor? = null) {
        applyUnoAction(0) { state, names ->
            UnoEngine.playCard(state, 0, cardId, chosenColor, names)
        }
    }

    fun hostDrawUnoCard() {
        applyUnoAction(0) { state, names -> UnoEngine.drawCard(state, 0, names) }
    }

    fun hostPassUnoTurn() {
        applyUnoAction(0) { state, names -> UnoEngine.passTurn(state, 0, names) }
    }

    fun hostCallUno() {
        applyUnoAction(0) { state, names -> UnoEngine.callUno(state, 0, names) }
    }

    fun hostCatchUno(targetSeat: Int) {
        applyUnoAction(0) { state, names -> UnoEngine.catchUno(state, 0, targetSeat, names) }
    }

    // --- Host (P1) Hanamikoji actions ---
    fun hostHanaSecret(cardId: String) {
        applyHanaAction { state, names -> HanamikojiEngine.performSecret(state, 0, cardId, names) }
    }

    fun hostHanaDiscard(cardIds: List<String>) {
        applyHanaAction { state, names -> HanamikojiEngine.performDiscard(state, 0, cardIds, names) }
    }

    fun hostHanaGift(cardIds: List<String>) {
        applyHanaAction { state, names -> HanamikojiEngine.performGift(state, 0, cardIds, names) }
    }

    fun hostHanaCompetition(groupA: List<String>, groupB: List<String>) {
        applyHanaAction { state, names -> HanamikojiEngine.performCompetition(state, 0, groupA, groupB, names) }
    }

    fun hostHanaResolveGift(cardId: String) {
        applyHanaAction { state, names -> HanamikojiEngine.resolveGift(state, 0, cardId, names) }
    }

    fun hostHanaResolveCompetition(group: Int) {
        applyHanaAction { state, names -> HanamikojiEngine.resolveCompetition(state, 0, group, names) }
    }

    private fun applyUnoAction(
        seat: Int,
        action: (UnoState, Map<Int, String>) -> UnoMoveResult
    ): String? {
        synchronized(lock) {
            val st = _uiState.value
            val uno = st.unoState ?: return "当前未在进行 UNO 对局"
            val names = st.seats.associate { it.seatIndex to it.name }
            return when (val res = action(uno, names)) {
                is UnoMoveResult.Success -> {
                    _uiState.update { it.copy(unoState = res.state, statusMessage = null) }
                    scheduleBotIfNeeded()
                    null
                }
                is UnoMoveResult.Failure -> {
                    if (seat == 0) _uiState.update { it.copy(statusMessage = res.message) }
                    res.message
                }
            }
        }
    }

    private fun applyHanaAction(
        action: (HanamikojiState, Map<Int, String>) -> HanaMoveResult
    ): String? {
        synchronized(lock) {
            val st = _uiState.value
            val hana = st.hanamikojiState ?: return "当前未在进行花间小路对局"
            val names = st.seats.associate { it.seatIndex to it.name }
            return when (val res = action(hana, names)) {
                is HanaMoveResult.Success -> {
                    _uiState.update { it.copy(hanamikojiState = res.state, statusMessage = null) }
                    scheduleBotIfNeeded()
                    null
                }
                is HanaMoveResult.Failure -> {
                    _uiState.update { it.copy(statusMessage = res.message) }
                    res.message
                }
            }
        }
    }

    private fun cancelBotLoop() {
        botJob?.cancel()
        botJob = null
    }

    private fun scheduleBotIfNeeded() {
        cancelBotLoop()
        botJob = viewModelScope.launch {
            while (true) {
                delay(900)
                val shouldContinue = executeSingleBotStep()
                if (!shouldContinue) break
            }
        }
    }

    private fun executeSingleBotStep(): Boolean {
        synchronized(lock) {
            val st = _uiState.value
            if (st.roomPhase != RoomPhase.PLAYING) return false
            val seatMap = st.seats.associateBy { it.seatIndex }
            val names = st.seats.associate { it.seatIndex to it.name }

            st.unoState?.let { uno ->
                if (uno.isFinished) return false
                val seat = seatMap[uno.currentTurnSeat] ?: return false
                if (seat.type != PlayerType.BOT) return false

                // Bot calls UNO automatically before playing down to 1 card
                var working = uno
                val handSize = working.hands[seat.seatIndex]?.size ?: 0
                if (handSize == 2 && seat.seatIndex !in working.unoCalledSeats) {
                    val callRes = UnoEngine.callUno(working, seat.seatIndex, names)
                    if (callRes is UnoMoveResult.Success) working = callRes.state
                }

                val res = when (val move = UnoEngine.computeBotMove(working, seat.seatIndex)) {
                    is UnoBotDecision.Play -> UnoEngine.playCard(working, seat.seatIndex, move.cardId, move.chosenColor, names)
                    UnoBotDecision.Draw -> UnoEngine.drawCard(working, seat.seatIndex, names)
                    UnoBotDecision.Pass -> UnoEngine.passTurn(working, seat.seatIndex, names)
                }
                if (res is UnoMoveResult.Success) {
                    _uiState.update { it.copy(unoState = res.state) }
                    val nextSeat = seatMap[res.state.currentTurnSeat]
                    return !res.state.isFinished && nextSeat?.type == PlayerType.BOT
                }
                return false
            }

            st.hanamikojiState?.let { hana ->
                if (hana.phase == HanaPhase.ROUND_SUMMARY || hana.phase == HanaPhase.GAME_OVER) return false
                val actingSeatIndex = when (hana.phase) {
                    HanaPhase.CHOOSE_ACTION -> hana.currentTurnSeat
                    HanaPhase.WAITING_OPPONENT_CHOICE -> hana.pendingOffer?.chooserSeat ?: return false
                    else -> return false
                }
                val seat = seatMap[actingSeatIndex] ?: return false
                if (seat.type != PlayerType.BOT) return false

                val move = HanamikojiEngine.computeBotAction(hana, actingSeatIndex) ?: return false
                val res = when (move) {
                    is HanaBotDecision.Secret -> HanamikojiEngine.performSecret(hana, actingSeatIndex, move.cardId, names)
                    is HanaBotDecision.Discard -> HanamikojiEngine.performDiscard(hana, actingSeatIndex, move.cardIds, names)
                    is HanaBotDecision.Gift -> HanamikojiEngine.performGift(hana, actingSeatIndex, move.cardIds, names)
                    is HanaBotDecision.Competition -> HanamikojiEngine.performCompetition(hana, actingSeatIndex, move.groupA, move.groupB, names)
                    is HanaBotDecision.ResolveGift -> HanamikojiEngine.resolveGift(hana, actingSeatIndex, move.chosenCardId, names)
                    is HanaBotDecision.ResolveCompetition -> HanamikojiEngine.resolveCompetition(hana, actingSeatIndex, move.chosenGroup, names)
                }
                if (res is HanaMoveResult.Success) {
                    _uiState.update { it.copy(hanamikojiState = res.state) }
                    val nextPhase = res.state.phase
                    val nextActor = when (nextPhase) {
                        HanaPhase.CHOOSE_ACTION -> res.state.currentTurnSeat
                        HanaPhase.WAITING_OPPONENT_CHOICE -> res.state.pendingOffer?.chooserSeat
                        else -> null
                    }
                    return nextActor != null && seatMap[nextActor]?.type == PlayerType.BOT
                }
                return false
            }
            return false
        }
    }

    // --- LanRequestHandler Implementation (Thread-Safe Server Callbacks) ---

    override fun getPublicRoomInfo(): JSONObject {
        synchronized(lock) {
            val st = _uiState.value
            val game = st.activeGameType
            return JSONObject().apply {
                put("ok", true)
                put("active", game != null && st.serverRunning)
                put("gameType", game?.id ?: "")
                put("gameTitle", game?.title ?: "")
                put("phase", st.roomPhase.name)
                put("hostName", st.hostName)
                put("playerCount", st.seats.size)
                put("maxPlayers", game?.maxPlayers ?: 0)
            }
        }
    }

    override fun handleJoin(body: JSONObject): ApiResponse {
        synchronized(lock) {
            val st = _uiState.value
            val game = st.activeGameType
                ?: return ApiResponse(404, LanGameServer.errorJson("房主尚未创建房间"))
            if (st.roomPhase != RoomPhase.LOBBY) {
                return ApiResponse(409, LanGameServer.errorJson("对局已经开始，暂不可加入"))
            }
            val pin = body.optString("pin", "").trim()
            if (pin != st.roomCode) {
                return ApiResponse(403, LanGameServer.errorJson("房间口令不正确，请核对房主屏幕上的 4 位数字"))
            }

            // Pick lowest free seat from 1..(maxPlayers-1); if full, check if a bot seat can be replaced
            val freeSeatIndex = (1 until game.maxPlayers).firstOrNull { idx ->
                st.seats.none { it.seatIndex == idx }
            } ?: st.seats.firstOrNull { it.type == PlayerType.BOT }?.seatIndex

            if (freeSeatIndex == null) {
                return ApiResponse(409, LanGameServer.errorJson("房间座位已满 (${game.maxPlayers}/${game.maxPlayers})"))
            }

            val playerName = sanitizeNickname(body.optString("name", ""), fallback = "玩家${freeSeatIndex + 1}")
            val token = generateSessionToken()
            val newSeat = PlayerSeat(
                seatIndex = freeSeatIndex,
                id = "web_$freeSeatIndex",
                name = playerName,
                type = PlayerType.WEB_CLIENT,
                token = token,
                lastSeenMs = System.currentTimeMillis()
            )
            _uiState.update { cur ->
                val filtered = cur.seats.filterNot { it.seatIndex == freeSeatIndex }
                cur.copy(
                    seats = (filtered + newSeat).sortedBy { it.seatIndex },
                    statusMessage = "${newSeat.seatTag} $playerName 通过局域网网页加入了房间！"
                )
            }

            return ApiResponse(
                200,
                JSONObject().apply {
                    put("ok", true)
                    put("token", token)
                    put("seatIndex", freeSeatIndex)
                    put("seatTag", newSeat.seatTag)
                    put("playerName", playerName)
                }
            )
        }
    }

    override fun handleGetState(bearerToken: String): ApiResponse {
        synchronized(lock) {
            val st = _uiState.value
            if (st.activeGameType == null) {
                return ApiResponse(404, LanGameServer.errorJson("房间已解散"))
            }
            val seat = st.seats.firstOrNull { it.token == bearerToken }
                ?: return ApiResponse(401, LanGameServer.errorJson("会话已失效或被移出房间"))
            return ApiResponse(200, buildPlayerStateJson(st, seat.seatIndex))
        }
    }

    override fun handleAction(bearerToken: String, body: JSONObject): ApiResponse {
        synchronized(lock) {
            val st = _uiState.value
            val seat = st.seats.firstOrNull { it.token == bearerToken }
                ?: return ApiResponse(401, LanGameServer.errorJson("会话已失效，请重新加入"))
            if (st.roomPhase != RoomPhase.PLAYING) {
                return ApiResponse(409, LanGameServer.errorJson("对局尚未开始"))
            }

            val type = body.optString("type", "")
            val names = st.seats.associate { it.seatIndex to it.name }
            val errorMsg: String? = when (type) {
                "uno_play" -> {
                    val cardId = body.optString("cardId", "")
                    val color = UnoColor.fromCode(body.optString("chosenColor", ""))
                    applyUnoAction(seat.seatIndex) { uno, n ->
                        UnoEngine.playCard(uno, seat.seatIndex, cardId, color, n)
                    }
                }
                "uno_draw" -> applyUnoAction(seat.seatIndex) { uno, n ->
                    UnoEngine.drawCard(uno, seat.seatIndex, n)
                }
                "uno_pass" -> applyUnoAction(seat.seatIndex) { uno, n ->
                    UnoEngine.passTurn(uno, seat.seatIndex, n)
                }
                "uno_call" -> applyUnoAction(seat.seatIndex) { uno, n ->
                    UnoEngine.callUno(uno, seat.seatIndex, n)
                }
                "uno_catch" -> {
                    val targetSeat = body.optInt("targetSeat", -1)
                    applyUnoAction(seat.seatIndex) { uno, n ->
                        UnoEngine.catchUno(uno, seat.seatIndex, targetSeat, n)
                    }
                }
                "hana_secret" -> {
                    val cardId = body.optString("cardId", "")
                    applyHanaAction { hana, _ -> HanamikojiEngine.performSecret(hana, seat.seatIndex, cardId, names) }
                }
                "hana_discard" -> {
                    val ids = jsonArrayToStringList(body.optJSONArray("cardIds"))
                    applyHanaAction { hana, _ -> HanamikojiEngine.performDiscard(hana, seat.seatIndex, ids, names) }
                }
                "hana_gift" -> {
                    val ids = jsonArrayToStringList(body.optJSONArray("cardIds"))
                    applyHanaAction { hana, _ -> HanamikojiEngine.performGift(hana, seat.seatIndex, ids, names) }
                }
                "hana_competition" -> {
                    val groupA = jsonArrayToStringList(body.optJSONArray("groupA"))
                    val groupB = jsonArrayToStringList(body.optJSONArray("groupB"))
                    applyHanaAction { hana, _ -> HanamikojiEngine.performCompetition(hana, seat.seatIndex, groupA, groupB, names) }
                }
                "hana_resolve_gift" -> {
                    val cardId = body.optString("cardId", "")
                    applyHanaAction { hana, _ -> HanamikojiEngine.resolveGift(hana, seat.seatIndex, cardId, names) }
                }
                "hana_resolve_competition" -> {
                    val group = body.optInt("group", -1)
                    applyHanaAction { hana, _ -> HanamikojiEngine.resolveCompetition(hana, seat.seatIndex, group, names) }
                }
                else -> "不支持的操作类型"
            }

            if (errorMsg != null) {
                return ApiResponse(400, LanGameServer.errorJson(errorMsg))
            }
            val latest = _uiState.value
            return ApiResponse(
                200,
                JSONObject().apply {
                    put("ok", true)
                    put("state", buildPlayerStateJson(latest, seat.seatIndex))
                }
            )
        }
    }

    override fun handleLeave(bearerToken: String): ApiResponse {
        synchronized(lock) {
            val st = _uiState.value
            val seat = st.seats.firstOrNull { it.token == bearerToken }
                ?: return ApiResponse(200, JSONObject().put("ok", true))
            if (st.roomPhase == RoomPhase.LOBBY) {
                _uiState.update { cur ->
                    cur.copy(
                        seats = cur.seats.filterNot { it.seatIndex == seat.seatIndex },
                        statusMessage = "${seat.name} 离开了房间"
                    )
                }
            }
            return ApiResponse(200, JSONObject().put("ok", true))
        }
    }

    private fun buildPlayerStateJson(st: TableBoxUiState, viewerSeat: Int): JSONObject {
        val seatsJson = JSONArray()
        for (s in st.seats) {
            seatsJson.put(
                JSONObject().apply {
                    put("seatIndex", s.seatIndex)
                    put("seatTag", s.seatTag)
                    put("name", s.name)
                    put("type", s.type.name)
                }
            )
        }
        val gameJson: Any = when {
            st.roomPhase == RoomPhase.PLAYING && st.unoState != null ->
                UnoEngine.toPlayerJson(st.unoState, viewerSeat, st.seats)
            st.roomPhase == RoomPhase.PLAYING && st.hanamikojiState != null ->
                HanamikojiEngine.toPlayerJson(st.hanamikojiState, viewerSeat, st.seats)
            else -> JSONObject.NULL
        }

        return JSONObject().apply {
            put("ok", true)
            put("phase", st.roomPhase.name)
            put("gameType", st.activeGameType?.id ?: "")
            put("gameTitle", st.activeGameType?.title ?: "")
            put("roomCode", st.roomCode)
            put("mySeatIndex", viewerSeat)
            put("seats", seatsJson)
            put("game", gameJson)
        }
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return List(arr.length()) { arr.optString(it, "") }.filter { it.isNotEmpty() }
    }

    private fun generateRoomCode(): String = (1000 + secureRandom.nextInt(9000)).toString()

    private fun generateSessionToken(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun sanitizeNickname(raw: String, fallback: String): String {
        val cleaned = raw.replace(Regex("[\\p{Cntrl}<>\"'`]"), "").trim().take(16)
        return cleaned.ifEmpty { fallback }
    }

    override fun onCleared() {
        cancelBotLoop()
        lanServer.stop()
        super.onCleared()
    }
}
