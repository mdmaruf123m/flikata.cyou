<?php
/**
 * Set Emoji Reaction on Post (Like, Love, Care, Haha, Wow, Sad, Angry)
 * Flikata API - Fully Synchronized with post_likes & posts.likes_count
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
    // 1. Remove reaction
    $stmt = $pdo->prepare("DELETE FROM post_reactions WHERE post_id = ? AND user_id = ?");
    $stmt->execute([$postId, $user['id']]);

    // 2. Remove like if exists and decrement count
    $chkLike = $pdo->prepare("SELECT id FROM post_likes WHERE post_id = ? AND user_id = ?");
    $chkLike->execute([$postId, $user['id']]);
    if ($chkLike->fetch()) {
        $delLike = $pdo->prepare("DELETE FROM post_likes WHERE post_id = ? AND user_id = ?");
        $delLike->execute([$postId, $user['id']]);

        $upd = $pdo->prepare("UPDATE posts SET likes_count = GREATEST(0, likes_count - 1) WHERE id = ?");
        $upd->execute([$postId]);
    }

    $cnt = $pdo->prepare("SELECT likes_count FROM posts WHERE id = ?");
    $cnt->execute([$postId]);
    $likesCount = (int)$cnt->fetchColumn();

    jsonResponse(true, ['emoji' => null, 'is_liked' => false, 'likes_count' => $likesCount], 'Reaction removed.');
} else {
    // 1. Upsert reaction
    $stmt = $pdo->prepare("
        INSERT INTO post_reactions (post_id, user_id, emoji)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE emoji = VALUES(emoji)
    ");
    $stmt->execute([$postId, $user['id'], $emoji]);

    // 2. Insert into post_likes if not already liked and increment count
    $chkLike = $pdo->prepare("SELECT id FROM post_likes WHERE post_id = ? AND user_id = ?");
    $chkLike->execute([$postId, $user['id']]);
    if (!$chkLike->fetch()) {
        $insLike = $pdo->prepare("INSERT INTO post_likes (post_id, user_id) VALUES (?, ?)");
        $insLike->execute([$postId, $user['id']]);

        $upd = $pdo->prepare("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?");
        $upd->execute([$postId]);
    }

    $cnt = $pdo->prepare("SELECT likes_count FROM posts WHERE id = ?");
    $cnt->execute([$postId]);
    $likesCount = (int)$cnt->fetchColumn();

    jsonResponse(true, ['emoji' => $emoji, 'is_liked' => true, 'likes_count' => $likesCount], 'Reaction updated.');
}
