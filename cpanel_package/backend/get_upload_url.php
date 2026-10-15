<?php
/**
 * Get Direct Presigned Cloudflare R2 Upload URL Endpoint
 * Flikata API
 * Enables mobile devices to upload large videos/media directly to Cloudflare R2
 * bypassing PHP size and execution time limits.
 */

require_once __DIR__ . '/config/db.php';
require_once __DIR__ . '/r2_helper.php';

$pdo = getDB();

$folder = trim($_REQUEST['folder'] ?? 'reels');
if (!in_array($folder, ['reels', 'images', 'videos', 'stories', 'media'])) {
    $folder = 'reels';
}

$rawName = trim($_REQUEST['filename'] ?? '');
$ext = strtolower(pathinfo($rawName, PATHINFO_EXTENSION)) ?: 'mp4';
$uniqueKey = $folder . '/flikata_' . date('Ymd_His') . '_' . bin2hex(random_bytes(6)) . '.' . $ext;
$mime = trim($_REQUEST['mime'] ?? 'video/mp4');

$bucket = CloudflareR2::getAvailableBucket($pdo);
if (!$bucket) {
    jsonResponse(false, null, 'No active Cloudflare R2 bucket available.');
}

$presigned = CloudflareR2::getPresignedPutUrl($bucket, $uniqueKey, $mime, 7200);

if ($presigned['success']) {
    jsonResponse(true, [
        'upload_url'  => $presigned['upload_url'],
        'file_url'    => $presigned['file_url'],
        'bucket_id'   => $presigned['bucket_id'],
        'key'         => $presigned['key']
    ], 'Direct Cloudflare R2 presigned upload URL generated successfully.');
} else {
    jsonResponse(false, null, 'Failed to generate direct upload URL.');
}
