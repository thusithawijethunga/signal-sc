<x-layouts.admin>

<div x-data="dashboardApp()">
<div class="w-full text-center py-6 mb-0 bg-gradient-to-r from-[#1f242c] via-[#bd2828]/25 to-[#10b981]/25 border border-[#30363d] rounded-t-xl shadow-lg flex flex-col gap-3">
  <h2 class="dynamic-banner-text text-2xl md:text-3xl font-black tracking-[0.2em] uppercase font-mono">🔥 GET RISK WIN YOUR LIFE 🔥</h2>
  <p class="dynamic-banner-text text-lg md:text-xl font-bold tracking-wider">"අවදානමක් ගන්න ජිවිතේ දිනන්න"</p>
  <div class="mt-1 flex flex-col items-center gap-1">
    <a href="https://t.me/signalxpress_official" target="_blank" class="flex items-center gap-2 bg-[#229ED9]/20 hover:bg-[#229ED9]/30 text-[#229ED9] border border-[#229ED9]/50 px-4 py-1.5 rounded-full text-sm font-bold transition-all shadow-[0_0_15px_rgba(34,158,217,0.25)]">
      <svg class="w-4 h-4 fill-current" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15.82-.7 3.32-.98 4.79-.12.63-.36.84-.58.86-.49.04-.86-.33-1.33-.64-.74-.48-1.16-.78-1.88-1.25-.83-.54-.29-.84.18-1.33.12-.13 2.27-2.08 2.31-2.27.01-.02.01-.11-.04-.16-.05-.05-.12-.03-.18-.02-.07.01-1.28.81-3.6 2.38-.34.23-.65.35-.93.34-.31 0-.91-.17-1.36-.32-.55-.18-.99-.28-.95-.59.02-.16.24-.33.67-.51 2.62-1.14 4.37-1.89 5.25-2.25 2.5-1.02 3.02-1.2 3.36-1.2.07 0 .24.02.35.1.09.07.12.17.13.25-.01.08 0 .26-.02.34z"/></svg>
      Signal Xpress Official Telegram Channel ➔
    </a>
  </div>
</div>

<div class="w-full overflow-hidden bg-[#161b22] border-x border-b border-[#30363d] rounded-b-xl py-2 mb-8 shadow-inner flex items-center">
  <div class="whitespace-nowrap flex gap-8 animate-[marquee_30s_linear_infinite]">
    <span class="text-sm text-gray-200">🚀 Welcome to SIGNALXPRESS (PRIVATE) LIMITED | 💎 VIP Forex & Gold Signals | 📊 SMC Indicators | 🤖 Auto Trading Bots | 📞 Call 0753773228</span>
  </div>
</div>

<div class="flex flex-col lg:flex-row justify-between items-center mb-6 gap-4 bg-[#161b22] p-4 rounded-xl border border-[#30363d]">
  <div class="flex flex-wrap items-center gap-3">
    <div class="flex flex-wrap items-center gap-1.5 bg-black p-1 rounded-lg border border-gray-800">
      <button type="button" @click="filterByPeriod('ALL')" class="period-btn" :class="{ 'active': currentPeriod === 'ALL' }">All Time</button>
      <button type="button" @click="filterByPeriod('THIS_WEEK')" class="period-btn" :class="{ 'active': currentPeriod === 'THIS_WEEK' }">This Week</button>
      <button type="button" @click="filterByPeriod('LAST_WEEK')" class="period-btn" :class="{ 'active': currentPeriod === 'LAST_WEEK' }">Last Week</button>
      <button type="button" @click="filterByPeriod('LAST_MONTH')" class="period-btn" :class="{ 'active': currentPeriod === 'LAST_MONTH' }">Last Month</button>
    </div>

    <div class="flex items-center gap-2">
      <label class="text-xs font-semibold uppercase" style="color: var(--gold)">📅 Calendar:</label>
      <input type="date" x-model="dateFilter" @change="filterByDate()" class="bg-black text-white border border-gray-700 px-3 py-1.5 rounded-lg outline-none cursor-pointer [color-scheme:dark]">
      <button @click="clearDateFilter()" class="text-xs bg-red-950/60 text-red-300 hover:bg-red-900/80 px-2.5 py-1.5 rounded border border-red-800">Clear Filter</button>
    </div>
  </div>
  <div class="flex gap-4 text-sm text-gray-300">
    <div>Start Balance: <span class="text-amber-400 font-bold">${{ number_format($startBalance, 2) }}</span></div>
    <div>Status: <span class="text-green-400 font-semibold">● Auto-Sync Active</span></div>
  </div>
