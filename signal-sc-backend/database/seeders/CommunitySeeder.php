<?php

namespace Database\Seeders;

use App\Models\CommunityPost;
use App\Models\CommunityComment;
use Illuminate\Database\Seeder;

class CommunitySeeder extends Seeder
{
    public function run(): void
    {
        $now = now();
        $hourMs = 3600;

        // ── Posts ──────────────────────────────────────
        $posts = [
            [
                'id' => 1,
                'user_id' => 1,
                'status' => 'approved',
                'author_name' => 'Kasun Perera',
                'author_badge' => 'VIP Master Trader',
                'author_avatar_hex' => '0xFFF59E0B',
                'post_type' => 'screenshot',
                'content' => 'Gold signal #2 was pure sniper entry! Hit TP2 effortlessly and locked +$1,650 in profit! Huge gratitude to Signal Xpress team! 🚀🔥',
                'hashtags' => '#XAUUSD #TP2Hit #ForexProfit #Screenshot',
                'image_uri' => 'res://drawable/img_gold_profit_shot',
                'pair' => 'XAU/USD',
                'trade_type' => 'BUY',
                'entry_price' => 4122.00,
                'exit_price' => 4138.50,
                'lot_size' => 1.00,
                'profit_amount' => 1650.00,
                'pips_gain' => 165,
                'roi_percentage' => 165.00,
                'broker_name' => 'Exness Pro',
                'card_theme' => 'GOLD_LUXURY',
                'is_verified_trade' => true,
                'likes_count' => 124,
                'fire_count' => 88,
                'rocket_count' => 52,
                'comments_count' => 3,
                'is_pinned' => true,
                'approved_at' => $now->subHours(2),
            ],
            [
                'id' => 2,
                'user_id' => 2,
                'status' => 'approved',
                'author_name' => 'Dinuka Silva',
                'author_badge' => 'Pro Scalper',
                'author_avatar_hex' => '0xFF10B981',
                'post_type' => 'screenshot',
                'content' => 'EUR/USD Technical breakdown on 15M chart! Clean support retest and sell continuation as predicted.',
                'hashtags' => '#EURUSD #PriceAction #ChartAnalysis',
                'image_uri' => 'res://drawable/img_chart_analysis_shot',
                'pair' => 'EUR/USD',
                'trade_type' => 'SELL',
                'entry_price' => 1.0850,
                'exit_price' => 1.0770,
                'lot_size' => 0.50,
                'profit_amount' => 400.00,
                'pips_gain' => 80,
                'roi_percentage' => 80.00,
                'broker_name' => 'IC Markets Raw',
                'card_theme' => 'EMERALD_NEON',
                'is_verified_trade' => true,
                'likes_count' => 76,
                'fire_count' => 45,
                'rocket_count' => 19,
                'comments_count' => 2,
                'is_pinned' => false,
                'approved_at' => $now->subHours(4),
            ],
            [
                'id' => 3,
                'user_id' => 3,
                'status' => 'approved',
                'author_name' => 'Ruwan Chamara',
                'author_badge' => 'Senior Analyst',
                'author_avatar_hex' => '0xFF38BDF8',
                'post_type' => 'text',
                'content' => 'US CPI High Impact News today at 6:00 PM SLST. Gold (XAU/USD) is respecting the 4,120 support zone strongly. Expect heavy volatility! Always lock TP1 profits and shift stop loss to breakeven before news release.',
                'hashtags' => '#CPI #GoldAnalysis #RiskManagement #SLST',
                'image_uri' => null,
                'pair' => 'XAU/USD',
                'trade_type' => 'BUY',
                'entry_price' => null,
                'exit_price' => null,
                'lot_size' => null,
                'profit_amount' => 0,
                'pips_gain' => 0,
                'roi_percentage' => 0,
                'broker_name' => null,
                'card_theme' => null,
                'is_verified_trade' => false,
                'likes_count' => 98,
                'fire_count' => 41,
                'rocket_count' => 18,
                'comments_count' => 3,
                'is_pinned' => false,
                'approved_at' => $now->subHours(6),
            ],
            [
                'id' => 4,
                'user_id' => 4,
                'status' => 'approved',
                'author_name' => 'Ashan Wijesinghe',
                'author_badge' => 'Gold Scalper',
                'author_avatar_hex' => '0xFF8B5CF6',
                'post_type' => 'screenshot',
                'content' => 'Quick scalping profit on today\'s Gold London open bounce! +$285.00 booked into wallet. Discipline is key 🎯',
                'hashtags' => '#XAUUSD #DailyTarget #Scalping',
                'image_uri' => 'res://drawable/img_gold_profit_shot',
                'pair' => 'XAU/USD',
                'trade_type' => 'BUY',
                'entry_price' => 4120.50,
                'exit_price' => 4130.00,
                'lot_size' => 0.30,
                'profit_amount' => 285.00,
                'pips_gain' => 95,
                'roi_percentage' => 95.00,
                'broker_name' => 'XM Ultra Low',
                'card_theme' => 'CYBER_SKY',
                'is_verified_trade' => true,
                'likes_count' => 52,
                'fire_count' => 31,
                'rocket_count' => 12,
                'comments_count' => 1,
                'is_pinned' => false,
                'approved_at' => $now->subHours(8),
            ],
        ];

        foreach ($posts as $post) {
            CommunityPost::updateOrCreate(
                ['id' => $post['id']],
                $post
            );
        }

        // ── Comments ──────────────────────────────────
        $comments = [
            [
                'post_id' => 1,
                'user_id' => 2,
                'status' => 'approved',
                'author_name' => 'Nalaka FX',
                'content' => 'Super trade bro! I caught 100 pips on this one too! 🔥',
                'approved_at' => $now->subHours(1),
            ],
            [
                'post_id' => 1,
                'user_id' => 3,
                'status' => 'approved',
                'author_name' => 'Sameera L.',
                'content' => 'Congratulations Kasun! Exness execution was blazing fast today.',
                'approved_at' => $now->subMinutes(30),
            ],
            [
                'post_id' => 1,
                'user_id' => 1,
                'status' => 'approved',
                'author_name' => 'Admin (Signal Xpress)',
                'content' => 'Great profit lock! Remember to maintain 1-2% risk per position team.',
                'approved_at' => $now->subMinutes(10),
            ],
            [
                'post_id' => 2,
                'user_id' => 3,
                'status' => 'approved',
                'author_name' => 'Tharindu M.',
                'content' => 'London breakout strategy worked perfectly on EUR/USD! 👏',
                'approved_at' => $now->subHours(3),
            ],
            [
                'post_id' => 3,
                'user_id' => 4,
                'status' => 'approved',
                'author_name' => 'Mahesh',
                'content' => 'Thanks for the reminder about CPI timings. Will wait for news candles to settle.',
                'approved_at' => $now->subHours(5),
            ],
        ];

        foreach ($comments as $comment) {
            CommunityComment::updateOrCreate(
                ['post_id' => $comment['post_id'], 'author_name' => $comment['author_name']],
                $comment
            );
        }
    }
}
