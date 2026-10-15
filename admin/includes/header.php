<?php
/**
 * Admin Panel Header & Layout Component
 * Light Mode Only, Card-Style Design
 */
require_once __DIR__ . '/db.php';
checkAdminAuth();

$currentPage = basename($_SERVER['PHP_SELF']);
$adminName = $_SESSION['admin_name'] ?? 'Super Admin';
$adminEmail = $_SESSION['admin_email'] ?? 'admin@gmail.com';

// Counts for badges
$pendingVerifCount = 0;
$pendingDepositCount = 0;
$pendingWithdrawCount = 0;
$pendingMonetizationCount = 0;
try {
    $pendingVerifCount = (int)$pdo->query("SELECT COUNT(*) FROM verification_requests WHERE status = 'pending'")->fetchColumn();
    $pendingDepositCount = (int)$pdo->query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'pending'")->fetchColumn();
    $pendingWithdrawCount = (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'pending'")->fetchColumn();
    $pendingMonetizationCount = (int)$pdo->query("SELECT COUNT(*) FROM monetization_requests WHERE status = 'pending'")->fetchColumn();
} catch (Exception $e) {}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= isset($pageTitle) ? htmlspecialchars($pageTitle) . ' - Flikata Admin' : 'Flikata Admin Panel' ?></title>
    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons & FontAwesome -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <!-- Chart.js CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --primary-light: #eef2ff;
            --primary-subtle: #e0e7ff;
            --accent: #06b6d4;
            --bg-body: #f8fafc;
            --card-bg: #ffffff;
            --text-main: #0f172a;
            --text-muted: #64748b;
            --border-color: #e2e8f0;
            --sidebar-width: 270px;
            --sidebar-bg: #ffffff;
            --topbar-height: 70px;
            --shadow-card: 0 4px 20px -2px rgba(0, 0, 0, 0.05), 0 2px 6px -1px rgba(0, 0, 0, 0.02);
            --shadow-hover: 0 10px 25px -3px rgba(0, 0, 0, 0.08), 0 4px 10px -2px rgba(0, 0, 0, 0.03);
            --radius-card: 16px;
        }

        body {
            font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: var(--bg-body);
            color: var(--text-main);
            overflow-x: hidden;
            min-height: 100vh;
        }

        /* Sidebar Styling */
        #sidebar {
            width: var(--sidebar-width);
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            background-color: var(--sidebar-bg);
            border-right: 1px solid var(--border-color);
            z-index: 1040;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            display: flex;
            flex-direction: column;
            box-shadow: 2px 0 12px rgba(0, 0, 0, 0.03);
        }

        #sidebar.collapsed {
            left: calc(-1 * var(--sidebar-width));
        }

        .sidebar-brand {
            height: var(--topbar-height);
            display: flex;
            align-items: center;
            padding: 0 1.5rem;
            border-bottom: 1px solid var(--border-color);
            background: #ffffff;
        }

        .sidebar-brand .brand-logo {
            width: 38px;
            height: 38px;
            background: linear-gradient(135deg, #4f46e5 0%, #06b6d4 100%);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 1.25rem;
            font-weight: 800;
            margin-right: 0.85rem;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
        }

        .sidebar-nav {
            padding: 1.25rem 0.85rem;
            flex-grow: 1;
            overflow-y: auto;
        }

        .nav-label {
            font-size: 0.725rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: #94a3b8;
            padding: 0.75rem 1rem 0.35rem 1rem;
        }

        .sidebar-link {
            display: flex;
            align-items: center;
            gap: 0.85rem;
            padding: 0.8rem 1.15rem;
            border-radius: 12px;
            font-weight: 600;
            font-size: 0.925rem;
            color: #475569;
            text-decoration: none;
            transition: all 0.2s ease;
            margin-bottom: 0.35rem;
        }

        .sidebar-link i {
            font-size: 1.15rem;
            color: #64748b;
            transition: all 0.2s ease;
            width: 24px;
            text-align: center;
        }

        .sidebar-link:hover {
            color: var(--primary);
            background-color: var(--primary-light);
        }

        .sidebar-link:hover i {
            color: var(--primary);
            transform: scale(1.1);
        }

        .sidebar-link.active {
            color: var(--primary);
            background-color: var(--primary-light);
            font-weight: 700;
            box-shadow: inset 3px 0 0 var(--primary);
        }

        .sidebar-link.active i {
            color: var(--primary);
        }

        .sidebar-footer {
            padding: 1.2rem;
            border-top: 1px solid var(--border-color);
            background: #ffffff;
        }

        /* Topbar Styling */
        #topbar {
            height: var(--topbar-height);
            position: fixed;
            top: 0;
            right: 0;
            left: var(--sidebar-width);
            background: #ffffff;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 1.75rem;
            z-index: 1030;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        #topbar.expanded {
            left: 0;
        }

        .hamburger-btn {
            background: #f1f5f9;
            border: none;
            width: 42px;
            height: 42px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: #334155;
            font-size: 1.25rem;
            transition: all 0.2s ease;
        }

        .hamburger-btn:hover {
            background: var(--primary-light);
            color: var(--primary);
        }

        /* Main Content Wrapper */
        #main-content {
            margin-left: var(--sidebar-width);
            padding-top: calc(var(--topbar-height) + 1.75rem);
            padding-bottom: 3rem;
            padding-left: 2rem;
            padding-right: 2rem;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            min-height: 100vh;
        }

        #main-content.expanded {
            margin-left: 0;
        }

        /* Modern Card Styling */
        .card-custom {
            background: var(--card-bg);
            border-radius: var(--radius-card);
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-card);
            padding: 1.5rem;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            position: relative;
        }

        .card-custom:hover {
            box-shadow: var(--shadow-hover);
        }

        .stat-icon-box {
            width: 52px;
            height: 52px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
        }

        .badge-subtle {
            font-size: 0.75rem;
            font-weight: 700;
            padding: 0.35rem 0.65rem;
            border-radius: 20px;
        }

        /* Buttons & Forms */
        .btn-primary-custom {
            background: var(--primary);
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            padding: 0.6rem 1.25rem;
            transition: all 0.2s ease;
        }

        .btn-primary-custom:hover {
            background: var(--primary-hover);
            color: #ffffff;
            box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35);
        }

        .btn-danger-custom {
            background: #ef4444;
            color: #ffffff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            padding: 0.5rem 1rem;
            transition: all 0.2s ease;
        }

        .btn-danger-custom:hover {
            background: #dc2626;
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }

        .table-custom {
            border-collapse: separate;
            border-spacing: 0;
            width: 100%;
        }

        .table-custom th {
            background-color: #f8fafc;
            color: #475569;
            font-weight: 700;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 0.85rem 1.15rem;
            border-bottom: 2px solid var(--border-color);
        }

        .table-custom td {
            padding: 1.15rem;
            vertical-align: middle;
            border-bottom: 1px solid var(--border-color);
            background-color: #ffffff;
            font-size: 0.925rem;
        }

        .table-custom tr:last-child td {
            border-bottom: none;
        }

        .table-custom tbody tr:hover td {
            background-color: #f8fafc;
        }

        .user-avatar-sm {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #e2e8f0;
        }

        /* Backdrop for mobile drawer */
        .sidebar-backdrop {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(15, 23, 42, 0.4);
            backdrop-filter: blur(4px);
            z-index: 1035;
        }

        @media (max-width: 991.98px) {
            #sidebar {
                left: calc(-1 * var(--sidebar-width));
            }
            #sidebar.show-mobile {
                left: 0;
            }
            #topbar {
                left: 0 !important;
            }
            #main-content {
                margin-left: 0 !important;
                padding-left: 1rem;
                padding-right: 1rem;
            }
            .sidebar-backdrop.show {
                display: block;
            }
        }
    </style>
