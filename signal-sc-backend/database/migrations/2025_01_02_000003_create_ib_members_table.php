<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('ib_members', function (Blueprint $table) {
            $table->id();
            $table->string('sx_id')->unique();
            $table->string('name');
            $table->string('broker')->default('XM');
            $table->string('account_id');
            $table->string('nic')->nullable();
            $table->string('whatsapp')->nullable();
            $table->string('telegram')->nullable();
            $table->foreignId('partner_id')->nullable()->constrained('ib_partners')->nullOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('ib_members');
    }
};
