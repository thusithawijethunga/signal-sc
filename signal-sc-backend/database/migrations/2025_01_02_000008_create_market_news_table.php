<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('market_news', function (Blueprint $table) {
            $table->id();
            $table->datetime('event_time')->nullable();
            $table->string('currency');
            $table->string('title');
            $table->string('impact');
            $table->string('forecast')->nullable();
            $table->string('previous')->nullable();
            $table->string('actual')->nullable();
            $table->text('description')->nullable();
            $table->timestamps();

            $table->index('event_time');
            $table->index('impact');
            $table->index('currency');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('market_news');
    }
};