</head>
<body>

<!-- Mobile Backdrop -->
<div class="sidebar-backdrop" id="sidebarBackdrop" onclick="toggleSidebar()"></div>

<!-- Left Sidebar Menu -->
<aside id="sidebar">
    <div class="sidebar-brand">
        <div class="brand-logo">
            <i class="bi bi-shield-lock-fill"></i>
        </div>
        <div>
            <h6 class="mb-0 fw-bold" style="font-size: 1.1rem; color: #0f172a; letter-spacing: -0.02em;">Flikata Admin</h6>
            <small class="text-muted fw-semibold" style="font-size: 0.75rem;">Control Panel • Light</small>
        </div>
    </div>

    <div class="sidebar-nav">
        <div class="nav-label">মেইন মেনু (Main Menu)</div>
        <a href="index.php" class="sidebar-link <?= $currentPage === 'index.php' ? 'active' : '' ?>">
            <i class="bi bi-grid-fill"></i>
            <span>ড্যাশবোর্ড (Dashboard)</span>
        </a>
        <a href="users.php" class="sidebar-link <?= $currentPage === 'users.php' ? 'active' : '' ?>">
            <i class="bi bi-people-fill"></i>
            <span>ইউজার ম্যানেজমেন্ট</span>
        </a>
        <a href="posts.php" class="sidebar-link <?= $currentPage === 'posts.php' ? 'active' : '' ?>">
            <i class="bi bi-collection-play-fill"></i>
            <span>পোস্টসমূহ (Posts)</span>
        </a>

        <div class="nav-label mt-3">ভেরিফিকেশন ও ওয়ালেট</div>
        <a href="verification_packages.php" class="sidebar-link <?= $currentPage === 'verification_packages.php' ? 'active' : '' ?>">
            <i class="bi bi-patch-check-fill text-primary"></i>
            <span>ভেরিফিকেশন ব্যাচ</span>
        </a>
        <a href="verification_requests.php" class="sidebar-link <?= $currentPage === 'verification_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-person-check-fill text-info"></i>
                <span>ভেরিফিকেশন রিকোয়েস্ট</span>
            </div>
            <?php if ($pendingVerifCount > 0): ?>
                <span class="badge bg-warning text-dark rounded-pill ms-auto"><?= $pendingVerifCount ?></span>
            <?php endif; ?>
        </a>
        <a href="deposit_methods.php" class="sidebar-link <?= $currentPage === 'deposit_methods.php' ? 'active' : '' ?>">
            <i class="bi bi-wallet2 text-success"></i>
            <span>ডিপোজিট মেথড</span>
        </a>
        <a href="deposit_requests.php" class="sidebar-link <?= $currentPage === 'deposit_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-cash-stack text-success"></i>
                <span>ডিপোজিট রিকোয়েস্ট</span>
            </div>
            <?php if ($pendingDepositCount > 0): ?>
                <span class="badge bg-success text-white rounded-pill ms-auto"><?= $pendingDepositCount ?></span>
            <?php endif; ?>
        </a>
        <a href="withdraw_methods.php" class="sidebar-link <?= $currentPage === 'withdraw_methods.php' ? 'active' : '' ?>">
            <i class="bi bi-credit-card-2-front-fill text-warning"></i>
            <span>উইথড্র মেথড</span>
        </a>
        <a href="withdraw_requests.php" class="sidebar-link <?= $currentPage === 'withdraw_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-arrow-up-right-circle-fill text-warning"></i>
                <span>উইথড্র রিকোয়েস্ট</span>
            </div>
            <?php if ($pendingWithdrawCount > 0): ?>
                <span class="badge bg-warning text-dark rounded-pill ms-auto"><?= $pendingWithdrawCount ?></span>
            <?php endif; ?>
        </a>

        <div class="nav-label mt-3">মনিটাইজেশন ও পার্টনার</div>
        <a href="monetization_requests.php" class="sidebar-link <?= $currentPage === 'monetization_requests.php' ? 'active' : '' ?> d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-cash-coin text-success"></i>
                <span>মনিটাইজেশন রিকোয়েস্ট</span>
            </div>
            <?php if ($pendingMonetizationCount > 0): ?>
                <span class="badge bg-success text-white rounded-pill ms-auto"><?= $pendingMonetizationCount ?></span>
            <?php endif; ?>
        </a>

        <div class="nav-label mt-3">কমিউনিকেশন ও নোটিশ</div>
        <a href="notifications.php" class="sidebar-link <?= $currentPage === 'notifications.php' ? 'active' : '' ?>">
            <i class="bi bi-bell-fill"></i>
            <span>পেজ নোটিফিকেশন</span>
        </a>

        <div class="nav-label mt-3">সিস্টেম ও সেটিংস</div>
        <a href="settings.php" class="sidebar-link <?= $currentPage === 'settings.php' ? 'active' : '' ?>">
            <i class="bi bi-sliders"></i>
            <span>এডমিন সেটিংস</span>
        </a>
    </div>

    <div class="sidebar-footer">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-2">
                <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 38px; height: 38px; font-size: 0.9rem;">
                    A
                </div>
                <div>
                    <div class="fw-bold text-dark text-truncate" style="max-width: 120px; font-size: 0.875rem;"><?= htmlspecialchars($adminName) ?></div>
                    <small class="text-muted" style="font-size: 0.725rem;"><?= htmlspecialchars($adminEmail) ?></small>
                </div>
            </div>
            <a href="logout.php" class="btn btn-sm btn-outline-danger border-0 rounded-circle p-2" title="লগআউট" onclick="return confirm('আপনি কি নিশ্চিত লগআউট করতে চান?');">
                <i class="bi bi-box-arrow-right fs-5"></i>
            </a>
        </div>
    </div>
