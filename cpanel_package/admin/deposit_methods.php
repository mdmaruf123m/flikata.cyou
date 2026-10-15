<?php
/**
 * Deposit Methods Management
 * Flikata Admin Control Panel
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'ডিপোজিট মেথড - Flikata Admin';
$pageHeading = 'ডিপোজিট মেথড (Deposit Methods)';
$pageSubheading = 'পেমেন্ট মেথড তৈরি, সম্পাদনা ও নাম্বার নিয়ন্ত্রণ (যেমন: bKash, Nagad, Rocket)';

$msg = '';
$err = '';

// Handle Add / Edit Method
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && in_array($_POST['action'], ['add_method', 'edit_method'])) {
    $name = trim($_POST['name'] ?? '');
    $number = trim($_POST['number'] ?? '');
    $imageUrl = trim($_POST['image_url'] ?? '');
    $description = trim($_POST['description'] ?? '');
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
            $fileName = 'method_' . time() . '_' . rand(100, 999) . '.' . $ext;
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

    if (empty($name) || empty($number)) {
        $err = 'মেথড নাম এবং নাম্বার অবশ্যই দিতে হবে!';
    } else {
        try {
            if ($_POST['action'] === 'add_method') {
                $stmt = $pdo->prepare("INSERT INTO deposit_methods (name, number, image_url, description, is_active) VALUES (?, ?, ?, ?, ?)");
                $stmt->execute([$name, $number, $imageUrl, $description, $isActive]);
                $msg = 'নতুন ডিপোজিট পেমেন্ট মেথড সফলভাবে যুক্ত হয়েছে!';
            } else {
                $id = intval($_POST['method_id'] ?? 0);
                if (empty($imageUrl)) {
                    $old = $pdo->prepare("SELECT image_url FROM deposit_methods WHERE id = ?");
                    $old->execute([$id]);
                    $imageUrl = $old->fetchColumn() ?: '';
                }
                $stmt = $pdo->prepare("UPDATE deposit_methods SET name = ?, number = ?, image_url = ?, description = ?, is_active = ? WHERE id = ?");
                $stmt->execute([$name, $number, $imageUrl, $description, $isActive, $id]);
                $msg = 'ডিপোজিট মেথড সফলভাবে আপডেট করা হয়েছে!';
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
        $stmt = $pdo->prepare("DELETE FROM deposit_methods WHERE id = ?");
        $stmt->execute([$id]);
        $msg = 'ডিপোজিট মেথড সফলভাবে মুছে ফেলা হয়েছে!';
    } catch (Exception $e) {
        $err = 'মেথড মুছতে সমস্যা হয়েছে: ' . $e->getMessage();
    }
}

$methods = [];
try {
    $methods = $pdo->query("SELECT * FROM deposit_methods ORDER BY id DESC")->fetchAll() ?: [];
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
            <h5 class="fw-bold mb-1"><i class="bi bi-wallet2 text-primary me-2"></i>ডিপোজিট মেথড তালিকা</h5>
            <small class="text-muted">ইউজাররা ওয়ালেট রিচার্জ/ডিপোজিট করার সময় এই সক্রিয় পেমেন্ট মেথডগুলো দেখতে পাবে</small>
        </div>
        <button class="btn btn-primary-custom" data-bs-toggle="modal" data-bs-target="#methodModal" onclick="openAddModal()">
            <i class="bi bi-plus-lg me-1"></i> নতুন মেথড যুক্ত করুন
        </button>
    </div>

    <div class="table-responsive">
        <table class="table table-custom">
            <thead>
                <tr>
                    <th>লোগো</th>
                    <th>মেথড নাম</th>
                    <th>মেথড নাম্বার</th>
                    <th>পেমেন্ট নির্দেশনা (বর্ণনা)</th>
                    <th>স্ট্যাটাস</th>
                    <th class="text-end">অ্যাকশন</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($methods)): ?>
                <tr>
                    <td colspan="6" class="text-center py-5 text-muted">
                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                        কোন ডিপোজিট মেথড পাওয়া যায়নি। "+ নতুন মেথড যুক্ত করুন" বাটনে ক্লিক করে যোগ করুন।
                    </td>
                </tr>
                <?php else: ?>
                <?php foreach ($methods as $m): ?>
                <tr>
                    <td>
                        <?php if (!empty($m['image_url'])): ?>
                            <img src="<?= htmlspecialchars($m['image_url']) ?>" style="width: 42px; height: 42px; object-fit: contain; border-radius: 10px; border: 1px solid #e2e8f0; background: #fff; padding: 2px;" alt="Logo" onerror="this.style.display='none'">
                        <?php else: ?>
                            <div class="bg-primary-subtle text-primary rounded-3 d-flex align-items-center justify-content-center fw-bold" style="width: 42px; height: 42px;">
                                <?= mb_substr($m['name'], 0, 1) ?>
                            </div>
                        <?php endif; ?>
                    </td>
                    <td class="fw-bold text-dark">
                        <?= htmlspecialchars($m['name']) ?>
                    </td>
                    <td>
                        <span class="badge bg-light text-dark fs-6 px-3 py-1 font-monospace border">
                            <?= htmlspecialchars($m['number']) ?>
                        </span>
                    </td>
                    <td class="text-muted small" style="max-width: 300px;">
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
                        <form method="POST" action="deposit_methods.php" class="d-inline" onsubmit="return confirm('আপনি কি নিশ্চিত এই মেথডটি ডিলিট করতে চান?');">
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
                <h5 class="modal-title fw-bold" id="methodModalTitle">নতুন ডিপোজিট মেথড</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="POST" action="deposit_methods.php" enctype="multipart/form-data">
                <div class="modal-body p-4">
                    <input type="hidden" name="action" id="modalAction" value="add_method">
                    <input type="hidden" name="method_id" id="modalMethodId" value="">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">মেথড নাম <span class="text-danger">*</span></label>
                        <input type="text" class="form-control rounded-3" name="name" id="inputName" placeholder="যেমন: bKash Personal বা Nagad Agent" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">পেমেন্ট মেথড নাম্বার <span class="text-danger">*</span></label>
                        <input type="text" class="form-control rounded-3 font-monospace" name="number" id="inputNumber" placeholder="যেমন: 017XXXXXXXX" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">লোগো ছবি ফাইল আপলোড করুন (ছবি সিলেক্ট করুন)</label>
                        <input type="file" class="form-control rounded-3" name="logo_file" id="inputLogoFile" accept="image/*">
                        <small class="text-muted d-block mt-1">লোগো ইমেজ ফাইল সরাসরি আপলোড করতে পারবেন (JPG, PNG, WebP)</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">অথবা লোগো ইমেজ লিঙ্ক (URL)</label>
                        <input type="text" class="form-control rounded-3" name="image_url" id="inputImageUrl" placeholder="https://example.com/bkash.png">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">পেমেন্ট নির্দেশনা (কিভাবে পেমেন্ট করতে হবে)</label>
                        <textarea class="form-control rounded-3" name="description" id="inputDescription" rows="3" placeholder="যেমন: এই নাম্বারে Send Money করুন। পেমেন্ট সফল হলে TrxID কপি করে নিচের বক্সে দিন..."></textarea>
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
    document.getElementById('methodModalTitle').innerText = 'নতুন ডিপোজিট মেথড';
    document.getElementById('modalAction').value = 'add_method';
    document.getElementById('modalMethodId').value = '';
    document.getElementById('inputName').value = '';
    document.getElementById('inputNumber').value = '';
    document.getElementById('inputLogoFile').value = '';
    document.getElementById('inputImageUrl').value = '';
    document.getElementById('inputDescription').value = '';
    document.getElementById('inputIsActive').checked = true;
}

function openEditModal(m) {
    document.getElementById('methodModalTitle').innerText = 'ডিপোজিট মেথড এডিট';
    document.getElementById('modalAction').value = 'edit_method';
    document.getElementById('modalMethodId').value = m.id;
    document.getElementById('inputName').value = m.name;
    document.getElementById('inputNumber').value = m.number;
    document.getElementById('inputLogoFile').value = '';
    document.getElementById('inputImageUrl').value = m.image_url || '';
    document.getElementById('inputDescription').value = m.description || '';
    document.getElementById('inputIsActive').checked = (parseInt(m.is_active) === 1);
    new bootstrap.Modal(document.getElementById('methodModal')).show();
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
