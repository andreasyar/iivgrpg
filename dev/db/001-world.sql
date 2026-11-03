-- Новый тестовый мир для MMORPG; архивные аккаунты и дампы не используются.
CREATE TABLE accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    login VARCHAR(16) NOT NULL UNIQUE,
    password VARCHAR(48) NOT NULL,
    createAt DATETIME NOT NULL,
    registerIP VARCHAR(48) NOT NULL
);
CREATE TABLE characters (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    name VARCHAR(16) NOT NULL,
    map_id INT NOT NULL,
    posX INT NOT NULL,
    posY INT NOT NULL
);
CREATE TABLE groundtiles (
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    passability TINYINT NOT NULL
);
CREATE TABLE maps (
    id INT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL
);
CREATE TABLE maptiles (
    map_id INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    tile_id INT NOT NULL,
    PRIMARY KEY (map_id, x, y)
);
CREATE TABLE objects (
    id INT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL
);
CREATE TABLE objectsubtiles (
    object_id INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    src VARCHAR(32) NOT NULL,
    passability TINYINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    PRIMARY KEY (object_id, x, y)
);
CREATE TABLE mapobjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    map_id INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    object_id INT NOT NULL,
    UNIQUE (map_id, x, y)
);
INSERT INTO groundtiles VALUES (1, 'grass', 1);
INSERT INTO maps VALUES (1, 'Test world', 40, 30), (2, 'Test room', 12, 12);
INSERT INTO objects VALUES
    (1, 'tree', 'Дерево: непроходимая клетка', 1, 1),
    (2, 'flower', 'Цветок: проходимая клетка', 1, 1),
    (3, 'pyramid', 'Пирамида с проходом в центре', 3, 2);
INSERT INTO objectsubtiles VALUES
    (1, 0, 0, 'tree', 0, ''),
    (2, 0, 0, 'flower', 1, ''),
    (3, 1, 0, 'piramid0_1', 0, ''),
    (3, 0, 1, 'piramid1_0', 0, ''),
    (3, 1, 1, 'piramid1_1', 2, ''),
    (3, 2, 1, 'piramid1_2', 0, '');
-- Преграда рядом со стартом позволяет проверить обход, цветы и пирамида — слои карты.
INSERT INTO mapobjects (map_id, x, y, object_id) VALUES
    (1, 3, 1, 1), (1, 3, 2, 1), (1, 3, 3, 1), (1, 3, 4, 1),
    (1, 1, 4, 2), (1, 2, 5, 2), (1, 5, 6, 2), (1, 8, 4, 3),
    (1, 12, 3, 1), (1, 13, 4, 1), (1, 15, 8, 1), (1, 18, 12, 3),
    (2, 4, 3, 1), (2, 5, 3, 1), (2, 6, 6, 2);
