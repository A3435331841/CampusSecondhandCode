-- 设置客户端字符集为 utf8mb4
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_secondhand;

-- 创建用户表（含管理员账号/密码字段）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号（管理员用）',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '登录密码（BCrypt加密）',
  `openid` VARCHAR(100) DEFAULT NULL COMMENT '微信openid',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `credit_score` INT DEFAULT 100 COMMENT '信用分',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin',
  `status` INT DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建商品表（含图片字段）
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `images` VARCHAR(2000) DEFAULT NULL COMMENT '商品图片URL列表，逗号分隔',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 1 COMMENT '库存',
  `status` INT DEFAULT 0 COMMENT '状态: 0待审核, 1在售, 2下架, 3售出',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_seller_id` (`seller_id`),
  INDEX `idx_status` (`status`),
  FULLTEXT INDEX `ft_title` (`title`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS `biz_order` (
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `buy_count` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` INT DEFAULT 0 COMMENT '状态: 0待交接, 1已完成, 2已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_no`),
  INDEX `idx_buyer_id` (`buyer_id`),
  INDEX `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建聊天会话表
CREATE TABLE IF NOT EXISTS `biz_chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_key` VARCHAR(128) NOT NULL COMMENT '会话唯一键: productId_minUserId_maxUserId',
  `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `user_a_id` BIGINT NOT NULL COMMENT '较小用户ID',
  `user_b_id` BIGINT NOT NULL COMMENT '较大用户ID',
  `last_message_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_key` (`session_key`),
  INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS `biz_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_key` VARCHAR(128) NOT NULL COMMENT '会话唯一键',
  `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型: text/system',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  INDEX `idx_session_key` (`session_key`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- =============================================
-- 初始化测试数据（如需重置，先执行TRUNCATE）
-- =============================================

-- 删除旧数据
TRUNCATE TABLE `biz_chat_message`;
TRUNCATE TABLE `biz_chat_session`;
TRUNCATE TABLE `biz_order`;
TRUNCATE TABLE `biz_product`;
TRUNCATE TABLE `sys_user`;

-- 插入管理员账号（密码: 123456，明文存储仅用于开发阶段）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `avatar`, `credit_score`, `role`, `status`)
VALUES ('admin', '123456', '超级管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 100, 'admin', 1);

-- 插入测试普通用户
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `avatar`, `credit_score`, `role`, `status`)
VALUES
  ('user001', '123456', '计科小明', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix', 100, 'user', 1),
  ('user002', '123456', '林学姐', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily', 95, 'user', 1),
  ('user003', '123456', '违规小号', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack', 40, 'user', 0);

-- 插入测试商品（seller_id=2 对应 user001，状态1=在售）
INSERT INTO `biz_product` (`seller_id`, `category_id`, `title`, `description`, `images`, `price`, `stock`, `status`)
VALUES
  (2, 1, '九成新 iPhone 13 128G 冰川蓝', '自用一年，外观无划痕，换新机转让，附赠原装充电头', 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=800&q=80', 2800.00, 1, 1),
  (2, 2, '《计算机网络》第8版（谢希仁）', '考研必备，九成新，内有少量铅笔标注，可擦', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80', 35.00, 1, 1),
  (3, 1, '全新未拆封 索尼WH-1000XM5 耳机', '礼品未拆封，型号WH-1000XM5，正品保障', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 1500.00, 1, 0),
  (2, 4, '捷安特山地车，毕业大甩卖', '骑了两年，车况良好，刹车灵敏，毕业清仓价', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80', 650.00, 1, 1),
  (3, 5, '宿舍用品：风扇+台灯+热水壶套装', '毕业不带走了，三件套一起卖，九成新', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80', 120.00, 1, 1);
