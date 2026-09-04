<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class IbPartner extends Model
{
    protected $table = 'ib_partners';

    protected $fillable = [
        'name',
    ];

    public function members(): HasMany
    {
        return $this->hasMany(User::class, 'partner_id')->where('role', 'ib_member');
    }
}
