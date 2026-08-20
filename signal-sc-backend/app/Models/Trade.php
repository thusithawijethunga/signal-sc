<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Trade extends Model
{
    protected $table = 'trades';

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
        'hit_level',
        'channel',
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
}
