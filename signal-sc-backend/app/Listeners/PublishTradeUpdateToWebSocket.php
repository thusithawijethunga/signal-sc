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
            'old_result' => $oldResult,
            'hit_level' => $trade->hit_level ?? 'NONE',
            'channel' => $trade->channel ?? 'VIP',
            'date' => $trade->date?->format('Y-m-d') ?? '',
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

            // Include signal_id and signal_no so the Android app can deduplicate notifications
            // (the same hit arrives from both trading:trades and trading:signals channels).
            $signalId = \App\Models\Signal::where('no', $trade->no)->value('id');

            $this->centrifugo->broadcastNotification(
                $label,
                $trade->pair . ' ' . $trade->direction . ' | ' . $trade->pips . ' pips | $' . $trade->profit,
                'trade_hit',
                array_merge(
                    ['trade_id' => $trade->id, 'result' => $trade->result, 'signal_no' => $trade->no],
                    $signalId ? ['signal_id' => $signalId] : []
                )
            );
        }
    }
}
