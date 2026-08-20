# Signal Xpress — Google Sheets Sync

## Current Script URL
```
https://script.google.com/macros/s/AKfycbyq70qX27BMYZzV2bv5wTq6wHy8anSrV53FE5EIdeXu9Baomo_qpJEdAi8p6aYWcVsA/exec
```

## How to deploy (important — must redeploy after every edit)

1. Open your Google Sheet
2. **Extensions → Apps Script**
3. Delete ALL existing code in `Code.gs`
4. Paste the contents of `google-sheets-script.gs`
5. Click **Deploy → New deployment** (or **Manage deployments → Edit → New version**)
6. Select type **Web app**, access **Anyone**
7. Click **Deploy**
8. Copy the new `/exec` URL (the deployment ID changes every time)
9. Update the URL in the admin panel Google Sheets input (or `.env` `GOOGLE_SHEETS_URL`)

## Cell highlight (not full row)

`highlightHitCell` highlights ONLY the specific hit cell:

| Hit | Column | Cell color |
|-----|--------|-----------|
| TP1 | tp1 (col 8) | green |
| TP2 | tp2 (col 9) | green |
| TP3 | tp3 (col 10) | green |
| TP4 | tp4 (col 11) | green |
| SL  | sl (col 7) | red |
| BE  | result (col 14) | gold |
