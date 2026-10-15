<?php
/**
 * Deposit Requests Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ডিপোজিট রিকোয়েস্ট - Flikata Admin';
$pageHeading = 'ডিপোজিট রিকোয়েস্ট (Deposit Requests)';
$pageSubheading = 'ব্যবহারকারীদের ওয়ালেট রিচার্জ/ডিপোজিট আবেদন পর্যালোচনা, অনুমোদন বা প্রত্যাখ্যান করুন';

$msg = '';
$err = '';

// Handle Approve / Reject
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    $reqId = intval($_POST['request_id'] ?? 0);
    $action = $_POST['action'];

    if ($action === 'approve') {
        try {
            $stmt = $pdo->prepare("SELECT * FROM deposit_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                if ($req['status'] === 'approved') {
                    $err = 'এই রিকোয়েস্টটি ইতিমধ্যে এপ্রুভ করা হয়েছে!';
                } else {
                    $userId = $req['user_id'];
                    $amount = floatval($req['amount']);
                    $methodName = $req['method_name'];
                    $trxId = $req['transaction_id'];

                    $pdo->beginTransaction();

                    // 1. Update deposit request
                    $up = $pdo->prepare("UPDATE deposit_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
                    $up->execute([$reqId]);

                    // 2. Add amount to user's wallet_balance
                    $upBal = $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?");
                    $upBal->execute([$amount, $userId]);

                    // 3. Insert transaction log
                    $tx = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'deposit', ?, ?, 'completed')");
                    $tx->execute([$userId, $amount, "{$methodName} ডিপোজিট সফল (TrxID: {$trxId})"]);

                    // 4. Send notification
                    try {
                        $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'deposit_approved', ?, NOW())");
                        $notif->execute([$userId, 1, "আপনার ৳ " . number_format($amount, 2) . " ডিপোজিট অনুমোদন করা হয়েছে এবং ওয়ালেটে জমা হয়েছে।"]);
                    } catch (Exception $e) {}

                    $pdo->commit();
                    $msg = "৳ " . number_format($amount, 2) . " ডিপোজিট সফলভাবে অনুমোদন করা হয়েছে এবং ইউজারের ওয়ালেটে জমা হয়েছে!";
                }
            }
        } catch (Exception $e) {
            if ($pdo->inTransaction()) $pdo->rollBack();
            $err = 'অনুমোদন ব্যর্থ: ' . $e->getMessage();
        }
    } elseif ($action === 'reject') {
        $reason = trim($_POST['rejection_reason'] ?? '');
        try {
            $stmt = $pdo->prepare("SELECT * FROM deposit_requests WHERE id = ?");
            $stmt->execute([$reqId]);
            $req = $stmt->fetch();

            if ($req) {
                $userId = $req['user_id'];
                $amount = floatval($req['amount']);

                $pdo->beginTransaction();

                // Update request
                $up = $pdo->prepare("UPDATE deposit_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
                $up->execute([$reason, $reqId]);

                // Notification
                try {
                    $notifMsg = "আপনার ৳ " . number_format($amount, 2) . " ডিপোজিট আবেদন বাতিল করা হয়েছে।" . ($reason ? " কারণ: {$reason}" : "");
                    $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'deposit_rejected', ?, NOW())");
                    $notif->execute([$userId, 1, $notifMsg]);
                } catch (Exception $e) {}

                $pdo->commit();
                $msg = 'ডিপোজিট রিকোয়েস্ট সফলভাবে বাতিল (Rejected) করা হয়েছে!';
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

$sql = "SELECT dr.*, u.full_name, u.email, u.phone, u.profile_pic AS avatar, u.wallet_balance 
        FROM deposit_requests dr 
        LEFT JOIN users u ON dr.user_id = u.id 
        WHERE 1=1";
$params = [];

if ($status !== 'all') {
    $sql .= " AND dr.status = ?";
    $params[] = $status;
}
$sql .= " ORDER BY dr.id DESC";

$requests = [];
$counts = ['pending' => 0, 'approved' => 0, 'rejected' => 0];

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $requests = $stmt->fetchAll() ?: [];

    $counts['pending'] = (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'pending'")->fetchColumn();
    $counts['approved'] = (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'approved'")->fetchColumn();
    $counts['rejected'] = (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'rejected'")->fetchColumn();
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
        <a class="nav-link rounded-pill px-4 <?= $status === 'pending' ? 'active bg-warning text-dark fw-bold' : 'bg-white text-muted border' ?>" href="deposit_requests.php?status=pending">
            <i class="bi bi-clock-history me-1"></i> পেন্ডিং রিকোয়েস্ট <span class="badge bg-dark text-white rounded-pill ms-1"><?= $counts['pending'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'approved' ? 'active bg-success text-white fw-bold' : 'bg-white text-muted border' ?>" href="deposit_requests.php?status=approved">
            <i class="bi bi-check2-circle me-1"></i> এপ্রুভড ডিপোজিট <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['approved'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'rejected' ? 'active bg-danger text-white fw-bold' : 'bg-white text-muted border' ?>" href="deposit_requests.php?status=rejected">
            <i class="bi bi-x-circle me-1"></i> রিজেক্টেড <span class="badge bg-light text-dark rounded-pill ms-1"><?= $counts['rejected'] ?></span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link rounded-pill px-4 <?= $status === 'all' ? 'active bg-primary text-white fw-bold' : 'bg-white text-muted border' ?>" href="deposit_requests.php?status=all">
            সব ডিপোজিট রিকোয়েস্ট
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
                    <th>মেথড</th>
                    <th>টাকার পরিমাণ</th>
                    <th>ট্রানজেকশন আইডি (TrxID)</th>
                    <th>স্ট্যাটাস</th>
                    <th>তারিখ</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($requests)): ?>
                <tr>
                    <td colspan="7" class="text-center py-5 text-muted">
                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                        এই ক্যাটাগরিতে কোন ডিপোজিট রিকোয়েস্ট পাওয়া যায়নি।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($requests as $r): ?>
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="<?= !empty($r['avatar']) ? htmlspecialchars($r['avatar']) : 'https://picsum.photos/seed/u'.$r['user_id'].'/100/100' ?>" class="user-avatar-sm" alt="Avatar">
                            <div>
                                <div class="fw-bold text-dark"><?= htmlspecialchars($r['full_name'] ?: 'ইউজার #'.$r['user_id']) ?></div>
                                <small class="text-muted d-block">বর্তমান ব্যালেন্স: ৳ <?= number_format($r['wallet_balance'] ?? 0, 2) ?></small>
                            </div>
                        </div>
                    </td>
                    <td>
                        <span class="badge bg-light text-primary border px-3 py-1 fs-6 fw-bold">
                            <?= htmlspecialchars($r['method_name']) ?>
                        </span>
                    </td>
                    <td>
                        <span class="badge bg-success-subtle text-success fs-6 fw-bold px-3 py-1">
                            ৳ <?= number_format($r['amount'], 2) ?>
                        </span>
                    </td>
                    <td>
                        <span class="badge bg-dark-subtle text-dark font-monospace fs-6 px-3 py-1 border">
                            <?= htmlspecialchars($r['transaction_id']) ?>
                        </span>
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
                            <form method="POST" action="deposit_requests.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই ডিপোজিটটি এপ্রুভ করতে চান? ইউজারের ওয়ালেটে ৳ <?= number_format($r['amount'], 2) ?> যোগ হবে।');">
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

<!-- Reject Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-bottom">
                <h5 class="modal-title fw-bold text-danger"><i class="bi bi-x-circle me-1"></i>ডিপোজিট রিজেক্ট করুন</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="deposit_requests.php">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" value="reject">
                    <input type="hidden" name="request_id" id="rejectRequestId" value="">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">রিজেক্ট করার কারণ (অপশনাল):</label>
                        <textarea class="form-control rounded-3" name="rejection_reason" rows="3" placeholder="যেমন: ভুয়া TrxID বা অ্যাকাউন্টে টাকা পাওয়া যায়নি..."></textarea>
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
