<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CommunityComment extends Model
{
    protected $table = 'community_comments';

    protected $fillable = [
        'post_id',
        'user_id',
        'author_name',
        'content',
        'likes_count',
    ];

    public function post(): BelongsTo
    {
        return $this->belongsTo(CommunityPost::class);
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
