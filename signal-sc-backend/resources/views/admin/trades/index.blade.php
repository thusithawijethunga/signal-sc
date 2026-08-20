<x-app-layout>
    <x-slot name="header">
        <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-chart-bar me-2"></i>Trade Ledger</h2>
    </x-slot>

    <div x-data="{ period: 'ALL', resultFilter: 'ALL' }" class="space-y-4">
        {{-- Filters --}}
        <div class="sx-card p-3 flex flex-wrap gap-2 items-center">
            <template x-for="p in ['ALL','THIS_WEEK','LAST_WEEK','LAST_MONTH']">
                <button @click="period = p" :class="period === p ? 'sx-btn-gold' : ''" class="sx-btn text-xs" x-text="p.replace('_',' ')"></button>
            </template>
            <div class="ms-auto flex gap-2">
                <template x-for="r in ['ALL','WIN','LOSS','BE']">
                    <button @click="resultFilter = r" :class="resultFilter === r ? 'sx-btn-gold' : ''" class="sx-btn text-xs" x-text="r"></button>
                </template>
            </div>
        </div>

        {{-- Table --}}
        <div class="sx-card p-4">
            <div class="overflow-x-auto">
                <table class="sx-table">
                    <thead>
                        <tr>
                            <th>#</th><th>Date</th><th>Pair</th><th>Direction</th>
                            <th>Entry</th><th>SL</th><th>TP</th>
                            <th class="text-right">Pips</th><th class="text-right">Profit</th><th class="text-center">Result</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($trades as $trade)
                        <tr>
                            <td style="font-weight:700;color:#fff;">{{ $trade->no }}</td>
                            <td>{{ $trade->date->format('Y-m-d') }}</td>
                            <td style="font-weight:700;color:#fff;">{{ $trade->pair }}</td>
                            <td style="color:{{ str_contains($trade->direction, 'BUY') ? '#60a5fa' : '#f59e0b' }};font-weight:700;">{{ $trade->direction }}</td>
                            <td>{{ $trade->entry1 }}{{ $trade->entry2 ? ' / '.$trade->entry2 : '' }}</td>
                            <td style="color:#f87171;font-weight:600;">{{ $trade->sl ?: '—' }}</td>
                            <td>{{ $trade->tp1 }}{{ $trade->tp2 ? ', '.$trade->tp2 : '' }}</td>
                            <td class="text-right" style="font-weight:600;">{{ $trade->pips }}</td>
                            <td class="text-right" style="font-weight:700;color:#fff;">${{ number_format($trade->profit, 2) }}</td>
                            <td class="text-center">
                                <span class="{{ $trade->result === 'WIN' ? 'badge-win' : ($trade->result === 'LOSS' ? 'badge-loss' : 'badge-be') }}">{{ $trade->result }}</span>
                            </td>
                            <td>
                                <div class="flex gap-1">
                                    <form action="{{ route('admin.trades.destroy', $trade) }}" method="POST" onsubmit="return confirm('Delete?')">
                                        @csrf @method('DELETE')
                                        <button class="sx-btn text-xs" style="background:rgba(255,59,59,0.2);color:#ff6b6b;border-color:rgba(255,59,59,0.4);padding:4px 8px;">Del</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                        @empty
                        <tr><td colspan="11" class="text-center py-4" style="color: var(--text-muted);">No trades recorded.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
            <div class="mt-4">{{ $trades->links() }}</div>
        </div>

        {{-- Quick Add --}}
        <div class="sx-card p-4">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color: var(--gold);">Quick Add Trade</h3>
            <form action="{{ route('admin.trades.store') }}" method="POST" class="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-3">
                @csrf
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Date</label><input type="date" name="date" value="{{ date('Y-m-d') }}" class="sx-input" required></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Pair</label><input type="text" name="pair" value="XAU/USD" class="sx-input"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Direction</label><select name="direction" class="sx-input"><option>BUY</option><option>SELL</option><option>BUY LIMIT</option><option>SELL LIMIT</option></select></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Entry</label><input type="number" name="entry1" step="0.01" class="sx-input"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Pips</label><input type="number" name="pips" step="0.1" class="sx-input" value="0"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Profit $</label><input type="number" name="profit" step="0.01" class="sx-input" value="0"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">SL</label><input type="number" name="sl" step="0.01" class="sx-input"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">TP1</label><input type="number" name="tp1" step="0.01" class="sx-input"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">TP2</label><input type="number" name="tp2" step="0.01" class="sx-input"></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Result</label><select name="result" class="sx-input"><option>WIN</option><option>LOSS</option><option>BE</option></select></div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Channel</label><select name="channel" class="sx-input"><option>VIP</option><option>FREE</option></select></div>
                <div class="flex items-end"><button type="submit" class="sx-btn sx-btn-green w-full"><i class="fas fa-plus me-1"></i> Add</button></div>
            </form>
        </div>
    </div>
</x-app-layout>
