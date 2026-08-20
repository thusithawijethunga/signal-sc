<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class AccountBalance extends Model
{
    protected $table = 'account_balances';

    protected $fillable = [
        'user_id',
        'start_balance',
        'deposit_balance',
        'withdraw_balance',
    ];

    protected function casts(): array
    {
        return [
            'start_balance' => 'decimal:2',
            'deposit_balance' => 'decimal:2',
            'withdraw_balance' => 'decimal:2',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
