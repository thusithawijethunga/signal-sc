<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>SIGNAL XPRESS — Unified Master Trading & IB Partner Admin Portal</title>

<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/icons-webfont@3.19.0/dist/tabler-icons.min.css">
<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>

<style>
  :root {
    --bg-deep: #0a0e0c;
    --bg-card-solid: #121815;
    --border: #2a3a32;
    --border-hover: #3e5449;
    --text-primary: #ffffff;
    --text-secondary: #d1d5db;
    --text-muted: #9ca3af;
    --green: #00B050;
    --green-light: #92D050;
    --green-glow: rgba(0, 176, 80, 0.2);
    --red: #ff3b3b;
    --red-glow: rgba(255, 59, 59, 0.2);
    --gold: #d4a04c;
    --gold-glow: rgba(212, 160, 76, 0.2);
  }

  * { box-sizing: border-box; }

  body {
    background-color: var(--bg-deep);
    color: var(--text-primary);
    font-family: 'Space Grotesk', sans-serif;
    min-height: 100vh;
    background-image: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(212,160,76,0.1), transparent 50%);
    background-attachment: fixed;
    padding-bottom: 2rem;
  }

  .nav-pills-custom .nav-link {
    padding: 10px 20px;
    font-weight: 700;
    font-size: 13px;
    border-radius: 8px;
    transition: all 0.2s ease;
    cursor: pointer;
    border: 1px solid var(--border);
    background: var(--bg-card-solid);
    color: var(--text-secondary);
  }
  .nav-pills-custom .nav-link.active {
    background: var(--gold);
    color: #0a0e0c;
    border-color: var(--gold);
    box-shadow: 0 0 15px var(--gold-glow);
  }

  .period-btn {
    padding: 6px 14px;
    font-weight: 600;
    font-size: 12px;
    border-radius: 7px;
    transition: all 0.2s ease;
    cursor: pointer;
    border: 1px solid var(--border);
    background: var(--bg-deep);
    color: var(--text-secondary);
  }
  .period-btn.active {
    background: var(--gold);
    color: #0a0e0c;
    border-color: var(--gold);
    box-shadow: 0 0 10px var(--gold-glow);
  }

  .star-container {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    pointer-events: none; z-index: 9999; overflow: hidden;
  }

  .dynamic-banner-text { animation: changeColor 6s linear infinite; }
  @keyframes changeColor {
    0% { color: #fbbf24; } 20% { color: #f87171; }
    40% { color: #34d399; } 60% { color: #22d3ee; }
    80% { color: #c084fc; } 100% { color: #fbbf24; }
  }

  @keyframes marquee {
    0% { transform: translateX(0%); }
    100% { transform: translateX(-50%); }
  }

  .card-admin { background: var(--bg-card-solid); border: 1px solid var(--border); border-radius: 14px; padding: 24px; }
  .form-group { display: flex; flex-direction: column; gap: 4px; }
  .form-group label { font-size: 11px; color: #d1d5db; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 600; }
  .form-group input, .form-group select {
    background: #000000; border: 1px solid var(--border); color: #ffffff;
    padding: 8px 12px; border-radius: 7px; font-family: inherit; font-size: 13px;
  }
  .form-group input:focus, .form-group select:focus { outline: none; border-color: var(--green); }

  .toast-custom {
    position: fixed; bottom: 24px; right: 24px; background: var(--bg-card-solid);
    border: 1px solid var(--green); color: var(--green-light); padding: 14px 20px;
    border-radius: 10px; font-size: 13px; font-weight: 500;
    transform: translateY(100px); opacity: 0; transition: all 0.3s; z-index: 10000;
  }
  .toast-custom.show { transform: translateY(0); opacity: 1; }
  .toast-custom.error { border-color: var(--red); color: var(--red); }

  .custom-modal-overlay {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0, 0, 0, 0.85);
    backdrop-filter: blur(4px);
    display: flex; justify-content: center; align-items: center;
    z-index: 10005;
  }
  .custom-modal-box {
    background: var(--bg-card-solid);
    border: 1px solid var(--gold);
    box-shadow: 0 0 25px var(--gold-glow);
    border-radius: 14px;
    width: 90%; max-width: 380px;
    padding: 24px;
  }

  .card-ib, .sec-ib {
    background-color: #162032;
    border: 1px solid #2a3a52;
    border-radius: 16px;
    box-shadow: 0 10px 25px -5px rgba(0,0,0,0.5);
  }

  .form-control-ib, .form-select-ib {
    background-color: #0b1329 !important;
    border: 1px solid #334155 !important;
    color: #fff !important;
    border-radius: 8px;
  }
  .form-control-ib:focus, .form-select-ib:focus {
    background-color: #0b1329 !important;
    color: #fff !important;
    border-color: #3b82f6 !important;
    box-shadow: 0 0 0 0.25rem rgba(59, 130, 246, 0.25);
  }

  .upload-box {
    background: #0f1320;
    border: 2px dashed #2a3a52;
    border-radius: 14px;
    padding: 2rem;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
  }
  .upload-box:hover { border-color: #3b82f6; background: #111825; }
  .upload-box.ready { border-color: #00e5a0; border-style: solid; background: #0a1f18; }
  .upload-box.ready .up-icon { color: #00e5a0; }
  .up-icon { font-size: 38px; color: #3b82f6; margin-bottom: 8px; display: block; }
  .up-title { font-size: 18px; font-weight: 600; color: #f8fafc; margin-bottom: 4px; }
  .up-sub { font-size: 13px; color: #9ca3af; }
  .up-fname { font-size: 14px; color: #00e5a0; margin-top: 8px; font-family: monospace; font-weight: bold; }
  .up-input { position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%; }

  .btn-run {
    background: #0a1f18;
    color: #00e5a0;
    border: 1px solid #00e5a0;
    border-radius: 8px;
    padding: 12px 24px;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    width: 100%;
    margin-top: 1.25rem;
    margin-bottom: 1.25rem;
    transition: all 0.2s;
  }
  .btn-run:hover { background: #0f2d20; box-shadow: 0 0 15px rgba(0, 229, 160, 0.3); }
  .btn-run:disabled { opacity: 0.35; cursor: default; box-shadow: none; }

  .metrics-ib { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-bottom: 1.25rem; }
  .mc { background: #151a28; border: 1px solid #1e2130; border-radius: 12px; padding: 0.9rem 1.1rem; position: relative; overflow: hidden; }
  .mc-accent { position: absolute; top: 0; left: 0; right: 0; height: 2px; }
  .mc-label { font-size: 11px; color: #5a6480; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.05em; }
  .mc-val { font-size: 22px; font-weight: 600; }
  .mc-sub { font-size: 11px; color: #5a6480; margin-top: 2px; }

  .table-container { background-color: #162032 !important; border-radius: 8px; }
  .table-dark-custom { color: #f8fafc !important; background-color: #162032 !important; margin-bottom: 0; }
  .table-dark-custom th {
    background-color: #0b1329 !important;
    color: #94a3b8 !important;
    border-bottom: 2px solid #2a3a52 !important;
    font-weight: 600; position: sticky; top: 0; z-index: 10;
  }
  .table-dark-custom td { background-color: #162032 !important; color: #f8fafc !important; border-bottom: 1px solid #2a3a52 !important; vertical-align: middle; }
  .table-dark-custom tbody tr:hover td { background-color: #1e2d42 !important; }

  .top10-list { display: flex; flex-direction: column; gap: 8px; }
  .top10-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; background: #13192a; border-radius: 10px; border: 1px solid #1e2130; }

  .sub-tabs { display: flex; gap: 6px; margin-bottom: 1.1rem; flex-wrap: wrap; }
  .sub-tab { padding: 7px 16px; font-size: 14px; border: 1px solid #1e2130; border-radius: 8px; cursor: pointer; background: #151a28; color: #5a6480; transition: all 0.15s; }
  .sub-tab.on { background: #0a1f18; color: #00e5a0; border-color: #00e5a040; }

  .search-card { background: #1a1f2e; border: 1px solid #2a3a52; border-radius: 10px; }
  .lbl { color: #6b7280; font-size: 12px; }
  .val-blue { color: #3b82f6; }
  .val-white { color: #ffffff; }
  .val-green { color: #10b981; }

  @media(max-width: 768px){
    .metrics-ib { grid-template-columns: repeat(2, 1fr); }
  }
</style>
</head>
<body class="p-3 p-md-4" x-data="dashboardApp()">

<div class="star-container" id="starContainer"></div>

<div class="max-w-7xl mx-auto relative z-10">

  <!-- Navigation Header Bar -->
  <div class="flex flex-col md:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-800 gap-4">
    <div class="flex items-center gap-3">
      <div class="w-12 h-12 bg-gradient-to-br from-[#d4a04c] to-[#f5c46d] rounded-xl flex items-center justify-center font-bold text-black text-xl shadow-lg">SX</div>
      <div>
        <h1 class="text-xl font-bold text-white tracking-tight">SIGNAL XPRESS</h1>
        <p class="text-xs text-gray-300 font-mono">Master Trading Portal & Unified Admin System</p>
      </div>
    </div>

    <!-- Right side: Nav tabs + User menu -->
    <div class="flex items-center gap-4 flex-wrap">
      <ul class="nav nav-pills nav-pills-custom flex flex-wrap gap-2 m-0 p-0 border-0" role="tablist">
        <li class="nav-item m-0">
          <button class="nav-link" :class="{ 'active': activeTab === 'dashboard' }" @click="switchTab('dashboard')">📊 Main Dashboard</button>
        </li>
        <li class="nav-item m-0">
          <button class="nav-link" :class="{ 'active': activeTab === 'admin' }" @click="switchTab('admin')">⚙️ Admin Panel</button>
        </li>
        <li class="nav-item m-0">
          <button class="nav-link" :class="{ 'active': activeTab === 'ib-partner' }" @click="switchTab('ib-partner')">🛡️ IB Partner System</button>
        </li>
        <li class="nav-item m-0">
          <button class="nav-link" :class="{ 'active': activeTab === 'csv-analytics' }" @click="switchTab('csv-analytics')">📈 Trading CSV Analytics</button>
        </li>
      </ul>

      <!-- User Menu -->
      <div class="relative" x-data="{ open: false }">
        <button @click="open = !open" class="flex items-center gap-2 bg-[#121815] border border-[#2a3a32] rounded-lg px-3 py-1.5 text-sm text-white hover:border-[#d4a04c] transition-all">
          <div class="w-7 h-7 bg-gradient-to-br from-[#d4a04c] to-[#f5c46d] rounded-full flex items-center justify-center text-black text-xs font-bold">{{ substr(Auth::user()->name ?? 'A', 0, 1) }}</div>
          <span class="text-gray-200 font-semibold">{{ Auth::user()->name ?? 'Admin' }}</span>
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </button>
        <div x-show="open" @click.away="open = false" x-transition
             class="absolute right-0 mt-2 w-48 bg-[#121815] border border-[#2a3a32] rounded-lg shadow-xl z-50 py-1">
          <div class="px-4 py-2 border-b border-[#2a3a32]">
            <div class="text-xs text-gray-400">Signed in as</div>
            <div class="text-sm font-bold text-white">{{ Auth::user()->name ?? 'Admin' }}</div>
          </div>
          <form method="POST" action="{{ route('logout') }}">
            @csrf
            <button type="submit" class="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-[#1f242c] transition-colors flex items-center gap-2">
              <i class="fa-solid fa-right-from-bracket"></i> Logout
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- SECTION 1: MAIN DASHBOARD -->
  <div x-show="activeTab === 'dashboard'" x-transition>
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
      <div class="card-admin p-4"><span class="text-xs text-purple-400 uppercase font-semibold">Net Pips Gain</span><div class="text-xl font-bold text-purple-400 mt-1" x-text="computedMetrics.netPips">{{ $netPips }}</div></div>
      <div class="card-admin p-4 border-l-2 border-l-red-500"><span class="text-xs text-red-400 uppercase font-semibold">Total Loss Pips</span><div class="text-xl font-bold text-red-400 mt-1" x-text="computedMetrics.lossPips">{{ $lossPips }}</div></div>
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
              <th class="p-3">TP 1-4</th>
              <th class="p-3 text-right">Pips</th>
              <th class="p-3 text-right">Profit ($)</th>
              <th class="p-3 text-center">Result</th>
            </tr>
          </thead>
          <tbody class="font-mono">
            @forelse($trades as $trade)
              @php
                $entries = array_filter([$trade->entry1 ?? null, $trade->entry2 ?? null]);
                $entries = $entries ? implode(' / ', $entries) : '—';
                $slVal = ($trade->sl ?? null) !== null && ($trade->sl ?? '') !== '' ? $trade->sl : '—';
                $tps = array_filter([$trade->tp1 ?? null, $trade->tp2 ?? null, $trade->tp3 ?? null, $trade->tp4 ?? null]);
                $tps = $tps ? implode(', ', $tps) : '—';
                $resClass = strtoupper($trade->result ?? '') === 'WIN' ? 'bg-green-900/60 text-green-300 border border-green-700' :
                            (strtoupper($trade->result ?? '') === 'LOSS' ? 'bg-red-900/60 text-red-300 border border-red-700' :
                            'bg-amber-900/60 text-amber-300 border border-amber-700');
                $dirClass = str_contains(strtoupper($trade->direction ?? ''), 'BUY') ? 'text-blue-400' : 'text-amber-400';
              @endphp
              <tr class="border-b border-gray-800 hover:bg-gray-800/40">
                <td class="p-3 text-white font-bold">{{ $trade->no ?? $trade->id }}</td>
                <td class="p-3 text-xs text-gray-200">{{ $trade->date }}</td>
                <td class="p-3 text-white font-bold">{{ $trade->pair }}</td>
                <td class="p-3 font-bold {{ $dirClass }}">{{ $trade->direction }}</td>
                <td class="p-3 text-xs text-gray-200">{{ $entries }}</td>
                <td class="p-3 text-xs text-red-400 font-bold">{{ $slVal }}</td>
                <td class="p-3 text-xs text-gray-200">{{ $tps }}</td>
                <td class="p-3 text-right text-gray-100 font-semibold">{{ $trade->pips }}</td>
                <td class="p-3 text-right text-white font-bold">${{ $trade->profit }}</td>
                <td class="p-3 text-center"><span class="px-2 py-0.5 rounded text-xs font-bold {{ $resClass }}">{{ strtoupper($trade->result) }}</span></td>
              </tr>
            @empty
              <tr><td colspan="10" class="p-4 text-center text-gray-400">No records found for selected period/filter.</td></tr>
            @endforelse
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

  <!-- SECTION 2: ADMIN PANEL -->
  <div x-show="activeTab === 'admin'" x-transition>
    <div class="flex justify-between items-center mb-6">
      <div class="text-lg font-bold" style="color: var(--gold)">⚙️ Trade Ledger Administration</div>
      <div class="flex gap-2">
        <input type="file" id="file-input" accept=".json" style="display:none">
        <button class="px-4 py-2 bg-gray-800 hover:bg-gray-700 text-white rounded text-xs font-bold border border-gray-700" onclick="document.getElementById('file-input').click()">📂 Load JSON</button>
        <button class="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-black rounded text-xs font-bold" @click="downloadJSON()">⬇ Download JSON</button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

      <div class="card-admin lg:col-span-3 border border-amber-500/40 bg-amber-950/20">
        <div class="flex justify-between items-center mb-3">
          <h3 class="text-sm font-bold uppercase tracking-wider text-amber-400">💰 Manual Balance Configuration (Start, Deposit & Withdraw)</h3>
          <button type="button" @click="showBalance = !showBalance" class="w-6 h-6 bg-gray-800 hover:bg-gray-700 text-gray-200 rounded font-bold text-xs flex items-center justify-center" x-text="showBalance ? '-' : '+'">-</button>
        </div>
        <div x-show="showBalance" x-transition class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-2">
          <div class="form-group"><label>Start Balance ($)</label><input type="number" step="0.01" :value="startBalance" @input="startBalance = parseFloat($event.target.value) || 0"></div>
          <div class="form-group"><label>Deposit Balance ($)</label><input type="number" step="0.01" :value="depositBalance" @input="depositBalance = parseFloat($event.target.value) || 0"></div>
          <div class="form-group"><label>Withdraw Balance ($)</label><input type="number" step="0.01" :value="withdrawBalance" @input="withdrawBalance = parseFloat($event.target.value) || 0"></div>
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
            <button type="button" @click="manualFetchFromGoogleSheets()" class="px-3 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-xs font-bold transition-all shadow">🔄 Sync Now</button>
            <button type="button" @click="showGsSettings = !showGsSettings" class="w-6 h-6 bg-gray-800 hover:bg-gray-700 text-gray-200 rounded font-bold text-xs flex items-center justify-center" x-text="showGsSettings ? '-' : '+'">-</button>
          </div>
        </div>
        <div x-show="showGsSettings" x-transition class="form-group mt-2">
          <label>Google Apps Script Web App URL</label>
          <input type="text" x-model="gsUrl" placeholder="https://script.google.com/macros/s/...">
        </div>
      </div>

      <!-- Edit / Add Form -->
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

      <!-- Manage Saved Trades Table -->
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
    </div>
  </div>

  <!-- SECTION 3: IB PARTNER SYSTEM -->
  <div x-show="activeTab === 'ib-partner'" x-transition>
    <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom border-secondary">
      <div class="d-flex align-items-center gap-3">
        <div class="p-2 rounded-3 bg-dark border border-primary">
          <i class="fa-solid fa-user-shield text-primary fs-3"></i>
        </div>
        <div>
          <h3 class="m-0 text-white fw-bold fs-4">IB Partner & Trading Analytics System</h3>
          <small class="text-secondary">Google Sheet Dynamic Sync & Member Registration Database</small>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2">
        <input type="text" x-model="ibGsUrl" class="form-control form-control-ib form-control-sm text-xs" style="width: 280px;" placeholder="Google Web App URL for IB Partner">
        <button class="btn btn-sm btn-outline-primary text-white" @click="loadIbData()">
          <i class="fa-solid fa-rotate me-1"></i> Refresh Sheet
        </button>
        <span class="badge bg-warning text-dark" x-text="ibSyncStatus">Connecting...</span>
      </div>
    </div>

    <div class="row g-4">
      <!-- Search Card -->
      <div class="col-12">
        <div class="card-ib p-4">
          <h5 class="text-white mb-3">
            <i class="fa-solid fa-magnifying-glass text-primary me-2"></i>සෙවීම (Search by SX ID / Account ID / NIC)
          </h5>
          <div class="input-group">
            <input type="text" x-model="ibSearchQuery" @input="searchIbUser()" class="form-control form-control-ib form-control-lg" placeholder="SX ID (උදා: 1012 හෝ SX1012), Account ID හෝ NIC එක ඇතුළත් කරන්න...">
            <button class="btn btn-primary px-4" @click="searchIbUser()">
              <i class="fa-solid fa-search me-1"></i> Search
            </button>
          </div>
          <div class="mt-3" x-html="ibSearchResultHtml"></div>
        </div>
      </div>

      <!-- Form: Add Member -->
      <div class="col-lg-5">
        <div class="card-ib p-4 h-100">
          <h5 class="text-white mb-3">
            <i class="fa-solid fa-user-plus text-success me-2"></i>අලුත් සාමාජිකයෙක් ඇතුළත් කිරීම
          </h5>
          <form @submit.prevent="saveIbMember()">
            <div class="mb-2">
              <label>නම (Name)</label>
              <input type="text" x-model="ibForm.name" class="form-control form-control-ib" placeholder="සාමාජිකයාගේ නම" required>
            </div>

            <div class="row mb-2">
              <div class="col-6">
                <label>Broker Name</label>
                <input type="text" x-model="ibForm.broker" class="form-control form-control-ib" value="XM">
              </div>
              <div class="col-6">
                <label>Account ID</label>
                <input type="text" x-model="ibForm.accountId" class="form-control form-control-ib" placeholder="Account ID" required>
              </div>
            </div>

            <div class="mb-2">
              <label>NIC / Passport Number</label>
              <input type="text" x-model="ibForm.nic" class="form-control form-control-ib" placeholder="ID නම්‍බර් එක" required>
            </div>

            <div class="row mb-2">
              <div class="col-6">
                <label>WhatsApp Number</label>
                <input type="text" x-model="ibForm.whatsapp" class="form-control form-control-ib" placeholder="07XXXXXXXX">
              </div>
              <div class="col-6">
                <label>Telegram Username</label>
                <input type="text" x-model="ibForm.telegram" class="form-control form-control-ib" placeholder="@username">
              </div>
            </div>

            <div class="mb-3">
              <label>Partner Name</label>
              <div class="input-group">
                <select x-model="ibForm.partner" class="form-select form-select-ib">
                  @foreach($ibPartners as $partner)
                    <option value="{{ $partner->name }}">{{ $partner->name }}</option>
                  @endforeach
                </select>
                <button type="button" class="btn btn-outline-secondary text-white" @click="addIbPartner()">+ Partner</button>
              </div>
            </div>

            <button type="submit" class="btn btn-success w-100 py-2 mt-2" :disabled="ibSaving">
              <i class="fa-solid fa-floppy-disk me-1"></i> Save Member
            </button>
          </form>
          <div class="mt-3" x-html="ibStatusHtml"></div>
        </div>
      </div>

      <!-- Database Members Table -->
      <div class="col-lg-7">
        <div class="card-ib p-4 h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="text-white m-0">
              <i class="fa-solid fa-list-ul text-info me-2"></i>සාමාජිකයින් ලැයිස්තුව
            </h5>
            <span class="badge bg-secondary fs-6" x-text="'Total: ' + ibFilteredMembers.length">Total: 0</span>
          </div>
          <div class="table-responsive table-container" style="max-height: 480px; overflow-y: auto;">
            <table class="table table-dark-custom">
              <thead>
                <tr>
                  <th>SX ID</th>
                  <th>Name</th>
                  <th>Account ID</th>
                  <th>Partner</th>
                </tr>
              </thead>
              <tbody>
                @forelse($ibMembers as $member)
                  <tr>
                    <td><span class="badge bg-primary font-mono">{{ $member->sx_id ?? $member->id }}</span></td>
                    <td class="fw-bold">{{ $member->name }}</td>
                    <td class="font-mono text-info">{{ $member->account_id }}</td>
                    <td><span class="badge bg-secondary">{{ $member->partner->name ?? $member->partner_name ?? '—' }}</span></td>
                  </tr>
                @empty
                  <tr><td colspan="4" class="text-center text-muted py-4">Google Sheet එකෙන් දත්ත Load වෙමින් පවතී...</td></tr>
                @endforelse
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- SECTION 4: TRADING CSV ANALYTICS -->
  <div x-show="activeTab === 'csv-analytics'" x-transition>
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

    <!-- Single Upload Box -->
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

    <!-- CSV Results Section -->
    <div x-show="csvResultsReady" x-transition>

      <div class="metrics-ib">
        <div class="mc"><div class="mc-accent" style="background:#00e5a0"></div><div class="mc-label">Partners</div><div class="mc-val" style="color:#00e5a0" x-text="csvMetrics.partners">—</div><div class="mc-sub">Total Partners</div></div>
        <div class="mc"><div class="mc-accent" style="background:#6a8aff"></div><div class="mc-label">Active traders</div><div class="mc-val" style="color:#6a8aff" x-text="csvMetrics.activeTraders">—</div><div class="mc-sub">With trade records</div></div>
        <div class="mc"><div class="mc-accent" style="background:#f5c842"></div><div class="mc-label">Total trades</div><div class="mc-val" style="color:#f5c842" x-text="csvMetrics.totalTrades">—</div><div class="mc-sub">All positions</div></div>
        <div class="mc"><div class="mc-accent" style="background:#00cfff"></div><div class="mc-label">Total lots</div><div class="mc-val" style="color:#00cfff" x-text="csvMetrics.totalLots">—</div><div class="mc-sub">All trades combined</div></div>
        <div class="mc"><div class="mc-accent" style="background:#a06af0"></div><div class="mc-label">Total commission</div><div class="mc-val" style="color:#a06af0" x-text="csvMetrics.totalCommission">—</div><div class="mc-sub">Affiliate earned</div></div>
      </div>

      <!-- TOP 10 & CHARTS SECTION -->
      <div class="row g-3 mb-4">
        <!-- Top 10 List -->
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

        <!-- Chart 1: Partner Match Status (Doughnut Chart) -->
        <div class="col-lg-4">
          <div class="sec-ib h-100 p-3">
            <div class="text-secondary mb-3 text-center"><i class="ti ti-chart-donut me-1"></i> Partner Match Status</div>
            <div style="height:270px; position:relative;">
              <canvas id="csvChart1"></canvas>
            </div>
          </div>
        </div>

        <!-- Chart 2: Traded Currencies / Pairs (Horizontal Bar Chart) -->
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

      <!-- Partner Directory -->
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

      <!-- Trades by Account -->
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

      <!-- No Account Match -->
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
  </div>

</div>

<!-- Hit Pips Modal -->
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

<div class="toast-custom" id="toast" :class="{ 'show': toastVisible, 'error': toastType === 'error' }" x-text="toastMsg"></div>

<script>
function dashboardApp() {
  return {
    activeTab: 'dashboard',
    currentPeriod: 'ALL',
    dateFilter: '',
    periodLabel: 'All Time',

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
    gsUrl: '',

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

    currentPage: 1,
    rowsPerPage: '10',
    filteredTrades: [],

    ibGsUrl: '',
    ibSyncStatus: 'Connecting...',
    ibMembersData: @json($ibMembersJson),
    ibFilteredMembers: @json($ibMembersJson),
    ibSearchQuery: '',
    ibSearchResultHtml: '',
    ibStatusHtml: '',
    ibSaving: false,
    ibForm: {
      name: '',
      broker: 'XM',
      accountId: '',
      nic: '',
      whatsapp: '',
      telegram: '',
      partner: '{{ $ibPartners->first()->name ?? '' }}'
    },

    csvFileReady: false,
    csvFileName: '',
    csvFile: null,
    csvResultsReady: false,
    csvActiveTab: 'partners',
    csvSearchQuery: '',
    csvMetrics: { partners: 0, activeTraders: 0, totalTrades: 0, totalLots: '0.00', totalCommission: '$0.00' },
    csvTop10: [],
    csvFilteredPartners: [],
    csvMatchedTrades: [],
    csvUnmatchedTrades: [],
    csvMembersData: @json($ibMembersJson),
    csvUniqueAccounts: [],

    modal: { show: false, title: '', pips: 0, profit: 0 },
    modalResolve: null,

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

    get paginationInfo() {
      const data = [...this.filteredData].reverse();
      if (data.length === 0) return 'Showing 0-0 of 0 trades';
      const rpp = this.rowsPerPage === 'ALL' ? data.length : parseInt(this.rowsPerPage);
      const totalPages = Math.ceil(data.length / rpp);
      if (this.currentPage > totalPages) this.currentPage = totalPages;
      const start = (this.currentPage - 1) * rpp + 1;
      const end = Math.min(this.currentPage * rpp, data.length);
      return `Showing ${start}-${end} of ${data.length} trades`;
    },

    get paginationPages() {
      const data = [...this.filteredData].reverse();
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
      this.applyInitialFilter();
      this.$watch('startBalance', () => this.renderCharts());
      this.$watch('depositBalance', () => this.renderCharts());
      this.$watch('withdrawBalance', () => this.renderCharts());
      this.$nextTick(() => this.renderCharts());
      this.loadSettings();
    },

    loadSettings() {
      this.tgToken = localStorage.getItem('sx_tg_token') || '';
      this.tgChatId = localStorage.getItem('sx_tg_chat') || '';
      this.gsUrl = localStorage.getItem('sx_gs_url') || '';
      this.ibGsUrl = localStorage.getItem('sx_ib_gs_url') || '';
    },

    switchTab(tab) {
      this.activeTab = tab;
      if (tab === 'ib-partner') this.loadIbData();
      if (tab === 'dashboard') this.$nextTick(() => this.renderCharts());
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
      this.currentPage = 1;
    },

    applyInitialFilter() {},

    recalcPagination() {},

    // Toast
    showToast(msg, type = 'success') {
      this.toastMsg = msg;
      this.toastType = type;
      this.toastVisible = true;
      setTimeout(() => { this.toastVisible = false; }, 2500);
    },

    // Charts
    renderCharts() {
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

      if (this.chartInstances.equity) this.chartInstances.equity.destroy();
      if (this.chartInstances.distribution) this.chartInstances.distribution.destroy();

      const eqCanvas = document.getElementById('equityChart');
      const distCanvas = document.getElementById('distributionChart');
      if (!eqCanvas || !distCanvas) return;

      const winPips = data.filter(t => t.pips > 0).reduce((s,t) => s + t.pips, 0);
      const lossPipsVal = data.filter(t => t.pips < 0).reduce((s,t) => s + t.pips, 0);

      const centerTextPlugin = {
        id: 'centerText',
        beforeDraw: function(chart) {
          var width = chart.width, height = chart.height, ctx = chart.ctx;
          ctx.restore();
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
    },

    // Trade form
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

      try {
        const resp = await fetch('{{ route("admin.trades.store") }}', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': '{{ csrf_token() }}',
            'Accept': 'application/json'
          },
          body: JSON.stringify(rec)
        });
        if (resp.ok) {
          this.showToast(this.editingNum ? `✓ Updated Trade #${this.editingNum}` : `✓ Trade #${rec.no} Added`);
          setTimeout(() => window.location.reload(), 500);
        }
      } catch(e) {
        this.showToast('Trade saved locally', 'success');
      }

      if (this.editingNum) {
        const idx = this.trades.findIndex(t => t.no === this.editingNum);
        if (idx >= 0) this.trades[idx] = rec;
      } else {
        this.trades.push(rec);
      }
      this.resetForm();
      this.$nextTick(() => this.renderCharts());
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
    },

    deleteTrade(no) {
      if (!confirm(`Delete Trade #${no}?`)) return;
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

      this.showToast(`✓ Trade #${no} marked as ${actionType} Hit!`);
      this.$nextTick(() => this.renderCharts());
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

    // Telegram
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

    // Google Sheets
    async manualFetchFromGoogleSheets() {
      const url = this.gsUrl.trim();
      if (!url) { this.showToast('Please enter Google Apps Script URL first!', 'error'); return; }
      try {
        const resp = await fetch(url);
        const data = await resp.json();
        if (Array.isArray(data)) {
          this.showToast('✓ Successfully Synced with Google Sheets!');
        }
      } catch(err) { this.showToast('Failed to fetch from Google Sheets', 'error'); }
    },

    // JSON
    downloadJSON() {
      const blob = new Blob([JSON.stringify(this.trades, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'trades.json'; a.click();
      URL.revokeObjectURL(url);
    },

    // IB Partner System
    async loadIbData() {
      const url = this.ibGsUrl.trim();
      if (!url) {
        this.ibSyncStatus = 'No URL Provided';
        return;
      }
      this.ibSyncStatus = 'Connecting...';
      try {
        const resp = await fetch(url);
        const data = await resp.json();
        if (Array.isArray(data)) {
          this.ibMembersData = data;
          this.ibFilteredMembers = [...data];
          this.ibSyncStatus = '● Connected & Synced';
        } else {
          throw new Error("Invalid format");
        }
      } catch(err) {
        this.ibSyncStatus = 'Sync Failed';
      }
      localStorage.setItem('sx_ib_gs_url', this.ibGsUrl);
    },

    searchIbUser() {
      const query = this.ibSearchQuery.trim().toLowerCase();
      if (!query) { this.ibFilteredMembers = [...this.ibMembersData]; this.ibSearchResultHtml = ''; return; }

      const filtered = this.ibMembersData.filter(m => {
        return String(m.sxId || m.sx_id || '').toLowerCase().includes(query) ||
               String(m.accountId || m.account_id || m.account || '').toLowerCase().includes(query) ||
               String(m.nic || '').toLowerCase().includes(query) ||
               String(m.name || '').toLowerCase().includes(query);
      });

      this.ibFilteredMembers = filtered;

      if (filtered.length > 0) {
        const u = filtered[0];
        this.ibSearchResultHtml = `
          <div class="search-card p-3 mt-2">
            <div class="row text-sm">
              <div class="col-md-3"><span class="lbl">SX ID:</span> <span class="val-blue font-mono font-bold">${u.sxId || u.sx_id || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Name:</span> <span class="val-white font-bold">${u.name || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Account ID:</span> <span class="val-green font-mono">${u.accountId || u.account_id || u.account || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Partner:</span> <span class="val-white">${u.partner || '—'}</span></div>
            </div>
          </div>`;
      } else {
        this.ibSearchResultHtml = '<div class="alert alert-dark border-danger text-danger mt-2 py-2 text-sm">❌ කිසිදු සාමාජිකයෙකු හමු නොවීය.</div>';
      }
    },

    async saveIbMember() {
      this.ibSaving = true;
      this.ibStatusHtml = '<span class="text-warning text-sm">නව සාමාජිකයා ඇතුළත් වෙමින් පවතී...</span>';

      try {
        const resp = await fetch('{{ route("admin.ib.member.store") }}', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': '{{ csrf_token() }}',
            'Accept': 'application/json'
          },
          body: JSON.stringify({
            name: this.ibForm.name,
            broker: this.ibForm.broker,
            account_id: this.ibForm.accountId,
            nic: this.ibForm.nic,
            whatsapp: this.ibForm.whatsapp,
            telegram: this.ibForm.telegram,
            partner: this.ibForm.partner
          })
        });
        const data = await resp.json();
        if (resp.ok) {
          this.showToast('✓ Member Saved Successfully!');
          this.ibStatusHtml = '<span class="text-success text-sm">✓ සාමාජිකයා සාර්ථකව ඇතුළත් විය!</span>';
          this.ibForm = { name: '', broker: 'XM', accountId: '', nic: '', whatsapp: '', telegram: '', partner: this.ibForm.partner };
          setTimeout(() => { this.ibStatusHtml = ''; window.location.reload(); }, 1500);
        } else {
          throw new Error(data.message || 'Failed');
        }
      } catch(err) {
        this.showToast('Failed to save member', 'error');
        this.ibStatusHtml = '<span class="text-danger text-sm">❌ සාමාජිකයා ඇතුළත් කිරීමට අපොහොසත් විය.</span>';
      } finally {
        this.ibSaving = false;
      }
    },

    addIbPartner() {
      const pName = prompt("නව Partner ගේ නම ඇතුළත් කරන්න:");
      if (pName) {
        const opt = document.createElement('option');
        opt.value = pName; opt.textContent = pName; opt.selected = true;
        this.ibForm.partner = pName;
      }
    },

    // CSV Analytics
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
        const accId = idxAcc >= 0 ? (cols[idxAcc] || '').trim() : (cols[1] || '').trim();
        if (!accId || accId.toLowerCase() === 'unknown') continue;
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

      // Build partner directory
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

      // Matched trades
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

      // Top 10
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

      // Render charts
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
      if (!q) return;
      this.csvFilteredPartners = this.csvFilteredPartners.filter(row => {
        return (row.name || '').toLowerCase().includes(q) ||
               (row.sxId || '').toLowerCase().includes(q) ||
               (row.accountId || '').toLowerCase().includes(q) ||
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
          const accId = idxAcc >= 0 ? (cols[idxAcc] || '').trim() : (cols[1] || '').trim();
          if (!accId || accId.toLowerCase() === 'unknown') continue;
          const symbol = idxSym >= 0 ? (cols[idxSym] || 'Other') : (cols[10] || 'Other');
          const lots = idxLots >= 0 ? (parseFloat(cols[idxLots]) || 0) : (parseFloat(cols[12]) || 0);
          const comm = idxComm >= 0 ? (parseFloat(cols[idxComm]) || 0) : (parseFloat(cols[16]) || 0);
          if (lots <= 0) continue;
          totalLots += lots;
          totalCommission += comm;
          if (!accountsMap[accId]) accountsMap[accId] = { trades: 0, lots: 0, comm: 0 };
          accountsMap[accId].trades += 1;
          accountsMap[accId].lots += lots;
          accountsMap[accId].comm += comm;
          if (!currencyMap[symbol]) currencyMap[symbol] = 0;
          currencyMap[symbol] += lots;
        }

        const totalTrades = Object.values(accountsMap).reduce((s, a) => s + a.trades, 0);
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
</body>
</html>
