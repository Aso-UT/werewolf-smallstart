<script lang="ts">
  import { onMount } from 'svelte'

  type Chronicle =
    | { type: 'observation'; recipient: string; category: string; content: string }
    | { type: 'action'; actor: string; category: string; content: string; intent: string }

  type LogEntry =
    | { type: 'observation'; title: string; body: string }
    | { type: 'attributedObservation'; title: string; actor: string; body: string }
    | { type: 'action'; title: string; body: string; intent: string }
    | { type: 'epilogue'; chronicles: Chronicle[] }

  type InputState =
    | { type: 'idle' }
    | { type: 'choose'; title: string; description: string; candidates: string[] }
    | { type: 'speak'; title: string; description: string }

  type PlayerStatus = 'alive' | 'executed' | 'attacked'
  type PlayerEntry = { name: string; status: PlayerStatus; claimedRole: string | null }

  type ReportEntry = { source: string; targetName: string; isWerewolf: boolean }
  type DivinationData = {
    playerNames: string[]
    divineResults: { targetName: string; isWerewolf: boolean }[]
    mediumResults: { targetName: string; isWerewolf: boolean }[]
    divineReports: ReportEntry[]
    mediumReports: ReportEntry[]
  }

  let status = '接続中...'
  let entries: LogEntry[] = []
  let input: InputState = { type: 'idle' }
  let speakText = ''
  let logEl: HTMLElement
  let ws: WebSocket
  let players: PlayerEntry[] = []
  let divination: DivinationData = {
    playerNames: [],
    divineResults: [],
    mediumResults: [],
    divineReports: [],
    mediumReports: [],
  }

  onMount(() => {
    ws = new WebSocket(`ws://${location.host}/game`)
    ws.onopen = () => { status = '接続済み' }
    ws.onclose = () => { status = '切断'; input = { type: 'idle' } }
    ws.onerror = () => { status = 'エラー' }
    ws.onmessage = ({ data }) => dispatch(JSON.parse(data as string))
  })

  function dispatch(msg: Record<string, unknown>) {
    switch (msg.type) {
      case 'observation':
        push({ type: 'observation', title: msg.title as string, body: msg.body as string })
        break
      case 'attributedObservation':
        push({ type: 'attributedObservation', title: msg.title as string, actor: msg.actor as string, body: msg.body as string })
        break
      case 'action':
        push({ type: 'action', title: msg.title as string, body: msg.body as string, intent: msg.intent as string })
        break
      case 'epilogue':
        push({ type: 'epilogue', chronicles: msg.chronicles as Chronicle[] })
        input = { type: 'idle' }
        break
      case 'choose':
        input = { type: 'choose', title: msg.title as string, description: msg.description as string, candidates: msg.candidates as string[] }
        break
      case 'speak':
        input = { type: 'speak', title: msg.title as string, description: msg.description as string }
        speakText = ''
        break
      case 'playerStatus':
        players = msg.players as PlayerEntry[]
        break
      case 'divination':
        divination = msg as unknown as DivinationData
        break
    }
  }

  function push(entry: LogEntry) {
    entries = [...entries, entry]
    setTimeout(() => logEl?.scrollTo(0, logEl.scrollHeight), 0)
  }

  function choose(name: string) {
    ws.send(JSON.stringify({ type: 'choose', value: name }))
    input = { type: 'idle' }
  }

  function speak() {
    if (ws.readyState !== WebSocket.OPEN) return
    ws.send(JSON.stringify({ type: 'speak', text: speakText.trim() }))
    input = { type: 'idle' }
    speakText = ''
  }

  function abort() {
    if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify({ type: 'abort' }))
    input = { type: 'idle' }
  }

  function formatChronicle(c: Chronicle): string {
    if (c.type === 'observation') return `[${c.recipient}] [${c.category}] ${c.content}`
    const line = `[${c.actor}] [${c.category}] ${c.content}`
    return c.intent ? `${line}\n  [${c.intent}]` : line
  }

  function statusLabel(s: PlayerStatus): string {
    if (s === 'alive') return '生存'
    if (s === 'executed') return '処刑'
    if (s === 'attacked') return '襲撃'
    throw new Error(`Unknown player status: ${s}`)
  }

  function uniqueSources(reports: ReportEntry[]): string[] {
    const seen = new Set<string>()
    return reports.reduce<string[]>((acc, r) => {
      if (!seen.has(r.source)) { seen.add(r.source); acc.push(r.source) }
      return acc
    }, [])
  }

  function reportOf(reports: ReportEntry[], player: string, source: string): ReportEntry | undefined {
    return reports.find(r => r.targetName === player && r.source === source)
  }

  function claimedRoleOf(name: string): string | null {
    return players.find(p => p.name === name)?.claimedRole ?? null
  }
