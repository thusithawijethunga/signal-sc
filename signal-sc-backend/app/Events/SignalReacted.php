<?php

namespace App\Events;

use App\Models\Signal;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class SignalReacted
{
    use Dispatchable, SerializesModels;

    public function __construct(
        public Signal $signal,
        public string $action,
        public string $emoji,
        public string $userName,
    ) {}
}
