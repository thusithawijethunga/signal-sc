<?php

namespace App\Http\Controllers;

use App\Models\CommunityPost;
use App\Models\CommunityComment;
use App\Models\CommunitySetting;
use App\Events\CommunityPostCreated;
use Illuminate\Http\Request;

class AdminCommunityController extends Controller
{
    public function index(Request $request)
    {
        $query = CommunityPost::with('user');

        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        $posts = $query->orderBy('created_at', 'desc')->paginate(20);
        $pendingCount = CommunityPost::where('status', 'pending')->count();
        $pendingComments = CommunityComment::where('status', 'pending')->count();

        $settings = [
            'require_post_approval' => CommunitySetting::get('require_post_approval', '1') === '1',
            'require_comment_approval' => CommunitySetting::get('require_comment_approval', '0') === '1',
        ];

        return view('admin.community', compact('posts', 'pendingCount', 'pendingComments', 'settings'));
    }

    public function approvePost(CommunityPost $post)
    {
        $post->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
            'rejection_reason' => null,
        ]);

        CommunityPostCreated::dispatch($post);

        return back()->with('success', 'Post approved and published');
    }

    public function approveAllPosts()
    {
        $posts = CommunityPost::where('status', 'pending')->get();
        foreach ($posts as $post) {
            $post->update([
                'status' => 'approved',
                'approved_at' => now(),
                'approved_by' => auth()->id(),
            ]);
            CommunityPostCreated::dispatch($post);
        }

        return back()->with('success', count($posts) . ' posts approved');
    }

    public function rejectPost(Request $request, CommunityPost $post)
    {
        $post->update([
            'status' => 'rejected',
            'approved_by' => auth()->id(),
            'rejection_reason' => $request->input('reason', 'Does not meet community guidelines'),
        ]);

        return back()->with('success', 'Post rejected');
    }

    public function deletePost(CommunityPost $post)
    {
        $post->delete();
        return back()->with('success', 'Post deleted');
    }

    public function approveComment(CommunityComment $comment)
    {
        $comment->update([
            'status' => 'approved',
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);
        $comment->post->increment('comments_count');

        return back()->with('success', 'Comment approved');
    }

    public function approveAllComments()
    {
        $comments = CommunityComment::where('status', 'pending')->get();
        foreach ($comments as $comment) {
            $comment->update([
                'status' => 'approved',
                'approved_at' => now(),
                'approved_by' => auth()->id(),
            ]);
            $comment->post->increment('comments_count');
        }

        return back()->with('success', count($comments) . ' comments approved');
    }

    public function rejectComment(CommunityComment $comment)
    {
        $comment->delete();
        return back()->with('success', 'Comment rejected and deleted');
    }

    public function updateSettings(Request $request)
    {
        CommunitySetting::set('require_post_approval', $request->input('require_post_approval') ? '1' : '0');
        CommunitySetting::set('require_comment_approval', $request->input('require_comment_approval') ? '1' : '0');

        return back()->with('success', 'Community settings updated');
    }

    public function postAsAdmin(Request $request)
    {
        $request->validate([
            'content' => 'required|string',
            'post_type' => 'required|string|in:text,screenshot,signal_card',
        ]);

        $post = CommunityPost::create([
            'user_id' => auth()->id(),
            'status' => 'approved',
            'author_name' => auth()->user()->name,
            'author_badge' => 'Admin',
            'author_avatar_hex' => '0xFF38BDF8',
            'post_type' => $request->post_type,
            'content' => $request->content,
            'hashtags' => $request->input('hashtags', '#Admin #SignalXpress'),
            'pair' => $request->input('pair'),
            'trade_type' => $request->input('trade_type'),
            'image_uri' => $request->input('image_uri'),
            'profit_amount' => $request->input('profit_amount', 0),
            'pips_gain' => $request->input('pips_gain', 0),
            'broker_name' => $request->input('broker_name', 'Signal Xpress'),
            'is_verified_trade' => true,
            'is_pinned' => $request->input('is_pinned', false),
            'approved_at' => now(),
            'approved_by' => auth()->id(),
        ]);

        CommunityPostCreated::dispatch($post);

        return back()->with('success', 'Post published to community');
    }
}
