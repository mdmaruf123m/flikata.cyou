<?php
/**
 * Get Comments for Post or Reel
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$rawPostId = $_GET['post_id'] ?? null;
$rawReelId = $_GET['reel_id'] ?? null;

if (!empty($rawReelId) && str_starts_with((string)$rawReelId, 'post_')) {
    $rawPostId = substr((string)$rawReelId, 5);
    $rawReelId = null;
} elseif (!empty($rawPostId) && str_starts_with((string)$rawPostId, 'post_')) {
    $rawPostId = substr((string)$rawPostId, 5);
}

$postId = !empty($rawPostId) ? (int)$rawPostId : 0;
$reelId = !empty($rawReelId) ? (int)$rawReelId : 0;

if (!$postId && !$reelId) {
    jsonResponse(false, null, 'post_id or reel_id is required.');
}

if ($postId) {
    $stmt = $pdo->prepare("
        SELECT c.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.is_verified
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.post_id = ? OR c.reel_id = ?
        ORDER BY c.id ASC
    ");
    $stmt->execute([$postId, $postId]);
} else {
    $stmt = $pdo->prepare("
        SELECT c.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.is_verified
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.reel_id = ? OR c.post_id = ?
        ORDER BY c.id ASC
    ");
    $stmt->execute([$reelId, $reelId]);
}

$comments = $stmt->fetchAll();

$formatted = array_map(function($c) {
    return [
        'id'            => (string)$c['id'],
        'postId'        => (string)($c['post_id'] ?? ''),
        'authorName'    => $c['author_name'],
        'authorAvatar'  => $c['author_avatar'] ?: '',
        'isVerified'    => (bool)$c['is_verified'],
        'text'          => $c['text'],
        'timestamp'     => $c['created_at'],
        'reactionEmoji' => $c['reaction_emoji'] ?? null,
        'likesCount'    => (int)$c['likes_count'],
        'isLiked'       => false,
        'parentId'      => $c['parent_id'],
        'replies'       => []
    ];
}, $comments);

jsonResponse(true, $formatted, 'Comments loaded.');
