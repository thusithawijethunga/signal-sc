<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>{{ config('app.name', 'Signal Xpress') }}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        :root {
            --bg-deep: #0a0e0c;
            --bg-card: #121815;
            --border: #2a3a32;
            --border-hover: #3e5449;
            --text-primary: #ffffff;
            --text-secondary: #d1d5db;
            --text-muted: #9ca3af;
            --green: #00B050;
            --green-light: #92D050;
            --red: #ff3b3b;
            --gold: #d4a04c;
            --gold-glow: rgba(212, 160, 76, 0.2);
        }
        body {
            background-color: var(--bg-deep);
            color: var(--text-primary);
            font-family: 'Space Grotesk', sans-serif;
            min-height: 100vh;
            background-image: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(212,160,76,0.1), transparent 50%);
            background-attachment: fixed;
        }
        .sx-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: 14px; }
        .sx-input { background: #000; border: 1px solid var(--border); color: #fff; padding: 8px 12px; border-radius: 7px; font-family: inherit; font-size: 13px; width: 100%; }
        .sx-input:focus { outline: none; border-color: var(--green); }
        .sx-btn { padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; transition: all 0.2s; border: 1px solid var(--border); }
        .sx-btn-gold { background: var(--gold); color: #0a0e0c; border-color: var(--gold); }
        .sx-btn-gold:hover { box-shadow: 0 0 15px var(--gold-glow); }
        .sx-btn-green { background: var(--green); color: #0a0e0c; border-color: var(--green); }
        .sx-btn-red { background: #991b1b; color: #fca5a5; border-color: var(--red); }
        .sx-nav-link { padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; color: var(--text-secondary); border: 1px solid transparent; transition: all 0.2s; text-decoration: none; display: inline-block; }
        .sx-nav-link:hover, .sx-nav-link.active { background: var(--gold); color: #0a0e0c; border-color: var(--gold); box-shadow: 0 0 10px var(--gold-glow); }
        .sx-table { width: 100%; border-collapse: collapse; }
        .sx-table th { background: #1f242c; color: #9ca3af; text-transform: uppercase; font-size: 11px; letter-spacing: 0.05em; padding: 12px; text-align: left; border-bottom: 2px solid var(--border); }
        .sx-table td { padding: 10px 12px; border-bottom: 1px solid var(--border); color: var(--text-secondary); font-size: 13px; }
        .sx-table tbody tr:hover td { background: rgba(0,176,80,0.05); }
        .badge-win { background: rgba(0,176,80,0.15); color: #92D050; border: 1px solid rgba(0,176,80,0.3); padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 11px; }
        .badge-loss { background: rgba(255,59,59,0.15); color: #ff6b6b; border: 1px solid rgba(255,59,59,0.3); padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 11px; }
        .badge-be { background: rgba(212,160,76,0.15); color: #d4a04c; border: 1px solid rgba(212,160,76,0.3); padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 11px; }
        .badge-running { background: rgba(56,189,248,0.15); color: #38bdf8; border: 1px solid rgba(56,189,248,0.3); padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 11px; }
    </style>
</head>
<body class="antialiased">
    <div class="min-h-screen">
        @include('layouts.navigation')
        @isset($header)
            <header class="border-b" style="border-color: var(--border); background: var(--bg-card);">
                <div class="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
                    {{ $header }}
                </div>
            </header>
        @endisset
        <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
            {{ $slot }}
        </main>
    </div>
</body>
</html>
