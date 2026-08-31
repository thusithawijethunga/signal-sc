<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Events\CommunityPostCreated;
use App\Models\CommunityComment;
use App\Models\CommunityPost;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminCommunityController extends Controller
{
    public function __construct()
    {
        $this->middleware(function ($request, $next) {
            if (!$request->user() || $request->user()->role !== 'admin') {
                return response()->json(['message' => 'Unauthorized'], 403);
            }
            return $next($request);
        });
    }

    // ── Pending Posts ─────────────────────────────────

    public function pendingPosts(Request $request): JsonResponse
    {
        try {
            $posts = CommunityPost::with('user')
                ->where('status', 'pending')
                ->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($posts);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch pending posts', 'error' => $e->getMessage()], 500);
        }
    }

    public function allPosts(Request $request): JsonResponse
    {
        try {
            $query = CommunityPost::with('user');

            if ($request->filled('status')) {
                $query->where('status', $request->status);
            }

            $posts = $query->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($posts);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch posts', 'error' => $e->getMessage()], 500);
        }
    }

    public function approvePost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $post->update([
                'status' => 'approved',
                'approved_at' => now(),
                'approved_by' => $request->user()->id,
                'rejection_reason' => null,
            ]);

            // Broadcast the approved post via WebSocket
            CommunityPostCreated::dispatch($post);

            return response()->json([
                'message' => 'Post approved and published',
                'post' => $post,
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to approve post', 'error' => $e->getMessage()], 500);
        }
    }

    public function approveAllPosts(Request $request): JsonResponse
    {
        try {
            $posts = CommunityPost::where('status', 'pending')->get();

            foreach ($posts as $post) {
                $post->update([
                    'status' => 'approved',
                    'approved_at' => now(),
                    'approved_by' => $request->user()->id,
                    'rejection_reason' => null,
                ]);
                CommunityPostCreated::dispatch($post);
            }

            return response()->json([
                'message' => count($posts) . ' posts approved and published',
                'count' => count($posts),
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to approve posts', 'error' => $e->getMessage()], 500);
        }
    }

    public function rejectPost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $request->validate([
                'rejection_reason' => 'nullable|string|max:500',
            ]);

            $post->update([
                'status' => 'rejected',
                'approved_at' => null,
                'approved_by' => $request->user()->id,
                'rejection_reason' => $request->input('rejection_reason', 'Does not meet community guidelines'),
            ]);

            return response()->json([
                'message' => 'Post rejected',
                'post' => $post,
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to reject post', 'error' => $e->getMessage()], 500);
        }
    }

    public function deletePost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $post->delete();
            return response()->json(['message' => 'Post deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to delete post', 'error' => $e->getMessage()], 500);
        }
    }

    // ── Pending Comments ──────────────────────────────

    public function pendingComments(Request $request): JsonResponse
    {
        try {
            $comments = CommunityComment::with('post', 'user')
                ->where('status', 'pending')
                ->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($comments);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch pending comments', 'error' => $e->getMessage()], 500);
        }
    }

    public function approveComment(Request $request, CommunityComment $comment): JsonResponse
    {
        try {
            $comment->update([
                'status' => 'approved',
                'approved_at' => now(),
                'approved_by' => $request->user()->id,
            ]);

            // Increment the parent post's comment count
            $comment->post->increment('comments_count');

            return response()->json([
                'message' => 'Comment approved',
                'comment' => $comment,
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to approve comment', 'error' => $e->getMessage()], 500);
        }
    }

    public function approveAllComments(Request $request): JsonResponse
    {
        try {
            $comments = CommunityComment::where('status', 'pending')->get();

            foreach ($comments as $comment) {
                $comment->update([
                    'status' => 'approved',
                    'approved_at' => now(),
                    'approved_by' => $request->user()->id,
                ]);
                $comment->post->increment('comments_count');
            }

            return response()->json([
                'message' => count($comments) . ' comments approved',
                'count' => count($comments),
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to approve comments', 'error' => $e->getMessage()], 500);
        }
    }

    public function rejectComment(Request $request, CommunityComment $comment): JsonResponse
    {
        try {
            $comment->delete();
            return response()->json(['message' => 'Comment rejected and deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to reject comment', 'error' => $e->getMessage()], 500);
        }
    }

    // ── Stats ─────────────────────────────────────────

    public function stats(): JsonResponse
    {
        try {
            return response()->json([
                'pending_posts' => CommunityPost::where('status', 'pending')->count(),
                'approved_posts' => CommunityPost::where('status', 'approved')->count(),
                'rejected_posts' => CommunityPost::where('status', 'rejected')->count(),
                'pending_comments' => CommunityComment::where('status', 'pending')->count(),
                'approved_comments' => CommunityComment::where('status', 'approved')->count(),
                'total_posts' => CommunityPost::count(),
                'total_comments' => CommunityComment::count(),
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch stats', 'error' => $e->getMessage()], 500);
        }
    }
}
