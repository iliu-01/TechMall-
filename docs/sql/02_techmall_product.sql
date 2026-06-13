-- ============================================================
-- TechMall 商品服务数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS techmall_product
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE techmall_product;

DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50) NOT NULL COMMENT '分类名',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 初始分类数据
INSERT INTO t_category (id, name, parent_id, sort) VALUES
(1, '智能手机', 0, 1),
(2, '笔记本',   0, 2),
(3, '耳机音频', 0, 3),
(4, '智能穿戴', 0, 4),
(5, '平板电脑', 0, 5),
(6, '配件',     0, 6);

DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(200)   NOT NULL COMMENT '商品名称',
    description TEXT           DEFAULT NULL COMMENT '商品描述',
    price       DECIMAL(10,2)  NOT NULL COMMENT '价格',
    stock       INT            NOT NULL DEFAULT 0 COMMENT '库存',
    category_id BIGINT         DEFAULT NULL COMMENT '分类ID',
    merchant_id BIGINT         NOT NULL COMMENT '商家用户ID',
    image_url   VARCHAR(500)   DEFAULT NULL COMMENT '商品图片',
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '0-下架, 1-上架',
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_merchant_id (merchant_id),
    KEY idx_status (status),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 初始商品数据 (商家1: merchant_id=2)
INSERT INTO t_product (name, description, price, stock, category_id, merchant_id, image_url, status) VALUES
('小米 16 Ultra 徕卡影像旗舰 5G',       '骁龙 8 Gen5 + 200MP 徕卡四摄 + 120W 快充',               6999.00,  100, 1, 2, '', 1),
('ROG 幻16 Air 锐龙AI版 轻薄游戏本',    'Ryzen AI 9 + RTX 5070 + 2.5K 240Hz OLED',                 14999.00,  50, 2, 2, '', 1),
('iPad Pro M4 芯片 13英寸',             'M4 芯片 + Liquid Retina XDR + Apple Pencil Pro',          11499.00,  30, 5, 2, '', 1),
('Samsung Galaxy S26 Ultra AI 钛金属',  '骁龙 8 Gen5 + 200MP + S Pen + 钛金属框架',                 9699.00,   80, 1, 2, '', 1),
('Anker Prime 250W GaN 桌面充电站',     '250W 总功率 + GaN + 6口 + 数显',                           799.00,  200, 6, 2, '', 1);

-- 初始商品数据 (商家2: merchant_id=3)
INSERT INTO t_product (name, description, price, stock, category_id, merchant_id, image_url, status) VALUES
('Sony WH-1000XM7 头戴式无线降噪耳机',  '降噪芯片 V3 + 50h 续航 + LDAC 高清传输',                  2899.00, 200, 3, 3, '', 1),
('Bose QuietComfort Ultra 耳塞 钻石款', 'ANC 3.0 自适应降噪 + 空间音频 + IPX5 防水',                1999.00, 150, 3, 3, '', 1),
('AirPods Pro 3 主动降噪',              'H3 芯片 + 自适应音频 + USB-C + 精确查找',                  1699.00, 120, 3, 3, '', 1);
