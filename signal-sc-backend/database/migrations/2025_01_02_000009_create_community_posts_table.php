<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('community_posts', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->string('author_name');
            $table->string('author_badge')->nullable();
            $table->string('author_avatar_hex')->nullable();
            $table->string('post_type');
            $table->text('content')->nullable();
            $table->string('hashtags')->nullable();
            $table->text('image_uri')->nullable();
            $table->string('pair')->nullable();
            $table->string('trade_type')->nullable();
            $table->decimal('entry_price', 10, 2)->nullable();
            $table->decimal('exit_price', 10, 2)->nullable();
            $table->decimal('lot_size', 10, 2)->nullable();
            $table->decimal('profit_amount', 10, 2)->nullable();
            $table->decimal('pips_gain', 10, 1)->nullable();
            $table->decimal('roi_percentage', 10, 2)->nullable();
            $table->string('broker_name')->nullable();
            $table->string('card_theme')->nullable();
            $table->boolean('is_verified_trade')->default(false);
            $table->integer('likes_count')->default(0);
            $table->integer('fire_count')->default(0);
            $table->integer('rocket_count')->default(0);
            $table->integer('comments_count')->default(0);
            $table->boolean('is_liked_by_me')->default(false);
            $table->boolean('is_fired_by_me')->default(false);
            $table->boolean('is_rocket_by_me')->default(false);
            $table->boolean('is_pinned')->default(false);
            $table->timestamps();

            $table->index('post_type');
            $table->index('pair');
            $table->index('is_pinned');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('community_posts');
    }
};
