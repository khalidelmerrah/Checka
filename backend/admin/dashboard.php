<?php
session_start();
require_once '../config/db.php';

if (!isset($_SESSION['admin_id'])) {
    header("Location: index.php");
    exit;
}

$pdo = get_db_connection();

// Fetch Stats
$user_count = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
$match_count = $pdo->query("SELECT COUNT(*) FROM matches")->fetchColumn();
$latest_users = $pdo->query("SELECT * FROM users ORDER BY created_at DESC LIMIT 5")->fetchAll();

?>
<!DOCTYPE html>
<html lang="en" data-bs-theme="dark">

<head>
    <meta charset="UTF-8">
    <title>Checka Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #121212;
            color: #ffffff;
        }

        .card {
            background-color: #1e1e1e;
            border: 1px solid #333;
        }

        .card-header {
            border-bottom: 1px solid #333;
            background-color: rgba(255, 255, 255, 0.05);
            font-weight: 600;
        }

        .table {
            color: #ddd;
            margin-bottom: 0;
            --bs-table-bg: transparent;
            --bs-table-border-color: #333;
            --bs-table-striped-bg: rgba(255, 255, 255, 0.03);
        }

        /* Navbar Styling */
        .navbar {
            border-bottom: 1px solid #333;
            background-color: #1e1e1e !important;
        }

        /* ALIGNMENT FIX: Restrict navbar content width to match body container */
        .navbar .container-fluid {
            max-width: 1320px;
            /* Standard Bootstrap XXL container max-width */
            margin: 0 auto;
            padding-left: 0.75rem;
            padding-right: 0.75rem;
        }

        .btn-outline-danger {
            border-color: #dc3545;
            color: #dc3545;
        }

        .btn-outline-danger:hover {
            background-color: #dc3545;
            color: white;
        }

        /* Ensure main content is also centered and bounded */
        body>.container {
            max-width: 1320px;
        }
    </style>
</head>

<body>

    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container-fluid">
            <a class="navbar-brand fw-bold" href="dashboard.php">
                <span class="text-primary">Checka</span> Admin
            </a>
            <div class="collapse navbar-collapse">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="dashboard.php">Dashboard</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="leaderboard.php">Leaderboard</a>
                    </li>
                </ul>
                <div class="d-flex align-items-center">
                    <span class="navbar-text me-3 text-secondary">
                        Welcome, <?php echo htmlspecialchars($_SESSION['admin_username']); ?>
                    </span>
                    <a href="logout.php" class="btn btn-outline-danger btn-sm">Logout</a>
                </div>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <!-- Stats Cards -->
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card bg-dark border-primary mb-3">
                    <div class="card-body">
                        <h6 class="card-subtitle mb-2 text-primary">Total Users</h6>
                        <h2 class="card-title text-white"><?php echo $user_count; ?></h2>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card bg-dark border-success mb-3">
                    <div class="card-body">
                        <h6 class="card-subtitle mb-2 text-success">Total Matches</h6>
                        <h2 class="card-title text-white"><?php echo $match_count; ?></h2>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Users -->
        <div class="card mb-4">
            <div class="card-header">Recent Users</div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle">
                        <thead>
                            <tr>
                                <th class="ps-3">ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Joined</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($latest_users as $user): ?>
                                <tr>
                                    <td class="ps-3">
                                        <?php echo $user['id']; ?>
                                    </td>
                                    <td>
                                        <?php echo htmlspecialchars($user['display_name'] ?? 'Unknown'); ?>
                                    </td>
                                    <td>
                                        <?php echo htmlspecialchars($user['email'] ?? '-'); ?>
                                    </td>
                                    <td>
                                        <?php echo $user['created_at']; ?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Settings Placeholder -->
        <div class="card">
            <div class="card-header">Quick Settings</div>
            <div class="card-body">
                <p class="mb-0">Maintenance Mode: <span class="badge bg-secondary">Off</span> (To be implemented)</p>
            </div>
        </div>
    </div>

</body>

</html>