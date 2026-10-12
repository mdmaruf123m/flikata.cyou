<?php
/**
 * Admin Database Connection & Auto-Setup
 * Flikata Admin Control Panel
 */

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Locate backend config/db.php
$backendDbPath = null;
$possiblePaths = [
    __DIR__ . '/../../backend/config/db.php',
    __DIR__ . '/../backend/config/db.php',
    $_SERVER['DOCUMENT_ROOT'] . '/backend/config/db.php',
    dirname(__DIR__, 2) . '/backend/config/db.php'
];

foreach ($possiblePaths as $path) {
    if (file_exists($path)) {
        $backendDbPath = $path;
        break;
    }
}

if ($backendDbPath) {
    require_once $backendDbPath;
} else {
    // Fallback direct credentials if backend folder moved
    if (!defined('DB_HOST')) define('DB_HOST', 'localhost');
    if (!defined('DB_NAME')) define('DB_NAME', 'flikata_bd');
    if (!defined('DB_USER')) define('DB_USER', 'flikata_bd');
    if (!defined('DB_PASS')) define('DB_PASS', 'flikata_bd');
    if (!defined('DB_CHARSET')) define('DB_CHARSET', 'utf8mb4');

    function getDB() {
        static $pdo = null;
        if ($pdo === null) {
            $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ];
            try {
                $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
            } catch (PDOException $e) {
                die("Database connection failed: " . htmlspecialchars($e->getMessage()));
            }
        }
        return $pdo;
    }
}

$pdo = getDB();

// Ensure admins table exists
$pdo->exec("
    CREATE TABLE IF NOT EXISTS `admins` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `name` VARCHAR(150) NOT NULL DEFAULT 'Super Admin',
      `email` VARCHAR(150) NOT NULL UNIQUE,
      `password_hash` VARCHAR(255) NOT NULL,
      `role` VARCHAR(50) DEFAULT 'admin',
      `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
      `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
");

// Ensure default admin exists: admin@gmail.com / 11223344
$chkAdmin = $pdo->prepare("SELECT id, password_hash FROM `admins` WHERE email = ? LIMIT 1");
$chkAdmin->execute(['admin@gmail.com']);
$existingAdmin = $chkAdmin->fetch();

if (!$existingAdmin) {
    $defaultPassHash = password_hash('11223344', PASSWORD_BCRYPT);
    $ins = $pdo->prepare("INSERT INTO `admins` (`name`, `email`, `password_hash`, `role`) VALUES (?, ?, ?, ?)");
    $ins->execute(['Super Admin', 'admin@gmail.com', $defaultPassHash, 'admin']);
}

// Ensure users table has is_banned column
try {
    $colCheck = $pdo->query("SHOW COLUMNS FROM `users` LIKE 'is_banned'");
    if ($colCheck->rowCount() == 0) {
        $pdo->exec("ALTER TABLE `users` ADD COLUMN `is_banned` TINYINT(1) DEFAULT 0");
    }
} catch (Exception $e) {
    // Ignore if column already exists
}

// Helper function to check admin session
function checkAdminAuth() {
    if (empty($_SESSION['admin_logged_in']) || empty($_SESSION['admin_id'])) {
        header('Location: login.php');
        exit;
    }
}
