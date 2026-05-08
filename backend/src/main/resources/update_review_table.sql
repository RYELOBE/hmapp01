-- 更新 review 表：order_id 可以为 null
ALTER TABLE review MODIFY COLUMN order_id BIGINT NULL;

-- 添加 seller_id 字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'review';
SET @columnname = 'seller_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0
  , 'SELECT 1'
  , CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN seller_id BIGINT NULL AFTER order_id')
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 status 字段（如果不存在）
SET @columnname2 = 'status';
SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname2)
  ) > 0
  , 'SELECT 1'
  , CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''APPROVED'' AFTER images')
));
PREPARE stmt2 FROM @preparedStatement2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 添加索引
SET @preparedStatement3 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = 'idx_seller_id')
  ) > 0
  , 'SELECT 1'
  , CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX idx_seller_id (seller_id)')
));
PREPARE stmt3 FROM @preparedStatement3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

SET @preparedStatement4 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = 'idx_status')
  ) > 0
  , 'SELECT 1'
  , CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX idx_status (status)')
));
PREPARE stmt4 FROM @preparedStatement4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;
