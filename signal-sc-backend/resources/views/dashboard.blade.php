<x-app-layout>
    <x-slot name="header">
        <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold" style="color: var(--gold);">
                <i class="fas fa-chart-line me-2"></i>Signal Xpress Dashboard
            </h2>
            <span class="text-xs" style="color: var(--green);"><i class="fas fa-circle me-1"></i> Auto-Sync Active</span>
        </div>
    </x-slot>

    <div x-data="dashboard()" class="space-y-6">
        {{-- Balance Cards --}}
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div class="sx-card p-4">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Start Balance</span>
                <div class="text-xl font-bold mt-1" style="color: #f59e0b;">${{ number_format($startBalance, 2) }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid #38bdf8;">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Deposit</span>
                <div class="text-xl font-bold mt-1" style="color: #38bdf8;">${{ number_format($depositBalance, 2) }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid #f87171;">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Withdraw</span>
                <div class="text-xl font-bold mt-1" style="color: #f87171;">${{ number_format($withdrawBalance, 2) }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid #60a5fa;">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Net Profit</span>
                <div class="text-xl font-bold mt-1" style="color: #60a5fa;">${{ number_format($totalProfit, 2) }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid var(--green);">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Current Balance</span>
                <div class="text-xl font-bold mt-1" style="color: var(--green);">${{ number_format($currentBalance, 2) }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid #a78bfa;">
                <span class="text-xs font-semibold uppercase" style="color: var(--text-muted);">Win Rate</span>
                <div class="text-xl font-bold mt-1" style="color: #fff;">{{ $winRate }}%</div>
            </div>
        </div>

        {{-- Trade Stats --}}
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div class="sx-card p-4">
                <span class="text-xs uppercase" style="color: var(--text-muted);">Total Trades</span>
                <div class="text-lg font-bold mt-1" style="color: var(--text-primary);">{{ $totalTrades }}</div>
            </div>
            <div class="sx-card p-4" style="border-bottom: 3px solid var(--green);">
                <span class="text-xs uppercase font-semibold" style="color: var(--green);">Wins</span>
                <div class="text-lg font-bold mt-1" style="color: var(--green);">{{ $wins }}</div>
            </div>
            <div class="sx-card p-4" style="border-bottom: 3px solid var(--red);">
                <span class="text-xs uppercase font-semibold" style="color: var(--red);">Losses</span>
                <div class="text-lg font-bold mt-1" style="color: var(--red);">{{ $losses }}</div>
            </div>
            <div class="sx-card p-4" style="border-bottom: 3px solid var(--gold);">
                <span class="text-xs uppercase font-semibold" style="color: var(--gold);">BE Trades</span>
                <div class="text-lg font-bold mt-1" style="color: var(--gold);">{{ $bes }}</div>
            </div>
            <div class="sx-card p-4">
                <span class="text-xs uppercase" style="color: #a78bfa;">Net Pips</span>
                <div class="text-lg font-bold mt-1" style="color: #a78bfa;">{{ $netPips >= 0 ? '+' : '' }}{{ $netPips }}</div>
            </div>
            <div class="sx-card p-4" style="border-left: 3px solid var(--red);">
                <span class="text-xs uppercase" style="color: var(--red);">Loss Pips</span>
                <div class="text-lg font-bold mt-1" style="color: var(--red);">{{ $lossPips }}</div>
            </div>
        </div>

        {{-- Recent Trades Table --}}
        <div class="sx-card p-4">
            <h3 class="text-base font-semibold mb-4" style="color: var(--text-primary);">Recent Trades</h3>
            <div class="overflow-x-auto">
                <table class="sx-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Date</th>
                            <th>Pair</th>
                            <th>Direction</th>
                            <th>Entry</th>
                            <th>SL</th>
                            <th>TP</th>
                            <th class="text-right">Pips</th>
                            <th class="text-right">Profit</th>
                            <th class="text-center">Result</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($recentTrades as $trade)
                        <tr>
                            <td style="font-weight:700;color:#fff;">{{ $trade->no }}</td>
                            <td>{{ $trade->date->format('Y-m-d') }}</td>
                            <td style="font-weight:700;color:#fff;">{{ $trade->pair }}</td>
                            <td style="font-weight:700;color:{{ str_contains($trade->direction, 'BUY') ? '#60a5fa' : '#f59e0b' }};">{{ $trade->direction }}</td>
                            <td>{{ $trade->entry1 }}{{ $trade->entry2 ? ' / '.$trade->entry2 : '' }}</td>
                            <td style="color:#f87171;font-weight:600;">{{ $trade->sl ?: '—' }}</td>
                            <td>{{ $trade->tp1 }}{{ $trade->tp2 ? ', '.$trade->tp2 : '' }}{{ $trade->tp3 ? ', '.$trade->tp3 : '' }}</td>
                            <td class="text-right" style="font-weight:600;">{{ $trade->pips }}</td>
                            <td class="text-right" style="font-weight:700;color:#fff;">${{ $trade->profit }}</td>
                            <td class="text-center">
                                <span class="{{ $trade->result === 'WIN' ? 'badge-win' : ($trade->result === 'LOSS' ? 'badge-loss' : 'badge-be') }}">{{ $trade->result }}</span>
                            </td>
                        </tr>
                        @empty
                        <tr>
                            <td colspan="10" class="text-center py-4" style="color: var(--text-muted);">No trades yet. Add trades in the Admin Panel.</td>
                        </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    @push('scripts')
    <script>
        function dashboard() {
            return {
                init() {}
            }
        }
    </script>
    @endpush
</x-app-layout>
