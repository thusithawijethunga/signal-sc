<?php

namespace App\Listeners;

use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Services\GoogleSheetsService;

class SyncTradeToGoogleSheets
{
    public function __construct(public GoogleSheetsService $sheets) {}

    public function handle(TradeCreated|TradeUpdated $event): void
    {
        $trade = $event->trade;
        $hitType = null;

        if ($event instanceof TradeUpdated) {
            $old = $event->oldResult;
            $explicitHit = $event->hitType;

            if ($explicitHit) {
                $hitType = $explicitHit;
            } elseif ($old !== null && $old !== $trade->result && $trade->result !== 'RUNNING') {
                $hitType = match ($trade->result) {
                    'WIN' => 'TP',
                    'LOSS' => 'SL',
                    'BE' => 'BE',
                    default => null,
                };
            }
        }

        $this->sheets->syncTrade($trade, $hitType);
    }
}
