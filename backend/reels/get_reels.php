<?php
/**
 * Get Reels Feed
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$currentUser = getAuthUser($pdo);
$currentUserId = $currentUser ? $currentUser['id'] : 0;

$stmt = $pdo->prepare("
    SELECT 
        r.*,
        u.full_name AS author_name,
        u.profile_pic AS author_avatar,
        u.is_verified AS author_verified,
        (SELECT COUNT(*) FROM reel_likes WHERE reel_id = r.id AND user_id = ?) AS is_liked
    FROM reels r
    JOIN users u ON r.user_id = u.id
    ORDER BY r.id DESC
    LIMIT 50
");
$stmt->execute([$currentUserId]);
$reels = $stmt->fetchAll();

$formatted = [];
foreach ($reels as $r) {
    // Fetch comments for this reel
    $cStmt = $pdo->prepare("
        SELECT c.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.is_verified
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.reel_id = ?
        ORDER BY c.id DESC
        LIMIT 20
    ");
    $cStmt->execute([$r['id']]);
    $comments = $cStmt->fetchAll();

    $formatted[] = [
        'id'            => (string)$r['id'],
        'userId'        => (string)$r['user_id'],
        'user_id'       => (string)$r['user_id'],
        'authorName'    => $r['author_name'],
        'authorAvatar'  => $r['author_avatar'] ?: '',
        'isVerified'    => (bool)$r['author_verified'],
        'mediaUrl'      => $r['media_url'],
        'caption'       => $r['caption'] ?: '',
        'audioTitle'    => $r['audio_title'] ?: 'Original Audio',
        'likesCount'    => (int)$r['likes_count'],
        'commentsCount' => (int)$r['comments_count'],
        'sharesCount'   => (int)$r['shares_count'],
        'viewsCount'    => $r['views_count'] > 1000 ? round($r['views_count'] / 1000, 1) . 'K' : (string)$r['views_count'],
        'isLiked'       => (bool)$r['is_liked'],
        'comments'      => array_map(function($c) {
            return [
                'id'            => (string)$c['id'],
                'authorName'    => $c['author_name'],
                'authorAvatar'  => $c['author_avatar'] ?: '',
                'isVerified'    => (bool)$c['is_verified'],
                'text'          => $c['text'],
                'timestamp'     => $c['created_at'],
                'isLiked'       => false,
                'reactionEmoji' => $c['reaction_emoji']
            ];
        }, $comments)
    ];
}

jsonResponse(true, $formatted, 'Reels loaded.');
