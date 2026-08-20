<x-app-layout>
    <x-slot name="header">
        <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-file-csv me-2"></i>Trading CSV Analytics</h2>
    </x-slot>

    <div class="space-y-4">
        {{-- Upload --}}
        <div class="sx-card p-6" style="border:2px dashed var(--border);text-align:center;" x-data="{ filename: '' }">
            <i class="fas fa-cloud-upload-alt text-4xl mb-2" style="color:var(--gold);"></i>
            <p style="color:var(--text-secondary);font-weight:600;">Upload Trading CSV File</p>
            <p style="color:var(--text-muted);font-size:12px;">CSV with columns: Account ID, Symbol, Lots, Commission</p>
            <form action="{{ route('admin.csv.upload') }}" method="POST" enctype="multipart/form-data" class="mt-4">
                @csrf
                <input type="file" name="csv_file" accept=".csv" class="sx-input" required onchange="this.closest('form').submit()">
            </form>
            @if(session('success'))
                <div class="mt-3 text-sm" style="color:var(--green);">{{ session('success') }}</div>
            @endif
        </div>

        {{-- Upload History --}}
        @if(isset($uploads) && $uploads->count())
        <div class="sx-card p-4">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:var(--text-muted);">Upload History</h3>
            <div class="overflow-x-auto">
                <table class="sx-table">
                    <thead><tr><th>ID</th><th>Filename</th><th>Records</th><th>Total Lots</th><th>Commission</th><th>Date</th></tr></thead>
                    <tbody>
                        @foreach($uploads as $u)
                        <tr>
                            <td>{{ $u->id }}</td>
                            <td style="color:#fff;font-weight:600;">{{ $u->filename }}</td>
                            <td>{{ $u->total_records }}</td>
                            <td>{{ number_format($u->total_lots, 2) }}</td>
                            <td style="color:var(--green);font-weight:700;">${{ number_format($u->total_commission, 2) }}</td>
                            <td>{{ $u->created_at->format('Y-m-d H:i') }}</td>
                        </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>
        @endif
    </div>
</x-app-layout>
