<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\File;

class DatabaseBackupController extends Controller
{
    private $backupPath;

    public function __construct()
    {
        $this->backupPath = storage_path('app/backups');
        if (!File::isDirectory($this->backupPath)) {
            File::makeDirectory($this->backupPath, 0755, true);
        }
    }

    public function index()
    {
        $backups = collect(File::files($this->backupPath))
            ->filter(fn($f) => $f->getExtension() === 'sql')
            ->map(fn($f) => [
                'name' => $f->getFilename(),
                'size' => $f->getSize(),
                'date' => $f->getMTime(),
            ])
            ->sortByDesc('date')
            ->values();

        return view('admin.database-backup', compact('backups'));
    }

    public function export()
    {
        $host = config('database.connections.mysql.host');
        $port = config('database.connections.mysql.port');
        $database = config('database.connections.mysql.database');
        $username = config('database.connections.mysql.username');
        $password = config('database.connections.mysql.password');

        $filename = 'backup_' . $database . '_' . now()->format('Y-m-d_His') . '.sql';
        $filepath = $this->backupPath . '/' . $filename;

        $password = escapeshellarg($password);
        $host = escapeshellarg($host);
        $port = escapeshellarg($port);
        $database = escapeshellarg($database);
        $username = escapeshellarg($username);
        $filepathEsc = escapeshellarg($filepath);

        $command = "mysqldump -h {$host} -P {$port} -u {$username} -p{$password} {$database} --single-transaction --routines --triggers --events > {$filepathEsc} 2>&1";

        exec($command, $output, $returnCode);

        if ($returnCode !== 0 || !File::exists($filepath)) {
            $error = implode("\n", $output);
            return back()->withErrors(['export' => 'Export failed: ' . $error]);
        }

        return response()->download($filepath, $filename)->deleteFileAfterSend(true);
    }

    public function import(Request $request)
    {
        $request->validate([
            'sql_file' => 'required|file|mimes:sql,sql.gz|max:51200',
        ]);

        $file = $request->file('sql_file');
        $filename = $file->getClientOriginalName();

        $destPath = $this->backupPath . '/import_' . now()->format('Y-m-d_His') . '_' . $filename;
        $file->move($this->backupPath, basename($destPath));

        $content = file_get_contents($destPath);

        if (!$content) {
            return back()->withErrors(['import' => 'Failed to read the uploaded file.']);
        }

        DB::purge();
        $pdo = DB::connection()->getPdo();

        $statements = $this->splitSqlStatements($content);
        $successCount = 0;
        $errorCount = 0;
        $errors = [];

        foreach ($statements as $stmt) {
            $stmt = trim($stmt);
            if (empty($stmt)) continue;

            try {
                $pdo->exec($stmt);
                $successCount++;
            } catch (\Exception $e) {
                $errorCount++;
                if (count($errors) < 5) {
                    $errors[] = substr($e->getMessage(), 0, 200);
                }
            }
        }

        @unlink($destPath);

        $msg = "Import completed: {$successCount} statements executed successfully.";
        if ($errorCount > 0) {
            $msg .= " {$errorCount} statement(s) had errors.";
        }

        $redirect = back()->with('success', $msg);
        if (!empty($errors)) {
            $redirect->with('import_errors', $errors);
        }
        return $redirect;
    }

    public function download($filename)
    {
        $filepath = $this->backupPath . '/' . basename($filename);

        if (!File::exists($filepath)) {
            abort(404);
        }

        return response()->download($filepath);
    }

    public function delete($filename)
    {
        $filepath = $this->backupPath . '/' . basename($filename);

        if (File::exists($filepath)) {
            File::delete($filepath);
        }

        return back()->with('success', 'Backup deleted successfully.');
    }

    private function splitSqlStatements($content)
    {
        if (function_exists('mb_regex_encoding')) {
            mb_regex_encoding('UTF-8');
        }

        $content = preg_replace('/\/\*.*?\*\//s', '', $content);
        $content = preg_replace('/--.*$/m', '', $content);

        $statements = [];
        $current = '';
        $inString = false;
        $stringChar = '';
        $len = strlen($content);

        for ($i = 0; $i < $len; $i++) {
            $ch = $content[$i];

            if ($inString) {
                $current .= $ch;
                if ($ch === $stringChar && ($i === 0 || $content[$i - 1] !== '\\')) {
                    $inString = false;
                }
                continue;
            }

            if ($ch === '\'' || $ch === '"') {
                $inString = true;
                $stringChar = $ch;
                $current .= $ch;
                continue;
            }

            if ($ch === ';') {
                $current .= $ch;
                $trimmed = trim($current);
                if (!empty($trimmed)) {
                    $statements[] = $trimmed;
                }
                $current = '';
                continue;
            }

            $current .= $ch;
        }

        $trimmed = trim($current);
        if (!empty($trimmed)) {
            $statements[] = $trimmed;
        }

        return $statements;
    }
}
