<?php
/**
 * Record View on Reel Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$reelId = (int)($input['reel_id'] ?? ($_GET['reel_id'] ?? 0));

if (!$reelId) {
    jsonResponse(false, null, 'Invalid reel ID.');
}

$stmt = $pdo->prepare("UPDATE reels SET views_count = views_count + 1 WHERE id = ?");
$stmt->execute([$reelId]);

$q = $pdo->prepare("SELECT views_count FROM reels WHERE id = ?");
$q->execute([$reelId]);
$views = (int)$q->fetchColumn();

jsonResponse(true, ['views_count' => $views], 'View recorded.');
