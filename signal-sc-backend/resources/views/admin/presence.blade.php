<x-layouts.admin>
<div x-data="presenceApp()" x-init="init()" class="container-fluid py-3" style="max-width: 1200px;">

  <div class="d-flex align-items-center justify-content-between mb-3">
    <h5 class="text-white mb-0"><i class="fa-solid fa-satellite-dish text-success me-2"></i>Live Presence <small class="text-secondary">who is online right now</small></h5>
    <span class="badge" :class="connected ? 'bg-success' : 'bg-secondary'" x-text="connected ? '● LIVE' : '● connecting…'"></span>
  </div>

  <!-- Counters -->
  <div class="row g-2 mb-3">
    <div class="col-4">
      <div class="border border-secondary rounded p-2 bg-dark text-center">
        <div class="h4 mb-0 text-white" x-text="usersOnline"></div>
        <small class="text-secondary">Users online</small>
      </div>
    </div>
    <div class="col-4">
      <div class="border border-secondary rounded p-2 bg-dark text-center">
        <div class="h4 mb-0 text-white" x-text="devicesOnline"></div>
        <small class="text-secondary">Devices online</small>
      </div>
    </div>
    <div class="col-4">
      <div class="border border-secondary rounded p-2 bg-dark text-center">
        <div class="h4 mb-0 text-warning" x-text="backgroundCount"></div>
        <small class="text-secondary">In background</small>
      </div>
    </div>
  </div>

  <!-- Device table -->
  <div class="border border-secondary rounded bg-dark">
    <div class="p-2 border-bottom border-secondary d-flex justify-content-between align-items-center">
      <strong class="text-white small">Devices</strong>
      <small class="text-secondary" x-text="'updated ' + (lastUpdate || '…')"></small>
    </div>
    <div class="table-responsive">
      <table class="table table-dark table-hover mb-0" style="font-size: 12px;">
        <thead>
          <tr class="text-secondary">
            <th>Status</th><th>User</th><th>Device</th><th>Android</th><th>App</th><th>Last seen</th>
          </tr>
        </thead>
        <tbody>
          <template x-for="d in sortedDevices" :key="d.user_id + ':' + d.device_key">
            <tr :class="d.state === 'offline' ? 'opacity-50' : ''">
              <td>
                <span class="badge" :class="d.state === 'online' ? 'bg-success' : (d.state === 'background' ? 'bg-warning text-dark' : 'bg-secondary')" x-text="d.state"></span>
              </td>
              <td><strong class="text-white" x-text="d.name"></strong><br><small class="text-secondary" x-text="d.email"></small></td>
              <td class="text-light" x-text="(d.device?.brand || '') + ' ' + (d.device?.model || '')"></td>
              <td class="text-light" x-text="d.device?.android || '—'"></td>
              <td class="text-light" x-text="d.device?.app_version || '—'"></td>
              <td class="text-secondary" x-text="d.last_seen_human || '—'"></td>
            </tr>
          </template>
          <tr x-show="sortedDevices.length === 0">
            <td colspan="6" class="text-center text-secondary py-4">No devices seen yet — open the app on a phone.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script>
function presenceApp() {
  return {
    devices: {},   // key -> row (keeps offline rows so goodbyes stay visible)
    connected: false,
    lastUpdate: '',
    usersOnline: 0,
    devicesOnline: 0,
    backgroundCount: 0,

    get sortedDevices() {
      const rank = { online: 0, background: 1, offline: 2 };
      return Object.values(this.devices).sort(
        (a, b) => (rank[a.state] ?? 3) - (rank[b.state] ?? 3)
      );
    },

    keyOf(d) { return d.user_id + ':' + d.device_key; },

    mergeRow(d) {
      const k = this.keyOf(d);
      const prev = this.devices[k];
      this.devices[k] = Object.assign({}, prev || {}, d);
      this.recalc();
    },

    applySnapshot(list) {
      const seen = new Set();
      (list || []).forEach(d => {
        seen.add(this.keyOf(d));
        this.mergeRow(Object.assign({}, d, { last_seen_human: d.last_seen_human || 'just now' }));
      });
      // Rows missing from the snapshot expired server-side → mark offline.
      Object.keys(this.devices).forEach(k => {
        if (!seen.has(k) && this.devices[k].state !== 'offline') {
          this.devices[k].state = 'offline';
        }
      });
      this.recalc();
    },

    recalc() {
      const live = Object.values(this.devices).filter(d => d.state !== 'offline');
      this.devicesOnline = live.length;
      this.usersOnline = new Set(live.map(d => d.user_id)).size;
      this.backgroundCount = live.filter(d => d.state === 'background').length;
      this.lastUpdate = new Date().toLocaleTimeString();
    },

    async load() {
      try {
        const r = await fetch('/admin/presence/data', { headers: { 'Accept': 'application/json' } });
        if (r.ok) this.applySnapshot((await r.json()).devices);
      } catch (e) { console.warn('presence snapshot failed', e); }
    },

    async init() {
      await this.load();
      setInterval(() => this.load(), 10000);

      // Live updates over Centrifugo (same session-auth token as the rest of the panel).
      try {
        const resp = await fetch('/websocket/token');
        if (!resp.ok) return;
        const data = await resp.json();
        const centrifuge = new Centrifuge(
          data.ws_url || 'wss://socket.hadawatha.lk/connection/websocket',
          { token: data.token }
        );
        const sub = centrifuge.newSubscription('presence:devices');
        sub.on('publication', ctx => {
          const d = ctx.data || {};
          if (!d.user_id || !d.device_key) return;
          if (d.event === 'offline') {
            const k = d.user_id + ':' + d.device_key;
            if (this.devices[k]) {
              this.devices[k].state = 'offline';
              this.recalc();
            }
          } else {
            this.mergeRow({
              user_id: d.user_id, name: d.name, email: d.email,
              device_key: d.device_key, device: d.device || {},
              state: d.state || 'online', last_seen_human: 'just now',
            });
          }
        });
        centrifuge.on('connect', () => { this.connected = true; });
        centrifuge.on('disconnect', () => { this.connected = false; });
        sub.subscribe();
        centrifuge.connect();
      } catch (e) { console.warn('presence live feed failed', e); }
    },
  };
}
</script>
</x-layouts.admin>
