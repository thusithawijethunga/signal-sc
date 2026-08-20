<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('csv_trade_records', function (Blueprint $table) {
            $table->id();
            $table->foreignId('csv_upload_id')->constrained()->cascadeOnDelete();
            $table->string('account_id');
            $table->string('symbol');
            $table->decimal('lots', 10, 4);
            $table->decimal('commission', 10, 2)->default(0);
            $table->timestamps();

            $table->index('csv_upload_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('csv_trade_records');
    }
};
