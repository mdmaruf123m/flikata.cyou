<?php
/**
 * Profile Deep Link Landing Page
 * Opens Flikata App profile directly
 */

require_once __DIR__ . '/config/db.php';
$pdo = getDB();

$userId = (int)($_GET['id'] ?? $_GET['user_id'] ?? 0);
$deepLink = $userId > 0 ? "flikata://profile/" . $userId : "flikata://home";

$title = "Flikata Profile";
if ($userId > 0) {
    $stmt = $pdo->prepare("SELECT full_name, profile_pic FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();
    if ($user) {
        $title = $user['full_name'] . " on Flikata";
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="refresh" content="0;url=<?= htmlspecialchars($deepLink) ?>">
    <title><?= htmlspecialchars($title) ?></title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #ffffff;
            color: #1c1e21;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
        }
        .loading-text {
            color: #65676B;
            font-size: 15px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="loading-text">Opening profile in Flikata...</div>

    <script>
        window.location.replace("<?= $deepLink ?>");
    </script>
</body>
</html>
