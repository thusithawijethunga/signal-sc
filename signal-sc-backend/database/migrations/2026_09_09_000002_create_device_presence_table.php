<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Live device presence — one row per app install, refreshed by heartbeat.
     * Rows older than the TTL are treated as gone (app killed / offline).
     */
    public function up(): void
    {
        Schema::create('device_presence', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('device_key', 128);
            $table->json('device')->nullable();
            $table->string('state', 16)->default('online'); // online | background
            $table->timestamp('last_seen_at')->useCurrent();
            $table->timestamps();

            $table->unique(['user_id', 'device_key']);
            $table->index('last_seen_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('device_presence');
    }
};
