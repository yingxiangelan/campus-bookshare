-- 校园二手书交易平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_bookshare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_bookshare;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `school` VARCHAR(100) DEFAULT '某某大学' COMMENT '学校',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `campus` VARCHAR(50) DEFAULT NULL COMMENT '校区',
    `certified` TINYINT DEFAULT 0 COMMENT '是否认证 0-未认证 1-已认证',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    INDEX idx_openid (`openid`),
    INDEX idx_student_id (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 书籍信息表（标准书籍库）
CREATE TABLE IF NOT EXISTS `book` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '书籍ID',
    `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN码',
    `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '定价',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面URL',
    `description` TEXT DEFAULT NULL COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_isbn (`isbn`),
    INDEX idx_book_name (`book_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍信息表';

-- 商品表（用户发布的二手书）
CREATE TABLE IF NOT EXISTS `goods` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `book_id` BIGINT DEFAULT NULL COMMENT '关联的书籍ID',
    `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN码',
    `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `condition` VARCHAR(20) NOT NULL COMMENT '新旧程度',
    `campus` VARCHAR(50) NOT NULL COMMENT '校区',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `course_name` VARCHAR(100) DEFAULT NULL COMMENT '适用课程',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `images` TEXT DEFAULT NULL COMMENT '商品图片（JSON数组）',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-在售 1-已售 2-已下架',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    INDEX idx_user_id (`user_id`),
    INDEX idx_book_id (`book_id`),
    INDEX idx_status (`status`),
    INDEX idx_campus (`campus`),
    INDEX idx_major (`major`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家用户ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-待确认 1-已确认 2-已完成 3-已取消',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (`order_no`),
    INDEX idx_goods_id (`goods_id`),
    INDEX idx_buyer_id (`buyer_id`),
    INDEX idx_seller_id (`seller_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `from_user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT DEFAULT 0 COMMENT '消息类型 0-文本 1-图片 2-系统',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    `related_goods_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_from_user (`from_user_id`),
    INDEX idx_to_user (`to_user_id`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `from_user_id` BIGINT NOT NULL COMMENT '评价者用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '被评价者用户ID',
    `rating` TINYINT NOT NULL COMMENT '评分 1-5',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (`order_id`),
    INDEX idx_to_user (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `collect` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_goods (`user_id`, `goods_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_goods_id (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 插入测试数据

-- 测试用户
INSERT INTO `user` (`openid`, `nick_name`, `avatar`, `student_id`, `school`, `major`, `campus`, `certified`) VALUES
('test_openid_1', '张同学', 'https://via.placeholder.com/150', '20210001', '某某大学', '计算机科学与技术', '国际校区', 1),
('test_openid_2', '李同学', 'https://via.placeholder.com/150', '20210002', '某某大学', '软件工程', '五山校区', 1);

-- 测试书籍
INSERT INTO `book` (`isbn`, `book_name`, `author`, `publisher`, `price`, `cover_url`) VALUES
('9787111544937', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', 139.00, 'https://via.placeholder.com/300x400'),
('9787115428028', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', 89.00, 'https://via.placeholder.com/300x400'),
('9787302511991', '数据结构与算法分析', 'Mark Allen Weiss', '清华大学出版社', 59.00, 'https://via.placeholder.com/300x400');

-- 测试商品
INSERT INTO `goods` (`user_id`, `book_id`, `book_name`, `author`, `publisher`, `isbn`, `original_price`, `price`, `condition`, `campus`, `major`, `course_name`, `description`, `images`, `status`) VALUES
(1, 1, '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', '9787111544937', 139.00, 80.00, '9成新', '国际校区', '计算机科学与技术', '计算机组成原理', '书籍保存完好，无笔记，适合计算机专业学生', '["https://via.placeholder.com/600x800"]', 0),
(2, 2, 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', '9787115428028', 89.00, 50.00, '8成新', '五山校区', '软件工程', 'Python程序设计', '有少量笔记，不影响阅读', '["https://via.placeholder.com/600x800"]', 0),
(1, 3, '数据结构与算法分析', 'Mark Allen Weiss', '清华大学出版社', '9787302511991', 59.00, 35.00, '9成新', '国际校区', '计算机科学与技术', '数据结构', '全新，未使用过', '["https://via.placeholder.com/600x800"]', 0);

-- 设置字符集
ALTER DATABASE campus_bookshare CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
