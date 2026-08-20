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
    text-decoration: none;
    display: inline-block;
  }
  .nav-pills-custom .nav-link.active {
    background: var(--gold);
    color: #0a0e0c;
    border-color: var(--gold);
    box-shadow: 0 0 15px var(--gold-glow);
  }
  .nav-pills-custom .nav-link:hover:not(.active) {
    border-color: var(--border-hover);
    color: var(--text-primary);
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
<body class="p-3 p-md-4">

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

    <!-- Right side: Nav links + User menu -->
    <div class="flex items-center gap-4 flex-wrap">
      <ul class="nav nav-pills nav-pills-custom flex flex-wrap gap-2 m-0 p-0 border-0" role="tablist">
        <li class="nav-item m-0">
          <a href="{{ route('dashboard') }}" class="nav-link {{ request()->routeIs('dashboard') ? 'active' : '' }}">📊 Main Dashboard</a>
        </li>
        <li class="nav-item m-0">
          <a href="{{ route('admin.panel') }}" class="nav-link {{ request()->routeIs('admin.panel') ? 'active' : '' }}">⚙️ Admin Panel</a>
        </li>
        <li class="nav-item m-0">
          <a href="{{ route('admin.ib-partners') }}" class="nav-link {{ request()->routeIs('admin.ib-partners') ? 'active' : '' }}">🛡️ IB Partner System</a>
        </li>
        <li class="nav-item m-0">
          <a href="{{ route('admin.csv-analytics') }}" class="nav-link {{ request()->routeIs('admin.csv-analytics') ? 'active' : '' }}">📈 Trading CSV Analytics</a>
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

  <!-- Page Content -->
  {{ $slot }}

</div>

<!-- Toast Notification -->
<div id="toast" class="toast-custom"></div>

</body>
</html>
