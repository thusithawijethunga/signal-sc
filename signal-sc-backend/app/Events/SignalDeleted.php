<?php

namespace App\Events;

use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class SignalDeleted
{
    use Dispatchable, SerializesModels;

    public function __construct(
        public int $signalId,
        public int $signalNo,
    ) {}
}
