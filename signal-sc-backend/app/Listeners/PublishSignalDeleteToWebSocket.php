<?php

namespace App\Listeners;

use App\Events\SignalDeleted;
use App\Services\CentrifugoService;

class PublishSignalDeleteToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(SignalDeleted $event): void
    {
        $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADING, [
            'id' => $event->signalId,
            'no' => $event->signalNo,
            'type' => 'signal',
            'action' => 'deleted',
            'timestamp' => now()->toISOString(),
        ]);

        $this->centrifugo->broadcastNotification(
            '🗑️ Signal Deleted',
            'Signal #' . $event->signalNo . ' has been removed',
            'signal_delete',
            ['signal_id' => $event->signalId]
        );
    }
}
