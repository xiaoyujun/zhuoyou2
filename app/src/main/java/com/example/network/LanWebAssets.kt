package com.example.network

object LanWebAssets {

    val CLIENT_HTML: String = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>桌游盒子 · 局域网联机</title>
<style>
  :root {
    --bg: #111017;
    --surface: #1B1924;
    --elev: #262333;
    --border: #3E3952;
    --text: #F6F1E7;
    --muted: #A8A1B8;
    --gold: #F2B84B;
    --sakura: #F2849E;
    --jade: #2FB380;
    --red: #E03131;
    --yellow: #F5B727;
    --green: #2B9348;
    --blue: #2563EB;
  }
  * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
  body {
    margin: 0; font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Noto Sans SC", sans-serif;
    background: radial-gradient(circle at top, #211D30 0%, var(--bg) 55%);
    color: var(--text); min-height: 100vh; padding-bottom: 32px;
  }
  .wrap { max-width: 560px; margin: 0 auto; padding: 16px; }
  .topbar {
    display: flex; justify-content: space-between; align-items: center;
    padding: 12px 16px; background: rgba(27,25,36,0.92); border: 1px solid var(--border);
    border-radius: 16px; margin-bottom: 14px; backdrop-filter: blur(8px);
  }
  .brand { font-weight: 800; font-size: 18px; color: var(--gold); letter-spacing: 0.5px; }
  .pill {
    font-size: 12px; padding: 4px 10px; border-radius: 999px;
    background: var(--elev); color: var(--muted); border: 1px solid var(--border);
  }
  .pill.active { background: rgba(242,184,75,0.18); color: var(--gold); border-color: var(--gold); }
  .card-box {
    background: var(--surface); border: 1px solid var(--border); border-radius: 18px;
    padding: 18px; margin-bottom: 14px; box-shadow: 0 10px 30px rgba(0,0,0,0.35);
  }
  h2 { margin: 0 0 8px 0; font-size: 20px; }
  .sub { color: var(--muted); font-size: 13px; margin: 0 0 14px 0; line-height: 1.5; }
  label { display: block; font-size: 12px; color: var(--muted); margin: 10px 0 6px; }
  input {
    width: 100%; padding: 12px 14px; border-radius: 12px; border: 1px solid var(--border);
    background: var(--bg); color: var(--text); font-size: 16px; outline: none;
  }
  input:focus { border-color: var(--gold); }
  .btn {
    width: 100%; padding: 13px 16px; border: none; border-radius: 13px;
    font-size: 15px; font-weight: 700; cursor: pointer; transition: transform 0.12s, opacity 0.12s;
  }
  .btn:active { transform: scale(0.97); }
  .btn:disabled { opacity: 0.42; cursor: not-allowed; }
  .btn-gold { background: linear-gradient(135deg, #F5C55B, #D99622); color: #15121C; }
  .btn-ghost { background: var(--elev); color: var(--text); border: 1px solid var(--border); }
  .btn-red { background: var(--red); color: #fff; }
  .btn-jade { background: var(--jade); color: #0B1F17; }
  .btn-sm { width: auto; padding: 8px 14px; font-size: 13px; border-radius: 10px; }
  .row { display: flex; gap: 10px; align-items: center; }
  .row-wrap { display: flex; gap: 8px; flex-wrap: wrap; }
  .event-banner {
    background: rgba(242,184,75,0.12); border: 1px solid rgba(242,184,75,0.35);
    color: #FFE6B3; padding: 10px 14px; border-radius: 12px; font-size: 13px;
    line-height: 1.45; margin-bottom: 12px;
  }
  .error-banner {
    background: rgba(224,49,49,0.16); border: 1px solid rgba(224,49,49,0.5);
    color: #FFD0D0; padding: 10px 14px; border-radius: 12px; font-size: 13px; margin-bottom: 12px;
  }
  .seat-item {
    display: flex; justify-content: space-between; align-items: center;
    padding: 12px 14px; background: var(--elev); border-radius: 12px; margin-bottom: 8px;
    border: 1px solid var(--border);
  }
  .seat-tag { font-weight: 800; color: var(--gold); margin-right: 8px; }
  /* UNO Styles */
  .uno-players { display: grid; grid-template-columns: repeat(auto-fit, minmax(110px, 1fr)); gap: 8px; margin-bottom: 12px; }
  .uno-p {
    background: var(--elev); border: 1px solid var(--border); border-radius: 12px;
    padding: 10px; text-align: center; font-size: 13px;
  }
  .uno-p.turn { border-color: var(--gold); box-shadow: 0 0 14px rgba(242,184,75,0.28); }
  .uno-center {
    display: flex; justify-content: center; align-items: center; gap: 18px;
    padding: 18px; background: var(--surface); border-radius: 18px;
    border: 1px solid var(--border); margin-bottom: 14px;
  }
  .uno-card {
    width: 70px; height: 104px; border-radius: 13px; border: 2px solid rgba(255,255,255,0.85);
    display: flex; flex-direction: column; justify-content: space-between; padding: 6px;
    font-weight: 800; color: #fff; user-select: none; position: relative; flex-shrink: 0;
    box-shadow: 0 6px 14px rgba(0,0,0,0.45); transition: transform 0.14s, opacity 0.14s;
  }
  .uno-card.RED { background: linear-gradient(145deg, #F03E3E, #A61E1E); }
  .uno-card.YELLOW { background: linear-gradient(145deg, #FCC419, #C2820A); color: #1A1405; }
  .uno-card.GREEN { background: linear-gradient(145deg, #37B24D, #1B6B2E); }
  .uno-card.BLUE { background: linear-gradient(145deg, #339AF0, #1849A9); }
  .uno-card.WILD { background: linear-gradient(145deg, #2D2645, #14111F); border-color: var(--gold); }
  .uno-card .mid { font-size: 26px; text-align: center; line-height: 1; }
  .uno-card .corner { font-size: 12px; }
  .uno-card.playable { cursor: pointer; transform: translateY(-6px); box-shadow: 0 10px 18px rgba(242,184,75,0.35); }
  .uno-card.dim { opacity: 0.42; }
  .hand-scroll { display: flex; gap: 8px; overflow-x: auto; padding: 12px 4px 8px; }
  .color-dot { width: 14px; height: 14px; border-radius: 50%; display: inline-block; vertical-align: middle; margin-right: 6px; }
  .color-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 12px; }
  /* Hanamikoji Styles */
  .hana-score { display: flex; justify-content: space-between; gap: 10px; margin-bottom: 12px; }
  .hana-score-box { flex: 1; background: var(--elev); border: 1px solid var(--border); border-radius: 12px; padding: 10px 12px; }
  .geisha-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(145px, 1fr)); gap: 8px; margin-bottom: 12px; }
  .geisha-card {
    background: var(--elev); border: 1px solid var(--border); border-radius: 14px;
    padding: 10px; display: flex; flex-direction: column; gap: 6px;
  }
  .geisha-head { display: flex; justify-content: space-between; align-items: center; }
  .geisha-badge {
    width: 28px; height: 28px; border-radius: 8px; display: flex; align-items: center;
    justify-content: center; font-weight: 800; color: #fff; font-size: 14px;
  }
  .favor-tag { font-size: 11px; padding: 3px 8px; border-radius: 999px; font-weight: 700; text-align: center; }
  .favor-ME { background: rgba(47,179,128,0.22); color: #7DF5C2; }
  .favor-OPPONENT { background: rgba(242,132,158,0.22); color: #FFB3C6; }
  .favor-NEUTRAL { background: rgba(255,255,255,0.08); color: var(--muted); }
  .action-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 12px; }
  .action-btn {
    padding: 10px; border-radius: 12px; border: 1px solid var(--border); background: var(--elev);
    color: var(--text); text-align: left; cursor: pointer;
  }
  .action-btn.selected { border-color: var(--gold); background: rgba(242,184,75,0.16); }
  .action-btn.used { opacity: 0.35; text-decoration: line-through; cursor: not-allowed; }
  .hana-card {
    min-width: 78px; padding: 10px 8px; border-radius: 13px; border: 2px solid var(--border);
    background: var(--elev); color: var(--text); text-align: center; cursor: pointer;
    flex-shrink: 0; transition: transform 0.12s;
  }
  .hana-card.sel-a { border-color: var(--gold); transform: translateY(-6px); background: rgba(242,184,75,0.18); }
  .hana-card.sel-b { border-color: var(--sakura); transform: translateY(-6px); background: rgba(242,132,158,0.18); }
  .modal-backdrop {
    position: fixed; inset: 0; background: rgba(0,0,0,0.72); display: flex;
    align-items: center; justify-content: center; padding: 20px; z-index: 50;
  }
  .modal { width: 100%; max-width: 380px; background: var(--surface); border: 1px solid var(--border); border-radius: 20px; padding: 20px; }
  .hidden { display: none !important; }
</style>
</head>
<body>
<div class="wrap">
  <div class="topbar">
    <div class="brand">桌游盒子 · LAN</div>
    <div id="seatPill" class="pill">未入座</div>
  </div>
  <div id="errorBox" class="error-banner hidden"></div>
  <div id="appRoot"></div>
</div>
<div id="modalRoot" class="modal-backdrop hidden"></div>

<script>
(function() {
  let sessionToken = null;
  let mySeatIndex = -1;
  let roomInfo = null;
  let currentState = null;
  let pollTimer = null;
  let hanaSelectedAction = null;
  let hanaPickA = [];
  let hanaPickB = [];
  let hanaAssignTarget = 'A';
  let pendingWildCardId = null;

  const appRoot = document.getElementById('appRoot');
  const modalRoot = document.getElementById('modalRoot');
  const errorBox = document.getElementById('errorBox');
  const seatPill = document.getElementById('seatPill');

  function el(tag, cls, text) {
    const node = document.createElement(tag);
    if (cls) node.className = cls;
    if (text !== undefined && text !== null) node.textContent = String(text);
    return node;
  }

  function showError(msg) {
    if (!msg) {
      errorBox.classList.add('hidden');
      errorBox.textContent = '';
    } else {
      errorBox.textContent = msg;
      errorBox.classList.remove('hidden');
    }
  }

  function clearNode(node) {
    while (node.firstChild) node.removeChild(node.firstChild);
  }

  async function apiFetch(url, method, bodyObj) {
    const headers = { 'Content-Type': 'application/json' };
    if (sessionToken) headers['Authorization'] = 'Bearer ' + sessionToken;
    const res = await fetch(url, {
      method: method || 'GET',
      headers,
      body: bodyObj ? JSON.stringify(bodyObj) : undefined
    });
    const data = await res.json();
    if (!res.ok || data.ok === false) {
      throw new Error(data.error || ('请求失败 (' + res.status + ')'));
    }
    return data;
  }

  async function loadPublicInfo() {
    try {
      roomInfo = await apiFetch('/api/info', 'GET');
      if (!sessionToken) renderJoinScreen();
    } catch (e) {
      showError('无法连接到房主设备：' + e.message);
    }
  }

  function renderJoinScreen() {
    clearNode(appRoot);
    seatPill.textContent = '未入座';
    seatPill.className = 'pill';

    const box = el('div', 'card-box');
    const title = roomInfo && roomInfo.active
      ? ('加入房间：' + roomInfo.gameTitle)
      : '等待房主创建房间';
    box.appendChild(el('h2', '', title));

    if (!roomInfo || !roomInfo.active) {
      box.appendChild(el('p', 'sub', '房主尚未开启局域网房间，请在房主手机创建 UNO 或花间小路房间后刷新。'));
      const refreshBtn = el('button', 'btn btn-gold', '刷新房间状态');
      refreshBtn.onclick = loadPublicInfo;
      box.appendChild(refreshBtn);
      appRoot.appendChild(box);
      return;
    }

    const infoLine = '房主：' + roomInfo.hostName + ' · 当前座位 ' + roomInfo.playerCount + '/' + roomInfo.maxPlayers;
    box.appendChild(el('p', 'sub', infoLine));

    const params = new URLSearchParams(window.location.search);
    const presetPin = params.get('pin') || '';

    box.appendChild(el('label', '', '房间口令 (4位数字)'));
    const pinInput = el('input');
    pinInput.type = 'tel';
    pinInput.maxLength = 6;
    pinInput.placeholder = '输入房主屏幕上的 4 位口令';
    pinInput.value = presetPin;
    box.appendChild(pinInput);

    box.appendChild(el('label', '', '你的玩家昵称'));
    const nameInput = el('input');
    nameInput.type = 'text';
    nameInput.maxLength = 16;
    nameInput.placeholder = '例如：小王 / Alice';
    nameInput.value = '玩家' + (roomInfo.playerCount + 1);
    box.appendChild(nameInput);

    const joinBtn = el('button', 'btn btn-gold', '加入局域网桌游');
    joinBtn.style.marginTop = '16px';
    joinBtn.onclick = async () => {
      showError('');
      joinBtn.disabled = true;
      try {
        const res = await apiFetch('/api/join', 'POST', {
          pin: pinInput.value.trim(),
          name: nameInput.value.trim()
        });
        sessionToken = res.token;
        mySeatIndex = res.seatIndex;
        seatPill.textContent = 'P' + (mySeatIndex + 1) + ' · ' + res.playerName;
        seatPill.className = 'pill active';
        startPolling();
      } catch (e) {
        showError(e.message);
        joinBtn.disabled = false;
      }
    };
    box.appendChild(joinBtn);
    appRoot.appendChild(box);
  }

  function startPolling() {
    if (pollTimer) clearInterval(pollTimer);
    pollState();
    pollTimer = setInterval(pollState, 750);
  }

  async function pollState() {
    if (!sessionToken) return;
    try {
      const data = await apiFetch('/api/state', 'GET');
      showError('');
      const prevSig = currentState ? JSON.stringify(currentState) : '';
      const nextSig = JSON.stringify(data);
      currentState = data;
      if (prevSig !== nextSig) {
        renderFromState();
      }
    } catch (e) {
      showError(e.message);
      if (e.message.indexOf('未授权') >= 0 || e.message.indexOf('已解散') >= 0) {
        sessionToken = null;
        if (pollTimer) clearInterval(pollTimer);
        loadPublicInfo();
      }
    }
  }

  async function sendAction(payload) {
    showError('');
    try {
      const res = await apiFetch('/api/action', 'POST', payload);
      if (res.state) {
        currentState = res.state;
        renderFromState();
      } else {
        pollState();
      }
    } catch (e) {
      showError(e.message);
    }
  }

  function renderFromState() {
    if (!currentState) return;
    if (currentState.phase === 'LOBBY') {
      hanaSelectedAction = null;
      hanaPickA = [];
      hanaPickB = [];
      renderLobby(currentState);
    } else if (currentState.game && currentState.game.gameType === 'uno') {
      renderUno(currentState.game);
    } else if (currentState.game && currentState.game.gameType === 'hanamikoji') {
      renderHanamikoji(currentState.game);
    }
  }

  function renderLobby(st) {
    clearNode(appRoot);
    const box = el('div', 'card-box');
    box.appendChild(el('h2', '', st.gameTitle + ' · 等待开始'));
    box.appendChild(el('p', 'sub', '已连接到局域网房间（口令 ' + st.roomCode + '），等待房主点击开始游戏。'));

    (st.seats || []).forEach(s => {
      const item = el('div', 'seat-item');
      const left = el('div');
      left.appendChild(el('span', 'seat-tag', s.seatTag));
      left.appendChild(el('span', '', s.name));
      item.appendChild(left);
      const role = s.type === 'HOST' ? '房主' : (s.type === 'BOT' ? '电脑 AI' : '网页玩家');
      item.appendChild(el('span', 'pill', role));
      box.appendChild(item);
    });

    const leaveBtn = el('button', 'btn btn-ghost', '离开房间');
    leaveBtn.style.marginTop = '12px';
    leaveBtn.onclick = async () => {
      try { await apiFetch('/api/leave', 'POST', {}); } catch (_) {}
      sessionToken = null;
      if (pollTimer) clearInterval(pollTimer);
      loadPublicInfo();
    };
    box.appendChild(leaveBtn);
    appRoot.appendChild(box);
  }

  function colorCss(code) {
    return { RED: '#E03131', YELLOW: '#F5B727', GREEN: '#2B9348', BLUE: '#2563EB' }[code] || '#F2B84B';
  }

  function renderUno(g) {
    clearNode(appRoot);
    appRoot.appendChild(el('div', 'event-banner', g.lastEvent));

    const pGrid = el('div', 'uno-players');
    g.players.forEach(p => {
      const card = el('div', 'uno-p' + (p.isCurrentTurn ? ' turn' : ''));
      card.appendChild(el('div', '', p.seatTag + ' ' + p.name));
      const countText = p.cardCount + ' 张牌' + (p.unoCalled ? ' · UNO!' : '');
      card.appendChild(el('div', 'sub', countText));
      card.appendChild(el('div', 'pill', '总分 ' + p.score));
      if (p.canBeCaught) {
        const catchBtn = el('button', 'btn btn-red btn-sm', '抓没喊UNO!');
        catchBtn.style.marginTop = '6px';
        catchBtn.onclick = () => sendAction({ type: 'uno_catch', targetSeat: p.seatIndex });
        card.appendChild(catchBtn);
      }
      pGrid.appendChild(card);
    });
    appRoot.appendChild(pGrid);

    const center = el('div', 'uno-center');
    const drawPile = el('div', 'uno-card WILD');
    drawPile.style.cursor = (g.isMyTurn && !g.hasDrawnThisTurn) ? 'pointer' : 'default';
    drawPile.appendChild(el('div', 'corner', '牌堆'));
    drawPile.appendChild(el('div', 'mid', String(g.drawPileCount)));
    drawPile.appendChild(el('div', 'corner', g.isMyTurn && !g.hasDrawnThisTurn ? '点此摸牌' : (g.direction > 0 ? '顺时针 ↻' : '逆时针 ↺')));
    if (g.isMyTurn && !g.hasDrawnThisTurn) {
      drawPile.onclick = () => sendAction({ type: 'uno_draw' });
    }
    center.appendChild(drawPile);

    const top = g.topCard;
    const topEl = el('div', 'uno-card ' + top.color);
    topEl.appendChild(el('div', 'corner', top.symbol));
    topEl.appendChild(el('div', 'mid', top.symbol));
    topEl.appendChild(el('div', 'corner', top.label));
    center.appendChild(topEl);

    const meta = el('div');
    const colorLine = el('div', '', '');
    const dot = el('span', 'color-dot');
    dot.style.background = colorCss(g.activeColor);
    colorLine.appendChild(dot);
    colorLine.appendChild(el('span', '', '当前色：' + g.activeColorLabel));
    meta.appendChild(colorLine);
    meta.appendChild(el('p', 'sub', '第 ' + g.roundNumber + ' 局'));
    if (g.isMyTurn) meta.appendChild(el('span', 'pill active', '轮到你出牌！'));
    center.appendChild(meta);
    appRoot.appendChild(center);

    if (g.winnerSeat !== null) {
      const winBox = el('div', 'card-box');
      winBox.appendChild(el('h2', '', '本局获胜：' + g.winnerName));
      winBox.appendChild(el('p', 'sub', '本局赢得 ' + g.lastRoundPoints + ' 分，等待房主开启下一局或返回大厅。'));
      appRoot.appendChild(winBox);
      return;
    }

    const ctrlRow = el('div', 'row');
    ctrlRow.style.marginBottom = '10px';
    if (g.isMyTurn && !g.hasDrawnThisTurn) {
      const dBtn = el('button', 'btn btn-ghost', '摸一张牌');
      dBtn.onclick = () => sendAction({ type: 'uno_draw' });
      ctrlRow.appendChild(dBtn);
    }
    if (g.hasDrawnThisTurn) {
      const pBtn = el('button', 'btn btn-ghost', '跳过出牌');
      pBtn.onclick = () => sendAction({ type: 'uno_pass' });
      ctrlRow.appendChild(pBtn);
    }
    if (g.canCallUno) {
      const uBtn = el('button', 'btn btn-red', '大喊 UNO!');
      uBtn.onclick = () => sendAction({ type: 'uno_call' });
      ctrlRow.appendChild(uBtn);
    }
    if (ctrlRow.childNodes.length > 0) appRoot.appendChild(ctrlRow);

    const handBox = el('div', 'card-box');
    handBox.appendChild(el('div', 'sub', '我的手牌 (' + g.myHand.length + ' 张) · 点击高亮卡牌打出'));
    const handRow = el('div', 'hand-scroll');
    g.myHand.forEach(c => {
      const cardEl = el('div', 'uno-card ' + c.color + (c.playable ? ' playable' : ' dim'));
      cardEl.appendChild(el('div', 'corner', c.symbol));
      cardEl.appendChild(el('div', 'mid', c.symbol));
      cardEl.appendChild(el('div', 'corner', c.label));
      if (c.playable) {
        cardEl.onclick = () => {
          if (c.isWild) {
            pendingWildCardId = c.id;
            openWildColorModal();
          } else {
            sendAction({ type: 'uno_play', cardId: c.id });
          }
        };
      }
      handRow.appendChild(cardEl);
    });
    handBox.appendChild(handRow);
    appRoot.appendChild(handBox);
  }

  function openWildColorModal() {
    clearNode(modalRoot);
    const modal = el('div', 'modal');
    modal.appendChild(el('h2', '', '选择万能牌颜色'));
    const grid = el('div', 'color-grid');
    [
      { code: 'RED', label: '红色', bg: '#E03131', fg: '#fff' },
      { code: 'YELLOW', label: '黄色', bg: '#F5B727', fg: '#1A1405' },
      { code: 'GREEN', label: '绿色', bg: '#2B9348', fg: '#fff' },
      { code: 'BLUE', label: '蓝色', bg: '#2563EB', fg: '#fff' }
    ].forEach(item => {
      const b = el('button', 'btn', item.label);
      b.style.background = item.bg;
      b.style.color = item.fg;
      b.onclick = () => {
        const cid = pendingWildCardId;
        pendingWildCardId = null;
        modalRoot.classList.add('hidden');
        sendAction({ type: 'uno_play', cardId: cid, chosenColor: item.code });
      };
      grid.appendChild(b);
    });
    modal.appendChild(grid);
    const cancel = el('button', 'btn btn-ghost', '取消');
    cancel.style.marginTop = '10px';
    cancel.onclick = () => { pendingWildCardId = null; modalRoot.classList.add('hidden'); };
    modal.appendChild(cancel);
    modalRoot.appendChild(modal);
    modalRoot.classList.remove('hidden');
  }

  function renderHanamikoji(g) {
    clearNode(appRoot);
    appRoot.appendChild(el('div', 'event-banner', g.lastEvent));

    // Score bar
    const scoreRow = el('div', 'hana-score');
    const meBox = el('div', 'hana-score-box');
    meBox.appendChild(el('div', '', '我 · ' + g.myName));
    meBox.appendChild(el('div', 'sub', '艺伎 ' + g.myGeishas + '/4 · 魅力 ' + g.myCharm + '/11'));
    scoreRow.appendChild(meBox);

    const oppBox = el('div', 'hana-score-box');
    oppBox.appendChild(el('div', '', '对手 · ' + g.opponentName));
    oppBox.appendChild(el('div', 'sub', '艺伎 ' + g.opponentGeishas + '/4 · 魅力 ' + g.opponentCharm + '/11'));
    scoreRow.appendChild(oppBox);
    appRoot.appendChild(scoreRow);

    // 7 Geishas Board
    const gGrid = el('div', 'geisha-grid');
    g.geishas.forEach(gs => {
      const gc = el('div', 'geisha-card');
      const head = el('div', 'geisha-head');
      const left = el('div', 'row');
      const badge = el('div', 'geisha-badge', gs.badgeChar);
      badge.style.background = gs.accentHex;
      left.appendChild(badge);
      const nameWrap = el('div');
      nameWrap.appendChild(el('div', '', gs.title + ' (' + gs.charm + '分)'));
      nameWrap.appendChild(el('div', 'sub', gs.itemName));
      nameWrap.lastChild.style.margin = '0';
      left.appendChild(nameWrap);
      head.appendChild(left);
      gc.appendChild(head);

      const counts = el('div', 'row');
      counts.style.justifyContent = 'space-between';
      counts.style.fontSize = '12px';
      counts.appendChild(el('span', '', '对手: ' + gs.opponentCards));
      counts.appendChild(el('span', '', '我方: ' + gs.myCards));
      gc.appendChild(counts);

      const fLabel = gs.favorSide === 'ME' ? '好感归我方' : (gs.favorSide === 'OPPONENT' ? '好感归对手' : '好感中立');
      gc.appendChild(el('div', 'favor-tag favor-' + gs.favorSide, fLabel));
      gGrid.appendChild(gc);
    });
    appRoot.appendChild(gGrid);

    // Round summary or Game Over
    if (g.phase === 'GAME_OVER' || g.phase === 'ROUND_SUMMARY') {
      const sumBox = el('div', 'card-box');
      sumBox.appendChild(el('h2', '', g.phase === 'GAME_OVER' ? ('胜者：' + g.winnerName) : ('第 ' + g.roundNumber + ' 轮结算')));
      sumBox.appendChild(el('p', 'sub', g.winReason));
      if (g.roundReveal) {
        const mySec = g.roundReveal.mySecret ? (g.roundReveal.mySecret.title + '(' + g.roundReveal.mySecret.charm + '分)') : '无';
        const oppSec = g.roundReveal.oppSecret ? (g.roundReveal.oppSecret.title + '(' + g.roundReveal.oppSecret.charm + '分)') : '无';
        sumBox.appendChild(el('p', 'sub', '秘密牌揭晓 · 我方：' + mySec + ' | 对手：' + oppSec));
      }
      sumBox.appendChild(el('p', 'sub', '等待房主点击进入下一轮或重开对局。'));
      appRoot.appendChild(sumBox);
      return;
    }

    // Pending Opponent Offer
    if (g.pendingOffer) {
      const offerBox = el('div', 'card-box');
      if (g.pendingOffer.type === 'GIFT') {
        offerBox.appendChild(el('h2', '', g.pendingOffer.isForMe ? '对手发来【赠礼】· 请选 1 张' : '等待对手挑选【赠礼】'));
        const row = el('div', 'hand-scroll');
        g.pendingOffer.cards.forEach(c => {
          const hc = makeHanaCardNode(c, '');
          if (g.pendingOffer.isForMe) {
            hc.onclick = () => sendAction({ type: 'hana_resolve_gift', cardId: c.id });
          }
          row.appendChild(hc);
        });
        offerBox.appendChild(row);
      } else if (g.pendingOffer.type === 'COMPETITION') {
        offerBox.appendChild(el('h2', '', g.pendingOffer.isForMe ? '对手发来【竞争】· 请选 A 组或 B 组' : '等待对手挑选【竞争】分组'));
        ['groupA', 'groupB'].forEach((key, idx) => {
          const grpTitle = idx === 0 ? 'A 组' : 'B 组';
          const wrap = el('div', 'seat-item');
          const cardsText = g.pendingOffer[key].map(c => c.title + '(' + c.charm + '分)').join(' + ');
          wrap.appendChild(el('span', '', grpTitle + '：' + cardsText));
          if (g.pendingOffer.isForMe) {
            const pickBtn = el('button', 'btn btn-gold btn-sm', '选择 ' + grpTitle);
            pickBtn.onclick = () => sendAction({ type: 'hana_resolve_competition', group: idx });
            wrap.appendChild(pickBtn);
          }
          offerBox.appendChild(wrap);
        });
      }
      appRoot.appendChild(offerBox);
    }

    // My Actions & Hand
    const actBox = el('div', 'card-box');
    const headerText = g.isMyTurn ? '轮到你行动 · 选择 1 个行动标记并选牌' : ('等待 ' + g.opponentName + ' 行动...');
    actBox.appendChild(el('div', 'sub', headerText + '（牌堆余 ' + g.deckCount + ' 张）'));

    const actGrid = el('div', 'action-grid');
    g.myActions.forEach(a => {
      const btn = el('button', 'action-btn' + (a.used ? ' used' : '') + (hanaSelectedAction === a.code ? ' selected' : ''));
      btn.disabled = a.used || !g.isMyTurn;
      btn.appendChild(el('div', '', a.title + ' (选' + a.requiredCards + '张)'));
      const desc = el('div', 'sub', a.shortRule);
      desc.style.margin = '4px 0 0 0';
      desc.style.fontSize = '11px';
      btn.appendChild(desc);
      btn.onclick = () => {
        hanaSelectedAction = a.code;
        hanaPickA = [];
        hanaPickB = [];
        hanaAssignTarget = 'A';
        renderHanamikoji(g);
      };
      actGrid.appendChild(btn);
    });
    actBox.appendChild(actGrid);

    if (g.isMyTurn && hanaSelectedAction === 'COMPETITION') {
      const toggleRow = el('div', 'row');
      toggleRow.style.marginBottom = '8px';
      const btnA = el('button', 'btn btn-sm ' + (hanaAssignTarget === 'A' ? 'btn-gold' : 'btn-ghost'), '正在选 A 组 (' + hanaPickA.length + '/2)');
      btnA.onclick = () => { hanaAssignTarget = 'A'; renderHanamikoji(g); };
      const btnB = el('button', 'btn btn-sm ' + (hanaAssignTarget === 'B' ? 'btn-gold' : 'btn-ghost'), '正在选 B 组 (' + hanaPickB.length + '/2)');
      btnB.onclick = () => { hanaAssignTarget = 'B'; renderHanamikoji(g); };
      toggleRow.appendChild(btnA);
      toggleRow.appendChild(btnB);
      actBox.appendChild(toggleRow);
    }

    const handRow = el('div', 'hand-scroll');
    g.myHand.forEach(c => {
      const inA = hanaPickA.indexOf(c.id) >= 0;
      const inB = hanaPickB.indexOf(c.id) >= 0;
      const cls = inA ? 'sel-a' : (inB ? 'sel-b' : '');
      const hc = makeHanaCardNode(c, cls);
      if (inA && hanaSelectedAction === 'COMPETITION') hc.appendChild(el('div', 'pill active', 'A 组'));
      if (inB && hanaSelectedAction === 'COMPETITION') hc.appendChild(el('div', 'pill active', 'B 组'));
      hc.onclick = () => {
        if (!g.isMyTurn || !hanaSelectedAction) return;
        toggleHanaCardPick(c.id);
        renderHanamikoji(g);
      };
      handRow.appendChild(hc);
    });
    actBox.appendChild(handRow);

    if (g.isMyTurn && hanaSelectedAction) {
      const ready = isHanaSelectionReady();
      const confirmBtn = el('button', 'btn btn-jade', '确认执行【' + actionTitle(hanaSelectedAction) + '】');
      confirmBtn.style.marginTop = '10px';
      confirmBtn.disabled = !ready;
      confirmBtn.onclick = submitHanaAction;
      actBox.appendChild(confirmBtn);
    }
    appRoot.appendChild(actBox);
  }

  function makeHanaCardNode(c, extraCls) {
    const hc = el('div', 'hana-card ' + extraCls);
    const badge = el('div', 'geisha-badge', c.badgeChar);
    badge.style.background = c.accentHex;
    badge.style.margin = '0 auto 6px';
    hc.appendChild(badge);
    hc.appendChild(el('div', '', c.title));
    const sub = el('div', 'sub', c.charm + '分 · ' + c.itemName);
    sub.style.margin = '2px 0 0';
    sub.style.fontSize = '11px';
    hc.appendChild(sub);
    return hc;
  }

  function actionTitle(code) {
    return { SECRET: '秘密', DISCARD: '弃置', GIFT: '赠礼', COMPETITION: '竞争' }[code] || code;
  }

  function toggleHanaCardPick(cardId) {
    if (hanaSelectedAction === 'COMPETITION') {
      if (hanaPickA.indexOf(cardId) >= 0) {
        hanaPickA = hanaPickA.filter(x => x !== cardId);
        return;
      }
      if (hanaPickB.indexOf(cardId) >= 0) {
        hanaPickB = hanaPickB.filter(x => x !== cardId);
        return;
      }
      if (hanaAssignTarget === 'A') {
        if (hanaPickA.length < 2) hanaPickA.push(cardId);
        if (hanaPickA.length === 2 && hanaPickB.length < 2) hanaAssignTarget = 'B';
      } else {
        if (hanaPickB.length < 2) hanaPickB.push(cardId);
        if (hanaPickB.length === 2 && hanaPickA.length < 2) hanaAssignTarget = 'A';
      }
      return;
    }
    const maxNeed = { SECRET: 1, DISCARD: 2, GIFT: 3 }[hanaSelectedAction] || 1;
    if (hanaPickA.indexOf(cardId) >= 0) {
      hanaPickA = hanaPickA.filter(x => x !== cardId);
    } else if (hanaPickA.length < maxNeed) {
      hanaPickA.push(cardId);
    }
  }

  function isHanaSelectionReady() {
    if (hanaSelectedAction === 'SECRET') return hanaPickA.length === 1;
    if (hanaSelectedAction === 'DISCARD') return hanaPickA.length === 2;
    if (hanaSelectedAction === 'GIFT') return hanaPickA.length === 3;
    if (hanaSelectedAction === 'COMPETITION') return hanaPickA.length === 2 && hanaPickB.length === 2;
    return false;
  }

  async function submitHanaAction() {
    const act = hanaSelectedAction;
    const a = hanaPickA.slice();
    const b = hanaPickB.slice();
    hanaSelectedAction = null;
    hanaPickA = [];
    hanaPickB = [];
    if (act === 'SECRET') await sendAction({ type: 'hana_secret', cardId: a[0] });
    else if (act === 'DISCARD') await sendAction({ type: 'hana_discard', cardIds: a });
    else if (act === 'GIFT') await sendAction({ type: 'hana_gift', cardIds: a });
    else if (act === 'COMPETITION') await sendAction({ type: 'hana_competition', groupA: a, groupB: b });
  }

  loadPublicInfo();
})();
</script>
</body>
</html>
""".trimIndent()
}
