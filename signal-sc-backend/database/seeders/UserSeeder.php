<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class UserSeeder extends Seeder
{
    public function run(): void
    {
        // Admin user
        User::updateOrCreate(
            ['email' => 'admin@signalxpress.com'],
            [
                'name' => 'Admin',
                'password' => Hash::make('password'),
                'role' => 'admin',
                'api_token' => Str::random(60),
            ]
        );

        // Demo viewer user
        User::updateOrCreate(
            ['email' => 'trader@signalxpress.com'],
            [
                'name' => 'Kasun Perera',
                'password' => Hash::make('password'),
                'role' => 'viewer',
                'api_token' => Str::random(60),
            ]
        );

        // Additional demo users for community posts
        $demoUsers = [
            ['name' => 'Dinuka Silva', 'email' => 'dinuka@signalxpress.com'],
            ['name' => 'Ruwan Chamara', 'email' => 'ruwan@signalxpress.com'],
            ['name' => 'Ashan Wijesinghe', 'email' => 'ashan@signalxpress.com'],
        ];

        foreach ($demoUsers as $u) {
            User::updateOrCreate(
                ['email' => $u['email']],
                [
                    'name' => $u['name'],
                    'password' => Hash::make('1password'),
                    'role' => 'viewer',
                    'api_token' => Str::random(60),
                ]
            );
        }
    }
}