</div>

<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-4 mb-4">
  <div class="card-admin p-5">
    <span class="text-xs text-gray-300 uppercase font-semibold">Start Balance</span>
    <div class="text-2xl font-bold text-amber-400 mt-2">${{ number_format($startBalance, 2) }}</div>
  </div>
  <div class="card-admin p-5 border-l-4 border-l-sky-500">
    <span class="text-xs text-gray-300 uppercase font-semibold">Deposit Balance</span>
    <div class="text-2xl font-bold text-sky-400 mt-2">${{ number_format($depositBalance, 2) }}</div>
  </div>
  <div class="card-admin p-5 border-l-4 border-l-rose-500">
    <span class="text-xs text-gray-300 uppercase font-semibold">Withdraw Balance</span>
    <div class="text-2xl font-bold text-rose-400 mt-2">${{ number_format($withdrawBalance, 2) }}</div>
  </div>
  <div class="card-admin p-5 border-l-4 border-l-blue-500">
    <span class="text-xs text-gray-300 uppercase font-semibold">Net Profit (<span class="text-gold" style="color: var(--gold)" x-text="periodLabel">All Time</span>)</span>
    <div class="text-2xl font-bold text-blue-400 mt-2" x-text="'$' + Math.abs(computedMetrics.totalProfit).toFixed(2)">${{ number_format(abs($totalProfit), 2) }}</div>
  </div>
  <div class="card-admin p-5 border-l-4 border-l-emerald-500">
    <span class="text-xs text-gray-300 uppercase font-semibold">Current Balance</span>
    <div class="text-2xl font-bold text-emerald-400 mt-2" x-text="'$' + computedMetrics.currentBalance.toFixed(2)">${{ number_format($startBalance + $depositBalance - $withdrawBalance + $totalProfit, 2) }}</div>
  </div>
  <div class="card-admin p-5 border-l-4 border-l-purple-500">
    <span class="text-xs text-gray-300 uppercase font-semibold">Win Rate (<span class="text-gold" style="color: var(--gold)" x-text="periodLabel">All Time</span>)</span>
    <div class="text-2xl font-bold text-white mt-2" x-text="computedMetrics.winRate + '%'">{{ $totalTrades > 0 ? round(($wins / $totalTrades) * 100, 1) . '%' : '0%' }}</div>
  </div>
</div>

<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-4 mb-8">
  <div class="card-admin p-4"><span class="text-xs text-gray-300 uppercase">Total Trades</span><div class="text-xl font-bold text-gray-100 mt-1" x-text="computedMetrics.totalTrades">{{ $totalTrades }}</div></div>
  <div class="card-admin p-4 border-b-2 border-b-green-500"><span class="text-xs text-green-400 uppercase font-semibold">Wins</span><div class="text-xl font-bold text-green-400 mt-1" x-text="computedMetrics.wins">{{ $wins }}</div></div>
  <div class="card-admin p-4 border-b-2 border-b-red-500"><span class="text-xs text-red-400 uppercase font-semibold">Losses</span><div class="text-xl font-bold text-red-400 mt-1" x-text="computedMetrics.losses">{{ $losses }}</div></div>
  <div class="card-admin p-4 border-b-2 border-b-amber-500"><span class="text-xs text-amber-400 uppercase font-semibold">BE Trades</span><div class="text-xl font-bold text-amber-400 mt-1" x-text="computedMetrics.bes">{{ $bes }}</div></div>
  <div class="card-admin p-4"><span class="text-xs text-purple-400 uppercase font-semibold">Net Pips Gain</span><div class="text-xl font-bold mt-1" :class="computedMetrics.netPips >= 0 ? 'text-emerald-400' : 'text-red-400'" x-text="fmtPips(computedMetrics.netPips)">{{ $netPips > 0 ? '+' : '' }}{{ number_format($netPips, 1) }}</div></div>
  <div class="card-admin p-4 border-l-2 border-l-red-500"><span class="text-xs text-red-400 uppercase font-semibold">Total Loss Pips</span><div class="text-xl font-bold text-red-400 mt-1" x-text="fmtPips(computedMetrics.lossPips)">{{ number_format($lossPips, 1) }}</div></div>
