<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('community_posts', function (Blueprint $table) {
            $table->string('status')->default('approved')->after('user_id');
            $table->boolean('comments_need_review')->default(false)->after('is_pinned');
            $table->timestamp('approved_at')->nullable()->after('status');
            $table->foreignId('approved_by')->nullable()->after('approved_at')->constrained('users')->nullOnDelete();
            $table->text('rejection_reason')->nullable()->after('approved_by');

            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::table('community_posts', function (Blueprint $table) {
            $table->dropIndex(['status']);
            $table->dropColumn([
                'status', 'comments_need_review', 'approved_at',
                'approved_by', 'rejection_reason'
            ]);
        });
    }
};
