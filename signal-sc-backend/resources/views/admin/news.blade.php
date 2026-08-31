<x-layouts.admin>
<div class="row g-4">
  <!-- Stats -->
  <div class="col-12">
    <div class="d-flex gap-3 flex-wrap">
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-primary fs-2 fw-bold">{{ $stats['total'] }}</div>
        <small class="text-secondary">Total Events</small>
      </div>
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-danger fs-2 fw-bold">{{ $stats['high'] }}</div>
        <small class="text-secondary">High Impact</small>
      </div>
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-warning fs-2 fw-bold">{{ $stats['medium'] }}</div>
        <small class="text-secondary">Medium Impact</small>
      </div>
    </div>
  </div>

  <!-- Add News + Sync -->
  <div class="col-lg-5">
    <div class="card-ib p-4">
      <h5 class="text-white mb-3"><i class="fa-solid fa-plus text-success me-2"></i>Add News Event</h5>
      <form action="{{ route('admin.news.store') }}" method="POST">
        @csrf
        <div class="mb-2"><input name="title" class="form-control form-control-ib" placeholder="Event Title" required></div>
        <div class="row mb-2">
          <div class="col"><input name="currency" class="form-control form-control-ib" placeholder="USD" required></div>
          <div class="col"><select name="impact" class="form-select form-control-ib"><option>HIGH</option><option>MEDIUM</option><option>LOW</option></select></div>
        </div>
        <div class="mb-2"><input name="event_time" type="datetime-local" class="form-control form-control-ib" required></div>
        <div class="row mb-2">
          <div class="col"><input name="forecast" class="form-control form-control-ib" placeholder="Forecast"></div>
          <div class="col"><input name="previous" class="form-control form-control-ib" placeholder="Previous"></div>
          <div class="col"><input name="actual" class="form-control form-control-ib" placeholder="Actual"></div>
        </div>
        <div class="mb-2"><textarea name="description" class="form-control form-control-ib" rows="2" placeholder="Description"></textarea></div>
        <button class="btn btn-success w-100"><i class="fa-solid fa-plus me-1"></i> Add Event</button>
      </form>

      <hr class="border-secondary my-3">

      <h6 class="text-white mb-2"><i class="fa-solid fa-sync text-info me-2"></i>Sync from External API</h6>
      <form action="{{ route('admin.news.sync') }}" method="POST">
        @csrf
        <button class="btn btn-outline-info w-100"><i class="fa-solid fa-cloud-arrow-down me-1"></i> Sync from nfp.ourforecast.com</button>
      </form>
    </div>
  </div>

  <!-- News List -->
  <div class="col-lg-7">
    <div class="card-ib p-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="text-white mb-0"><i class="fa-solid fa-newspaper text-primary me-2"></i>Market News Events</h5>
        <div class="d-flex gap-2">
          <a href="{{ route('admin.news', array_merge(request()->query(), ['sort' => 'event_time'])) }}"
             class="btn btn-sm {{ request('sort', 'event_time') === 'event_time' ? 'btn-warning' : 'btn-outline-secondary' }}">
            <i class="fa-solid fa-calendar me-1"></i> By Date
          </a>
          <a href="{{ route('admin.news', array_merge(request()->query(), ['sort' => 'newest'])) }}"
             class="btn btn-sm {{ request('sort') === 'newest' ? 'btn-success' : 'btn-outline-secondary' }}">
            <i class="fa-solid fa-bolt me-1"></i> Latest First
          </a>
        </div>
      </div>

      @forelse($news as $item)
      @php
        $isNew = $item->created_at && $item->created_at->diffInMinutes(now()) < 60;
      @endphp
      <div class="border border-secondary rounded p-3 mb-3 {{ $isNew ? 'border-success' : '' }}">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <strong class="text-white">{{ $item->title }}</strong>
            @if($isNew)
              <span class="badge bg-success ms-2" style="font-size:10px;animation:pulse 1.5s infinite;">NEW</span>
            @endif
            <div class="mt-1">
              <span class="badge bg-{{ $item->impact === 'HIGH' ? 'danger' : ($item->impact === 'MEDIUM' ? 'warning' : 'secondary') }}">{{ $item->impact }}</span>
              <span class="badge bg-primary">{{ $item->currency }}</span>
              <small class="text-secondary ms-2">{{ $item->event_time?->format('M d, Y H:i') }}</small>
            </div>
          </div>
          <div class="d-flex gap-1">
            <form action="{{ route('admin.news.destroy', $item) }}" method="POST">
              @csrf @method('DELETE')
              <button class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete?')"><i class="fa-solid fa-trash"></i></button>
            </form>
          </div>
        </div>
        <div class="row mt-2 small">
          <div class="col text-info">Forecast: {{ $item->forecast ?? '-' }}</div>
          <div class="col text-warning">Previous: {{ $item->previous ?? '-' }}</div>
          <div class="col text-success">Actual: {{ $item->actual ?? '-' }}</div>
        </div>
        @if($item->description)<p class="text-secondary mt-2 mb-0 small">{{ $item->description }}</p>@endif
      </div>
      @empty
      <div class="text-center text-secondary py-4">No news events</div>
      @endforelse

      {{ $news->withQueryString()->links() }}
    </div>
  </div>
</div>

@if(session('success'))
<div class="position-fixed bottom-0 end-0 p-3" style="z-index:9999">
  <div class="toast show align-items-center text-bg-success border-0"><div class="d-flex"><div class="toast-body">{{ session('success') }}</div></div></div>
</div>
@endif
@if(session('error'))
<div class="position-fixed bottom-0 end-0 p-3" style="z-index:9999">
  <div class="toast show align-items-center text-bg-danger border-0"><div class="d-flex"><div class="toast-body">{{ session('error') }}</div></div></div>
</div>
@endif
</x-layouts.admin>

<style>
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
