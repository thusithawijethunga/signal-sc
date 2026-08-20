<?php

namespace App\Events;

use App\Models\CommunityPost;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class CommunityPostCreated
{
    use Dispatchable, SerializesModels;

    public function __construct(public CommunityPost $post) {}
}
