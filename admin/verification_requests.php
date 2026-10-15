<?php
/**
 * Verification Requests Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ভেরিফিকেশন রিকোয়েস্ট - Flikata Admin';
$pageHeading = 'ভেরিফিকেশন রিকোয়েস্ট (Verification Requests)';
$pageSubheading = 'ব্যবহারকারীদের আবেদন পর্যালোচনা, অনুমোদন বা প্রত্যাখ্যান করুন';

$msg = '';
$err = '';

// Handle Approve / Reject
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    $reqId = intval($_POST['request_id'] ?? 0);
    $action = $_POST['action'];

    if ($action === 'approve') {
        try {
            $stmt = $pdo->prepare("SELECT vr.*, vp.duration_type, vp.duration_value FROM verification_requests vr LEFT JOIN verification_packages vp ON vr.package_id = vp.id WHERE vr.id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = $req['user_id'];
                $durationType = $req['duration_type'] ?? 'month';
                $durationVal = intval($req['duration_value'] ?? 1);

                // Calculate expiration date
                if ($durationType === 'lifetime') {
                    $verifiedUntil = '2099-12-31 23:59:59';
                } else {
                    $intervalStr = "+{$durationVal} " . ($durationType === 'day' ? 'days' : ($durationType === 'year' ? 'years' : 'months'));
                    $verifiedUntil = date('Y-m-d H:i:s', strtotime($intervalStr));
                }

                $pdo->beginTransaction();
                // Update request
                $upReq = $pdo->prepare("UPDATE verification_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
                $upReq->execute([$reqId]);

                // Update user verification
                $upUser = $pdo->prepare("UPDATE users SET is_verified = 1, verified_until = ? WHERE id = ?");
                $upUser->execute([$verifiedUntil, $userId]);

                // Insert notification for user
                try {
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'verification', 'অভিনন্দন! আপনার ভেরিফিকেশন ব্যাচ অনুমোদন করা হয়েছে।', NOW())");
                    $notif->execute([$userId, 1]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'ভেরিফিকেশন আবেদন সফলভাবে অনুমোদন (Approved) করা হয়েছে!';
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'অনুমোদন ব্যর্থ হয়েছে: ' . $e->getMessage();
        }
    } elseif ($action === 'reject') {
        $reason = trim($_POST['rejection_reason'] ?? '');
        try {
            $stmt = $pdo->prepare("SELECT * FROM verification_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = $req['user_id'];
                $amount = floatval($req['amount']);

                $pdo->beginTransaction();
                // Update request
                $upReq = $pdo->prepare("UPDATE verification_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
                $upReq->execute([$reason, $reqId]);

                // Refund to user wallet if amount was deducted
                if ($amount > 0) {
                    $ref = $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?");
                    $ref->execute([$amount, $userId]);

                    // Add wallet transaction record
                    $tx = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'refund', ?, 'ভেরিফিকেশন প্রত্যাখ্যাত হওয়ায় টাকা ফেরত দেওয়া হয়েছে', 'completed')");
                    $tx->execute([$userId, $amount]);
                }

                // Insert notification for user
                try {
                    $msgText = 'আপনার ভেরিফিকেশন আবেদন বাতিল করা হয়েছে।' . ($reason ? " কারণ: {$reason}" : '');
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'verification_rejected', ?, NOW())");
                    $notif->execute([$userId, 1, $msgText]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'ভেরিফিকেশন আবেদন সফলভাবে বাতিল (Rejected) ও ওয়ালেটে টাকা ফেরত দেওয়া হয়েছে!';
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'বাতিল করতে সমস্যা হয়েছে: ' . $e->getMessage();
        }
    }
}

// Filter Tab
$status = $_GET['status'] ?? 'pending';
$allowedStatuses = ['pending', 'approved', 'rejected', 'all'];
if (!in_array($status, $allowedStatuses)) $status = 'pending';

$sql = "SELECT vr.*, u.full_name, u.email, u.phone, u.profile_pic AS avatar, u.is_verified AS user_verified_now 
        FROM verification_requests vr 
        LEFT JOIN users u ON vr.user_id = u.id 
        WHERE 1=1";
$params = [];

if ($status !== 'all') {
    $sql .= " AND vr.status = ?";
    $params[] = $status;
}
$sql .= " ORDER BY vr.id DESC";

$requests = [];
$counts = ['pending' => 0, 'approved' => 0, 'rejected' => 0];

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $requests = $stmt->fetchAll() ?: [];

    $counts['pending'] = (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'pending'")->fetchColumn();
    $counts['approved'] = (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'approved'")->fetchColumn();
    $counts['rejected'] = (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'rejected'")->fetchColumn();
} catch (Exception $e) {
    $err = 'ডাটাবেজ সমস্যা: ' . $e->getMessage();
}

require_once __DIR__ . '/includes/header.php';
?>

<?php if ($msg): ?>
<div class="alert alert-success alert-dismissible fade show rounded-4" role="alert">
    <i class="bi bi-check-circle-fill me-2"></i><?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<?php endif; ?>

<?php if ($err): ?>
<div class="alert alert-danger alert-dismissible fade show rounded-4" role="alert">
    <i class="bi bi-exclamation-triangle-fill me-2"></i><?= htmlspecialchars($err) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<?php endif; ?>

<!-- Tabs navigation -->
<ul class="nav nav-pills mb-4 gap-2">
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'pending' ? 'active bg-warning text-dark fw-bold' : 'bg-white text-muted border' ?>" href="verification_requests.php?status=pending">
            <i class="bi bi-clock-history me-1"></i> পেন্ডিং (Pending) <span class="badge bg-dark text-white rounded-pill ms-1"><?= $counts['pending'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'approved' ? 'active bg-success text-white fw-bold' : 'bg-white text-muted border' ?>" href="verification_requests.php?status=approved">
            <i class="bi bi-check2-circle me-1"></i> এপ্রুভ (Approved) <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['approved'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'rejected' ? 'active bg-danger text-white fw-bold' : 'bg-white text-muted border' ?>" href="verification_requests.php?status=rejected">
            <i class="bi bi-x-circle me-1"></i> রিজেক্ট (Rejected) <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['rejected'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'all' ? 'active bg-primary text-white fw-bold' : 'bg-white text-muted border' ?>" href="verification_requests.php?status=all">
            সব আবেদন (All)
        </a>
    </li>
</ul>

<!-- Requests List -->
<div class="card-custom">
    <div class="table-responsive">
        <table class="table table-custom">
            <thead>
                <tr>
                    <th>ইউজার</th>
                    <th>প্যাকেজ ও ফি</th>
                    <th>আবেদনের ডকুমেন্ট</th>
                    <th>স্ট্যাটাস</th>
                    <th>তারিখ</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($requests)): ?>
                <tr>
                    <td colspan="6" class="text-center py-5 text-muted">
                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                        এই ক্যাটাগরিতে কোন আবেদন পাওয়া যায়নি।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($requests as $r): ?>
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="<?= !empty($r['avatar']) ? htmlspecialchars($r['avatar']) : 'https://picsum.photos/seed/u'.$r['user_id'].'/100/100' ?>" class="user-avatar-sm" alt="Avatar">
                            <div>
                                <div class="fw-bold text-dark d-flex align-items-center gap-1">
                                    <?= htmlspecialchars($r['full_name'] ?: 'ইউজার #'.$r['user_id']) ?>
                                    <?php if ($r['user_verified_now']): ?>
                                        <i class="bi bi-patch-check-fill text-primary" title="বর্তমানে ভেরিফাইড"></i>
                                    <?php endif; ?>
                                </div>
                                <small class="text-muted d-block"><?= htmlspecialchars($r['email'] ?: $r['phone']) ?></small>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div class="fw-bold text-dark"><?= htmlspecialchars($r['package_title'] ?: 'প্যাকেজ #'.$r['package_id']) ?></div>
                        <span class="badge bg-success-subtle text-success fw-bold px-2 py-1">৳ <?= number_format($r['amount'], 2) ?></span>
                    </td>
                    <td>
                        <div class="d-flex gap-2 align-items-center">
                            <?php if (!empty($r['id_front_url'])): ?>
                                <a href="<?= htmlspecialchars($r['id_front_url']) ?>" target="_blank" class="badge bg-primary-subtle text-primary text-decoration-none px-2 py-1" title="আইডি কার্ড ফ্রন্ট">
                                    <i class="bi bi-image"></i> ফ্রন্ট কার্ড
                                </a>
                            <?php endif; ?>
                            <?php if (!empty($r['id_back_url'])): ?>
                                <a href="<?= htmlspecialchars($r['id_back_url']) ?>" target="_blank" class="badge bg-primary-subtle text-primary text-decoration-none px-2 py-1" title="আইডি কার্ড ব্যাক">
                                    <i class="bi bi-image"></i> ব্যাক কার্ড
                                </a>
                            <?php endif; ?>
                            <?php if (!empty($r['selfie_url'])): ?>
                                <a href="<?= htmlspecialchars($r['selfie_url']) ?>" target="_blank" class="badge bg-info-subtle text-info text-decoration-none px-2 py-1" title="সেলফি">
                                    <i class="bi bi-person-bounding-box"></i> সেলফি
                                </a>
                            <?php endif; ?>
                            <?php if (empty($r['id_front_url']) && empty($r['id_back_url']) && empty($r['selfie_url'])): ?>
                                <small class="text-muted">কোন ডকুমেন্ট প্রয়োজন ছিল না</small>
                            <?php endif; ?>
                        </div>
                    </td>
                    <td>
                        <?php if ($r['status'] === 'pending'): ?>
                            <span class="badge bg-warning-subtle text-warning fw-bold px-3 py-1 rounded-pill"><i class="bi bi-clock-fill me-1"></i>পেন্ডিং</span>
                        <?php elseif ($r['status'] === 'approved'): ?>
                            <span class="badge bg-success-subtle text-success fw-bold px-3 py-1 rounded-pill"><i class="bi bi-check-circle-fill me-1"></i>এপ্রুভড</span>
                        <?php else: ?>
                            <span class="badge bg-danger-subtle text-danger fw-bold px-3 py-1 rounded-pill"><i class="bi bi-x-circle-fill me-1"></i>রিজেক্টেড</span>
                            <?php if (!empty($r['rejection_reason'])): ?>
                                <small class="d-block text-danger mt-1">কারণ: <?= htmlspecialchars($r['rejection_reason']) ?></small>
                            <?php endif; ?>
                        <?php endif; ?>
                    </td>
                    <td>
                        <small class="text-muted"><?= date('d M Y, h:i A', strtotime($r['created_at'])) ?></small>
                    </td>
                    <td class="text-end">
                        <?php if ($r['status'] === 'pending'): ?>
                            <form method="POST" action="verification_requests.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই আবেদনটি এপ্রুভ করতে চান?');">
                                <input type="hidden" name="action" value="approve">
                                <input type="hidden" name="request_id" value="<?= $r['id'] ?>">
                                <button type="submit" class="btn btn-sm btn-success rounded-pill px-3">
                                    <i class="bi bi-check-lg"></i> এপ্রুভ
                                </button>
                            </form>
                            <button type="button" class="btn btn-sm btn-danger rounded-pill px-3 ms-1" onclick="openRejectModal(<?= $r['id'] ?>)">
                                <i class="bi bi-x-lg"></i> রিজেক্ট
                            </button>
                        <?php else: ?>
                            <span class="text-muted small">সম্পন্ন</span>
                        <?php endif; ?>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<!-- Reject Reason Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-bottom">
                <h5 class="modal-title fw-bold text-danger"><i class="bi bi-x-circle me-1"></i>আবেদন রিজেক্ট করুন</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="verification_requests.php">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" value="reject">
                    <input type="hidden" name="request_id" id="rejectRequestId" value="">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">রিজেক্ট করার কারণ (অপশনাল):</label>
                        <textarea class="form-control rounded-3" name="rejection_reason" rows="3" placeholder="যেমন: আইডি কার্ডের ছবি স্পষ্ট নয়, অনুগ্রহ করে পুনরায় স্পষ্ট ছবি দিয়ে আবেদন করুন..."></textarea>
                        <small class="text-muted">এই কারণটি ব্যবহারকারী দেখতে পাবে এবং তার ওয়ালেটে টাকা রিফান্ড হয়ে যাবে।</small>
                    </div>
                </div>
                <div class="modal-footer border-top">
                    <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-danger rounded-pill px-4">রিজেক্ট নিশ্চিত করুন</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function openRejectModal(id) {
    document.getElementById('rejectRequestId').value = id;
    new bootstrap.Modal(document.getElementById('rejectModal')).show();
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
