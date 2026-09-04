<x-layouts.admin>

<div x-data="dbBackupApp()">
  <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom border-secondary">
    <div class="d-flex align-items-center gap-3">
      <div class="p-2 rounded-3 bg-dark border border-warning">
        <i class="ti ti-database text-warning fs-3"></i>
      </div>
      <div>
        <h3 class="m-0 text-white fw-bold fs-4">Database Backup & Restore</h3>
        <small class="text-secondary">Export SQL dumps and import to restore</small>
      </div>
    </div>
  </div>

  @if(session('success'))
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      <i class="ti ti-check-circle me-1"></i> {{ session('success') }}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  @endif

  @if(session('import_errors'))
    <div class="alert alert-warning alert-dismissible fade show" role="alert">
      <i class="ti ti-alert-triangle me-1"></i> <strong>Import Warnings:</strong>
      <ul class="mb-0 mt-1">
        @foreach(session('import_errors') as $err)
          <li class="text-small">{{ $err }}</li>
        @endforeach
      </ul>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  @endif

  @if($errors->any())
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      <i class="ti ti-x-circle me-1"></i>
      @foreach($errors->all() as $err)
        <div>{{ $err }}</div>
      @endforeach
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  @endif

  <div class="row g-4 mb-4">
    <div class="col-md-6">
      <div class="sec-ib p-4 h-100">
        <div class="d-flex align-items-center gap-2 mb-3">
          <i class="ti ti-upload text-success fs-4"></i>
          <h5 class="m-0 text-white fw-bold">Export Database</h5>
        </div>
        <p class="text-secondary mb-3">Download a full SQL dump of the database. Uses <code>mysqldump</code> with single-transaction, routines, triggers, and events.</p>
        <div class="d-flex align-items-center gap-3">
          <a href="{{ route('admin.db-backup.export') }}" class="btn btn-success btn-lg" onclick="this.classList.add('disabled'); this.innerHTML='<i class=\'ti ti-loader ti-spin me-1\'></i> Exporting...';">
            <i class="ti ti-download me-2"></i> Export SQL Dump
          </a>
          <span class="text-secondary text-small">Downloads as <code>.sql</code> file</span>
        </div>
      </div>
    </div>

    <div class="col-md-6">
      <div class="sec-ib p-4 h-100">
        <div class="d-flex align-items-center gap-2 mb-3">
          <i class="ti ti-upload text-info fs-4"></i>
          <h5 class="m-0 text-white fw-bold">Import Database</h5>
        </div>
        <p class="text-secondary mb-3">Upload a <code>.sql</code> file to restore the database. Max file size: 50MB. Statements are executed one by one.</p>
        <form action="{{ route('admin.db-backup.import') }}" method="POST" enctype="multipart/form-data" id="importForm">
          @csrf
          <div class="d-flex align-items-center gap-3">
            <div class="flex-grow-1">
              <input type="file" class="form-control form-control-ib" name="sql_file" accept=".sql,.sql.gz" required
                     @change="importFile = $event.target.files[0]?.name || ''">
            </div>
            <button type="submit" class="btn btn-info btn-lg" :disabled="!importFile || importing" @click="importing = true">
              <span x-show="!importing"><i class="ti ti-upload me-1"></i> Import</span>
              <span x-show="importing"><i class="ti ti-loader ti-spin me-1"></i> Importing...</span>
            </button>
          </div>
          <div class="text-secondary text-small mt-2" x-show="importFile">
            <i class="ti ti-file me-1"></i> <span x-text="importFile"></span>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div class="sec-ib p-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="d-flex align-items-center gap-2">
        <i class="ti ti-folder-open text-warning fs-5"></i>
        <h5 class="m-0 text-white fw-bold">Existing Backups</h5>
      </div>
      <span class="badge bg-secondary" x-text="'{{ $backups->count() }} file(s)'"></span>
    </div>

    @if($backups->isEmpty())
      <div class="text-center text-secondary py-5">
        <i class="ti ti-database-off fs-1 mb-2 d-block"></i>
        <p>No backup files found. Export your first backup above.</p>
      </div>
    @else
      <div class="table-responsive">
        <table class="table table-dark-custom">
          <thead>
            <tr>
              <th style="width:40px">#</th>
              <th>Filename</th>
              <th style="width:120px">Size</th>
              <th style="width:180px">Date</th>
              <th style="width:140px">Actions</th>
            </tr>
          </thead>
          <tbody>
            @foreach($backups as $idx => $backup)
              <tr>
                <td x-text="{{ $idx + 1 }}"></td>
                <td class="font-mono text-info">{{ $backup['name'] }}</td>
                <td>{{ number_format($backup['size'] / 1024, 1) }} KB</td>
                <td>{{ date('Y-m-d H:i:s', $backup['date']) }}</td>
                <td>
                  <a href="{{ route('admin.db-backup.download', $backup['name']) }}" class="btn btn-sm btn-outline-success me-1" title="Download">
                    <i class="ti ti-download"></i>
                  </a>
                  <button class="btn btn-sm btn-outline-danger" title="Delete"
                          @click="if(confirm('Delete {{ $backup['name'] }}?')) $refs.deleteForm{{ $idx }}.submit()">
                    <i class="ti ti-trash"></i>
                  </button>
                  <form x-ref="deleteForm{{ $idx }}" action="{{ route('admin.db-backup.delete', $backup['name']) }}" method="POST" class="d-none">
                    @csrf
                    @method('DELETE')
                  </form>
                </td>
              </tr>
            @endforeach
          </tbody>
        </table>
      </div>
    @endif
  </div>
</div>

<script>
function dbBackupApp() {
  return {
    importFile: '',
    importing: false,
  };
}
</script>

</x-layouts.admin>
