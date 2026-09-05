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
            'pair' => $signal->pair ?? '',
            'direction' => $signal->direction ?? '',
            'entry1' => $signal->entry1 ?? 0,
            'entry2' => $signal->entry2 ?? 0,
            'sl' => $signal->sl ?? 0,
            'tp1' => $signal->tp1 ?? 0,
            'tp2' => $signal->tp2 ?? 0,
            'tp3' => $signal->tp3 ?? 0,
            'tp4' => $signal->tp4 ?? 0,
            'pips' => $signal->pips ?? 0,
            'profit' => $signal->profit ?? 0,
            'result' => $signal->result ?? 'RUNNING',
            'channel' => $signal->channel ?? 'VIP',
            'date' => $signal->date?->format('Y-m-d') ?? '',
            'hit_level' => $signal->hit_level ?? 'NONE',
            'status' => $signal->status ?? 'active',
            'thumbs_count' => $signal->thumbs_count ?? 0,
            'fire_count' => $signal->fire_count ?? 0,
            'rocket_count' => $signal->rocket_count ?? 0,
            'broken_heart_count' => $signal->broken_heart_count ?? 0,
            'timestamp' => now()->toISOString(),
        ]);

        $label = match(strtoupper($action)) {
            'TP1' => '🎯 TP1 Hit!',
            'TP2' => '🎯 TP2 Hit!',
            'TP3' => '🎯 TP3 Hit!',
            'TP4' => '🎯 TP4 Hit!',
            'SL' => '🛑 SL Hit!',
            'BE' => '⚖️ BE Hit!',
            default => '📊 Signal Updated',
        };

        $this->centrifugo->broadcastNotification(
            $label,
            $signal->pair . ' ' . $signal->direction . ' | ' . ($signal->pips ?? 0) . ' pips',
            'signal_update',
            ['signal_id' => $signal->id, 'signal_no' => $signal->no, 'action' => $action]
        );
    }
}
