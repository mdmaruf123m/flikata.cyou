<?php
/**
 * Add Comment to Post or Reel
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

$postId   = !empty($input['post_id']) ? (int)$input['post_id'] : null;
$reelId   = !empty($input['reel_id']) ? (int)$input['reel_id'] : null;
$parentId = !empty($input['parent_id']) ? (int)$input['parent_id'] : null;
$text     = trim($input['text'] ?? '');

if (!$text || (!$postId && !$reelId)) {
    jsonResponse(false, null, 'Comment text and post_id or reel_id are required.');
}

$stmt = $pdo->prepare("
    INSERT INTO comments (post_id, reel_id, parent_id, user_id, text)
    VALUES (?, ?, ?, ?, ?)
");
$stmt->execute([$postId, $reelId, $parentId, $user['id'], $text]);
$commentId = $pdo->lastInsertId();

if ($postId) {
    $upd = $pdo->prepare("UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?");
    $upd->execute([$postId]);
} elseif ($reelId) {
    $upd = $pdo->prepare("UPDATE reels SET comments_count = comments_count + 1 WHERE id = ?");
    $upd->execute([$reelId]);
}

$newComment = [
    'id'            => (string)$commentId,
    'authorName'    => $user['full_name'],
    'authorAvatar'  => $user['profile_pic'] ?: '',
    'isVerified'    => (bool)$user['is_verified'],
    'text'          => $text,
    'timestamp'     => 'Just now',
    'reactionEmoji' => null,
    'likesCount'    => 0,
    'parentId'      => $parentId
];

jsonResponse(true, $newComment, 'Comment added successfully!');
