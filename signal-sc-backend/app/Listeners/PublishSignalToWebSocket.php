<?php

namespace App\Listeners;

use App\Events\SignalCreated;
use App\Services\CentrifugoService;

class PublishSignalToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(SignalCreated $event): void
    {
        $signal = $event->signal;

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADING, [
            'id' => $signal->id,
            'no' => $signal->no,
            'type' => 'signal',
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
            'timestamp' => now()->toISOString(),
        ]);

        $this->centrifugo->broadcastNotification(
            '📡 New Signal: ' . $signal->pair,
            $signal->direction . ' ' . $signal->pair . ' | Entry: ' . ($signal->entry1 ?? '—'),
            'signal',
            ['signal_id' => $signal->id]
        );
    }
}
