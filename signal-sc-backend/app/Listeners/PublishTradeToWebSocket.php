<?php

namespace App\Listeners;

use App\Events\TradeCreated;
use App\Services\CentrifugoService;

class PublishTradeToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(TradeCreated $event): void
    {
        $trade = $event->trade;

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => 'trade',
            'action' => 'created',
            'pair' => $trade->pair ?? '',
            'direction' => $trade->direction ?? '',
            'entry1' => $trade->entry1 ?? 0,
            'entry2' => $trade->entry2 ?? 0,
            'sl' => $trade->sl ?? 0,
            'tp1' => $trade->tp1 ?? 0,
            'tp2' => $trade->tp2 ?? 0,
            'tp3' => $trade->tp3 ?? 0,
            'tp4' => $trade->tp4 ?? 0,
            'pips' => $trade->pips ?? 0,
            'profit' => $trade->profit ?? 0,
            'result' => $trade->result ?? 'RUNNING',
            'channel' => $trade->channel ?? 'VIP',
            'date' => $trade->date?->format('Y-m-d') ?? '',
            'timestamp' => now()->toISOString(),
        ]);

        // Include signal_id and signal_no in broadcast so Android can deduplicate notifications
        $signalId = \App\Models\Signal::where('no', $trade->no)->value('id');

        $this->centrifugo->broadcastNotification(
            '📊 New Trade: ' . $trade->pair,
            $trade->direction . ' ' . $trade->pair . ' | ' . $trade->result,
            'trade',
            array_merge(
                ['trade_id' => $trade->id, 'signal_no' => $trade->no],
                $signalId ? ['signal_id' => $signalId] : []
            )
        );
    }
}
