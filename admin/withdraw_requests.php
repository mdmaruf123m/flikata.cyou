<?php
/**
 * Withdraw Requests Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'উইথড্র রিকোয়েস্ট - Flikata Admin';
$pageHeading = 'উইথড্র রিকোয়েস্ট (Withdraw Requests)';
$pageSubheading = 'ব্যবহারকারীদের টাকা তোলার আবেদন অনুমোদন অথবা কারণ লিখে বাতিল করুন';

$msg = '';
$err = '';

// Handle Approve / Reject
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    $reqId = intval($_POST['request_id'] ?? 0);
    $action = $_POST['action'];

    if ($action === 'approve') {
        try {
            $stmt = $pdo->prepare("SELECT * FROM withdraw_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                if ($req['status'] === 'approved') {
                    $err = 'এই রিকোয়েস্টটি ইতিমধ্যে এপ্রুভ করা হয়েছে!';
                } else {
                    $userId = (int)$req['user_id'];
                    $amount = floatval($req['amount']);
                    $methodName = $req['method_name'];
                    $accountNumber = $req['account_number'];

                    $pdo->beginTransaction();

                    // 1. Update withdraw request
                    $up = $pdo->prepare("UPDATE withdraw_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
                    $up->execute([$reqId]);

                    // 2. Update wallet_transactions to completed
                    $tx = $pdo->prepare("UPDATE wallet_transactions SET status = 'completed' WHERE user_id = ? AND type = 'withdraw' AND status = 'pending' ORDER BY id DESC LIMIT 1");
                    $tx->execute([$userId]);

                    // 3. Send notification
                    try {
                        $notifMsg = "আপনার ৳ " . number_format($amount, 2) . " উইথড্র রিকোয়েস্ট ({$methodName} - {$accountNumber}) সফলভাবে অনুমোদন করা হয়েছে এবং টাকা পাঠিয়ে দেওয়া হয়েছে।";
                        $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'withdraw_approved', ?, NOW())");
                        $notif->execute([$userId, 1, $notifMsg]);
                    } catch (Exception $e) {}

                    $pdo->commit();
                    $msg = "৳ " . number_format($amount, 2) . " উইথড্র সফলভাবে অনুমোদন করা হয়েছে!";
                }
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'অনুমোদন ব্যর্থ: ' . $e->getMessage();
        }
    } elseif ($action === 'reject') {
        $reason = trim($_POST['rejection_reason'] ?? '');
        try {
            $stmt = $pdo->prepare("SELECT * FROM withdraw_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = (int)$req['user_id'];
                $amount = floatval($req['amount']);

                $pdo->beginTransaction();

                // 1. Update request status to rejected with reason
                $up = $pdo->prepare("UPDATE withdraw_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
                $up->execute([$reason, $reqId]);

                // 2. Refund money back to user's wallet
                $refStmt = $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?");
                $refStmt->execute([$amount, $userId]);

                // 3. Insert refund transaction
                $tStmt = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'refund', ?, ?, 'completed')");
                $refDesc = "উইথড্র বাতিল রিফান্ড: ৳ " . number_format($amount, 2) . ($reason ? " (কারণ: {$reason})" : "");
                $tStmt->execute([$userId, $amount, $refDesc]);

                // 4. Notification
                try {
                    $notifMsg = "আপনার ৳ " . number_format($amount, 2) . " উইথড্র আবেদন বাতিল করা হয়েছে এবং টাকা আপনার ওয়ালেটে ফেরত দেওয়া হয়েছে।" . ($reason ? " কারণ: {$reason}" : "");
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'withdraw_rejected', ?, NOW())");
                    $notif->execute([$userId, 1, $notifMsg]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'উইথড্র রিকোয়েস্ট সফলভাবে বাতিল (Rejected) করা হয়েছে এবং ব্যালেন্স রিফান্ড করা হয়েছে!';
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'বাতিল ব্যর্থ: ' . $e->getMessage();
        }
    }
}

// Filter Tab
$status = $_GET['status'] ?? 'pending';
$allowedStatuses = ['pending', 'approved', 'rejected', 'all'];
if (!in_array($status, $allowedStatuses)) $status = 'pending';

$sql = "SELECT wr.*, u.full_name, u.email, u.phone, u.profile_pic AS avatar, u.wallet_balance 
        FROM withdraw_requests wr 
        LEFT JOIN users u ON wr.user_id = u.id 
        WHERE 1=1";
$params = [];

if ($status !== 'all') {
    $sql .= " AND wr.status = ?";
    $params[] = $status;
}
$sql .= " ORDER BY wr.id DESC";

$requests = [];
$counts = ['pending' => 0, 'approved' => 0, 'rejected' => 0];

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $requests = $stmt->fetchAll() ?: [];

    $counts['pending'] = (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'pending'")->fetchColumn();
    $counts['approved'] = (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'approved'")->fetchColumn();
    $counts['rejected'] = (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'rejected'")->fetchColumn();
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
        <a class="nav-link rounded-pill px-4 <?= $status === 'pending' ? 'active bg-warning text-dark fw-bold' : 'bg-white text-muted border' ?>" href="withdraw_requests.php?status=pending">
            <i class="bi bi-clock-history me-1"></i> পেন্ডিং (Pending) <span class="badge bg-dark text-white rounded-pill ms-1"><?= $counts['pending'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'approved' ? 'active bg-success text-white fw-bold' : 'bg-white text-muted border' ?>" href="withdraw_requests.php?status=approved">
            <i class="bi bi-check2-circle me-1"></i> এপ্রুভ (Approved) <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['approved'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'rejected' ? 'active bg-danger text-white fw-bold' : 'bg-white text-muted border' ?>" href="withdraw_requests.php?status=rejected">
            <i class="bi bi-x-circle me-1"></i> রিজেক্ট (Rejected) <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['rejected'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'all' ? 'active bg-primary text-white fw-bold' : 'bg-white text-muted border' ?>" href="withdraw_requests.php?status=all">
            <i class="bi bi-list-ul me-1"></i> সকল রিকোয়েস্ট (All)
        </a>
    </li>
</ul>

<div class="card-custom">
    <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
        <div>
            <h5 class="fw-bold mb-1"><i class="bi bi-arrow-up-right-circle-fill text-warning me-2"></i>উইথড্র রিকোয়েস্ট তালিকা</h5>
            <small class="text-muted">ইউজারদের টাকা তোলার আবেদন যাচাই করে অনুমোদন দিন বা কারণ উল্লেখ করে রিজেক্ট করুন</small>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-custom align-middle">
            <thead>
                <tr>
                    <th>ইউজার তথ্য</th>
                    <th>উইথড্র অ্যামাউন্ট</th>
                    <th>মেথড ও একাউন্ট তথ্য</th>
                    <th>বর্তমান ব্যালেন্স</th>
                    <th>আবেদনের সময়</th>
                    <th>স্ট্যাটাস</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($requests)): ?>
                <tr>
                    <td colspan="7" class="text-center py-5 text-muted">
                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                        এই ক্যাটাগরিতে কোন উইথড্র আবেদন পাওয়া যায়নি।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($requests as $r): ?>
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="<?= !empty($r['avatar']) ? htmlspecialchars($r['avatar']) : 'https://picsum.photos/seed/u'.$r['user_id'].'/100/100' ?>" class="user-avatar-sm" alt="Avatar">
                            <div>
                                <div class="fw-bold text-dark">
                                    <?= htmlspecialchars($r['full_name'] ?: 'ইউজার #'.$r['user_id']) ?>
                                </div>
                                <small class="text-muted d-block"><?= htmlspecialchars($r['email'] ?: $r['phone']) ?></small>
                            </div>
                        </div>
                    </td>
                    <td>
                        <span class="fs-5 fw-bold text-danger">৳ <?= number_format($r['amount'], 2) ?></span>
                    </td>
                    <td>
                        <div class="fw-bold text-dark"><?= htmlspecialchars($r['method_name']) ?></div>
                        <div class="small text-muted">নাম: <strong class="text-dark"><?= htmlspecialchars($r['account_name']) ?></strong></div>
                        <span class="badge bg-light text-dark font-monospace border mt-1 fs-6">
                            <?= htmlspecialchars($r['account_number']) ?>
                        </span>
                    </td>
                    <td>
                        <span class="badge bg-secondary-subtle text-secondary fw-semibold">৳ <?= number_format($r['wallet_balance'], 2) ?></span>
                    </td>
                    <td>
                        <div class="small text-muted"><?= date('d M Y, h:i A', strtotime($r['created_at'])) ?></div>
                    </td>
                    <td>
                        <?php if ($r['status'] === 'pending'): ?>
                            <span class="badge bg-warning-subtle text-dark rounded-pill px-3 py-1 fw-bold"><i class="bi bi-clock me-1"></i>পেন্ডিং</span>
                        <?php elseif ($r['status'] === 'approved'): ?>
                            <span class="badge bg-success-subtle text-success rounded-pill px-3 py-1 fw-bold"><i class="bi bi-check-circle me-1"></i>অনুমোদিত</span>
                        <?php else: ?>
                            <span class="badge bg-danger-subtle text-danger rounded-pill px-3 py-1 fw-bold"><i class="bi bi-x-circle me-1"></i>বাতিল</span>
                            <?php if (!empty($r['rejection_reason'])): ?>
                                <div class="small text-danger mt-1" style="max-width: 200px;">
                                    <strong>কারণ:</strong> <?= htmlspecialchars($r['rejection_reason']) ?>
                                </div>
                            <?php endif; ?>
                        <?php endif; ?>
                    </td>
                    <td class="text-end">
                        <?php if ($r['status'] === 'pending'): ?>
                            <form method="POST" action="withdraw_requests.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই উইথড্র রিকোয়েস্টটি এপ্রুভ করতে চান? ইউজারকে টাকা পাঠানো হয়েছে কিনা যাচাই করুন।');">
                                <input type="hidden" name="action" value="approve">
                                <input type="hidden" name="request_id" value="<?= $r['id'] ?>">
                                <button type="submit" class="btn btn-sm btn-success rounded-pill px-3 me-1">
                                    <i class="bi bi-check-lg"></i> এপ্রুভ
                                </button>
                            </form>
                            <button type="button" class="btn btn-sm btn-outline-danger rounded-pill px-3" onclick='openRejectModal(<?= $r['id'] ?>, "<?= htmlspecialchars($r['full_name']) ?>", "<?= number_format($r['amount'], 2) ?>")'>
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
                <h5 class="modal-title fw-bold text-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>উইথড্র রিকোয়েস্ট বাতিল করুন</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="withdraw_requests.php">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" value="reject">
                    <input type="hidden" name="request_id" id="rejectRequestId" value="">

                    <p class="text-muted mb-3" id="rejectInfoText">ইউজারের উইথড্র রিকোয়েস্ট বাতিল করা হবে এবং টাকা তার ওয়ালেটে ফেরত যাবে।</p>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">বাতিল করার সুনির্দিষ্ট কারণ লিখুন <span class="text-danger">*</span></label>
                        <textarea class="form-control rounded-3" name="rejection_reason" id="rejectReason" rows="3" placeholder="যেমন: আপনার একাউন্ট নাম্বারটি ভুল ছিল / নাম এবং নাম্বার মিলেনি / সাময়িক পেমেন্ট সমস্যা..." required></textarea>
                        <small class="text-muted">এই কারণটি ইউজার তার অ্যাপ্লিকেশন এবং ওয়ালেট স্টেটমেন্টে দেখতে পাবে।</small>
                    </div>
                </div>
                <div class="modal-footer border-top">
                    <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">ফিরে যান</button>
                    <button type="submit" class="btn btn-danger rounded-pill px-4">বাতিল নিশ্চিত করুন</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function openRejectModal(id, name, amount) {
    document.getElementById('rejectRequestId').value = id;
    document.getElementById('rejectReason').value = '';
    document.getElementById('rejectInfoText').innerHTML = '<strong>' + name + '</strong> এর <strong>৳ ' + amount + '</strong> উইথড্র আবেদন বাতিল করতে চাচ্ছেন। টাকা ইউজারের ওয়ালেটে ফেরত যাবে।';
    new bootstrap.Modal(document.getElementById('rejectModal')).show();
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
