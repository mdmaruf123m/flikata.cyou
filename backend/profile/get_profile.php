<?php
/**
 * Get User Profile Details & Statistics
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$targetUserId = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : ($auth ? (int)$auth['id'] : 0);

if (!$targetUserId) {
    jsonResponse(false, null, 'User ID or authorization required.');
}

$stmt = $pdo->prepare("
    SELECT id, full_name, email, phone, profile_pic, cover_pic, bio, occupation, location, gender, dob, is_verified, is_online, wallet_balance, verified_until, created_at, updated_at 
    FROM users 
    WHERE id = ?
");
$stmt->execute([$targetUserId]);
$user = $stmt->fetch();

if (!$user) {
    jsonResponse(false, null, 'User not found.');
}

// User posts count
$pStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ?");
$pStmt->execute([$targetUserId]);
$user['posts_count'] = (int)$pStmt->fetchColumn();

// User photos count
$imgStmt = $pdo->prepare("SELECT COUNT(*) FROM posts WHERE user_id = ? AND image_url IS NOT NULL");
$imgStmt->execute([$targetUserId]);
$user['photos_count'] = (int)$imgStmt->fetchColumn();

// User reels count
$rStmt = $pdo->prepare("SELECT COUNT(*) FROM reels WHERE user_id = ?");
$rStmt->execute([$targetUserId]);
$user['reels_count'] = (int)$rStmt->fetchColumn();

$secondsAgo = !empty($user['updated_at']) ? (time() - strtotime($user['updated_at'])) : 99999;
if ((int)($user['is_verified'] ?? 0) === 1 && !empty($user['verified_until'])) {
    $exp = strtotime($user['verified_until']);
    if ($exp < strtotime('2090-01-01') && time() > ($exp + 3 * 86400)) {
        $pdo->prepare("UPDATE users SET is_verified = 0 WHERE id = ?")->execute([$targetUserId]);
        $user['is_verified'] = 0;
    }
}
$user['is_verified'] = (int)($user['is_verified'] ?? 0);
$user['wallet_balance'] = (float)($user['wallet_balance'] ?? 0.00);
$user['is_online'] = ((int)($user['is_online'] ?? 0) === 1 && $secondsAgo <= 60) ? 1 : 0;
$user['joinedDate'] = 'Joined ' . date('F Y', strtotime($user['created_at']));

jsonResponse(true, $user, 'Profile loaded.');
