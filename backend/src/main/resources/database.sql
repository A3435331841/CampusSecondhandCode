-- 设置客户端字符集为 utf8mb4
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_secondhand;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(100) DEFAULT NULL COMMENT '微信openid',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `credit_score` INT DEFAULT 100 COMMENT '信用分',
  `status` INT DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建商品表
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 1 COMMENT '库存',
  `status` INT DEFAULT 0 COMMENT '状态: 0待审核, 1在售, 2下架, 3售出',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS `biz_order` (
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` INT DEFAULT 0 COMMENT '状态: 0待支付, 1已支付, 2已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 清理可能存在的旧测试数据
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `biz_product`;

-- 插入一条测试数据
INSERT INTO `sys_user` (`nickname`, `credit_score`, `status`) VALUES ('测试用户', 100, 1);
INSERT INTO `biz_product` (`seller_id`, `category_id`, `title`, `description`, `price`, `stock`, `status`) VALUES (1, 1, '测试商品', '这是一件完美的二手商品', 99.00, 1, 1);
