<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class VipMember extends Model
{
    protected $table = 'vip_members';

    protected $fillable = [
        'rank',
        'name',
        'member_id',
        'lots',
        'progress_fraction',
        'accent_hex',
        'period',
        'win_rate',
        'total_trades',
        'broker',
        'favorite_pair',
    ];

    protected function casts(): array
    {
        return [
            'lots' => 'decimal:2',
            'progress_fraction' => 'decimal:4',
            'win_rate' => 'decimal:2',
        ];
    }
}
