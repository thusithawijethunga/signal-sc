<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class DevicePresence extends Model
{
    protected $table = 'device_presence';

    protected $fillable = [
        'user_id',
        'device_key',
        'device',
        'state',
        'last_seen_at',
    ];

    protected $casts = [
        'device' => 'array',
        'last_seen_at' => 'datetime',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
