<?php

namespace App\Http\Controllers;

use App\Models\CsvUpload;
use App\Models\CsvTradeRecord;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class CsvManageController extends Controller
{
    public function index()
    {
        $uploads = CsvUpload::with('user')->latest()->paginate(20);

        return view('admin.csv.index', compact('uploads'));
    }

    public function upload(Request $request)
    {
        $request->validate([
            'csv_file' => 'required|file|mimes:csv,txt|max:10240',
        ]);

        $file = $request->file('csv_file');
        $filename = time() . '_' . $file->getClientOriginalName();
        $path = $file->storeAs('csv-uploads', $filename, 'public');

        $lines = array_map('str_getcsv', file(storage_path('app/public/' . $path)));
        $header = array_shift($lines);

        $totalRecords = count($lines);
        $totalLots = 0;
        $totalCommission = 0;

        $upload = CsvUpload::create([
            'user_id' => $request->user()->id,
            'filename' => $filename,
            'total_records' => $totalRecords,
            'total_lots' => 0,
            'total_commission' => 0,
        ]);

        foreach ($lines as $line) {
            $data = array_combine($header, $line);

            $lots = (float) ($data['lots'] ?? $data['volume'] ?? 0);
            $commission = (float) ($data['commission'] ?? 0);

            $totalLots += $lots;
            $totalCommission += $commission;

            CsvTradeRecord::create([
                'csv_upload_id' => $upload->id,
                'account_id' => $data['account_id'] ?? $data['login'] ?? null,
                'symbol' => $data['symbol'] ?? $data['pair'] ?? null,
                'lots' => $lots,
                'commission' => $commission,
            ]);
        }

        $upload->update([
            'total_lots' => $totalLots,
            'total_commission' => $totalCommission,
        ]);

        return redirect()->route('admin.csv.index')->with('success', "CSV processed: {$totalRecords} records, {$totalLots} lots, \${$totalCommission} commission.");
    }
}
