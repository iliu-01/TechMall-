-- ============================================================
-- TechMall 订单服务数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS techmall_order
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE techmall_order;

DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_order;

CREATE TABLE t_order (
    id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(32)    NOT NULL COMMENT '订单编号',
    user_id         BIGINT         NOT NULL COMMENT '下单用户ID',
    total_amount    DECIMAL(10,2)  NOT NULL COMMENT '订单总额',
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    receiver_name   VARCHAR(50)    DEFAULT NULL COMMENT '收货人',
    receiver_phone  VARCHAR(20)    DEFAULT NULL COMMENT '收货电话',
    receiver_addr   VARCHAR(200)   DEFAULT NULL COMMENT '收货地址',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE t_order_item (
    id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id        BIGINT         NOT NULL COMMENT '订单ID',
    product_id      BIGINT         NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(200)   NOT NULL COMMENT '商品名称(快照)',
    product_price   DECIMAL(10,2)  NOT NULL COMMENT '商品单价(快照)',
    quantity        INT            NOT NULL COMMENT '数量',
    amount          DECIMAL(10,2)  NOT NULL COMMENT '小计',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';
