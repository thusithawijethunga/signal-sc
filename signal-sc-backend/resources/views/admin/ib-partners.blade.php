<x-layouts.admin>

<div x-data="ibPartnerApp()">
  <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom border-secondary">
    <div class="d-flex align-items-center gap-3">
      <div class="p-2 rounded-3 bg-dark border border-primary">
        <i class="fa-solid fa-user-shield text-primary fs-3"></i>
      </div>
      <div>
        <h3 class="m-0 text-white fw-bold fs-4">IB Partner & Trading Analytics System</h3>
        <small class="text-secondary">Google Sheet Dynamic Sync & Member Registration Database</small>
      </div>
    </div>
    <div class="d-flex align-items-center gap-2">
      <input type="text" x-model="ibGsUrl" class="form-control form-control-ib form-control-sm text-xs" style="width: 280px;" placeholder="Google Web App URL for IB Partner">
      <button class="btn btn-sm btn-outline-primary text-white" @click="loadIbData()">
        <i class="fa-solid fa-rotate me-1"></i> Refresh Sheet
      </button>
      <span class="badge bg-warning text-dark" x-text="ibSyncStatus">Connecting...</span>
    </div>
  </div>

  <div class="row g-4">
    <div class="col-12">
      <div class="card-ib p-4">
        <h5 class="text-white mb-3">
          <i class="fa-solid fa-magnifying-glass text-primary me-2"></i>සෙවීම (Search by SX ID / Account ID / NIC)
        </h5>
        <div class="input-group">
          <input type="text" x-model="ibSearchQuery" @input="searchIbUser()" class="form-control form-control-ib form-control-lg" placeholder="SX ID (උදා: 1012 හෝ SX1012), Account ID හෝ NIC එක ඇතුළත් කරන්න...">
          <button class="btn btn-primary px-4" @click="searchIbUser()">
            <i class="fa-solid fa-search me-1"></i> Search
          </button>
        </div>
        <div class="mt-3" x-html="ibSearchResultHtml"></div>
      </div>
    </div>

    <div class="col-lg-5">
      <div class="card-ib p-4 h-100">
        <h5 class="text-white mb-3">
          <i class="fa-solid fa-user-plus text-success me-2"></i>අලුත් සාමාජිකයෙක් ඇතුළත් කිරීම
        </h5>
        <form @submit.prevent="saveIbMember()">
          <div class="mb-2">
            <label>නම (Name)</label>
            <input type="text" x-model="ibForm.name" class="form-control form-control-ib" placeholder="සාමාජිකයාගේ නම" required>
          </div>

          <div class="row mb-2">
            <div class="col-6">
              <label>Broker Name</label>
              <input type="text" x-model="ibForm.broker" class="form-control form-control-ib" value="XM">
            </div>
            <div class="col-6">
              <label>Account ID</label>
              <input type="text" x-model="ibForm.accountId" class="form-control form-control-ib" placeholder="Account ID" required>
            </div>
          </div>

          <div class="mb-2">
            <label>NIC / Passport Number</label>
            <input type="text" x-model="ibForm.nic" class="form-control form-control-ib" placeholder="ID නම්‍බර් එක" required>
          </div>

          <div class="row mb-2">
            <div class="col-6">
              <label>WhatsApp Number</label>
              <input type="text" x-model="ibForm.whatsapp" class="form-control form-control-ib" placeholder="07XXXXXXXX">
            </div>
            <div class="col-6">
              <label>Telegram Username</label>
              <input type="text" x-model="ibForm.telegram" class="form-control form-control-ib" placeholder="@username">
            </div>
          </div>

          <div class="mb-3">
            <label>Partner Name</label>
            <div class="input-group">
              <select x-model="ibForm.partner" class="form-select form-select-ib">
                @foreach($ibPartners as $partner)
                  <option value="{{ $partner->name }}">{{ $partner->name }}</option>
                @endforeach
              </select>
              <button type="button" class="btn btn-outline-secondary text-white" @click="addIbPartner()">+ Partner</button>
            </div>
          </div>

          <button type="submit" class="btn btn-success w-100 py-2 mt-2" :disabled="ibSaving">
            <i class="fa-solid fa-floppy-disk me-1"></i> Save Member
          </button>
        </form>
        <div class="mt-3" x-html="ibStatusHtml"></div>
      </div>
    </div>

    <div class="col-lg-7">
      <div class="card-ib p-4 h-100">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="text-white m-0">
            <i class="fa-solid fa-list-ul text-info me-2"></i>සාමාජිකයින් ලැයිස්තුව
          </h5>
          <span class="badge bg-secondary fs-6" x-text="'Total: ' + ibFilteredMembers.length">Total: 0</span>
        </div>
        <div class="table-responsive table-container" style="max-height: 480px; overflow-y: auto;">
          <table class="table table-dark-custom">
            <thead>
              <tr>
                <th>SX ID</th>
                <th>Name</th>
                <th>Account ID</th>
                <th>Partner</th>
              </tr>
            </thead>
            <tbody>
              @forelse($ibMembers as $member)
                <tr>
                  <td><span class="badge bg-primary font-mono">{{ $member->sx_id ?? $member->id }}</span></td>
                  <td class="fw-bold">{{ $member->name }}</td>
                  <td class="font-mono text-info">{{ $member->account_id }}</td>
                  <td><span class="badge bg-secondary">{{ $member->partner->name ?? $member->partner_name ?? '—' }}</span></td>
                </tr>
              @empty
                <tr><td colspan="4" class="text-center text-muted py-4">Google Sheet එකෙන් දත්ත Load වෙමින් පවතී...</td></tr>
              @endforelse
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div class="toast-custom" id="toast" :class="{ 'show': toastVisible, 'error': toastType === 'error' }" x-text="toastMsg"></div>
</div>

