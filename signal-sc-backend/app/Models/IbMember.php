<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class IbMember extends Model
{
    protected $table = 'ib_members';

    protected $fillable = [
        'sx_id',
        'user_id',
        'name',
        'broker',
        'account_id',
        'nic',
        'whatsapp',
        'telegram',
        'partner_id',
    ];

    protected static function boot(): void
    {
        parent::boot();

        static::creating(function ($model) {
            if (empty($model->sx_id)) {
                $lastMember = static::orderByDesc('id')->first();
                $nextNumber = $lastMember ? intval(substr($lastMember->sx_id, 2)) + 1 : 1;
                $model->sx_id = 'SX' . str_pad($nextNumber, 5, '0', STR_PAD_LEFT);
            }
        });
    }

    public function partner(): BelongsTo
    {
        return $this->belongsTo(IbPartner::class, 'partner_id');
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
