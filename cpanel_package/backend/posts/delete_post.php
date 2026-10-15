<?php
/**
 * Delete Post Endpoint
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

$postId = (int)($input['post_id'] ?? 0);

if (!$postId) {
    jsonResponse(false, null, 'Invalid post ID.');
}

// Find the post first
$stmt = $pdo->prepare("SELECT p.*, u.full_name AS author_name FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.id = ?");
$stmt->execute([$postId]);
$post = $stmt->fetch();

if (!$post) {
    // If post is already absent, return success so app knows it's deleted
    jsonResponse(true, null, 'Post already deleted.');
}

// Clean up child tables to maintain data integrity
$pdo->prepare("DELETE FROM post_likes WHERE post_id = ?")->execute([$postId]);
$pdo->prepare("DELETE FROM post_reactions WHERE post_id = ?")->execute([$postId]);
$pdo->prepare("DELETE FROM comments WHERE post_id = ?")->execute([$postId]);

// If this post had a video, also delete from reels table
if (!empty($post['video_url'])) {
    $pdo->prepare("DELETE FROM reels WHERE media_url = ?")->execute([$post['video_url']]);
}

$del = $pdo->prepare("DELETE FROM posts WHERE id = ?");
$del->execute([$postId]);

jsonResponse(true, null, 'Post deleted permanently.');
