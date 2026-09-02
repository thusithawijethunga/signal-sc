<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CsvTradeRecord;
use App\Models\CsvUpload;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CsvController extends Controller
{
    public function upload(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'file' => 'required|file|mimes:csv,txt|max:10240',
            ]);

            $file = $request->file('file');
            $user = $request->user();

            $upload = CsvUpload::create([
                'user_id' => $user->id,
                'filename' => $file->getClientOriginalName(),
                'total_records' => 0,
                'total_lots' => 0,
                'total_commission' => 0,
            ]);

            $handle = fopen($file->getPathname(), 'r');
            $header = fgetcsv($handle);

            $headerMap = array_map(fn($h) => strtolower(trim($h)), $header);

            $findCol = function (...$names) use ($headerMap) {
                foreach ($names as $name) {
                    $idx = array_search(strtolower($name), $headerMap);
                    if ($idx !== false) return $idx;
                }
                return -1;
            };

            $idxAcc = $findCol('MT4/MT5 ID', 'Account', 'account_id', 'Login');
            $idxSym = $findCol('Instrument', 'Symbol', 'Pair');
            $idxLots = $findCol('Lots', 'Volume');
            $idxComm = $findCol('Affiliate Comm.', 'Commission', 'Total Comm.', 'Comm');

            $totalLots = 0;
            $totalCommission = 0;
            $recordCount = 0;

            while (($row = fgetcsv($handle)) !== false) {
                if (count($row) < 3) {
                    continue;
                }

                $accId = $idxAcc >= 0 ? ($row[$idxAcc] ?? null) : ($row[1] ?? null);
                $symbol = $idxSym >= 0 ? ($row[$idxSym] ?? null) : ($row[10] ?? null);
                $lots = $idxLots >= 0 ? (float) ($row[$idxLots] ?? 0) : (float) ($row[12] ?? 0);
                $comm = $idxComm >= 0 ? (float) ($row[$idxComm] ?? 0) : (float) ($row[16] ?? 0);

                CsvTradeRecord::create([
                    'csv_upload_id' => $upload->id,
                    'account_id' => $accId,
                    'symbol' => $symbol,
                    'lots' => $lots,
                    'commission' => $comm,
                ]);

                $totalLots += $lots;
                $totalCommission += $comm;
                $recordCount++;
            }

            fclose($handle);

            $upload->update([
                'total_records' => $recordCount,
                'total_lots' => round($totalLots, 2),
                'total_commission' => round($totalCommission, 2),
            ]);

            return response()->json([
                'upload' => $upload,
                'records_count' => $recordCount,
            ], 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Upload failed', 'error' => $e->getMessage()], 500);
        }
    }

    public function uploads(Request $request): JsonResponse
    {
        try {
            $uploads = CsvUpload::with('user')
                ->where('user_id', $request->user()->id)
                ->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($uploads);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch uploads', 'error' => $e->getMessage()], 500);
        }
    }

    public function analysis(Request $request, CsvUpload $csvUpload): JsonResponse
    {
        try {
            $records = CsvTradeRecord::where('csv_upload_id', $csvUpload->id)->get();

            $symbols = $records->groupBy('symbol')->map(function ($group) {
                return [
                    'count' => $group->count(),
                    'total_lots' => round($group->sum('lots'), 2),
                    'total_commission' => round($group->sum('commission'), 2),
                ];
            });

            return response()->json([
                'upload' => $csvUpload,
                'total_records' => $csvUpload->total_records,
                'total_lots' => $csvUpload->total_lots,
                'total_commission' => $csvUpload->total_commission,
                'symbols' => $symbols,
                'records' => $records,
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to generate analysis', 'error' => $e->getMessage()], 500);
        }
    }
}
