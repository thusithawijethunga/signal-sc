<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class CsvUpload extends Model
{
    protected $table = 'csv_uploads';

    protected $fillable = [
        'user_id',
        'filename',
        'total_records',
        'total_lots',
        'total_commission',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function records(): HasMany
    {
        return $this->hasMany(CsvTradeRecord::class);
    }
}
