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
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'entry1' => $trade->entry1,
            'entry2' => $trade->entry2,
            'sl' => $trade->sl,
            'tp1' => $trade->tp1,
            'tp2' => $trade->tp2,
            'tp3' => $trade->tp3,
            'tp4' => $trade->tp4,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'channel' => $trade->channel,
            'date' => $trade->date?->format('Y-m-d'),
            'timestamp' => now()->toISOString(),
        ]);

        $this->centrifugo->broadcastNotification(
            '📊 New Trade: ' . $trade->pair,
            $trade->direction . ' ' . $trade->pair . ' | ' . $trade->result,
            'trade',
            ['trade_id' => $trade->id]
        );
    }
}
