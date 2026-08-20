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

        $this->centrifugo->publish(CentrifugoService::CHANNEL_COMMUNITY, [
            'id' => $post->id,
            'type' => 'community_post',
            'author_name' => $post->author_name,
            'post_type' => $post->post_type,
            'content' => $post->content,
            'pair' => $post->pair,
            'profit_amount' => $post->profit_amount,
            'pips_gain' => $post->pips_gain,
            'timestamp' => now()->toISOString(),
        ]);
    }
}
