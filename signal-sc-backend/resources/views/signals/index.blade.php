<x-app-layout>
    <x-slot name="header">
        <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-broadcast-tower me-2"></i>Signal Management</h2>
            <a href="{{ route('admin.signals.create') }}" class="sx-btn sx-btn-gold">
                <i class="fas fa-plus me-1"></i> New Signal
            </a>
        </div>
    </x-slot>

    <div class="sx-card p-4">
        <div class="overflow-x-auto">
            <table class="sx-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Date</th>
                        <th>Pair</th>
                        <th>Direction</th>
                        <th>Entry 1/2</th>
                        <th>SL</th>
                        <th>TP 1-4</th>
                        <th class="text-right">Pips</th>
                        <th class="text-right">Profit</th>
                        <th>Channel</th>
                        <th class="text-center">Result</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($signals as $signal)
                    <tr>
                        <td style="font-weight:700;color:#fff;">{{ $signal->no }}</td>
                        <td>{{ $signal->date->format('Y-m-d') }}</td>
                        <td style="font-weight:700;color:#fff;">{{ $signal->pair }}</td>
                        <td style="color:{{ str_contains($signal->direction, 'BUY') ? '#60a5fa' : '#f59e0b' }};font-weight:700;">{{ $signal->direction }}</td>
                        <td>{{ $signal->entry1 }}{{ $signal->entry2 ? ' / '.$signal->entry2 : '' }}</td>
                        <td style="color:#f87171;font-weight:600;">{{ $signal->sl ?: '—' }}</td>
                        <td>{{ $signal->tp1 }}{{ $signal->tp2 ? ', '.$signal->tp2 : '' }}{{ $signal->tp3 ? ', '.$signal->tp3 : '' }}{{ $signal->tp4 ? ', '.$signal->tp4 : '' }}</td>
                        <td class="text-right">{{ $signal->pips }}</td>
                        <td class="text-right" style="font-weight:700;color:#fff;">${{ number_format($signal->profit, 2) }}</td>
                        <td><span style="color:{{ $signal->channel === 'VIP' ? '#f59e0b' : '#38bdf8' }};font-weight:600;font-size:11px;">{{ $signal->channel }}</span></td>
                        <td class="text-center">
                            <span class="{{ $signal->result === 'WIN' ? 'badge-win' : ($signal->result === 'LOSS' ? 'badge-loss' : ($signal->result === 'BE' ? 'badge-be' : 'badge-running')) }}">{{ $signal->result }}</span>
                        </td>
                        <td>
                            <div class="flex gap-1">
                                <a href="{{ route('admin.signals.edit', $signal) }}" class="sx-btn text-xs" style="background:rgba(212,160,76,0.2);color:#d4a04c;border-color:rgba(212,160,76,0.4);padding:4px 8px;">Edit</a>
                                <form action="{{ route('admin.signals.destroy', $signal) }}" method="POST" onsubmit="return confirm('Delete this signal?')">
                                    @csrf @method('DELETE')
                                    <button class="sx-btn text-xs" style="background:rgba(255,59,59,0.2);color:#ff6b6b;border-color:rgba(255,59,59,0.4);padding:4px 8px;">Del</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="12" class="text-center py-4" style="color: var(--text-muted);">No signals yet.</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        <div class="mt-4">
            {{ $signals->links() }}
        </div>
    </div>
</x-app-layout>
