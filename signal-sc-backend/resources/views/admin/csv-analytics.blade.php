<x-layouts.admin>

<div x-data="csvAnalyticsApp()">
  <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom border-secondary">
    <div class="d-flex align-items-center gap-3">
      <div class="p-2 rounded-3 bg-dark border border-info">
        <i class="fa-solid fa-chart-line text-info fs-3"></i>
      </div>
      <div>
        <h3 class="m-0 text-white fw-bold fs-4">Trading Trades CSV Analytics</h3>
        <small class="text-secondary">Direct CSV Data Processing & Affiliate Partner Metrics</small>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-12">
      <div class="upload-box" :class="{ 'ready': csvFileReady }" @click="$refs.csvFileInput.click()">
        <input type="file" class="up-input" accept=".csv" x-ref="csvFileInput" @change="handleCsvFile($event)">
        <i class="ti ti-receipt up-icon" :class="{ 'text-[#00e5a0]': csvFileReady }"></i>
        <div class="up-title">Trading Trades CSV File එක මෙතැනට Upload කරන්න</div>
        <div class="up-sub">Trades & Commission data අඩංගු CSV එක upload කළ පසු Direct Analysis සිදු වේ</div>
        <div class="up-fname" x-text="csvFileName"></div>
      </div>
    </div>
  </div>

  <button class="btn-run" @click="processCsvData()" :disabled="!csvFileReady">
    <i class="ti ti-player-play me-2"></i> Trading CSV එක Analysis කර Analytics Dashboard එක ලබාගන්න
  </button>

  <div x-show="csvResultsReady" x-transition>

    <div class="metrics-ib">
      <div class="mc"><div class="mc-accent" style="background:#00e5a0"></div><div class="mc-label">Partners</div><div class="mc-val" style="color:#00e5a0" x-text="csvMetrics.partners">—</div><div class="mc-sub">Total Partners</div></div>
      <div class="mc"><div class="mc-accent" style="background:#6a8aff"></div><div class="mc-label">Active traders</div><div class="mc-val" style="color:#6a8aff" x-text="csvMetrics.activeTraders">—</div><div class="mc-sub">With trade records</div></div>
      <div class="mc"><div class="mc-accent" style="background:#f5c842"></div><div class="mc-label">Total trades</div><div class="mc-val" style="color:#f5c842" x-text="csvMetrics.totalTrades">—</div><div class="mc-sub">All positions</div></div>
      <div class="mc"><div class="mc-accent" style="background:#00cfff"></div><div class="mc-label">Total lots</div><div class="mc-val" style="color:#00cfff" x-text="csvMetrics.totalLots">—</div><div class="mc-sub">All trades combined</div></div>
      <div class="mc"><div class="mc-accent" style="background:#a06af0"></div><div class="mc-label">Total commission</div><div class="mc-val" style="color:#a06af0" x-text="csvMetrics.totalCommission">—</div><div class="mc-sub">Affiliate earned</div></div>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-lg-4">
        <div class="sec-ib h-100 p-3">
          <div class="text-secondary mb-3"><i class="ti ti-trophy me-1"></i> Top 10 — Lots අනුව</div>
          <div class="top10-list">
            <template x-for="(item, index) in csvTop10" :key="item.id">
              <div class="top10-item">
                <span class="badge bg-warning text-dark font-bold" x-text="'#' + (index + 1)"></span>
                <div class="flex-grow-1">
                  <div class="text-white text-xs font-bold" x-text="item.name || 'Account: ' + item.id"></div>
                  <div class="text-muted text-[10px] font-mono" x-text="item.id"></div>
                </div>
                <div class="text-end">
                  <div class="text-info text-xs font-bold" x-text="item.lots.toFixed(2) + ' Lots'"></div>
                  <div class="text-success text-[10px] font-bold" x-text="'$' + item.comm.toFixed(2)"></div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="sec-ib h-100 p-3">
          <div class="text-secondary mb-3 text-center"><i class="ti ti-chart-donut me-1"></i> Partner Match Status</div>
          <div style="height:270px; position:relative;">
            <canvas id="csvChart1"></canvas>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="sec-ib h-100 p-3">
          <div class="text-secondary mb-3 text-center"><i class="ti ti-chart-bar me-1"></i> Traded Currencies / Pairs (Lots)</div>
          <div style="height:270px; position:relative;">
            <canvas id="csvChart2"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="sub-tabs">
      <div class="sub-tab" :class="{ 'on': csvActiveTab === 'partners' }" @click="csvActiveTab = 'partners'"><i class="ti ti-id-badge me-1"></i> Partner directory</div>
      <div class="sub-tab" :class="{ 'on': csvActiveTab === 'trades' }" @click="csvActiveTab = 'trades'"><i class="ti ti-receipt me-1"></i> Trades by account</div>
      <div class="sub-tab" :class="{ 'on': csvActiveTab === 'nomatch' }" @click="csvActiveTab = 'nomatch'"><i class="ti ti-alert-triangle me-1"></i> No account match</div>
    </div>

    <div x-show="csvActiveTab === 'partners'">
      <div class="sec-ib p-3">
        <div class="text-secondary mb-2"><i class="ti ti-search me-1"></i> Partner search</div>
        <input class="form-control form-control-ib mb-3" x-model="csvSearchQuery" placeholder="Name, SX ID, Account ID, Broker..." @input="filterCsvPartners()">
        <div class="table-responsive">
          <table class="table table-dark-custom">
            <thead>
              <tr>
                <th style="width:40px">#</th>
                <th style="width:90px">SX ID</th>
                <th>Name</th>
                <th style="width:80px">Broker</th>
                <th style="width:120px">Account ID</th>
                <th style="width:100px">Partner</th>
                <th style="width:70px">Trades</th>
                <th style="width:90px">Lots</th>
                <th style="width:110px">Commission</th>
                <th style="width:90px">Status</th>
              </tr>
            </thead>
            <tbody>
              <template x-for="(row, idx) in csvFilteredPartners" :key="idx">
                <tr>
                  <td x-text="idx + 1"></td>
                  <td><span class="badge bg-primary font-mono" x-text="row.sxId || '—'"></span></td>
                  <td class="fw-bold" x-text="row.name || '—'"></td>
                  <td x-text="row.broker || 'XM'"></td>
                  <td class="font-mono text-info" x-text="row.accountId || '—'"></td>
                  <td x-text="row.partner || '—'"></td>
                  <td x-text="row.trades"></td>
                  <td x-text="row.lots.toFixed(2)"></td>
                  <td class="text-success font-bold" x-text="'$' + row.comm.toFixed(2)"></td>
                  <td>
                    <span class="badge" :class="row.matched ? 'bg-success' : 'bg-secondary'" x-text="row.matched ? 'Active' : 'No Trades'"></span>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div x-show="csvActiveTab === 'trades'">
      <div class="sec-ib p-3">
        <div class="text-secondary mb-2"><i class="ti ti-receipt me-1"></i> Account summary</div>
        <div class="table-responsive">
          <table class="table table-dark-custom">
            <thead>
              <tr>
                <th>Account ID</th>
                <th>Matched Partner</th>
                <th>SX ID</th>
                <th>Trades</th>
                <th>Total Lots</th>
                <th>Commission</th>
              </tr>
            </thead>
            <tbody>
              <template x-for="(row, idx) in csvMatchedTrades" :key="idx">
                <tr>
                  <td class="font-mono text-info" x-text="row.accountId"></td>
                  <td class="fw-bold" x-text="row.partnerName || '—'"></td>
                  <td><span class="badge bg-primary font-mono" x-text="row.sxId || '—'"></span></td>
                  <td x-text="row.trades"></td>
                  <td x-text="row.lots.toFixed(2)"></td>
                  <td class="text-success font-bold" x-text="'$' + row.comm.toFixed(2)"></td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div x-show="csvActiveTab === 'nomatch'">
      <div class="sec-ib p-3">
        <div class="text-secondary mb-2"><i class="ti ti-alert-triangle me-1"></i> Unmatched trades from CSV</div>
        <div class="table-responsive">
          <table class="table table-dark-custom">
            <thead>
              <tr>
                <th>Account ID</th>
                <th>Trades</th>
                <th>Total Lots</th>
                <th>Commission</th>
              </tr>
            </thead>
            <tbody>
              <template x-for="(row, idx) in csvUnmatchedTrades" :key="idx">
                <tr>
                  <td class="font-mono text-warning" x-text="row.accountId"></td>
                  <td x-text="row.trades"></td>
                  <td x-text="row.lots.toFixed(2)"></td>
                  <td class="text-success font-bold" x-text="'$' + row.comm.toFixed(2)"></td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>

  <div class="toast-custom" id="toast" :class="{ 'show': toastVisible, 'error': toastType === 'error' }" x-text="toastMsg"></div>