</div>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
  <div class="card-admin lg:col-span-2">
    <h3 class="text-base font-semibold text-white mb-4">Account Growth History Curve</h3>
    <div style="position: relative; height:280px;"><canvas id="equityChart"></canvas></div>
  </div>
  <div class="card-admin">
    <h3 class="text-base font-semibold text-white mb-4">Win vs Loss vs BE Distribution</h3>
    <div style="position: relative; height:280px;" class="flex justify-center items-center"><canvas id="distributionChart"></canvas></div>
  </div>
</div>

<div class="card-admin">
  <div class="flex justify-between items-center mb-4">
    <h3 class="text-base font-semibold text-white">Signal Ledger Database</h3>
    <div class="flex gap-2">
      <button @click="filterResult('ALL')" class="px-3 py-1 text-xs rounded bg-gray-700 text-white font-bold">All</button>
      <button @click="filterResult('WIN')" class="px-3 py-1 text-xs rounded bg-green-950 text-green-300 font-bold border border-green-800">Wins</button>
      <button @click="filterResult('LOSS')" class="px-3 py-1 text-xs rounded bg-red-950 text-red-300 font-bold border border-red-800">Losses</button>
      <button @click="filterResult('BE')" class="px-3 py-1 text-xs rounded bg-amber-950 text-amber-300 font-bold border border-amber-800">BE</button>
    </div>
  </div>
  <div class="overflow-x-auto">
    <table class="w-full text-left text-sm text-gray-200">
      <thead class="bg-[#1f242c] text-xs uppercase text-gray-100 font-bold">
        <tr>
          <th class="p-3">#</th>
          <th class="p-3">Date</th>
          <th class="p-3">Pair</th>
          <th class="p-3">Direction</th>
          <th class="p-3">Entry 1 / 2</th>
          <th class="p-3">SL</th>
          <th class="p-3">TP1</th>
          <th class="p-3">TP2</th>
          <th class="p-3">TP3</th>
          <th class="p-3">TP4</th>
          <th class="p-3 text-right">Pips</th>
          <th class="p-3 text-right">Profit ($)</th>
          <th class="p-3 text-center">Result</th>
        </tr>
      </thead>
      <tbody class="font-mono">
        <template x-for="t in paginatedData" :key="t.no || t.id">
          <tr class="border-b border-gray-800 hover:bg-gray-800/40">
            <td class="p-3 text-white font-bold" x-text="t.no || t.id"></td>
            <td class="p-3 text-xs text-gray-200" x-text="t.date"></td>
            <td class="p-3 text-white font-bold" x-text="t.pair"></td>
            <td class="p-3 font-bold" :class="String(t.direction).includes('BUY') ? 'text-blue-400' : 'text-amber-400'" x-text="t.direction"></td>
            <td class="p-3 text-xs text-gray-200" x-text="[t.entry1, t.entry2].filter(Boolean).join(' / ') || '—'"></td>
            <td class="p-3 text-xs font-bold" :class="isSlHit(t) ? 'bg-red-900/40 text-red-200 border border-red-700 rounded px-1.5 py-0.5' : 'text-red-400'" x-text="t.sl || '—'"></td>
            <td class="p-3"><span class="inline-block px-1.5 py-0.5 rounded text-xs font-bold border" :class="tpBadgeClass(t, 1)" x-text="t.tp1 || '—'"></span></td>
            <td class="p-3"><span class="inline-block px-1.5 py-0.5 rounded text-xs font-bold border" :class="tpBadgeClass(t, 2)" x-text="t.tp2 || '—'"></span></td>
            <td class="p-3"><span class="inline-block px-1.5 py-0.5 rounded text-xs font-bold border" :class="tpBadgeClass(t, 3)" x-text="t.tp3 || '—'"></span></td>
            <td class="p-3"><span class="inline-block px-1.5 py-0.5 rounded text-xs font-bold border" :class="tpBadgeClass(t, 4)" x-text="t.tp4 || '—'"></span></td>
            <td class="p-3 text-right text-gray-100 font-semibold" x-text="t.pips"></td>
            <td class="p-3 text-right text-white font-bold" x-text="'$' + (t.profit || 0)"></td>
            <td class="p-3 text-center">
              <span class="px-2 py-0.5 rounded text-xs font-bold"
                :class="t.result === 'WIN' ? 'bg-green-900/60 text-green-300 border border-green-700' : t.result === 'LOSS' ? 'bg-red-900/60 text-red-300 border border-red-700' : 'bg-amber-900/60 text-amber-300 border border-amber-700'"
                x-text="t.result"></span>
              <span x-show="t.hit_level" class="ml-1 px-1.5 py-0.5 rounded text-[10px] font-bold border"
                :class="isSlHit(t) ? 'bg-red-900/60 text-red-300 border-red-700' : isBeHit(t) ? 'bg-amber-900/60 text-amber-300 border-amber-700' : 'bg-emerald-900/60 text-emerald-300 border-emerald-700'"
                x-text="String(t.hit_level).toUpperCase()"></span>
            </td>
          </tr>
        </template>
        <tr x-show="paginatedData.length === 0">
          <td colspan="13" class="p-4 text-center text-gray-400">No records found for selected period/filter.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="flex flex-col sm:flex-row justify-between items-center mt-4 pt-4 border-t border-gray-800 gap-3">
    <div class="flex items-center gap-2">
      <span class="text-xs text-gray-300">Rows per page:</span>
      <select x-model="rowsPerPage" @change="currentPage = 1; recalcPagination()" class="bg-black text-white border border-gray-700 px-2.5 py-1 rounded-lg text-xs outline-none cursor-pointer [color-scheme:dark]">
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="ALL">All</option>
      </select>
    </div>
    <div class="text-xs text-gray-300" x-text="paginationInfo">Showing 0-0 of 0 trades</div>
    <div class="flex items-center gap-1">
      <template x-for="p in paginationPages" :key="p">
        <button
          @click="if(p !== '...') { currentPage = parseInt(p); recalcPagination() }"
          :class="{
            'px-2.5 py-1 rounded bg-black font-bold border shadow': String(currentPage) === String(p),
            'px-2.5 py-1 rounded bg-black text-gray-200 border border-gray-700 hover:bg-gray-800': String(currentPage) !== String(p),
            'text-amber-400 border-amber-500': String(currentPage) === String(p),
            'text-gray-400 px-1': p === '...'
          }"
          x-text="p"
        ></button>
      </template>
    </div>
  </div>
