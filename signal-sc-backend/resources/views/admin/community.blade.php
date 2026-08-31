<x-layouts.admin>
<div class="row g-4">
  <!-- Stats -->
  <div class="col-12">
    <div class="d-flex gap-3 flex-wrap">
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-warning fs-2 fw-bold">{{ $pendingCount }}</div>
        <small class="text-secondary">Pending Posts</small>
      </div>
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-info fs-2 fw-bold">{{ $pendingComments }}</div>
        <small class="text-secondary">Pending Comments</small>
      </div>
      <div class="card-ib px-4 py-3 flex-fill text-center">
        <div class="text-success fs-2 fw-bold">{{ \App\Models\CommunityPost::where('status','approved')->count() }}</div>
        <small class="text-secondary">Published Posts</small>
      </div>
    </div>
  </div>

  <!-- Settings -->
  <div class="col-12">
    <div class="card-ib p-4">
      <h5 class="text-white mb-3"><i class="fa-solid fa-cog text-primary me-2"></i>Community Settings</h5>
      <form action="{{ route('admin.community.settings') }}" method="POST" class="d-flex gap-4 align-items-center">
        @csrf
        <label class="form-check text-white">
          <input type="checkbox" name="require_post_approval" value="1" class="form-check-input" {{ $settings['require_post_approval'] ? 'checked' : '' }}>
          Require Post Approval
        </label>
        <label class="form-check text-white">
          <input type="checkbox" name="require_comment_approval" value="1" class="form-check-input" {{ $settings['require_comment_approval'] ? 'checked' : '' }}>
          Require Comment Approval
        </label>
        <button class="btn btn-sm btn-primary">Save Settings</button>
      </form>
    </div>
  </div>

  <!-- Post as Admin -->
  <div class="col-lg-5">
    <div class="card-ib p-4 h-100">
      <h5 class="text-white mb-3"><i class="fa-solid fa-pen-to-square text-success me-2"></i>Post to Community</h5>
      <form action="{{ route('admin.community.post') }}" method="POST">
        @csrf
        <div class="mb-2">
          <select name="post_type" class="form-select form-control-ib">
            <option value="text">Text Post</option>
            <option value="screenshot">Screenshot</option>
            <option value="signal_card">Signal Card</option>
          </select>
        </div>
        <div class="mb-2">
          <textarea name="content" class="form-control form-control-ib" rows="4" placeholder="Write your post..." required></textarea>
        </div>
        <div class="row mb-2">
          <div class="col"><input name="pair" class="form-control form-control-ib" placeholder="Pair (XAU/USD)"></div>
          <div class="col"><input name="hashtags" class="form-control form-control-ib" placeholder="#hashtags"></div>
        </div>
        <div class="form-check mb-2">
          <input type="checkbox" name="is_pinned" value="1" class="form-check-input">
          <label class="form-check-label text-white">Pin this post</label>
        </div>
        <button class="btn btn-success w-100"><i class="fa-solid fa-paper-plane me-1"></i> Publish Post</button>
      </form>
    </div>
  </div>

  <!-- Pending Posts -->
  <div class="col-lg-7">
    <div class="card-ib p-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="text-white mb-0"><i class="fa-solid fa-clock text-warning me-2"></i>Pending Posts ({{ $pendingCount }})</h5>
        @if($pendingCount > 0)
        <form action="{{ route('admin.community.approve-all') }}" method="POST" class="d-inline">
          @csrf
          <button class="btn btn-sm btn-success"><i class="fa-solid fa-check-double me-1"></i>Approve All</button>
        </form>
        @endif
      </div>

      @forelse($posts->where('status', 'pending') as $post)
      <div class="border border-secondary rounded p-3 mb-3">
        <div class="d-flex justify-content-between">
          <div>
            <strong class="text-white">{{ $post->author_name }}</strong>
            <span class="badge bg-secondary ms-2">{{ $post->post_type }}</span>
            @if($post->is_pinned)<span class="badge bg-warning text-dark ms-1">Pinned</span>@endif
          </div>
          <small class="text-secondary">{{ $post->created_at->diffForHumans() }}</small>
        </div>
        <p class="text-light mt-2 mb-2">{{ Str::limit($post->content, 200) }}</p>
        @if($post->pair)<small class="text-info">{{ $post->pair }} {{ $post->trade_type }}</small>@endif
        <div class="d-flex gap-2 mt-2">
          <form action="{{ route('admin.community.approve', $post) }}" method="POST" class="d-inline">
            @csrf
            <button class="btn btn-sm btn-success"><i class="fa-solid fa-check me-1"></i>Approve</button>
          </form>
          <form action="{{ route('admin.community.reject', $post) }}" method="POST" class="d-inline">
            @csrf
            <button class="btn btn-sm btn-danger"><i class="fa-solid fa-times me-1"></i>Reject</button>
          </form>
          <form action="{{ route('admin.community.delete', $post) }}" method="POST" class="d-inline">
            @csrf @method('DELETE')
            <button class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete?')"><i class="fa-solid fa-trash me-1"></i>Delete</button>
          </form>
        </div>
      </div>
      @empty
      <div class="text-center text-secondary py-4">No pending posts</div>
      @endforelse

      <!-- Published Posts -->
      <h6 class="text-white mt-4 mb-3"><i class="fa-solid fa-check-circle text-success me-2"></i>Published Posts</h6>
      @forelse($posts->where('status', 'approved') as $post)
      <div class="border border-secondary rounded p-3 mb-2">
        <div class="d-flex justify-content-between">
          <div>
            <strong class="text-white">{{ $post->author_name }}</strong>
            <span class="badge bg-success ms-2">Approved</span>
          </div>
          <form action="{{ route('admin.community.delete', $post) }}" method="POST" class="d-inline">
            @csrf @method('DELETE')
            <button class="btn btn-sm btn-outline-danger" onclick="return confirm('Delete?')"><i class="fa-solid fa-trash"></i></button>
          </form>
        </div>
        <p class="text-secondary mt-1 mb-0 small">{{ Str::limit($post->content, 100) }}</p>
      </div>
      @empty
      <div class="text-secondary small">No published posts</div>
      @endforelse
    </div>
  </div>
</div>

@if(session('success'))
<div class="position-fixed bottom-0 end-0 p-3" style="z-index:9999">
  <div class="toast show align-items-center text-bg-success border-0"><div class="d-flex"><div class="toast-body">{{ session('success') }}</div></div></div>
</div>
@endif
</x-layouts.admin>
