<?php
/**
 * Admin API: Deposit Payment Methods Management
 * Flikata Admin Panel
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$action = !empty($_REQUEST['action']) ? trim($_REQUEST['action']) : '';
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

if ($action === 'create') {
    $name = !empty($input['name']) ? trim($input['name']) : '';
    $number = !empty($input['number']) ? trim($input['number']) : '';
    $imageUrl = !empty($input['image_url']) ? trim($input['image_url']) : '';
    $desc = !empty($input['description']) ? trim($input['description']) : '';

    if (empty($name) || empty($number)) {
        jsonResponse(false, null, 'Method name and number/account are required.');
    }

    $isActive = 1;
    if (isset($input['is_active'])) {
        $v = $input['is_active'];
        if ($v === 0 || $v === '0' || $v === false || $v === 'false') {
            $isActive = 0;
        } else {
            $isActive = 1;
        }
    }

    $stmt = $pdo->prepare("INSERT INTO deposit_methods (name, number, image_url, description, is_active) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$name, $number, $imageUrl, $desc, $isActive]);
    $newId = (int)$pdo->lastInsertId();

    jsonResponse(true, ['method_id' => $newId], 'Deposit method created successfully.');
}

if ($action === 'edit') {
    $id = !empty($input['id']) ? (int)$input['id'] : 0;
    $name = !empty($input['name']) ? trim($input['name']) : '';
    $number = !empty($input['number']) ? trim($input['number']) : '';
    $imageUrl = !empty($input['image_url']) ? trim($input['image_url']) : '';
    $desc = !empty($input['description']) ? trim($input['description']) : '';
    
    $isActive = 1;
    if (isset($input['is_active'])) {
        $v = $input['is_active'];
        if ($v === 0 || $v === '0' || $v === false || $v === 'false') {
            $isActive = 0;
        } else {
            $isActive = 1;
        }
    }

    if (!$id || empty($name) || empty($number)) {
        jsonResponse(false, null, 'Method ID, name, and number are required.');
    }

    if (empty($imageUrl)) {
        $old = $pdo->prepare("SELECT image_url FROM deposit_methods WHERE id = ?");
        $old->execute([$id]);
        $imageUrl = $old->fetchColumn() ?: '';
    }

    $stmt = $pdo->prepare("UPDATE deposit_methods SET name = ?, number = ?, image_url = ?, description = ?, is_active = ? WHERE id = ?");
    $stmt->execute([$name, $number, $imageUrl, $desc, $isActive, $id]);

    jsonResponse(true, null, 'Deposit method updated successfully.');
}

if ($action === 'delete') {
    $id = !empty($input['id']) ? (int)$input['id'] : 0;
    if (!$id) {
        jsonResponse(false, null, 'Method ID required.');
    }

    $stmt = $pdo->prepare("DELETE FROM deposit_methods WHERE id = ?");
    $stmt->execute([$id]);

    jsonResponse(true, null, 'Deposit method deleted successfully.');
}

// Default: Fetch all methods
$stmt = $pdo->query("SELECT * FROM deposit_methods ORDER BY id DESC");
$raw = $stmt->fetchAll();
$methods = [];
$isHttps = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ||
           (isset($_SERVER['HTTP_X_FORWARDED_PROTO']) && $_SERVER['HTTP_X_FORWARDED_PROTO'] === 'https') ||
           (isset($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == 443);
$host = $_SERVER['HTTP_HOST'] ?? 'flikata.cyou';
$baseHostUrl = ($isHttps ? "https://" : "http://") . $host;

foreach ($raw as $m) {
    $img = $m['image_url'] ?? '';
    if (!empty($img) && !preg_match('#^https?://#i', $img)) {
        $img = rtrim($baseHostUrl, '/') . '/' . ltrim($img, '/');
    }
    $methods[] = [
        'id'          => (int)$m['id'],
        'name'        => (string)$m['name'],
        'number'      => (string)$m['number'],
        'image_url'   => $img ?: null,
        'description' => (string)($m['description'] ?? ''),
        'is_active'   => (int)$m['is_active']
    ];
}

jsonResponse(true, $methods, 'Deposit methods loaded.');
