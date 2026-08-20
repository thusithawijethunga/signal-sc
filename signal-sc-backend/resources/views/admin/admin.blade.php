<x-layouts.admin>

<div x-data="adminApp()" class="flex justify-between items-center mb-6">
  <div class="text-lg font-bold" style="color: var(--gold)">⚙️ Trade Ledger Administration</div>
  <div class="flex gap-2">
    <input type="file" id="file-input" accept=".json" style="display:none" @change="importJSON($event)">
    <button class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-white rounded text-xs font-bold border border-gray-700" onclick="document.getElementById('file-input').click()">📂 Load JSON</button>
    <button class="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-black rounded text-xs font-bold" @click="downloadJSON()">⬇ Download JSON</button>
  </div>
</div>

<div x-data="adminApp()" class="grid grid-cols-1 lg:grid-cols-3 gap-6">

  <div class="card-admin lg:col-span-3 border border-amber-500/40 bg-amber-950/20">
    <div class="flex justify-between items-center mb-3">
      <h3 class="text-sm font-bold uppercase tracking-wider text-amber-400">💰 Manual Balance Configuration (Start, Deposit & Withdraw)</h3>
      <button type="button" @click="showBalance = !showBalance" class="w-6 h-6 bg-gray-800 hover:bg-gray-700 text-gray-200 rounded font-bold text-xs flex items-center justify-center" x-text="showBalance ? '-' : '+'">-</button>
    </div>
    <div x-show="showBalance" x-transition class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-2">
      <div class="form-group"><label>Start Balance ($)</label><input type="number" step="0.01" :value="startBalance" @input="startBalance = parseFloat($event.target.value) || 0"></div>
      <div class="form-group"><label>Deposit Balance ($)</label><input type="number" step="0.01" :value="depositBalance" @input="depositBalance = parseFloat($event.target.value) || 0"></div>
      <div class="form-group"><label>Withdraw Balance ($)</label><input type="number" step="0.01" :value="withdrawBalance" @input="withdrawBalance = parseFloat($event.target.value) || 0"></div>
      <div class="form-group justify-end"><label>&nbsp;</label><button type="button" @click="saveBalances()" class="bg-amber-500 hover:bg-amber-400 text-black px-4 py-2 rounded text-xs font-bold">💾 Save Balances</button></div>
    </div>
  </div>

  <div class="card-admin lg:col-span-3 border border-blue-500/40 bg-blue-950/20">
    <div class="flex justify-between items-center mb-3">
      <h3 class="text-sm font-bold uppercase tracking-wider text-blue-400">🤖 Telegram Bot Settings</h3>
      <div class="flex gap-2 items-center flex-wrap">
        <button type="button" @click="testTelegramConnection()" class="px-3 py-1 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-bold transition-all shadow">🧪 Test Bot Connection</button>
        <div class="flex items-center gap-1 bg-black p-1 rounded border border-blue-500/40">
          <select x-model="summaryType" class="bg-black text-xs text-white border border-gray-700 rounded px-2 py-1 outline-none">
            <option value="DAILY">Daily Summary</option>
            <option value="WEEKLY">Weekly Summary</option>
            <option value="MONTHLY">Monthly Summary</option>
          </select>
          <input type="date" x-model="summaryDate" class="bg-transparent text-xs text-white outline-none [color-scheme:dark]">
          <button type="button" @click="sendSummaryToTelegram()" class="px-3 py-1 bg-amber-500 hover:bg-amber-400 text-black rounded text-xs font-bold transition-all shadow">📊 Send Summary</button>
        </div>
        <button type="button" @click="showTgSettings = !showTgSettings" class="w-6 h-6 bg-gray-800 hover:bg-gray-700 text-gray-200 rounded font-bold text-xs flex items-center justify-center" x-text="showTgSettings ? '-' : '+'">-</button>
      </div>
    </div>
    <div x-show="showTgSettings" x-transition class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
      <div class="form-group"><label>Bot Token</label><input type="text" x-model="tgToken" placeholder="123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ"></div>
      <div class="form-group"><label>Chat ID / Channel Username</label><input type="text" x-model="tgChatId" placeholder="@signalxpress_official or -100xxxxxxxxxx"></div>
    </div>
  </div>

  <div class="card-admin lg:col-span-3 border border-emerald-500/40 bg-emerald-950/20">
    <div class="flex justify-between items-center mb-3">
      <h3 class="text-sm font-bold uppercase tracking-wider text-emerald-400">📊 Google Sheets Live Two-Way Sync</h3>
      <div class="flex gap-2 items-center">
        <button type="button" @click="testGoogleSheetsConnection()" class="px-3 py-1 bg-blue-600 hover:bg-blue-500 text-white rounded text-xs font-bold transition-all shadow">🧪 Test Connection</button>
        <button type="button" @click="syncFromGoogleSheets()" class="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-bold transition-all shadow">🔄 Sync Now</button>
      </div>
    </div>
    <div class="text-xs mt-2" style="color: var(--text-muted);">
      <span class="text-emerald-400 font-semibold">● Auto-Sync Active</span> — Signal updates push to Google Sheets automatically. Use Sync Now to pull changes from the sheet.
    </div>
  </div>

  <div class="card-admin lg:col-span-3">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-sm font-bold uppercase tracking-wider text-amber-400" x-text="editingNum ? 'Edit Trade #' + editingNum : 'Add New Trade / Balance Entry'">Add New Trade / Balance Entry</h3>
      <div x-show="editingNum" class="flex gap-1 items-center bg-black p-1.5 rounded-lg border border-gray-800">
        <span class="text-[10px] text-gray-300 uppercase font-bold mr-1">Quick Hit:</span>
        <button type="button" @click="formHitAction('TP1')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-2 py-1 rounded text-[10px] font-bold">TP1 Hit</button>
        <button type="button" @click="formHitAction('TP2')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-2 py-1 rounded text-[10px] font-bold">TP2 Hit</button>
        <button type="button" @click="formHitAction('TP3')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-2 py-1 rounded text-[10px] font-bold">TP3 Hit</button>
        <button type="button" @click="formHitAction('TP4')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-2 py-1 rounded text-[10px] font-bold">TP4 Hit</button>
        <button type="button" @click="formHitAction('BE')" class="bg-amber-600 hover:bg-amber-500 text-white px-2 py-1 rounded text-[10px] font-bold">BE Hit</button>
        <button type="button" @click="formHitAction('SL')" class="bg-red-600 hover:bg-red-500 text-white px-2 py-1 rounded text-[10px] font-bold">SL Hit</button>
      </div>
    </div>

    <form @submit.prevent="submitTrade()" class="flex flex-col gap-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="form-group"><label>Date</label><input type="date" x-model="form.date" required class="[color-scheme:dark]"></div>
        <div class="form-group"><label>Pair / Type</label><input type="text" x-model="form.pair" value="XAU/USD"></div>
        <div class="form-group">
          <label>Direction / Action</label>
          <select x-model="form.direction">
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
            <option value="BUY LIMIT">BUY LIMIT</option>
            <option value="SELL LIMIT">SELL LIMIT</option>
          </select>
        </div>
        <div class="form-group">
          <label>Channel</label>
          <select x-model="form.channel"><option value="VIP">VIP</option><option value="FREE">FREE</option></select>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div class="form-group"><label>Entry 1</label><input type="number" x-model="form.entry1" step="0.01" placeholder="2350.00"></div>
        <div class="form-group"><label>Entry 2</label><input type="number" x-model="form.entry2" step="0.01" placeholder="2345.00"></div>
        <div class="form-group"><label>Stop Loss (SL)</label><input type="number" x-model="form.sl" step="0.01" placeholder="2330.00"></div>
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="form-group"><label>TP 1</label><input type="number" x-model="form.tp1" step="0.01" placeholder="2360.00"></div>
        <div class="form-group"><label>TP 2</label><input type="number" x-model="form.tp2" step="0.01" placeholder="2370.00"></div>
        <div class="form-group"><label>TP 3</label><input type="number" x-model="form.tp3" step="0.01" placeholder="2380.00"></div>
        <div class="form-group"><label>TP 4</label><input type="number" x-model="form.tp4" step="0.01" placeholder="2390.00"></div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div class="form-group"><label>Pips</label><input type="number" x-model="form.pips" step="0.1"></div>
        <div class="form-group"><label>Profit ($)</label><input type="number" x-model="form.profit" step="0.01"></div>
        <div class="form-group">
          <label>Result</label>
          <select x-model="form.result">
            <option value="WIN">WIN</option>
            <option value="LOSS">LOSS</option>
            <option value="BE">BE</option>
          </select>
        </div>
      </div>

      <div class="flex items-center gap-2 mt-2 bg-[#161b22] p-3 rounded-lg border border-gray-800">
        <input type="checkbox" x-model="sendToTg" class="w-4 h-4 accent-amber-500 cursor-pointer" id="send-to-telegram">
        <label for="send-to-telegram" class="text-xs text-amber-300 font-bold uppercase cursor-pointer">🚀 Send updated signal directly to Telegram on save</label>
      </div>

      <div class="flex gap-2 mt-2">
        <button type="submit" class="flex-1 bg-green-600 hover:bg-green-500 text-black font-bold py-2.5 rounded text-xs uppercase" x-text="editingNum ? 'Save Changes & Send' : 'Save Trade & Send'">Save Trade & Send</button>
        <button type="button" @click="resetForm()" class="bg-gray-700 hover:bg-gray-600 text-white px-6 rounded text-xs font-bold">Reset</button>
      </div>
    </form>
  </div>

  <div class="card-admin lg:col-span-3 overflow-x-auto">
    <h3 class="text-sm font-bold uppercase tracking-wider text-gray-200 mb-4">Manage Saved Trades</h3>
    <table class="w-full text-left text-xs text-gray-200">
      <thead class="bg-[#1f242c] uppercase text-gray-100 font-bold">
        <tr>
          <th class="p-2">#</th>
          <th class="p-2">Date</th>
          <th class="p-2">Dir</th>
          <th class="p-2">Entry 1/2</th>
          <th class="p-2">SL</th>
          <th class="p-2">TPs</th>
          <th class="p-2">Pips</th>
          <th class="p-2">Profit</th>
          <th class="p-2">Result</th>
          <th class="p-2">Quick Hit Status / Actions</th>
        </tr>
      </thead>
      <tbody class="font-mono">
        @forelse($trades as $trade)
          @php
            $entries = array_filter([$trade->entry1 ?? null, $trade->entry2 ?? null]);
            $entries = $entries ? implode('/', $entries) : '—';
            $slVal = ($trade->sl ?? null) !== null && ($trade->sl ?? '') !== '' ? $trade->sl : '—';
            $tps = array_filter([$trade->tp1 ?? null, $trade->tp2 ?? null, $trade->tp3 ?? null, $trade->tp4 ?? null]);
            $tps = $tps ? implode(',', $tps) : '—';
          @endphp
          <tr class="border-b border-gray-800 hover:bg-gray-800/20">
            <td class="p-2 text-white font-bold">{{ $trade->no ?? $trade->id }}</td>
            <td class="p-2 text-gray-200">{{ $trade->date }}</td>
            <td class="p-2 font-bold {{ str_contains(strtoupper($trade->direction ?? ''), 'BUY') ? 'text-blue-400' : 'text-amber-400' }}">{{ $trade->direction }}</td>
            <td class="p-2 text-xs text-gray-200">{{ $entries }}</td>
            <td class="p-2 text-xs text-red-400 font-bold">{{ $slVal }}</td>
            <td class="p-2 text-xs text-gray-200">{{ $tps }}</td>
            <td class="p-2 text-gray-100">{{ $trade->pips }}</td>
            <td class="p-2 text-white font-bold">${{ $trade->profit }}</td>
            <td class="p-2">
              <span class="{{ strtoupper($trade->result ?? '') === 'WIN' ? 'text-green-400' : (strtoupper($trade->result ?? '') === 'LOSS' ? 'text-red-400' : 'text-amber-400') }} font-bold">{{ strtoupper($trade->result) }}</span>
            </td>
            <td class="p-2">
              <div class="flex flex-wrap gap-1 mb-1">
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'TP1')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">TP1 Hit</button>
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'TP2')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">TP2 Hit</button>
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'TP3')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">TP3 Hit</button>
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'TP4')" class="bg-emerald-600 hover:bg-emerald-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">TP4 Hit</button>
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'BE')" class="bg-amber-600 hover:bg-amber-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">BE Hit</button>
                <button type="button" @click="hitAction({{ $trade->no ?? $trade->id }}, 'SL')" class="bg-red-600 hover:bg-red-500 text-white px-1.5 py-0.5 rounded text-[10px] font-bold">SL Hit</button>
              </div>
              <div class="flex gap-1">
                <button type="button" @click="editTrade({{ $trade->no ?? $trade->id }})" class="bg-amber-500/30 text-amber-300 border border-amber-500/50 px-2 py-0.5 rounded text-[10px] font-bold">Edit</button>
                <button type="button" @click="deleteTrade({{ $trade->no ?? $trade->id }})" class="bg-red-500/30 text-red-300 border border-red-500/50 px-2 py-0.5 rounded text-[10px] font-bold">Del</button>
              </div>
            </td>
          </tr>
        @empty
          <tr><td colspan="10" class="p-2 text-center text-gray-400">No trades saved yet.</td></tr>
        @endforelse
      </tbody>
    </table>
  </div>

  <div x-show="modal.show" x-transition class="custom-modal-overlay" style="display: none;" @click.self="modal.show = false">
    <div class="custom-modal-box">
      <h3 class="text-base font-bold text-amber-400 uppercase tracking-wider mb-3" x-text="'🎯 ' + modal.title + ' HIT DETAILS'"></h3>
      <p class="text-xs text-gray-200 mb-4" x-text="'Enter Pips and Dollar Profit for Trade ' + modal.title"></p>

      <div class="form-group mb-3">
        <label class="text-xs text-gray-200 uppercase">Pips Amount</label>
        <input type="number" x-model="modal.pips" step="0.1" class="w-full bg-black border border-gray-700 text-white rounded p-2 text-sm outline-none focus:border-amber-500" placeholder="e.g. 50">
      </div>

      <div class="form-group mb-5">
        <label class="text-xs text-gray-200 uppercase">Profit Dollar ($)</label>
        <input type="number" x-model="modal.profit" step="0.01" class="w-full bg-black border border-gray-700 text-white rounded p-2 text-sm outline-none focus:border-amber-500" placeholder="e.g. 20.00">
      </div>

      <div class="flex gap-2">
        <button type="button" @click="modalResolve({ pips: modal.pips, profit: modal.profit })" class="flex-1 bg-amber-500 hover:bg-amber-400 text-black font-bold py-2 rounded text-xs uppercase">Submit</button>
        <button type="button" @click="modalResolve(null)" class="bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded text-xs font-bold">Cancel</button>
      </div>
    </div>
  </div>

  <div class="toast-custom" :class="{ 'show': toastVisible, 'error': toastType === 'error' }" x-text="toastMsg"></div>

