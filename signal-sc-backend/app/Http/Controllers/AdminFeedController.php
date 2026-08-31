<?php

namespace App\Http\Controllers;

use App\Models\CommunityPost;
use App\Models\CommunityComment;
use App\Models\Trade;
use App\Models\MarketNews;
use App\Models\Signal;
use App\Models\CommunitySetting;
use App\Events\CommunityPostCreated;
use App\Events\SignalCreated;
use App\Services\CentrifugoService;
use Illuminate\Http\Request;

class AdminFeedController extends Controller
{
    public function index()
    {
        // All posts (pending + approved + rejected) for admin
        $posts = CommunityPost::with('user', 'comments')
            ->orderBy('is_pinned', 'desc')
            ->orderBy('created_at', 'desc')
            ->limit(50)
            ->get();

        $pendingCount = CommunityPost::where('status', 'pending')->count();
        $pendingComments = CommunityComment::where('status', 'pending')->with('post')->latest()->limit(20)->get();

        // Recent trades for quick posting
        $recentTrades = Trade::orderBy('date', 'desc')->limit(5)->get();
        $recentSignals = Signal::orderBy('date', 'desc')->limit(5)->get();

        return view('admin.feed', compact(
            'posts', 'pendingCount', 'pendingComments',
            'recentTrades', 'recentSignals'
        ));
    }

    public function approvePost(CommunityPost $post)
    {
        $post->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);
        CommunityPostCreated::dispatch($post);

        return response()->json(['ok' => true, 'message' => 'Post approved']);
    }

    public function rejectPost(CommunityPost $post)
    {
        $post->update([
            'status' => 'rejected',
            'approved_by' => auth()->id(),
            'rejection_reason' => 'Rejected by admin',
        ]);
        return response()->json(['ok' => true, 'message' => 'Post rejected']);
    }

    public function deletePost(CommunityPost $post)
    {
        $post->delete();
        return response()->json(['ok' => true, 'message' => 'Post deleted']);
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

    public function postAsAdmin(Request $request, CentrifugoService $centrifugo)
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

        return response()->json(['ok' => true, 'message' => 'Published', 'post_id' => $post->id]);
    }

    public function togglePin(CommunityPost $post)
    {
        $post->update(['is_pinned' => !$post->is_pinned]);
        return response()->json(['ok' => true, 'pinned' => $post->is_pinned]);
    }

    public function getPendingCount()
    {
        return response()->json([
            'pending_posts' => CommunityPost::where('status', 'pending')->count(),
            'pending_comments' => CommunityComment::where('status', 'pending')->count(),
        ]);
    }
}
