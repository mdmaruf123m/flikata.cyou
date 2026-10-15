<?php
/**
 * Cloudflare R2 Media Upload API Endpoint
 * Handles file uploads, auto-selects available active R2 bucket, updates storage quota.
 */

// Allow large uploads & runtime execution
@ini_set('upload_max_filesize', '512M');
@ini_set('post_max_size', '512M');
@ini_set('memory_limit', '512M');
@ini_set('max_execution_time', '600');

require_once __DIR__ . '/config/db.php';
require_once __DIR__ . '/r2_helper.php';

$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] !== 'POST' && $_SERVER['REQUEST_METHOD'] !== 'PUT') {
    jsonResponse(false, null, 'POST or PUT request required for uploads.');
}

$fileData = null;
$originalName = 'upload_' . time();
$mimeType = 'application/octet-stream';
$fileSize = 0;

if (isset($_FILES['file'])) {
    $err = $_FILES['file']['error'];
    if ($err !== UPLOAD_ERR_OK) {
        $msg = 'File upload error occurred.';
        if ($err === UPLOAD_ERR_INI_SIZE || $err === UPLOAD_ERR_FORM_SIZE) {
            $msg = 'File size exceeds server upload limit. Please upload directly or check PHP upload_max_filesize.';
        } elseif ($err === UPLOAD_ERR_PARTIAL) {
            $msg = 'File upload was interrupted.';
        } elseif ($err === UPLOAD_ERR_NO_FILE) {
            $msg = 'No file was submitted.';
        }
        jsonResponse(false, null, $msg);
    }
    $fileSize = $_FILES['file']['size'];
    $originalName = $_FILES['file']['name'];
    $tmpPath = $_FILES['file']['tmp_name'];
    $fileData = file_get_contents($tmpPath);
    $mimeType = mime_content_type($tmpPath) ?: 'application/octet-stream';
} else {
    // Check raw input stream (e.g. PUT or direct binary POST)
    $rawInput = file_get_contents('php://input');
    if ($rawInput && strlen($rawInput) > 0) {
        $fileData = $rawInput;
        $fileSize = strlen($rawInput);
        $headers = getallheaders();
        $originalName = $headers['X-File-Name'] ?? $headers['x-file-name'] ?? ('upload_' . time() . '.mp4');
        $mimeType = $headers['Content-Type'] ?? $headers['content-type'] ?? 'application/octet-stream';
    }
}

if (!$fileData || $fileSize === 0) {
    jsonResponse(false, null, 'No file data received or empty upload.');
}

// Determine file extension
$ext = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));
if (!$ext) {
    $ext = (strpos($mimeType, 'video') !== false) ? 'mp4' : 'jpg';
}

// Allowed media types
$allowedImageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'heic'];
$allowedVideoExts = ['mp4', 'mov', 'avi', 'mkv', 'webm', '3gp'];

$customFolder = trim($_POST['folder'] ?? $_GET['folder'] ?? '');
if (!empty($customFolder) && preg_match('/^[a-zA-Z0-9_\-]+$/', $customFolder)) {
    $folder = $customFolder;
} elseif (in_array($ext, $allowedImageExts)) {
    $folder = 'images';
} elseif (in_array($ext, $allowedVideoExts)) {
    $folder = 'reels'; // Save directly to reels folder
} else {
    $folder = 'reels';
}

// 1. Find next available active R2 bucket with sufficient quota
$bucket = CloudflareR2::getAvailableBucket($pdo, $fileSize);
if (!$bucket) {
    jsonResponse(false, null, 'No active Cloudflare R2 bucket with available storage quota! Please check storage.php to add or enable an R2 bucket.');
}

// 2. Generate unique object key
$uniqueKey = $folder . '/flikata_' . date('Ymd_His') . '_' . bin2hex(random_bytes(6)) . '.' . $ext;

// 3. Upload to Cloudflare R2
$uploadResult = CloudflareR2::uploadFile($bucket, $fileData, $uniqueKey, $mimeType);

if ($uploadResult['success']) {
    // 4. Increment bucket used bytes in DB
    CloudflareR2::incrementBucketUsage($pdo, $bucket['id'], $fileSize);

    jsonResponse(true, [
        'url'         => $uploadResult['file_url'],
        'bucket_id'   => $bucket['id'],
        'bucket_name' => $bucket['bucket_name'],
        'size'        => $fileSize,
        'mime'        => $mimeType
    ], 'File successfully uploaded to Cloudflare R2!');
} else {
    jsonResponse(false, null, $uploadResult['message']);
}