</div>

<script>
function adminApp() {
  return {
    startBalance: {{ number_format($startBalance, 2, '.', '') }},
    depositBalance: {{ number_format($depositBalance, 2, '.', '') }},
    withdrawBalance: {{ number_format($withdrawBalance, 2, '.', '') }},

    showBalance: true,
    showTgSettings: true,
    showGsSettings: true,

    tgToken: '',
    tgChatId: '',
    summaryType: 'DAILY',
    summaryDate: '',

    trades: @json($tradesJson),
    editingNum: null,
    sendToTg: true,
    form: {
      date: new Date().toLocaleDateString('en-CA'),
      pair: 'XAU/USD',
      direction: 'BUY',
      channel: 'VIP',
      entry1: '',
      entry2: '',
      sl: '',
      tp1: '',
      tp2: '',
      tp3: '',
      tp4: '',
      pips: '',
      profit: '',
      result: 'WIN'
    },

    modal: { show: false, title: '', pips: 0, profit: 0 },
    modalResolve: null,

    toastVisible: false,
    toastMsg: '',
    toastType: 'success',

    init() {
      this.loadSettings();
      this.$watch('startBalance', () => this.renderCharts());
      this.$watch('depositBalance', () => this.renderCharts());
      this.$watch('withdrawBalance', () => this.renderCharts());
      this.$nextTick(() => this.renderCharts());
    },

    loadSettings() {
      this.tgToken = localStorage.getItem('sx_tg_token') || '';
      this.tgChatId = localStorage.getItem('sx_tg_chat') || '';
    },

    async saveBalances() {
      try {
        const resp = await fetch('{{ route("admin.settings.update") }}', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': '{{ csrf_token() }}',
            'Accept': 'application/json'
          },
          body: JSON.stringify({
            start_balance: this.startBalance,
            deposit_balance: this.depositBalance,
            withdraw_balance: this.withdrawBalance
          })
        });
        if (resp.ok) {
          this.showToast('✓ Balances saved');
        } else {
          this.showToast('Failed to save balances', 'error');
        }
      } catch(err) {
        this.showToast('Failed to save balances', 'error');
      }
    },

    showToast(msg, type = 'success') {
      this.toastMsg = msg;
      this.toastType = type;
      this.toastVisible = true;
      setTimeout(() => { this.toastVisible = false; }, 2500);
    },

    renderCharts() {
      const data = [...this.trades].sort((a, b) => a.no - b.no);
      const balance = this.startBalance + this.depositBalance - this.withdrawBalance;
      let bal = balance;
      const labels = ['Start'];
      const points = [bal];
      data.forEach(t => {
        bal += (t.profit || 0);
        labels.push('T' + t.no);
        points.push(bal);
      });

      const eqCanvas = document.getElementById('equityChart');
      const distCanvas = document.getElementById('distributionChart');
      if (eqCanvas && !this._eqChart) {
        this._eqChart = new Chart(eqCanvas, {
          type: 'line',
          data: {
            labels: labels,
            datasets: [{
              label: 'Account Balance ($)',
              data: points,
              borderColor: '#10b981',
              backgroundColor: 'rgba(16, 185, 129, 0.15)',
              fill: true, tension: 0.2
            }]
          },
          options: {
            responsive: true, maintainAspectRatio: false,
            scales: {
              x: { grid: { color: '#2a3a32' }, ticks: { color: '#d1d5db' } },
              y: { grid: { color: '#2a3a32' }, ticks: { color: '#d1d5db' } }
            },
            plugins: { legend: { labels: { color: '#ffffff' } } }
          }
        });
      }
    },

    async submitTrade() {
      const rec = {
        no: this.editingNum || (this.trades.length > 0 ? Math.max(...this.trades.map(t => t.no)) + 1 : 1),
        date: this.form.date,
        pair: this.form.pair || 'XAU/USD',
        direction: this.form.direction,
        entry1: this.form.entry1 ? Number(this.form.entry1) : '',
        entry2: this.form.entry2 ? Number(this.form.entry2) : '',
        sl: this.form.sl !== '' ? Number(this.form.sl) : '',
        tp1: this.form.tp1 ? Number(this.form.tp1) : '',
        tp2: this.form.tp2 ? Number(this.form.tp2) : '',
        tp3: this.form.tp3 ? Number(this.form.tp3) : '',
        tp4: this.form.tp4 ? Number(this.form.tp4) : '',
        pips: Number(this.form.pips) || 0,
        profit: Number(this.form.profit) || 0,
        result: this.form.result,
        channel: this.form.channel
      };

      const csrf = '{{ csrf_token() }}';
      const headers = { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrf, 'Accept': 'application/json' };

      try {
        let resp;
        if (this.editingNum) {
          const trade = this.trades.find(t => t.no === this.editingNum);
          const tradeId = trade ? (trade.id || trade.no) : this.editingNum;
          resp = await fetch(`/admin/trades/${tradeId}`, { method: 'PUT', headers, body: JSON.stringify(rec) });
        } else {
          resp = await fetch('{{ route("admin.trades.store") }}', { method: 'POST', headers, body: JSON.stringify(rec) });
        }
        if (resp.ok) {
          this.showToast(this.editingNum ? `✓ Updated Trade #${this.editingNum}` : `✓ Trade #${rec.no} Added`);
          if (this.sendToTg) {
            this.sendToTelegram(rec);
          }
          setTimeout(() => window.location.reload(), 500);
        } else {
          const err = await resp.json();
          this.showToast(err.message || 'Save failed', 'error');
        }
      } catch(e) {
        if (this.editingNum) {
          const idx = this.trades.findIndex(t => t.no === this.editingNum);
          if (idx >= 0) this.trades[idx] = rec;
        } else {
          this.trades.push(rec);
        }
        this.showToast('Saved locally', 'success');
      }
      this.resetForm();
      this.$nextTick(() => this.renderCharts());
    },

    async sendToTelegram(rec) {
      const token = this.tgToken.trim();
      const chatId = this.tgChatId.trim();
      if (!token || !chatId) return;

      let entries = [rec.entry1, rec.entry2].filter(Boolean).join(' / ');
      let slVal = rec.sl !== undefined && rec.sl !== '' ? rec.sl : 'N/A';

      let message = `🚨 SIGNALXPRESS SIGNAL UPDATE 🚨\n\n` +
        `💎 Pairs: ${rec.pair}\n` +
        `📊 Type:  ${rec.direction}\n` +
        `📥 Entry:  ${entries || 'N/A'}\n\n` +
        `🎯 TP1:   ${rec.tp1 || 'N/A'}\n` +
        `🎯 TP2:  ${rec.tp2 || 'N/A'}\n` +
        `🎯 TP3:  ${rec.tp3 || 'N/A'}\n` +
        `🎯 TP4:  ${rec.tp4 || 'N/A'}\n\n` +
        `🛑 SL:   ${slVal}\n\n` +
        `Follow Money Management🙏🙏🙏\n` +
        `🔥 GET RISK WIN YOUR LIFE ?`;

      try {
        await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ chat_id: chatId, text: message })
        });
      } catch(err) {
        console.error('Telegram send failed:', err);
      }
    },

    editTrade(no) {
      const t = this.trades.find(x => x.no === no);
      if (!t) return;
      this.editingNum = no;
      this.form.date = t.date;
      this.form.pair = t.pair;
      this.form.direction = t.direction;
      this.form.entry1 = t.entry1 !== undefined ? t.entry1 : '';
      this.form.entry2 = t.entry2 !== undefined ? t.entry2 : '';
      this.form.sl = t.sl !== undefined ? t.sl : '';
      this.form.tp1 = t.tp1 !== undefined ? t.tp1 : '';
      this.form.tp2 = t.tp2 !== undefined ? t.tp2 : '';
      this.form.tp3 = t.tp3 !== undefined ? t.tp3 : '';
      this.form.tp4 = t.tp4 !== undefined ? t.tp4 : '';
      this.form.pips = t.pips;
      this.form.profit = t.profit;
      this.form.result = t.result;
      this.form.channel = t.channel;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    async deleteTrade(no) {
      if (!confirm(`Delete Trade #${no}?`)) return;
      const trade = this.trades.find(t => t.no === no);
      const tradeId = trade ? (trade.id || trade.no) : no;
      try {
        await fetch(`/admin/trades/${tradeId}`, {
          method: 'DELETE',
          headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}', 'Accept': 'application/json' }
        });
      } catch(e) {}
      this.trades = this.trades.filter(t => t.no !== no);
      this.showToast('Trade Deleted', 'error');
      this.$nextTick(() => this.renderCharts());
    },

    resetForm() {
      this.editingNum = null;
      this.form = {
        date: new Date().toLocaleDateString('en-CA'),
        pair: 'XAU/USD',
        direction: 'BUY',
        channel: 'VIP',
        entry1: '', entry2: '', sl: '',
        tp1: '', tp2: '', tp3: '', tp4: '',
        pips: '', profit: '', result: 'WIN'
      };
      this.sendToTg = true;
    },

    async hitAction(no, actionType) {
      const t = this.trades.find(x => x.no === no);
      if (!t) return;
      let defaultPips = actionType === 'BE' ? 0 : actionType === 'SL' ? -30 : actionType === 'TP1' ? 30 : actionType === 'TP2' ? 60 : actionType === 'TP3' ? 100 : 150;
      let defaultProfit = actionType === 'BE' ? 0 : actionType === 'SL' ? -10 : actionType === 'TP1' ? 10 : actionType === 'TP2' ? 20 : actionType === 'TP3' ? 35 : 50;

      const result = await this.openHitModal(actionType, t.pips || defaultPips, t.profit || defaultProfit);
      if (!result) return;

      t.pips = result.pips;
      t.profit = result.profit;
      if (actionType === 'SL') t.result = 'LOSS';
      else if (actionType === 'BE') t.result = 'BE';
      else t.result = 'WIN';

      const tradeId = t.id || t.no;
      try {
        await fetch(`/admin/trades/${tradeId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}', 'Accept': 'application/json' },
          body: JSON.stringify({ date: t.date, pair: t.pair, direction: t.direction, entry1: t.entry1, entry2: t.entry2, sl: t.sl, tp1: t.tp1, tp2: t.tp2, tp3: t.tp3, tp4: t.tp4, pips: t.pips, profit: t.profit, result: t.result, channel: t.channel, hit_type: actionType })
        });
      } catch(e) {}

      this.sendToTelegramWithHit(t, actionType);

      this.showToast(`✓ Trade #${no} marked as ${actionType} Hit!`);
      this.$nextTick(() => this.renderCharts());
    },

    async sendToTelegramWithHit(rec, hitName) {
      const token = this.tgToken.trim();
      const chatId = this.tgChatId.trim();
      if (!token || !chatId) return;

      let entries = [rec.entry1, rec.entry2].filter(Boolean).join(' / ');

      let message = `🎯 QUICK HIT: ${hitName} HIT! 🎯\n\n` +
        `💎 Pairs: ${rec.pair}\n` +
        `📊 Type: ${rec.direction}\n` +
        `📥 Entry:  ${entries || 'N/A'}\n` +
        `📈 Status: ${hitName} Achieved\n` +
        `🏆 Result: ${rec.result} (${rec.pips} Pips | $${rec.profit})\n\n` +
        `🔥 GET RISK WIN YOUR LIFE ?`;

      try {
        await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ chat_id: chatId, text: message })
        });
      } catch(err) {
        console.error('Telegram hit send failed:', err);
      }
    },

    async formHitAction(actionType) {
      if (!this.editingNum) return;
      const t = this.trades.find(x => x.no === this.editingNum);
      if (!t) return;
      let defaultPips = actionType === 'BE' ? 0 : actionType === 'SL' ? -30 : actionType === 'TP1' ? 30 : actionType === 'TP2' ? 60 : actionType === 'TP3' ? 100 : 150;
      let defaultProfit = actionType === 'BE' ? 0 : actionType === 'SL' ? -10 : actionType === 'TP1' ? 10 : actionType === 'TP2' ? 20 : actionType === 'TP3' ? 35 : 50;

      const result = await this.openHitModal(actionType, this.form.pips || defaultPips, this.form.profit || defaultProfit);
      if (!result) return;

      t.pips = result.pips;
      t.profit = result.profit;
      this.form.pips = result.pips;
      this.form.profit = result.profit;

      if (actionType === 'SL') { t.result = 'LOSS'; this.form.result = 'LOSS'; }
      else if (actionType === 'BE') { t.result = 'BE'; this.form.result = 'BE'; }
      else { t.result = 'WIN'; this.form.result = 'WIN'; }

      this.showToast(`✓ Trade #${this.editingNum} marked as ${actionType} Hit!`);
      this.$nextTick(() => this.renderCharts());
    },

    openHitModal(actionType, defaultPips, defaultProfit) {
      return new Promise((resolve) => {
        this.modal = { show: true, title: actionType, pips: defaultPips, profit: defaultProfit };
        this.modalResolve = (val) => {
          this.modal.show = false;
          resolve(val);
        };
      });
    },

    async testTelegramConnection() {
      const token = this.tgToken.trim();
      const chatId = this.tgChatId.trim();
      if (!token || !chatId) { this.showToast('Please enter Bot Token and Chat ID first!', 'error'); return; }

      const message = `📢 *SIGNAL XPRESS වෙත සාදරයෙන් පිළිගනිමු!*\nඔබ දැන් SIGNAL XPRESS Trading Community එකේ සාමාජිකයෙකි.\n\n⚠️ *වැදගත් අවවාදය*\nTrading කියන්නේ ඉක්මනින් සල්ලි හොයන ක්‍රමයක් නොවෙයි. ලාභ ලැබෙන වගේම පාඩුත් ලැබිය හැකියි. ඔබට අහිමි වුවහොත් දරාගත හැකි මුදලක් පමණක් ආයෝජනය කරන්න. Risk Management පිළිපදින්න, හැඟීම් මත නොව සැලසුමකට අනුව Trade කරන්න.\n\nSIGNAL XPRESS මඟින් ලබාදෙන Signals සහ සේවාවන් ඔබගේ Trading තීරණ සඳහා සහායක් පමණි. අවසාන Trading තීරණය සහ එහි වගකීම ඔබ සතුය.\n\nඔබගේ Trading ගමනට සුභ පැතුම්! 🚀`;

      try {
        this.showToast('Sending test message...');
        const resp = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ chat_id: chatId, text: message, parse_mode: 'Markdown' })
        });
        const data = await resp.json();
        if (data.ok) this.showToast('✓ Test Message Sent Successfully!');
        else this.showToast('Telegram Error: ' + data.description, 'error');
      } catch(err) { this.showToast('Failed to reach Telegram API', 'error'); }
    },

    async sendSummaryToTelegram() {
      const token = this.tgToken.trim();
      const chatId = this.tgChatId.trim();
      if (!token || !chatId) { this.showToast('Please enter Bot Token and Chat ID first!', 'error'); return; }
      if (!this.summaryDate) { this.showToast('Please select a date first!', 'error'); return; }

      let targetTrades = [];
      const baseDate = new Date(this.summaryDate);
      if (this.summaryType === 'DAILY') {
        targetTrades = this.trades.filter(t => t.date === this.summaryDate);
      } else if (this.summaryType === 'WEEKLY') {
        const day = baseDate.getDay();
        const diffToMonday = baseDate.getDate() - day + (day === 0 ? -6 : 1);
        const monday = new Date(baseDate); monday.setDate(diffToMonday); monday.setHours(0,0,0,0);
        const sunday = new Date(monday); sunday.setDate(monday.getDate() + 6); sunday.setHours(23,59,59,999);
        targetTrades = this.trades.filter(t => { const d = new Date(t.date); return d >= monday && d <= sunday; });
      } else if (this.summaryType === 'MONTHLY') {
        const firstDay = new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
        const lastDay = new Date(baseDate.getFullYear(), baseDate.getMonth() + 1, 0);
        lastDay.setHours(23,59,59,999);
        targetTrades = this.trades.filter(t => { const d = new Date(t.date); return d >= firstDay && d <= lastDay; });
      }

      if (targetTrades.length === 0) { this.showToast(`No trades found for selected period!`, 'error'); return; }

      let wins = targetTrades.filter(t => t.result === 'WIN').length;
      let losses = targetTrades.filter(t => t.result === 'LOSS').length;
      let bes = targetTrades.filter(t => t.result === 'BE').length;
      let totalPips = targetTrades.reduce((s,t) => s + (t.pips || 0), 0);
      let totalProfit = targetTrades.reduce((s,t) => s + (t.profit || 0), 0);
      let winRate = ((wins / targetTrades.length) * 100).toFixed(1);

      let tradeDetails = targetTrades.map((t, i) => {
        let emoji = t.result === 'WIN' ? '✅' : t.result === 'LOSS' ? '❌' : '🪙';
        return `${i+1}. ${emoji} *${t.pair}* (${t.direction})\n   └ Result: ${t.result} | ${t.pips >= 0 ? '+' : ''}${t.pips} Pips | $${t.profit}`;
      }).join('\n\n');

      let message = `📊 *SIGNAL XPRESS - ${this.summaryType} SUMMARY* 📊\n\n━━━━━━━━━━━━━━━━━━━\n\n🎯 *Total Signals:* ${targetTrades.length}\n✅ *Wins:* ${wins}\n❌ *Losses:* ${losses}\n🪙 *BE Trades:* ${bes}\n📈 *Win Rate:* ${winRate}%\n🌐 *Net Pips:* ${totalPips >= 0 ? '+' : ''}${totalPips} Pips\n💰 *Net Profit:* $${totalProfit.toFixed(2)}\n\n━━━━━━━━━━━━━━━━━━━\n📝 *TRADES BREAKDOWN:*\n\n${tradeDetails}\n\n━━━━━━━━━━━━━━━━━━━\n🔥 *GET RISK WIN YOUR LIFE* 🔥\n⚡️ Signal Xpress Official Trading Community`;

      try {
        this.showToast(`Sending ${this.summaryType} Summary...`);
        const resp = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ chat_id: chatId, text: message, parse_mode: 'Markdown' })
        });
        const data = await resp.json();
        if (data.ok) this.showToast(`✓ ${this.summaryType} Summary Sent Successfully!`);
        else this.showToast('Telegram Error: ' + data.description, 'error');
      } catch(err) { this.showToast('Failed to send summary to Telegram', 'error'); }
    },

    async testGoogleSheetsConnection() {
      this.showToast('Testing Google Sheets connection...');
      try {
        const resp = await fetch('{{ route("admin.gs.test") }}');
        const data = await resp.json();
        if (data.ok) {
          this.showToast(`✓ Connected — ${data.count ?? 0} rows in sheet`);
        } else {
          this.showToast(data.message || data.error || 'Connection failed', 'error');
        }
      } catch(err) {
        this.showToast('Connection test failed', 'error');
      }
    },

    async syncFromGoogleSheets() {
      this.showToast('Syncing from Google Sheets...');
      try {
        const resp = await fetch('{{ route("admin.gs.sync") }}', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': '{{ csrf_token() }}',
            'Accept': 'application/json'
          }
        });
        const data = await resp.json();
        if (resp.ok && data.ok) {
          this.showToast(`✓ Synced ${data.count} trades from Google Sheets`);
          setTimeout(() => window.location.reload(), 800);
        } else {
          this.showToast(data.message || 'Sync failed', 'error');
        }
      } catch(err) {
        this.showToast('Sync failed', 'error');
      }
    },

    downloadJSON() {
      const clean = this.trades.map(t => ({
        no: t.no,
        date: t.date,
        pair: t.pair,
        direction: t.direction,
        entry1: t.entry1 ?? '',
        entry2: t.entry2 ?? '',
        sl: t.sl ?? '',
        tp1: t.tp1 ?? '',
        tp2: t.tp2 ?? '',
        tp3: t.tp3 ?? '',
        tp4: t.tp4 ?? '',
        pips: t.pips || 0,
        profit: t.profit || 0,
        result: t.result,
        channel: t.channel || 'VIP'
      }));
      const blob = new Blob([JSON.stringify(clean, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'trades.json'; a.click();
      URL.revokeObjectURL(url);
    },

    async importJSON(event) {
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async (e) => {
        try {
          const data = JSON.parse(e.target.result);
          if (!Array.isArray(data)) {
            this.showToast('Invalid JSON: expected an array', 'error');
            return;
          }
          this.showToast(`Importing ${data.length} trades...`);
          const resp = await fetch('{{ route("admin.trades.import") }}', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'X-CSRF-TOKEN': '{{ csrf_token() }}',
              'Accept': 'application/json'
            },
            body: JSON.stringify({ trades: data })
          });
          const result = await resp.json();
          if (resp.ok) {
            this.showToast(`✓ ${result.count} trades imported`);
            setTimeout(() => window.location.reload(), 800);
          } else {
            this.showToast(result.message || 'Import failed', 'error');
          }
        } catch (err) {
          this.showToast('Invalid JSON file', 'error');
        }
        event.target.value = '';
      };
      reader.readAsText(file);
    }
  };
}
</script>

</x-layouts.admin>
