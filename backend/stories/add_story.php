<?php
/**
 * Add Story Endpoint
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

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$user = getAuthUser($pdo);
if (!$user && !empty($input['user_id'])) {
    $uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
    $uStmt->execute([$input['user_id']]);
    $user = $uStmt->fetch() ?: null;
}

if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$storyImage  = trim($input['story_image'] ?? '');
$textContent = trim($input['text_content'] ?? '');
$bgColor     = trim($input['background_color'] ?? '#1877F2');
$bucketId    = !empty($input['bucket_id']) ? (int)$input['bucket_id'] : null;

if (!$storyImage && !$textContent) {
    jsonResponse(false, null, 'Story must contain image or text.');
}

// Fetch configured expiry duration in hours
$expiryHours = 24;
try {
    $confStmt = $pdo->query("SELECT expiry_hours FROM story_settings WHERE id = 1 LIMIT 1");
    if ($confStmt) {
        $val = $confStmt->fetchColumn();
        if ($val && (int)$val > 0) {
            $expiryHours = (int)$val;
        }
    }
} catch (Exception $e) {}

$stmt = $pdo->prepare("
    INSERT INTO stories (user_id, story_image, text_content, background_color, bucket_id, expires_at)
    VALUES (?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL ? HOUR))
");
$stmt->execute([
    $user['id'],
    $storyImage ?: null,
    $textContent ?: null,
    $bgColor,
    $bucketId,
    $expiryHours
]);

jsonResponse(true, ['story_id' => $pdo->lastInsertId(), 'expires_in_hours' => $expiryHours], 'Story added successfully!');
