<?php
/**
 * User Management Page
 * Search by Name, Email, Phone. Edit, Delete, Ban/Unban, Single User Notification.
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ইউজার ম্যানেজমেন্ট - Flikata Admin';
$pageHeading = 'ইউজার ম্যানেজমেন্ট (User Management)';
$pageSubheading = 'ইউজার অনুসন্ধান, প্রোফাইল সম্পাদনা, ব্যান/আনব্যান ও একক নোটিফিকেশন প্রেরণ';

// Search and filter parameters
$search = trim($_GET['search'] ?? '');
$statusFilter = trim($_GET['status'] ?? 'all');

// Build query
$sql = "SELECT * FROM users WHERE 1=1";
$params = [];

if (!empty($search)) {
    $sql .= " AND (full_name LIKE ? OR email LIKE ? OR phone LIKE ?)";
    $like = "%$search%";
    $params[] = $like;
    $params[] = $like;
    $params[] = $like;
}

if ($statusFilter === 'banned') {
    $sql .= " AND is_banned = 1";
} elseif ($statusFilter === 'active') {
    $sql .= " AND (is_banned = 0 OR is_banned IS NULL)";
} elseif ($statusFilter === 'verified') {
    $sql .= " AND is_verified = 1";
}

$sql .= " ORDER BY id DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$users = $stmt->fetchAll();

require_once __DIR__ . '/includes/header.php';
?>

<!-- Search Bar & Filters Card -->
<div class="card-custom mb-4">
    <form method="GET" action="users.php" class="row g-3 align-items-center">
        <!-- Search Input -->
        <div class="col-12 col-md-6 col-lg-7">
            <div class="input-group">
                <span class="input-group-text bg-light border-end-0 border-1" style="border-radius: 12px 0 0 12px; border-color: var(--border-color);">
                    <i class="bi bi-search text-muted"></i>
                </span>
                <input type="text" name="search" class="form-control bg-light border-start-0 border-1" style="border-radius: 0 12px 12px 0; border-color: var(--border-color);" placeholder="নাম, ইমেইল বা ফোন নাম্বার দিয়ে খুঁজুন..." value="<?= htmlspecialchars($search) ?>">
            </div>
        </div>

        <!-- Filter Dropdown -->
        <div class="col-6 col-md-3 col-lg-3">
            <select name="status" class="form-select bg-light border-1" style="border-radius: 12px; border-color: var(--border-color);">
                <option value="all" <?= $statusFilter === 'all' ? 'selected' : '' ?>>সকল ইউজার</option>
                <option value="active" <?= $statusFilter === 'active' ? 'selected' : '' ?>>সচল (Active)</option>
                <option value="banned" <?= $statusFilter === 'banned' ? 'selected' : '' ?>>ব্যানড (Banned)</option>
                <option value="verified" <?= $statusFilter === 'verified' ? 'selected' : '' ?>>ভেরিফায়েড (Verified)</option>
            </select>
        </div>

        <!-- Search Button & Reset -->
        <div class="col-6 col-md-3 col-lg-2 d-flex gap-2">
            <button type="submit" class="btn btn-primary-custom w-100 d-flex align-items-center justify-content-center gap-2">
                <i class="bi bi-funnel-fill"></i> ফিল্টার
            </button>
            <?php if (!empty($search) || $statusFilter !== 'all'): ?>
                <a href="users.php" class="btn btn-light border rounded-3 p-2" title="রিসেট">
                    <i class="bi bi-x-lg"></i>
                </a>
            <?php endif; ?>
        </div>
    </form>
</div>

<!-- Users List Table Card -->
<div class="card-custom">
    <div class="d-flex align-items-center justify-content-between mb-3">
        <div>
            <h6 class="fw-bold mb-0 text-dark">ইউজার তালিকা (মোট: <?= count($users) ?> জন)</h6>
            <small class="text-muted">প্রত্যেক ইউজারের তথ্যাদি ও অ্যাকশন বাটন</small>
        </div>
    </div>

    <div class="table-responsive">
        <table class="table table-custom align-middle mb-0">
            <thead>
                <tr>
                    <th>ইউজার ও আইডি</th>
                    <th>ইমেইল ও ফোন</th>
                    <th>পেশা ও লোকেশন</th>
                    <th>স্ট্যাটাস</th>
                    <th>যোগদানের তারিখ</th>
                    <th class="text-end" style="min-width: 180px;">অ্যাকশন (Actions)</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($users)): ?>
                    <tr>
                        <td colspan="6" class="text-center py-5 text-muted">
                            <i class="bi bi-people display-4 d-block mb-2 text-muted"></i>
                            কোনো ইউজার খুঁজে পাওয়া যায়নি।
                        </td>
                    </tr>
                <?php else: ?>
                    <?php foreach ($users as $u): ?>
                        <tr id="user-row-<?= $u['id'] ?>">
                            <!-- User & Avatar -->
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <img src="<?= !empty($u['profile_pic']) ? htmlspecialchars($u['profile_pic']) : 'https://ui-avatars.com/api/?name=' . urlencode($u['full_name']) . '&background=4f46e5&color=fff' ?>" class="user-avatar-sm" alt="Profile">
                                    <div>
                                        <div class="fw-bold text-dark d-flex align-items-center gap-1">
                                            <?= htmlspecialchars($u['full_name']) ?>
                                            <span id="verify-icon-<?= $u['id'] ?>">
                                                <?php if (!empty($u['is_verified'])): ?>
                                                    <i class="bi bi-patch-check-fill text-primary" title="ভেরিফায়েড একাউন্ট"></i>
                                                <?php endif; ?>
                                            </span>
                                        </div>
                                        <small class="text-muted">UID: #<?= $u['id'] ?></small>
                                    </div>
                                </div>
                            </td>

                            <!-- Email & Phone -->
                            <td>
                                <div class="small fw-semibold text-dark"><i class="bi bi-envelope me-1 text-muted"></i><?= htmlspecialchars($u['email'] ?: 'N/A') ?></div>
                                <div class="small text-muted"><i class="bi bi-telephone me-1 text-muted"></i><?= htmlspecialchars($u['phone'] ?: 'N/A') ?></div>
                            </td>

                            <!-- Occupation & Location -->
                            <td>
                                <div class="small fw-semibold text-dark"><?= htmlspecialchars($u['occupation'] ?: 'User') ?></div>
                                <small class="text-muted"><?= htmlspecialchars($u['location'] ?: 'Bangladesh') ?></small>
                            </td>

                            <!-- Status Badge -->
                            <td id="status-cell-<?= $u['id'] ?>">
                                <?php if (!empty($u['is_banned'])): ?>
                                    <span class="badge bg-danger-subtle text-danger fw-bold px-2 py-1"><i class="bi bi-slash-circle me-1"></i>ব্যানড</span>
                                <?php else: ?>
                                    <span class="badge bg-success-subtle text-success fw-bold px-2 py-1"><i class="bi bi-check-circle me-1"></i>সচল</span>
                                <?php endif; ?>
                            </td>

                            <!-- Date -->
                            <td class="small text-muted">
                                <?= date('d M, Y', strtotime($u['created_at'])) ?>
                            </td>

                            <!-- Actions -->
                            <td class="text-end">
                                <div class="d-flex align-items-center justify-content-end gap-1">
                                    <!-- Blue Verification Badge Toggle Button -->
                                    <button id="verify-btn-<?= $u['id'] ?>" class="btn btn-sm <?= !empty($u['is_verified']) ? 'btn-primary' : 'btn-light border text-primary' ?>" title="<?= !empty($u['is_verified']) ? 'ব্লু ভেরিফিকেশন বন্ধ করুন (Unverify)' : 'ফেসবুক ব্লু ভেরিফিকেশন চালু করুন (Verify)' ?>" onclick="toggleVerifyUser(<?= $u['id'] ?>, <?= !empty($u['is_verified']) ? 0 : 1 ?>)">
                                        <i class="bi <?= !empty($u['is_verified']) ? 'bi-patch-check-fill' : 'bi-patch-check' ?>"></i>
                                    </button>

                                    <!-- Send Single Notification -->
                                    <button class="btn btn-sm btn-light border text-primary" title="একক নোটিফিকেশন পাঠান" onclick="openNotifyModal(<?= $u['id'] ?>, '<?= htmlspecialchars(addslashes($u['full_name'])) ?>')">
                                        <i class="bi bi-bell-fill"></i>
                                    </button>

                                    <!-- Edit User Modal Trigger -->
                                    <button class="btn btn-sm btn-light border text-dark" title="এডিট করুন" onclick='openEditModal(<?= json_encode($u) ?>)'>
                                        <i class="bi bi-pencil-square"></i>
                                    </button>

                                    <!-- Ban / Unban Toggle Button -->
                                    <button id="ban-btn-<?= $u['id'] ?>" class="btn btn-sm <?= !empty($u['is_banned']) ? 'btn-success' : 'btn-warning text-dark' ?>" title="<?= !empty($u['is_banned']) ? 'আনব্যান করুন' : 'ব্যান করুন' ?>" onclick="toggleBanUser(<?= $u['id'] ?>, <?= !empty($u['is_banned']) ? 0 : 1 ?>)">
                                        <i class="bi <?= !empty($u['is_banned']) ? 'bi-shield-check' : 'bi-slash-circle' ?>"></i>
                                    </button>

                                    <!-- Delete User Button -->
                                    <button class="btn btn-sm btn-danger-custom" title="স্থায়ীভাবে ডিলিট করুন" onclick="deleteUserConfirm(<?= $u['id'] ?>, '<?= htmlspecialchars(addslashes($u['full_name'])) ?>')">
                                        <i class="bi bi-trash3-fill"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<!-- Modal 1: Edit User Modal -->
<div class="modal fade" id="editUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-bottom py-3">
                <h6 class="modal-title fw-bold text-dark"><i class="bi bi-person-gear text-primary me-2"></i>ইউজার তথ্য সম্পাদনা</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editUserForm" onsubmit="submitUserEdit(event)">
                <input type="hidden" name="action" value="edit_user">
                <input type="hidden" id="editUserId" name="user_id">
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">পুরো নাম (Full Name)</label>
                        <input type="text" id="editFullName" name="full_name" class="form-control rounded-3" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ইমেইল (Email)</label>
                        <input type="email" id="editEmail" name="email" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ফোন নাম্বার (Phone)</label>
                        <input type="text" id="editPhone" name="phone" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">পেশা (Occupation)</label>
                        <input type="text" id="editOccupation" name="occupation" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">ঠিকানা (Location)</label>
                        <input type="text" id="editLocation" name="location" class="form-control rounded-3">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">বায়ো (Bio)</label>
                        <textarea id="editBio" name="bio" rows="2" class="form-control rounded-3"></textarea>
                    </div>
                    <div class="form-check form-switch mt-3">
                        <input class="form-check-input" type="checkbox" id="editVerified" name="is_verified" value="1">
                        <label class="form-check-label fw-bold text-dark small" for="editVerified">ভেরিফায়েড ব্যাজ (Blue Tick Badge)</label>
                    </div>
                </div>
                <div class="modal-footer border-top py-3">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary-custom px-4">সংরক্ষণ করুন</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal 2: Send Single User Notification Modal -->
<div class="modal fade" id="notifyUserModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow rounded-4">
            <div class="modal-header border-bottom py-3">
                <h6 class="modal-title fw-bold text-dark">
                    <i class="bi bi-bell-fill text-primary me-2"></i>একক নোটিফিকেশন প্রেরণ
                </h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="notifyUserForm" onsubmit="submitSingleNotification(event)">
                <input type="hidden" name="action" value="send_single_notification">
                <input type="hidden" id="notifyUserId" name="user_id">
                <div class="modal-body p-4">
                    <div class="alert alert-primary-subtle border-0 rounded-3 py-2 px-3 small mb-3">
                        প্রাপক: <strong id="notifyUserName" class="text-dark">User</strong>
                        <div class="text-muted mt-1" style="font-size: 0.785rem;">এই নোটিফিকেশনটি শুধুমাত্র এই ইউজারের নোটিফিকেশন পেজে যাবে।</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">শিরোনাম (Notification Title)</label>
                        <input type="text" name="title" class="form-control rounded-3" placeholder="যেমন: আপনার একাউন্ট সংক্রান্ত নোটিশ" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">বার্তা (Message Body)</label>
                        <textarea name="message" rows="3" class="form-control rounded-3" placeholder="ইউজারকে জানানোর সম্পূর্ণ বার্তা লিখুন..." required></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold text-secondary">নোটিফিকেশনের ধরন (Type)</label>
                        <select name="type" class="form-select rounded-3">
                            <option value="general">সাধারণ বার্তা (General)</option>
                            <option value="alert">সতর্কবার্তা (Alert)</option>
                            <option value="reward">পুরস্কার/অভিনন্দন (Reward)</option>
                            <option value="admin_notice">অফিশিয়াল নোটিশ (Official Notice)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer border-top py-3">
                    <button type="button" class="btn btn-light rounded-pill px-3" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary-custom px-4">
                        <i class="bi bi-send-fill me-1"></i> নোটিফিকেশন পাঠান
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Edit User Modal Initialization
    let editModalInstance = null;
    function openEditModal(userData) {
        document.getElementById('editUserId').value = userData.id;
        document.getElementById('editFullName').value = userData.full_name || '';
        document.getElementById('editEmail').value = userData.email || '';
        document.getElementById('editPhone').value = userData.phone || '';
        document.getElementById('editOccupation').value = userData.occupation || '';
        document.getElementById('editLocation').value = userData.location || '';
        document.getElementById('editBio').value = userData.bio || '';
        document.getElementById('editVerified').checked = (userData.is_verified == 1);

        if (!editModalInstance) {
            editModalInstance = new bootstrap.Modal(document.getElementById('editUserModal'));
        }
        editModalInstance.show();
    }

    function submitUserEdit(e) {
        e.preventDefault();
        const form = document.getElementById('editUserForm');
        const formData = new FormData(form);

        fetch('api.php', {
            method: 'POST',
            body: formData
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                editModalInstance.hide();
                Swal.fire('সম্পন্ন!', 'ইউজারের তথ্য সফলভাবে আপডেট হয়েছে।', 'success')
                    .then(() => location.reload());
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'আপডেট করা সম্ভব হয়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারের সাথে যোগাযোগ করা যায়নি।', 'error'));
    }

    // Toggle Facebook Blue Verification Badge
    function toggleVerifyUser(userId, newVerifyStatus) {
        const actionText = newVerifyStatus === 1 ? 'ব্লু ভেরিফিকেশন চালু (Verify)' : 'ভেরিফিকেশন বন্ধ (Unverify)';
        Swal.fire({
            title: `ইউজারের ${actionText} করতে চান?`,
            text: newVerifyStatus === 1 ? 'চালু করলে ইউজারের নামের পাশে ফেসবুকের মতো কাটা কাটা ব্লু ভেরিফিকেশন ব্যাচ প্রদর্শিত হবে।' : 'বন্ধ করলে ভেরিফিকেশন ব্যাচ মুছে যাবে।',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: newVerifyStatus === 1 ? '#0866ff' : '#64748b',
            cancelButtonColor: '#94a3b8',
            confirmButtonText: `হ্যাঁ, ${actionText} করুন`,
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'toggle_verify', user_id: userId, is_verified: newVerifyStatus })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        Swal.fire('সফল!', res.message, 'success')
                            .then(() => location.reload());
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'অপারেশন ব্যর্থ হয়েছে।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Toggle Ban / Unban User
    function toggleBanUser(userId, newBanStatus) {
        const actionText = newBanStatus === 1 ? 'ব্যান (Ban)' : 'আনব্যান (Unban)';
        Swal.fire({
            title: `ইউজারকে ${actionText} করতে চান?`,
            text: newBanStatus === 1 ? 'ব্যান করলে ইউজার অ্যাপে লগইন করতে পারবে না।' : 'আনব্যান করলে ইউজার স্বাভাবিকভাবে অ্যাপ ব্যবহার করতে পারবে।',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: newBanStatus === 1 ? '#eab308' : '#10b981',
            cancelButtonColor: '#64748b',
            confirmButtonText: `হ্যাঁ, ${actionText} করুন`,
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'toggle_ban', user_id: userId, is_banned: newBanStatus })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        Swal.fire('সফল!', `ইউজার সফলভাবে ${actionText} করা হয়েছে।`, 'success')
                            .then(() => location.reload());
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'অপারেশন ব্যর্থ হয়েছে।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Delete User Confirm
    function deleteUserConfirm(userId, userName) {
        Swal.fire({
            title: `ইউজার "${userName}" স্থায়ীভাবে ডিলিট করতে চান?`,
            text: 'সতর্কতা: ইউজারের পোস্ট, মেসেজ ও কমেন্ট সহ সকল ডাটা সম্পূর্ণ মুছে যাবে!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'হ্যাঁ, স্থায়ীভাবে ডিলিট করুন',
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'delete_user', user_id: userId })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        const row = document.getElementById('user-row-' + userId);
                        if (row) row.remove();
                        Swal.fire('ডিলিট সম্পন্ন!', 'ইউজারকে ডাটাবেজ থেকে মুছে ফেলা হয়েছে।', 'success');
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'ডিলিট সম্পন্ন হয়নি।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Open Single Notification Modal
    let notifyModalInstance = null;
    function openNotifyModal(userId, userName) {
        document.getElementById('notifyUserId').value = userId;
        document.getElementById('notifyUserName').innerText = userName + ' (ID: #' + userId + ')';

        if (!notifyModalInstance) {
            notifyModalInstance = new bootstrap.Modal(document.getElementById('notifyUserModal'));
        }
        notifyModalInstance.show();
    }

    function submitSingleNotification(e) {
        e.preventDefault();
        const form = document.getElementById('notifyUserForm');
        const formData = new FormData(form);

        fetch('api.php', {
            method: 'POST',
            body: formData
        })
        .then(r => r.json())
        .then(res => {
            if (res.success) {
                notifyModalInstance.hide();
                form.reset();
                Swal.fire('নোটিফিকেশন পাঠানো হয়েছে!', 'শুধুমাত্র এই নির্দিষ্ট ইউজারের ইনবক্সে বার্তা পৌঁছে গেছে।', 'success');
            } else {
                Swal.fire('ব্যর্থ!', res.message || 'নোটিফিকেশন পাঠানো সম্ভব হয়নি।', 'error');
            }
        })
        .catch(() => Swal.fire('ত্রুটি!', 'সার্ভার সংযোগ সমস্যা।', 'error'));
    }
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
