<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CsvTradeRecord extends Model
{
    protected $table = 'csv_trade_records';

    protected $fillable = [
        'csv_upload_id',
        'account_id',
        'symbol',
        'lots',
        'commission',
    ];

    public function csvUpload(): BelongsTo
    {
        return $this->belongsTo(CsvUpload::class);
    }
}
