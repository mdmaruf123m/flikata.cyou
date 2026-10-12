<?php
/**
 * Get User Notifications
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    $uid = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : (!empty($_POST['user_id']) ? (int)$_POST['user_id'] : 0);
    if ($uid > 0) {
        $uStmt = $pdo->prepare("SELECT * FROM users WHERE id = ?");
        $uStmt->execute([$uid]);
        $user = $uStmt->fetch();
    }
}

if (!$user) {
    // Return empty list with success so client doesn't crash if session is starting
    jsonResponse(true, [], 'No active user session.');
}

$userId = (int)$user['id'];

$stmt = $pdo->prepare("
    SELECT n.*, 
           u.full_name AS from_name, 
           u.profile_pic AS from_avatar,
           u.is_verified AS from_verified
    FROM notifications n
    LEFT JOIN users u ON n.from_user_id = u.id
    WHERE n.user_id = ? OR n.user_id = 0
    ORDER BY n.id DESC
    LIMIT 100
");
$stmt->execute([$userId]);
$notifications = $stmt->fetchAll();

$formatted = array_map(function($n) {
    $isSystem = empty($n['from_user_id']);
    $title = !empty($n['title']) ? $n['title'] : ($isSystem ? 'Admin Announcement' : 'New Notification');
    
    // Choose clean avatar URL or empty
    $avatar = !empty($n['from_avatar']) ? $n['from_avatar'] : '';

    return [
        'id'            => (string)$n['id'],
        'title'         => $title,
        'message'       => (string)$n['message'],
        'type'          => !empty($n['type']) ? $n['type'] : 'general',
        'timestamp'     => date('M d, h:i A', strtotime($n['created_at'])),
        'isRead'        => (bool)$n['is_read'],
        'avatarUrl'     => $avatar,
        'postThumbnail' => null
    ];
}, $notifications);

jsonResponse(true, $formatted, 'Notifications loaded.');
