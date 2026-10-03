<?php
/**
 * Get Conversation Messages
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$user = getAuthUser($pdo);
if (!$user) {
    jsonResponse(false, null, 'Authentication required.');
}

$convId = (int)($_GET['conversation_id'] ?? 0);
$partnerId = (int)($_GET['partner_id'] ?? 0);
$myId = (int)$user['id'];

// If no conversation_id, look up or create one using partner_id
if (!$convId && $partnerId) {
    $min = min($myId, $partnerId);
    $max = max($myId, $partnerId);
    $stmt = $pdo->prepare("SELECT id FROM conversations WHERE user_one_id = ? AND user_two_id = ? LIMIT 1");
    $stmt->execute([$min, $max]);
    $conv = $stmt->fetch();
    if ($conv) {
        $convId = (int)$conv['id'];
    } else {
        $cIns = $pdo->prepare("INSERT INTO conversations (user_one_id, user_two_id) VALUES (?, ?)");
        $cIns->execute([$min, $max]);
        $convId = (int)$pdo->lastInsertId();
    }
}

if (!$convId) {
    jsonResponse(false, null, 'Valid conversation_id or partner_id is required.');
}

// Mark unread messages as read
$upd = $pdo->prepare("UPDATE messages SET is_read = 1 WHERE conversation_id = ? AND receiver_id = ?");
$upd->execute([$convId, $myId]);

// Fetch messages
$stmt = $pdo->prepare("
    SELECT m.*, u.full_name AS sender_name,
           r.text AS reply_text, r.image_url AS reply_image, r.video_url AS reply_video,
           ru.full_name AS reply_sender_name
    FROM messages m
    JOIN users u ON m.sender_id = u.id
    LEFT JOIN messages r ON m.reply_to_id = r.id
    LEFT JOIN users ru ON r.sender_id = ru.id
    WHERE m.conversation_id = ?
    ORDER BY m.id ASC
");
$stmt->execute([$convId]);
$messages = $stmt->fetchAll();

$formatted = array_map(function($m) use ($myId) {
    $replyObj = null;
    if ($m['reply_to_id']) {
        $replyObj = [
            'id'         => (string)$m['reply_to_id'],
            'senderName' => $m['reply_sender_name'] ?: 'User',
            'text'       => $m['reply_text'] ?: ($m['reply_image'] ? 'Photo' : 'Video')
        ];
    }

    return [
        'id'           => (string)$m['id'],
        'text'         => $m['text'] ?: '',
        'imageUrl'     => $m['image_url'],
        'videoUrl'     => $m['video_url'],
        'isMe'         => ((int)$m['sender_id'] === $myId),
        'time'         => date('h:i A', strtotime($m['created_at'])),
        'isEdited'     => (bool)$m['is_edited'],
        'isCallNotice' => (bool)$m['is_call_notice'],
        'replyTo'      => $replyObj
    ];
}, $messages);

jsonResponse(true, [
    'conversation_id' => $convId,
    'messages'        => $formatted
], 'Messages loaded.');
