<?php

namespace Database\Seeders;

use App\Models\MarketNews;
use Illuminate\Database\Seeder;

class MarketNewsSeeder extends Seeder
{
    public function run(): void
    {
        $now = now();
        $dayMs = 86400;

        $news = [
            [
                'event_time' => $now->copy()->addSeconds(1),
                'currency' => 'USD',
                'title' => 'Core CPI m/m (Consumer Price Index)',
                'impact' => 'HIGH',
                'forecast' => '0.3%',
                'previous' => '0.2%',
                'actual' => '0.3%',
                'description' => 'Key inflation measure for USD. High volatility expected on XAU/USD and major USD pairs.',
            ],
            [
                'event_time' => $now->copy()->addSeconds(2),
                'currency' => 'USD',
                'title' => 'CPI y/y (Annual Inflation)',
                'impact' => 'HIGH',
                'forecast' => '3.1%',
                'previous' => '3.0%',
                'actual' => '3.1%',
                'description' => 'Measures price change of goods and services purchased by consumers.',
            ],
            [
                'event_time' => $now->copy()->addSeconds(3),
                'currency' => 'USD',
                'title' => 'Federal Funds Rate & FOMC Statement',
                'impact' => 'HIGH',
                'forecast' => '5.25%',
                'previous' => '5.25%',
                'actual' => '5.25%',
                'description' => 'Major central bank interest rate decision. Heavy market impact across all pairs!',
            ],
            [
                'event_time' => $now->copy()->addDays(1),
                'currency' => 'USD',
                'title' => 'Non-Farm Employment Change (NFP)',
                'impact' => 'HIGH',
                'forecast' => '165K',
                'previous' => '142K',
                'actual' => null,
                'description' => 'Monthly non-farm job creation figure. Expected to cause high momentum spikes.',
            ],
            [
                'event_time' => $now->copy()->addDays(1)->addSeconds(1),
                'currency' => 'USD',
                'title' => 'Unemployment Rate',
                'impact' => 'HIGH',
                'forecast' => '4.2%',
                'previous' => '4.3%',
                'actual' => null,
                'description' => 'Percentage of total work force that is unemployed and actively seeking employment.',
            ],
            [
                'event_time' => $now->copy()->addDays(2),
                'currency' => 'EUR',
                'title' => 'ECB Main Refinancing Rate',
                'impact' => 'HIGH',
                'forecast' => '3.65%',
                'previous' => '3.90%',
                'actual' => null,
                'description' => 'European Central Bank interest rate announcement for the Eurozone.',
            ],
            [
                'event_time' => $now->copy()->addDays(2)->addSeconds(1),
                'currency' => 'GBP',
                'title' => 'Official Bank Rate & MPC Summary',
                'impact' => 'HIGH',
                'forecast' => '5.00%',
                'previous' => '5.00%',
                'actual' => null,
                'description' => 'Bank of England interest rate decision affecting all GBP pairs.',
            ],
            [
                'event_time' => $now->copy()->addDays(3),
                'currency' => 'CAD',
                'title' => 'Employment Change & Unemployment Rate',
                'impact' => 'HIGH',
                'forecast' => '25.0K',
                'previous' => '-2.8K',
                'actual' => null,
                'description' => 'Canadian labor market data release. Strong impact on USD/CAD and CAD/JPY.',
            ],
            [
                'event_time' => $now->copy()->addDays(3)->addSeconds(1),
                'currency' => 'AUD',
                'title' => 'RBA Cash Rate Statement',
                'impact' => 'HIGH',
                'forecast' => '4.35%',
                'previous' => '4.35%',
                'actual' => null,
                'description' => 'Reserve Bank of Australia interest rate release and monetary policy commentary.',
            ],
            [
                'event_time' => $now->copy()->addDays(4),
                'currency' => 'EUR',
                'title' => 'German Flash Manufacturing PMI',
                'impact' => 'MEDIUM',
                'forecast' => '45.8',
                'previous' => '45.5',
                'actual' => null,
                'description' => 'Leading economic indicator for German manufacturing health.',
            ],
        ];

        foreach ($news as $item) {
            MarketNews::updateOrCreate(
                ['title' => $item['title'], 'event_time' => $item['event_time']],
                $item
            );
        }
    }
}
