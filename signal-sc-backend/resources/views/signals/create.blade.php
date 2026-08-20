<x-app-layout>
    <x-slot name="header">
        <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-plus-circle me-2"></i>Broadcast New Signal</h2>
    </x-slot>

    <div class="sx-card p-6 max-w-4xl">
        <form action="{{ route('admin.signals.store') }}" method="POST" class="space-y-4">
            @csrf
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Date</label>
                    <input type="date" name="date" value="{{ date('Y-m-d') }}" class="sx-input" required>
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Pair</label>
                    <input type="text" name="pair" value="XAU/USD" class="sx-input">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Direction</label>
                    <select name="direction" class="sx-input">
                        <option value="BUY">BUY</option>
                        <option value="SELL">SELL</option>
                        <option value="BUY LIMIT">BUY LIMIT</option>
                        <option value="SELL LIMIT">SELL LIMIT</option>
                    </select>
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Channel</label>
                    <select name="channel" class="sx-input">
                        <option value="VIP">VIP</option>
                        <option value="FREE">FREE</option>
                    </select>
                </div>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Entry 1</label>
                    <input type="number" name="entry1" step="0.01" class="sx-input" placeholder="2350.00">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Entry 2</label>
                    <input type="number" name="entry2" step="0.01" class="sx-input" placeholder="2345.00">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Stop Loss</label>
                    <input type="number" name="sl" step="0.01" class="sx-input" placeholder="2330.00">
                </div>
            </div>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">TP 1</label>
                    <input type="number" name="tp1" step="0.01" class="sx-input" placeholder="2360.00">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">TP 2</label>
                    <input type="number" name="tp2" step="0.01" class="sx-input" placeholder="2370.00">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">TP 3</label>
                    <input type="number" name="tp3" step="0.01" class="sx-input" placeholder="2380.00">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">TP 4</label>
                    <input type="number" name="tp4" step="0.01" class="sx-input" placeholder="2390.00">
                </div>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Pips</label>
                    <input type="number" name="pips" step="0.1" class="sx-input" value="0">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Profit ($)</label>
                    <input type="number" name="profit" step="0.01" class="sx-input" value="0">
                </div>
                <div>
                    <label class="block text-xs font-semibold uppercase mb-1" style="color: var(--text-muted);">Result</label>
                    <select name="result" class="sx-input">
                        <option value="RUNNING">RUNNING</option>
                        <option value="WIN">WIN</option>
                        <option value="LOSS">LOSS</option>
                        <option value="BE">BE</option>
                    </select>
                </div>
            </div>
            @if ($errors->any())
                <div class="text-sm" style="color: var(--red);">@foreach ($errors->all() as $error) <p>{{ $error }}</p> @endforeach</div>
            @endif
            <div class="flex gap-3">
                <button type="submit" class="sx-btn sx-btn-green flex-1"><i class="fas fa-save me-1"></i> Broadcast Signal</button>
                <a href="{{ route('admin.signals.index') }}" class="sx-btn" style="background:var(--border);color:var(--text-secondary);">Cancel</a>
            </div>
        </form>
    </div>
</x-app-layout>
