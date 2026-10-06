<?php
/**
 * Edit / Update Post Endpoint (Owner Only)
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

$postId   = (int)($input['post_id'] ?? 0);
$content  = trim($input['content'] ?? '');
$privacy  = trim($input['privacy'] ?? 'Public');

if (!$postId) {
    jsonResponse(false, null, 'Invalid post ID.');
}

// 1. Verify that post exists and belongs to the authenticated user
$chk = $pdo->prepare("SELECT * FROM posts WHERE id = ?");
$chk->execute([$postId]);
$existingPost = $chk->fetch();

if (!$existingPost) {
    jsonResponse(false, null, 'Post not found.');
}

if ((int)$existingPost['user_id'] !== (int)$user['id']) {
    jsonResponse(false, null, 'Unauthorized. You can only edit your own posts.');
}

// 2. Update post content, privacy, and updated_at
$stmt = $pdo->prepare("
    UPDATE posts 
    SET content = ?, privacy = ?, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ? AND user_id = ?
");
$stmt->execute([
    $content ?: null,
    $privacy ?: 'Public',
    $postId,
    $user['id']
]);

// 3. Return updated post details
$updatedStmt = $pdo->prepare("SELECT * FROM posts WHERE id = ?");
$updatedStmt->execute([$postId]);
$updated = $updatedStmt->fetch();

jsonResponse(true, [
    'id'         => (string)$updated['id'],
    'content'    => $updated['content'] ?: '',
    'privacy'    => $updated['privacy'] ?: 'Public',
    'updated_at' => $updated['updated_at']
], 'Post updated successfully!');
