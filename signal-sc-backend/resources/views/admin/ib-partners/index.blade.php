<x-app-layout>
    <x-slot name="header">
        <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-users me-2"></i>IB Partner System</h2>
    </x-slot>

    <div x-data="{ tab: 'members' }" class="space-y-4">
        <div class="flex gap-2">
            <button @click="tab = 'members'" :class="tab === 'members' ? 'sx-btn-gold' : ''" class="sx-btn text-xs"><i class="fas fa-list me-1"></i> Members</button>
            <button @click="tab = 'add'" :class="tab === 'add' ? 'sx-btn-gold' : ''" class="sx-btn text-xs"><i class="fas fa-user-plus me-1"></i> Add Member</button>
            <button @click="tab = 'partners'" :class="tab === 'partners' ? 'sx-btn-gold' : ''" class="sx-btn text-xs"><i class="fas fa-handshake me-1"></i> Partners</button>
        </div>

        {{-- Members List --}}
        <div x-show="tab === 'members'" class="sx-card p-4">
            <div class="mb-3">
                <input type="text" placeholder="Search by SX ID, Name, Account ID, NIC..." class="sx-input" style="max-width:400px;">
            </div>
            <div class="overflow-x-auto">
                <table class="sx-table">
                    <thead><tr><th>SX ID</th><th>Name</th><th>Broker</th><th>Account ID</th><th>NIC</th><th>Partner</th></tr></thead>
                    <tbody>
                        @forelse($members as $m)
                        <tr>
                            <td><span style="background:rgba(56,189,248,0.15);color:#38bdf8;padding:2px 8px;border-radius:4px;font-weight:700;font-family:monospace;">{{ $m->sx_id }}</span></td>
                            <td style="font-weight:700;color:#fff;">{{ $m->name }}</td>
                            <td>{{ $m->broker }}</td>
                            <td style="font-family:monospace;color:#38bdf8;">{{ $m->account_id }}</td>
                            <td>{{ $m->nic }}</td>
                            <td><span style="background:var(--border);padding:2px 8px;border-radius:4px;font-size:11px;">{{ $m->partner->name ?? '—' }}</span></td>
                        </tr>
                        @empty
                        <tr><td colspan="6" class="text-center py-4" style="color:var(--text-muted);">No members yet.</td></tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
            <div class="mt-3"><span class="text-xs" style="color:var(--text-muted);">Total: {{ $members->count() }}</span></div>
        </div>

        {{-- Add Member Form --}}
        <div x-show="tab === 'add'" class="sx-card p-4">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:#10b981;">Add New Member</h3>
            <form action="{{ route('admin.ib.member.store') }}" method="POST" class="space-y-3 max-w-lg">
                @csrf
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Name</label><input type="text" name="name" class="sx-input" required></div>
                <div class="grid grid-cols-2 gap-3">
                    <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Broker</label><input type="text" name="broker" value="XM" class="sx-input"></div>
                    <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Account ID</label><input type="text" name="account_id" class="sx-input" required></div>
                </div>
                <div><label class="block text-xs mb-1" style="color:var(--text-muted);">NIC / Passport</label><input type="text" name="nic" class="sx-input"></div>
                <div class="grid grid-cols-2 gap-3">
                    <div><label class="block text-xs mb-1" style="color:var(--text-muted);">WhatsApp</label><input type="text" name="whatsapp" class="sx-input"></div>
                    <div><label class="block text-xs mb-1" style="color:var(--text-muted);">Telegram</label><input type="text" name="telegram" class="sx-input"></div>
                </div>
                <div>
                    <label class="block text-xs mb-1" style="color:var(--text-muted);">Partner</label>
                    <select name="partner_id" class="sx-input">
                        <option value="">Select Partner</option>
                        @foreach($partners as $p)
                            <option value="{{ $p->id }}">{{ $p->name }}</option>
                        @endforeach
                    </select>
                </div>
                <button type="submit" class="sx-btn sx-btn-green w-full"><i class="fas fa-save me-1"></i> Save Member</button>
            </form>
        </div>

        {{-- Partners List --}}
        <div x-show="tab === 'partners'" class="sx-card p-4">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:var(--gold);">Partner Directory</h3>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
                @foreach($partners as $p)
                    <div class="sx-card p-3 text-center">
                        <div style="color:#fff;font-weight:700;">{{ $p->name }}</div>
                        <div style="color:var(--text-muted);font-size:11px;">{{ $p->members_count ?? $p->members->count() }} members</div>
                    </div>
                @endforeach
            </div>
            <form action="{{ route('admin.ib.partner.store') }}" method="POST" class="flex gap-2 max-w-md">
                @csrf
                <input type="text" name="name" placeholder="New partner name" class="sx-input flex-1" required>
                <button type="submit" class="sx-btn sx-btn-gold"><i class="fas fa-plus me-1"></i> Add Partner</button>
            </form>
        </div>
    </div>
</x-app-layout>
