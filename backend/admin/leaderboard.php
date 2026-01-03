<?php
session_start();
require_once '../config/env.php';
require_once '../config/db.php';
require_once '../api/elo.php'; // Include Elo logic for Rank Titles

if (!isset($_SESSION['admin_id'])) {
    header("Location: index.php");
    exit;
}

$pdo = get_db_connection();

// Fetch Leaderboard Match Data
$limit = 100;
$stmt = $pdo->prepare("SELECT id, display_name, avatar_url, elo_rating, total_wins, total_matches, xp, level, created_at FROM users ORDER BY elo_rating DESC LIMIT ?");
$stmt->bindValue(1, $limit, PDO::PARAM_INT);
$stmt->execute();
$players = $stmt->fetchAll(PDO::FETCH_ASSOC);

?>
<!DOCTYPE html>
<html lang="en" data-bs-theme="dark">

<head>
    <meta charset="UTF-8">
    <title>Checka Admin - Leaderboard</title>
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

        .navbar .container-fluid {
            max-width: 1320px;
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

        .avatar-img {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            object-fit: cover;
            border: 1px solid #444;
            background-color: #333;
        }

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
                        <a class="nav-link" href="dashboard.php">Dashboard</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="leaderboard.php">Leaderboard</a>
                    </li>
                </ul>
                <div class="d-flex align-items-center">
                    <span class="navbar-text me-3 text-secondary">
                        <?php echo htmlspecialchars($_SESSION['admin_username']); ?>
                    </span>
                    <a href="logout.php" class="btn btn-outline-danger btn-sm">Logout</a>
                </div>
            </div>
        </div>
    </nav>

    <div class="container mt-4">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3>Global Leaderboard <span class="text-muted fs-6">(Top 100)</span></h3>
        </div>

        <div class="card mb-4">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle">
                        <thead>
                            <tr>
                                <th class="ps-3" style="width: 60px;">#</th>
                                <th style="width: 60px;">Avatar</th>
                                <th>Player</th>
                                <th>Rank Title</th>
                                <th>ELO</th>
                                <th>Level</th>
                                <th>Win Rate</th>
                                <th>Games</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php
                            $rank = 1;
                            foreach ($players as $p):
                                $rankTitle = EloRating::getRankTitle($p['elo_rating']);
                                $winRate = ($p['total_matches'] > 0) ? round(($p['total_wins'] / $p['total_matches']) * 100, 1) . '%' : '-';
                                $avatar = $p['avatar_url'] ?: 'https://ui-avatars.com/api/?name=' . urlencode($p['display_name']) . '&background=random';
                                ?>
                                <tr>
                                    <td class="ps-3 fw-bold text-secondary">
                                        <?php echo $rank++; ?>
                                    </td>
                                    <td>
                                        <img src="<?php echo htmlspecialchars($avatar); ?>" class="avatar-img" alt="Avatar">
                                    </td>
                                    <td class="fw-bold">
                                        <?php echo htmlspecialchars($p['display_name']); ?>
                                    </td>
                                    <td>
                                        <span class="badge bg-dark border border-secondary text-light">
                                            <?php echo $rankTitle; ?>
                                        </span>
                                    </td>
                                    <td class="text-warning fw-bold">
                                        <?php echo $p['elo_rating']; ?>
                                    </td>
                                    <td>
                                        <?php echo $p['level']; ?> <span class="text-muted small">test(
                                            <?php echo $p['xp']; ?> XP)
                                        </span>
                                    </td>
                                    <td>
                                        <?php echo $winRate; ?>
                                    </td>
                                    <td>
                                        <?php echo $p['total_matches']; ?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

</body>

</html>