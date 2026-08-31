<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        $this->call([
            UserSeeder::class,
            SignalSeeder::class,
            MarketNewsSeeder::class,
            CommunitySeeder::class,
            VipMemberSeeder::class,
        ]);
    }
}
