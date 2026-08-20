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

            $totalLots = 0;
            $totalCommission = 0;
            $recordCount = 0;

            while (($row = fgetcsv($handle)) !== false) {
                if (count($row) < count($header)) {
                    continue;
                }

                $data = array_combine($header, $row);

                CsvTradeRecord::create([
                    'csv_upload_id' => $upload->id,
                    'account_id' => $data['Account'] ?? $data['account_id'] ?? null,
                    'symbol' => $data['Symbol'] ?? $data['symbol'] ?? null,
                    'lots' => $data['Lots'] ?? $data['lots'] ?? 0,
                    'commission' => $data['Commission'] ?? $data['commission'] ?? 0,
                ]);

                $totalLots += (float) ($data['Lots'] ?? $data['lots'] ?? 0);
                $totalCommission += (float) ($data['Commission'] ?? $data['commission'] ?? 0);
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