</div>
</div>

<div class="toast-custom" id="toast" :class="{ 'show': toastVisible, 'error': toastType === 'error' }" x-text="toastMsg"></div>

<script>
function dashboardApp() {
  return {
    currentPeriod: 'ALL',
    dateFilter: '',
    periodLabel: 'All Time',

    startBalance: {{ number_format($startBalance, 2, '.', '') }},
    depositBalance: {{ number_format($depositBalance, 2, '.', '') }},
    withdrawBalance: {{ number_format($withdrawBalance, 2, '.', '') }},

    trades: @json($tradesJson),

    currentPage: 1,
    rowsPerPage: '10',
    resultFilter: 'ALL',
    filteredTrades: [],

    toastVisible: false,
    toastMsg: '',
    toastType: 'success',

    chartInstances: {},

    get filteredData() {
      let data = [...this.trades];
      const today = new Date();
      if (this.currentPeriod === 'THIS_WEEK') {
        const day = today.getDay();
        const diffToMonday = today.getDate() - day + (day === 0 ? -6 : 1);
        const monday = new Date(today);
        monday.setDate(diffToMonday);
        monday.setHours(0,0,0,0);
        const sunday = new Date(monday);
        sunday.setDate(monday.getDate() + 6);
        sunday.setHours(23,59,59,999);
        data = data.filter(t => { const d = new Date(t.date); return d >= monday && d <= sunday; });
      } else if (this.currentPeriod === 'LAST_WEEK') {
        const day = today.getDay();
        const diffToMonday = today.getDate() - day + (day === 0 ? -6 : 1);
        const thisMonday = new Date(today);
        thisMonday.setDate(diffToMonday);
        const lastMonday = new Date(thisMonday);
        lastMonday.setDate(thisMonday.getDate() - 7);
        lastMonday.setHours(0,0,0,0);
        const lastSunday = new Date(thisMonday);
        lastSunday.setDate(thisMonday.getDate() - 1);
        lastSunday.setHours(23,59,59,999);
        data = data.filter(t => { const d = new Date(t.date); return d >= lastMonday && d <= lastSunday; });
      } else if (this.currentPeriod === 'LAST_MONTH') {
        const firstDay = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        firstDay.setHours(0,0,0,0);
        const lastDay = new Date(today.getFullYear(), today.getMonth(), 0);
        lastDay.setHours(23,59,59,999);
        data = data.filter(t => { const d = new Date(t.date); return d >= firstDay && d <= lastDay; });
      } else if (this.currentPeriod === 'CUSTOM_DATE' && this.dateFilter) {
        data = data.filter(t => t.date === this.dateFilter);
      }
      if (this.resultFilter !== 'ALL') {
        data = data.filter(t => t.result === this.resultFilter);
      }
      return data;
    },

    get computedMetrics() {
      const data = this.filteredData;
      const totalTrades = data.length;
      const wins = data.filter(t => t.result === 'WIN').length;
      const losses = data.filter(t => t.result === 'LOSS').length;
      const bes = data.filter(t => t.result === 'BE').length;
      const totalProfit = data.reduce((s, t) => s + (t.profit || 0), 0);
      const netPips = data.reduce((s, t) => s + (t.pips || 0), 0);
      const lossPips = data.filter(t => t.pips < 0).reduce((s, t) => s + t.pips, 0);
      const currentBalance = this.startBalance + this.depositBalance - this.withdrawBalance + totalProfit;
      const winRate = totalTrades > 0 ? ((wins / totalTrades) * 100).toFixed(1) : '0';
      return { totalTrades, wins, losses, bes, totalProfit, netPips, lossPips, currentBalance, winRate };
    },

    get paginatedData() {
      const data = [...this.filteredData];
      if (this.rowsPerPage === 'ALL') return data;
      const rpp = parseInt(this.rowsPerPage);
      const totalPages = Math.ceil(data.length / rpp);
      if (this.currentPage > totalPages) this.currentPage = Math.max(totalPages, 1);
      const start = (this.currentPage - 1) * rpp;
      return data.slice(start, start + rpp);
    },

    get paginationInfo() {
      const data = [...this.filteredData];
      if (data.length === 0) return 'Showing 0-0 of 0 trades';
      const rpp = this.rowsPerPage === 'ALL' ? data.length : parseInt(this.rowsPerPage);
      const totalPages = Math.ceil(data.length / rpp);
      if (this.currentPage > totalPages) this.currentPage = totalPages;
      const start = (this.currentPage - 1) * rpp + 1;
      const end = Math.min(this.currentPage * rpp, data.length);
      return `Showing ${start}-${end} of ${data.length} trades`;
    },

    get paginationPages() {
      const data = [...this.filteredData];
      if (this.rowsPerPage === 'ALL') return [];
      const rpp = parseInt(this.rowsPerPage);
      const totalPages = Math.ceil(data.length / rpp);
      if (totalPages <= 1) return [];
      const pages = [];
      for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= this.currentPage - 1 && i <= this.currentPage + 1)) {
          pages.push(i);
        } else if (i === this.currentPage - 2 || i === this.currentPage + 2) {
          pages.push('...');
        }
      }
      return pages;
    },

    init() {
      this.$watch('startBalance', () => this.renderCharts());
      this.$watch('depositBalance', () => this.renderCharts());
      this.$watch('withdrawBalance', () => this.renderCharts());
      this.$nextTick(() => this.renderCharts());

      window.onWsTrade = (d) => this.applyTradeUpdate(d);

      setInterval(() => {
        const last = window.wsStore?.lastTrade;
        if (last && last._applied !== true) {
          last._applied = true;
          this.applyTradeUpdate(last);
        }
      }, 1000);
    },

    applyTradeUpdate(d) {
      if (!d || !d.id) return;
      const idx = this.trades.findIndex(t => t.id === d.id || t.no === d.no);
      if (idx >= 0) {
        this.trades[idx] = { ...this.trades[idx], pips: d.pips, profit: d.profit, result: d.result, hit_level: d.hit_level };
        this.trades = [...this.trades];
      } else if (d.type === 'trade' && d.action === 'created') {
        this.trades = [d, ...this.trades];
      }
      this.$nextTick(() => this.renderCharts());
    },

    hitNumber(level) {
      if (!level) return 0;
      const m = String(level).toUpperCase().match(/TP\s*(\d+)/);
      return m ? parseInt(m[1], 10) : 0;
    },

    isTpHit(t, n) {
      return this.hitNumber(t.hit_level) >= n;
    },

    isBeHit(t) {
      return String(t.hit_level || '').toUpperCase() === 'BE';
    },

    isSlHit(t) {
      return String(t.hit_level || '').toUpperCase() === 'SL';
    },

    tpBadgeClass(t, n) {
      return this.isTpHit(t, n)
        ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500'
        : 'text-gray-300 border-transparent';
    },

    fmtPips(v) {
      const n = Number(v) || 0;
      return (n > 0 ? '+' : '') + n.toFixed(1);
    },

    filterByPeriod(period) {
      this.currentPeriod = period;
      this.currentPage = 1;
      this.dateFilter = '';
      if (period === 'ALL') this.periodLabel = 'All Time';
      else if (period === 'THIS_WEEK') this.periodLabel = 'This Week';
      else if (period === 'LAST_WEEK') this.periodLabel = 'Last Week';
      else if (period === 'LAST_MONTH') this.periodLabel = 'Last Month';
    },

    filterByDate() {
      if (this.dateFilter) {
        this.currentPeriod = 'CUSTOM_DATE';
        this.periodLabel = 'Selected Date: ' + this.dateFilter;
      } else {
        this.currentPeriod = 'ALL';
        this.periodLabel = 'All Time';
      }
      this.currentPage = 1;
    },

    clearDateFilter() {
      this.dateFilter = '';
      this.filterByPeriod('ALL');
    },

    filterResult(type) {
      this.resultFilter = type;
      this.currentPage = 1;
    },

    applyInitialFilter() {},

    recalcPagination() {},

    showToast(msg, type = 'success') {
      this.toastMsg = msg;
      this.toastType = type;
      this.toastVisible = true;
      setTimeout(() => { this.toastVisible = false; }, 2500);
    },

    renderCharts() {
      try {
      const eqCanvas = document.getElementById('equityChart');
      const distCanvas = document.getElementById('distributionChart');
      if (!eqCanvas || !distCanvas) return;
      const eqCtx = eqCanvas.getContext('2d');
      const distCtx = distCanvas.getContext('2d');
      if (!eqCtx || !distCtx) return;

      // Destroy any existing charts on these canvases
      const existingEq = Chart.getChart(eqCanvas);
      if (existingEq) existingEq.destroy();
      const existingDist = Chart.getChart(distCanvas);
      if (existingDist) existingDist.destroy();

      const data = this.filteredData;
      const m = this.computedMetrics;
      const balance = this.startBalance + this.depositBalance - this.withdrawBalance;
      let bal = balance;
      const labels = ['Start'];
      const points = [bal];
      [...data].sort((a,b) => a.no - b.no).forEach(t => {
        bal += (t.profit || 0);
        labels.push('T' + t.no);
        points.push(bal);
      });

      const winPips = data.filter(t => t.pips > 0).reduce((s,t) => s + t.pips, 0);
      const lossPipsVal = data.filter(t => t.pips < 0).reduce((s,t) => s + t.pips, 0);

      const centerTextPlugin = {
        id: 'centerText',
        beforeDraw: function(chart) {
          var width = chart.width, height = chart.height, ctx = chart.ctx;
          ctx.save();
          var fontSize = (height / 180).toFixed(2);
          ctx.font = "bold " + fontSize + "em sans-serif";
          ctx.textBaseline = "middle";
          ctx.fillStyle = "#ffffff";
          var text = m.winRate + '%',
              textX = Math.round((width - ctx.measureText(text).width) / 2),
              textY = (height / 2) + 10;
          ctx.fillText(text, textX, textY);
          ctx.font = "bold 11px sans-serif";
          ctx.fillStyle = "#d1d5db";
          var label = "WIN RATE",
              labelX = Math.round((width - ctx.measureText(label).width) / 2),
              labelY = textY - 22;
          ctx.fillText(label, labelX, labelY);
          ctx.save();
        }
      };

      this.chartInstances.distribution = new Chart(distCanvas, {
        type: 'doughnut',
        data: {
          labels: ['Wins', 'Losses', 'BE'],
          datasets: [{ data: [m.wins, m.losses, m.bes], backgroundColor: ['#10b981', '#ef4444', '#f59e0b'], borderWidth: 0 }]
        },
        plugins: [centerTextPlugin],
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { labels: { color: '#ffffff' } } }
        }
      });

      this.chartInstances.equity = new Chart(eqCanvas, {
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
      } catch(e) { /* canvas not ready */ }
    }
  };
}
</script>

</x-layouts.admin>
