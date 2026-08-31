<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Events\CommunityPostCreated;
use App\Models\CommunityComment;
use App\Models\CommunityPost;
use App\Models\CommunityReaction;
use App\Models\CommunitySetting;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CommunityController extends Controller
{
    public function posts(Request $request): JsonResponse
    {
        try {
            $user = $request->user();
            $isAdmin = $user && $user->role === 'admin';

            $query = CommunityPost::with('user');

            // Non-admin users only see approved posts + their own pending posts
            if (!$isAdmin) {
                $query->where(function ($q) use ($user) {
                    $q->where('status', 'approved')
                      ->orWhere(function ($q2) use ($user) {
                          $q2->where('user_id', $user->id)
                             ->where('status', 'pending');
                      })
                      ->orWhere(function ($q3) use ($user) {
                          $q3->where('user_id', $user->id)
                             ->where('status', 'rejected');
                      });
                });
            } else {
                // Admin can filter by status
                if ($request->filled('status')) {
                    $query->where('status', $request->status);
                }
            }

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

            $requiresApproval = CommunitySetting::isPostApprovalRequired();
            $status = $requiresApproval ? 'pending' : 'approved';

            $post = CommunityPost::create([
                'user_id' => $request->user()->id,
                'status' => $status,
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
                'comments_need_review' => CommunitySetting::get('require_comment_approval', '0') === '1',
            ]);

            // Only broadcast if approved (no approval required)
            if (!$requiresApproval) {
                CommunityPostCreated::dispatch($post);
            }

            $message = $requiresApproval
                ? 'Post submitted for review. It will appear after admin approval.'
                : 'Post published successfully.';

            return response()->json([
                'post' => $post,
                'message' => $message,
                'status' => $status,
            ], 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create post', 'error' => $e->getMessage()], 500);
        }
    }

    public function destroyPost(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $user = $request->user();
            // Users can only delete their own posts, admin can delete any
            if ($user->role !== 'admin' && $post->user_id !== $user->id) {
                return response()->json(['message' => 'Unauthorized'], 403);
            }

            $post->delete();
            return response()->json(['message' => 'Post deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to delete post', 'error' => $e->getMessage()], 500);
        }
    }

    public function comments(Request $request, CommunityPost $post): JsonResponse
    {
        try {
            $user = $request->user();
            $isAdmin = $user && $user->role === 'admin';

            $query = CommunityComment::with('user')
                ->where('post_id', $post->id);

            // Non-admin: show approved comments + own pending comments
            if (!$isAdmin) {
                $query->where(function ($q) use ($user) {
                    $q->where('status', 'approved')
                      ->orWhere(function ($q2) use ($user) {
                          $q2->where('user_id', $user->id)
                             ->where('status', 'pending');
                      });
                });
            } else {
                if ($request->filled('status')) {
                    $query->where('status', $request->status);
                }
            }

            $comments = $query->orderBy('created_at', 'asc')
                ->paginate($request->get('per_page', 50));

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

            $requiresApproval = $post->comments_need_review || CommunitySetting::isCommentApprovalRequired();
            $status = $requiresApproval ? 'pending' : 'approved';

            $comment = CommunityComment::create([
                'post_id' => $post->id,
                'user_id' => $request->user()->id,
                'author_name' => $request->user()->name,
                'content' => $request->content,
                'status' => $status,
            ]);

            // Only increment count if auto-approved
            if (!$requiresApproval) {
                $post->increment('comments_count');
            }

            $message = $requiresApproval
                ? 'Comment submitted for review.'
                : 'Comment posted.';

            return response()->json([
                'comment' => $comment,
                'message' => $message,
                'status' => $status,
            ], 201);
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

    // Settings endpoint (admin only)
    public function settings(Request $request): JsonResponse
    {
        try {
            if ($request->user()->role !== 'admin') {
                return response()->json(['message' => 'Unauthorized'], 403);
            }

            if ($request->isMethod('GET')) {
                return response()->json([
                    'require_post_approval' => CommunitySetting::isPostApprovalRequired(),
                    'require_comment_approval' => CommunitySetting::isCommentApprovalRequired(),
                    'max_post_length' => CommunitySetting::get('max_post_length', '5000'),
                ]);
            }

            // Update settings
            if ($request->has('require_post_approval')) {
                CommunitySetting::set('require_post_approval', $request->input('require_post_approval') ? '1' : '0');
            }
            if ($request->has('require_comment_approval')) {
                CommunitySetting::set('require_comment_approval', $request->input('require_comment_approval') ? '1' : '0');
            }
            if ($request->has('max_post_length')) {
                CommunitySetting::set('max_post_length', $request->input('max_post_length'));
            }

            return response()->json(['message' => 'Settings updated']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to update settings', 'error' => $e->getMessage()], 500);
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