<script>
function ibPartnerApp() {
  return {
    ibGsUrl: '',
    ibSyncStatus: 'Connecting...',
    ibMembersData: @json($ibMembersJson),
    ibFilteredMembers: @json($ibMembersJson),
    ibSearchQuery: '',
    ibSearchResultHtml: '',
    ibStatusHtml: '',
    ibSaving: false,
    ibForm: {
      name: '',
      broker: 'XM',
      accountId: '',
      nic: '',
      whatsapp: '',
      telegram: '',
      partner: '{{ $ibPartners->first()->name ?? '' }}'
    },

    toastVisible: false,
    toastMsg: '',
    toastType: 'success',

    init() {
      this.loadSettings();
    },

    loadSettings() {
      this.ibGsUrl = localStorage.getItem('sx_ib_gs_url') || '';
    },

    showToast(msg, type = 'success') {
      this.toastMsg = msg;
      this.toastType = type;
      this.toastVisible = true;
      setTimeout(() => { this.toastVisible = false; }, 2500);
    },

    async loadIbData() {
      const url = this.ibGsUrl.trim();
      if (!url) {
        this.ibSyncStatus = 'No URL Provided';
        return;
      }
      this.ibSyncStatus = 'Connecting...';
      try {
        const resp = await fetch(url);
        const data = await resp.json();
        if (Array.isArray(data)) {
          this.ibMembersData = data;
          this.ibFilteredMembers = [...data];
          this.ibSyncStatus = '● Connected & Synced';
        } else {
          throw new Error("Invalid format");
        }
      } catch(err) {
        this.ibSyncStatus = 'Sync Failed';
      }
      localStorage.setItem('sx_ib_gs_url', this.ibGsUrl);
    },

    searchIbUser() {
      const query = this.ibSearchQuery.trim().toLowerCase();
      if (!query) { this.ibFilteredMembers = [...this.ibMembersData]; this.ibSearchResultHtml = ''; return; }

      const filtered = this.ibMembersData.filter(m => {
        return String(m.sxId || m.sx_id || '').toLowerCase().includes(query) ||
               String(m.accountId || m.account_id || m.account || '').toLowerCase().includes(query) ||
               String(m.nic || '').toLowerCase().includes(query) ||
               String(m.name || '').toLowerCase().includes(query);
      });

      this.ibFilteredMembers = filtered;

      if (filtered.length > 0) {
        const u = filtered[0];
        this.ibSearchResultHtml = `
          <div class="search-card p-3 mt-2">
            <div class="row text-sm">
              <div class="col-md-3"><span class="lbl">SX ID:</span> <span class="val-blue font-mono font-bold">${u.sxId || u.sx_id || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Name:</span> <span class="val-white font-bold">${u.name || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Account ID:</span> <span class="val-green font-mono">${u.accountId || u.account_id || u.account || '—'}</span></div>
              <div class="col-md-3"><span class="lbl">Partner:</span> <span class="val-white">${u.partner || '—'}</span></div>
            </div>
          </div>`;
      } else {
        this.ibSearchResultHtml = '<div class="alert alert-dark border-danger text-danger mt-2 py-2 text-sm">❌ කිසිදු සාමාජිකයෙකු හමු නොවීය.</div>';
      }
    },

    async saveIbMember() {
      this.ibSaving = true;
      this.ibStatusHtml = '<span class="text-warning text-sm">නව සාමාජිකයා ඇතුළත් වෙමින් පවතී...</span>';

      try {
        const resp = await fetch('{{ route("admin.ib.member.store") }}', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': '{{ csrf_token() }}',
            'Accept': 'application/json'
          },
          body: JSON.stringify({
            name: this.ibForm.name,
            broker: this.ibForm.broker,
            account_id: this.ibForm.accountId,
            nic: this.ibForm.nic,
            whatsapp: this.ibForm.whatsapp,
            telegram: this.ibForm.telegram,
            partner: this.ibForm.partner
          })
        });
        const data = await resp.json();
        if (resp.ok) {
          this.showToast('✓ Member Saved Successfully!');
          this.ibStatusHtml = '<span class="text-success text-sm">✓ සාමාජිකයා සාර්ථකව ඇතුළත් විය!</span>';
          this.ibForm = { name: '', broker: 'XM', accountId: '', nic: '', whatsapp: '', telegram: '', partner: this.ibForm.partner };
          setTimeout(() => { this.ibStatusHtml = ''; window.location.reload(); }, 1500);
        } else {
          throw new Error(data.message || 'Failed');
        }
      } catch(err) {
        this.showToast('Failed to save member', 'error');
        this.ibStatusHtml = '<span class="text-danger text-sm">❌ සාමාජිකයා ඇතුළත් කිරීමට අපොහොසත් විය.</span>';
      } finally {
        this.ibSaving = false;
      }
    },

    addIbPartner() {
      const pName = prompt("නව Partner ගේ නම ඇතුළත් කරන්න:");
      if (pName) {
        const opt = document.createElement('option');
        opt.value = pName; opt.textContent = pName; opt.selected = true;
        this.ibForm.partner = pName;
      }
    }
  };
}
</script>

</x-layouts.admin>