</div>

<script>
function csvAnalyticsApp() {
  return {
    csvFileReady: false,
    csvFileName: '',
    csvFile: null,
    csvResultsReady: false,
    csvActiveTab: 'partners',
    csvSearchQuery: '',
    csvMetrics: { partners: 0, activeTraders: 0, totalTrades: 0, totalLots: '0.00', totalCommission: '$0.00' },
    csvTop10: [],
    csvFilteredPartners: [],
    csvFilteredPartnersSource: [],
    csvMatchedTrades: [],
    csvUnmatchedTrades: [],
    csvMembersData: @json($ibMembersJson),
    csvUniqueAccounts: [],

    chartInstances: {},

    toastVisible: false,
    toastMsg: '',
    toastType: 'success',

    showToast(msg, type = 'success') {
      this.toastMsg = msg;
      this.toastType = type;
      this.toastVisible = true;
      setTimeout(() => { this.toastVisible = false; }, 2500);
    },

    handleCsvFile(event) {
      const file = event.target.files[0];
      if (file) {
        this.csvFileName = file.name;
        this.csvFileReady = true;
        this.csvFile = file;
      }
    },

    processCsvData() {
      if (!this.csvFile) return;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.parseCsvData(e.target.result);
        this.autoCreateMembersFromCsv();
      };
      reader.readAsText(this.csvFile);
    },

    parseCSVLine(line) {
      const result = [];
      let current = '';
      let inQuotes = false;
      for (let i = 0; i < line.length; i++) {
        const ch = line[i];
        if (inQuotes) {
          if (ch === '"') {
            if (i + 1 < line.length && line[i + 1] === '"') { current += '"'; i++; }
            else { inQuotes = false; }
          } else { current += ch; }
        } else {
          if (ch === '"') { inQuotes = true; }
          else if (ch === ',') { result.push(current.trim()); current = ''; }
          else { current += ch; }
        }
      }
      result.push(current.trim());
      return result;
    },

    parseCsvData(text) {
      const lines = text.split('\n').filter(l => l.trim() !== '');
      if (lines.length < 2) return;

      const headers = this.parseCSVLine(lines[0]);
      const hMap = {};
      headers.forEach((h, i) => { hMap[h.trim().toLowerCase()] = i; });

      const getIdx = (...names) => { for (const n of names) { if (hMap[n] !== undefined) return hMap[n]; } return -1; };
      const idxAcc = getIdx('mt4/mt5 id', 'account', 'account_id', 'login');
      const idxSym = getIdx('instrument', 'symbol', 'pair');
      const idxLots = getIdx('lots', 'volume');
      const idxComm = getIdx('affiliate comm.', 'commission', 'total comm.', 'comm');
      const idxBroker = getIdx('account brand', 'broker');

      let totalLots = 0, totalCommission = 0, totalTrades = lines.length - 1;
      let accountsMap = {}, currencyMap = {}, uniqueAccounts = {};

      for (let i = 1; i < lines.length; i++) {
        const cols = this.parseCSVLine(lines[i]);
        if (cols.length < 3) continue;
        const accId = idxAcc >= 0 ? (cols[idxAcc] || 'Unknown') : (cols[1] || 'Unknown');
        const symbol = idxSym >= 0 ? (cols[idxSym] || 'Other') : (cols[10] || 'Other');
        const lots = idxLots >= 0 ? (parseFloat(cols[idxLots]) || 0) : (parseFloat(cols[12]) || 0);
        const comm = idxComm >= 0 ? (parseFloat(cols[idxComm]) || 0) : (parseFloat(cols[16]) || 0);
        const broker = idxBroker >= 0 ? (cols[idxBroker] || 'XM') : 'XM';
        totalLots += lots;
        totalCommission += comm;
        if (!accountsMap[accId]) accountsMap[accId] = { trades: 0, lots: 0, comm: 0 };
        accountsMap[accId].trades += 1;
        accountsMap[accId].lots += lots;
        accountsMap[accId].comm += comm;
        if (!currencyMap[symbol]) currencyMap[symbol] = 0;
        currencyMap[symbol] += lots;
        if (!uniqueAccounts[accId]) uniqueAccounts[accId] = broker;
      }

      const uniqueTradersCount = Object.keys(accountsMap).length;
      this.csvMetrics = {
        partners: this.csvMembersData.length || 0,
        activeTraders: uniqueTradersCount,
        totalTrades: totalTrades,
        totalLots: totalLots.toFixed(2),
        totalCommission: '$' + totalCommission.toFixed(2)
      };

      let matchedCount = 0, unmatchedCount = 0;
      this.csvFilteredPartners = this.csvMembersData.map((m, idx) => {
        const acc = accountsMap[m.accountId || m.account_id || m.account];
        if (acc) { matchedCount++; }
        return {
          sxId: m.sx_id || m.sxId || '—',
          name: m.name || '—',
          broker: m.broker || 'XM',
          accountId: m.accountId || m.account_id || m.account || '—',
          partner: m.partner || '—',
          trades: acc ? acc.trades : 0,
          lots: acc ? acc.lots : 0,
          comm: acc ? acc.comm : 0,
          matched: !!acc
        };
      });
      this.csvFilteredPartnersSource = [...this.csvFilteredPartners];

      this.csvMatchedTrades = [];
      this.csvUnmatchedTrades = [];

      Object.keys(accountsMap).forEach(accId => {
        const m = this.csvMembersData.find(x => (x.accountId || x.account_id || x.account) == accId);
        const acc = accountsMap[accId];
        if (m) {
          this.csvMatchedTrades.push({
            accountId: accId,
            partnerName: m.name || '—',
            sxId: m.sx_id || m.sxId || '—',
            trades: acc.trades,
            lots: acc.lots,
            comm: acc.comm
          });
        } else {
          unmatchedCount++;
          this.csvUnmatchedTrades.push({
            accountId: accId,
            trades: acc.trades,
            lots: acc.lots,
            comm: acc.comm
          });
        }
      });

      this.csvTop10 = Object.keys(accountsMap)
        .map(id => ({ id, ...accountsMap[id] }))
        .sort((a, b) => b.lots - a.lots)
        .slice(0, 10)
        .map(item => {
          const member = this.csvMembersData.find(x => (x.accountId || x.account_id || x.account) == item.id);
          return { id: item.id, name: member ? member.name : null, lots: item.lots, comm: item.comm };
        });

      this.csvUniqueAccounts = Object.keys(uniqueAccounts).map(id => ({ account_id: id, broker: uniqueAccounts[id] }));
      this.csvResultsReady = true;

      this.$nextTick(() => this.renderCsvCharts(matchedCount, unmatchedCount, currencyMap));
    },

    renderCsvCharts(matched, unmatched, currencyMap) {
      if (this.chartInstances.csv1) this.chartInstances.csv1.destroy();
      if (this.chartInstances.csv2) this.chartInstances.csv2.destroy();

      const c1 = document.getElementById('csvChart1');
      const c2 = document.getElementById('csvChart2');
      if (!c1 || !c2) return;

      this.chartInstances.csv1 = new Chart(c1, {
        type: 'doughnut',
        data: {
          labels: ['Matched Accounts', 'Unmatched CSV Accounts'],
          datasets: [{ data: [matched, unmatched], backgroundColor: ['#00e5a0', '#ff3b3b'] }]
        },
        options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#ffffff' } } } }
      });

      this.chartInstances.csv2 = new Chart(c2, {
        type: 'bar',
        data: {
          labels: Object.keys(currencyMap),
          datasets: [{ label: 'Lots Traded', data: Object.values(currencyMap), backgroundColor: '#3b82f6' }]
        },
        options: {
          responsive: true, maintainAspectRatio: false, indexAxis: 'y',
          scales: { x: { ticks: { color: '#ffffff' } }, y: { ticks: { color: '#ffffff' } } },
          plugins: { legend: { labels: { color: '#ffffff' } } }
        }
      });
    },

    filterCsvPartners() {
      const q = this.csvSearchQuery.toLowerCase();
      if (!q) {
        this.csvFilteredPartners = [...this.csvFilteredPartnersSource];
        return;
      }
      this.csvFilteredPartners = this.csvFilteredPartnersSource.filter(row => {
        return (row.name || '').toLowerCase().includes(q) ||
               (row.sxId || '').toLowerCase().includes(q) ||
               (row.accountId || '').toLowerCase().includes(q) ||
               (row.broker || '').toLowerCase().includes(q) ||
               (row.partner || '').toLowerCase().includes(q);
      });
    },

    async autoCreateMembersFromCsv() {
      if (!this.csvUniqueAccounts.length) return;

      const existingIds = new Set(this.csvMembersData.map(m => String(m.account_id || m.accountId || '')));
      const newAccounts = this.csvUniqueAccounts.filter(a => !existingIds.has(String(a.account_id)));

      if (!newAccounts.length) {
        this.showToast('All CSV accounts already exist as members');
        return;
      }

      try {
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.content || '';
        const resp = await fetch('{{ route("admin.csv-analytics.auto-create") }}', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken, 'Accept': 'application/json' },
          body: JSON.stringify({ accounts: newAccounts })
        });
        const data = await resp.json();
        if (data.ok) {
          this.csvMembersData = data.ib_members;
          this.showToast(data.created_count + ' new member(s) created, ' + data.existing_count + ' already existed');
          this.rebuildAnalyticsFromCsv();
        }
      } catch (err) {
        console.error('Auto-create members error:', err);
        this.showToast('Failed to auto-create members', 'error');
      }
    },

    rebuildAnalyticsFromCsv() {
      let totalLots = 0, totalCommission = 0;
      let accountsMap = {}, currencyMap = {};

      const accountsMapTemp = {};
      for (let i = 0; i < this.csvUniqueAccounts.length; i++) {
        const acc = this.csvUniqueAccounts[i];
        accountsMapTemp[acc.account_id] = acc.broker;
      }

      if (!this.csvFile) return;
      const reader = new FileReader();
      reader.onload = (e) => {
        const text = e.target.result;
        const lines = text.split('\n').filter(l => l.trim() !== '');
        if (lines.length < 2) return;

        const headers = this.parseCSVLine(lines[0]);
        const hMap = {};
        headers.forEach((h, i) => { hMap[h.trim().toLowerCase()] = i; });
        const getIdx = (...names) => { for (const n of names) { if (hMap[n] !== undefined) return hMap[n]; } return -1; };
        const idxAcc = getIdx('mt4/mt5 id', 'account', 'account_id', 'login');
        const idxSym = getIdx('instrument', 'symbol', 'pair');
        const idxLots = getIdx('lots', 'volume');
        const idxComm = getIdx('affiliate comm.', 'commission', 'total comm.', 'comm');

        for (let i = 1; i < lines.length; i++) {
          const cols = this.parseCSVLine(lines[i]);
          if (cols.length < 3) continue;
          const accId = idxAcc >= 0 ? (cols[idxAcc] || 'Unknown') : (cols[1] || 'Unknown');
          const symbol = idxSym >= 0 ? (cols[idxSym] || 'Other') : (cols[10] || 'Other');
          const lots = idxLots >= 0 ? (parseFloat(cols[idxLots]) || 0) : (parseFloat(cols[12]) || 0);
          const comm = idxComm >= 0 ? (parseFloat(cols[idxComm]) || 0) : (parseFloat(cols[16]) || 0);
          totalLots += lots;
          totalCommission += comm;
          if (!accountsMap[accId]) accountsMap[accId] = { trades: 0, lots: 0, comm: 0 };
          accountsMap[accId].trades += 1;
          accountsMap[accId].lots += lots;
          accountsMap[accId].comm += comm;
          if (!currencyMap[symbol]) currencyMap[symbol] = 0;
          currencyMap[symbol] += lots;
        }

        const totalTrades = lines.length - 1;
        const uniqueTradersCount = Object.keys(accountsMap).length;
        this.csvMetrics = {
          partners: this.csvMembersData.length || 0,
          activeTraders: uniqueTradersCount,
          totalTrades: totalTrades,
          totalLots: totalLots.toFixed(2),
          totalCommission: '$' + totalCommission.toFixed(2)
        };

        let matchedCount = 0, unmatchedCount = 0;
        this.csvFilteredPartners = this.csvMembersData.map((m, idx) => {
          const acc = accountsMap[m.accountId || m.account_id || m.account];
          if (acc) { matchedCount++; }
          return {
            sxId: m.sx_id || m.sxId || '—',
            name: m.name || '—',
            broker: m.broker || 'XM',
            accountId: m.accountId || m.account_id || m.account || '—',
            partner: m.partner || '—',
            trades: acc ? acc.trades : 0,
            lots: acc ? acc.lots : 0,
            comm: acc ? acc.comm : 0,
            matched: !!acc
          };
        });
        this.csvFilteredPartnersSource = [...this.csvFilteredPartners];

        this.csvMatchedTrades = [];
        this.csvUnmatchedTrades = [];
        Object.keys(accountsMap).forEach(accId => {
          const m = this.csvMembersData.find(x => (x.accountId || x.account_id || x.account) == accId);
          const acc = accountsMap[accId];
          if (m) {
            this.csvMatchedTrades.push({ accountId: accId, partnerName: m.name || '—', sxId: m.sx_id || m.sxId || '—', trades: acc.trades, lots: acc.lots, comm: acc.comm });
          } else {
            unmatchedCount++;
            this.csvUnmatchedTrades.push({ accountId: accId, trades: acc.trades, lots: acc.lots, comm: acc.comm });
          }
        });

        this.csvTop10 = Object.keys(accountsMap)
          .map(id => ({ id, ...accountsMap[id] }))
          .sort((a, b) => b.lots - a.lots)
          .slice(0, 10)
          .map(item => {
            const member = this.csvMembersData.find(x => (x.accountId || x.account_id || x.account) == item.id);
            return { id: item.id, name: member ? member.name : null, lots: item.lots, comm: item.comm };
          });

        this.$nextTick(() => this.renderCsvCharts(matchedCount, unmatchedCount, currencyMap));
      };
      reader.readAsText(this.csvFile);
    }
  };
}
</script>

</x-layouts.admin>
