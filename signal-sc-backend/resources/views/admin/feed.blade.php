<x-layouts.admin>
<div x-data="feedApp()" x-init="init()" class="row g-0" style="height: calc(100vh - 80px);">

  <!-- Left Sidebar - Pending Queue -->
  <div class="col-lg-2 border-end border-secondary d-flex flex-column" style="overflow-y: auto; max-height: 100%;">
    <div class="p-2 border-bottom border-secondary">
      <h6 class="text-white mb-0 small"><i class="fa-solid fa-inbox text-warning me-1"></i>Inbox</h6>
    </div>
    <div class="p-2 flex-grow-1" style="overflow-y: auto;">
      <!-- Pending Posts -->
      <template x-for="post in pendingPosts" :key="'pp-'+post.id">
        <div class="border border-warning rounded p-2 mb-2 bg-dark" style="font-size:11px;">
          <div class="d-flex justify-content-between">
            <strong class="text-white" x-text="post.author_name?.substring(0,12)"></strong>
            <span class="badge bg-warning text-dark" style="font-size:8px;">POST</span>
          </div>
          <p class="text-light mt-1 mb-1" x-text="post.content?.substring(0, 60) + '...'" style="font-size:10px;"></p>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-success py-0 px-1" style="font-size:10px;" @click="approvePost(post.id)"><i class="fa-solid fa-check"></i></button>
            <button class="btn btn-sm btn-danger py-0 px-1" style="font-size:10px;" @click="rejectPost(post.id)"><i class="fa-solid fa-times"></i></button>
          </div>
        </div>
      </template>

      <!-- Pending Chats -->
      <template x-for="chat in pendingChats" :key="'pc-'+chat.id">
        <div class="border border-info rounded p-2 mb-2 bg-dark" style="font-size:11px;">
          <div class="d-flex justify-content-between">
            <strong class="text-white" x-text="chat.author_name?.substring(0,12)"></strong>
            <span class="badge bg-info" style="font-size:8px;" x-text="chat.type || 'CHAT'"></span>
          </div>
          <p class="text-light mt-1 mb-1" x-text="chat.message?.substring(0, 60)" style="font-size:10px;"></p>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-success py-0 px-1" style="font-size:10px;" @click="approveChat(chat.id)"><i class="fa-solid fa-check"></i></button>
            <button class="btn btn-sm btn-danger py-0 px-1" style="font-size:10px;" @click="rejectChat(chat.id)"><i class="fa-solid fa-times"></i></button>
          </div>
        </div>
      </template>

      <div x-show="pendingPosts.length === 0 && pendingChats.length === 0" class="text-center text-secondary py-3 small">
        <i class="fa-solid fa-check-circle fa-2x d-block text-success opacity-50 mb-1"></i>
        All clear
      </div>
    </div>
  </div>

  <!-- Center - Unified Feed + Chat -->
  <div class="col-lg-7 d-flex flex-column">
    <!-- Tabs -->
    <div class="d-flex border-bottom border-secondary bg-dark">
      <button class="btn btn-sm flex-fill py-2 rounded-0 fw-bold" :class="activeTab === 'feed' ? 'text-warning border-bottom border-warning' : 'text-secondary'" @click="activeTab = 'feed'">
        <i class="fa-solid fa-tower-broadcast me-1"></i> Community Feed
      </button>
      <button class="btn btn-sm flex-fill py-2 rounded-0 fw-bold" :class="activeTab === 'chat' ? 'text-info border-bottom border-info' : 'text-secondary'" @click="activeTab = 'chat'">
        <i class="fa-solid fa-comments me-1"></i> Live Chat
        <span x-show="pendingChats.length > 0" class="badge bg-danger ms-1" x-text="pendingChats.length" style="font-size:9px;"></span>
      </button>
    </div>

    <!-- ═══════════ COMMUNITY FEED TAB ═══════════ -->
    <div x-show="activeTab === 'feed'" class="flex-grow-1 overflow-auto p-3" style="background: linear-gradient(180deg, #0a0e0c 0%, #0d1117 100%); max-height: calc(100vh - 180px);" x-ref="feedBox">
      <template x-for="post in posts" :key="post.id">
        <div class="mb-3">
          <div class="rounded-3 overflow-hidden" :style="getStatusStyle(post.status)">
            <div x-show="post.is_pinned" class="bg-warning text-dark text-center py-1 small fw-bold"><i class="fa-solid fa-thumbtack me-1"></i> Pinned</div>
            <div class="p-3 pb-2">
              <div class="d-flex justify-content-between align-items-start">
                <div class="d-flex align-items-center gap-2">
                  <div class="rounded-circle d-flex align-items-center justify-center text-white fw-bold" :style="'background:' + getColorForName(post.author_name) + '; width:36px; height:36px; font-size:13px;'" x-text="post.author_name?.substring(0,2).toUpperCase()"></div>
                  <div>
                    <div class="d-flex align-items-center gap-1">
                      <strong class="text-white small" x-text="post.author_name"></strong>
                      <span class="badge" :class="post.author_badge === 'Admin' ? 'bg-primary' : 'bg-secondary'" style="font-size:8px;" x-text="post.author_badge || 'Trader'"></span>
                    </div>
                    <small class="text-secondary" style="font-size:10px;" x-text="timeAgo(post.created_at) + (post.pair ? ' • ' + post.pair : '')"></small>
                  </div>
                </div>
                <div class="d-flex gap-1">
                  <span class="badge" :class="{'bg-success': post.status==='approved', 'bg-warning text-dark': post.status==='pending', 'bg-danger': post.status==='rejected'}" x-text="post.status" style="font-size:8px;"></span>
                  <span x-show="post.trade_type" class="badge" :class="post.trade_type==='BUY' ? 'bg-success' : 'bg-danger'" x-text="post.trade_type" style="font-size:9px;"></span>
                </div>
              </div>
            </div>
            <div class="px-3 pb-2">
              <p class="text-light mb-2 small" style="line-height:1.5;" x-text="post.content"></p>
              <!-- Profit Card -->
              <div x-show="post.post_type === 'profit_card' && post.profit_amount > 0" class="rounded-2 p-3 mb-2" :style="'background: linear-gradient(135deg, ' + getCardBg(post.card_theme) + ')'">
                <div class="d-flex justify-content-between align-items-center">
                  <div><div class="text-white fw-bold" style="font-size:18px;" x-text="'+$' + parseFloat(post.profit_amount).toFixed(2)"></div><small class="text-white-50" x-text="'(' + post.pips_gain + ' Pips)'"></small></div>
                  <div class="text-end"><small class="text-white-50" x-text="post.pair"></small><br><span class="badge" :class="post.trade_type==='BUY' ? 'bg-success' : 'bg-danger'" x-text="post.trade_type"></span></div>
                </div>
              </div>
              <!-- Trade Idea -->
              <div x-show="post.post_type === 'trade_idea'" class="rounded-2 p-2 mb-2 bg-dark border border-info small">
                <span class="badge bg-info">Trade Idea</span>
                <span class="text-white ms-2" x-text="post.pair + ' ' + post.trade_type"></span>
                <span class="text-warning ms-2" x-text="'Entry: ' + (post.entry_price || '-')"></span>
              </div>
              <!-- Hashtags -->
              <div x-show="post.hashtags" class="mb-2">
                <template x-for="tag in (post.hashtags||'').split(' ').filter(t=>t.startsWith('#'))">
                  <span class="badge bg-primary me-1" style="font-size:9px;" x-text="tag"></span>
                </template>
              </div>
              <!-- Engagement -->
              <div class="d-flex justify-content-between align-items-center pt-2 border-top border-secondary" style="border-top-style:dashed!important;">
                <div class="d-flex gap-3 small text-secondary">
                  <span><i class="fa-solid fa-heart text-danger"></i> <span x-text="post.likes_count||0"></span></span>
                  <span><i class="fa-solid fa-fire text-warning"></i> <span x-text="post.fire_count||0"></span></span>
                  <span><i class="fa-solid fa-rocket text-info"></i> <span x-text="post.rocket_count||0"></span></span>
                  <span><i class="fa-solid fa-comment text-success"></i> <span x-text="post.comments_count||0"></span></span>
                </div>
                <div class="d-flex gap-1">
                  <button x-show="post.status==='pending'" class="btn btn-sm btn-success py-0 px-1" style="font-size:10px;" @click="approvePost(post.id)"><i class="fa-solid fa-check"></i></button>
                  <button class="btn btn-sm btn-outline-secondary py-0 px-1" style="font-size:10px;" @click="togglePin(post.id)"><i class="fa-solid fa-thumbtack" :class="post.is_pinned?'text-warning':''"></i></button>
                  <button class="btn btn-sm btn-outline-danger py-0 px-1" style="font-size:10px;" @click="deletePost(post.id)"><i class="fa-solid fa-trash"></i></button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- ═══════════ CHAT TAB ═══════════ -->
    <div x-show="activeTab === 'chat'" class="flex-grow-1 overflow-auto p-3" style="background: linear-gradient(180deg, #0a0e14 0%, #0d1117 100%); max-height: calc(100vh - 180px);" x-ref="chatBox">
      <template x-for="chat in chatMessages" :key="chat.id">
        <div class="mb-3" :class="chat.user_id === currentUserId ? 'text-end' : ''">
          <div class="d-inline-block text-start" style="max-width:75%;">
            <div class="d-flex align-items-center gap-1 mb-1" :class="chat.user_id === currentUserId ? 'justify-content-end' : ''">
              <div class="rounded-circle d-flex align-items-center justify-center text-white fw-bold" :style="'background:' + getColorForName(chat.author_name) + '; width:24px; height:24px; font-size:9px;'" x-text="chat.author_name?.substring(0,2).toUpperCase()"></div>
              <small class="text-secondary" x-text="chat.author_name"></small>
              <span x-show="chat.author_role==='admin'" class="badge bg-primary" style="font-size:7px;">ADMIN</span>
              <span x-show="chat.status==='pending'" class="badge bg-warning text-dark" style="font-size:7px;">PENDING</span>
            </div>
            <!-- Text Message -->
            <div x-show="chat.type==='text' || !chat.type" class="rounded-3 px-3 py-2" :class="chat.user_id === currentUserId ? 'bg-primary' : 'bg-dark border border-secondary'" style="word-wrap:break-word;">
              <span class="text-white small" x-text="chat.message"></span>
            </div>
            <!-- Profit Card in Chat -->
            <div x-show="chat.type==='profit_card'" class="rounded-2 p-2 border border-success" :style="'background: linear-gradient(135deg, ' + getCardBg(chat.payload?.card_theme) + ')'">
              <div class="text-white fw-bold" x-text="'+$' + parseFloat(chat.payload?.profit_amount||0).toFixed(2)"></div>
              <small class="text-white-50" x-text="chat.message"></small>
            </div>
            <!-- Trade Idea in Chat -->
            <div x-show="chat.type==='trade_idea'" class="rounded-2 p-2 border border-info bg-dark">
              <span class="badge bg-info" style="font-size:8px;">IDEA</span>
              <span class="text-white small ms-1" x-text="chat.message"></span>
            </div>
            <small class="text-secondary" style="font-size:9px;" x-text="formatTime(chat.created_at)"></small>
            <!-- Admin actions on pending -->
            <div x-show="chat.status==='pending'" class="mt-1 d-flex gap-1">
              <button class="btn btn-sm btn-success py-0 px-1" style="font-size:9px;" @click="approveChat(chat.id)"><i class="fa-solid fa-check"></i> Approve</button>
              <button class="btn btn-sm btn-danger py-0 px-1" style="font-size:9px;" @click="rejectChat(chat.id)"><i class="fa-solid fa-times"></i></button>
            </div>
          </div>
        </div>
      </template>
      <div x-show="chatMessages.length === 0" class="text-center text-secondary py-5">
        <i class="fa-solid fa-comments fa-3x mb-3 opacity-25"></i>
        <p>No messages yet</p>
      </div>
    </div>

    <!-- Input Area (shared for both tabs) -->
    <div class="p-2 border-top border-secondary bg-dark">
      <!-- Quick Actions -->
      <div class="d-flex gap-1 mb-2 flex-wrap">
        <button class="btn btn-sm btn-outline-secondary py-0" @click="showComposer('text')"><i class="fa-solid fa-comment me-1"></i>Chat</button>
        <button class="btn btn-sm btn-outline-success py-0" @click="showComposer('profit_card')"><i class="fa-solid fa-chart-line me-1"></i>Profit Card</button>
        <button class="btn btn-sm btn-outline-warning py-0" @click="showComposer('trade_idea')"><i class="fa-solid fa-lightbulb me-1"></i>Trade Idea</button>
      </div>

      <!-- Composer -->
      <div x-show="composerVisible" class="rounded p-2 mb-2 bg-dark border border-secondary" style="font-size:12px;">
        <div class="d-flex justify-content-between mb-1">
          <strong class="text-white" style="font-size:11px;" x-text="'New ' + composerType.replace('_',' ').toUpperCase()"></strong>
          <button class="btn btn-sm btn-close btn-close-white" style="font-size:8px;" @click="composerVisible=false"></button>
        </div>
        <textarea x-model="composerContent" class="form-control form-control-sm bg-dark text-white border-secondary mb-1" rows="2" placeholder="Write your message..." style="font-size:12px;"></textarea>
        <div x-show="composerType==='profit_card'" class="row g-1 mb-1">
          <div class="col-3"><input x-model="composerPair" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pair" style="font-size:11px;"></div>
          <div class="col-3"><select x-model="composerTradeType" class="form-select form-select-sm bg-dark text-white border-secondary" style="font-size:11px;"><option>BUY</option><option>SELL</option></select></div>
          <div class="col-3"><input x-model="composerProfit" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Profit $" type="number" style="font-size:11px;"></div>
          <div class="col-3"><input x-model="composerPips" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pips" type="number" style="font-size:11px;"></div>
        </div>
        <div x-show="composerType==='trade_idea'" class="row g-1 mb-1">
          <div class="col-3"><input x-model="composerPair" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Pair" style="font-size:11px;"></div>
          <div class="col-3"><select x-model="composerTradeType" class="form-select form-select-sm bg-dark text-white border-secondary" style="font-size:11px;"><option>BUY</option><option>SELL</option></select></div>
          <div class="col-3"><input x-model="composerEntry" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Entry" style="font-size:11px;"></div>
          <div class="col-3"><input x-model="composerSl" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="SL" style="font-size:11px;"></div>
        </div>
        <div class="d-flex justify-content-between align-items-center">
          <input x-model="composerHashtags" class="form-control form-control-sm bg-dark text-white border-secondary" style="width:50%; font-size:11px;" placeholder="#hashtags">
          <div class="d-flex gap-2 align-items-center">
            <label class="form-check small text-white" style="font-size:10px;"><input type="checkbox" x-model="composerPinned" class="form-check-input"> Pin</label>
            <button class="btn btn-sm btn-primary py-0" @click="sendComposer()" :disabled="!composerContent.trim()"><i class="fa-solid fa-paper-plane me-1"></i> Send</button>
          </div>
        </div>
      </div>

      <!-- Quick Input -->
      <div x-show="!composerVisible" class="d-flex gap-2">
        <input type="text" x-model="quickMessage" @keydown.enter="sendQuick()" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Type a message..." style="font-size:12px;">
        <button class="btn btn-sm btn-primary" @click="sendQuick()" :disabled="!quickMessage.trim()"><i class="fa-solid fa-paper-plane"></i></button>
      </div>
    </div>
  </div>

  <!-- Right Sidebar - Stats -->
  <div class="col-lg-3 border-start border-secondary d-flex flex-column" style="overflow-y: auto; max-height: 100%;">
    <div class="p-2 border-bottom border-secondary">
      <h6 class="text-white mb-0 small"><i class="fa-solid fa-chart-pie text-primary me-1"></i>Stats</h6>
    </div>
    <div class="p-2">
      <div class="row g-1 mb-2">
        <div class="col-4"><div class="bg-dark rounded p-1 text-center border border-secondary"><div class="text-success fw-bold small" x-text="posts.filter(p=>p.status==='approved').length"></div><small class="text-secondary" style="font-size:9px;">Published</small></div></div>
        <div class="col-4"><div class="bg-dark rounded p-1 text-center border border-secondary"><div class="text-warning fw-bold small" x-text="pendingPosts.length"></div><small class="text-secondary" style="font-size:9px;">Pending</small></div></div>
        <div class="col-4"><div class="bg-dark rounded p-1 text-center border border-secondary"><div class="text-info fw-bold small" x-text="chatMessages.length"></div><small class="text-secondary" style="font-size:9px;">Chats</small></div></div>
      </div>

      <h6 class="text-white small fw-bold mb-1" style="font-size:11px;">Recent Trades</h6>
      <template x-for="trade in recentTrades" :key="trade.id">
        <div class="bg-dark rounded p-1 mb-1 border border-secondary small" style="font-size:10px;">
          <div class="d-flex justify-content-between"><span class="text-white" x-text="trade.pair + ' ' + trade.direction"></span><span class="badge" :class="trade.result==='WIN'?'bg-success':'bg-danger'" x-text="trade.result" style="font-size:8px;"></span></div>
          <small class="text-secondary" x-text="trade.pips + ' pips'"></small>
        </div>
      </template>

      <!-- Community Settings -->
      <h6 class="text-white small fw-bold mb-1 mt-3" style="font-size:11px;"><i class="fa-solid fa-cog text-warning me-1"></i>Settings</h6>
      <form action="{{ route('admin.feed.settings') }}" method="POST" class="bg-dark rounded p-2 border border-secondary">
        @csrf
        <div class="form-check mb-1">
          <input type="checkbox" name="require_post_approval" value="1" class="form-check-input" id="postApproval" {{ $settings['require_post_approval'] ? 'checked' : '' }}>
          <label class="form-check-label text-white" for="postApproval" style="font-size:10px;">Require Post Approval</label>
        </div>
        <div class="form-check mb-2">
          <input type="checkbox" name="require_comment_approval" value="1" class="form-check-input" id="commentApproval" {{ $settings['require_comment_approval'] ? 'checked' : '' }}>
          <label class="form-check-label text-white" for="commentApproval" style="font-size:10px;">Require Comment Approval</label>
        </div>
        <button type="submit" class="btn btn-sm btn-outline-warning w-100 py-0" style="font-size:10px;">Save Settings</button>
      </form>

      <h6 class="text-white small fw-bold mb-1 mt-3" style="font-size:11px;">Channels</h6>
      <div style="font-size:10px;">
        <div class="mb-1"><span class="badge bg-success" style="font-size:7px;">●</span> <span class="text-secondary">trading:signals</span></div>
        <div class="mb-1"><span class="badge bg-primary" style="font-size:7px;">●</span> <span class="text-secondary">trading:trades</span></div>
        <div class="mb-1"><span class="badge bg-info" style="font-size:7px;">●</span> <span class="text-secondary">chat:community</span></div>
      </div>
    </div>
  </div>
