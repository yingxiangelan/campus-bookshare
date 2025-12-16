-- ============================================================
-- 校园二手书交易平台 - 完整数据库初始化脚本
-- Campus BookShare - Complete Database Initialization Script
-- ============================================================

-- 创建数据库
DROP DATABASE IF EXISTS campus_bookshare;
CREATE DATABASE campus_bookshare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_bookshare;

-- ============================================================
-- 1. 用户表 (user)
-- ============================================================
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
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
    
    UNIQUE KEY `uk_openid` (`openid`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 书籍信息表 (book) - 标准书籍库
-- ============================================================
CREATE TABLE `book` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '书籍ID',
    `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN码',
    `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '定价',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面URL',
    `description` TEXT DEFAULT NULL COMMENT '简介',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY `uk_isbn` (`isbn`),
    INDEX `idx_book_name` (`book_name`),
    INDEX `idx_author` (`author`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍信息表（标准书籍库）';

-- ============================================================
-- 3. 商品表 (goods) - 用户发布的二手书
-- ============================================================
CREATE TABLE `goods` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `book_id` BIGINT DEFAULT NULL COMMENT '关联的书籍ID（标准书籍库）',
    `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN码',
    `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `condition` VARCHAR(20) NOT NULL COMMENT '新旧程度（全新/9成新/8成新/7成新/6成新及以下）',
    `campus` VARCHAR(50) NOT NULL COMMENT '校区',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '适用专业',
    `course_name` VARCHAR(100) DEFAULT NULL COMMENT '适用课程',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `images` TEXT DEFAULT NULL COMMENT '商品图片（逗号分隔的URL）',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-在售 1-已售 2-已下架',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_campus` (`campus`),
    INDEX `idx_major` (`major`),
    INDEX `idx_price` (`price`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表（用户发布的二手书）';

-- ============================================================
-- 4. 订单表 (order)
-- 注意: order 是 MySQL 保留字，需要用反引号转义
-- ============================================================
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号（格式：ORD+时间戳+随机数）',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家用户ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0-待确认 1-已确认 2-已完成 3-已取消',
    `buyer_message` VARCHAR(500) DEFAULT NULL COMMENT '买家留言',
    `trade_location` VARCHAR(200) DEFAULT NULL COMMENT '交易地点',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '卖家确认时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '交易完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `cancel_by` TINYINT DEFAULT NULL COMMENT '取消方 0-买家 1-卖家',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_goods_id` (`goods_id`),
    INDEX `idx_buyer_id` (`buyer_id`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 5. 消息表 (message)
-- ============================================================
CREATE TABLE `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `from_user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT DEFAULT 0 COMMENT '消息类型 0-文本 1-图片 2-系统消息',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    `related_goods_id` BIGINT DEFAULT NULL COMMENT '关联商品ID（聊天时讨论的商品）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_from_user` (`from_user_id`),
    INDEX `idx_to_user` (`to_user_id`),
    INDEX `idx_related_goods` (`related_goods_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_conversation` (`from_user_id`, `to_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ============================================================
-- 6. 评价表 (review)
-- ============================================================
CREATE TABLE `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `from_user_id` BIGINT NOT NULL COMMENT '评价者用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '被评价者用户ID',
    `rating` TINYINT NOT NULL COMMENT '评分 1-5星',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `images` TEXT DEFAULT NULL COMMENT '评价图片（逗号分隔的URL）',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名 0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_from_user` (`from_user_id`),
    INDEX `idx_to_user` (`to_user_id`),
    INDEX `idx_rating` (`rating`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================================
-- 7. 收藏表 (collect)
-- ============================================================
CREATE TABLE `collect` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goods_id` (`goods_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================================
-- 8. 浏览历史表 (browse_history) - 可选
-- ============================================================
CREATE TABLE `browse_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `browse_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goods_id` (`goods_id`),
    INDEX `idx_browse_time` (`browse_time`),
    INDEX `idx_user_time` (`user_id`, `browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';

-- ============================================================
-- 插入测试数据
-- ============================================================

-- 测试用户
INSERT INTO `user` (`openid`, `nick_name`, `avatar`, `student_id`, `school`, `major`, `campus`, `certified`, `status`) VALUES
('test_openid_1', '张同学', 'https://via.placeholder.com/150', '20210001', '某某大学', '计算机科学与技术', '国际校区', 1, 1),
('test_openid_2', '李同学', 'https://via.placeholder.com/150', '20210002', '某某大学', '软件工程', '五山校区', 1, 1),
('test_openid_3', '王同学', 'https://via.placeholder.com/150', '20210003', '某某大学', '人工智能', '大学城校区', 1, 1),
('test_openid_4', '赵同学', 'https://via.placeholder.com/150', '20210004', '某某大学', '数据科学', '国际校区', 0, 1),
('test_openid_5', '陈同学', 'https://via.placeholder.com/150', '20210005', '某某大学', '电子信息工程', '五山校区', 1, 1);

-- 测试书籍（标准书籍库）
INSERT INTO `book` (`isbn`, `book_name`, `author`, `publisher`, `price`, `cover_url`, `category`) VALUES
('9787111544937', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', 139.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787115428028', 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', 89.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787302511991', '数据结构与算法分析', 'Mark Allen Weiss', '清华大学出版社', 59.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787111407010', '算法导论', 'Thomas H.Cormen', '机械工业出版社', 128.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787111213826', 'Java核心技术 卷I', 'Cay S.Horstmann', '机械工业出版社', 139.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787111641247', 'Java核心技术 卷II', 'Cay S.Horstmann', '机械工业出版社', 149.00, 'https://via.placeholder.com/300x400', '计算机'),
('9787115546081', '机器学习', '周志华', '清华大学出版社', 108.00, 'https://via.placeholder.com/300x400', '人工智能'),
('9787115472953', '深度学习', 'Ian Goodfellow', '人民邮电出版社', 168.00, 'https://via.placeholder.com/300x400', '人工智能'),
('9787302517597', '线性代数', '同济大学数学系', '高等教育出版社', 39.00, 'https://via.placeholder.com/300x400', '数学'),
('9787040396638', '高等数学 上册', '同济大学数学系', '高等教育出版社', 38.80, 'https://via.placeholder.com/300x400', '数学');

-- 测试商品（二手书）
INSERT INTO `goods` (`user_id`, `book_id`, `book_name`, `author`, `publisher`, `isbn`, `original_price`, `price`, `condition`, `campus`, `major`, `course_name`, `description`, `images`, `status`, `view_count`) VALUES
(1, 1, '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', '9787111544937', 139.00, 80.00, '9成新', '国际校区', '计算机科学与技术', '计算机组成原理', '书籍保存完好，无笔记，适合计算机专业学生', 'https://via.placeholder.com/600x800', 0, 156),
(2, 2, 'Python编程：从入门到实践', 'Eric Matthes', '人民邮电出版社', '9787115428028', 89.00, 50.00, '8成新', '五山校区', '软件工程', 'Python程序设计', '有少量笔记，不影响阅读，送练习题答案', 'https://via.placeholder.com/600x800', 0, 203),
(1, 3, '数据结构与算法分析', 'Mark Allen Weiss', '清华大学出版社', '9787302511991', 59.00, 35.00, '9成新', '国际校区', '计算机科学与技术', '数据结构', '全新未使用，买多了一本', 'https://via.placeholder.com/600x800', 0, 89),
(3, 4, '算法导论', 'Thomas H.Cormen', '机械工业出版社', '9787111407010', 128.00, 75.00, '8成新', '大学城校区', '人工智能', '算法设计与分析', '经典教材，有部分笔记和重点标注', 'https://via.placeholder.com/600x800', 0, 178),
(2, 5, 'Java核心技术 卷I', 'Cay S.Horstmann', '机械工业出版社', '9787111213826', 139.00, 85.00, '9成新', '五山校区', '软件工程', 'Java程序设计', '保存完好，适合Java入门学习', 'https://via.placeholder.com/600x800', 0, 134),
(4, 7, '机器学习', '周志华', '清华大学出版社', '9787115546081', 108.00, 65.00, '9成新', '国际校区', '数据科学', '机器学习导论', '西瓜书，机器学习必备，有少量笔记', 'https://via.placeholder.com/600x800', 0, 267),
(5, 8, '深度学习', 'Ian Goodfellow', '人民邮电出版社', '9787115472953', 168.00, 100.00, '8成新', '五山校区', '人工智能', '深度学习基础', '花书，深度学习经典教材', 'https://via.placeholder.com/600x800', 0, 198),
(3, 9, '线性代数', '同济大学数学系', '高等教育出版社', '9787302517597', 39.00, 20.00, '7成新', '大学城校区', '数学', '线性代数', '有较多笔记，但不影响阅读', 'https://via.placeholder.com/600x800', 0, 76),
(1, 10, '高等数学 上册', '同济大学数学系', '高等教育出版社', '9787040396638', 38.80, 22.00, '8成新', '国际校区', '计算机科学与技术', '高等数学', '考研复习用书，有重点标注', 'https://via.placeholder.com/600x800', 0, 112);

-- 测试订单
INSERT INTO `order` (`order_no`, `goods_id`, `buyer_id`, `seller_id`, `price`, `status`, `buyer_message`, `trade_location`, `create_time`) VALUES
('ORD20241215100001', 1, 2, 1, 80.00, 0, '你好，这本书还在吗？什么时候方便交易？', '图书馆一楼大厅', '2024-12-15 10:00:00'),
('ORD20241214153001', 2, 1, 2, 50.00, 1, '可以便宜点吗？', '教学楼A座门口', '2024-12-14 15:30:00'),
('ORD20241213090001', 3, 3, 1, 35.00, 2, NULL, '食堂门口', '2024-12-13 09:00:00'),
('ORD20241212140001', 4, 1, 3, 75.00, 3, '我想要这本书', NULL, '2024-12-12 14:00:00');

-- 更新已完成订单的时间
UPDATE `order` SET `confirm_time` = '2024-12-14 16:00:00' WHERE `order_no` = 'ORD20241214153001';
UPDATE `order` SET `confirm_time` = '2024-12-13 10:00:00', `finish_time` = '2024-12-13 15:00:00' WHERE `order_no` = 'ORD20241213090001';
UPDATE `order` SET `cancel_time` = '2024-12-12 16:00:00', `cancel_reason` = '买家取消', `cancel_by` = 0 WHERE `order_no` = 'ORD20241212140001';

-- 测试消息
INSERT INTO `message` (`from_user_id`, `to_user_id`, `content`, `type`, `is_read`, `related_goods_id`, `create_time`) VALUES
(2, 1, '你好，这本《深入理解计算机系统》还在吗？', 0, 1, 1, '2024-12-15 09:30:00'),
(1, 2, '在的，你什么时候方便来取？', 0, 1, 1, '2024-12-15 09:35:00'),
(2, 1, '今天下午3点可以吗？', 0, 1, 1, '2024-12-15 09:40:00'),
(1, 2, '可以，我们在图书馆一楼大厅见', 0, 0, 1, '2024-12-15 09:45:00'),
(1, 2, '你好，Python那本书能便宜点吗？', 0, 1, 2, '2024-12-14 15:00:00'),
(2, 1, '最低45，可以送你一份电子版习题答案', 0, 1, 2, '2024-12-14 15:10:00');

-- 测试评价
INSERT INTO `review` (`order_id`, `from_user_id`, `to_user_id`, `rating`, `content`, `create_time`) VALUES
(3, 3, 1, 5, '书籍质量很好，卖家很热情，交易愉快！', '2024-12-13 16:00:00'),
(3, 1, 3, 5, '买家准时到达，交易顺利', '2024-12-13 16:05:00');

-- 测试收藏
INSERT INTO `collect` (`user_id`, `goods_id`, `create_time`) VALUES
(2, 3, '2024-12-14 10:00:00'),
(2, 4, '2024-12-14 10:05:00'),
(3, 1, '2024-12-13 08:00:00'),
(4, 2, '2024-12-12 20:00:00'),
(4, 5, '2024-12-12 20:10:00');

-- 测试浏览历史
INSERT INTO `browse_history` (`user_id`, `goods_id`, `browse_time`) VALUES
(2, 1, '2024-12-15 09:00:00'),
(2, 3, '2024-12-15 09:10:00'),
(3, 1, '2024-12-14 18:00:00'),
(3, 2, '2024-12-14 18:05:00'),
(4, 6, '2024-12-13 12:00:00');

-- ============================================================
-- 创建视图（可选，方便查询）
-- ============================================================

-- 商品详情视图（包含卖家信息）
CREATE OR REPLACE VIEW `v_goods_detail` AS
SELECT 
    g.*,
    u.nick_name AS seller_name,
    u.avatar AS seller_avatar,
    u.certified AS seller_certified,
    b.cover_url AS book_cover
FROM goods g
LEFT JOIN user u ON g.user_id = u.id
LEFT JOIN book b ON g.book_id = b.id
WHERE g.is_deleted = 0;

-- 订单详情视图（包含买卖双方和商品信息）
CREATE OR REPLACE VIEW `v_order_detail` AS
SELECT 
    o.*,
    g.book_name AS goods_name,
    SUBSTRING_INDEX(g.images, ',', 1) AS goods_image,
    buyer.nick_name AS buyer_name,
    buyer.avatar AS buyer_avatar,
    seller.nick_name AS seller_name,
    seller.avatar AS seller_avatar
FROM `order` o
LEFT JOIN goods g ON o.goods_id = g.id
LEFT JOIN user buyer ON o.buyer_id = buyer.id
LEFT JOIN user seller ON o.seller_id = seller.id;

-- ============================================================
-- 完成提示
-- ============================================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('用户数量: ', COUNT(*)) AS info FROM user;
SELECT CONCAT('书籍数量: ', COUNT(*)) AS info FROM book;
SELECT CONCAT('商品数量: ', COUNT(*)) AS info FROM goods;
SELECT CONCAT('订单数量: ', COUNT(*)) AS info FROM `order`;
SELECT CONCAT('消息数量: ', COUNT(*)) AS info FROM message;
