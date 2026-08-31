<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CommunitySetting extends Model
{
    protected $table = 'community_settings';

    protected $fillable = ['key', 'value', 'description'];

    public static function get(string $key, mixed $default = null): mixed
    {
        $setting = self::where('key', $key)->first();
        return $setting ? $setting->value : $default;
    }

    public static function set(string $key, mixed $value): void
    {
        self::updateOrCreate(
            ['key' => $key],
            ['value' => $value]
        );
    }

    public static function isPostApprovalRequired(): bool
    {
        return self::get('require_post_approval', '1') === '1';
    }

    public static function isCommentApprovalRequired(): bool
    {
        return self::get('require_comment_approval', '0') === '1';
    }
}
