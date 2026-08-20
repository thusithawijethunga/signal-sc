<?php

namespace App\Providers;

use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Events\SignalCreated;
use App\Events\CommunityPostCreated;
use App\Events\MarketNewsCreated;
use App\Listeners\PublishTradeToWebSocket;
use App\Listeners\PublishTradeUpdateToWebSocket;
use App\Listeners\SyncTradeToGoogleSheets;
use App\Listeners\PublishSignalToWebSocket;
use App\Listeners\PublishCommunityPostToWebSocket;
use App\Listeners\PublishNewsToWebSocket;
use Illuminate\Foundation\Support\Providers\EventServiceProvider as ServiceProvider;

class EventServiceProvider extends ServiceProvider
{
    protected $listen = [
        TradeCreated::class => [
            PublishTradeToWebSocket::class,
            SyncTradeToGoogleSheets::class,
        ],
        TradeUpdated::class => [
            PublishTradeUpdateToWebSocket::class,
            SyncTradeToGoogleSheets::class,
        ],
        SignalCreated::class => [
            PublishSignalToWebSocket::class,
        ],
        CommunityPostCreated::class => [
            PublishCommunityPostToWebSocket::class,
        ],
        MarketNewsCreated::class => [
            PublishNewsToWebSocket::class,
        ],
    ];

    public function boot(): void
    {
        //
    }
}