</script>

<div class="layout">
  <div class="main">
    <h1>人狼ゲーム</h1>
    <p class="status">{status}</p>

    <div class="controls">
      <button on:click={abort}>ゲームを中断</button>
    </div>

    {#if input.type === 'choose'}
      <div class="input-area">
        <p class="input-title">{input.title}</p>
        <p class="input-description">{input.description}</p>
        <div>
          {#each input.candidates as name}
            <button on:click={() => choose(name)}>{name}</button>
          {/each}
        </div>
      </div>
    {:else if input.type === 'speak'}
      <div class="input-area">
        <p class="input-title">{input.title}</p>
        <p class="input-description">{input.description}</p>
        <div>
          <input
            type="text"
            bind:value={speakText}
            placeholder="発言を入力..."
          />
          <button on:click={speak}>送信</button>
        </div>
      </div>
    {/if}

    <div class="log" bind:this={logEl}>
      {#each entries as entry}
        {#if entry.type === 'observation'}
          <div class="event"><span class="title">[{entry.title}]</span> {entry.body}</div>
        {:else if entry.type === 'attributedObservation'}
          {@const role = claimedRoleOf(entry.actor)}
          <div class="event">
            <span class="title">[{entry.title}]</span>
            {entry.actor}{#if role}<span class="role-badge">{role}</span>{/if}: {entry.body}
          </div>
        {:else if entry.type === 'action'}
          <div class="event">
            <span class="title">[{entry.title}]</span> {entry.body}
            {#if entry.intent}<br /><span class="intent">  [{entry.intent}]</span>{/if}
          </div>
        {:else if entry.type === 'epilogue'}
          <pre class="epilogue">{entry.chronicles.map(formatChronicle).join('\n')}</pre>
        {/if}
      {/each}
    </div>
  </div>

  <aside class="panel">
    <h2>生存状況・CO</h2>
    {#if players.length === 0}
      <p class="panel-empty">ゲーム開始前</p>
    {:else}
      <table class="status-table">
        <thead>
          <tr><th>プレイヤー</th><th>状態</th><th>CO</th></tr>
        </thead>
        <tbody>
          {#each players as p}
            <tr class={p.status}>
              <td class="row-label">{p.name}</td>
              <td><span class="player-status">{statusLabel(p.status)}</span></td>
              <td class:co-empty={!p.claimedRole}>{p.claimedRole ?? '-'}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}

    {#if divination.divineResults.length > 0}
      <h2 class="panel-section">占い結果</h2>
      <ul class="result-list">
        {#each divination.divineResults as r}
          <li class="result-entry">
            <span class="result-name">{r.targetName}</span>
            <span class="badge {r.isWerewolf ? 'black' : 'white'}">&nbsp;</span>
          </li>
        {/each}
      </ul>
    {/if}

    {#if divination.mediumResults.length > 0}
      <h2 class="panel-section">霊媒結果</h2>
      <ul class="result-list">
        {#each divination.mediumResults as r}
          <li class="result-entry">
            <span class="result-name">{r.targetName}</span>
            <span class="badge {r.isWerewolf ? 'black' : 'white'}">&nbsp;</span>
          </li>
        {/each}
      </ul>
    {/if}

    {#if divination.divineReports.length > 0}
      {@const sources = uniqueSources(divination.divineReports)}
      <h2 class="panel-section">占い申告</h2>
      <div class="report-scroll">
        <table class="report-table">
          <thead>
            <tr>
              <th></th>
              {#each sources as src}<th>{src}</th>{/each}
            </tr>
          </thead>
          <tbody>
            {#each divination.playerNames as player}
              <tr>
                <td class="row-label">{player}</td>
                {#each sources as src}
                  {@const entry = reportOf(divination.divineReports, player, src)}
                  <td>
                    {#if entry}
                      <span class="badge {entry.isWerewolf ? 'black' : 'white'}">&nbsp;</span>
                    {/if}
                  </td>
                {/each}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}

    {#if divination.mediumReports.length > 0}
      {@const sources = uniqueSources(divination.mediumReports)}
      <h2 class="panel-section">霊媒申告</h2>
      <div class="report-scroll">
        <table class="report-table">
          <thead>
            <tr>
              <th></th>
              {#each sources as src}<th>{src}</th>{/each}
            </tr>
          </thead>
          <tbody>
            {#each divination.playerNames as player}
              <tr>
                <td class="row-label">{player}</td>
                {#each sources as src}
                  {@const entry = reportOf(divination.mediumReports, player, src)}
                  <td>
                    {#if entry}
                      <span class="badge {entry.isWerewolf ? 'black' : 'white'}">&nbsp;</span>
                    {/if}
                  </td>
                {/each}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </aside>
</div>

<style>
  .layout { display: flex; gap: 16px; max-width: 1100px; margin: 0 auto; padding: 16px; font-family: sans-serif; }
  .main { flex: 1; min-width: 0; }
  .panel { width: 240px; flex-shrink: 0; border: 1px solid #ccc; border-radius: 4px; padding: 12px; background: #fafafa; align-self: flex-start; }
  h1 { margin: 0 0 4px; }
  h2 { margin: 0 0 10px; font-size: 1em; color: #444; }
  .panel-section { margin: 14px 0 6px; }
  .status { color: #666; margin-bottom: 8px; }
  .controls { margin: 8px 0; }
  .log { border: 1px solid #ccc; height: 500px; overflow-y: auto; padding: 8px; background: #fafafa; }
  .event { margin: 4px 0; line-height: 1.4; }
  .title { font-weight: bold; color: #444; }
  .intent { color: #888; font-size: 0.9em; }
  .role-badge { display: inline-block; margin-left: 4px; padding: 1px 6px; border-radius: 3px; font-size: 0.8em; background: #dde6f7; color: #33507a; }
  .epilogue { margin: 16px 0 0; padding: 12px; background: #eef; border-left: 4px solid #88a; white-space: pre-wrap; font-family: inherit; }
  .input-area { margin-bottom: 8px; padding: 12px; border: 2px solid #88a; background: #f0f0fa; }
  .input-title { font-weight: bold; margin: 0 0 4px; }
  .input-description { color: #666; font-size: 0.9em; margin: 0 0 10px; }
  button { margin: 4px; padding: 6px 14px; cursor: pointer; }
  input[type='text'] { width: 70%; padding: 6px; font-size: 1em; }

  .panel-empty { color: #999; font-size: 0.9em; }
  .status-table { border-collapse: collapse; font-size: 0.85em; width: 100%; }
  .status-table th { font-weight: normal; color: #666; padding: 3px 4px; text-align: left; }
  .status-table td { padding: 4px 4px; border-bottom: 1px solid #eee; }
  .status-table tr:last-child td { border-bottom: none; }
  .co-empty { color: #999; }
  .player-status { font-size: 0.8em; padding: 1px 6px; border-radius: 3px; white-space: nowrap; }
  .alive .player-status { background: #d4f7d4; color: #2a6e2a; }
  .executed .player-status { background: #f7d4d4; color: #6e2a2a; }
  .attacked .player-status { background: #f7ead4; color: #6e4a2a; }

  .result-list { list-style: none; margin: 0; padding: 0; }
  .result-entry { display: flex; justify-content: space-between; align-items: center; padding: 3px 0; font-size: 0.9em; }
  .result-name { flex: 1; }

  .report-scroll { overflow-x: auto; }
  .report-table { border-collapse: collapse; font-size: 0.8em; width: 100%; }
  .report-table th { font-weight: normal; color: #666; padding: 2px 4px; text-align: center; white-space: nowrap; }
  .report-table td { padding: 2px 4px; text-align: center; }
  .row-label { text-align: left; white-space: nowrap; color: #444; }
  .badge { display: inline-block; padding: 1px 5px; border-radius: 3px; font-size: 0.85em; }
  .badge.black { background: #333; color: #fff; }
  .badge.white { background: #eee; color: #333; border: 1px solid #ccc; }
</style>
