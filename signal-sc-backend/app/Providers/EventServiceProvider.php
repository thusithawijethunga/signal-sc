<?php

namespace App\Providers;

use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Events\SignalCreated;
use App\Events\SignalUpdated;
use App\Events\SignalDeleted;
use App\Events\CommunityPostCreated;
use App\Events\MarketNewsCreated;
use App\Listeners\PublishTradeToWebSocket;
use App\Listeners\PublishTradeUpdateToWebSocket;
use App\Listeners\PublishSignalToWebSocket;
use App\Listeners\PublishSignalUpdateToWebSocket;
use App\Listeners\PublishSignalDeleteToWebSocket;
use App\Listeners\PublishCommunityPostToWebSocket;
use App\Listeners\PublishNewsToWebSocket;
use Illuminate\Foundation\Support\Providers\EventServiceProvider as ServiceProvider;

class EventServiceProvider extends ServiceProvider
{
    protected $listen = [
        TradeCreated::class => [
            PublishTradeToWebSocket::class,
        ],
        TradeUpdated::class => [
            PublishTradeUpdateToWebSocket::class,
        ],
        SignalCreated::class => [
            PublishSignalToWebSocket::class,
        ],
        SignalUpdated::class => [
            PublishSignalUpdateToWebSocket::class,
        ],
        SignalDeleted::class => [
            PublishSignalDeleteToWebSocket::class,
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
