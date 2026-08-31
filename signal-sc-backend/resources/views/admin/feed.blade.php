<x-layouts.admin>
<div x-data="feedApp()" x-init="init()" class="row g-0" style="height: calc(100vh - 80px);">

  <!-- Left Sidebar - Pending Queue -->
  <div class="col-lg-3 border-end border-secondary d-flex flex-column" style="overflow-y: auto;">
    <div class="p-3 border-bottom border-secondary">
      <h6 class="text-white mb-0"><i class="fa-solid fa-inbox text-warning me-2"></i>Inbox</h6>
      <small class="text-secondary">Pending approvals</small>
    </div>
    <div class="p-2 flex-grow-1" style="overflow-y: auto;">
      <!-- Pending Posts -->
      <template x-for="post in pendingPosts" :key="'pending-'+post.id">
        <div class="border border-warning rounded p-2 mb-2 bg-dark">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <strong class="text-white small" x-text="post.author_name"></strong>
              <span class="badge bg-warning text-dark ms-1" style="font-size:9px" x-text="post.post_type"></span>
            </div>
          </div>
          <p class="text-light small mt-1 mb-2" x-text="post.content?.substring(0, 100) + '...'"></p>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-success py-0 px-2" @click="approvePost(post.id)">
              <i class="fa-solid fa-check"></i>
            </button>
            <button class="btn btn-sm btn-danger py-0 px-2" @click="rejectPost(post.id)">
              <i class="fa-solid fa-times"></i>
            </button>
          </div>
        </div>
      </template>
      <div x-show="pendingPosts.length === 0" class="text-center text-secondary py-4 small">
        <i class="fa-solid fa-check-circle fa-2x mb-2 d-block text-success opacity-50"></i>
        All caught up
      </div>

      <!-- Pending Comments -->
      <div class="border-top border-secondary mt-2 pt-2" x-show="pendingComments.length > 0">
        <small class="text-warning fw-bold">Comments</small>
        <template x-for="c in pendingComments" :key="'pc-'+c.id">
          <div class="border border-info rounded p-2 mb-1 bg-dark">
            <small class="text-white" x-text="c.author_name + ': ' + c.content?.substring(0, 60)"></small>
            <div class="d-flex gap-1 mt-1">
              <button class="btn btn-sm btn-success py-0 px-2" @click="approveComment(c.id)"><i class="fa-solid fa-check"></i></button>
              <button class="btn btn-sm btn-danger py-0 px-2" @click="rejectComment(c.id)"><i class="fa-solid fa-times"></i></button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>

  <!-- Center - Main Feed -->
  <div class="col-lg-6 d-flex flex-column">
    <!-- Feed Header -->
    <div class="p-3 border-bottom border-secondary d-flex justify-content-between align-items-center bg-dark">
      <div>
        <h5 class="text-white mb-0"><i class="fa-solid fa-tower-broadcast text-success me-2"></i>Community Feed</h5>
        <small class="text-secondary" x-text="posts.length + ' posts • ' + onlineCount + ' online'"></small>
      </div>
      <div class="d-flex gap-2">
        <span class="badge" :class="wsConnected ? 'bg-success' : 'bg-danger'" x-text="wsConnected ? 'Live' : 'Offline'"></span>
      </div>
    </div>

    <!-- Messages Feed -->
    <div class="flex-grow-1 overflow-auto p-3 bg-black" style="background: linear-gradient(180deg, #0a0e0c 0%, #0d1117 100%);" x-ref="feedBox">
      <template x-for="post in posts" :key="post.id">
        <div class="mb-4" :id="'post-'+post.id">
          <!-- Post Card -->
          <div class="rounded-3 overflow-hidden" :style="getStatusStyle(post.status)">
            <!-- Pinned Badge -->
            <div x-show="post.is_pinned" class="bg-warning text-dark text-center py-1 small fw-bold">
              <i class="fa-solid fa-thumbtack me-1"></i> Pinned
            </div>

            <!-- Post Header -->
            <div class="p-3 pb-2">
              <div class="d-flex justify-content-between align-items-start">
                <div class="d-flex align-items-center gap-2">
                  <div class="rounded-circle d-flex align-items-center justify-center text-white fw-bold"
                    :style="'background:' + (post.author_badge === 'Admin' ? 'linear-gradient(135deg, #38BDF8, #0284C7)' : getColorForName(post.author_name)) + '; width:40px; height:40px; font-size:14px;'"
                    x-text="post.author_name?.substring(0,2).toUpperCase()">
                  </div>
                  <div>
                    <div class="d-flex align-items-center gap-2">
                      <strong class="text-white" x-text="post.author_name"></strong>
                      <span class="badge" :class="post.author_badge === 'Admin' ? 'bg-primary' : 'bg-secondary'" style="font-size:9px" x-text="post.author_badge || 'Trader'"></span>
                    </div>
                    <small class="text-secondary" x-text="timeAgo(post.created_at) + ' • ' + (post.pair || '')"></small>
                  </div>
                </div>
                <div class="d-flex gap-1">
                  <!-- Status Badge -->
                  <span class="badge" :class="{
                    'bg-success': post.status === 'approved',
                    'bg-warning text-dark': post.status === 'pending',
                    'bg-danger': post.status === 'rejected'
                  }" x-text="post.status" style="font-size:9px;"></span>
                  <!-- Trade Type Badge -->
                  <span x-show="post.trade_type" class="badge" :class="post.trade_type === 'BUY' ? 'bg-success' : 'bg-danger'" x-text="post.trade_type" style="font-size:10px;"></span>
                </div>
              </div>
            </div>

            <!-- Content -->
            <div class="px-3 pb-2">
              <p class="text-light mb-2" style="line-height: 1.5;" x-text="post.content"></p>

              <!-- Profit Card -->
              <div x-show="post.post_type === 'profit_card' && post.profit_amount > 0" class="rounded-2 p-3 mb-2"
                :style="'background: linear-gradient(135deg, ' + getCardBg(post.card_theme) + ')'">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white fw-bold" style="font-size: 20px;" x-text="'+$' + parseFloat(post.profit_amount).toFixed(2)"></div>
                    <small class="text-white-50" x-text="'(' + post.pips_gain + ' Pips)'"></small>
                  </div>
                  <div class="text-end">
                    <div class="text-white-50 small" x-text="post.pair"></div>
                    <span class="badge" :class="post.trade_type === 'BUY' ? 'bg-success' : 'bg-danger'" x-text="post.trade_type"></span>
                  </div>
                </div>
              </div>

              <!-- Trade Idea Card -->
              <div x-show="post.post_type === 'trade_idea'" class="rounded-2 p-3 mb-2 bg-dark border border-info">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="badge bg-info">Trade Idea</span>
                  <small class="text-white-50" x-text="post.pair"></small>
                </div>
                <div class="row small text-white">
                  <div class="col-6">Entry: <span class="text-warning" x-text="post.entry_price || '-'"></span></div>
                  <div class="col-6">SL: <span class="text-danger" x-text="post.exit_price || '-'"></span></div>
                </div>
              </div>

              <!-- Hashtags -->
              <div x-show="post.hashtags" class="mb-2">
                <template x-for="tag in (post.hashtags || '').split(' ').filter(t => t.startsWith('#'))">
                  <span class="badge bg-primary me-1" style="font-size:10px;" x-text="tag"></span>
                </template>
              </div>

              <!-- Engagement Bar -->
              <div class="d-flex justify-content-between align-items-center pt-2 border-top border-secondary" style="border-top-style: dashed !important;">
                <div class="d-flex gap-3">
                  <span class="text-secondary small"><i class="fa-solid fa-heart text-danger"></i> <span x-text="post.likes_count || 0"></span></span>
                  <span class="text-secondary small"><i class="fa-solid fa-fire text-warning"></i> <span x-text="post.fire_count || 0"></span></span>
                  <span class="text-secondary small"><i class="fa-solid fa-rocket text-info"></i> <span x-text="post.rocket_count || 0"></span></span>
                  <span class="text-secondary small"><i class="fa-solid fa-comment text-success"></i> <span x-text="post.comments_count || 0"></span></span>
                </div>
                <div class="d-flex gap-1">
                  <button x-show="post.status === 'pending'" class="btn btn-sm btn-success py-0" @click="approvePost(post.id)"><i class="fa-solid fa-check"></i></button>
                  <button class="btn btn-sm btn-outline-secondary py-0" @click="togglePin(post.id)">
                    <i class="fa-solid fa-thumbtack" :class="post.is_pinned ? 'text-warning' : ''"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger py-0" @click="deletePost(post.id)"><i class="fa-solid fa-trash"></i></button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Message Input -->
    <div class="p-3 border-top border-secondary bg-dark">
      <!-- Quick Actions -->
      <div class="d-flex gap-2 mb-2 flex-wrap">
        <button class="btn btn-sm btn-outline-info" @click="showComposer('text')">
          <i class="fa-solid fa-comment me-1"></i> Text
        </button>
        <button class="btn btn-sm btn-outline-success" @click="showComposer('profit_card')">
          <i class="fa-solid fa-chart-line me-1"></i> Profit Card
        </button>
        <button class="btn btn-sm btn-outline-warning" @click="showComposer('trade_idea')">
          <i class="fa-solid fa-lightbulb me-1"></i> Trade Idea
        </button>
        <button class="btn btn-sm btn-outline-primary" @click="showComposer('screenshot')">
          <i class="fa-solid fa-camera me-1"></i> Screenshot
        </button>
      </div>

      <!-- Composer Form -->
      <div x-show="composerVisible" class="rounded-2 p-3 mb-2 bg-dark border border-secondary">
        <div class="d-flex justify-content-between mb-2">
          <strong class="text-white small" x-text="'New ' + composerType.replace('_', ' ').toUpperCase()"></strong>
          <button class="btn btn-sm btn-close btn-close-white" @click="composerVisible = false"></button>
        </div>
        <textarea x-model="composerContent" class="form-control form-control-sm bg-dark text-white border-secondary mb-2" rows="3" placeholder="Write your message..."></textarea>

        <!-- Profit Card Fields -->
        <div x-show="composerType === 'profit_card'" class="row g-2 mb-2">
          <div class="col-3"><input x-model="composerPair" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pair"></div>
          <div class="col-3"><select x-model="composerTradeType" class="form-select form-select-sm bg-dark text-white border-secondary"><option>BUY</option><option>SELL</option></select></div>
          <div class="col-3"><input x-model="composerProfit" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Profit $" type="number"></div>
          <div class="col-3"><input x-model="composerPips" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pips" type="number"></div>
        </div>

        <!-- Trade Idea Fields -->
        <div x-show="composerType === 'trade_idea'" class="row g-2 mb-2">
          <div class="col-3"><input x-model="composerPair" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pair"></div>
          <div class="col-3"><select x-model="composerTradeType" class="form-select form-select-sm bg-dark text-white border-secondary"><option>BUY</option><option>SELL</option></select></div>
          <div class="col-3"><input x-model="composerEntry" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Entry"></div>
          <div class="col-3"><input x-model="composerSl" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="SL"></div>
        </div>

        <div class="d-flex justify-content-between align-items-center">
          <input x-model="composerHashtags" class="form-control form-control-sm bg-dark text-white border-secondary" style="width:60%;" placeholder="#hashtags">
          <div class="d-flex gap-2">
            <label class="form-check small text-white"><input type="checkbox" x-model="composerPinned" class="form-check-input"> Pin</label>
            <button class="btn btn-sm btn-primary" @click="sendPost()" :disabled="!composerContent.trim()">
              <i class="fa-solid fa-paper-plane me-1"></i> Publish
            </button>
          </div>
        </div>
      </div>

      <!-- Quick Text Input -->
      <div x-show="!composerVisible" class="d-flex gap-2">
        <input type="text" x-model="quickMessage" @keydown.enter="sendQuick()" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Type a message or click an action above...">
        <button class="btn btn-sm btn-primary" @click="sendQuick()" :disabled="!quickMessage.trim()"><i class="fa-solid fa-paper-plane"></i></button>
      </div>
    </div>
  </div>

  <!-- Right Sidebar - Stats & Info -->
  <div class="col-lg-3 border-start border-secondary d-flex flex-column" style="overflow-y: auto;">
    <div class="p-3 border-bottom border-secondary">
      <h6 class="text-white mb-0"><i class="fa-solid fa-chart-pie text-primary me-2"></i>Feed Stats</h6>
    </div>
    <div class="p-3">
      <div class="row g-2 mb-3">
        <div class="col-6">
          <div class="bg-dark rounded p-2 text-center border border-secondary">
            <div class="text-success fw-bold" x-text="posts.filter(p => p.status === 'approved').length"></div>
            <small class="text-secondary">Published</small>
          </div>
        </div>
        <div class="col-6">
          <div class="bg-dark rounded p-2 text-center border border-secondary">
            <div class="text-warning fw-bold" x-text="pendingPosts.length"></div>
            <small class="text-secondary">Pending</small>
          </div>
        </div>
        <div class="col-6">
          <div class="bg-dark rounded p-2 text-center border border-secondary">
            <div class="text-info fw-bold" x-text="posts.filter(p => p.post_type === 'profit_card').length"></div>
            <small class="text-secondary">Profit Cards</small>
          </div>
        </div>
        <div class="col-6">
          <div class="bg-dark rounded p-2 text-center border border-secondary">
            <div class="text-primary fw-bold" x-text="posts.filter(p => p.post_type === 'trade_idea').length"></div>
            <small class="text-secondary">Trade Ideas</small>
          </div>
        </div>
      </div>

      <h6 class="text-white small fw-bold mb-2">Recent Trades</h6>
      <template x-for="trade in recentTrades" :key="trade.id">
        <div class="bg-dark rounded p-2 mb-1 border border-secondary small">
          <div class="d-flex justify-content-between">
            <span class="text-white" x-text="trade.pair + ' ' + trade.direction"></span>
            <span class="badge" :class="trade.result === 'WIN' ? 'bg-success' : 'bg-danger'" x-text="trade.result" style="font-size:9px;"></span>
          </div>
          <small class="text-secondary" x-text="trade.date + ' • ' + trade.pips + ' pips'"></small>
        </div>
      </template>

      <h6 class="text-white small fw-bold mb-2 mt-3">Live Channels</h6>
      <div class="small">
        <div class="mb-1"><span class="badge bg-success" style="font-size:8px;">●</span> <span class="text-secondary">trading:signals</span></div>
        <div class="mb-1"><span class="badge bg-primary" style="font-size:8px;">●</span> <span class="text-secondary">trading:trades</span></div>
        <div class="mb-1"><span class="badge bg-info" style="font-size:8px;">●</span> <span class="text-secondary">trading:community</span></div>
      </div>
    </div>
  </div>
