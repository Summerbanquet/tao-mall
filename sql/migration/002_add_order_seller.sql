-- 002 订单关联商品与卖家，支撑「卖家查看卖出订单 / 卖家发货 / 卖家完成」
USE tao;

-- 商品ID
SET @col = 'goods_id';
SELECT COUNT(*) INTO @c FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'tao' AND TABLE_NAME = 't_order' AND COLUMN_NAME = @col;
SET @sql = IF(@c = 0, 'ALTER TABLE t_order ADD COLUMN goods_id BIGINT DEFAULT NULL COMMENT ''商品ID''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 卖家(商品发布者)用户ID
SET @col = 'seller_id';
SELECT COUNT(*) INTO @c FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'tao' AND TABLE_NAME = 't_order' AND COLUMN_NAME = @col;
SET @sql = IF(@c = 0, 'ALTER TABLE t_order ADD COLUMN seller_id BIGINT DEFAULT NULL COMMENT ''卖家(商品发布者)用户ID''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
