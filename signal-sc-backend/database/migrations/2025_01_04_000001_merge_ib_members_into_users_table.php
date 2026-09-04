<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;
use App\Models\User;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('sx_id')->nullable()->after('role');
            $table->string('broker')->nullable()->after('sx_id');
            $table->string('account_id')->nullable()->after('broker');
            $table->string('nic')->nullable()->after('account_id');
            $table->string('whatsapp')->nullable()->after('nic');
            $table->string('telegram')->nullable()->after('whatsapp');
            $table->unsignedBigInteger('partner_id')->nullable()->after('telegram');
            $table->index('sx_id');
            $table->index('account_id');
        });

        $ibMembers = DB::table('ib_members')->get();

        foreach ($ibMembers as $member) {
            if ($member->user_id) {
                User::where('id', $member->user_id)->update([
                    'sx_id' => $member->sx_id,
                    'broker' => $member->broker,
                    'account_id' => $member->account_id,
                    'nic' => $member->nic,
                    'whatsapp' => $member->whatsapp,
                    'telegram' => $member->telegram,
                    'partner_id' => $member->partner_id,
                    'role' => 'ib_member',
                ]);
            } else {
                $email = 'ib' . $member->account_id . '@signalxpress.local';
                $user = User::create([
                    'name' => $member->name,
                    'email' => $email,
                    'password' => 'SignalXp',
                    'role' => 'ib_member',
                    'sx_id' => $member->sx_id,
                    'broker' => $member->broker,
                    'account_id' => $member->account_id,
                    'nic' => $member->nic,
                    'whatsapp' => $member->whatsapp,
                    'telegram' => $member->telegram,
                    'partner_id' => $member->partner_id,
                ]);

                DB::table('ib_members')->where('id', $member->id)->update(['user_id' => $user->id]);
            }
        }

        Schema::dropIfExists('ib_members');
    }

    public function down(): void
    {
        Schema::create('ib_members', function (Blueprint $table) {
            $table->id();
            $table->string('sx_id')->unique();
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();
            $table->string('name');
            $table->string('broker')->default('XM');
            $table->string('account_id');
            $table->string('nic')->nullable();
            $table->string('whatsapp')->nullable();
            $table->string('telegram')->nullable();
            $table->foreignId('partner_id')->nullable()->constrained()->nullOnDelete();
            $table->timestamps();
        });

        $users = User::where('role', 'ib_member')->get();
        foreach ($users as $user) {
            DB::table('ib_members')->insert([
                'sx_id' => $user->sx_id,
                'user_id' => $user->id,
                'name' => $user->name,
                'broker' => $user->broker,
                'account_id' => $user->account_id,
                'nic' => $user->nic,
                'whatsapp' => $user->whatsapp,
                'telegram' => $user->telegram,
                'partner_id' => $user->partner_id,
                'created_at' => $user->created_at,
                'updated_at' => $user->updated_at,
            ]);
        }

        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['sx_id', 'broker', 'account_id', 'nic', 'whatsapp', 'telegram', 'partner_id']);
        });
    }
};
