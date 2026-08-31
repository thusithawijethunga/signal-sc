<!DOCTYPE html>
<html lang="{{ str_replace('_', '-', app()->getLocale()) }}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">

    <title>{{ config('app.name', 'SIGNAL XPRESS') }} — Login</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>

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
            --gold: #d4a04c;
            --gold-glow: rgba(212, 160, 76, 0.2);
            --red: #ff3b3b;
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            background-color: var(--bg-deep);
            color: var(--text-primary);
            font-family: 'Space Grotesk', sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background-image: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(212,160,76,0.1), transparent 50%);
            background-attachment: fixed;
            padding: 1.5rem;
        }

        .auth-container {
            width: 100%;
            max-width: 420px;
        }

        .auth-logo {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 2rem;
        }

        .auth-logo-icon {
            width: 52px;
            height: 52px;
            background: linear-gradient(135deg, #d4a04c, #f5c46d);
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            color: #0a0e0c;
            font-size: 20px;
            box-shadow: 0 4px 20px var(--gold-glow);
        }

        .auth-logo-text h1 {
            font-size: 22px;
            font-weight: 700;
            color: #ffffff;
            letter-spacing: -0.02em;
        }

        .auth-logo-text p {
            font-size: 11px;
            color: var(--text-muted);
            font-family: 'JetBrains Mono', monospace;
        }

        .auth-card {
            background: var(--bg-card-solid);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 2rem;
            box-shadow: 0 8px 32px rgba(0,0,0,0.4);
        }

        .auth-card h2 {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 0.25rem;
        }

        .auth-card .subtitle {
            font-size: 13px;
            color: var(--text-muted);
            margin-bottom: 1.5rem;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
            margin-bottom: 1rem;
        }

        .form-group label {
            font-size: 12px;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.06em;
            font-weight: 600;
        }

        .form-group input {
            background: #000000;
            border: 1px solid var(--border);
            color: #ffffff;
            padding: 10px 14px;
            border-radius: 8px;
            font-family: 'Space Grotesk', sans-serif;
            font-size: 14px;
            transition: border-color 0.2s;
            width: 100%;
        }

        .form-group input:focus {
            outline: none;
            border-color: var(--gold);
            box-shadow: 0 0 0 3px var(--gold-glow);
        }

        .form-group input::placeholder {
            color: #4a5568;
        }

        .form-check-group {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 1rem;
        }

        .form-check-group input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: var(--gold);
            cursor: pointer;
        }

        .form-check-group label {
            font-size: 13px;
            color: var(--text-muted);
            cursor: pointer;
        }

        .btn-auth {
            width: 100%;
            padding: 11px 24px;
            border: none;
            border-radius: 8px;
            font-family: 'Space Grotesk', sans-serif;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-auth-primary {
            background: var(--gold);
            color: #0a0e0c;
        }

        .btn-auth-primary:hover {
            box-shadow: 0 0 20px var(--gold-glow);
            transform: translateY(-1px);
        }

        .auth-links {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 1.25rem;
        }

        .auth-links a {
            font-size: 13px;
            color: var(--gold);
            text-decoration: none;
            transition: color 0.2s;
        }

        .auth-links a:hover {
            color: #f5c46d;
        }

        .auth-footer {
            text-align: center;
            margin-top: 1.25rem;
            font-size: 13px;
            color: var(--text-muted);
        }

        .auth-footer a {
            color: var(--gold);
            text-decoration: none;
            font-weight: 600;
        }

        .auth-footer a:hover {
            color: #f5c46d;
        }

        .alert-session {
            background: rgba(0, 176, 80, 0.1);
            border: 1px solid rgba(0, 176, 80, 0.3);
            color: var(--green-light);
            padding: 10px 14px;
            border-radius: 8px;
            font-size: 13px;
            margin-bottom: 1rem;
        }

        .alert-error {
            background: rgba(255, 59, 59, 0.1);
            border: 1px solid rgba(255, 59, 59, 0.3);
            color: var(--red);
            padding: 10px 14px;
            border-radius: 8px;
            font-size: 13px;
            margin-bottom: 1rem;
        }

        .input-error {
            font-size: 12px;
            color: var(--red);
            margin-top: 4px;
        }

        .star-container {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            pointer-events: none; z-index: 9999; overflow: hidden;
        }
    </style>
</head>
<body>
    <div class="star-container" id="starContainer"></div>

    <div class="auth-container relative z-10">
        <div class="auth-logo">
            <div class="auth-logo-icon">SX</div>
            <div class="auth-logo-text">
                <h1>SIGNAL XPRESS</h1>
                <p>Master Trading Portal</p>
            </div>
        </div>

        {{ $slot }}
    </div>

    <script>
    (function(){
        var c = document.getElementById('starContainer');
        for(var i=0;i<50;i++){
            var s = document.createElement('div');
            s.style.cssText = 'position:absolute;width:'+Math.random()*2+'px;height:'+Math.random()*2+'px;background:#fff;border-radius:50%;opacity:'+(Math.random()*0.4+0.1)+';left:'+Math.random()*100+'%;top:'+Math.random()*100+'%;animation:twinkle '+((Math.random()*4)+2)+'s ease-in-out infinite '+(Math.random()*4)+'s';
            c.appendChild(s);
        }
    })();
    </script>
    <style>
    @keyframes twinkle { 0%,100%{opacity:0.1} 50%{opacity:0.5} }
    </style>
</body>
</html>
