<?php
/**
 * Audio Stream Relay between Caller & Receiver
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$bufferDir = __DIR__ . '/buffers';
if (!is_dir($bufferDir)) {
    @mkdir($bufferDir, 0777, true);
}

$callId = !empty($_REQUEST['call_id']) ? (int)$_REQUEST['call_id'] : 0;
$senderId = !empty($_REQUEST['sender_id']) ? (int)$_REQUEST['sender_id'] : 0;
$partnerId = !empty($_REQUEST['partner_id']) ? (int)$_REQUEST['partner_id'] : 0;

if (!$callId) {
    jsonResponse(false, null, 'Call ID required.');
}

$myFile = $bufferDir . '/buf_' . $callId . '_' . $senderId . '.bin';
$partnerFile = $bufferDir . '/buf_' . $callId . '_' . $partnerId . '.bin';

// Handle mute signal from sender
if (!empty($_REQUEST['is_muted'])) {
    if (file_exists($myFile)) {
        @unlink($myFile);
    }
    jsonResponse(true, null, 'Muted.');
}

// Upload audio buffer from sender
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $rawAudio = file_get_contents('php://input');
    if (!empty($rawAudio)) {
        file_put_contents($myFile, $rawAudio);
    }
}

// Fetch partner's audio buffer
$partnerAudio = '';
if (file_exists($partnerFile) && (time() - filemtime($partnerFile)) < 4) {
    $partnerAudio = file_get_contents($partnerFile);
    @unlink($partnerFile); // Consume once to prevent audio echo/stuttering
}

if (!empty($_GET['get_raw'])) {
    header('Content-Type: application/octet-stream');
    header('Cache-Control: no-cache, no-store, must-revalidate');
    echo $partnerAudio;
    exit;
}

jsonResponse(true, [
    'has_audio' => !empty($partnerAudio),
    'audio_b64' => !empty($partnerAudio) ? base64_encode($partnerAudio) : null
], 'Audio relay ok.');
