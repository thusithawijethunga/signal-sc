<?php

namespace App\Services;

use App\Models\Trade;
use App\Models\Signal;
use App\Models\CommunityPost;
use App\Models\MarketNews;
use Illuminate\Support\Str;

class SignalXpressCentrifugoService
{
    protected CentrifugoService $centrifugo;

    public function __construct(CentrifugoService $centrifugo)
    {
        $this->centrifugo = $centrifugo;
    }

    public function publishSignal(Signal $signal): bool
    {
        $payload = [
            'id' => $signal->id,
            'no' => $signal->no,
            'type' => 'signal',
            'pair' => $signal->pair,
            'direction' => $signal->direction,
            'entry1' => $signal->entry1,
            'entry2' => $signal->entry2,
            'sl' => $signal->sl,
            'tp1' => $signal->tp1,
            'tp2' => $signal->tp2,
            'tp3' => $signal->tp3,
            'tp4' => $signal->tp4,
            'pips' => $signal->pips,
            'profit' => $signal->profit,
            'result' => $signal->result,
            'channel' => $signal->channel,
            'date' => $signal->date?->format('Y-m-d'),
            'timestamp' => now()->toISOString(),
        ];

        $ok = $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADING, $payload);

        $this->centrifugo->broadcastNotification(
            'New Signal: ' . $signal->pair,
            $signal->direction . ' ' . $signal->pair . ' | Entry: ' . ($signal->entry1 ?? '—'),
            'signal',
            ['signal_id' => $signal->id]
        );

        return $ok;
    }

    public function publishTrade(Trade $trade, string $action = 'created'): bool
    {
        $payload = [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => 'trade',
            'action' => $action,
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'entry1' => $trade->entry1,
            'entry2' => $trade->entry2,
            'sl' => $trade->sl,
            'tp1' => $trade->tp1,
            'tp2' => $trade->tp2,
            'tp3' => $trade->tp3,
            'tp4' => $trade->tp4,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'channel' => $trade->channel,
            'date' => $trade->date?->format('Y-m-d'),
            'timestamp' => now()->toISOString(),
        ];

        return $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, $payload);
    }

    public function publishTradeHit(Trade $trade, string $hitType): bool
    {
        $payload = [
            'id' => $trade->id,
            'no' => $trade->no,
            'type' => 'trade_hit',
            'hit_type' => $hitType,
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'timestamp' => now()->toISOString(),
        ];

        $ok = $this->centrifugo->publish(CentrifugoService::CHANNEL_TRADES, $payload);

        $this->centrifugo->broadcastNotification(
            "🎯 {$hitType} Hit!",
            $trade->pair . ' ' . $trade->direction . ' | ' . $trade->pips . ' pips | $' . $trade->profit,
            'trade_hit',
            ['trade_id' => $trade->id, 'hit_type' => $hitType]
        );

        return $ok;
    }

    public function publishCommunityPost(CommunityPost $post): bool
    {
        $payload = [
            'id' => $post->id,
            'type' => 'community_post',
            'author_name' => $post->author_name,
            'post_type' => $post->post_type,
            'content' => $post->content,
            'pair' => $post->pair,
            'profit_amount' => $post->profit_amount,
            'pips_gain' => $post->pips_gain,
            'timestamp' => now()->toISOString(),
        ];

        return $this->centrifugo->publish(CentrifugoService::CHANNEL_COMMUNITY, $payload);
    }

    public function publishNews(MarketNews $news): bool
    {
        $payload = [
            'id' => $news->id,
            'type' => 'market_news',
            'currency' => $news->currency,
            'title' => $news->title,
            'impact' => $news->impact,
            'forecast' => $news->forecast,
            'previous' => $news->previous,
            'actual' => $news->actual,
            'event_time' => $news->event_time?->toISOString(),
            'timestamp' => now()->toISOString(),
        ];

        return $this->centrifugo->publish(CentrifugoService::CHANNEL_NEWS, $payload);
    }
}
