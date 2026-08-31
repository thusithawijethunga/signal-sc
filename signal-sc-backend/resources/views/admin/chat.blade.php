<x-layouts.admin>
<div class="row g-4" x-data="chatApp()" x-init="init()">
  <!-- Chat Panel -->
  <div class="col-lg-8">
    <div class="card-ib p-0" style="height: 70vh;">
      <!-- Chat Header -->
      <div class="p-3 border-bottom border-secondary d-flex justify-content-between align-items-center">
        <h5 class="text-white mb-0"><i class="fa-solid fa-comments text-success me-2"></i>Real-time Chat</h5>
        <span class="badge" :class="connected ? 'bg-success' : 'bg-danger'" x-text="connected ? 'Online' : 'Offline'"></span>
      </div>

      <!-- Messages -->
      <div class="p-3 overflow-auto flex-grow-1" style="height: calc(70vh - 130px);" id="chatMessages" x-ref="chatBox">
        <template x-for="msg in messages" :key="msg.id">
          <div class="mb-3" :class="msg.user_id === currentUserId ? 'text-end' : ''">
            <div class="d-inline-block text-start" style="max-width: 75%;">
              <small class="text-secondary" x-text="msg.user_name"></small>
              <div class="rounded-3 px-3 py-2 mt-1"
                :class="msg.user_id === currentUserId ? 'bg-primary' : 'bg-dark border border-secondary'"
                style="word-wrap: break-word;">
                <span class="text-white" x-text="msg.message"></span>
              </div>
              <small class="text-secondary" x-text="formatTime(msg.timestamp)"></small>
            </div>
          </div>
        </template>
        <div x-show="messages.length === 0" class="text-center text-secondary py-5">
          <i class="fa-solid fa-comments fa-3x mb-3 opacity-25"></i>
          <p>No messages yet. Start the conversation!</p>
        </div>
      </div>

      <!-- Input -->
      <div class="p-3 border-top border-secondary">
        <form @submit.prevent="sendMessage()" class="d-flex gap-2">
          <input type="text" x-model="newMessage" class="form-control form-control-ib" placeholder="Type a message..." :disabled="!connected">
          <button type="submit" class="btn btn-primary px-4" :disabled="!connected || !newMessage.trim()">
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </form>
      </div>
    </div>
  </div>

  <!-- Online Users -->
  <div class="col-lg-4">
    <div class="card-ib p-4">
      <h6 class="text-white mb-3"><i class="fa-solid fa-users text-info me-2"></i>Connected Traders</h6>
      <template x-for="user in onlineUsers" :key="user.user_id">
        <div class="d-flex align-items-center gap-2 mb-2">
          <span class="badge bg-success rounded-circle" style="width:8px;height:8px;"></span>
          <span class="text-white" x-text="user.name || 'Trader #' + user.user_id"></span>
        </div>
      </template>
      <div x-show="onlineUsers.length === 0" class="text-secondary small">No other users online</div>
    </div>

    <div class="card-ib p-4 mt-3">
      <h6 class="text-white mb-3"><i class="fa-solid fa-broadcast-tower text-warning me-2"></i>Live Channels</h6>
      <div class="text-secondary small">
        <div class="mb-1"><span class="badge bg-success">trading:signals</span> Signal broadcasts</div>
        <div class="mb-1"><span class="badge bg-primary">trading:trades</span> Trade updates</div>
        <div class="mb-1"><span class="badge bg-info">trading:news</span> Market news</div>
        <div class="mb-1"><span class="badge bg-warning">trading:community</span> Community posts</div>
        <div class="mb-1"><span class="badge bg-danger">chat:admin</span> Admin chat</div>
      </div>
    </div>
  </div>
</div>

<script>
function chatApp() {
  return {
    connected: false,
    messages: [],
    newMessage: '',
    onlineUsers: [],
    currentUserId: {{ auth()->id() }},
    ws: null,

    async init() {
      try {
        const resp = await fetch('{{ route("admin.chat.token") }}');
        const data = await resp.json();
        this.connectWebSocket(data.token);
      } catch(e) { console.error('Token fetch failed:', e); }
    },

    connectWebSocket(token) {
      const wsUrl = 'wss://backend.signalxpress.com/connection/websocket';
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        this.ws.send(JSON.stringify({ id: 1, connect: { token } }));
      };

      this.ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.connect) {
          this.connected = true;
          this.ws.send(JSON.stringify({ id: 2, subscribe: { channel: 'chat:admin' } }));
          this.ws.send(JSON.stringify({ id: 3, subscribe: { channel: 'trading:signals' } }));
          this.ws.send(JSON.stringify({ id: 4, subscribe: { channel: 'trading:trades' } }));
          this.ws.send(JSON.stringify({ id: 5, subscribe: { channel: 'trading:community' } }));
        }

        if (data.push) {
          const pub = data.push.pub;
          if (pub.channel === 'chat:admin' && pub.data.type === 'chat_message') {
            this.messages.push(pub.data);
            this.$nextTick(() => { this.$refs.chatBox.scrollTop = this.$refs.chatBox.scrollHeight; });
          }
        }
      };

      this.ws.onclose = () => {
        this.connected = false;
        setTimeout(() => this.connectWebSocket(token), 3000);
      };

      this.ws.onerror = () => { this.connected = false; };
    },

    async sendMessage() {
      if (!this.newMessage.trim()) return;
      try {
        await fetch('{{ route("admin.chat.send") }}', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': '{{ csrf_token() }}' },
          body: JSON.stringify({ message: this.newMessage, channel: 'chat:admin' })
        });
        this.newMessage = '';
      } catch(e) { console.error('Send failed:', e); }
    },

    formatTime(ts) {
      if (!ts) return '';
      return new Date(ts).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }
  };
}
</script>
</x-layouts.admin>
