<?php
/**
 * Withdraw Methods Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'উইথড্র মেথড - Flikata Admin';
$pageHeading = 'উইথড্র মেথড (Withdraw Methods)';
$pageSubheading = 'ব্যবহারকারীদের টাকা তোলার পেমেন্ট মাধ্যম তৈরি, সম্পাদনা ও সীমা নিয়ন্ত্রণ';

$msg = '';
$err = '';

// Handle Add / Edit Method
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && in_array($_POST['action'], ['add_method', 'edit_method'])) {
    $name = trim($_POST['name'] ?? '');
    $number = trim($_POST['number'] ?? '');
    $imageUrl = trim($_POST['image_url'] ?? '');
    $description = trim($_POST['description'] ?? '');
    $minAmount = floatval($_POST['min_amount'] ?? 50.00);
    $maxAmount = floatval($_POST['max_amount'] ?? 25000.00);
    $isActive = (isset($_POST['is_active']) && $_POST['is_active'] == '1') ? 1 : 0;

    // Handle File Upload if provided
    if (isset($_FILES['logo_file']) && $_FILES['logo_file']['error'] === UPLOAD_ERR_OK) {
        $uploadDir = __DIR__ . '/uploads/methods/';
        if (!is_dir($uploadDir)) {
            @mkdir($uploadDir, 0777, true);
        }
        $ext = strtolower(pathinfo($_FILES['logo_file']['name'], PATHINFO_EXTENSION));
        $allowedExts = ['jpg', 'jpeg', 'png', 'webp', 'svg', 'gif'];
        if (in_array($ext, $allowedExts)) {
            $fileName = 'withdraw_' . time() . '_' . rand(100, 999) . '.' . $ext;
            $targetPath = $uploadDir . $fileName;
            if (move_uploaded_file($_FILES['logo_file']['tmp_name'], $targetPath)) {
                $isHttps = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ||
                           (isset($_SERVER['HTTP_X_FORWARDED_PROTO']) && $_SERVER['HTTP_X_FORWARDED_PROTO'] === 'https') ||
                           (isset($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == 443);
                $host = $_SERVER['HTTP_HOST'] ?? 'flikata.cyou';
                if ($host === 'localhost' || $host === '127.0.0.1') {
                    $protocol = $isHttps ? "https://" : "http://";
                    $imageUrl = $protocol . $host . '/admin/uploads/methods/' . $fileName;
                } else {
                    $imageUrl = 'https://' . $host . '/admin/uploads/methods/' . $fileName;
                }
            }
        }
    }

    if (empty($name)) {
        $err = 'মেথড নাম অবশ্যই দিতে হবে!';
    } else {
        try {
            if ($_POST['action'] === 'add_method') {
                $stmt = $pdo->prepare("INSERT INTO withdraw_methods (name, number, image_url, description, min_amount, max_amount, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)");
                $stmt->execute([$name, $number, $imageUrl, $description, $minAmount, $maxAmount, $isActive]);
                $msg = 'নতুন উইথড্র পেমেন্ট মেথড সফলভাবে যুক্ত হয়েছে!';
            } else {
                $id = intval($_POST['method_id'] ?? 0);
                if (empty($imageUrl)) {
                    $old = $pdo->prepare("SELECT image_url FROM withdraw_methods WHERE id = ?");
                    $old->execute([$id]);
                    $imageUrl = $old->fetchColumn() ?: '';
                }
                $stmt = $pdo->prepare("UPDATE withdraw_methods SET name = ?, number = ?, image_url = ?, description = ?, min_amount = ?, max_amount = ?, is_active = ? WHERE id = ?");
                $stmt->execute([$name, $number, $imageUrl, $description, $minAmount, $maxAmount, $isActive, $id]);
                $msg = 'উইথড্র মেথড সফলভাবে আপডেট করা হয়েছে!';
            }
        } catch (Exception $e) {
            $err = 'মেথড সেভ ব্যর্থ হয়েছে: ' . $e->getMessage();
        }
    }
}

// Handle Delete Method
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'delete_method') {
    $id = intval($_POST['method_id'] ?? 0);
    try {
        $stmt = $pdo->prepare("DELETE FROM withdraw_methods WHERE id = ?");
        $stmt->execute([$id]);
        $msg = 'উইথড্র মেথড সফলভাবে মুছে ফেলা হয়েছে!';
    } catch (Exception $e) {
        $err = 'মেথড মুছতে সমস্যা হয়েছে: ' . $e->getMessage();
    }
}

$methods = [];
try {
    $methods = $pdo->query("SELECT * FROM withdraw_methods ORDER BY id DESC")->fetchAll() ?: [];
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

<div class="card-custom">
    <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
        <div>
            <h5 class="fw-bold mb-1"><i class="bi bi-credit-card-2-front-fill text-warning me-2"></i>উইথড্র মেথড তালিকা</h5>
            <small class="text-muted">ইউজাররা ওয়ালেট থেকে ব্যালেন্স উইথড্র করার সময় সক্রিয় মেথডগুলো দেখতে পাবে</small>
        </div>
        <button class="btn btn-primary-custom" data-bs-toggle="modal" data-bs-target="#methodModal" onclick="openAddModal()">
            <i class="bi bi-plus-lg me-1"></i> নতুন উইথড্র মেথড যুক্ত করুন
        </button>
    </div>

    <div class="table-responsive">
        <table class="table table-custom align-middle">
            <thead>
                <tr>
                    <th style="width: 70px;">লোগো</th>
                    <th>মেথডের নাম</th>
                    <th>নাম্বার / হেল্পলাইন</th>
                    <th>সর্বনিম্ন / সর্বোচ্চ সীমা</th>
                    <th>নির্দেশনা</th>
                    <th>স্ট্যাটাস</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($methods)): ?>
                <tr>
                    <td colspan="7" class="text-center py-5 text-muted">
                        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                        কোন উইথড্র মেথড তৈরি করা হয়নি। নতুন মেথড যুক্ত করুন।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($methods as $m): ?>
                <tr>
                    <td>
                        <?php if (!empty($m['image_url'])): ?>
                            <img src="<?= htmlspecialchars($m['image_url']) ?>" alt="Logo" class="rounded-3 border p-1 bg-white" style="width: 44px; height: 44px; object-fit: contain;">
                        <?php else: ?>
                            <div class="bg-light text-warning rounded-3 border d-flex align-items-center justify-content-center" style="width: 44px; height: 44px;">
                                <i class="bi bi-credit-card-2-front fs-5"></i>
                            </div>
                        <?php endif; ?>
                    </td>
                    <td class="fw-bold text-dark">
                        <?= htmlspecialchars($m['name']) ?>
                    </td>
                    <td>
                        <span class="badge bg-light text-dark fs-6 px-3 py-1 font-monospace border">
                            <?= htmlspecialchars($m['number'] ?: 'N/A') ?>
                        </span>
                    </td>
                    <td>
                        <span class="badge bg-primary-subtle text-primary fw-bold">৳ <?= number_format($m['min_amount'], 2) ?></span>
                        -
                        <span class="badge bg-dark-subtle text-dark fw-bold">৳ <?= number_format($m['max_amount'], 2) ?></span>
                    </td>
                    <td class="text-muted small" style="max-width: 250px;">
                        <?= htmlspecialchars($m['description'] ?: 'কোন নির্দেশনা নেই') ?>
                    </td>
                    <td>
                        <?php if ((int)$m['is_active'] === 1): ?>
                            <span class="badge bg-success-subtle text-success rounded-pill px-3 py-1 fw-bold"><i class="bi bi-check-circle me-1"></i>সক্রিয় (Active)</span>
                        <?php else: ?>
                            <span class="badge bg-danger-subtle text-danger rounded-pill px-3 py-1 fw-bold"><i class="bi bi-x-circle me-1"></i>ডিজেবল / নিষ্ক্রিয়</span>
                        <?php endif; ?>
                    </td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary rounded-pill px-3 me-1" 
                            onclick='openEditModal(<?= json_encode($m) ?>)'>
                            <i class="bi bi-pencil-square"></i> এডিট
                        </button>
                        <form method="POST" action="withdraw_methods.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই উইথড্র মেথডটি ডিলিট করতে চান?');">
                            <input type="hidden" name="action" value="delete_method">
                            <input type="hidden" name="method_id" value="<?= $m['id'] ?>">
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

<!-- Add / Edit Method Modal -->
<div class="modal fade" id="methodModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-header border-bottom">
                <h5 class="modal-title fw-bold" id="methodModalTitle">নতুন উইথড্র মেথড</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="withdraw_methods.php" enctype="multipart/form-data">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" id="modalAction" value="add_method">
                    <input type="hidden" name="method_id" id="modalMethodId" value="">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">মেথড নাম <span class="text-danger">*</span></label>
                        <input type="text" class="form-control rounded-3" name="name" id="inputName" placeholder="যেমন: bKash Personal বা Nagad Personal বা Bank" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">নাম্বার / হেল্পলাইন</label>
                        <input type="text" class="form-control rounded-3 font-monospace" name="number" id="inputNumber" placeholder="যেমন: 017XXXXXXXX বা হেল্পলাইন">
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label fw-semibold">সর্বনিম্ন উইথড্র (৳)</label>
                            <input type="number" step="0.01" class="form-control rounded-3" name="min_amount" id="inputMinAmount" value="50">
                        </div>
                        <div class="col-6">
                            <label class="form-label fw-semibold">সর্বোচ্চ উইথড্র (৳)</label>
                            <input type="number" step="0.01" class="form-control rounded-3" name="max_amount" id="inputMaxAmount" value="25000">
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">লোগো ছবি ফাইল আপলোড করুন</label>
                        <input type="file" class="form-control rounded-3" name="logo_file" id="inputLogoFile" accept="image/*">
                        <small class="text-muted d-block mt-1">সরাসরি ইমেজ আপলোড করতে পারেন (JPG, PNG, WebP)</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">অথবা লোগো ইমেজ লিঙ্ক (URL)</label>
                        <input type="text" class="form-control rounded-3" name="image_url" id="inputImageUrl" placeholder="https://example.com/logo.png">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">উইথড্র নির্দেশনা (ইউজারদের জন্য)</label>
                        <textarea class="form-control rounded-3" name="description" id="inputDescription" rows="2" placeholder="যেমন: ১-২ ঘণ্টার মধ্যে আপনার একাউন্টে টাকা পাঠিয়ে দেওয়া হবে।"></textarea>
                    </div>

                    <div class="form-check form-switch p-3 bg-light rounded-3 d-flex align-items-center justify-content-between mt-3">
                        <label class="form-check-label fw-bold" for="inputIsActive">
                            মেথড স্ট্যাটাস (সক্রিয় রাখুন)
                        </label>
                        <input class="form-check-input fs-5 ms-0" type="checkbox" role="switch" name="is_active" id="inputIsActive" value="1" checked>
                    </div>
                    <small class="text-muted d-block mt-1">এটি বন্ধ (নিষ্ক্রিয়) করে রাখলে ব্যবহারকারীরা অ্যাপ্লিকেশনে এই মেথড দেখতে পাবে না।</small>
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
function openAddModal() {
    document.getElementById('methodModalTitle').innerText = 'নতুন উইথড্র মেথড';
    document.getElementById('modalAction').value = 'add_method';
    document.getElementById('modalMethodId').value = '';
    document.getElementById('inputName').value = '';
    document.getElementById('inputNumber').value = '';
    document.getElementById('inputMinAmount').value = '50';
    document.getElementById('inputMaxAmount').value = '25000';
    document.getElementById('inputLogoFile').value = '';
    document.getElementById('inputImageUrl').value = '';
    document.getElementById('inputDescription').value = '';
    document.getElementById('inputIsActive').checked = true;
}

function openEditModal(m) {
    document.getElementById('methodModalTitle').innerText = 'উইথড্র মেথড এডিট';
    document.getElementById('modalAction').value = 'edit_method';
    document.getElementById('modalMethodId').value = m.id;
    document.getElementById('inputName').value = m.name;
    document.getElementById('inputNumber').value = m.number || '';
    document.getElementById('inputMinAmount').value = m.min_amount || '50';
    document.getElementById('inputMaxAmount').value = m.max_amount || '25000';
    document.getElementById('inputLogoFile').value = '';
    document.getElementById('inputImageUrl').value = m.image_url || '';
    document.getElementById('inputDescription').value = m.description || '';
    document.getElementById('inputIsActive').checked = (parseInt(m.is_active) === 1);
    new bootstrap.Modal(document.getElementById('methodModal')).show();
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
