<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CommunityComment;
use App\Models\CommunityPost;
use App\Models\CommunityReaction;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CommunityController extends Controller
{
    public function posts(Request $request): JsonResponse
    {
        try {
            $query = CommunityPost::with('user');

            if ($request->filled('post_type')) {
                $query->where('post_type', $request->post_type);
            }
            if ($request->filled('pair')) {
                $query->where('pair', $request->pair);
            }
            if ($request->filled('search')) {
                $search = $request->search;
                $query->where(function ($q) use ($search) {
                    $q->where('content', 'like', "%{$search}%")
                      ->orWhere('author_name', 'like', "%{$search}%")
                      ->orWhere('hashtags', 'like', "%{$search}%");
                });
            }

            $posts = $query->orderBy('is_pinned', 'desc')
                ->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($posts);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch posts', 'error' => $e->getMessage()], 500);
        }
    }

    public function storePost(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'content' => 'required|string',
                'post_type' => 'required|string|in:text,trade_update,screenshot,signal_card',
            ]);

            $post = CommunityPost::create([
                'user_id' => $request->user()->id,
                'author_name' => $request->user()->name,
                'author_badge' => $request->get('author_badge'),
                'author_avatar_hex' => $request->get('author_avatar_hex'),
                'post_type' => $request->post_type,
                'content' => $request->content,
                'hashtags' => $request->get('hashtags'),
                'image_uri' => $request->get('image_uri'),
                'pair' => $request->get('pair'),
                'trade_type' => $request->get('trade_type'),
                'entry_price' => $request->get('entry_price'),
                'exit_price' => $request->get('exit_price'),
                'lot_size' => $request->get('lot_size'),
                'profit_amount' => $request->get('profit_amount'),
                'pips_gain' => $request->get('pips_gain'),
                'roi_percentage' => $request->get('roi_percentage'),
                'broker_name' => $request->get('broker_name'),
                'card_theme' => $request->get('card_theme'),
                'is_verified_trade' => $request->get('is_verified_trade', false),
            ]);

            return response()->json($post, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create post', 'error' => $e->getMessage()], 500);
        }
    }

    public function destroyPost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $post->delete();
            return response()->json(['message' => 'Post deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to delete post', 'error' => $e->getMessage()], 500);
        }
    }

    public function comments(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $comments = CommunityComment::with('user')
                ->where('post_id', $post->id)
                ->orderBy('created_at', 'asc')
                ->paginate($request->get('per_page', 20));

            return response()->json($comments);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch comments', 'error' => $e->getMessage()], 500);
        }
    }

    public function storeComment(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $request->validate([
                'content' => 'required|string',
            ]);

            $comment = CommunityComment::create([
                'post_id' => $post->id,
                'user_id' => $request->user()->id,
                'author_name' => $request->user()->name,
                'content' => $request->content,
            ]);

            $post->increment('comments_count');

            return response()->json($comment, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create comment', 'error' => $e->getMessage()], 500);
        }
    }

    public function reactPost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $request->validate([
                'emoji' => 'required|string|in:thumbs,fire,rocket',
            ]);

            $userId = $request->user()->id;
            $emoji = $request->emoji;

            $existing = CommunityReaction::where('post_id', $post->id)
                ->where('user_id', $userId)
                ->first();

            if ($existing) {
                if ($existing->emoji === $emoji) {
                    $existing->delete();
                    $this->decrementPostCount($post, $emoji);
                    return response()->json(['message' => 'Reaction removed', 'post' => $post->fresh()]);
                }

                $this->decrementPostCount($post, $existing->emoji);
                $existing->update(['emoji' => $emoji]);
                $this->incrementPostCount($post, $emoji);

                return response()->json(['message' => 'Reaction changed', 'post' => $post->fresh()]);
            }

            CommunityReaction::create([
                'post_id' => $post->id,
                'user_id' => $userId,
                'emoji' => $emoji,
            ]);
            $this->incrementPostCount($post, $emoji);

            return response()->json(['message' => 'Reaction added', 'post' => $post->fresh()]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to react', 'error' => $e->getMessage()], 500);
        }
    }

    private function incrementPostCount(CommunityPost $post, string $emoji): void
    {
        match ($emoji) {
            'thumbs' => $post->increment('likes_count'),
            'fire' => $post->increment('fire_count'),
            'rocket' => $post->increment('rocket_count'),
            default => null,
        };
    }

    private function decrementPostCount(CommunityPost $post, string $emoji): void
    {
        match ($emoji) {
            'thumbs' => $post->decrement('likes_count'),
            'fire' => $post->decrement('fire_count'),
            'rocket' => $post->decrement('rocket_count'),
            default => null,
        };
    }
}
