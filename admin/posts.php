<?php
/**
 * Posts Management Page
 * 4 Categories: Text Post, Image Post, Video Post, Stories Post
 * Instant Database Removal via Delete Icon
 */
require_once __DIR__ . '/includes/db.php';

$pageTitle = 'পোস্টসমূহ - Flikata Admin';
$pageHeading = 'পোস্ট ও কনটেন্ট ম্যানেজমেন্ট';
$pageSubheading = '৪ টি প্রধান ক্যাটাগরির পোস্ট মনিটরিং ও ডাটাবেজ থেকে সরাসরি অপসারণ';

$category = trim($_GET['tab'] ?? 'text');
if (!in_array($category, ['text', 'image', 'video', 'stories'])) {
    $category = 'text';
}

$searchQuery = trim($_GET['q'] ?? '');

// Counts for the 4 category tabs
$countText = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE (image_url IS NULL OR image_url = '') AND (video_url IS NULL OR video_url = '')")->fetchColumn();
$countImage = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE image_url IS NOT NULL AND image_url != ''")->fetchColumn();
$countVideo = (int)$pdo->query("SELECT COUNT(*) FROM posts WHERE video_url IS NOT NULL AND video_url != ''")->fetchColumn();
$countStories = 0;
try {
    $countStories = (int)$pdo->query("SELECT COUNT(*) FROM stories")->fetchColumn();
} catch (Exception $e) {}

// Fetch Posts or Stories depending on selected category
$items = [];
if ($category === 'stories') {
    $sql = "
        SELECT s.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.email AS author_email
        FROM stories s
        LEFT JOIN users u ON s.user_id = u.id
        WHERE 1=1
    ";
    $params = [];
    if (!empty($searchQuery)) {
        $sql .= " AND (s.text_content LIKE ? OR u.full_name LIKE ?)";
        $params[] = "%$searchQuery%";
        $params[] = "%$searchQuery%";
    }
    $sql .= " ORDER BY s.id DESC LIMIT 100";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $items = $stmt->fetchAll();
} else {
    $sql = "
        SELECT p.*, u.full_name AS author_name, u.profile_pic AS author_avatar, u.email AS author_email
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE 1=1
    ";
    $params = [];

    if ($category === 'text') {
        $sql .= " AND (p.image_url IS NULL OR p.image_url = '') AND (p.video_url IS NULL OR p.video_url = '')";
    } elseif ($category === 'image') {
        $sql .= " AND p.image_url IS NOT NULL AND p.image_url != ''";
    } elseif ($category === 'video') {
        $sql .= " AND p.video_url IS NOT NULL AND p.video_url != ''";
    }

    if (!empty($searchQuery)) {
        $sql .= " AND (p.content LIKE ? OR u.full_name LIKE ?)";
        $params[] = "%$searchQuery%";
        $params[] = "%$searchQuery%";
    }

    $sql .= " ORDER BY p.id DESC LIMIT 100";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $items = $stmt->fetchAll();
}

require_once __DIR__ . '/includes/header.php';
?>

<style>
    .nav-tabs-custom {
        border-bottom: 2px solid #e2e8f0;
        gap: 0.5rem;
    }
    .nav-tabs-custom .nav-link {
        border: none;
        color: #64748b;
        font-weight: 700;
        font-size: 0.95rem;
        padding: 0.85rem 1.5rem;
        border-radius: 12px 12px 0 0;
        transition: all 0.2s ease;
        position: relative;
    }
    .nav-tabs-custom .nav-link:hover {
        color: var(--primary);
        background: #f8fafc;
    }
    .nav-tabs-custom .nav-link.active {
        color: var(--primary);
        background: transparent;
        border-bottom: 3px solid var(--primary);
    }
    .post-card {
        background: #ffffff;
        border: 1px solid var(--border-color);
        border-radius: 16px;
        box-shadow: var(--shadow-card);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        display: flex;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
    }
    .post-card:hover {
        box-shadow: var(--shadow-hover);
        transform: translateY(-2px);
    }
    .post-media-container {
        width: 100%;
        height: 220px;
        background-color: #f1f5f9;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
    }
    .post-media-container img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
    }
    .post-media-container img:hover {
        transform: scale(1.03);
    }
    .btn-delete-icon {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        background-color: #fee2e2;
        color: #ef4444;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1rem;
        transition: all 0.2s ease;
    }
    .btn-delete-icon:hover {
        background-color: #ef4444;
        color: #ffffff;
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35);
    }
</style>

