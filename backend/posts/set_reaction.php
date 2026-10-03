<?php
/**
 * Set Emoji Reaction on Post
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

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
$postId = (int)($input['post_id'] ?? 0);
$emoji  = trim($input['emoji'] ?? '');

if (!$postId) {
    jsonResponse(false, null, 'Invalid post ID.');
}

if (!$emoji) {
    // Remove reaction
    $stmt = $pdo->prepare("DELETE FROM post_reactions WHERE post_id = ? AND user_id = ?");
    $stmt->execute([$postId, $user['id']]);
    jsonResponse(true, ['emoji' => null], 'Reaction removed.');
} else {
    // Upsert reaction
    $stmt = $pdo->prepare("
        INSERT INTO post_reactions (post_id, user_id, emoji)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE emoji = VALUES(emoji)
    ");
    $stmt->execute([$postId, $user['id'], $emoji]);

    jsonResponse(true, ['emoji' => $emoji], 'Reaction updated.');
}
