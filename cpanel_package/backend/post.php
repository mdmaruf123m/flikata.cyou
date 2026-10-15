<?php
/**
 * Post & Video Deep Link Landing Page
 * Opens Flikata App immediately via deep link
 */

require_once __DIR__ . '/config/db.php';
$pdo = getDB();

$rawPostId = $_GET['id'] ?? $_GET['post_id'] ?? 0;
$rawReelId = $_GET['reel_id'] ?? 0;

if (!empty($rawReelId) && str_starts_with((string)$rawReelId, 'post_')) {
    $rawPostId = substr((string)$rawReelId, 5);
    $rawReelId = 0;
}

$postId = (int)$rawPostId;
$reelId = (int)$rawReelId;

$title = "Flikata - Social Network";
$content = "Check out this update on Flikata!";
$author = "Flikata User";
$imageUrl = "";
$videoUrl = "";
$deepLink = "flikata://home";

if ($postId > 0) {
    $stmt = $pdo->prepare("SELECT p.*, u.full_name, u.profile_pic FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?");
    $stmt->execute([$postId]);
    $post = $stmt->fetch();
    if ($post) {
        $author = $post['full_name'];
        $content = $post['content'] ?: "Post by " . $author;
        $title = $author . " on Flikata: \"" . mb_strimwidth($content, 0, 60, "...") . "\"";
        $imageUrl = $post['image_url'] ?: $post['profile_pic'];
        $videoUrl = $post['video_url'];
        $deepLink = "flikata://post/" . $postId;
    } else {
        $deepLink = "flikata://post/" . $postId;
    }
} elseif ($reelId > 0) {
    $stmt = $pdo->prepare("SELECT r.*, u.full_name, u.profile_pic FROM reels r JOIN users u ON r.user_id = u.id WHERE r.id = ?");
    $stmt->execute([$reelId]);
    $reel = $stmt->fetch();
    if ($reel) {
        $author = $reel['full_name'];
        $content = $reel['caption'] ?: "Reel by " . $author;
        $title = $author . "'s Reel on Flikata";
        $videoUrl = $reel['media_url'];
        $deepLink = "flikata://reel/" . $reelId;
    } else {
        $deepLink = "flikata://reel/" . $reelId;
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
    <!-- Open Graph for Facebook, WhatsApp, Messenger previews -->
    <meta property="og:title" content="<?= htmlspecialchars($title) ?>" />
    <meta property="og:description" content="<?= htmlspecialchars($content) ?>" />
    <?php if ($imageUrl): ?>
    <meta property="og:image" content="<?= htmlspecialchars($imageUrl) ?>" />
    <?php endif; ?>
    <?php if ($videoUrl): ?>
    <meta property="og:video" content="<?= htmlspecialchars($videoUrl) ?>" />
    <meta property="og:type" content="video.other" />
    <?php else: ?>
    <meta property="og:type" content="article" />
    <?php endif; ?>
    <meta property="og:site_name" content="Flikata" />

    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
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
    <div class="loading-text">Opening in Flikata...</div>

    <script>
        // Immediately redirect to Flikata app without requiring user interaction
        window.location.replace("<?= $deepLink ?>");
    </script>
</body>
</html>
