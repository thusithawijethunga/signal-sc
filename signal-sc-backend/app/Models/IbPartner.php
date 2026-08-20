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
        return $this->hasMany(IbMember::class);
    }
}
