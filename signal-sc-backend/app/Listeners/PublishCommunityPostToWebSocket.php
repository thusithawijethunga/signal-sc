<?php

namespace App\Listeners;

use App\Events\CommunityPostCreated;
use App\Services\CentrifugoService;

class PublishCommunityPostToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(CommunityPostCreated $event): void
    {
        $post = $event->post;

        // Only broadcast approved posts
        if ($post->status !== 'approved') {
            return;
        }

        $this->centrifugo->publish(CentrifugoService::CHANNEL_COMMUNITY, [
            'id' => $post->id,
            'type' => 'community_post',
            'author_name' => $post->author_name,
            'author_badge' => $post->author_badge,
            'author_avatar_hex' => $post->author_avatar_hex,
            'post_type' => $post->post_type,
            'content' => $post->content,
            'hashtags' => $post->hashtags,
            'image_uri' => $post->image_uri,
            'pair' => $post->pair,
            'trade_type' => $post->trade_type,
            'profit_amount' => $post->profit_amount,
            'pips_gain' => $post->pips_gain,
            'broker_name' => $post->broker_name,
            'card_theme' => $post->card_theme,
            'is_verified_trade' => $post->is_verified_trade,
            'is_pinned' => $post->is_pinned,
            'status' => 'approved',
            'timestamp' => now()->toISOString(),
        ]);

        $this->centrifugo->broadcastNotification(
            '👥 New Community Post',
            $post->author_name . ' shared: ' . substr($post->content ?? '', 0, 80) . '...',
            'community',
            ['post_id' => $post->id]
        );
    }
}
