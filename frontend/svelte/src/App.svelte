<script lang="ts">
  import { onMount } from 'svelte'

  type Chronicle =
    | { type: 'observation'; recipient: string; category: string; content: string }
    | { type: 'action'; actor: string; category: string; content: string; intent: string }

  type LogEntry =
    | { type: 'observation'; title: string; body: string }
    | { type: 'action'; title: string; body: string; intent: string }
    | { type: 'epilogue'; chronicles: Chronicle[] }

  type InputState =
    | { type: 'idle' }
    | { type: 'choose'; title: string; description: string; candidates: string[] }
    | { type: 'speak'; title: string; description: string }

  type PlayerStatus = 'alive' | 'executed' | 'attacked'
  type SurvivalEntry = { name: string; status: PlayerStatus }

  let status = '接続中...'
  let entries: LogEntry[] = []
  let input: InputState = { type: 'idle' }
  let speakText = ''
  let logEl: HTMLElement
  let ws: WebSocket
  let survivalPlayers: SurvivalEntry[] = []

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
      case 'survival':
        survivalPlayers = msg.players as SurvivalEntry[]
        break
    }
  }

  function push(entry: LogEntry) {
    entries = [...entries, entry]
    setTimeout(() => logEl?.scrollTo(0, logEl.scrollHeight), 0)
  }

  function choose(name: string) {
    ws.send(name)
    input = { type: 'idle' }
  }

  function speak() {
    const text = speakText.trim()
    if (!text || ws.readyState !== WebSocket.OPEN) return
    ws.send(text)
    input = { type: 'idle' }
    speakText = ''
  }

  function abort() {
    if (ws.readyState === WebSocket.OPEN) ws.send('abort')
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
            on:keydown={(e) => e.key === 'Enter' && speak()}
          />
          <button on:click={speak}>送信</button>
        </div>
      </div>
    {/if}

    <div class="log" bind:this={logEl}>
      {#each entries as entry}
        {#if entry.type === 'observation'}
          <div class="event"><span class="title">[{entry.title}]</span> {entry.body}</div>
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
    <h2>生存状況</h2>
    {#if survivalPlayers.length === 0}
      <p class="panel-empty">ゲーム開始前</p>
    {:else}
      <ul class="survival-list">
        {#each survivalPlayers as p}
          <li class="survival-entry {p.status}">
            <span class="player-name">{p.name}</span>
            <span class="player-status">{statusLabel(p.status)}</span>
          </li>
        {/each}
      </ul>
    {/if}
  </aside>
</div>

<style>
  .layout { display: flex; gap: 16px; max-width: 1100px; margin: 0 auto; padding: 16px; font-family: sans-serif; }
  .main { flex: 1; min-width: 0; }
  .panel { width: 200px; flex-shrink: 0; border: 1px solid #ccc; border-radius: 4px; padding: 12px; background: #fafafa; align-self: flex-start; }
  h1 { margin: 0 0 4px; }
  h2 { margin: 0 0 10px; font-size: 1em; color: #444; }
  .status { color: #666; margin-bottom: 8px; }
  .controls { margin: 8px 0; }
  .log { border: 1px solid #ccc; height: 500px; overflow-y: auto; padding: 8px; background: #fafafa; }
  .event { margin: 4px 0; line-height: 1.4; }
  .title { font-weight: bold; color: #444; }
  .intent { color: #888; font-size: 0.9em; }
  .epilogue { margin: 16px 0 0; padding: 12px; background: #eef; border-left: 4px solid #88a; white-space: pre-wrap; font-family: inherit; }
  .input-area { margin-bottom: 8px; padding: 12px; border: 2px solid #88a; background: #f0f0fa; }
  .input-title { font-weight: bold; margin: 0 0 4px; }
  .input-description { color: #666; font-size: 0.9em; margin: 0 0 10px; }
  button { margin: 4px; padding: 6px 14px; cursor: pointer; }
  input[type='text'] { width: 70%; padding: 6px; font-size: 1em; }

  .panel-empty { color: #999; font-size: 0.9em; }
  .survival-list { list-style: none; margin: 0; padding: 0; }
  .survival-entry { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; border-bottom: 1px solid #eee; font-size: 0.9em; }
  .survival-entry:last-child { border-bottom: none; }
  .player-name { flex: 1; }
  .player-status { font-size: 0.8em; padding: 1px 6px; border-radius: 3px; }
  .alive .player-status { background: #d4f7d4; color: #2a6e2a; }
  .executed .player-status { background: #f7d4d4; color: #6e2a2a; }
  .attacked .player-status { background: #f7ead4; color: #6e4a2a; }
</style>