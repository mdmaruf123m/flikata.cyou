<?php
/**
 * Database Diagnostic Tool
 * Visit in browser: https://yourdomain.com/backend/db_test.php
 */

header('Content-Type: text/html; charset=utf-8');
require_once __DIR__ . '/config/db.php';

?>
<!DOCTYPE html>
<html>
<head>
    <title>Flikata Database Connection Diagnostic</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #0b1118; color: #fff; padding: 30px; }
        .card { background: #161f2c; border-radius: 12px; padding: 24px; max-width: 650px; margin: 0 auto; box-shadow: 0 4px 20px rgba(0,0,0,0.5); }
        h2 { color: #00c853; margin-top: 0; }
        .error { color: #ff5252; }
        .success { color: #00e676; }
        .info { background: #202d3e; padding: 12px 16px; border-radius: 8px; margin: 12px 0; font-family: monospace; font-size: 13px; }
        ul { padding-left: 20px; line-height: 1.6; }
    </style>
</head>
<body>
<div class="card">
    <h2>Flikata Database Connection Test</h2>
    <?php
    try {
        $pdo = getDB();
        echo "<h3 class='success'>✅ Database Connected Successfully!</h3>";
        
        $tables = $pdo->query("SHOW TABLES")->fetchAll(PDO::FETCH_COLUMN);
        echo "<p>Connected Database contains <strong>" . count($tables) . "</strong> tables:</p>";
        echo "<div class='info'>" . implode(', ', $tables) . "</div>";
    } catch (Exception $e) {
        echo "<h3 class='error'>❌ Database Connection Failed</h3>";
        echo "<div class='info error'>" . htmlspecialchars($e->getMessage()) . "</div>";
    }
    ?>
    <hr style="border-color: #2a394d; margin: 20px 0;">
    <p><strong>Configured Settings in db.php:</strong></p>
    <div class="info">
        DB_HOST: <?php echo DB_HOST; ?><br>
        DB_NAME: <?php echo DB_NAME; ?><br>
        DB_USER: <?php echo DB_USER; ?><br>
        DB_PASS: <?php echo htmlspecialchars(DB_PASS); ?>
    </div>
</div>
</body>
</html>
