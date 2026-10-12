<?php
/**
 * Global Page Notifications (পেজ নোটিফিকেশন)
 * Broadcast notifications to ALL users at once
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'পেজ নোটিফিকেশন - Flikata Admin';
$pageHeading = 'পেজ নোটিফিকেশন (Global Broadcast)';
$pageSubheading = 'এক ক্লিকে সকল নিবন্ধিত ইউজারের নোটিফিকেশন ইনবক্সে বার্তা ও আপডেট প্রেরণ';

// Total active recipients
$totalUsers = (int)$pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();

// Handle Form Submission directly if submitted via POST
$successMsg = '';
$errorMsg = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['broadcast_notification'])) {
    $title = trim($_POST['title'] ?? '');
    $message = trim($_POST['message'] ?? '');
    $type = trim($_POST['type'] ?? 'announcement');

    if (empty($title) || empty($message)) {
        $errorMsg = 'অনুগ্রহ করে শিরোনাম ও বার্তার বিবরণ লিখুন।';
    } else {
        // Fetch all user IDs
        $usersStmt = $pdo->query("SELECT id FROM users");
        $userIds = $usersStmt->fetchAll(PDO::FETCH_COLUMN);

        if (!empty($userIds)) {
            $pdo->beginTransaction();
            try {
                $ins = $pdo->prepare("INSERT INTO notifications (user_id, from_user_id, title, message, type, is_read, created_at) VALUES (?, NULL, ?, ?, ?, 0, NOW())");
                foreach ($userIds as $uid) {
                    $ins->execute([(int)$uid, $title, $message, $type]);
                }
                $pdo->commit();
                $successMsg = 'সাফল্যের সাথে মোট ' . count($userIds) . ' জন ইউজারের কাছে নোটিফিকেশন পাঠানো হয়েছে!';
            } catch (Exception $e) {
                $pdo->rollBack();
                $errorMsg = 'নোটিফিকেশন পাঠাতে সমস্যা হয়েছে: ' . $e->getMessage();
            }
        } else {
            $errorMsg = 'সিস্টেমে বর্তমানে কোনো নিবন্ধিত ইউজার নেই।';
        }
    }
}

// Fetch Recent Notifications Sent
$recentNotifs = [];
try {
    $notifStmt = $pdo->query("
        SELECT title, message, type, created_at, COUNT(*) as sent_count
        FROM notifications
        GROUP BY title, message, type, DATE(created_at), HOUR(created_at), MINUTE(created_at)
        ORDER BY MAX(id) DESC
        LIMIT 15
    ");
    $recentNotifs = $notifStmt->fetchAll();
} catch (Exception $e) {}

require_once __DIR__ . '/includes/header.php';
?>

<div class="row g-4">
    <!-- Broadcast Form Card -->
    <div class="col-12 col-xl-5">
        <div class="card-custom">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div>
                    <h6 class="fw-bold text-dark mb-0"><i class="bi bi-broadcast text-primary me-2"></i>সকল ইউজারের জন্য নোটিফিকেশন</h6>
                    <small class="text-muted">এই নোটিফিকেশন প্রতিটি ইউজারের অ্যাপে দৃশ্যমান হবে</small>
                </div>
                <span class="badge bg-primary-subtle text-primary fw-bold px-3 py-2">
                    <i class="bi bi-people-fill me-1"></i> <?= number_format($totalUsers) ?> গ্রাহক
                </span>
            </div>

            <?php if (!empty($successMsg)): ?>
                <div class="alert alert-success d-flex align-items-center gap-2 border-0 rounded-3 py-2 px-3 mb-3 small" role="alert">
                    <i class="bi bi-check-circle-fill fs-5 text-success"></i>
                    <div><?= htmlspecialchars($successMsg) ?></div>
                </div>
            <?php endif; ?>

            <?php if (!empty($errorMsg)): ?>
                <div class="alert alert-danger d-flex align-items-center gap-2 border-0 rounded-3 py-2 px-3 mb-3 small" role="alert">
                    <i class="bi bi-exclamation-triangle-fill fs-5 text-danger"></i>
                    <div><?= htmlspecialchars($errorMsg) ?></div>
                </div>
            <?php endif; ?>

            <form method="POST" action="notifications.php" onsubmit="return confirmBroadcast(event)">
                <input type="hidden" name="broadcast_notification" value="1">

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">নোটিফিকেশনের শিরোনাম (Title)</label>
                    <input type="text" name="title" class="form-control rounded-3" placeholder="যেমন: নতুন ফিচার আপডেট বা অফার!" required>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold text-secondary">বিস্তারিত বার্তা (Message Body)</label>
                    <textarea name="message" rows="4" class="form-control rounded-3" placeholder="ব্যবহারকারীদের উদ্দেশ্যে সম্পূর্ণ বার্তাটি এখানে লিখুন..." required></textarea>
                </div>

                <div class="mb-4">
                    <label class="form-label small fw-bold text-secondary">ক্যাটাগরি / টাইপ (Notice Type)</label>
                    <select name="type" class="form-select rounded-3">
                        <option value="announcement">ঘোষণা ও আপডেট (Announcement)</option>
                        <option value="general">সাধারণ তথ্য (General Notice)</option>
                        <option value="promotion">অফার বা প্রমোশন (Promotion)</option>
                        <option value="system_alert">সিস্টেম বার্তা (System Alert)</option>
                    </select>
                </div>

                <div class="p-3 bg-light rounded-3 mb-4 small text-muted border">
                    <i class="bi bi-info-circle-fill text-primary me-1"></i>
                    <strong>গুরুত্বপূর্ণ:</strong> বাটনটিতে ক্লিক করার সাথে সাথে ডাটাবেজের সকল <strong><?= $totalUsers ?></strong> জন ইউজারের একাউন্টে এই নোটিফিকেশন পৌঁছে যাবে।
                </div>

                <button type="submit" class="btn btn-primary-custom w-100 py-2 d-flex align-items-center justify-content-center gap-2">
                    <i class="bi bi-send-fill"></i> সকল ইউজারের নিকট নোটিফিকেশন পাঠান
                </button>
            </form>
        </div>
    </div>

    <!-- History / Previously Sent Broadcasts -->
    <div class="col-12 col-xl-7">
        <div class="card-custom h-100">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <div>
                    <h6 class="fw-bold text-dark mb-0">পূর্ববর্তী প্রেরিত নোটিফিকেশনসমূহ (Broadcast History)</h6>
                    <small class="text-muted">পূর্বে পাঠানো সকল সার্বজনীন নোটিফিকেশনের ইতিহাস</small>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-custom align-middle mb-0">
                    <thead>
                        <tr>
                            <th>শিরোনাম ও বার্তা</th>
                            <th>ক্যাটাগরি</th>
                            <th>প্রাপক সংখ্যা</th>
                            <th>তারিখ</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if (empty($recentNotifs)): ?>
                            <tr>
                                <td colspan="4" class="text-center py-5 text-muted">
                                    <i class="bi bi-bell-slash display-5 d-block mb-2 text-muted"></i>
                                    এখনো পর্যন্ত কোনো সার্বজনীন নোটিফিকেশন পাঠানো হয়নি।
                                </td>
                            </tr>
                        <?php else: ?>
                            <?php foreach ($recentNotifs as $rn): ?>
                                <tr>
                                    <td>
                                        <div class="fw-bold text-dark mb-1"><?= htmlspecialchars($rn['title']) ?></div>
                                        <div class="small text-muted text-truncate" style="max-width: 260px;">
                                            <?= htmlspecialchars($rn['message']) ?>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-primary-subtle text-primary fw-bold text-uppercase" style="font-size: 0.725rem;">
                                            <?= htmlspecialchars($rn['type']) ?>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge bg-success-subtle text-success fw-bold">
                                            <i class="bi bi-check-all me-1"></i> <?= $rn['sent_count'] ?> জন
                                        </span>
                                    </td>
                                    <td class="small text-muted">
                                        <?= date('d M, h:i A', strtotime($rn['created_at'])) ?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    function confirmBroadcast(e) {
        if (!confirm('আপনি কি নিশ্চিত যে সকল <?= $totalUsers ?> জন ইউজারের কাছে এই নোটিফিকেশনটি পাঠাতে চান?')) {
            e.preventDefault();
            return false;
        }
        return true;
    }
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
