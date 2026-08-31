<x-guest-layout>
    <div class="auth-card">
        <h2><i class="fa-solid fa-lock" style="color: var(--gold);"></i> Reset Password</h2>
        <p class="subtitle">Enter your email and we'll send you a reset link</p>

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

        <form method="POST" action="{{ route('password.email') }}">
            @csrf

            <!-- Email -->
            <div class="form-group">
                <label for="email">Email Address</label>
                <input id="email" type="email" name="email" value="{{ old('email') }}" required autofocus placeholder="admin@signalxpress.com">
            </div>

            <!-- Submit -->
            <button type="submit" class="btn-auth btn-auth-primary">
                <i class="fa-solid fa-paper-plane"></i> Send Reset Link
            </button>

            <!-- Back to Login -->
            <div class="auth-links" style="justify-content: center; margin-top: 1.5rem;">
                <a href="{{ route('login') }}">
                    <i class="fa-solid fa-arrow-left"></i> Back to Login
                </a>
            </div>
        </form>
    </div>
</x-guest-layout>
