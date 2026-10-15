<?php
/**
 * Admin API: Verification Packages & Requirements Settings
 * Flikata Admin Panel
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$action = !empty($_REQUEST['action']) ? trim($_REQUEST['action']) : '';
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

if ($action === 'save_settings') {
    $idCardReq = !empty($input['id_card_required']) ? 1 : 0;
    $selfieReq = !empty($input['selfie_required']) ? 1 : 0;
    $graceDays = isset($input['grace_period_days']) ? max(0, (int)$input['grace_period_days']) : 3;

    $stmt = $pdo->prepare("UPDATE verification_settings SET id_card_required = ?, selfie_required = ?, grace_period_days = ? WHERE id = 1");
    $stmt->execute([$idCardReq, $selfieReq, $graceDays]);

    jsonResponse(true, [
        'id_card_required'  => (bool)$idCardReq,
        'selfie_required'   => (bool)$selfieReq,
        'grace_period_days' => $graceDays
    ], 'Settings updated successfully.');
}

if ($action === 'create_package') {
    $title = !empty($input['title']) ? trim($input['title']) : '';
    $description = !empty($input['description']) ? trim($input['description']) : null;
    $price = isset($input['price']) ? (float)$input['price'] : 0.00;
    $durationType = !empty($input['duration_type']) ? strtolower(trim($input['duration_type'])) : 'month';
    $durationValue = isset($input['duration_value']) ? (int)$input['duration_value'] : 1;

    if (!in_array($durationType, ['day', 'month', 'year', 'lifetime'])) {
        $durationType = 'month';
    }

    if ($durationType === 'lifetime') {
        $durationValue = 0;
    } else {
        $durationValue = max(1, $durationValue);
    }

    if (empty($title)) {
        jsonResponse(false, null, 'Package title is required.');
    }

    $stmt = $pdo->prepare("INSERT INTO verification_packages (title, description, price, duration_type, duration_value, is_active) VALUES (?, ?, ?, ?, ?, 1)");
    $stmt->execute([$title, $description, $price, $durationType, $durationValue]);
    $newId = (int)$pdo->lastInsertId();

    jsonResponse(true, ['package_id' => $newId], 'Package created successfully.');
}

if ($action === 'edit_package') {
    $id = !empty($input['id']) ? (int)$input['id'] : 0;
    $title = !empty($input['title']) ? trim($input['title']) : '';
    $description = !empty($input['description']) ? trim($input['description']) : null;
    $price = isset($input['price']) ? (float)$input['price'] : 0.00;
    $durationType = !empty($input['duration_type']) ? strtolower(trim($input['duration_type'])) : 'month';
    $durationValue = isset($input['duration_value']) ? (int)$input['duration_value'] : 1;

    if (!$id || empty($title)) {
        jsonResponse(false, null, 'Package ID and Title required.');
    }

    if ($durationType === 'lifetime') {
        $durationValue = 0;
    } else {
        $durationValue = max(1, $durationValue);
    }

    $stmt = $pdo->prepare("UPDATE verification_packages SET title = ?, description = ?, price = ?, duration_type = ?, duration_value = ? WHERE id = ?");
    $stmt->execute([$title, $description, $price, $durationType, $durationValue, $id]);

    jsonResponse(true, null, 'Package updated successfully.');
}

if ($action === 'delete_package') {
    $id = !empty($input['id']) ? (int)$input['id'] : 0;
    if (!$id) {
        jsonResponse(false, null, 'Package ID required.');
    }

    $stmt = $pdo->prepare("DELETE FROM verification_packages WHERE id = ?");
    $stmt->execute([$id]);

    jsonResponse(true, null, 'Package deleted successfully.');
}

// Default: Fetch all packages & settings
$sStmt = $pdo->query("SELECT * FROM verification_settings WHERE id = 1");
$settings = $sStmt->fetch() ?: [
    'id_card_required' => 0,
    'selfie_required'  => 0,
    'grace_period_days' => 3
];

$pStmt = $pdo->query("SELECT * FROM verification_packages ORDER BY id DESC");
$packages = $pStmt->fetchAll();

jsonResponse(true, [
    'settings' => [
        'id_card_required'  => (bool)$settings['id_card_required'],
        'selfie_required'   => (bool)$settings['selfie_required'],
        'grace_period_days' => (int)$settings['grace_period_days']
    ],
    'packages' => $packages
], 'Admin packages loaded.');
