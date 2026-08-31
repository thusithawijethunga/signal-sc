<x-guest-layout>
    <div class="auth-card">
        <h2>Welcome Back</h2>
        <p class="subtitle">Sign in to your admin portal</p>

        <!-- Session Status -->
        @if (session('status'))
            <div class="alert-session">{{ session('status') }}</div>
        @endif

        <!-- Validation Errors -->
        @if ($errors->any())
            <div class="alert-error">
                @foreach ($errors->all() as $error)
                    <div>{{ $error }}</div>
                @endforeach
            </div>
        @endif

        <form method="POST" action="{{ route('login') }}">
            @csrf

            <!-- Email -->
            <div class="form-group">
                <label for="email">Email Address</label>
                <input id="email" type="email" name="email" value="{{ old('email') }}" required autofocus autocomplete="username" placeholder="admin@signalxpress.com">
            </div>

            <!-- Password -->
            <div class="form-group">
                <label for="password">Password</label>
                <input id="password" type="password" name="password" required autocomplete="current-password" placeholder="Enter your password">
            </div>

            <!-- Remember Me -->
            <div class="form-check-group">
                <input id="remember_me" type="checkbox" name="remember">
                <label for="remember_me">Remember me</label>
            </div>

            <!-- Submit -->
            <button type="submit" class="btn-auth btn-auth-primary">
                <i class="fa-solid fa-right-to-bracket"></i> Log in
            </button>

            <!-- Links -->
            <div class="auth-links">
                @if (Route::has('password.request'))
                    <a href="{{ route('password.request') }}">
                        <i class="fa-solid fa-key"></i> Forgot password?
                    </a>
                @endif
            </div>
        </form>

        @if (Route::has('register'))
        <div class="auth-footer">
            Don't have an account? <a href="{{ route('register') }}">Register</a>
        </div>
        @endif
    </div>
</x-guest-layout>
