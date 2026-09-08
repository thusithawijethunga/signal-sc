<?php

return [
    'api_url' => env('CENTRIFUGO_API_URL', 'http://socket.hadawatha.lk/api'),
    'api_key' => env('CENTRIFUGO_API_KEY', 'c2f9a7b4e1d6f3a8b9c0d1e2f3a4b5c6'),
    'token_hmac_secret_key' => env('CENTRIFUGO_TOKEN_HMAC_SECRET_KEY', '8f3c2b1a9d4e6f7c8a1b2c3d4e5f6789a0b1c2d3e4f567890abcdef123456789'),
    'token_ttl' => (int) env('CENTRIFUGO_TOKEN_TTL', 3600),
];