<!-- Top 4-Category Navigation Tabs -->
<div class="card-custom mb-4 p-3">
    <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">
        <!-- 4 Tabs as requested: টেক্সট পোস্ট, ইমেজ পোস্ট, ভিডিও পোস্ট, স্টোরিজ পোস্ট -->
        <ul class="nav nav-tabs-custom flex-nowrap overflow-x-auto pb-1 mb-0">
            <li class="nav-item">
                <a class="nav-link <?= $category === 'text' ? 'active' : '' ?>" href="posts.php?tab=text">
                    <i class="bi bi-file-text-fill me-1 text-primary"></i> টেক্সট পোস্ট
                    <span class="badge bg-primary-subtle text-primary rounded-pill ms-2"><?= $countText ?></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link <?= $category === 'image' ? 'active' : '' ?>" href="posts.php?tab=image">
                    <i class="bi bi-image-fill me-1 text-success"></i> ইমেজ পোস্ট
                    <span class="badge bg-success-subtle text-success rounded-pill ms-2"><?= $countImage ?></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link <?= $category === 'video' ? 'active' : '' ?>" href="posts.php?tab=video">
                    <i class="bi bi-camera-video-fill me-1 text-info"></i> ভিডিও পোস্ট
                    <span class="badge bg-info-subtle text-info rounded-pill ms-2"><?= $countVideo ?></span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link <?= $category === 'stories' ? 'active' : '' ?>" href="posts.php?tab=stories">
                    <i class="bi bi-hourglass-top me-1 text-warning"></i> স্টোরিজ পোস্ট
                    <span class="badge bg-warning-subtle text-dark rounded-pill ms-2"><?= $countStories ?></span>
                </a>
            </li>
        </ul>

        <!-- Search Bar within current category -->
        <form method="GET" action="posts.php" class="d-flex align-items-center gap-2" style="min-width: 260px;">
            <input type="hidden" name="tab" value="<?= htmlspecialchars($category) ?>">
            <div class="input-group">
                <input type="text" name="q" class="form-control form-control-sm bg-light" placeholder="পোস্টে খুঁজুন..." value="<?= htmlspecialchars($searchQuery) ?>" style="border-radius: 10px 0 0 10px;">
                <button type="submit" class="btn btn-sm btn-primary-custom" style="border-radius: 0 10px 10px 0;">
                    <i class="bi bi-search"></i>
                </button>
            </div>
            <?php if (!empty($searchQuery)): ?>
                <a href="posts.php?tab=<?= $category ?>" class="btn btn-sm btn-light border p-2" title="রিসেট">
                    <i class="bi bi-x-lg"></i>
                </a>
            <?php endif; ?>
        </form>
    </div>
</div>

<!-- Posts Cards Grid View -->
<?php if (empty($items)): ?>
    <div class="card-custom text-center py-5">
        <i class="bi bi-inbox display-3 text-muted mb-3 d-block"></i>
        <h5 class="fw-bold text-dark">এই ক্যাটাগরিতে কোনো পোস্ট পাওয়া যায়নি</h5>
        <p class="text-muted small">ব্যবহারকারীরা পোস্ট করলে বা স্টোরি দিলে তা স্বয়ংক্রিয়ভাবে এখানে প্রদর্শিত হবে।</p>
    </div>