</aside>

<!-- Top Navbar Header with 3-Line Hamburger Menu -->
<header id="topbar">
    <div class="d-flex align-items-center gap-3">
        <!-- 3-Line Hamburger Toggle Button -->
        <button class="hamburger-btn" id="sidebarToggle" onclick="toggleSidebar()" aria-label="Toggle Navigation Menu" title="মেনু খুলুন/বন্ধ করুন">
            <i class="fa-solid fa-bars"></i>
        </button>
        <div>
            <h5 class="mb-0 fw-bold text-dark" style="font-size: 1.15rem;"><?= $pageHeading ?? 'এডমিন ড্যাশবোর্ড' ?></h5>
            <small class="text-muted d-none d-sm-inline"><?= $pageSubheading ?? 'Flikata সোশ্যাল অ্যাপ এডমিনিস্ট্রেশন' ?></small>
        </div>
    </div>

        <!-- Download Latest APK Button -->
        <a href="flikata_latest.apk" download class="btn btn-sm btn-primary rounded-pill px-3 py-1.5 d-flex align-items-center gap-2 fw-bold text-white shadow-sm" title="লেটেস্ট Android APK ডাউনলোড করুন">
            <i class="bi bi-android2 fs-6"></i>
            <span class="d-none d-md-inline">ডাউনলোড APK</span>
        </a>

        <!-- Download Full Backend & Admin ZIP Button -->
        <a href="flikata_backend_and_admin_cpanel.zip" download class="btn btn-sm btn-outline-primary rounded-pill px-3 py-1.5 d-flex align-items-center gap-2 fw-bold shadow-sm" title="সম্পূর্ণ ব্যাকএন্ড ও এডমিন প্যানেল জিপ ডাউনলোড করুন">
            <i class="bi bi-file-earmark-zip-fill fs-6 text-primary"></i>
            <span class="d-none d-md-inline">ব্যাকএন্ড ZIP</span>
        </a>

        <!-- Database Status Indicator -->
        <div class="badge rounded-pill bg-success-subtle text-success px-3 py-2 d-flex align-items-center gap-2 fw-semibold border border-success-subtle">
            <span class="spinner-grow spinner-grow-sm text-success" style="width: 8px; height: 8px;" role="status"></span>
            <span>ডাটাবেজ কানেক্টেড</span>
        </div>

        <!-- Quick Profile & Logout -->
        <div class="dropdown">
            <button class="btn btn-light rounded-pill px-3 py-1 d-flex align-items-center gap-2 border shadow-sm" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle fs-5 text-primary"></i>
                <span class="fw-bold text-dark d-none d-md-inline"><?= htmlspecialchars($adminName) ?></span>
                <i class="bi bi-chevron-down fs-7 text-muted"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3 p-2 mt-2">
                <li><span class="dropdown-header text-muted">লগইন করা আছে</span></li>
                <li><span class="dropdown-item-text fw-bold"><?= htmlspecialchars($adminEmail) ?></span></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item rounded-2" href="settings.php"><i class="bi bi-shield-lock me-2 text-primary"></i> পাসওয়ার্ড পরিবর্তন</a></li>
                <li><a class="dropdown-item rounded-2 text-danger" href="logout.php"><i class="bi bi-box-arrow-right me-2"></i> লগআউট করুন</a></li>
            </ul>
        </div>
    </div>
</header>

<!-- Main Workspace Container -->
<main id="main-content">
