-- =====================================================================
-- 001：商品表增加分类字段
-- 说明：该字段用于首页按分类筛选商品
-- 幂等：重复执行不会报错（字段已存在时自动跳过）
-- =====================================================================

USE tao;

SET @db_name = 'tao';

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME   = 'goods'
      AND COLUMN_NAME  = 'category_id'
);

SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE goods ADD COLUMN category_id BIGINT NULL DEFAULT NULL COMMENT ''分类ID'' AFTER cover',
    'SELECT ''goods.category_id 已存在，跳过'' AS skip_reason'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME   = 'goods'
      AND INDEX_NAME   = 'idx_category_id'
);

SET @ddl2 = IF(@idx_exists = 0,
    'ALTER TABLE goods ADD INDEX idx_category_id (category_id)',
    'SELECT ''idx_category_id 已存在，跳过'' AS skip_reason'
);

PREPARE stmt2 FROM @ddl2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
