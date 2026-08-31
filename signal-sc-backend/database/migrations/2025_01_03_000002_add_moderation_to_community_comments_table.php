<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('community_comments', function (Blueprint $table) {
            $table->string('status')->default('approved')->after('user_id');
            $table->timestamp('approved_at')->nullable()->after('status');
            $table->foreignId('approved_by')->nullable()->after('approved_at')->constrained('users')->nullOnDelete();

            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::table('community_comments', function (Blueprint $table) {
            $table->dropIndex(['status']);
            $table->dropColumn(['status', 'approved_at', 'approved_by']);
        });
    }
};
