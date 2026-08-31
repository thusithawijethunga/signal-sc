<?php

namespace App\Http\Controllers;

use App\Models\CommunityPost;
use App\Models\CommunityComment;
use App\Models\ChatMessage;
use App\Models\Trade;
use App\Models\MarketNews;
use App\Models\Signal;
use App\Events\CommunityPostCreated;
use App\Services\CentrifugoService;
use Illuminate\Http\Request;

class AdminFeedController extends Controller
{
    public function index()
    {
        $posts = CommunityPost::with('user', 'comments')
            ->orderBy('is_pinned', 'desc')
            ->orderBy('created_at', 'desc')
            ->limit(50)
            ->get();

        $pendingPosts = CommunityPost::where('status', 'pending')->count();
        $pendingComments = CommunityComment::where('status', 'pending')->count();

        // Chat messages (approved + pending for admin)
        $chatMessages = ChatMessage::with('user')
            ->orderBy('created_at', 'asc')
            ->limit(100)
            ->get();

        $pendingChats = ChatMessage::where('status', 'pending')->count();

        $recentTrades = Trade::orderBy('date', 'desc')->limit(5)->get();
        $recentSignals = Signal::orderBy('date', 'desc')->limit(5)->get();

        return view('admin.feed', compact(
            'posts', 'pendingPosts', 'pendingComments',
            'chatMessages', 'pendingChats',
            'recentTrades', 'recentSignals'
        ));
    }

    // ── Post Management ───────────────────────────────

    public function approvePost(CommunityPost $post)
    {
        $post->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);
        CommunityPostCreated::dispatch($post);
        return response()->json(['ok' => true]);
    }

    public function rejectPost(CommunityPost $post)
    {
        $post->update([
            'status' => 'rejected',
            'approved_by' => auth()->id(),
            'rejection_reason' => 'Rejected by admin',
        ]);
        return response()->json(['ok' => true]);
    }

    public function deletePost(CommunityPost $post)
    {
        $post->delete();
        return response()->json(['ok' => true]);
    }

    public function togglePin(CommunityPost $post)
    {
        $post->update(['is_pinned' => !$post->is_pinned]);
        return response()->json(['ok' => true, 'pinned' => $post->is_pinned]);
    }

    public function approveComment(CommunityComment $comment)
    {
        $comment->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);
        $comment->post->increment('comments_count');
        return response()->json(['ok' => true]);
    }

    public function rejectComment(CommunityComment $comment)
    {
        $comment->delete();
        return response()->json(['ok' => true]);
    }

    public function postAsAdmin(Request $request)
    {
        $request->validate([
            'content' => 'required|string|max:5000',
            'post_type' => 'required|string|in:text,profit_card,trade_idea,signal_card,screenshot',
        ]);

        $post = CommunityPost::create([
            'user_id' => auth()->id(),
            'status' => 'approved',
            'author_name' => auth()->user()->name,
            'author_badge' => 'Admin',
            'author_avatar_hex' => '0xFF38BDF8',
            'post_type' => $request->post_type,
            'content' => $request->content,
            'hashtags' => $request->input('hashtags', '#SignalXpress'),
            'pair' => $request->input('pair'),
            'trade_type' => $request->input('trade_type'),
            'entry_price' => $request->input('entry_price'),
            'exit_price' => $request->input('exit_price'),
            'lot_size' => $request->input('lot_size'),
            'profit_amount' => $request->input('profit_amount', 0),
            'pips_gain' => $request->input('pips_gain', 0),
            'roi_percentage' => $request->input('roi_percentage', 0),
            'broker_name' => $request->input('broker_name', 'Signal Xpress'),
            'card_theme' => $request->input('card_theme', 'EMERALD_NEON'),
            'is_verified_trade' => $request->input('is_verified_trade', false),
            'is_pinned' => $request->input('is_pinned', false),
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);

        CommunityPostCreated::dispatch($post);
        return response()->json(['ok' => true, 'post_id' => $post->id]);
    }

    // ── Chat Management ───────────────────────────────

    public function sendChat(Request $request, CentrifugoService $centrifugo)
    {
        $request->validate([
            'message' => 'required|string|max:2000',
            'type' => 'nullable|string|in:text,profit_card,trade_idea,signal_card',
        ]);

        $user = $request->user();
        $payload = null;

        if ($request->type !== 'text') {
            $payload = [
                'pair' => $request->input('pair'),
                'trade_type' => $request->input('trade_type'),
                'profit_amount' => $request->input('profit_amount'),
                'pips_gain' => $request->input('pips_gain'),
                'entry_price' => $request->input('entry_price'),
                'exit_price' => $request->input('exit_price'),
                'card_theme' => $request->input('card_theme', 'EMERALD_NEON'),
            ];
        }

        $chat = ChatMessage::create([
            'user_id' => $user->id,
            'author_name' => $user->name,
            'author_role' => $user->role,
            'message' => $request->message,
            'type' => $request->input('type', 'text'),
            'payload' => $payload,
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => $user->id,
        ]);

        // Broadcast to WebSocket
        $centrifugo->publish('chat:community', [
            'id' => $chat->id,
            'type' => 'chat_message',
            'user_id' => $user->id,
            'user_name' => $user->name,
            'user_role' => $user->role,
            'message' => $chat->message,
            'chat_type' => $chat->type,
            'payload' => $chat->payload,
            'timestamp' => $chat->created_at->toISOString(),
        ]);

        return response()->json(['ok' => true, 'chat' => $chat]);
    }

    public function approveChat(ChatMessage $chat, CentrifugoService $centrifugo)
    {
        $chat->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);

        $centrifugo->publish('chat:community', [
            'id' => $chat->id,
            'type' => 'chat_message',
            'user_id' => $chat->user_id,
            'user_name' => $chat->author_name,
            'user_role' => $chat->author_role,
            'message' => $chat->message,
            'chat_type' => $chat->type,
            'payload' => $chat->payload,
            'timestamp' => $chat->created_at->toISOString(),
        ]);

        return response()->json(['ok' => true]);
    }

    public function rejectChat(ChatMessage $chat)
    {
        $chat->update(['status' => 'rejected']);
        return response()->json(['ok' => true]);
    }

    public function deleteChat(ChatMessage $chat)
    {
        $chat->delete();
        return response()->json(['ok' => true]);
    }

    public function getPendingCount()
    {
        return response()->json([
            'pending_posts' => CommunityPost::where('status', 'pending')->count(),
            'pending_comments' => CommunityComment::where('status', 'pending')->count(),
            'pending_chats' => ChatMessage::where('status', 'pending')->count(),
        ]);
    }
}
