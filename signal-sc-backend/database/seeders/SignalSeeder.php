<?php

namespace Database\Seeders;

use App\Models\Signal;
use Illuminate\Database\Seeder;

class SignalSeeder extends Seeder
{
    public function run(): void
    {
        $signals = [
            [
                'no' => 1,
                'date' => '2026-08-09',
                'pair' => 'EUR/USD',
                'direction' => 'SELL',
                'entry1' => 1.0850,
                'entry2' => null,
                'sl' => 1.0880,
                'tp1' => 1.0830,
                'tp2' => 1.0810,
                'tp3' => 1.0790,
                'tp4' => 1.0770,
                'pips' => 80,
                'profit' => 80.00,
                'result' => 'WIN',
                'channel' => 'VIP',
                'hit_level' => '4',
                'status' => 'active',
                'thumbs_count' => 45,
                'fire_count' => 89,
                'rocket_count' => 34,
                'broken_heart_count' => 0,
                'user_id' => 1,
            ],
            [
                'no' => 2,
                'date' => '2026-08-09',
                'pair' => 'XAU/USD',
                'direction' => 'BUY',
                'entry1' => 4122.00,
                'entry2' => 4120.00,
                'sl' => 4115.00,
                'tp1' => 4125.00,
                'tp2' => 4130.00,
                'tp3' => 4135.00,
                'tp4' => 4140.00,
                'pips' => 100,
                'profit' => 100.00,
                'result' => 'WIN',
                'channel' => 'VIP',
                'hit_level' => '2',
                'status' => 'active',
                'thumbs_count' => 15,
                'fire_count' => 28,
                'rocket_count' => 12,
                'broken_heart_count' => 0,
                'user_id' => 1,
            ],
            [
                'no' => 3,
                'date' => '2026-08-05',
                'pair' => 'GBP/JPY',
                'direction' => 'SELL',
                'entry1' => 188.50,
                'entry2' => null,
                'sl' => 189.00,
                'tp1' => 188.20,
                'tp2' => 187.80,
                'tp3' => 187.40,
                'tp4' => 187.00,
                'pips' => -50,
                'profit' => -50.00,
                'result' => 'LOSS',
                'channel' => 'VIP',
                'hit_level' => 'SL',
                'status' => 'active',
                'thumbs_count' => 0,
                'fire_count' => 0,
                'rocket_count' => 0,
                'broken_heart_count' => 14,
                'user_id' => 1,
            ],
        ];

        foreach ($signals as $signal) {
            Signal::updateOrCreate(
                ['no' => $signal['no']],
                $signal
            );
        }
    }
}
