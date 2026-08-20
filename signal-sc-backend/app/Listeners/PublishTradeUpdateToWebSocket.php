<?php

namespace App\Listeners;

use App\Events\TradeUpdated;
use App\Services\CentrifugoService;

class PublishTradeUpdateToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(TradeUpdated $event): void
    {
        $trade = $event->trade;

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => 'trade',
            'action' => 'updated',
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'date' => $trade->date?->format('Y-m-d'),
            'timestamp' => now()->toISOString(),
        ]);
    }
}
