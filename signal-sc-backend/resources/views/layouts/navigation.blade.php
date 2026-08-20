<nav x-data="{ open: false }" style="background: var(--bg-card); border-bottom: 1px solid var(--border);">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
            <div class="flex items-center gap-3">
                <a href="{{ route('dashboard') }}" class="flex items-center gap-2 text-decoration-none">
                    <div style="width:36px;height:36px;background:linear-gradient(135deg,#d4a04c,#f5c46d);border-radius:10px;display:flex;align-items:center;justify-content:center;font-weight:700;color:#0a0e0c;font-size:14px;">SX</div>
                    <span style="font-weight:700;color:#fff;font-size:15px;">SIGNAL XPRESS</span>
                </a>
                <div class="hidden sm:flex items-center gap-2 ms-6">
                    <a href="{{ route('dashboard') }}" class="sx-nav-link {{ request()->routeIs('dashboard') ? 'active' : '' }}">
                        <i class="fas fa-chart-line me-1"></i> Dashboard
                    </a>
                    <a href="{{ route('admin.signals.index') }}" class="sx-nav-link {{ request()->routeIs('admin.signals.*') ? 'active' : '' }}">
                        <i class="fas fa-broadcast-tower me-1"></i> Signals
                    </a>
                    <a href="{{ route('admin.trades.index') }}" class="sx-nav-link {{ request()->routeIs('admin.trades.*') ? 'active' : '' }}">
                        <i class="fas fa-chart-bar me-1"></i> Trades
                    </a>
                    <a href="{{ route('admin.ib.index') }}" class="sx-nav-link {{ request()->routeIs('admin.ib.*') ? 'active' : '' }}">
                        <i class="fas fa-users me-1"></i> IB Partners
                    </a>
                    <a href="{{ route('admin.csv.index') }}" class="sx-nav-link {{ request()->routeIs('admin.csv.*') ? 'active' : '' }}">
                        <i class="fas fa-file-csv me-1"></i> CSV Analytics
                    </a>
                    <a href="{{ route('admin.settings.index') }}" class="sx-nav-link {{ request()->routeIs('admin.settings.*') ? 'active' : '' }}">
                        <i class="fas fa-cog me-1"></i> Settings
                    </a>
                </div>
            </div>
            <div class="hidden sm:flex sm:items-center sm:ms-6">
                <x-dropdown align="right" width="48">
                    <x-slot name="trigger">
                        <button class="sx-nav-link flex items-center gap-1">
                            <i class="fas fa-user-circle"></i>
                            {{ Auth::user()->name }}
                            <svg class="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
                        </button>
                    </x-slot>
                    <x-slot name="content">
                        <x-dropdown-link :href="route('profile.edit')">
                            <i class="fas fa-user-edit me-1"></i> {{ __('Profile') }}
                        </x-dropdown-link>
                        <form method="POST" action="{{ route('logout') }}">
                            @csrf
                            <x-dropdown-link :href="route('logout')" onclick="event.preventDefault(); this.closest('form').submit();">
                                <i class="fas fa-sign-out-alt me-1"></i> {{ __('Log Out') }}
                            </x-dropdown-link>
                        </form>
                    </x-slot>
                </x-dropdown>
            </div>
            <div class="-me-2 flex items-center sm:hidden">
                <button @click="open = ! open" class="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none">
                    <svg class="h-6 w-6" stroke="currentColor" fill="none" viewBox="0 0 24 24">
                        <path :class="{'hidden': open, 'inline-flex': ! open }" class="inline-flex" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                        <path :class="{'hidden': ! open, 'inline-flex': open }" class="hidden" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </div>
    </div>
    <div :class="{'block': open, 'hidden': ! open}" class="hidden sm:hidden">
        <div class="pt-2 pb-3 space-y-1 px-4">
            <a href="{{ route('dashboard') }}" class="block sx-nav-link {{ request()->routeIs('dashboard') ? 'active' : '' }}">Dashboard</a>
            <a href="{{ route('admin.signals.index') }}" class="block sx-nav-link {{ request()->routeIs('admin.signals.*') ? 'active' : '' }}">Signals</a>
            <a href="{{ route('admin.trades.index') }}" class="block sx-nav-link {{ request()->routeIs('admin.trades.*') ? 'active' : '' }}">Trades</a>
            <a href="{{ route('admin.ib.index') }}" class="block sx-nav-link {{ request()->routeIs('admin.ib.*') ? 'active' : '' }}">IB Partners</a>
            <a href="{{ route('admin.csv.index') }}" class="block sx-nav-link {{ request()->routeIs('admin.csv.*') ? 'active' : '' }}">CSV Analytics</a>
            <a href="{{ route('admin.settings.index') }}" class="block sx-nav-link {{ request()->routeIs('admin.settings.*') ? 'active' : '' }}">Settings</a>
        </div>
        <div class="pt-4 pb-1 border-t" style="border-color: var(--border);">
            <div class="px-4">
                <div class="font-medium text-sm" style="color: var(--text-primary);">{{ Auth::user()->name }}</div>
                <div class="font-medium text-xs" style="color: var(--text-muted);">{{ Auth::user()->email }}</div>
            </div>
            <div class="mt-3 space-y-1 px-4">
                <form method="POST" action="{{ route('logout') }}">
                    @csrf
                    <button type="submit" class="block sx-nav-link w-full text-left">Log Out</button>
                </form>
            </div>
        </div>
    </div>
</nav>
