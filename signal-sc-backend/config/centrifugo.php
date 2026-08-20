<?php

return [
    'api_url' => env('CENTRIFUGO_API_URL', 'http://localhost:8000/api'),
    'api_key' => env('CENTRIFUGO_API_KEY', ''),
    'token_hmac_secret_key' => env('CENTRIFUGO_TOKEN_HMAC_SECRET_KEY', ''),
    'token_ttl' => (int) env('CENTRIFUGO_TOKEN_TTL', 3600),
];
