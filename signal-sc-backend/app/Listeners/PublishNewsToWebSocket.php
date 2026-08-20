<?php

namespace App\Listeners;

use App\Events\MarketNewsCreated;
use App\Services\CentrifugoService;

class PublishNewsToWebSocket
{
    public function __construct(public CentrifugoService $centrifugo) {}

    public function handle(MarketNewsCreated $event): void
    {
        $news = $event->news;

        $this->centrifugo->publish(CentrifugoService::CHANNEL_NEWS, [
            'id' => $news->id,
            'type' => 'market_news',
            'currency' => $news->currency,
            'title' => $news->title,
            'impact' => $news->impact,
            'forecast' => $news->forecast,
            'previous' => $news->previous,
            'actual' => $news->actual,
            'event_time' => $news->event_time?->toISOString(),
            'timestamp' => now()->toISOString(),
        ]);
    }
}
