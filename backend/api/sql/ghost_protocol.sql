-- 1. Add 'is_bot' flag to users table
ALTER TABLE users ADD COLUMN is_bot TINYINT DEFAULT 0;

-- 2. Matchmaking Queue Table (For real players waiting)
CREATE TABLE IF NOT EXISTS matchmaking_queue (
    user_id INT PRIMARY KEY,
    elo_rating INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Seed Ghost Users (Fake Players)
-- Randomized Elo between 800 (Beginner) and 1600 (Solid Player)

INSERT INTO users (username, password_hash, elo_rating, is_bot, total_matches, total_wins) VALUES
('CheckaMaster', 'BOT_NO_LOGIN', 1550, 1, 105, 60),
('QueenB', 'BOT_NO_LOGIN', 1420, 1, 80, 42),
('PawnStar', 'BOT_NO_LOGIN', 950, 1, 20, 8),
('GaryCheckasov', 'BOT_NO_LOGIN', 1600, 1, 200, 120),
('DeepBlueJr', 'BOT_NO_LOGIN', 1350, 1, 55, 30),
('WallBuilder', 'BOT_NO_LOGIN', 1100, 1, 40, 18),
('NoPassan', 'BOT_NO_LOGIN', 1250, 1, 60, 31),
('Glitch', 'BOT_NO_LOGIN', 850, 1, 15, 3),
('MatrixNeo', 'BOT_NO_LOGIN', 1480, 1, 90, 50),
('AlphaZeroOne', 'BOT_NO_LOGIN', 1590, 1, 150, 95),
('CasualDave', 'BOT_NO_LOGIN', 1050, 1, 35, 15),
('ProGamer123', 'BOT_NO_LOGIN', 1150, 1, 45, 22),
('ShadowNinja', 'BOT_NO_LOGIN', 1320, 1, 70, 38),
('FastFurious', 'BOT_NO_LOGIN', 1280, 1, 65, 33),
('ThinkingMan', 'BOT_NO_LOGIN', 1180, 1, 48, 24),
('StrategyBot', 'BOT_NO_LOGIN', 1450, 1, 85, 46),
('CheckMatey', 'BOT_NO_LOGIN', 980, 1, 25, 10),
('RookNRoll', 'BOT_NO_LOGIN', 1220, 1, 58, 29),
('KnightRider', 'BOT_NO_LOGIN', 1380, 1, 75, 40),
('BishopTakes', 'BOT_NO_LOGIN', 1120, 1, 42, 20),
('KingSlayer', 'BOT_NO_LOGIN', 1520, 1, 110, 65),
('QueenGambit', 'BOT_NO_LOGIN', 1460, 1, 95, 52),
('EndgameWiz', 'BOT_NO_LOGIN', 1580, 1, 140, 85),
('OpeningBook', 'BOT_NO_LOGIN', 1290, 1, 68, 35),
('MidgameMaster', 'BOT_NO_LOGIN', 1360, 1, 78, 41),
('AlexChecka', 'BOT_NO_LOGIN', 1020, 1, 12, 5),
('SarahPlays', 'BOT_NO_LOGIN', 1190, 1, 50, 25),
('MikeMoving', 'BOT_NO_LOGIN', 920, 1, 18, 6),
('LisaLogic', 'BOT_NO_LOGIN', 1410, 1, 82, 44),
('TomTactics', 'BOT_NO_LOGIN', 1260, 1, 62, 31),
('EmmaEngine', 'BOT_NO_LOGIN', 1540, 1, 108, 62),
('DavidDefense', 'BOT_NO_LOGIN', 1080, 1, 38, 17),
('ChrisClutch', 'BOT_NO_LOGIN', 1310, 1, 72, 38),
('AnnaAttack', 'BOT_NO_LOGIN', 1490, 1, 98, 55),
('SteveSpeed', 'BOT_NO_LOGIN', 890, 1, 14, 4),
('KellyKing', 'BOT_NO_LOGIN', 1340, 1, 74, 39),
('BrianBoard', 'BOT_NO_LOGIN', 1160, 1, 46, 23),
('RachelRank', 'BOT_NO_LOGIN', 1440, 1, 88, 48),
('KevinKnight', 'BOT_NO_LOGIN', 1230, 1, 54, 27),
('LauraLegend', 'BOT_NO_LOGIN', 1570, 1, 130, 80),
('TopGamerVN', 'BOT_NO_LOGIN', 1300, 1, 66, 33),
('USAChecka', 'BOT_NO_LOGIN', 1140, 1, 44, 21),
('EuroMaster', 'BOT_NO_LOGIN', 1470, 1, 92, 51),
('AsianTiger', 'BOT_NO_LOGIN', 1560, 1, 120, 70),
('BrazilianStar', 'BOT_NO_LOGIN', 1210, 1, 56, 28),
('RussianBear', 'BOT_NO_LOGIN', 1390, 1, 76, 41),
('IndianGrand', 'BOT_NO_LOGIN', 1510, 1, 102, 58),
('AussieRoo', 'BOT_NO_LOGIN', 990, 1, 22, 9),
('KiwiFly', 'BOT_NO_LOGIN', 1090, 1, 32, 14),
('ArcticFox', 'BOT_NO_LOGIN', 1430, 1, 84, 45);