</div>

<script>
function feedApp() {
  return {
    posts: @json($posts),
    pendingPosts: [],
    pendingComments: [],
    recentTrades: @json($recentTrades),
    recentSignals: @json($recentSignals),
    wsConnected: false,
    onlineCount: 0,
    composerVisible: false,
    composerType: 'text',
    composerContent: '',
    composerPair: 'XAU/USD',
    composerTradeType: 'BUY',
    composerProfit: '',
    composerPips: '',
    composerEntry: '',
    composerSl: '',
    composerHashtags: '#SignalXpress',
    composerPinned: false,
    quickMessage: '',

    init() {
      this.pendingPosts = this.posts.filter(p => p.status === 'pending');
      this.pendingComments = @json($pendingComments ?? []);
      this.connectWs();
    },

    connectWs() {
      fetch('/admin/chat/token').then(r => r.json()).then(data => {
        const ws = new WebSocket('wss://backend.signalxpress.com/connection/websocket');
        ws.onopen = () => ws.send(JSON.stringify({ id: 1, connect: { token: data.token } }));
        ws.onmessage = (e) => {
          const d = JSON.parse(e.data);
          if (d.connect?.result) {
            this.wsConnected = true;
            ws.send(JSON.stringify({ id: 2, subscribe: { channel: 'trading:community' } }));
            ws.send(JSON.stringify({ id: 3, subscribe: { channel: 'trading:signals' } }));
          }
          if (d.push?.pub?.data?.type === 'community_post') {
            const newPost = d.push.pub.data;
            if (!this.posts.find(p => p.id === newPost.id)) {
              this.posts.unshift({ ...newPost, status: 'approved', likes_count: 0, fire_count: 0, rocket_count: 0, comments_count: 0 });
            }
          }
        };
        ws.onclose = () => { this.wsConnected = false; setTimeout(() => this.connectWs(), 3000); };
      }).catch(() => {});
    },

    showComposer(type) { this.composerVisible = true; this.composerType = type; this.composerContent = ''; },

    async sendPost() {
      const data = {
        content: this.composerContent,
        post_type: this.composerType,
        hashtags: this.composerHashtags,
        pair: this.composerPair,
        trade_type: this.composerTradeType,
        profit_amount: this.composerProfit,
        pips_gain: this.composerPips,
        entry_price: this.composerEntry,
        exit_price: this.composerSl,
        is_pinned: this.composerPinned,
        is_verified_trade: true,
        broker_name: 'Signal Xpress',
      };
      try {
        const resp = await fetch('/admin/feed/post', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify(data)
        });
        const result = await resp.json();
        if (result.ok) {
          this.composerVisible = false;
          this.composerContent = '';
          this.refreshFeed();
        }
      } catch(e) { console.error(e); }
    },

    async sendQuick() {
      if (!this.quickMessage.trim()) return;
      await fetch('/admin/feed/post', {
        method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
        body: JSON.stringify({ content: this.quickMessage, post_type: 'text', hashtags: '#SignalXpress' })
      });
      this.quickMessage = '';
      this.refreshFeed();
    },

    async approvePost(id) {
      await fetch('/admin/feed/posts/' + id + '/approve', { method: 'POST', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      this.refreshFeed();
    },

    async rejectPost(id) {
      await fetch('/admin/feed/posts/' + id + '/reject', { method: 'POST', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      this.refreshFeed();
    },

    async deletePost(id) {
      if (!confirm('Delete this post?')) return;
      await fetch('/admin/feed/posts/' + id, { method: 'DELETE', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      this.posts = this.posts.filter(p => p.id !== id);
    },

    async togglePin(id) {
      const resp = await fetch('/admin/feed/posts/' + id + '/pin', { method: 'POST', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      const data = await resp.json();
      const post = this.posts.find(p => p.id === id);
      if (post) post.is_pinned = data.pinned;
    },

    async approveComment(id) {
      await fetch('/admin/feed/comments/' + id + '/approve', { method: 'POST', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      this.pendingComments = this.pendingComments.filter(c => c.id !== id);
    },

    async rejectComment(id) {
      await fetch('/admin/feed/comments/' + id + '/reject', { method: 'POST', headers: { 'X-CSRF-TOKEN': '{{ csrf_token() }}' } });
      this.pendingComments = this.pendingComments.filter(c => c.id !== id);
    },

    async refreshFeed() {
      const resp = await fetch('/admin/feed');
      const html = await resp.text();
      // Simple reload for now
      location.reload();
    },

    getStatusStyle(status) {
      if (status === 'approved') return 'border: 1px solid #2a3a32; background: #121815;';
      if (status === 'pending') return 'border: 1px solid #d4a04c; background: #1a1508;';
      return 'border: 1px solid #3b1111; background: #1a0d0d;';
    },

    getColorForName(name) {
      const colors = ['#F59E0B', '#10B981', '#38BDF8', '#8B5CF6', '#EF4444', '#D946EF', '#06B6D4', '#F97316'];
      let hash = 0; for (let i = 0; i < (name||'').length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
      return colors[Math.abs(hash) % colors.length];
    },

    getCardBg(theme) {
      const themes = { 'EMERALD_NEON': '#065F46, #064E3B', 'GOLD_LUXURY': '#78350F, #451A03', 'CYBER_SKY': '#0C4A6E, #164E63', 'DEEP_VIOLET': '#4C1D95, #3B0764' };
      return themes[theme] || '#1F2937, #111827';
    },

    timeAgo(date) {
      if (!date) return '';
      const seconds = Math.floor((new Date() - new Date(date)) / 1000);
      if (seconds < 60) return 'just now';
      if (seconds < 3600) return Math.floor(seconds/60) + 'm ago';
      if (seconds < 86400) return Math.floor(seconds/3600) + 'h ago';
      return Math.floor(seconds/86400) + 'd ago';
    }
  };
}
</script>
</x-layouts.admin>
