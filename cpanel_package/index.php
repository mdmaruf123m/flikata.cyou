<?php
/**
 * Flikata Social App - Server Entry Point
 * Auto-redirects to Admin Control Panel
 */
if (file_exists(__DIR__ . '/admin/index.php')) {
    header("Location: admin/");
    exit;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Flikata Social Server</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; background: #f8fafc; margin: 0; }
        .box { background: white; padding: 2.5rem; border-radius: 1.25rem; box-shadow: 0 10px 30px rgba(0,0,0,0.06); text-align: center; max-width: 450px; width: 90%; }
        h2 { margin-top: 0; color: #0f172a; font-size: 1.5rem; }
        p { color: #64748b; font-size: 0.95rem; line-height: 1.5; }
        a { display: inline-block; margin-top: 1rem; padding: 12px 28px; background: #4f46e5; color: white; border-radius: 50px; text-decoration: none; font-weight: bold; box-shadow: 0 4px 14px rgba(79, 70, 229, 0.4); }
        a:hover { background: #4338ca; }
    </style>
</head>
<body>
    <div class="box">
        <h2>Flikata Server Active</h2>
        <p>Your backend APIs and Admin Panel are active and running.</p>
        <a href="admin/">Open Admin Panel</a>
    </div>
</body>
</html>
