<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class User extends Authenticatable
{
    use HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'role',
        'api_token',
        'sx_id',
        'broker',
        'account_id',
        'nic',
        'whatsapp',
        'telegram',
        'partner_id',
    ];

    protected $hidden = [
        'password',
        'remember_token',
        'api_token',
    ];

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    public function signals(): HasMany
    {
        return $this->hasMany(Signal::class);
    }

    public function trades(): HasMany
    {
        return $this->hasMany(Trade::class);
    }

    public function posts(): HasMany
    {
        return $this->hasMany(CommunityPost::class);
    }

    public function comments(): HasMany
    {
        return $this->hasMany(CommunityComment::class);
    }

    public function balances(): HasMany
    {
        return $this->hasMany(AccountBalance::class);
    }

    public function syncQueue(): HasMany
    {
        return $this->hasMany(SyncQueue::class);
    }

    public function partner(): BelongsTo
    {
        return $this->belongsTo(IbPartner::class, 'partner_id');
    }

    public function scopeIbMembers($query)
    {
        return $query->where('role', 'ib_member');
    }
}
