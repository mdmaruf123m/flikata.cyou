<?php
/**
 * Story Settings Management API Endpoint
 * View or update auto-deletion expiry duration in hours
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

// Ensure story_settings table exists
$pdo->exec("
    CREATE TABLE IF NOT EXISTS `story_settings` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `expiry_hours` INT NOT NULL DEFAULT 24,
      `description` VARCHAR(255) DEFAULT 'Story expiration duration in hours (e.g. 24 for 1 day, 12, 48, etc.)',
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
");
$pdo->exec("
    INSERT INTO `story_settings` (`id`, `expiry_hours`, `description`) 
    VALUES (1, 24, 'Story auto-deletion duration in hours. Change this value to adjust story lifetime.')
    ON DUPLICATE KEY UPDATE `id` = `id`;
");

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?: $_POST;
    $newHours = !empty($input['expiry_hours']) ? (int)$input['expiry_hours'] : 0;
    
    if ($newHours > 0) {
        $stmt = $pdo->prepare("UPDATE story_settings SET expiry_hours = ? WHERE id = 1");
        $stmt->execute([$newHours]);
        jsonResponse(true, ['expiry_hours' => $newHours], "Story expiry hours updated to {$newHours} hours!");
    } else {
        jsonResponse(false, null, 'Invalid expiry_hours. Must be greater than 0.');
    }
}

// GET request: return current settings
$stmt = $pdo->query("SELECT * FROM story_settings WHERE id = 1 LIMIT 1");
$settings = $stmt->fetch();

jsonResponse(true, $settings, 'Current story settings.');
