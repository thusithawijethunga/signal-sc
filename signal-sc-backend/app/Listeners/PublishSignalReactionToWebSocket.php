<?php

namespace App\Listeners;

use App\Events\SignalReacted;
use App\Services\CentrifugoService;

class PublishSignalReactionToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(SignalReacted $event): void
    {
        $signal = $event->signal;

        $emojiDisplay = match($event->emoji) {
            'thumbs' => '👍',
            'fire' => '🔥',
            'rocket' => '🚀',
            'broken_heart' => '💔',
            default => $event->emoji,
        };

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADING, [
            'id' => $signal->id,
            'no' => $signal->no,
            'type' => 'signal',
            'action' => 'reaction',
            'reaction_action' => $event->action,
            'emoji' => $event->emoji,
            'emoji_display' => $emojiDisplay,
            'user_name' => $event->userName,
            'pair' => $signal->pair ?? '',
            'direction' => $signal->direction ?? '',
            'thumbs_count' => $signal->thumbs_count ?? 0,
            'fire_count' => $signal->fire_count ?? 0,
            'rocket_count' => $signal->rocket_count ?? 0,
            'broken_heart_count' => $signal->broken_heart_count ?? 0,
            'timestamp' => now()->toISOString(),
        ]);

        $verb = match($event->action) {
            'added' => 'reacted',
            'removed' => 'removed reaction',
            'changed' => 'changed reaction',
            default => 'reacted',
        };

        $this->centrifugo->broadcastNotification(
            $emojiDisplay . ' ' . $event->userName . ' ' . $verb,
            'Signal #' . $signal->no . ' ' . $signal->pair,
            'signal_reaction',
            ['signal_id' => $signal->id]
        );
    }
}
