<?php

namespace Database\Seeders;

use App\Models\VipMember;
use Illuminate\Database\Seeder;

class VipMemberSeeder extends Seeder
{
    public function run(): void
    {
        $members = [
            [
                'rank' => 1,
                'name' => 'Prabath manjula',
                'member_id' => 'SX1043',
                'lots' => 81.15,
                'progress_fraction' => 0.88,
                'accent_hex' => '#F59E0B',
                'period' => 'MONTHLY',
                'win_rate' => 86.4,
                'total_trades' => 128,
                'broker' => 'Exness Raw Spread',
                'favorite_pair' => 'XAU/USD',
            ],
            [
                'rank' => 2,
                'name' => 'Unknown',
                'member_id' => '—',
                'lots' => 36.66,
                'progress_fraction' => 0.44,
                'accent_hex' => '#E2E8F0',
                'period' => 'MONTHLY',
                'win_rate' => 79.2,
                'total_trades' => 64,
                'broker' => 'XM Ultra Low',
                'favorite_pair' => 'EUR/USD',
            ],
            [
                'rank' => 3,
                'name' => 'Unknown',
                'member_id' => '—',
                'lots' => 21.13,
                'progress_fraction' => 0.26,
                'accent_hex' => '#F97316',
                'period' => 'MONTHLY',
                'win_rate' => 75.0,
                'total_trades' => 48,
                'broker' => 'IC Markets Pro',
                'favorite_pair' => 'GBP/USD',
            ],
            [
                'rank' => 4,
                'name' => 'Asitha lakmal',
                'member_id' => 'SX1029',
                'lots' => 17.62,
                'progress_fraction' => 0.21,
                'accent_hex' => '#6366F1',
                'period' => 'MONTHLY',
                'win_rate' => 81.5,
                'total_trades' => 39,
                'broker' => 'Exness Pro',
                'favorite_pair' => 'XAU/USD',
            ],
            [
                'rank' => 5,
                'name' => 'Tharindu manoj',
                'member_id' => 'SX1036',
                'lots' => 13.86,
                'progress_fraction' => 0.17,
                'accent_hex' => '#D946EF',
                'period' => 'MONTHLY',
                'win_rate' => 72.8,
                'total_trades' => 31,
                'broker' => 'Pepperstone Razor',
                'favorite_pair' => 'USD/JPY',
            ],
            [
                'rank' => 6,
                'name' => 'Unknown',
                'member_id' => '—',
                'lots' => 11.07,
                'progress_fraction' => 0.13,
                'accent_hex' => '#10B981',
                'period' => 'MONTHLY',
                'win_rate' => 68.4,
                'total_trades' => 25,
                'broker' => 'Exness Standard',
                'favorite_pair' => 'XAU/USD',
            ],
            [
                'rank' => 7,
                'name' => 'Unknown',
                'member_id' => '—',
                'lots' => 10.10,
                'progress_fraction' => 0.12,
                'accent_hex' => '#EF4444',
                'period' => 'MONTHLY',
                'win_rate' => 70.0,
                'total_trades' => 22,
                'broker' => 'XM Standard',
                'favorite_pair' => 'GBP/JPY',
            ],
            [
                'rank' => 8,
                'name' => 'Roshan',
                'member_id' => 'SX1081',
                'lots' => 9.63,
                'progress_fraction' => 0.11,
                'accent_hex' => '#06B6D4',
                'period' => 'MONTHLY',
                'win_rate' => 76.5,
                'total_trades' => 19,
                'broker' => 'Exness Pro',
                'favorite_pair' => 'EUR/USD',
            ],
            [
                'rank' => 9,
                'name' => 'Vidya Karunaratne',
                'member_id' => 'SX1003',
                'lots' => 7.17,
                'progress_fraction' => 0.09,
                'accent_hex' => '#EAB308',
                'period' => 'MONTHLY',
                'win_rate' => 83.3,
                'total_trades' => 16,
                'broker' => 'IC Markets',
                'favorite_pair' => 'XAU/USD',
            ],
            [
                'rank' => 10,
                'name' => 'Unknown',
                'member_id' => '—',
                'lots' => 6.06,
                'progress_fraction' => 0.07,
                'accent_hex' => '#8B5CF6',
                'period' => 'MONTHLY',
                'win_rate' => 66.7,
                'total_trades' => 14,
                'broker' => 'Exness Standard',
                'favorite_pair' => 'AUD/USD',
            ],
        ];

        foreach ($members as $member) {
            VipMember::updateOrCreate(
                ['member_id' => $member['member_id'], 'period' => $member['period']],
                $member
            );
        }
    }
}
