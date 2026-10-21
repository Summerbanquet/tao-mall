-- =====================================================================
-- 校园淘 初始化建表脚本
-- 数据库名：tao（与 application.yml 中 jdbc:mysql://127.0.0.1:3306/tao 对应）
-- 字符集：utf8mb4
-- 执行方式：mysql -uroot -p123456 < sql/init.sql
--          或在 MySQL Workbench / Navicat 中打开执行
-- =====================================================================

CREATE DATABASE IF NOT EXISTS tao DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tao;

-- ---------------------------------------------------------------------
-- 用户表 t_user（对应实体 com.tao.user.entity.User）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    user_name   VARCHAR(50)  NOT NULL                COMMENT '用户账号',
    password    VARCHAR(120) NOT NULL                COMMENT '用户密码(MD5加盐,格式:盐$$摘要)',
    type        TINYINT      NOT NULL DEFAULT 2      COMMENT '用户类型(1运营 2用户)',
    nick_name   VARCHAR(50)  NULL                    COMMENT '用户昵称',
    name        VARCHAR(50)  NULL                    COMMENT '用户姓名',
    phone       VARCHAR(20)  NULL                    COMMENT '手机号码',
    email       VARCHAR(100) NULL                    COMMENT '用户邮箱',
    avatar      VARCHAR(255) NULL                    COMMENT '用户头像',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '数据状态(0禁用 1正常)',
    create_time DATETIME     NULL                    COMMENT '创建时间',
    update_time DATETIME     NULL                    COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0正常 1已删除)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 订单表 t_order（对应实体 com.tao.order.entity.Order）
-- 注意：表名不能用 order，order 是 MySQL 保留字，故使用 t_order
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no     VARCHAR(40)   NOT NULL COMMENT '订单编号',
    user_id      BIGINT        NULL     COMMENT '下单用户ID',
    user_name    VARCHAR(50)   NULL     COMMENT '用户名(冗余)',
    goods_name   VARCHAR(100)  NULL     COMMENT '商品名称',
    goods_price  DECIMAL(10,2) NULL     COMMENT '商品单价',
    quantity     INT           NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(10,2) NULL     COMMENT '订单总金额',
    receiver     VARCHAR(50)   NULL     COMMENT '收货人',
    phone        VARCHAR(20)   NULL     COMMENT '收货手机号',
    address      VARCHAR(255)  NULL     COMMENT '收货地址',
    remark       VARCHAR(255)  NULL     COMMENT '备注',
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态(0待付款 1已付款 2已发货 3已完成 4已取消)',
    create_time  DATETIME      NULL     COMMENT '创建时间',
    update_time  DATETIME      NULL     COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- ---------------------------------------------------------------------
-- 商品表 goods（对应实体 com.tao.goods.entity.Goods）
-- 注意：status 实际用法为 0-下架 / 1-在售
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS goods (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT        NOT NULL                COMMENT '发布人ID',
    title       VARCHAR(100)  NOT NULL                COMMENT '商品标题',
    description TEXT                                  COMMENT '商品描述',
    price       DECIMAL(10,2) NOT NULL                COMMENT '售价',
    cover       VARCHAR(255)  NULL                    COMMENT '封面图URL',
    category_id BIGINT        NULL                    COMMENT '分类ID(对应 category.id)',
    newness      TINYINT       NULL                    COMMENT '新旧程度(100全新/90九成新/80八成新/70七成新/60六成及以下)',
    trade_location VARCHAR(100) NULL                    COMMENT '交易地点',
    status      TINYINT       NOT NULL DEFAULT 1      COMMENT '状态(0下架 1在售)',
    create_time DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除(0正常 1已删除)',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category_id (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '二手商品表';

-- ---------------------------------------------------------------------
-- 分类表 category（对应实体 com.tao.goods.entity.Category）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50) NOT NULL                COMMENT '分类名称',
    sort        INT         NOT NULL DEFAULT 0      COMMENT '排序(小的在前)',
    create_time DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除(0正常 1已删除)',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- ---------------------------------------------------------------------
-- 收藏表 collect（对应实体 com.tao.goods.entity.Collect）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS collect (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID',
    goods_id    BIGINT   NOT NULL                COMMENT '商品ID',
    create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除(0正常 1已删除)',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';

-- ---------------------------------------------------------------------
-- 初始分类数据（首页分类筛选依赖此数据）
-- ---------------------------------------------------------------------
INSERT IGNORE INTO category (id, name, sort) VALUES
    (1, '电子产品', 1),
    (2, '书籍文具', 2),
    (3, '服饰鞋包', 3),
    (4, '生活用品', 4);

-- ---------------------------------------------------------------------
-- 初始管理员账号（密码均为 123456，仅用于本地开发/演示）
-- 说明：无需手工插入账号，启动应用后调用注册接口即可创建普通用户：
--   POST /web/user/register
--   {"userName":"test01","password":"123456","name":"张三","phone":"13800138000","email":"a@b.com","validateCode":"1234"}
-- 需要管理员时，把该用户的 type 改为 1：
--   UPDATE t_user SET type = 1 WHERE user_name = 'test01';
-- ---------------------------------------------------------------------
