<?php
/**
 * Verification Packages & Requirements Settings
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ভেরিফিকেশন ব্যাচ প্যাকেজ - Flikata Admin';
$pageHeading = 'ভেরিফিকেশন ব্যাচ (Verification Badge)';
$pageSubheading = 'প্যাকেজ তৈরি, সম্পাদনা, মুছে ফেলা এবং আইডি কার্ড/সেলফি রিকোয়ারমেন্ট কন্ট্রোল';

$msg = '';
$err = '';

// Handle Settings Update (ID card and Selfie toggles)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'update_settings') {
    $idCardReq = isset($_POST['id_card_required']) ? 1 : 0;
    $selfieReq = isset($_POST['selfie_required']) ? 1 : 0;
    $graceDays = intval($_POST['grace_period_days'] ?? 3);

    try {
        $stmt = $pdo->prepare("UPDATE verification_settings SET id_card_required = ?, selfie_required = ?, grace_period_days = ? WHERE id = 1");
        $stmt->execute([$idCardReq, $selfieReq, $graceDays]);
        $msg = 'ভেরিফিকেশন রিকোয়ারমেন্ট সেটিংস সফলভাবে আপডেট হয়েছে!';
    } catch (Exception $e) {
        $err = 'সেটিংস আপডেট ব্যর্থ: ' . $e->getMessage();
    }
}

// Handle Add / Edit Package
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && in_array($_POST['action'], ['add_package', 'edit_package'])) {
    $title = trim($_POST['title'] ?? '');
    $description = trim($_POST['description'] ?? '');
    $price = floatval($_POST['price'] ?? 0);
    $durationType = $_POST['duration_type'] ?? 'month';
    $durationVal = intval($_POST['duration_value'] ?? 1);
    $isActive = isset($_POST['is_active']) ? 1 : 0;

    if (empty($title)) {
        $err = 'প্যাকেজের টাইটেল অবশ্যই পূরণ করতে হবে!';
    } else {
        if ($durationType === 'lifetime') {
            $durationVal = 0;
        }

        try {
            if ($_POST['action'] === 'add_package') {
                $stmt = $pdo->prepare("INSERT INTO verification_packages (title, description, price, duration_type, duration_value, is_active) VALUES (?, ?, ?, ?, ?, ?)");
                $stmt->execute([$title, $description, $price, $durationType, $durationVal, $isActive]);
                $msg = 'নতুন ভেরিফিকেশন প্যাকেজ যুক্ত করা হয়েছে!';
            } else {
                $id = intval($_POST['package_id'] ?? 0);
                $stmt = $pdo->prepare("UPDATE verification_packages SET title = ?, description = ?, price = ?, duration_type = ?, duration_value = ?, is_active = ? WHERE id = ?");
                $stmt->execute([$title, $description, $price, $durationType, $durationVal, $isActive, $id]);
                $msg = 'ভেরিফিকেশন প্যাকেজ সফলভাবে আপডেট করা হয়েছে!';
            }
        } catch (Exception $e) {
            $err = 'প্যাকেজ সেভ করতে সমস্যা হয়েছে: ' . $e->getMessage();
        }
    }
}

// Handle Delete Package
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'delete_package') {
    $id = intval($_POST['package_id'] ?? 0);
    try {
        $stmt = $pdo->prepare("DELETE FROM verification_packages WHERE id = ?");
        $stmt->execute([$id]);
        $msg = 'ভেরিফিকেশন প্যাকেজ মুছে ফেলা হয়েছে!';
    } catch (Exception $e) {
        $err = 'প্যাকেজ মুছতে সমস্যা হয়েছে: ' . $e->getMessage();
    }
}

// Fetch Settings
$settings = ['id_card_required' => 0, 'selfie_required' => 0, 'grace_period_days' => 3];
$packages = [];

try {
    $settings = $pdo->query("SELECT * FROM verification_settings WHERE id = 1")->fetch() ?: ['id_card_required' => 0, 'selfie_required' => 0, 'grace_period_days' => 3];
    $packages = $pdo->query("SELECT * FROM verification_packages ORDER BY id DESC")->fetchAll() ?: [];
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

<!-- Requirements Setting Box -->
<div class="card-custom mb-4">
    <div class="d-flex align-items-center justify-content-between mb-3 border-bottom pb-3">
        <div>
            <h5 class="fw-bold mb-1"><i class="bi bi-shield-check text-primary me-2"></i>ভেরিফিকেশন প্রয়োজনীয় রিকোয়ারমেন্ট সেটিংস</h5>
            <small class="text-muted">ইউজাররা ভেরিফিকেশন আবেদন করার সময় আইডি কার্ড বা সেলফি দিতে বাধ্য থাকবে কিনা তা নির্ধারণ করুন</small>
        </div>
    </div>
    <form method="POST" action="verification_packages.php" class="row g-3 align-items-center">
        <input type="hidden" name="action" value="update_settings">
        
        <div class="col-md-4">
            <div class="form-check form-switch p-3 bg-light rounded-3 d-flex align-items-center justify-content-between">
                <label class="form-check-label fw-semibold" for="idCardToggle">
                    <i class="bi bi-card-heading me-1 text-primary"></i> আইডি কার্ড (সামনে ও পেছনে)
                </label>
                <input class="form-check-input fs-5 ms-0" type="checkbox" role="switch" id="idCardToggle" name="id_card_required" <?= !empty($settings['id_card_required']) ? 'checked' : '' ?>>
            </div>
        </div>

        <div class="col-md-4">
            <div class="form-check form-switch p-3 bg-light rounded-3 d-flex align-items-center justify-content-between">
                <label class="form-check-label fw-semibold" for="selfieToggle">
                    <i class="bi bi-camera me-1 text-primary"></i> লাইভ সেলফি ফটো
                </label>
                <input class="form-check-input fs-5 ms-0" type="checkbox" role="switch" id="selfieToggle" name="selfie_required" <?= !empty($settings['selfie_required']) ? 'checked' : '' ?>>
            </div>
        </div>

        <div class="col-md-4 text-end">
            <button type="submit" class="btn btn-primary-custom px-4 py-2 w-100">
                <i class="bi bi-save me-1"></i> সেটিংস সংরক্ষণ করুন
            </button>
        </div>
    </form>
</div>

<!-- Packages List Card -->
<div class="card-custom">
    <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
        <div>
            <h5 class="fw-bold mb-1"><i class="bi bi-patch-check-fill text-info me-2"></i>ভেরিফিকেশন প্যাকেজ তালিকা</h5>
            <small class="text-muted">ইউজাররা যেসকল প্যাকেজের জন্য আবেদন করতে পারবে (আনলিমিটেড প্যাকেজ যোগ/সম্পাদনা/ডিলিট)</small>
        </div>
        <button class="btn btn-primary-custom" data-bs-toggle="modal" data-bs-target="#packageModal" onclick="openAddModal()">
            <i class="bi bi-plus-lg me-1"></i> নতুন প্যাকেজ যুক্ত করুন
        </button>
    </div>

    <div class="table-responsive">
        <table class="table table-custom">
            <thead>
                <tr>
                    <th>প্যাকেজ টাইটেল</th>
                    <th>বর্ণনা</th>
                    <th>মূল্য (টাকা)</th>
                    <th>মেয়াদ (Duration)</th>
                    <th>স্ট্যাটাস</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($packages)): ?>
                <tr>
                    <td colspan="6" class="text-center py-4 text-muted">
                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                        কোন প্যাকেজ পাওয়া যায়নি। উপরের "+ নতুন প্যাকেজ যুক্ত করুন" বাটনে ক্লিক করে প্যাকেজ যোগ করুন।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($packages as $pkg): ?>
                <tr>
                    <td class="fw-bold text-dark">
                        <span class="d-flex align-items-center gap-2">
                            <i class="bi bi-patch-check-fill text-primary fs-5"></i>
                            <?= htmlspecialchars($pkg['title']) ?>
                        </span>
                    </td>
                    <td class="text-muted small" style="max-width: 250px;">
                        <?= htmlspecialchars($pkg['description'] ?: 'কোন বর্ণনা নেই') ?>
                    </td>
                    <td>
                        <span class="badge bg-success-subtle text-success fs-6 fw-bold px-3 py-1">
                            ৳ <?= number_format($pkg['price'], 2) ?>
                        </span>
                    </td>
                    <td>
                        <span class="badge bg-primary-subtle text-primary px-3 py-1 fw-bold">
                            <?php 
                            if ($pkg['duration_type'] === 'lifetime') {
                                echo 'লাইফ টাইম (Lifetime)';
                            } else {
                                $types = ['day' => 'দিন', 'month' => 'মাস', 'year' => 'বছর'];
                                echo htmlspecialchars($pkg['duration_value'] . ' ' . ($types[$pkg['duration_type']] ?? $pkg['duration_type']));
                            }
                            ?>
                        </span>
                    </td>
                    <td>
                        <?php if ($pkg['is_active']): ?>
                            <span class="badge bg-success-subtle text-success rounded-pill px-3 py-1"><i class="bi bi-check-circle me-1"></i>সক্রিয়</span>
                        <?php else: ?>
                            <span class="badge bg-secondary-subtle text-secondary rounded-pill px-3 py-1"><i class="bi bi-x-circle me-1"></i>নিষ্ক্রিয়</span>
                        <?php endif; ?>
                    </td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary rounded-pill px-3 me-1" 
                            onclick='openEditModal(<?= json_encode($pkg) ?>)'>
                            <i class="bi bi-pencil-square"></i> এডিট
                        </button>
                        <form method="POST" action="verification_packages.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই প্যাকেজটি ডিলিট করতে চান?');">
                            <input type="hidden" name="action" value="delete_package">
                            <input type="hidden" name="package_id" value="<?= $pkg['id'] ?>">
                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-2">
                                <i class="bi bi-trash"></i>
                            </button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<!-- Add / Edit Package Modal -->
<div class="modal fade" id="packageModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-bottom">
                <h5 class="modal-title fw-bold" id="packageModalTitle">নতুন ভেরিফিকেশন প্যাকেজ</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="verification_packages.php">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" id="modalAction" value="add_package">
                    <input type="hidden" name="package_id" id="modalPackageId" value="">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">প্যাকেজের টাইটেল <span class="text-danger">*</span></label>
                        <input type="text" class="form-control rounded-3" name="title" id="inputTitle" placeholder="যেমন: ১ মাসের ভেরিফাইড ব্যাজ" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">প্যাকেজের বর্ণনা (অপশনাল)</label>
                        <textarea class="form-control rounded-3" name="description" id="inputDescription" rows="2" placeholder="প্যাকেজের সুযোগ-সুবিধা লিখুন..."></textarea>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">দাম (টাকা) <span class="text-danger">*</span></label>
                            <input type="number" step="0.01" class="form-control rounded-3" name="price" id="inputPrice" placeholder="0.00" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">ডিউরেশন টাইপ <span class="text-danger">*</span></label>
                            <select class="form-select rounded-3" name="duration_type" id="inputDurationType" onchange="toggleDurationBox()">
                                <option value="day">দিন (Day)</option>
                                <option value="month" selected>মাস (Month)</option>
                                <option value="year">বছর (Year)</option>
                                <option value="lifetime">লাইফ টাইম (Lifetime)</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3" id="durationValueContainer">
                        <label class="form-label fw-semibold">কত দিন/মাস/বছর এর জন্য?</label>
                        <input type="number" min="1" class="form-control rounded-3" name="duration_value" id="inputDurationValue" value="1" placeholder="সংখ্যা লিখুন (যেমন: ১)">
                    </div>

                    <div class="form-check form-switch mt-3">
                        <input class="form-check-input" type="checkbox" role="switch" name="is_active" id="inputIsActive" checked>
                        <label class="form-check-label fw-semibold" for="inputIsActive">প্যাকেজ সক্রিয় রাখুন</label>
                    </div>
                </div>
                <div class="modal-footer border-top">
                    <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">বাতিল</button>
                    <button type="submit" class="btn btn-primary-custom rounded-pill px-4">সংরক্ষণ করুন</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function toggleDurationBox() {
    const val = document.getElementById('inputDurationType').value;
    const container = document.getElementById('durationValueContainer');
    if (val === 'lifetime') {
        container.style.display = 'none';
    } else {
        container.style.display = 'block';
    }
}

function openAddModal() {
    document.getElementById('packageModalTitle').innerText = 'নতুন ভেরিফিকেশন প্যাকেজ';
    document.getElementById('modalAction').value = 'add_package';
    document.getElementById('modalPackageId').value = '';
    document.getElementById('inputTitle').value = '';
    document.getElementById('inputDescription').value = '';
    document.getElementById('inputPrice').value = '0.00';
    document.getElementById('inputDurationType').value = 'month';
    document.getElementById('inputDurationValue').value = '1';
    document.getElementById('inputIsActive').checked = true;
    toggleDurationBox();
}

function openEditModal(pkg) {
    document.getElementById('packageModalTitle').innerText = 'ভেরিফিকেশন প্যাকেজ এডিট';
    document.getElementById('modalAction').value = 'edit_package';
    document.getElementById('modalPackageId').value = pkg.id;
    document.getElementById('inputTitle').value = pkg.title;
    document.getElementById('inputDescription').value = pkg.description || '';
    document.getElementById('inputPrice').value = pkg.price;
    document.getElementById('inputDurationType').value = pkg.duration_type;
    document.getElementById('inputDurationValue').value = pkg.duration_value;
    document.getElementById('inputIsActive').checked = (pkg.is_active == 1);
    toggleDurationBox();
    new bootstrap.Modal(document.getElementById('packageModal')).show();
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
