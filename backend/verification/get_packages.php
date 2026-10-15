<?php
/**
 * Get Verification Packages & Live Status
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$userId = !empty($_GET['user_id']) ? (int)$_GET['user_id'] : ($auth ? (int)$auth['id'] : 0);

// 1. Fetch settings
$sStmt = $pdo->query("SELECT * FROM verification_settings WHERE id = 1");
$settings = $sStmt->fetch() ?: [
    'id_card_required' => 0,
    'selfie_required'  => 0,
    'grace_period_days' => 3
];

$graceDays = (int)($settings['grace_period_days'] ?? 3);

// 2. Fetch packages (only active)
$pStmt = $pdo->query("SELECT * FROM verification_packages WHERE is_active = 1 ORDER BY price ASC");
$packages = $pStmt->fetchAll();

// 3. User verification status
$userInfo = null;
if ($userId) {
    $uStmt = $pdo->prepare("SELECT id, full_name, profile_pic, is_verified, verified_until, wallet_balance FROM users WHERE id = ?");
    $uStmt->execute([$userId]);
    $u = $uStmt->fetch();

    if ($u) {
        $isVerified = (int)$u['is_verified'] === 1;
        $verifiedUntil = $u['verified_until'];
        $remainingSeconds = 0;
        $isGraceActive = false;
        $isLifetime = false;

        if ($isVerified && !empty($verifiedUntil)) {
            $expiryTime = strtotime($verifiedUntil);
            $now = time();
            $graceEndTime = $expiryTime + ($graceDays * 86400);

            if ($expiryTime >= strtotime('2090-01-01')) {
                $isLifetime = true;
                $remainingSeconds = 999999999;
            } elseif ($now <= $expiryTime) {
                $remainingSeconds = $expiryTime - $now;
            } elseif ($now <= $graceEndTime) {
                // In grace period
                $isGraceActive = true;
                $remainingSeconds = $graceEndTime - $now;
            } else {
                // Expired beyond grace period -> remove badge
                $pdo->prepare("UPDATE users SET is_verified = 0 WHERE id = ?")->execute([$userId]);
                $isVerified = false;
                $remainingSeconds = 0;
            }
        }

        $userInfo = [
            'id'                => (int)$u['id'],
            'full_name'         => $u['full_name'],
            'profile_pic'       => $u['profile_pic'],
            'is_verified'       => $isVerified,
            'verified_until'    => $verifiedUntil,
            'is_lifetime'       => $isLifetime,
            'remaining_seconds' => $remainingSeconds,
            'is_grace_active'   => $isGraceActive,
            'wallet_balance'    => (float)($u['wallet_balance'] ?? 0.00)
        ];
    }
}

// 4. Check if user has a pending verification request
$pendingRequest = null;
if ($userId) {
    $rStmt = $pdo->prepare("SELECT * FROM verification_requests WHERE user_id = ? AND status = 'pending' ORDER BY id DESC LIMIT 1");
    $rStmt->execute([$userId]);
    $pendingRequest = $rStmt->fetch();
}

jsonResponse(true, [
    'settings'        => [
        'id_card_required'  => (bool)$settings['id_card_required'],
        'selfie_required'   => (bool)$settings['selfie_required'],
        'grace_period_days' => (int)$settings['grace_period_days']
    ],
    'packages'        => $packages,
    'user'            => $userInfo,
    'pending_request' => $pendingRequest
], 'Packages loaded.');
