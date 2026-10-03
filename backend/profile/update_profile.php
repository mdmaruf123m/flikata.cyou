<?php
/**
 * Update Profile Details
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

$fullName   = trim($input['full_name'] ?? $user['full_name']);
$bio        = trim($input['bio'] ?? $user['bio']);
$occupation = trim($input['occupation'] ?? $user['occupation']);
$location   = trim($input['location'] ?? $user['location']);
$profilePic = trim($input['profile_pic'] ?? $user['profile_pic']);
$coverPic   = trim($input['cover_pic'] ?? $user['cover_pic']);

$stmt = $pdo->prepare("
    UPDATE users 
    SET full_name = ?, bio = ?, occupation = ?, location = ?, profile_pic = ?, cover_pic = ?
    WHERE id = ?
");
$stmt->execute([
    $fullName,
    $bio,
    $occupation,
    $location,
    $profilePic ?: null,
    $coverPic ?: null,
    $user['id']
]);

$stmt = $pdo->prepare("SELECT id, full_name, email, phone, profile_pic, cover_pic, bio, occupation, location, is_verified, auth_token FROM users WHERE id = ?");
$stmt->execute([$user['id']]);
$updatedUser = $stmt->fetch();

jsonResponse(true, $updatedUser, 'Profile updated successfully!');
