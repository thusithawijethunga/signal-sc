<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Signal extends Model
{
    protected $table = 'signals';

    protected $fillable = [
        'no',
        'date',
        'pair',
        'direction',
        'entry1',
        'entry2',
        'sl',
        'tp1',
        'tp2',
        'tp3',
        'tp4',
        'pips',
        'profit',
        'result',
        'channel',
        'hit_level',
        'status',
        'thumbs_count',
        'fire_count',
        'rocket_count',
        'broken_heart_count',
        'user_reacted_emoji',
        'user_id',
    ];

    protected function casts(): array
    {
        return [
            'date' => 'date',
            'entry1' => 'decimal:2',
            'entry2' => 'decimal:2',
            'sl' => 'decimal:2',
            'tp1' => 'decimal:2',
            'tp2' => 'decimal:2',
            'tp3' => 'decimal:2',
            'tp4' => 'decimal:2',
            'pips' => 'decimal:1',
            'profit' => 'decimal:2',
            'no' => 'integer',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function reactions(): HasMany
    {
        return $this->hasMany(SignalReaction::class);
    }
}
