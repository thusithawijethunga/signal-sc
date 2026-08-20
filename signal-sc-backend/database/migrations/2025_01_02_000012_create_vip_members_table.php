<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('vip_members', function (Blueprint $table) {
            $table->id();
            $table->integer('rank');
            $table->string('name');
            $table->string('member_id')->unique();
            $table->decimal('lots', 10, 2);
            $table->decimal('progress_fraction', 5, 4)->default(0);
            $table->string('accent_hex')->nullable();
            $table->string('period');
            $table->decimal('win_rate', 5, 2)->nullable();
            $table->integer('total_trades')->default(0);
            $table->string('broker')->nullable();
            $table->string('favorite_pair')->nullable();
            $table->timestamps();

            $table->index('period');
            $table->index('rank');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('vip_members');
    }
};
