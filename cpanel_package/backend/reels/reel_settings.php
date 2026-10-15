<?php
/**
 * Reel Settings Endpoint
 * Controls maximum allowed duration for Reels in seconds (Default: 60s)
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

// Ensure reel_settings table exists
$pdo->exec("
    CREATE TABLE IF NOT EXISTS `reel_settings` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `max_duration_seconds` INT NOT NULL DEFAULT 60,
      `description` VARCHAR(255) DEFAULT 'Maximum allowed video duration for reels in seconds (default 60s). Change this to any limit.',
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
");

$pdo->exec("
    INSERT IGNORE INTO `reel_settings` (`id`, `max_duration_seconds`, `description`) 
    VALUES (1, 60, 'Maximum allowed duration in seconds for reels/videos. Change this value to adjust the duration limit.');
");

// Update settings if requested via POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
    if (isset($input['max_duration_seconds'])) {
        $maxDur = max(5, (int)$input['max_duration_seconds']);
        $uStmt = $pdo->prepare("UPDATE `reel_settings` SET `max_duration_seconds` = ? WHERE `id` = 1");
        $uStmt->execute([$maxDur]);
        jsonResponse(true, ['max_duration_seconds' => $maxDur], 'Reel max duration updated successfully.');
    }
}

// Fetch current setting
$stmt = $pdo->query("SELECT `max_duration_seconds` FROM `reel_settings` WHERE `id` = 1 LIMIT 1");
$row = $stmt ? $stmt->fetch() : null;
$maxDuration = $row ? (int)$row['max_duration_seconds'] : 60;

jsonResponse(true, [
    'max_duration_seconds' => $maxDuration
], 'Reel settings loaded successfully.');
