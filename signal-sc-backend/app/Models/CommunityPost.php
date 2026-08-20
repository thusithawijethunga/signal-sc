<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class CommunityPost extends Model
{
    protected $table = 'community_posts';

    protected $fillable = [
        'user_id',
        'author_name',
        'author_badge',
        'author_avatar_hex',
        'post_type',
        'content',
        'hashtags',
        'image_uri',
        'pair',
        'trade_type',
        'entry_price',
        'exit_price',
        'lot_size',
        'profit_amount',
        'pips_gain',
        'roi_percentage',
        'broker_name',
        'card_theme',
        'is_verified_trade',
        'likes_count',
        'fire_count',
        'rocket_count',
        'comments_count',
        'is_liked_by_me',
        'is_fired_by_me',
        'is_rocket_by_me',
        'is_pinned',
    ];

    protected function casts(): array
    {
        return [
            'is_verified_trade' => 'boolean',
            'is_liked_by_me' => 'boolean',
            'is_fired_by_me' => 'boolean',
            'is_rocket_by_me' => 'boolean',
            'is_pinned' => 'boolean',
            'entry_price' => 'decimal:2',
            'exit_price' => 'decimal:2',
            'lot_size' => 'decimal:2',
            'profit_amount' => 'decimal:2',
            'roi_percentage' => 'decimal:2',
            'pips_gain' => 'decimal:1',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function comments(): HasMany
    {
        return $this->hasMany(CommunityComment::class);
    }

    public function reactions(): HasMany
    {
        return $this->hasMany(CommunityReaction::class);
    }
}
