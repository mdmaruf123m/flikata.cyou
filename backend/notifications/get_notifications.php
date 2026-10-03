<?php
/**
 * Get User Notifications
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$stmt = $pdo->prepare("
    SELECT n.*, u.full_name AS from_name, u.profile_pic AS from_avatar
    FROM notifications n
    LEFT JOIN users u ON n.from_user_id = u.id
    WHERE n.user_id = ?
    ORDER BY n.id DESC
    LIMIT 50
");
$stmt->execute([$user['id']]);
$notifications = $stmt->fetchAll();

$formatted = array_map(function($n) {
    return [
        'id'        => (string)$n['id'],
        'title'     => $n['title'],
        'message'   => $n['message'],
        'type'      => $n['type'],
        'timestamp' => date('M d, h:i A', strtotime($n['created_at'])),
        'isRead'    => (bool)$n['is_read'],
        'avatarUrl' => $n['from_avatar'] ?: ''
    ];
}, $notifications);

jsonResponse(true, $formatted, 'Notifications loaded.');