</div>

<script>
function feedApp() {
  return {
    activeTab: 'feed',
    posts: @json($posts),
    pendingPosts: [],
    pendingChats: [],
    chatMessages: @json($chatMessages),
    recentTrades: @json($recentTrades),
    recentSignals: @json($recentSignals),
    currentUserId: {{ auth()->id() }},
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
      this.connectWs();
      this.scrollChat();
    },

    connectWs() {
      fetch('/admin/chat/token').then(r => r.json()).then(data => {
        const ws = new WebSocket('wss://backend.signalxpress.com/connection/websocket');
        ws.onopen = () => ws.send(JSON.stringify({ id: 1, connect: { token: data.token } }));
        ws.onmessage = (e) => {
          const d = JSON.parse(e.data);
          if (d.connect?.result) {
            ws.send(JSON.stringify({ id: 2, subscribe: { channel: 'trading:community' } }));
            ws.send(JSON.stringify({ id: 3, subscribe: { channel: 'chat:community' } }));
          }
          if (d.push) {
            const pub = d.push.pub;
            if (pub.channel === 'chat:community' && pub.data?.type === 'chat_message') {
              if (!this.chatMessages.find(c => c.id === pub.data.id)) {
                this.chatMessages.push({ ...pub.data, status: 'approved' });
                this.$nextTick(() => this.scrollChat());
              }
            }
            if (pub.channel === 'trading:community' && pub.data?.type === 'community_post') {
              if (!this.posts.find(p => p.id === pub.data.id)) {
                this.posts.unshift({ ...pub.data, status: 'approved', likes_count: 0, fire_count: 0, rocket_count: 0, comments_count: 0 });
              }
            }
          }
        };
        ws.onclose = () => setTimeout(() => this.connectWs(), 3000);
      }).catch(() => {});
    },

    showComposer(type) { this.composerVisible = true; this.composerType = type; this.composerContent = ''; },

    async sendComposer() {
      if (this.activeTab === 'chat') {
        // Send as chat message
        await fetch('/admin/feed/chat', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify({
            message: this.composerContent, type: this.composerType,
            pair: this.composerPair, trade_type: this.composerTradeType,
            profit_amount: this.composerProfit, pips_gain: this.composerPips,
            entry_price: this.composerEntry, exit_price: this.composerSl,
          })
        });
      } else {
        // Send as community post
        await fetch('/admin/feed/post', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify({
            content: this.composerContent, post_type: this.composerType,
            hashtags: this.composerHashtags, pair: this.composerPair,
            trade_type: this.composerTradeType, profit_amount: this.composerProfit,
            pips_gain: this.composerPips, entry_price: this.composerEntry,
            exit_price: this.composerSl, is_pinned: this.composerPinned,
            is_verified_trade: true, broker_name: 'Signal Xpress',
          })
        });
      }
      this.composerVisible = false;
      this.composerContent = '';
      setTimeout(() => location.reload(), 300);
    },

    async sendQuick() {
      if (!this.quickMessage.trim()) return;
      if (this.activeTab === 'chat') {
        await fetch('/admin/feed/chat', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify({ message: this.quickMessage, type: 'text' })
        });
      } else {
        await fetch('/admin/feed/post', {
          method: 'POST', headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify({ content: this.quickMessage, post_type: 'text', hashtags: '#SignalXpress' })
        });
      }
      this.quickMessage = '';
      setTimeout(() => location.reload(), 300);
    },

    async approvePost(id) { await fetch('/admin/feed/posts/'+id+'/approve', { method:'POST', headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'} }); location.reload(); },
    async rejectPost(id) { await fetch('/admin/feed/posts/'+id+'/reject', { method:'POST', headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'} }); location.reload(); },
    async deletePost(id) { if(!confirm('Delete?'))return; await fetch('/admin/feed/posts/'+id, { method:'DELETE', headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'} }); this.posts=this.posts.filter(p=>p.id!==id); },
    async togglePin(id) { const r=await fetch('/admin/feed/posts/'+id+'/pin',{method:'POST',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); const d=await r.json(); const p=this.posts.find(p=>p.id===id); if(p)p.is_pinned=d.pinned; },
    async approveComment(id) { await fetch('/admin/feed/comments/'+id+'/approve',{method:'POST',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); },
    async rejectComment(id) { await fetch('/admin/feed/comments/'+id+'/reject',{method:'POST',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); },
    async approveChat(id) { await fetch('/admin/feed/chat/'+id+'/approve',{method:'POST',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); location.reload(); },
    async rejectChat(id) { await fetch('/admin/feed/chat/'+id+'/reject',{method:'POST',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); this.pendingChats=this.pendingChats.filter(c=>c.id!==id); },
    async deleteChat(id) { if(!confirm('Delete?'))return; await fetch('/admin/feed/chat/'+id,{method:'DELETE',headers:{'X-CSRF-TOKEN':'{{ csrf_token() }}'}}); this.chatMessages=this.chatMessages.filter(c=>c.id!==id); },

    getStatusStyle(s) { return s==='approved'?'border:1px solid #2a3a32;background:#121815;':s==='pending'?'border:1px solid #d4a04c;background:#1a1508;':'border:1px solid #3b1111;background:#1a0d0d;'; },
    getColorForName(n) { const c=['#F59E0B','#10B981','#38BDF8','#8B5CF6','#EF4444','#D946EF','#06B6D4','#F97316']; let h=0;for(let i=0;i<(n||'').length;i++)h=n.charCodeAt(i)+((h<<5)-h);return c[Math.abs(h)%c.length]; },
    getCardBg(t) { const b={'EMERALD_NEON':'#065F46,#064E3B','GOLD_LUXURY':'#78350F,#451A03','CYBER_SKY':'#0C4A6E,#164E63','DEEP_VIOLET':'#4C1D95,#3B0764'}; return b[t]||'#1F2937,#111827'; },
    timeAgo(d) { if(!d)return'';const s=Math.floor((new Date()-new Date(d))/1000);if(s<60)return'now';if(s<3600)return Math.floor(s/60)+'m';if(s<86400)return Math.floor(s/3600)+'h';return Math.floor(s/86400)+'d'; },
    formatTime(ts) { if(!ts)return'';return new Date(ts).toLocaleTimeString('en-US',{hour:'2-digit',minute:'2-digit'}); },
    scrollChat() { this.$nextTick(() => { if(this.$refs.chatBox) this.$refs.chatBox.scrollTop = this.$refs.chatBox.scrollHeight; }); },
  };
}
</script>
</x-layouts.admin>
