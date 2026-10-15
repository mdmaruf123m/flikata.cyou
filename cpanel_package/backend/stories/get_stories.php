<?php
/**
 * Get Stories Endpoint
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

$currentUser = getAuthUser($pdo);
$currentUserId = $currentUser ? (int)$currentUser['id'] : (!empty($_GET['user_id']) ? (int)$_GET['user_id'] : 0);

// Fetch configured expiry duration in hours from story_settings table
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

// Fetch non-expired stories based on configured expiryHours
$stmt = $pdo->prepare("
    SELECT s.*, u.full_name AS author_name, u.profile_pic AS author_avatar
    FROM stories s
    JOIN users u ON s.user_id = u.id
    WHERE s.expires_at > NOW() AND s.created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR)
    ORDER BY s.id DESC
");
$stmt->execute([$expiryHours]);
$stories = $stmt->fetchAll();

$formatted = array_map(function($s) use ($currentUserId) {
    return [
        'id'               => (string)$s['id'],
        'authorName'       => $s['author_name'],
        'author_name'      => $s['author_name'],
        'authorAvatar'     => $s['author_avatar'] ?: '',
        'author_avatar'    => $s['author_avatar'] ?: '',
        'storyImage'       => $s['story_image'] ?: '',
        'story_image'      => $s['story_image'] ?: '',
        'isUserStory'      => false,
        'is_user_story'    => false,
        'textContent'      => $s['text_content'],
        'text_content'     => $s['text_content'],
        'backgroundColor'  => $s['background_color'],
        'background_color' => $s['background_color']
    ];
}, $stories);

jsonResponse(true, $formatted, 'Stories loaded.');
