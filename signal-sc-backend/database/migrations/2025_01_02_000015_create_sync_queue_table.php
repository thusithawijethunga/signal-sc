<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('sync_queue', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->string('device_id')->nullable();
            $table->string('table_name');
            $table->integer('record_id')->nullable();
            $table->string('action');
            $table->json('payload');
            $table->datetime('synced_at')->nullable();
            $table->timestamps();

            $table->index('synced_at');
            $table->index(['table_name', 'record_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('sync_queue');
    }
};
