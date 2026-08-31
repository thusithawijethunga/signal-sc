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
use Illuminate\Support\Str;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // ═══════════════════════════════════════════════════════════════
        // 1. USERS
        // ═══════════════════════════════════════════════════════════════
        $admin = User::create([
            'name' => 'Admin',
            'email' => 'admin@signalxpress.com',
            'password' => Hash::make('password'),
            'role' => 'admin',
            'api_token' => Str::random(60),
        ]);

        $trader = User::create([
            'name' => 'Kasun Perera',
            'email' => 'trader@signalxpress.com',
            'password' => Hash::make('password'),
            'role' => 'viewer',
            'api_token' => Str::random(60),
        ]);

        $demoUsers = [
            ['name' => 'Dinuka Silva', 'email' => 'dinuka@signalxpress.com'],
            ['name' => 'Ruwan Chamara', 'email' => 'ruwan@signalxpress.com'],
            ['name' => 'Ashan Wijesinghe', 'email' => 'ashan@signalxpress.com'],
        ];
        foreach ($demoUsers as $u) {
            User::create([
                'name' => $u['name'],
                'email' => $u['email'],
                'password' => Hash::make('password'),
                'role' => 'viewer',
                'api_token' => Str::random(60),
            ]);
        }

        // ═══════════════════════════════════════════════════════════════
        // 2. IB PARTNERS
        // ═══════════════════════════════════════════════════════════════
        $partnerNames = [
            'Tharindu', 'Malaka', 'Asanka', 'Wajira',
            'Nadun', 'Wijenayaka Sir', 'Tharanga Akka', 'Menala',
        ];
        $partners = [];
        foreach ($partnerNames as $name) {
            $partners[$name] = IbPartner::create(['name' => $name]);
        }

        // ═══════════════════════════════════════════════════════════════
        // 3. SIGNALS (mobile app demo + old backend data)
        // ═══════════════════════════════════════════════════════════════
        // --- Mobile app seed data (3 signals) ---
        Signal::create([
            'no' => 1, 'date' => '2026-08-09', 'pair' => 'EUR/USD', 'direction' => 'SELL',
            'entry1' => 1.0850, 'entry2' => null, 'sl' => 1.0880,
            'tp1' => 1.0830, 'tp2' => 1.0810, 'tp3' => 1.0790, 'tp4' => 1.0770,
            'pips' => 80, 'profit' => 80.00, 'result' => 'WIN', 'channel' => 'VIP',
            'hit_level' => '4', 'status' => 'active',
            'thumbs_count' => 45, 'fire_count' => 89, 'rocket_count' => 34, 'broken_heart_count' => 0,
            'user_id' => $admin->id,
        ]);

        Signal::create([
            'no' => 2, 'date' => '2026-08-09', 'pair' => 'XAU/USD', 'direction' => 'BUY',
            'entry1' => 4122.00, 'entry2' => 4120.00, 'sl' => 4115.00,
            'tp1' => 4125.00, 'tp2' => 4130.00, 'tp3' => 4135.00, 'tp4' => 4140.00,
            'pips' => 100, 'profit' => 100.00, 'result' => 'WIN', 'channel' => 'VIP',
            'hit_level' => '2', 'status' => 'active',
            'thumbs_count' => 15, 'fire_count' => 28, 'rocket_count' => 12, 'broken_heart_count' => 0,
            'user_id' => $admin->id,
        ]);

        Signal::create([
            'no' => 3, 'date' => '2026-08-05', 'pair' => 'GBP/JPY', 'direction' => 'SELL',
            'entry1' => 188.50, 'entry2' => null, 'sl' => 189.00,
            'tp1' => 188.20, 'tp2' => 187.80, 'tp3' => 187.40, 'tp4' => 187.00,
            'pips' => -50, 'profit' => -50.00, 'result' => 'LOSS', 'channel' => 'VIP',
            'hit_level' => 'SL', 'status' => 'active',
            'thumbs_count' => 0, 'fire_count' => 0, 'rocket_count' => 0, 'broken_heart_count' => 14,
            'user_id' => $admin->id,
        ]);

        // --- Old backend seed data (3 extra signals) ---
        Signal::create([
            'no' => 4, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'SELL',
            'entry1' => 2350.00, 'entry2' => 2355.00, 'sl' => 2365.00,
            'tp1' => 2340.00, 'tp2' => 2330.00, 'tp3' => 2320.00, 'tp4' => 2310.00,
            'pips' => 870, 'profit' => 84.00, 'result' => 'WIN', 'channel' => 'FREE',
            'hit_level' => '4', 'status' => 'active',
            'user_id' => $admin->id,
        ]);

        Signal::create([
            'no' => 5, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'BUY',
            'entry1' => 2360.00, 'entry2' => null, 'sl' => 2350.00,
            'tp1' => 2370.00, 'tp2' => 2380.00, 'tp3' => null, 'tp4' => null,
            'pips' => 130, 'profit' => 13.00, 'result' => 'WIN', 'channel' => 'VIP',
            'hit_level' => '2', 'status' => 'active',
            'user_id' => $admin->id,
        ]);

        Signal::create([
            'no' => 6, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'BUY',
            'entry1' => 2340.00, 'entry2' => 2335.00, 'sl' => 2325.00,
            'tp1' => 2350.00, 'tp2' => 2360.00, 'tp3' => 2370.00, 'tp4' => null,
            'pips' => 158, 'profit' => 15.00, 'result' => 'WIN', 'channel' => 'VIP',
            'hit_level' => '3', 'status' => 'active',
            'user_id' => $admin->id,
        ]);

        // ═══════════════════════════════════════════════════════════════
        // 4. TRADES
        // ═══════════════════════════════════════════════════════════════
        $tradesData = [
            ['no' => 1, 'date' => '2026-08-09', 'pair' => 'EUR/USD', 'direction' => 'SELL', 'entry1' => 1.0850, 'sl' => 1.0880, 'tp1' => 1.0830, 'tp2' => 1.0810, 'tp3' => 1.0790, 'tp4' => 1.0770, 'pips' => 80, 'profit' => 80.00, 'result' => 'WIN', 'channel' => 'VIP'],
            ['no' => 2, 'date' => '2026-08-09', 'pair' => 'XAU/USD', 'direction' => 'BUY', 'entry1' => 4122.00, 'entry2' => 4120.00, 'sl' => 4115.00, 'tp1' => 4125.00, 'tp2' => 4130.00, 'tp3' => 4135.00, 'tp4' => 4140.00, 'pips' => 100, 'profit' => 100.00, 'result' => 'WIN', 'channel' => 'VIP'],
            ['no' => 3, 'date' => '2026-08-05', 'pair' => 'GBP/JPY', 'direction' => 'SELL', 'entry1' => 188.50, 'sl' => 189.00, 'tp1' => 188.20, 'tp2' => 187.80, 'tp3' => 187.40, 'tp4' => 187.00, 'pips' => -50, 'profit' => -50.00, 'result' => 'LOSS', 'channel' => 'VIP'],
            ['no' => 4, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'SELL', 'entry1' => 2350.00, 'entry2' => 2355.00, 'sl' => 2365.00, 'tp1' => 2340.00, 'tp2' => 2330.00, 'tp3' => 2320.00, 'tp4' => 2310.00, 'pips' => 870, 'profit' => 84.00, 'result' => 'WIN', 'channel' => 'FREE'],
            ['no' => 5, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'BUY', 'entry1' => 2360.00, 'sl' => 2350.00, 'tp1' => 2370.00, 'tp2' => 2380.00, 'pips' => 130, 'profit' => 13.00, 'result' => 'WIN', 'channel' => 'VIP'],
            ['no' => 6, 'date' => '2026-01-02', 'pair' => 'XAU/USD', 'direction' => 'BUY', 'entry1' => 2340.00, 'entry2' => 2335.00, 'sl' => 2325.00, 'tp1' => 2350.00, 'tp2' => 2360.00, 'tp3' => 2370.00, 'pips' => 158, 'profit' => 15.00, 'result' => 'WIN', 'channel' => 'VIP'],
        ];
        foreach ($tradesData as $t) {
            Trade::create(array_merge($t, ['user_id' => $admin->id]));
        }

        // ═══════════════════════════════════════════════════════════════
        // 5. ACCOUNT BALANCE
        // ═══════════════════════════════════════════════════════════════
        AccountBalance::create([
            'user_id' => $admin->id,
            'start_balance' => 1000.00,
            'deposit_balance' => 0.00,
            'withdraw_balance' => 0.00,
        ]);

        // ═══════════════════════════════════════════════════════════════
        // 6. IB MEMBERS
        // ═══════════════════════════════════════════════════════════════
        $ibMembers = [
            ['sx_id' => 'SX00001', 'name' => 'Kumara Perera', 'broker' => 'XM', 'account_id' => 'ACC001', 'nic' => '92xxxxxxx', 'whatsapp' => '0771234567', 'telegram' => '@kumara', 'partner_id' => $partners['Tharindu']->id],
            ['sx_id' => 'SX00002', 'name' => 'Silva Fernando', 'broker' => 'XM', 'account_id' => 'ACC002', 'nic' => '93xxxxxxx', 'whatsapp' => '0771234568', 'telegram' => '@silva', 'partner_id' => $partners['Malaka']->id],
            ['sx_id' => 'SX00003', 'name' => 'Raj Kumar', 'broker' => 'XM', 'account_id' => 'ACC003', 'nic' => '91xxxxxxx', 'whatsapp' => '0771234569', 'telegram' => '@rajk', 'partner_id' => $partners['Asanka']->id],
        ];
        foreach ($ibMembers as $member) {
            IbMember::create($member);
        }

        // ═══════════════════════════════════════════════════════════════
        // 7. VIP MEMBERS (mobile app top 10 + old backend)
        // ═══════════════════════════════════════════════════════════════
        // --- Mobile app seed (MONTHLY) ---
        $vipMembers = [
            ['rank' => 1, 'name' => 'Prabath manjula', 'member_id' => 'SX1043', 'lots' => 81.15, 'progress_fraction' => 0.88, 'accent_hex' => '#F59E0B', 'period' => 'MONTHLY', 'win_rate' => 86.4, 'total_trades' => 128, 'broker' => 'Exness Raw Spread', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 2, 'name' => 'Unknown', 'member_id' => 'SX-M002', 'lots' => 36.66, 'progress_fraction' => 0.44, 'accent_hex' => '#E2E8F0', 'period' => 'MONTHLY', 'win_rate' => 79.2, 'total_trades' => 64, 'broker' => 'XM Ultra Low', 'favorite_pair' => 'EUR/USD'],
            ['rank' => 3, 'name' => 'Unknown', 'member_id' => 'SX-M003', 'lots' => 21.13, 'progress_fraction' => 0.26, 'accent_hex' => '#F97316', 'period' => 'MONTHLY', 'win_rate' => 75.0, 'total_trades' => 48, 'broker' => 'IC Markets Pro', 'favorite_pair' => 'GBP/USD'],
            ['rank' => 4, 'name' => 'Asitha lakmal', 'member_id' => 'SX1029', 'lots' => 17.62, 'progress_fraction' => 0.21, 'accent_hex' => '#6366F1', 'period' => 'MONTHLY', 'win_rate' => 81.5, 'total_trades' => 39, 'broker' => 'Exness Pro', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 5, 'name' => 'Tharindu manoj', 'member_id' => 'SX1036', 'lots' => 13.86, 'progress_fraction' => 0.17, 'accent_hex' => '#D946EF', 'period' => 'MONTHLY', 'win_rate' => 72.8, 'total_trades' => 31, 'broker' => 'Pepperstone Razor', 'favorite_pair' => 'USD/JPY'],
            ['rank' => 6, 'name' => 'Unknown', 'member_id' => 'SX-M006', 'lots' => 11.07, 'progress_fraction' => 0.13, 'accent_hex' => '#10B981', 'period' => 'MONTHLY', 'win_rate' => 68.4, 'total_trades' => 25, 'broker' => 'Exness Standard', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 7, 'name' => 'Unknown', 'member_id' => 'SX-M007', 'lots' => 10.10, 'progress_fraction' => 0.12, 'accent_hex' => '#EF4444', 'period' => 'MONTHLY', 'win_rate' => 70.0, 'total_trades' => 22, 'broker' => 'XM Standard', 'favorite_pair' => 'GBP/JPY'],
            ['rank' => 8, 'name' => 'Roshan', 'member_id' => 'SX1081', 'lots' => 9.63, 'progress_fraction' => 0.11, 'accent_hex' => '#06B6D4', 'period' => 'MONTHLY', 'win_rate' => 76.5, 'total_trades' => 19, 'broker' => 'Exness Pro', 'favorite_pair' => 'EUR/USD'],
            ['rank' => 9, 'name' => 'Vidya Karunaratne', 'member_id' => 'SX1003', 'lots' => 7.17, 'progress_fraction' => 0.09, 'accent_hex' => '#EAB308', 'period' => 'MONTHLY', 'win_rate' => 83.3, 'total_trades' => 16, 'broker' => 'IC Markets', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 10, 'name' => 'Unknown', 'member_id' => 'SX-M010', 'lots' => 6.06, 'progress_fraction' => 0.07, 'accent_hex' => '#8B5CF6', 'period' => 'MONTHLY', 'win_rate' => 66.7, 'total_trades' => 14, 'broker' => 'Exness Standard', 'favorite_pair' => 'AUD/USD'],
        ];

        // --- Old backend seed (Jan 2026) ---
        $oldVip = [
            ['rank' => 1, 'name' => 'Dinesh Fernando', 'member_id' => 'VIP001', 'lots' => 152.30, 'period' => 'Jan 2026', 'win_rate' => 78.50, 'total_trades' => 42, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 2, 'name' => 'Amara Silva', 'member_id' => 'VIP002', 'lots' => 138.75, 'period' => 'Jan 2026', 'win_rate' => 75.00, 'total_trades' => 38, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 3, 'name' => 'Sunil Perera', 'member_id' => 'VIP003', 'lots' => 125.50, 'period' => 'Jan 2026', 'win_rate' => 80.00, 'total_trades' => 35, 'broker' => 'XM', 'favorite_pair' => 'EUR/USD'],
            ['rank' => 4, 'name' => 'Nisha Jayawardena', 'member_id' => 'VIP004', 'lots' => 112.20, 'period' => 'Jan 2026', 'win_rate' => 72.50, 'total_trades' => 30, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 5, 'name' => 'Kasun Rajapaksa', 'member_id' => 'VIP005', 'lots' => 98.60, 'period' => 'Jan 2026', 'win_rate' => 76.00, 'total_trades' => 28, 'broker' => 'XM', 'favorite_pair' => 'GBP/USD'],
            ['rank' => 6, 'name' => 'Thilini De Alwis', 'member_id' => 'VIP006', 'lots' => 87.40, 'period' => 'Jan 2026', 'win_rate' => 70.00, 'total_trades' => 25, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 7, 'name' => 'Ruwan Gunasekara', 'member_id' => 'VIP007', 'lots' => 76.90, 'period' => 'Jan 2026', 'win_rate' => 73.50, 'total_trades' => 22, 'broker' => 'XM', 'favorite_pair' => 'USD/JPY'],
            ['rank' => 8, 'name' => 'Madushi Liyanage', 'member_id' => 'VIP008', 'lots' => 65.30, 'period' => 'Jan 2026', 'win_rate' => 68.00, 'total_trades' => 20, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
            ['rank' => 9, 'name' => 'Chathura Bandara', 'member_id' => 'VIP009', 'lots' => 54.80, 'period' => 'Jan 2026', 'win_rate' => 71.00, 'total_trades' => 18, 'broker' => 'XM', 'favorite_pair' => 'EUR/GBP'],
            ['rank' => 10, 'name' => 'Wasana Kumari', 'member_id' => 'VIP010', 'lots' => 42.15, 'period' => 'Jan 2026', 'win_rate' => 67.50, 'total_trades' => 15, 'broker' => 'XM', 'favorite_pair' => 'XAU/USD'],
        ];

        foreach (array_merge($vipMembers, $oldVip) as $vip) {
            VipMember::create($vip);
        }

        // ═══════════════════════════════════════════════════════════════
        // 8. COMMUNITY POSTS (mobile app 4 + old backend 3)
        // ═══════════════════════════════════════════════════════════════
        // --- Mobile app seed ---
        CommunityPost::create([
            'user_id' => $trader->id, 'status' => 'approved',
            'author_name' => 'Kasun Perera', 'author_badge' => 'VIP Master Trader',
            'author_avatar_hex' => '0xFFF59E0B', 'post_type' => 'screenshot',
            'content' => 'Gold signal #2 was pure sniper entry! Hit TP2 effortlessly and locked +$1,650 in profit! Huge gratitude to Signal Xpress team! 🚀🔥',
            'hashtags' => '#XAUUSD #TP2Hit #ForexProfit #Screenshot',
            'image_uri' => 'res://drawable/img_gold_profit_shot',
            'pair' => 'XAU/USD', 'trade_type' => 'BUY',
            'entry_price' => 4122.00, 'exit_price' => 4138.50, 'lot_size' => 1.00,
            'profit_amount' => 1650.00, 'pips_gain' => 165, 'roi_percentage' => 165.00,
            'broker_name' => 'Exness Pro', 'card_theme' => 'GOLD_LUXURY',
            'is_verified_trade' => true, 'likes_count' => 124, 'fire_count' => 88,
            'rocket_count' => 52, 'comments_count' => 3, 'is_pinned' => true,
            'approved_at' => now()->subHours(2),
        ]);

        CommunityPost::create([
            'user_id' => 3, 'status' => 'approved',
            'author_name' => 'Dinuka Silva', 'author_badge' => 'Pro Scalper',
            'author_avatar_hex' => '0xFF10B981', 'post_type' => 'screenshot',
            'content' => 'EUR/USD Technical breakdown on 15M chart! Clean support retest and sell continuation as predicted.',
            'hashtags' => '#EURUSD #PriceAction #ChartAnalysis',
            'image_uri' => 'res://drawable/img_chart_analysis_shot',
            'pair' => 'EUR/USD', 'trade_type' => 'SELL',
            'entry_price' => 1.0850, 'exit_price' => 1.0770, 'lot_size' => 0.50,
            'profit_amount' => 400.00, 'pips_gain' => 80, 'roi_percentage' => 80.00,
            'broker_name' => 'IC Markets Raw', 'card_theme' => 'EMERALD_NEON',
            'is_verified_trade' => true, 'likes_count' => 76, 'fire_count' => 45,
            'rocket_count' => 19, 'comments_count' => 2,
            'approved_at' => now()->subHours(4),
        ]);

        CommunityPost::create([
            'user_id' => 4, 'status' => 'approved',
            'author_name' => 'Ruwan Chamara', 'author_badge' => 'Senior Analyst',
            'author_avatar_hex' => '0xFF38BDF8', 'post_type' => 'text',
            'content' => 'US CPI High Impact News today at 6:00 PM SLST. Gold (XAU/USD) is respecting the 4,120 support zone strongly. Expect heavy volatility! Always lock TP1 profits and shift stop loss to breakeven before news release.',
            'hashtags' => '#CPI #GoldAnalysis #RiskManagement #SLST',
            'pair' => 'XAU/USD', 'trade_type' => 'BUY',
            'likes_count' => 98, 'fire_count' => 41, 'rocket_count' => 18,
            'comments_count' => 3, 'approved_at' => now()->subHours(6),
        ]);

        CommunityPost::create([
            'user_id' => 5, 'status' => 'approved',
            'author_name' => 'Ashan Wijesinghe', 'author_badge' => 'Gold Scalper',
            'author_avatar_hex' => '0xFF8B5CF6', 'post_type' => 'screenshot',
            'content' => 'Quick scalping profit on today\'s Gold London open bounce! +$285.00 booked into wallet. Discipline is key 🎯',
            'hashtags' => '#XAUUSD #DailyTarget #Scalping',
            'image_uri' => 'res://drawable/img_gold_profit_shot',
            'pair' => 'XAU/USD', 'trade_type' => 'BUY',
            'entry_price' => 4120.50, 'exit_price' => 4130.00, 'lot_size' => 0.30,
            'profit_amount' => 285.00, 'pips_gain' => 95, 'roi_percentage' => 95.00,
            'broker_name' => 'XM Ultra Low', 'card_theme' => 'CYBER_SKY',
            'is_verified_trade' => true, 'likes_count' => 52, 'fire_count' => 31,
            'rocket_count' => 12, 'comments_count' => 1,
            'approved_at' => now()->subHours(8),
        ]);

        // --- Old backend seed ---
        CommunityPost::create([
            'user_id' => $admin->id, 'status' => 'approved',
            'author_name' => 'Admin', 'author_badge' => 'Verified',
            'author_avatar_hex' => '0xFF4A90D9', 'post_type' => 'profit_card',
            'content' => 'Great start to the year! XAU/USD SELL LIMIT hit all TPs. +84 pips!',
            'hashtags' => '#XAUUSD #WIN #SignalXpress',
            'pair' => 'XAU/USD', 'trade_type' => 'SELL',
            'entry_price' => 2350.00, 'exit_price' => 2310.00, 'lot_size' => 1.00,
            'profit_amount' => 84.00, 'pips_gain' => 870, 'roi_percentage' => 8.40,
            'broker_name' => 'XM', 'card_theme' => 'EMERALD_NEON',
            'is_verified_trade' => true, 'likes_count' => 12, 'fire_count' => 5,
            'rocket_count' => 3, 'comments_count' => 1, 'is_pinned' => true,
            'approved_at' => now()->subDays(30),
        ]);

        CommunityPost::create([
            'user_id' => $admin->id, 'status' => 'approved',
            'author_name' => 'Admin', 'author_badge' => 'Verified',
            'author_avatar_hex' => '0xFF4A90D9', 'post_type' => 'text',
            'content' => 'Welcome to Signal Xpress community! Share your trading results and discuss strategies here.',
            'hashtags' => '#Community #Trading #Forex',
            'likes_count' => 8, 'fire_count' => 2, 'rocket_count' => 1,
            'comments_count' => 1, 'approved_at' => now()->subDays(30),
        ]);

        CommunityPost::create([
            'user_id' => $admin->id, 'status' => 'approved',
            'author_name' => 'Admin', 'author_badge' => 'Verified',
            'author_avatar_hex' => '0xFF4A90D9', 'post_type' => 'screenshot',
            'content' => 'XAU/USD BUY signal +13 pips. VIP channel delivering results.',
            'hashtags' => '#XAUUSD #VIP #Signal',
            'pair' => 'XAU/USD', 'trade_type' => 'BUY',
            'entry_price' => 2360.00, 'exit_price' => 2370.00, 'lot_size' => 0.50,
            'profit_amount' => 13.00, 'pips_gain' => 130, 'roi_percentage' => 1.30,
            'broker_name' => 'XM', 'card_theme' => 'CYBER_SKY',
            'is_verified_trade' => true, 'likes_count' => 6, 'fire_count' => 3,
            'rocket_count' => 2, 'approved_at' => now()->subDays(25),
        ]);

        // ═══════════════════════════════════════════════════════════════
        // 9. COMMUNITY COMMENTS (mobile app 5 + old backend 2)
        // ═══════════════════════════════════════════════════════════════
        // --- Mobile app seed ---
        CommunityComment::create(['post_id' => 1, 'user_id' => 3, 'status' => 'approved', 'author_name' => 'Nalaka FX', 'content' => 'Super trade bro! I caught 100 pips on this one too! 🔥', 'approved_at' => now()->subHours(1)]);
        CommunityComment::create(['post_id' => 1, 'user_id' => 4, 'status' => 'approved', 'author_name' => 'Sameera L.', 'content' => 'Congratulations Kasun! Exness execution was blazing fast today.', 'approved_at' => now()->subMinutes(30)]);
        CommunityComment::create(['post_id' => 1, 'user_id' => $admin->id, 'status' => 'approved', 'author_name' => 'Admin (Signal Xpress)', 'content' => 'Great profit lock! Remember to maintain 1-2% risk per position team.', 'approved_at' => now()->subMinutes(10)]);
        CommunityComment::create(['post_id' => 2, 'user_id' => 4, 'status' => 'approved', 'author_name' => 'Tharindu M.', 'content' => 'London breakout strategy worked perfectly on EUR/USD! 👏', 'approved_at' => now()->subHours(3)]);
        CommunityComment::create(['post_id' => 3, 'user_id' => 5, 'status' => 'approved', 'author_name' => 'Mahesh', 'content' => 'Thanks for the reminder about CPI timings. Will wait for news candles to settle.', 'approved_at' => now()->subHours(5)]);

        // --- Old backend seed ---
        CommunityComment::create(['post_id' => 5, 'user_id' => $admin->id, 'status' => 'approved', 'author_name' => 'Admin', 'content' => 'Amazing result! Let\'s keep the winning streak going.', 'likes_count' => 3, 'approved_at' => now()->subDays(30)]);
        CommunityComment::create(['post_id' => 6, 'user_id' => $admin->id, 'status' => 'approved', 'author_name' => 'Admin', 'content' => 'Feel free to ask any questions about our signal system.', 'likes_count' => 1, 'approved_at' => now()->subDays(30)]);

        // ═══════════════════════════════════════════════════════════════
        // 10. MARKET NEWS (mobile app 10 + old backend 5)
        // ═══════════════════════════════════════════════════════════════
        $now = now();
        $dayMs = 86400;

        // --- Mobile app seed ---
        $mobileNews = [
            ['event_time' => $now->copy()->addSeconds(1), 'currency' => 'USD', 'title' => 'Core CPI m/m (Consumer Price Index)', 'impact' => 'HIGH', 'forecast' => '0.3%', 'previous' => '0.2%', 'actual' => '0.3%', 'description' => 'Key inflation measure for USD. High volatility expected on XAU/USD and major USD pairs.'],
            ['event_time' => $now->copy()->addSeconds(2), 'currency' => 'USD', 'title' => 'CPI y/y (Annual Inflation)', 'impact' => 'HIGH', 'forecast' => '3.1%', 'previous' => '3.0%', 'actual' => '3.1%', 'description' => 'Measures price change of goods and services purchased by consumers.'],
            ['event_time' => $now->copy()->addSeconds(3), 'currency' => 'USD', 'title' => 'Federal Funds Rate & FOMC Statement', 'impact' => 'HIGH', 'forecast' => '5.25%', 'previous' => '5.25%', 'actual' => '5.25%', 'description' => 'Major central bank interest rate decision. Heavy market impact across all pairs!'],
            ['event_time' => $now->copy()->addDays(1), 'currency' => 'USD', 'title' => 'Non-Farm Employment Change (NFP)', 'impact' => 'HIGH', 'forecast' => '165K', 'previous' => '142K', 'description' => 'Monthly non-farm job creation figure. Expected to cause high momentum spikes.'],
            ['event_time' => $now->copy()->addDays(1)->addSeconds(1), 'currency' => 'USD', 'title' => 'Unemployment Rate', 'impact' => 'HIGH', 'forecast' => '4.2%', 'previous' => '4.3%', 'description' => 'Percentage of total work force that is unemployed and actively seeking employment.'],
            ['event_time' => $now->copy()->addDays(2), 'currency' => 'EUR', 'title' => 'ECB Main Refinancing Rate', 'impact' => 'HIGH', 'forecast' => '3.65%', 'previous' => '3.90%', 'description' => 'European Central Bank interest rate announcement for the Eurozone.'],
            ['event_time' => $now->copy()->addDays(2)->addSeconds(1), 'currency' => 'GBP', 'title' => 'Official Bank Rate & MPC Summary', 'impact' => 'HIGH', 'forecast' => '5.00%', 'previous' => '5.00%', 'description' => 'Bank of England interest rate decision affecting all GBP pairs.'],
            ['event_time' => $now->copy()->addDays(3), 'currency' => 'CAD', 'title' => 'Employment Change & Unemployment Rate', 'impact' => 'HIGH', 'forecast' => '25.0K', 'previous' => '-2.8K', 'description' => 'Canadian labor market data release. Strong impact on USD/CAD and CAD/JPY.'],
            ['event_time' => $now->copy()->addDays(3)->addSeconds(1), 'currency' => 'AUD', 'title' => 'RBA Cash Rate Statement', 'impact' => 'HIGH', 'forecast' => '4.35%', 'previous' => '4.35%', 'description' => 'Reserve Bank of Australia interest rate release and monetary policy commentary.'],
            ['event_time' => $now->copy()->addDays(4), 'currency' => 'EUR', 'title' => 'German Flash Manufacturing PMI', 'impact' => 'MEDIUM', 'forecast' => '45.8', 'previous' => '45.5', 'description' => 'Leading economic indicator for German manufacturing health.'],
        ];

        // --- Old backend seed ---
        $oldNews = [
            ['event_time' => $now->copy()->addDays(5)->setTime(8, 30), 'currency' => 'USD', 'title' => 'CPI (Consumer Price Index)', 'impact' => 'HIGH', 'forecast' => '3.2%', 'previous' => '3.1%', 'description' => 'Measures the average change in prices paid by consumers for goods and services. Key inflation indicator.'],
            ['event_time' => $now->copy()->addDays(7)->setTime(14, 0), 'currency' => 'USD', 'title' => 'FOMC Statement', 'impact' => 'HIGH', 'description' => 'Federal Open Market Committee statement on monetary policy direction and interest rate decisions.'],
            ['event_time' => $now->copy()->addDays(10)->setTime(8, 30), 'currency' => 'USD', 'title' => 'Non-Farm Payrolls (NFP)', 'impact' => 'HIGH', 'forecast' => '180K', 'previous' => '150K', 'description' => 'Measures the number of jobs added in the US economy excluding farm workers. Major market mover.'],
            ['event_time' => $now->copy()->addDays(14)->setTime(7, 45), 'currency' => 'EUR', 'title' => 'ECB Interest Rate Decision', 'impact' => 'HIGH', 'forecast' => '4.50%', 'previous' => '4.50%', 'description' => 'European Central Bank decision on main refinancing rate. Impacts EUR pairs significantly.'],
            ['event_time' => $now->copy()->addDays(18)->setTime(7, 0), 'currency' => 'GBP', 'title' => 'BOE Interest Rate Decision', 'impact' => 'HIGH', 'forecast' => '5.25%', 'previous' => '5.25%', 'description' => 'Bank of England decision on base rate. Key event for GBP pairs.'],
        ];

        foreach (array_merge($mobileNews, $oldNews) as $news) {
            MarketNews::create($news);
        }
    }
}
