# 校园二手书交易平台 - Campus BookShare (微信登录版)

> **当前版本状态**：已完成核心功能的**前后端联调**，实现了真实的微信一键登录、数据库持久化存储、以及首页商品数据的真实读取。告别了 Mock 假数据，是一个真正跑通的完整 Demo。

## 项目简介

本项目是一个完整的校园二手书交易平台，包含微信小程序前端和 Spring Boot 后端。旨在为大学生提供便捷、高效的二手书交易服务。

**与原版的区别：**
*   **真实后端逻辑**：重构了 Controller/Service/Mapper/Model 层级结构。
*   **真实数据库**：所有用户、书籍数据均存储于 MySQL 数据库。
*   **真实微信登录**：接入微信官方 API，实现了 `Code` 换取 `OpenID` 及用户身份自动注册。

## 主要功能

### 前端功能 (小程序)
- ✅ **微信一键登录**：支持获取微信头像、昵称，自动注册并登录（不再是假登录）。
- ✅ **首页书籍展示**：从后端数据库实时拉取书籍列表，支持封面图、价格、成色显示。
- ✅ **个人中心**：显示真实的头像、昵称，支持退出登录。
- ✅ **多维度筛选**：支持按校区、专业筛选书籍（UI已适配）。
- ✅ **书籍详情**：点击列表可查看书籍详情。

### 后端功能 (Spring Boot)
- ✅ **用户系统**：基于微信 OpenID 的用户注册与登录，JWT Token 鉴权。
- ✅ **商品系统**：提供书籍列表查询接口，支持按时间倒序排列。
- ✅ **数据库交互**：使用 MyBatis Plus 进行高效的 CRUD 操作。
- ✅ **异常处理**：统一的接口返回格式。

## 技术栈

### 前端
- 微信小程序原生框架 (WXML + WXSS + JS)
- 封装的 `request.js` (支持 Promise，自动携带 Token)

### 后端
- Spring Boot 2.7.x
- MyBatis Plus 3.5.3
- MySQL 8.0
- Hutool 工具库 (HTTP请求、JSON解析)
- JWT (用户凭证)

## 项目结构 (已重构)

```text
campus-bookshare/
├── miniprogram/              # 微信小程序前端
│   ├── pages/
│   │   ├── index/          # 首页 (已对接真实接口)
│   │   ├── login/          # [新增] 微信授权登录页
│   │   ├── profile/        # 个人中心 (已对接真实数据)
│   │   └── ...
│   ├── utils/
│   │   └── request.js      # [重构] 移除Bearer前缀，适配简易后端
│   ├── images/             # 图片资源 (需确保 logo.png, default-avatar.png 存在)
│   └── app.js              # 全局配置 (baseUrl)
├── backend/                  # Spring Boot后端
│   ├── src/main/java/com/campus/bookshare/
│   │   ├── controller/     #UserController, GoodsController
│   │   ├── service/        # [新增] UserService, GoodsService
│   │   ├── mapper/         # [新增] UserMapper, GoodsMapper
│   │   ├── model/          # [新增] User, Goods (实体类，含注解映射)
│   │   ├── utils/          # [新增] JwtUtils
│   │   └── Application.java
│   └── resources/
│       └── application.yml # 配置文件 (含数据库和微信配置)
└── database/
    └── init.sql            # 数据库初始化脚本
```

## 快速开始

### 1. 数据库准备

请确保本地安装了 MySQL 8.0+。

1.  创建数据库：
    ```sql
    CREATE DATABASE campus_bookshare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  导入表结构（运行 `init.sql`）。
3.  **[关键] 导入演示书籍数据**：
    ```sql
    USE campus_bookshare;
    -- 清空脏数据
    TRUNCATE TABLE goods;
    -- 插入演示数据
    INSERT INTO goods (user_id, book_name, author, price, campus, `condition`, status, images, description, create_time) VALUES
    (1, '深入理解计算机系统', 'Randal E.Bryant', 80.00, '东校区', '9成新', 0, 'https://dummyimage.com/300x400/ccc/000.png&text=CSAPP', '计算机神书', NOW()),
    (1, 'Python编程: 从入门到实践', 'Eric Matthes', 50.00, '西校区', '95新', 0, 'https://dummyimage.com/300x400/ccc/000.png&text=Python', '学Python必备', NOW());
    ```

### 2. 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_bookshare?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root          # 你的数据库账号
    password: your_password # 你的数据库密码

wechat:
  appid: wxxxxxxxxxxxxxxx   # 你的微信小程序 AppID (必须真实)
  secret: xxxxxxxxxxxxxxxx  # 你的微信小程序 Secret (必须真实)
```

### 3. 启动后端

在 IDE (VS Code / IDEA) 中运行 `Application.java`。
确保控制台显示 `Started Application` 且无报错。

### 4. 前端配置与启动

1.  打开微信开发者工具，导入 `miniprogram` 目录。
2.  修改 `miniprogram/app.js`：
    ```javascript
    globalData: {
      baseUrl: 'http://localhost:8080' // 根据后端配置，确认是否需要加 /api
    }
    ```
3.  **重要检查**：
    *   确保 `miniprogram/images/` 目录下有 `default-avatar.png` 和 `logo.png` 图片，否则控制台会报红。
    *   点击工具栏的“详情” -> “本地设置” -> 勾选 **“不校验合法域名...”**。

## 开发避坑指南 (Troubleshooting)

我们在开发过程中解决的常见问题记录：

### Q1: 登录时报 500 错误？
*   **检查数据库连接**：密码是否正确？数据库名是否叫 `campus_bookshare`？
*   **检查 AppID**：`application.yml` 里的 AppID 和 Secret 必须是真实的（或测试号的），不能是假的。

### Q2: 报错 `Unknown column 'open_id'` 或 `'cover_url'`？
*   **原因**：Java 实体类默认驼峰转下划线（如 `openId` -> `open_id`），但数据库实际列名可能是 `openid` 或 `images`。
*   **解决**：在 Java 实体类的字段上使用 `@TableField("数据库真实列名")` 注解。
    *   `User.java`: `@TableField("openid")`, `@TableField("avatar")`
    *   `Goods.java`: `@TableField("images")`, `@TableField("`condition`")`

### Q3: 首页图片显示报错 `%22https...`？
*   **原因**：数据库里的图片链接存成了 JSON 字符串格式（`["url"]`），导致小程序无法识别。
*   **解决**：清洗数据库，确保存入的是纯净的 `https://...` 字符串。

### Q4: 头像显示灰色或 `dummyimage`？
*   **原因**：使用了旧的 Mock 数据或前端默认值。
*   **解决**：确保 `login/index.js` 中上传逻辑正确，并在数据库中将旧的脏数据（dummyimage链接）清空。

## API 接口变更 (实战版)

我们主要实现了以下真实接口：

- **用户**
    - `POST /user/login/wechat` : 接收 `code`，返回 JWT Token。
    - `POST /user/updateUserInfo`: 更新头像和昵称。
    - `GET /user/info`: 获取当前登录用户的真实信息。

- **商品**
    - `GET /goods/list`: 获取首页书籍列表。
    - `GET /goods/my`: 获取“我发布的”书籍。

## 贡献者

- **原作者**: 提供基础 Demo 框架。
- **重构者**: [你的名字] - 完成了数据库对接、微信登录及核心功能修复。

## 许可证

MIT License