<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('community_settings', function (Blueprint $table) {
            $table->id();
            $table->string('key')->unique();
            $table->text('value')->nullable();
            $table->text('description')->nullable();
            $table->timestamps();
        });

        // Seed default settings
        DB::table('community_settings')->insert([
            ['key' => 'require_post_approval', 'value' => '1', 'description' => 'Admin must approve new posts before they are visible to all users', 'created_at' => now(), 'updated_at' => now()],
            ['key' => 'require_comment_approval', 'value' => '0', 'description' => 'Admin must approve new comments before they are visible', 'created_at' => now(), 'updated_at' => now()],
            ['key' => 'max_post_length', 'value' => '5000', 'description' => 'Maximum characters per post', 'created_at' => now(), 'updated_at' => now()],
        ]);
    }

    public function down(): void
    {
        Schema::dropIfExists('community_settings');
    }
};
