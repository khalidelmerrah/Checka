-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jan 03, 2026 at 10:38 AM
-- Server version: 10.6.24-MariaDB
-- PHP Version: 8.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `reyagency_chekadmin`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`id`, `username`, `password_hash`, `created_at`) VALUES
(1, 'admin_9a505ab8', '$2y$10$wbhzIlzFkxcpgdigD45euOSsqkUcOWr6x7zlEJJaFao1DVep3U0Q6', '2026-01-02 17:15:17');

-- --------------------------------------------------------

--
-- Table structure for table `app_settings`
--

CREATE TABLE `app_settings` (
  `setting_key` varchar(50) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `app_settings`
--

INSERT INTO `app_settings` (`setting_key`, `setting_value`, `description`) VALUES
('maintenance_mode', 'false', 'Set to true to block app access'),
('min_version_code', '1', 'Force update if app version is lower');

-- --------------------------------------------------------

--
-- Table structure for table `matches`
--

CREATE TABLE `matches` (
  `id` int(11) NOT NULL,
  `game_mode` varchar(50) NOT NULL,
  `difficulty` varchar(20) DEFAULT NULL,
  `player1_id` int(11) DEFAULT NULL,
  `player2_id` int(11) DEFAULT NULL,
  `winner_id` int(11) DEFAULT NULL,
  `duration_seconds` int(11) DEFAULT NULL,
  `played_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `current_turn_player_id` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'waiting' COMMENT 'waiting, active, finished',
  `last_move_at` timestamp(3) NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `matchmaking_queue`
--

CREATE TABLE `matchmaking_queue` (
  `user_id` int(11) NOT NULL,
  `elo_rating` int(11) NOT NULL,
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `moves`
--

CREATE TABLE `moves` (
  `id` bigint(20) NOT NULL,
  `match_id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `move_number` int(11) NOT NULL,
  `move_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'Coordinates: {from: "e2", to: "e4", type: "move|wall", wall_orientation: "h|v"}' CHECK (json_valid(`move_data`)),
  `created_at` timestamp(3) NOT NULL DEFAULT current_timestamp(3)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `google_subject_id` varchar(255) DEFAULT NULL COMMENT 'The unique ID from Google Auth',
  `email` varchar(255) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `avatar_url` text DEFAULT NULL,
  `is_banned` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL,
  `xp` int(11) DEFAULT 0,
  `level` int(11) DEFAULT 1,
  `best_turns` int(11) DEFAULT 999,
  `is_bot` tinyint(4) DEFAULT 0,
  `elo_rating` int(11) DEFAULT 1200,
  `total_wins` int(11) DEFAULT 0,
  `total_matches` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `google_subject_id`, `email`, `display_name`, `avatar_url`, `is_banned`, `created_at`, `last_login`, `xp`, `level`, `best_turns`, `is_bot`, `elo_rating`, `total_wins`, `total_matches`) VALUES
(1, NULL, NULL, 'CheckaMaster', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 5000, 10, 999, 1, 1550, 60, 105),
(2, NULL, NULL, 'QueenB', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Cat%20Face.png', 0, '2026-01-02 21:44:24', NULL, 4000, 8, 999, 1, 1420, 42, 80),
(3, NULL, NULL, 'PawnStar', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 500, 2, 999, 1, 950, 8, 20),
(4, NULL, NULL, 'GaryCheckasov', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Alien%20Monster.png', 0, '2026-01-02 21:44:24', NULL, 8000, 15, 999, 1, 1600, 120, 200),
(5, NULL, NULL, 'DeepBlueJr', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 3000, 6, 999, 1, 1350, 30, 55),
(6, NULL, NULL, 'WallBuilder', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Panda.png', 0, '2026-01-02 21:44:24', NULL, 1500, 3, 999, 1, 1100, 18, 40),
(7, NULL, NULL, 'NoPassan', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 0, '2026-01-02 21:44:24', NULL, 2500, 5, 999, 1, 1250, 31, 60),
(8, NULL, NULL, 'Glitch', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Bear.png', 0, '2026-01-02 21:44:24', NULL, 200, 1, 999, 1, 850, 3, 15),
(9, NULL, NULL, 'MatrixNeo', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 4500, 9, 999, 1, 1480, 50, 90),
(10, NULL, NULL, 'AlphaZeroOne', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 7500, 14, 999, 1, 1590, 95, 150),
(11, NULL, NULL, 'CasualDave', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 1200, 3, 999, 1, 1050, 15, 35),
(12, NULL, NULL, 'ProGamer123', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 1800, 4, 999, 1, 1150, 22, 45),
(13, NULL, NULL, 'ShadowNinja', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 2800, 6, 999, 1, 1320, 38, 70),
(14, NULL, NULL, 'FastFurious', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 0, '2026-01-02 21:44:24', NULL, 2600, 5, 999, 1, 1280, 33, 65),
(15, NULL, NULL, 'ThinkingMan', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Alien%20Monster.png', 0, '2026-01-02 21:44:24', NULL, 2000, 4, 999, 1, 1180, 24, 48),
(16, NULL, NULL, 'StrategyBot', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Cat%20Face.png', 0, '2026-01-02 21:44:24', NULL, 4200, 8, 999, 1, 1450, 46, 85),
(17, NULL, NULL, 'CheckMatey', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Unicorn.png', 0, '2026-01-02 21:44:24', NULL, 800, 2, 999, 1, 980, 10, 25),
(18, NULL, NULL, 'RookNRoll', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 2200, 4, 999, 1, 1220, 29, 58),
(19, NULL, NULL, 'KnightRider', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 3200, 6, 999, 1, 1380, 40, 75),
(20, NULL, NULL, 'BishopTakes', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Robot.png', 0, '2026-01-02 21:44:24', NULL, 1600, 3, 999, 1, 1120, 20, 42),
(21, NULL, NULL, 'KingSlayer', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Unicorn.png', 0, '2026-01-02 21:44:24', NULL, 6000, 12, 999, 1, 1520, 65, 110),
(22, NULL, NULL, 'QueenGambit', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Fox.png', 0, '2026-01-02 21:44:24', NULL, 4800, 9, 999, 1, 1460, 52, 95),
(23, NULL, NULL, 'EndgameWiz', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 7000, 13, 999, 1, 1580, 85, 140),
(24, NULL, NULL, 'OpeningBook', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Chicken.png', 0, '2026-01-02 21:44:24', NULL, 2700, 5, 999, 1, 1290, 35, 68),
(25, NULL, NULL, 'MidgameMaster', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Robot.png', 0, '2026-01-02 21:44:24', NULL, 3100, 6, 999, 1, 1360, 41, 78),
(26, NULL, NULL, 'AlexChecka', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 0, '2026-01-02 21:44:24', NULL, 300, 1, 999, 1, 1020, 5, 12),
(27, NULL, NULL, 'SarahPlays', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Robot.png', 0, '2026-01-02 21:44:24', NULL, 1900, 4, 999, 1, 1190, 25, 50),
(28, NULL, NULL, 'MikeMoving', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 400, 1, 999, 1, 920, 6, 18),
(29, NULL, NULL, 'LisaLogic', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 3800, 7, 999, 1, 1410, 44, 82),
(30, NULL, NULL, 'TomTactics', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Unicorn.png', 0, '2026-01-02 21:44:24', NULL, 2400, 5, 999, 1, 1260, 31, 62),
(31, NULL, NULL, 'EmmaEngine', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Alien%20Monster.png', 0, '2026-01-02 21:44:24', NULL, 6500, 11, 999, 1, 1540, 62, 108),
(32, NULL, NULL, 'DavidDefense', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Chicken.png', 0, '2026-01-02 21:44:24', NULL, 1300, 3, 999, 1, 1080, 17, 38),
(33, NULL, NULL, 'ChrisClutch', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 2900, 6, 999, 1, 1310, 38, 72),
(34, NULL, NULL, 'AnnaAttack', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 5500, 10, 999, 1, 1490, 55, 98),
(35, NULL, NULL, 'SteveSpeed', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 0, '2026-01-02 21:44:24', NULL, 350, 1, 999, 1, 890, 4, 14),
(36, NULL, NULL, 'KellyKing', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 3000, 6, 999, 1, 1340, 39, 74),
(37, NULL, NULL, 'BrianBoard', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Bear.png', 0, '2026-01-02 21:44:24', NULL, 1700, 3, 999, 1, 1160, 23, 46),
(38, NULL, NULL, 'RachelRank', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Fox.png', 0, '2026-01-02 21:44:24', NULL, 4100, 8, 999, 1, 1440, 48, 88),
(39, NULL, NULL, 'KevinKnight', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 2300, 5, 999, 1, 1230, 27, 54),
(40, NULL, NULL, 'LauraLegend', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 6800, 13, 999, 1, 1570, 80, 130),
(41, NULL, NULL, 'TopGamerVN', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 2800, 6, 999, 1, 1300, 33, 66),
(42, NULL, NULL, 'USAChecka', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Skull.png', 0, '2026-01-02 21:44:24', NULL, 1750, 3, 999, 1, 1140, 21, 44),
(43, NULL, NULL, 'EuroMaster', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Panda.png', 0, '2026-01-02 21:44:24', NULL, 4300, 9, 999, 1, 1470, 51, 92),
(44, NULL, NULL, 'AsianTiger', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Panda.png', 0, '2026-01-02 21:44:24', NULL, 6200, 12, 999, 1, 1560, 70, 120),
(45, NULL, NULL, 'BrazilianStar', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Cat%20Face.png', 0, '2026-01-02 21:44:24', NULL, 2100, 4, 999, 1, 1210, 28, 56),
(46, NULL, NULL, 'RussianBear', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Robot.png', 0, '2026-01-02 21:44:24', NULL, 3300, 7, 999, 1, 1390, 41, 76),
(47, NULL, NULL, 'IndianGrand', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Lion.png', 0, '2026-01-02 21:44:24', NULL, 5800, 11, 999, 1, 1510, 58, 102),
(48, NULL, NULL, 'AussieRoo', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Dragon.png', 0, '2026-01-02 21:44:24', NULL, 600, 2, 999, 1, 990, 9, 22),
(49, NULL, NULL, 'KiwiFly', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Ghost.png', 0, '2026-01-02 21:44:24', NULL, 1400, 3, 999, 1, 1090, 14, 32),
(50, NULL, NULL, 'ArcticFox', 'https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Animals/Unicorn.png', 0, '2026-01-02 21:44:24', NULL, 3900, 8, 999, 1, 1430, 45, 84),
(51, 'bot_f7bf11ee4f8d4ee6a7059ced864dfda5', 'bot@checka.top', 'CheckaMaster', NULL, 0, '2026-01-03 01:38:16', NULL, 669, 1, 999, 1, 1397, 0, 0),
(52, 'bot_61ffcd2882cb9b8b698ef08f1e32929d', 'bot@checka.top', 'RookieOne', NULL, 0, '2026-01-03 01:38:16', NULL, 229, 1, 999, 1, 1230, 0, 0),
(53, 'bot_ebaad41a814ddbf8c386dc27c721f1bf', 'bot@checka.top', 'AlphaZero_v1', NULL, 0, '2026-01-03 01:38:16', NULL, 888, 1, 999, 1, 1210, 0, 0),
(54, 'bot_c58073813cffd4b12e2e350e0fb64c6d', 'bot@checka.top', 'DeepBlue_Mini', NULL, 0, '2026-01-03 01:38:16', NULL, 1898, 2, 999, 1, 837, 0, 0),
(55, 'bot_cad2707b3083f583e48608d3b9b01ca4', 'bot@checka.top', 'KasparovAI', NULL, 0, '2026-01-03 01:38:16', NULL, 4357, 5, 999, 1, 1534, 0, 0),
(56, 'bot_db33dae9f7cd8fd047c84f4d12367d98', 'bot@checka.top', 'QueenGambit', NULL, 0, '2026-01-03 01:38:16', NULL, 4114, 5, 999, 1, 828, 0, 0),
(57, 'bot_c63811969328df2f474e8ee657103626', 'bot@checka.top', 'PawnStar', NULL, 0, '2026-01-03 01:38:16', NULL, 2852, 3, 999, 1, 984, 0, 0),
(58, 'bot_720ee93e1a1b437864ef3686d62d09a6', 'bot@checka.top', 'KnightRider', NULL, 0, '2026-01-03 01:38:16', NULL, 1855, 2, 999, 1, 1135, 0, 0),
(59, 'bot_e760c9574306cc094e1d2540a403dc18', 'bot@checka.top', 'BishopTakesQueen', NULL, 0, '2026-01-03 01:38:16', NULL, 4517, 5, 999, 1, 1128, 0, 0),
(60, 'bot_de0d675f83d101636789f9a8705c0368', 'bot@checka.top', 'EndgamePro', NULL, 0, '2026-01-03 01:38:16', NULL, 2655, 3, 999, 1, 1467, 0, 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `app_settings`
--
ALTER TABLE `app_settings`
  ADD PRIMARY KEY (`setting_key`);

--
-- Indexes for table `matches`
--
ALTER TABLE `matches`
  ADD PRIMARY KEY (`id`),
  ADD KEY `player1_id` (`player1_id`),
  ADD KEY `player2_id` (`player2_id`),
  ADD KEY `winner_id` (`winner_id`),
  ADD KEY `idx_matches_status_player` (`status`,`player1_id`,`player2_id`);

--
-- Indexes for table `matchmaking_queue`
--
ALTER TABLE `matchmaking_queue`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `moves`
--
ALTER TABLE `moves`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_moves_polling` (`match_id`,`move_number`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `google_subject_id` (`google_subject_id`),
  ADD KEY `idx_users_elo` (`elo_rating`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `matches`
--
ALTER TABLE `matches`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `moves`
--
ALTER TABLE `moves`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `matches`
--
ALTER TABLE `matches`
  ADD CONSTRAINT `matches_ibfk_1` FOREIGN KEY (`player1_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `matches_ibfk_2` FOREIGN KEY (`player2_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `matches_ibfk_3` FOREIGN KEY (`winner_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `moves`
--
ALTER TABLE `moves`
  ADD CONSTRAINT `moves_ibfk_1` FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
