<?php

namespace App\Listeners;

use App\Events\TradeHit;
use App\Services\CentrifugoService;

class PublishTradeHitToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(TradeHit $event): void
    {
        $trade = $event->trade;
        $hitType = $event->hitType;

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => 'trade_hit',
            'hit_type' => $hitType,
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'timestamp' => now()->toISOString(),
        ]);

        $this->centrifugo->broadcastNotification(
            "🎯 {$hitType} Hit!",
            $trade->pair . ' ' . $trade->direction . ' | ' . $trade->pips . ' pips | $' . $trade->profit,
            'trade_hit',
            ['trade_id' => $trade->id, 'hit_type' => $hitType]
        );
    }
}
