<?php

namespace App\Events;

use App\Models\Signal;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class SignalUpdated
{
    use Dispatchable, SerializesModels;

    public function __construct(
        public Signal $signal,
        public ?string $action = null,
        public ?string $oldResult = null,
    ) {}
}
