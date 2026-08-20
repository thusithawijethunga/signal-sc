<?php

namespace Database\Seeders;

use App\Models\AccountBalance;
use App\Models\CommunityComment;
use App\Models\CommunityPost;
use App\Models\IbMember;
use App\Models\IbPartner;
use App\Models\MarketNews;
use App\Models\Signal;
use App\Models\Trade;
use App\Models\User;
use App\Models\VipMember;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // ─── 1. Admin User ───────────────────────────────────────────
        $admin = User::create([
            'name'     => 'Admin',
            'email'    => 'admin@signalxpress.com',
            'password' => Hash::make('password'),
            'role'     => 'admin',
        ]);

        // ─── 2. IB Partners ─────────────────────────────────────────
        $partnerNames = [
            'Tharindu',
            'Malaka',
            'Asanka',
            'Wajira',
            'Nadun',
            'Wijenayaka Sir',
            'Tharanga Akka',
            'Menala',
        ];

        $partners = [];
        foreach ($partnerNames as $name) {
            $partners[$name] = IbPartner::create(['name' => $name]);
        }

        // ─── 3. Signals ─────────────────────────────────────────────
        $signal1 = Signal::create([
            'no'        => 1,
            'date'      => '2026-01-02',
            'pair'      => 'XAU/USD',
            'direction' => 'sell',
            'entry1'    => 2350.00,
            'entry2'    => 2355.00,
            'sl'        => 2365.00,
            'tp1'       => 2340.00,
            'tp2'       => 2330.00,
            'tp3'       => 2320.00,
            'tp4'       => 2310.00,
            'pips'      => 870.0,
            'profit'    => 84.00,
            'result'    => 'win',
            'channel'   => 'FREE',
            'user_id'   => $admin->id,
        ]);

        $signal2 = Signal::create([
            'no'        => 2,
            'date'      => '2026-01-02',
            'pair'      => 'XAU/USD',
            'direction' => 'buy',
            'entry1'    => 2360.00,
            'entry2'    => null,
            'sl'        => 2350.00,
            'tp1'       => 2370.00,
            'tp2'       => 2380.00,
            'tp3'       => null,
            'tp4'       => null,
            'pips'      => 130.0,
            'profit'    => 13.00,
            'result'    => 'win',
            'channel'   => 'VIP',
            'user_id'   => $admin->id,
        ]);

        $signal3 = Signal::create([
            'no'        => 3,
            'date'      => '2026-01-02',
            'pair'      => 'XAU/USD',
            'direction' => 'buy',
            'entry1'    => 2340.00,
            'entry2'    => 2335.00,
            'sl'        => 2325.00,
            'tp1'       => 2350.00,
            'tp2'       => 2360.00,
            'tp3'       => 2370.00,
            'tp4'       => null,
            'pips'      => 158.0,
            'profit'    => 15.00,
            'result'    => 'win',
            'channel'   => 'VIP',
            'user_id'   => $admin->id,
        ]);

        // ─── 4. Trades (matching signals) ──────────────────────────
        Trade::create([
            'no'        => $signal1->no,
            'date'      => $signal1->date,
            'pair'      => $signal1->pair,
            'direction' => $signal1->direction,
            'entry1'    => $signal1->entry1,
            'entry2'    => $signal1->entry2,
            'sl'        => $signal1->sl,
            'tp1'       => $signal1->tp1,
            'tp2'       => $signal1->tp2,
            'tp3'       => $signal1->tp3,
            'tp4'       => $signal1->tp4,
            'pips'      => $signal1->pips,
            'profit'    => $signal1->profit,
            'result'    => $signal1->result,
            'channel'   => $signal1->channel,
            'user_id'   => $admin->id,
        ]);

        Trade::create([
            'no'        => $signal2->no,
            'date'      => $signal2->date,
            'pair'      => $signal2->pair,
            'direction' => $signal2->direction,
            'entry1'    => $signal2->entry1,
            'entry2'    => $signal2->entry2,
            'sl'        => $signal2->sl,
            'tp1'       => $signal2->tp1,
            'tp2'       => $signal2->tp2,
            'tp3'       => $signal2->tp3,
            'tp4'       => $signal2->tp4,
            'pips'      => $signal2->pips,
            'profit'    => $signal2->profit,
            'result'    => $signal2->result,
            'channel'   => $signal2->channel,
            'user_id'   => $admin->id,
        ]);

        Trade::create([
            'no'        => $signal3->no,
            'date'      => $signal3->date,
            'pair'      => $signal3->pair,
            'direction' => $signal3->direction,
            'entry1'    => $signal3->entry1,
            'entry2'    => $signal3->entry2,
            'sl'        => $signal3->sl,
            'tp1'       => $signal3->tp1,
            'tp2'       => $signal3->tp2,
            'tp3'       => $signal3->tp3,
            'tp4'       => $signal3->tp4,
            'pips'      => $signal3->pips,
            'profit'    => $signal3->profit,
            'result'    => $signal3->result,
            'channel'   => $signal3->channel,
            'user_id'   => $admin->id,
        ]);

        // ─── 5. Account Balance ─────────────────────────────────────
        AccountBalance::create([
            'user_id'          => $admin->id,
            'start_balance'    => 1000.00,
            'deposit_balance'  => 0.00,
            'withdraw_balance' => 0.00,
        ]);

        // ─── 6. IB Members ──────────────────────────────────────────
        $ibMembers = [
            [
                'sx_id'      => 'SX00001',
                'name'       => 'Kumara Perera',
                'broker'     => 'XM',
                'account_id' => 'ACC001',
                'nic'        => '92xxxxxxx',
                'whatsapp'   => '0771234567',
                'telegram'   => '@kumara',
                'partner_id' => $partners['Tharindu']->id,
            ],
            [
                'sx_id'      => 'SX00002',
                'name'       => 'Silva Fernando',
                'broker'     => 'XM',
                'account_id' => 'ACC002',
                'nic'        => '93xxxxxxx',
                'whatsapp'   => '0771234568',
                'telegram'   => '@silva',
                'partner_id' => $partners['Malaka']->id,
            ],
            [
                'sx_id'      => 'SX00003',
                'name'       => 'Raj Kumar',
                'broker'     => 'XM',
                'account_id' => 'ACC003',
                'nic'        => '91xxxxxxx',
                'whatsapp'   => '0771234569',
                'telegram'   => '@rajk',
                'partner_id' => $partners['Asanka']->id,
            ],
        ];

        foreach ($ibMembers as $member) {
            IbMember::create($member);
        }

        // ─── 7. VIP Members (ranked by lots) ───────────────────────
        $vipMembers = [
            ['rank' => 1,  'name' => 'Dinesh Fernando',     'member_id' => 'VIP001', 'lots' => 152.30, 'period' => 'Jan 2026', 'win_rate' => 78.50, 'total_trades' => 42, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 2,  'name' => 'Amara Silva',          'member_id' => 'VIP002', 'lots' => 138.75, 'period' => 'Jan 2026', 'win_rate' => 75.00, 'total_trades' => 38, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 3,  'name' => 'Sunil Perera',         'member_id' => 'VIP003', 'lots' => 125.50, 'period' => 'Jan 2026', 'win_rate' => 80.00, 'total_trades' => 35, 'broker' => 'XM', 'favorite_pair' => 'EUR/USD'],
            ['rank' => 4,  'name' => 'Nisha Jayawardena',    'member_id' => 'VIP004', 'lots' => 112.20, 'period' => 'Jan 2026', 'win_rate' => 72.50, 'total_trades' => 30, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 5,  'name' => 'Kasun Rajapaksa',      'member_id' => 'VIP005', 'lots' => 98.60,  'period' => 'Jan 2026', 'win_rate' => 76.00, 'total_trades' => 28, 'broker' => 'XM', 'favorite_pair' => 'GBP/USD'],
            ['rank' => 6,  'name' => 'Thilini De Alwis',     'member_id' => 'VIP006', 'lots' => 87.40,  'period' => 'Jan 2026', 'win_rate' => 70.00, 'total_trades' => 25, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 7,  'name' => 'Ruwan Gunasekara',     'member_id' => 'VIP007', 'lots' => 76.90,  'period' => 'Jan 2026', 'win_rate' => 73.50, 'total_trades' => 22, 'broker' => 'XM', 'favorite_pair' => 'USD/JPY'],
            ['rank' => 8,  'name' => 'Madushi Liyanage',     'member_id' => 'VIP008', 'lots' => 65.30,  'period' => 'Jan 2026', 'win_rate' => 68.00, 'total_trades' => 20, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 9,  'name' => 'Chathura Bandara',     'member_id' => 'VIP009', 'lots' => 54.80,  'period' => 'Jan 2026', 'win_rate' => 71.00, 'total_trades' => 18, 'broker' => 'XM', 'favorite_pair' => 'EUR/GBP'],
            ['rank' => 10, 'name' => 'Wasana Kumari',        'member_id' => 'VIP010', 'lots' => 42.15,  'period' => 'Jan 2026', 'win_rate' => 67.50, 'total_trades' => 15, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
        ];

        foreach ($vipMembers as $vip) {
            VipMember::create($vip);
        }

        // ─── 8. Community Posts ─────────────────────────────────────
        $post1 = CommunityPost::create([
            'user_id'            => $admin->id,
            'author_name'        => 'Admin',
            'author_badge'       => 'Verified',
            'author_avatar_hex'  => '#4A90D9',
            'post_type'          => 'profit_card',
            'content'            => 'Great start to the year! XAU/USD SELL LIMIT hit all TPs. +84 pips!',
            'hashtags'           => '#XAUUSD #WIN #SignalXpress',
            'pair'               => 'XAU/USD',
            'trade_type'         => 'SELL LIMIT',
            'entry_price'        => 2350.00,
            'exit_price'         => 2310.00,
            'lot_size'           => 1.00,
            'profit_amount'      => 84.00,
            'pips_gain'          => 870.0,
            'roi_percentage'     => 8.40,
            'broker_name'        => 'XM',
            'card_theme'         => 'green',
            'is_verified_trade'  => true,
            'likes_count'        => 12,
            'fire_count'         => 5,
            'rocket_count'       => 3,
            'comments_count'     => 1,
            'is_pinned'          => true,
        ]);

        $post2 = CommunityPost::create([
            'user_id'            => $admin->id,
            'author_name'        => 'Admin',
            'author_badge'       => 'Verified',
            'author_avatar_hex'  => '#4A90D9',
            'post_type'          => 'discussion',
            'content'            => 'Welcome to Signal Xpress community! Share your trading results and discuss strategies here.',
            'hashtags'           => '#Community #Trading #Forex',
            'likes_count'        => 8,
            'fire_count'         => 2,
            'rocket_count'       => 1,
            'comments_count'     => 1,
        ]);

        $post3 = CommunityPost::create([
            'user_id'            => $admin->id,
            'author_name'        => 'Admin',
            'author_badge'       => 'Verified',
            'author_avatar_hex'  => '#4A90D9',
            'post_type'          => 'screenshot',
            'content'            => 'XAU/USD BUY signal +13 pips. VIP channel delivering results.',
            'hashtags'           => '#XAUUSD #VIP #Signal',
            'pair'               => 'XAU/USD',
            'trade_type'         => 'BUY',
            'entry_price'        => 2360.00,
            'exit_price'         => 2370.00,
            'lot_size'           => 0.50,
            'profit_amount'      => 13.00,
            'pips_gain'          => 130.0,
            'roi_percentage'     => 1.30,
            'broker_name'        => 'XM',
            'card_theme'         => 'blue',
            'is_verified_trade'  => true,
            'likes_count'        => 6,
            'fire_count'         => 3,
            'rocket_count'       => 2,
            'comments_count'     => 0,
        ]);

        // ─── 9. Community Comments ─────────────────────────────────
        CommunityComment::create([
            'post_id'     => $post1->id,
            'user_id'     => $admin->id,
            'author_name' => 'Admin',
            'content'     => 'Amazing result! Let\'s keep the winning streak going.',
            'likes_count' => 3,
        ]);

        CommunityComment::create([
            'post_id'     => $post2->id,
            'user_id'     => $admin->id,
            'author_name' => 'Admin',
            'content'     => 'Feel free to ask any questions about our signal system.',
            'likes_count' => 1,
        ]);

        // ─── 10. Market News ───────────────────────────────────────
        $newsItems = [
            [
                'event_time'  => now()->addDays(3)->setTime(8, 30),
                'currency'    => 'USD',
                'title'       => 'CPI (Consumer Price Index)',
                'impact'      => 'High',
                'forecast'    => '3.2%',
                'previous'    => '3.1%',
                'actual'      => null,
                'description' => 'Measures the average change in prices paid by consumers for goods and services. Key inflation indicator.',
            ],
            [
                'event_time'  => now()->addDays(7)->setTime(14, 0),
                'currency'    => 'USD',
                'title'       => 'FOMC Statement',
                'impact'      => 'High',
                'forecast'    => null,
                'previous'    => null,
                'actual'      => null,
                'description' => 'Federal Open Market Committee statement on monetary policy direction and interest rate decisions.',
            ],
            [
                'event_time'  => now()->addDays(10)->setTime(8, 30),
                'currency'    => 'USD',
                'title'       => 'Non-Farm Payrolls (NFP)',
                'impact'      => 'High',
                'forecast'    => '180K',
                'previous'    => '150K',
                'actual'      => null,
                'description' => 'Measures the number of jobs added in the US economy excluding farm workers. Major market mover.',
            ],
            [
                'event_time'  => now()->addDays(14)->setTime(7, 45),
                'currency'    => 'EUR',
                'title'       => 'ECB Interest Rate Decision',
                'impact'      => 'High',
                'forecast'    => '4.50%',
                'previous'    => '4.50%',
                'actual'      => null,
                'description' => 'European Central Bank decision on main refinancing rate. Impacts EUR pairs significantly.',
            ],
            [
                'event_time'  => now()->addDays(18)->setTime(7, 0),
                'currency'    => 'GBP',
                'title'       => 'BOE Interest Rate Decision',
                'impact'      => 'High',
                'forecast'    => '5.25%',
                'previous'    => '5.25%',
                'actual'      => null,
                'description' => 'Bank of England decision on base rate. Key event for GBP pairs.',
            ],
        ];

        foreach ($newsItems as $news) {
            MarketNews::create($news);
        }
    }
}
