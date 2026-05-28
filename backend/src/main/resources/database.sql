-- 设置客户端字符集为 utf8mb4
SET NAMES utf8mb4;

-- 在执行本脚本前，请先选中目标数据库（默认应用配置为 campus_secondhand）

-- 创建用户表（含管理员账号/密码字段）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号（管理员用）',
  `password` VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt 登录密码哈希',
  `openid` VARCHAR(100) DEFAULT NULL COMMENT '微信openid',
  `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `student_no` VARCHAR(32) DEFAULT NULL COMMENT '学号',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '学院',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
  `id_card_suffix` VARCHAR(8) DEFAULT NULL COMMENT '身份证后缀',
  `verify_status` VARCHAR(20) DEFAULT 'UNVERIFIED' COMMENT '实名状态',
  `verify_time` DATETIME DEFAULT NULL COMMENT '实名时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  `credit_score` INT DEFAULT 100 COMMENT '信用分',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `buy_rating_avg` DECIMAL(3,2) DEFAULT 0.00 COMMENT '买家平均评分',
  `sell_rating_avg` DECIMAL(3,2) DEFAULT 0.00 COMMENT '卖家平均评分',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user/admin',
  `status` INT DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建分类表
CREATE TABLE IF NOT EXISTS `biz_category` (
  `id` INT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 创建商品表（含图片与面交字段）
CREATE TABLE IF NOT EXISTS `biz_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `images` VARCHAR(2000) DEFAULT NULL COMMENT '商品图片URL列表，逗号分隔',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 1 COMMENT '库存',
  `pickup_place_name` VARCHAR(100) DEFAULT NULL COMMENT '面交点名称',
  `pickup_address` VARCHAR(255) DEFAULT NULL COMMENT '面交点地址',
  `pickup_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `pickup_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` INT DEFAULT 0 COMMENT '状态: 0待审核, 1在售, 2下架, 3售出, 4审核驳回',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_seller_id` (`seller_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_category_id` (`category_id`),
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
  `pickup_place_name` VARCHAR(100) DEFAULT NULL COMMENT '面交点名称',
  `pickup_address` VARCHAR(255) DEFAULT NULL COMMENT '面交点地址',
  `pickup_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `pickup_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `buyer_rated` TINYINT NOT NULL DEFAULT 0 COMMENT '买家是否已评价',
  `seller_rated` TINYINT NOT NULL DEFAULT 0 COMMENT '卖家是否已评价',
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

-- 创建学籍名单表（本地权威数据源模拟）
CREATE TABLE IF NOT EXISTS `biz_student_roster` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` VARCHAR(32) NOT NULL COMMENT '学号',
  `real_name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `college` VARCHAR(100) NOT NULL COMMENT '学院',
  `major` VARCHAR(100) NOT NULL COMMENT '专业',
  `grade` VARCHAR(20) NOT NULL COMMENT '年级',
  `id_card_suffix` VARCHAR(8) NOT NULL COMMENT '身份证后缀',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1有效 0失效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no_roster` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学籍名单表';

-- 创建收藏表
CREATE TABLE IF NOT EXISTS `biz_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_favorite` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 创建商品评论表
CREATE TABLE IF NOT EXISTS `biz_product_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1显示 0隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_comment_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- 创建订单评价表
CREATE TABLE IF NOT EXISTS `biz_order_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `reviewer_id` BIGINT NOT NULL COMMENT '评价人ID',
  `reviewee_id` BIGINT NOT NULL COMMENT '被评价人ID',
  `role_type` VARCHAR(32) NOT NULL COMMENT '评价方向',
  `score` TINYINT NOT NULL COMMENT '评分',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_reviewer_role` (`order_no`, `reviewer_id`, `role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';

-- 创建用户行为表
CREATE TABLE IF NOT EXISTS `biz_user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `behavior_type` VARCHAR(20) NOT NULL COMMENT '行为类型',
  `behavior_value` VARCHAR(255) DEFAULT NULL COMMENT '行为附加值',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_behavior_user_id` (`user_id`),
  INDEX `idx_behavior_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';