<?php else: ?>
    <div class="row g-4">
        <?php foreach ($items as $item): ?>
            <div class="col-12 col-md-6 col-xl-4" id="item-card-<?= $category === 'stories' ? 'story-' . $item['id'] : 'post-' . $item['id'] ?>">
                <div class="post-card">
                    <!-- Post Header: Author info & Delete Button -->
                    <div class="p-3 border-bottom d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-2">
                            <img src="<?= !empty($item['author_avatar']) ? htmlspecialchars($item['author_avatar']) : 'https://ui-avatars.com/api/?name=' . urlencode($item['author_name'] ?? 'User') . '&background=4f46e5&color=fff' ?>" class="user-avatar-sm" alt="Author">
                            <div>
                                <div class="fw-bold text-dark text-truncate" style="max-width: 160px; font-size: 0.925rem;">
                                    <?= htmlspecialchars($item['author_name'] ?? 'User #' . $item['user_id']) ?>
                                </div>
                                <small class="text-muted" style="font-size: 0.75rem;">
                                    <?= date('d M, Y • h:i A', strtotime($item['created_at'])) ?>
                                </small>
                            </div>
                        </div>

                        <!-- Single Click Permanent Delete Icon Requested by User -->
                        <?php if ($category === 'stories'): ?>
                            <button class="btn-delete-icon" title="স্টোরি স্থায়ীভাবে মুছে ফেলুন" onclick="deleteStoryConfirm(<?= $item['id'] ?>)">
                                <i class="bi bi-trash3-fill"></i>
                            </button>
                        <?php else: ?>
                            <button class="btn-delete-icon" title="পোস্টটি ডাটাবেজ থেকে সরাসরি মুছে ফেলুন" onclick="deletePostConfirm(<?= $item['id'] ?>)">
                                <i class="bi bi-trash3-fill"></i>
                            </button>
                        <?php endif; ?>
                    </div>

                    <!-- Category Media or Story Display -->
                    <?php if ($category === 'image' && !empty($item['image_url'])): ?>
                        <div class="post-media-container">
                            <img src="<?= htmlspecialchars($item['image_url']) ?>" alt="Post Image" loading="lazy" onclick="previewImage('<?= htmlspecialchars(addslashes($item['image_url'])) ?>')">
                        </div>
                    <?php elseif ($category === 'video' && !empty($item['video_url'])): ?>
                        <div class="post-media-container bg-dark">
                            <video src="<?= htmlspecialchars($item['video_url']) ?>" controls style="max-height: 100%; max-width: 100%;"></video>
                        </div>
                    <?php elseif ($category === 'stories'): ?>
                        <?php if (!empty($item['story_image'])): ?>
                            <div class="post-media-container">
                                <img src="<?= htmlspecialchars($item['story_image']) ?>" alt="Story Image" loading="lazy">
                            </div>
                        <?php else: ?>
                            <div class="post-media-container text-white p-3 text-center" style="background-color: <?= htmlspecialchars($item['background_color'] ?: '#4f46e5') ?>;">
                                <div class="fw-bold fs-6"><?= htmlspecialchars($item['text_content'] ?: 'টেক্সট স্টোরি') ?></div>
                            </div>
                        <?php endif; ?>
                    <?php endif; ?>

                    <!-- Post Body / Content -->
                    <div class="p-3 flex-grow-1">
                        <?php
                            $textBody = $category === 'stories' ? ($item['text_content'] ?? '') : ($item['content'] ?? '');
                        ?>
                        <?php if (!empty($textBody)): ?>
                            <p class="text-dark mb-2" style="font-size: 0.925rem; white-space: pre-line;">
                                <?= htmlspecialchars($textBody) ?>
                            </p>
                        <?php else: ?>
                            <p class="text-muted fst-italic mb-2 small">[কোনো ক্যাপশন নেই]</p>
                        <?php endif; ?>
                    </div>

                    <!-- Post Footer Stats -->
                    <div class="p-3 bg-light border-top d-flex align-items-center justify-content-between small text-muted">
                        <?php if ($category === 'stories'): ?>
                            <div>
                                <i class="bi bi-hourglass-split text-warning me-1"></i>
                                মেয়াদ: <?= date('d M, h:i A', strtotime($item['expires_at'])) ?>
                            </div>
                            <span class="badge bg-warning-subtle text-dark fw-bold">24h Story</span>
                        <?php else: ?>
                            <div class="d-flex align-items-center gap-3">
                                <span><i class="bi bi-heart-fill text-danger me-1"></i> <?= (int)($item['likes_count'] ?? 0) ?> লাইক</span>
                                <span><i class="bi bi-chat-dots-fill text-primary me-1"></i> <?= (int)($item['comments_count'] ?? 0) ?> কমেন্ট</span>
                            </div>
                            <span class="badge bg-light border text-muted">ID: #<?= $item['id'] ?></span>
                        <?php endif; ?>
                    </div>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
<?php endif; ?>

<script>
    // Delete Post from Database
    function deletePostConfirm(postId) {
        Swal.fire({
            title: 'পোস্টটি নিশ্চিত মুছে ফেলতে চান?',
            text: 'এই পোস্টটি ডাটাবেজ থেকে সম্পূর্ণ মুছে যাবে এবং ইউজারদের অ্যাপ থেকেও উধাও হয়ে যাবে!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'হ্যাঁ, ডাটাবেজ থেকে মুছে দিন',
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'delete_post', post_id: postId })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        const card = document.getElementById('item-card-post-' + postId);
                        if (card) {
                            card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                            card.style.opacity = '0';
                            card.style.transform = 'scale(0.85)';
                            setTimeout(() => card.remove(), 400);
                        }
                        Toast.fire({ icon: 'success', title: 'পোস্টটি সফলভাবে মুছে ফেলা হয়েছে।' });
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'ডিলিট করা সম্ভব হয়নি।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    // Delete Story from Database
    function deleteStoryConfirm(storyId) {
        Swal.fire({
            title: 'স্টোরিটি মুছে ফেলতে চান?',
            text: 'এই স্টোরিটি ডাটাবেজ থেকে স্থায়ীভাবে মুছে যাবে।',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#64748b',
            confirmButtonText: 'হ্যাঁ, মুছে দিন',
            cancelButtonText: 'বাতিল'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('api.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ action: 'delete_story', story_id: storyId })
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        const card = document.getElementById('item-card-story-' + storyId);
                        if (card) {
                            card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                            card.style.opacity = '0';
                            card.style.transform = 'scale(0.85)';
                            setTimeout(() => card.remove(), 400);
                        }
                        Toast.fire({ icon: 'success', title: 'স্টোরিটি সফলভাবে মুছে ফেলা হয়েছে।' });
                    } else {
                        Swal.fire('ব্যর্থ!', res.message || 'ডিলিট করা সম্ভব হয়নি।', 'error');
                    }
                })
                .catch(() => Swal.fire('ত্রুটি!', 'সার্ভারে সংযোগ ব্যর্থ হয়েছে।', 'error'));
            }
        });
    }

    function previewImage(url) {
        Swal.fire({
            imageUrl: url,
            imageAlt: 'Preview',
            showConfirmButton: false,
            showCloseButton: true,
            background: '#ffffff'
        });
    }
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
