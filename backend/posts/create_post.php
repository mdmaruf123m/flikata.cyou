<?php
/**
 * Create New Post Endpoint
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
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(401);
    echo json_encode(['error' => 'Authentication required.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$content  = trim($input['content'] ?? '');
$imageUrl = trim($input['image_url'] ?? '');
$videoUrl = trim($input['video_url'] ?? '');
$privacy  = trim($input['privacy'] ?? 'Public');
$bgColor  = trim($input['bg_color'] ?? $input['background_style'] ?? '');
$bucketId = !empty($input['bucket_id']) ? (int)$input['bucket_id'] : null;

if (!$content && !$imageUrl && !$videoUrl) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(400);
    echo json_encode(['error' => 'Post content, image, or video is required.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$stmt = $pdo->prepare("
    INSERT INTO posts (user_id, content, image_url, video_url, privacy, bg_color, bucket_id)
    VALUES (?, ?, ?, ?, ?, ?, ?)
");
$stmt->execute([
    $user['id'],
    $content ?: null,
    $imageUrl ?: null,
    $videoUrl ?: null,
    $privacy,
    $bgColor ?: null,
    $bucketId
]);

$postId = $pdo->lastInsertId();

// If this post has a video, automatically make it available in Reels as well
if ($videoUrl) {
    $rStmt = $pdo->prepare("
        INSERT INTO reels (user_id, media_url, caption, audio_title, bucket_id)
        VALUES (?, ?, ?, ?, ?)
    ");
    $rStmt->execute([
        $user['id'],
        $videoUrl,
        $content ?: 'New Reel by ' . $user['full_name'],
        'Original Audio - ' . $user['full_name'],
        $bucketId
    ]);
}

$postObj = [
    'id'               => (string)$postId,
    'user_id'          => (string)$user['id'],
    'author_name'      => $user['full_name'],
    'author_avatar'    => $user['profile_pic'] ?: '',
    'is_verified'      => (bool)$user['is_verified'],
    'timestamp'        => 'Just now',
    'content'          => $content ?: '',
    'background_style' => $bgColor ?: null,
    'image_url'        => $imageUrl ?: null,
    'video_url'        => $videoUrl ?: null,
    'is_video'         => !empty($videoUrl),
    'likes_count'      => 0,
    'comments_count'   => 0,
    'shares_count'     => 0,
    'is_liked'         => false,
    'privacy'          => $privacy
];

header('Content-Type: application/json; charset=utf-8');
echo json_encode($postObj, JSON_UNESCAPED_UNICODE);
exit;
