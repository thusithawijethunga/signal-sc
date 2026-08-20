<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class MarketNews extends Model
{
    protected $table = 'market_news';

    protected $fillable = [
        'event_time',
        'currency',
        'title',
        'impact',
        'forecast',
        'previous',
        'actual',
        'description',
    ];

    protected function casts(): array
    {
        return [
            'event_time' => 'datetime',
        ];
    }
}
