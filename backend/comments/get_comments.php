<?php
/**
 * Get Comments for Post or Reel
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$postId = (int)($_GET['post_id'] ?? 0);
$reelId = (int)($_GET['reel_id'] ?? 0);

if (!$postId && !$reelId) {
    jsonResponse(false, null, 'post_id or reel_id is required.');
}

if ($postId) {
    $stmt = $pdo->prepare("
        SELECT c.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.is_verified
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.post_id = ?
        ORDER BY c.id ASC
    ");
    $stmt->execute([$postId]);
} else {
    $stmt = $pdo->prepare("
        SELECT c.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.is_verified
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.reel_id = ?
        ORDER BY c.id ASC
    ");
    $stmt->execute([$reelId]);
}

$comments = $stmt->fetchAll();

$formatted = array_map(function($c) {
    return [
        'id'            => (string)$c['id'],
        'authorName'    => $c['author_name'],
        'authorAvatar'  => $c['author_avatar'] ?: '',
        'isVerified'    => (bool)$c['is_verified'],
        'text'          => $c['text'],
        'timestamp'     => $c['created_at'],
        'reactionEmoji' => $c['reaction_emoji'],
        'likesCount'    => (int)$c['likes_count'],
        'parentId'      => $c['parent_id']
    ];
}, $comments);

jsonResponse(true, $formatted, 'Comments loaded.');
