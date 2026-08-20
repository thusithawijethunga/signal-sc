<?php

namespace App\Events;

use App\Models\MarketNews;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class MarketNewsCreated
{
    use Dispatchable, SerializesModels;

    public function __construct(public MarketNews $news) {}
}
