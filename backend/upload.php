<?php
/**
 * Cloudflare R2 Media Upload API Endpoint
 * Handles file uploads, auto-selects available active R2 bucket, updates storage quota.
 */

require_once __DIR__ . '/config/db.php';
require_once __DIR__ . '/r2_helper.php';

$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonResponse(false, null, 'POST request required for uploads.');
}

if (!isset($_FILES['file']) || $_FILES['file']['error'] !== UPLOAD_ERR_OK) {
    jsonResponse(false, null, 'No file uploaded or upload error occurred.');
}

$uploadedFile = $_FILES['file'];
$fileSize = $uploadedFile['size'];
$originalName = $uploadedFile['name'];
$tmpPath = $uploadedFile['tmp_name'];

// Determine file extension and MIME type
$ext = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));
$mimeType = mime_content_type($tmpPath) ?: 'application/octet-stream';

// Allowed media types
$allowedImageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'heic'];
$allowedVideoExts = ['mp4', 'mov', 'avi', 'mkv', 'webm', '3gp'];

$customFolder = trim($_POST['folder'] ?? $_GET['folder'] ?? '');
if (!empty($customFolder) && preg_match('/^[a-zA-Z0-9_\-]+$/', $customFolder)) {
    $folder = $customFolder;
} elseif (in_array($ext, $allowedImageExts)) {
    $folder = 'images';
} elseif (in_array($ext, $allowedVideoExts)) {
    $folder = 'videos';
} else {
    $folder = 'media';
}

// 1. Find next available active R2 bucket with sufficient quota
$bucket = CloudflareR2::getAvailableBucket($pdo, $fileSize);

if (!$bucket) {
    jsonResponse(false, null, 'No active Cloudflare R2 bucket with available storage quota! Please check storage.php to add or enable an R2 bucket.');
}

// 2. Read file data and generate unique object key
$fileData = file_get_contents($tmpPath);
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
