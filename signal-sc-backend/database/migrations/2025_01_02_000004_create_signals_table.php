<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('signals', function (Blueprint $table) {
            $table->id();
            $table->integer('no');
            $table->date('date');
            $table->string('pair')->default('XAU/USD');
            $table->string('direction');
            $table->decimal('entry1', 10, 2)->nullable();
            $table->decimal('entry2', 10, 2)->nullable();
            $table->decimal('sl', 10, 2)->nullable();
            $table->decimal('tp1', 10, 2)->nullable();
            $table->decimal('tp2', 10, 2)->nullable();
            $table->decimal('tp3', 10, 2)->nullable();
            $table->decimal('tp4', 10, 2)->nullable();
            $table->decimal('pips', 10, 1)->default(0);
            $table->decimal('profit', 10, 2)->default(0);
            $table->string('result')->default('RUNNING');
            $table->string('channel')->default('VIP');
            $table->string('hit_level')->nullable();
            $table->string('status')->default('active');
            $table->integer('thumbs_count')->default(0);
            $table->integer('fire_count')->default(0);
            $table->integer('rocket_count')->default(0);
            $table->integer('broken_heart_count')->default(0);
            $table->string('user_reacted_emoji')->nullable();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->timestamps();

            $table->index('pair');
            $table->index('direction');
            $table->index('result');
            $table->index('channel');
            $table->index('date');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('signals');
    }
};
