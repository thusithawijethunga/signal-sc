<?php

namespace App\Events;

use App\Models\Trade;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class TradeUpdated
{
    use Dispatchable, SerializesModels;

    public function __construct(
        public Trade $trade,
        public ?string $oldResult = null,
        public ?string $hitType = null,
    ) {}
}
