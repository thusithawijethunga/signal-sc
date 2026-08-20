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
        $oldResult = $event->oldResult;
        $resultChanged = $oldResult !== null && $oldResult !== $trade->result && $trade->result !== 'RUNNING';

        $payload = [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => $resultChanged ? 'trade_hit' : 'trade',
            'action' => $resultChanged ? strtolower($trade->result) : 'updated',
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
            'old_result' => $oldResult,
            'hit_level' => $trade->hit_level,
            'channel' => $trade->channel,
            'date' => $trade->date?->format('Y-m-d'),
            'timestamp' => now()->toISOString(),
        ];

        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, $payload);

        if ($resultChanged) {
            $label = match($trade->result) {
                'WIN' => '🎯 TP Hit!',
                'LOSS' => '🛑 SL Hit!',
                'BE' => '⚖️ BE Hit!',
                default => '📊 Result Update',
            };
            $this->centrifugo->broadcastNotification(
                $label,
                $trade->pair . ' ' . $trade->direction . ' | ' . $trade->pips . ' pips | $' . $trade->profit,
                'trade_hit',
                ['trade_id' => $trade->id, 'result' => $trade->result]
            );
        }
    }
}
