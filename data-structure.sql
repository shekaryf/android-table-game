-- ==========================================
-- 1. ساخت جداول (Tables Creation)
-- ==========================================

-- جدول دسته‌بندی‌ها
CREATE TABLE IF NOT EXISTS Categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    is_unlocked INTEGER DEFAULT 0 -- 0: قفل، 1: باز
);

-- جدول مراحل
CREATE TABLE IF NOT EXISTS Levels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER,
    level_number INTEGER NOT NULL,
    grid_rows INTEGER NOT NULL,
    grid_cols INTEGER NOT NULL,
    grid_data TEXT NOT NULL,  -- آرایه دو بعدی از حروف صحیح (JSON)
    clues_data TEXT NOT NULL, -- اطلاعات خانه‌های سوال و فلش‌ها (JSON)
    is_completed INTEGER DEFAULT 0,
    stars INTEGER DEFAULT 0,
    FOREIGN KEY(category_id) REFERENCES Categories(id)
);

-- جدول پروفایل/وضعیت کاربر
CREATE TABLE IF NOT EXISTS User (
    id INTEGER PRIMARY KEY CHECK (id = 1), -- فقط یک ردیف برای کاربر لوکال
    username TEXT DEFAULT 'بازیکن',
    coins INTEGER DEFAULT 100, -- سکه اولیه
    total_score INTEGER DEFAULT 0,
    current_category_id INTEGER DEFAULT 1,
    current_level_id INTEGER DEFAULT 1
);

-- جدول چالش‌های روزانه
CREATE TABLE IF NOT EXISTS DailyPuzzles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    puzzle_date TEXT NOT NULL UNIQUE, -- فرمت YYYY-MM-DD
    grid_data TEXT NOT NULL,
    clues_data TEXT NOT NULL,
    reward_coins INTEGER DEFAULT 50,
    status INTEGER DEFAULT 0 -- 0: حل نشده، 1: حل شده
);

-- جدول دستاوردها و ماموریت‌ها
CREATE TABLE IF NOT EXISTS Achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    target_value INTEGER NOT NULL,
    current_value INTEGER DEFAULT 0,
    reward_coins INTEGER NOT NULL,
    is_claimed INTEGER DEFAULT 0 -- 0: دریافت نشده، 1: دریافت شده
);

-- جدول تاریخچه تراکنش‌ها (فروشگاه)
CREATE TABLE IF NOT EXISTS Transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku_id TEXT NOT NULL,
    purchase_token TEXT NOT NULL,
    purchase_date INTEGER NOT NULL, -- Timestamp
    amount INTEGER NOT NULL
);


-- ==========================================
-- 2. درج داده‌های اولیه (Insert Initial Data)
-- ==========================================

-- درج دسته‌بندی‌های نمونه
INSERT INTO Categories (name, description, is_unlocked) VALUES
('حیوانات', 'اسامی حیوانات مختلف', 1),
('میوه‌ها', 'انواع میوه‌های خوشمزه', 0),
('ورزشی', 'اصطلاحات و رشته‌های ورزشی', 0);

-- درج مراحل نمونه (مرحله 1 از دسته‌بندی حیوانات)
-- در grid_data حروف صحیح قرار دارند. در clues_data مختصات خانه‌های سوال و جهت فلش‌ها.
INSERT INTO Levels (category_id, level_number, grid_rows, grid_cols, grid_data, clues_data, is_completed, stars) VALUES
(1, 1, 5, 5,
'[ ["س","گ","*","*","*"], ["*","*","*","*","*"] ]', -- نمونه ساده شده
'[ {"row":0, "col":0, "clue":"بهترین دوست انسان", "direction":"LEFT"} ]',
0, 0),

(1, 2, 6, 6,
'[ ["گ","ر","ب","ه","*","*"] ]',
'[ {"row":0, "col":0, "clue":"حیوان ملوس خانگی", "direction":"DOWN"} ]',
0, 0);

-- ایجاد پروفایل پیش‌فرض برای بازیکن
INSERT INTO User (id, username, coins, total_score, current_category_id, current_level_id) VALUES
(1, 'بازیکن', 200, 0, 1, 1);

-- درج یک چالش روزانه نمونه
INSERT INTO DailyPuzzles (puzzle_date, grid_data, clues_data, reward_coins, status) VALUES
('2026-05-05', '[ ["ش","ی","ر"] ]', '[ {"row":0, "col":0, "clue":"سلطان جنگل", "direction":"LEFT"} ]', 100, 0);

-- درج چند دستاورد نمونه
INSERT INTO Achievements (title, description, target_value, current_value, reward_coins, is_claimed) VALUES
('آغاز مسیر', 'اولین مرحله را با موفقیت به پایان برسان', 1, 0, 50, 0),
('جدول‌باز حرفه‌ای', '۱۰ مرحله را کامل کن', 10, 0, 200, 0),
('مرد ثروتمند', 'موجودی سکه‌هایت را به ۵۰۰ برسان', 500, 200, 100, 0);
