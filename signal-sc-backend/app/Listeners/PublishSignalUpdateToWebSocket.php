<?php

namespace App\Listeners;

use App\Events\SignalUpdated;
use App\Services\CentrifugoService;

class PublishSignalUpdateToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(SignalUpdated $event): void
    {
        $signal = $event->signal;
        $action = $event->action ?? 'updated';

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADING, [
            'id' => $signal->id,
            'no' => $signal->no,
            'type' => 'signal',
            'action' => $action,
            'pair' => $signal->pair,
            'direction' => $signal->direction,
            'entry1' => $signal->entry1,
            'entry2' => $signal->entry2,
            'sl' => $signal->sl,
            'tp1' => $signal->tp1,
            'tp2' => $signal->tp2,
            'tp3' => $signal->tp3,
            'tp4' => $signal->tp4,
            'pips' => $signal->pips,
            'profit' => $signal->profit,
            'result' => $signal->result,
            'channel' => $signal->channel,
            'date' => $signal->date?->format('Y-m-d'),
            'hit_level' => $signal->hit_level,
            'status' => $signal->status,
            'thumbs_count' => $signal->thumbs_count ?? 0,
            'fire_count' => $signal->fire_count ?? 0,
            'rocket_count' => $signal->rocket_count ?? 0,
            'broken_heart_count' => $signal->broken_heart_count ?? 0,
            'timestamp' => now()->toISOString(),
        ]);

        $label = match($action) {
            'tp1_hit', 'tp2_hit', 'tp3_hit', 'tp4_hit' => '🎯 ' . strtoupper($action) . ' Hit!',
            'sl_hit' => '🛑 SL Hit!',
            'be' => '⚖️ BE Hit!',
            default => '📊 Signal Updated',
        };

        $this->centrifugo->broadcastNotification(
            $label,
            $signal->pair . ' ' . $signal->direction . ' | ' . ($signal->pips ?? 0) . ' pips',
            'signal_update',
            ['signal_id' => $signal->id, 'action' => $action]
        );
    }
}
