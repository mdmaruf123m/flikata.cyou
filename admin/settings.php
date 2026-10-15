<?php
/**
 * Admin Settings Page
 * Change Admin Password & Configure System Variables
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'এডমিন সেটিংস - Flikata Admin';
$pageHeading = 'এডমিন ও সিস্টেম সেটিংস';
$pageSubheading = 'পাসওয়ার্ড পরিবর্তন, স্টোরি এক্সপায়ার সময় ও সিস্টেম কনফিগারেশন';

$successMsg = '';
$errorMsg = '';

// Handle Password Change
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['change_password'])) {
    $currentPass = trim($_POST['current_password'] ?? '');
    $newPass = trim($_POST['new_password'] ?? '');
    $confirmPass = trim($_POST['confirm_password'] ?? '');

    if (empty($currentPass) || empty($newPass) || empty($confirmPass)) {
        $errorMsg = 'সকল পাসওয়ার্ড ফিল্ড পূরণ করা আবশ্যক।';
    } elseif ($newPass !== $confirmPass) {
        $errorMsg = 'নতুন পাসওয়ার্ড এবং নিশ্চিতকরণ পাসওয়ার্ড মেলেনি!';
    } elseif (strlen($newPass) < 6) {
        $errorMsg = 'নতুন পাসওয়ার্ড ন্যূনতম ৬ অক্ষরের হতে হবে।';
    } else {
        $stmt = $pdo->prepare("SELECT * FROM admins WHERE id = ? LIMIT 1");
        $stmt->execute([$_SESSION['admin_id']]);
        $adm = $stmt->fetch();

        if ($adm && (password_verify($currentPass, $adm['password_hash']) || $currentPass === $adm['password_hash'])) {
            $newHash = password_hash($newPass, PASSWORD_BCRYPT);
            $up = $pdo->prepare("UPDATE admins SET password_hash = ? WHERE id = ?");
            $up->execute([$newHash, $adm['id']]);
            $successMsg = 'এডমিন পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!';
        } else {
            $errorMsg = 'বর্তমান পাসওয়ার্ডটি সঠিক নয়!';
        }
    }
}

// Handle App Settings (Story expiry hours & Reel seconds)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['update_app_settings'])) {
    $expiryHours = (int)($_POST['expiry_hours'] ?? 24);
    $maxDuration = (int)($_POST['max_duration'] ?? 60);

    try {
        $pdo->prepare("UPDATE story_settings SET expiry_hours = ? WHERE id = 1")->execute([$expiryHours]);
        $pdo->prepare("UPDATE reel_settings SET max_duration_seconds = ? WHERE id = 1")->execute([$maxDuration]);
        $successMsg = 'অ্যাপ কনফিগারেশন সফলভাবে আপডেট করা হয়েছে!';
    } catch (Exception $e) {
        $errorMsg = 'কনফিগারেশন সেভ করতে ব্যর্থ: ' . $e->getMessage();
    }
}

// Handle Monetization & Dashboard Settings
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['update_monetization_settings'])) {
    $minFollowers = (int)($_POST['min_followers'] ?? 1000);
    $minPosts = (int)($_POST['min_posts'] ?? 10);
    $minReels = (int)($_POST['min_reels'] ?? 5);
    $minDays = (int)($_POST['min_account_age_days'] ?? 30);
    $postLimit = (int)($_POST['dashboard_posts_limit'] ?? 5);
    $revShare = (int)($_POST['revenue_share_percent'] ?? 55);

    try {
        $up = $pdo->prepare("
            INSERT INTO monetization_settings 
            (id, min_followers, min_posts, min_reels, min_account_age_days, dashboard_posts_limit, revenue_share_percent)
            VALUES (1, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              min_followers = VALUES(min_followers),
              min_posts = VALUES(min_posts),
              min_reels = VALUES(min_reels),
              min_account_age_days = VALUES(min_account_age_days),
              dashboard_posts_limit = VALUES(dashboard_posts_limit),
              revenue_share_percent = VALUES(revenue_share_percent)
        ");
        $up->execute([$minFollowers, $minPosts, $minReels, $minDays, $postLimit, $revShare]);
        $successMsg = 'মনিটাইজেশন ও ড্যাশবোর্ড সেটিংস সফলভাবে আপডেট করা হয়েছে!';
    } catch (Exception $e) {
        $errorMsg = 'সেটিংস সেভ করতে ব্যর্থ: ' . $e->getMessage();
    }
}

// Fetch current app settings
$currExpiry = 24;
try {
    $currExpiry = (int)$pdo->query("SELECT expiry_hours FROM story_settings WHERE id = 1")->fetchColumn() ?: 24;
} catch (Exception $e) {}

$currReelSec = 60;
try {
    $currReelSec = (int)$pdo->query("SELECT max_duration_seconds FROM reel_settings WHERE id = 1")->fetchColumn() ?: 60;
} catch (Exception $e) {}

// Fetch monetization settings
$mSettings = [
    'min_followers' => 1000,
    'min_posts' => 10,
    'min_reels' => 5,
    'min_account_age_days' => 30,
    'dashboard_posts_limit' => 5,
    'revenue_share_percent' => 55
];
try {
    $mRow = $pdo->query("SELECT * FROM monetization_settings WHERE id = 1 LIMIT 1")->fetch(PDO::FETCH_ASSOC);
    if ($mRow) $mSettings = array_merge($mSettings, $mRow);
} catch (Exception $e) {}

require_once __DIR__ . '/includes/header.php';
?>

<div class="row g-4">
    <!-- Change Admin Password Card -->
    <div class="col-12 col-md-6">
        <div class="card-custom h-100">
            <h6 class="fw-bold text-dark mb-1"><i class="bi bi-shield-lock-fill text-primary me-2"></i>এডমিন পাসওয়ার্ড পরিবর্তন</h6>
            <small class="text-muted d-block mb-3">লগইন ক্রেডেনশিয়াল সুরক্ষিত রাখতে নিয়মিত পাসওয়ার্ড পরিবর্তন করুন</small>

            <?php if (!empty($successMsg)): ?>
                <div class="alert alert-success border-0 rounded-3 py-2 px-3 mb-3 small"><?= htmlspecialchars($successMsg) ?></div>
            <?php endif; ?>
            <?php if (!empty($errorMsg)): ?>
                <div class="alert alert-danger border-0 rounded-3 py-2 px-3 mb-3 small"><?= htmlspecialchars($errorMsg) ?></div>
            <?php endif; ?>

            <form method="POST" action="settings.php">
                <input type="hidden" name="change_password" value="1">

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">এডমিন ইমেইল</label>
                    <input type="text" class="form-control bg-light rounded-3" value="<?= htmlspecialchars($_SESSION['admin_email'] ?? 'admin@gmail.com') ?>" readonly>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">বর্তমান পাসওয়ার্ড</label>
                    <input type="password" name="current_password" class="form-control rounded-3" placeholder="বর্তমান পাসওয়ার্ড দিন" required>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">নতুন পাসওয়ার্ড</label>
                    <input type="password" name="new_password" class="form-control rounded-3" placeholder="নতুন শক্তিশালী পাসওয়ার্ড দিন" required>
                </div>

                <div class="mb-4">
                    <label class="form-label small fw-bold text-secondary">নতুন পাসওয়ার্ড নিশ্চিত করুন</label>
                    <input type="password" name="confirm_password" class="form-control rounded-3" placeholder="পুনরায় নতুন পাসওয়ার্ড দিন" required>
                </div>

                <button type="submit" class="btn btn-primary-custom w-100">
                    <i class="bi bi-check2-circle me-1"></i> পাসওয়ার্ড আপডেট করুন
                </button>
            </form>
        </div>
    </div>

    <!-- App System Configurations Card -->
    <div class="col-12 col-md-6">
        <div class="card-custom h-100">
            <h6 class="fw-bold text-dark mb-1"><i class="bi bi-sliders text-info me-2"></i>অ্যাপ কনফিগারেশন</h6>
            <small class="text-muted d-block mb-3">সোশ্যাল অ্যাপ্লিকেশনের স্বয়ংক্রিয় আচরণ নিয়ন্ত্রণ</small>

            <form method="POST" action="settings.php">
                <input type="hidden" name="update_app_settings" value="1">

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">স্টোরিজ মেয়াদ (ঘন্টায় / Hours)</label>
                    <div class="input-group">
                        <input type="number" name="expiry_hours" class="form-control rounded-3" value="<?= $currExpiry ?>" min="1" max="168" required>
                        <span class="input-group-text bg-light border">ঘন্টা (Hours)</span>
                    </div>
                    <small class="text-muted">ডিফল্ট ২৪ ঘন্টা। এটি পরিবর্তন করলে নতুন সকল স্টোরিজ এই সময় অনুযায়ী এক্সপায়ার হবে।</small>
                </div>

                <div class="mb-4">
                    <label class="form-label small fw-bold text-secondary">রিলস / শর্ট ভিডিও সর্বোচ্চ সময়সীমা (সেকেন্ড)</label>
                    <div class="input-group">
                        <input type="number" name="max_duration" class="form-control rounded-3" value="<?= $currReelSec ?>" min="10" max="300" required>
                        <span class="input-group-text bg-light border">সেকেন্ড</span>
                    </div>
                    <small class="text-muted">ডিফল্ট ৬০ সেকেন্ড (১ মিনিট)। ব্যবহারকারীদের আপলোড করা রিলসের সর্বোচ্চ সময়সীমা।</small>
                </div>

                <div class="p-3 bg-light rounded-3 mb-4 small border">
                    <div class="fw-bold text-dark mb-1"><i class="bi bi-database-check text-success me-1"></i> ডাটাবেজ স্থিতি:</div>
                    <div class="text-muted">Host: <strong><?= defined('DB_HOST') ? DB_HOST : 'localhost' ?></strong> | Database: <strong><?= defined('DB_NAME') ? DB_NAME : 'flikata_bd' ?></strong></div>
                </div>

                <button type="submit" class="btn btn-primary-custom w-100">
                    <i class="bi bi-save2 me-1"></i> সেটিংস সংরক্ষণ করুন
                </button>
            </form>
        </div>
    </div>

    <!-- Monetization Requirements & Dashboard Content Settings Card -->
    <div class="col-12">
        <div class="card-custom">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div>
                    <h6 class="fw-bold text-dark mb-1"><i class="bi bi-cash-coin text-success me-2"></i>মনিটাইজেশন রিকোয়ারমেন্ট ও ড্যাশবোর্ড সেটিংস</h6>
                    <small class="text-muted">কনটেন্ট নির্মাতাদের মনিটাইজেশন অর্জনের শর্তাবলী ও ড্যাশবোর্ড সাম্প্রতিক পোস্ট সংখ্যা নির্ধারণ</small>
                </div>
                <span class="badge bg-success-subtle text-success border border-success px-3 py-2 fw-bold">
                    রেভিনিউ শেয়ার: <?= $mSettings['revenue_share_percent'] ?>%
                </span>
            </div>

            <form method="POST" action="settings.php">
                <input type="hidden" name="update_monetization_settings" value="1">

                <div class="row g-3">
                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-bold text-secondary">প্রয়োজনীয় ফলোয়ার্স (Followers)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-people"></i></span>
                            <input type="number" name="min_followers" class="form-control" value="<?= $mSettings['min_followers'] ?>" min="0" required>
                        </div>
                        <small class="text-muted">ডিফল্ট: ১,০০০ ফলোয়ার্স</small>
                    </div>

                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-bold text-secondary">প্রয়োজনীয় মোট পোস্ট (Posts)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-file-text"></i></span>
                            <input type="number" name="min_posts" class="form-control" value="<?= $mSettings['min_posts'] ?>" min="0" required>
                        </div>
                        <small class="text-muted">ডিফল্ট: ১০টি পোস্ট</small>
                    </div>

                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-bold text-secondary">প্রয়োজনীয় রিলস ভিডিও (Reels)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-play-btn"></i></span>
                            <input type="number" name="min_reels" class="form-control" value="<?= $mSettings['min_reels'] ?>" min="0" required>
                        </div>
                        <small class="text-muted">ডিফল্ট: ৫টি রিলস ভিডিও</small>
                    </div>

                    <div class="col-12 col-md-3">
                        <label class="form-label small fw-bold text-secondary">একাউন্টের বয়স (দিন / Days)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-calendar-event"></i></span>
                            <input type="number" name="min_account_age_days" class="form-control" value="<?= $mSettings['min_account_age_days'] ?>" min="0" required>
                        </div>
                        <small class="text-muted">ডিফল্ট: ৩০ দিন বয়স</small>
                    </div>

                    <div class="col-12 col-md-6">
                        <label class="form-label small fw-bold text-secondary">ড্যাশবোর্ডে সাম্প্রতিক কতটি পোস্ট শো করবে (Post Limit)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-grid-3x3-gap"></i></span>
                            <input type="number" name="dashboard_posts_limit" class="form-control" value="<?= $mSettings['dashboard_posts_limit'] ?>" min="1" max="50" required>
                            <span class="input-group-text bg-light">টি পোস্ট</span>
                        </div>
                        <small class="text-muted">ইউজার ড্যাশবোর্ডে সাম্প্রতিক যতগুলো পোস্ট ও রিলস শো করবে (ডিফল্ট: ৫টি)। বাকিগুলো "See More" এ দেখা যাবে।</small>
                    </div>

                    <div class="col-12 col-md-6">
                        <label class="form-label small fw-bold text-secondary">এড ভিউস রেভিনিউ শেয়ার শতকরা হার (%)</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-percent"></i></span>
                            <input type="number" name="revenue_share_percent" class="form-control" value="<?= $mSettings['revenue_share_percent'] ?>" min="1" max="100" required>
                            <span class="input-group-text bg-light">% Revenue</span>
                        </div>
                        <small class="text-muted">ক্রিয়েটরদের জন্য এড ভিউ আর্নিং শেয়ার (ডিফল্ট: ৫৫%)</small>
                    </div>
                </div>

                <div class="mt-4 text-end">
                    <button type="submit" class="btn btn-success px-4 py-2">
                        <i class="bi bi-check2-circle me-1"></i> মনিটাইজেশন সেটিংস সেভ করুন
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
