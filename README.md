# 校园二手书交易平台 - Campus BookShare

面向校内场景的大学生二手书交易平台微信小程序完整Demo

## 项目简介

本项目是一个完整的校园二手书交易平台，包含微信小程序前端和Spring Boot后端，旨在为大学生提供便捷、高效的二手书交易服务。

## 主要功能

### 前端功能
- ✅ 微信授权登录
- ✅ 书籍列表浏览（支持校区、专业、排序筛选）
- ✅ 书籍详情查看
- ✅ 快速发布（支持扫描ISBN码、拍照识别）
- ✅ 智能价格建议
- ✅ 消息聊天
- ✅ 个人中心（我的发布、订单、收藏）
- ✅ 学生身份认证

### 后端功能
- ✅ 用户管理（注册、登录、认证）
- ✅ 书籍管理（标准书籍库、商品管理）
- ✅ 订单管理
- ✅ 消息管理
- ✅ 文件上传
- ✅ 评价系统
- ✅ 收藏系统

## 技术栈

### 前端
- 微信小程序原生框架
- WXML + WXSS + JavaScript
- 微信小程序API

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3
- MySQL 8.0
- JWT 认证
- Hutool 工具库

## 项目结构

```
campus-bookshare/
├── miniprogram/              # 微信小程序前端
│   ├── pages/               # 页面
│   │   ├── index/          # 首页
│   │   ├── detail/         # 详情页
│   │   ├── publish/        # 发布页
│   │   ├── message/        # 消息页
│   │   └── profile/        # 个人中心
│   ├── components/          # 组件
│   ├── utils/              # 工具类
│   │   ├── request.js      # 网络请求封装
│   │   ├── api.js          # API接口定义
│   │   └── util.js         # 通用工具函数
│   ├── images/             # 图片资源
│   ├── app.js              # 全局逻辑
│   ├── app.json            # 全局配置
│   └── app.wxss            # 全局样式
├── backend/                 # Spring Boot后端
│   ├── src/main/
│   │   ├── java/com/campus/bookshare/
│   │   │   ├── controller/    # 控制器
│   │   │   ├── service/       # 服务层
│   │   │   ├── mapper/        # 数据访问层
│   │   │   ├── model/         # 数据模型
│   │   │   ├── config/        # 配置类
│   │   │   ├── utils/         # 工具类
│   │   │   ├── common/        # 通用类
│   │   │   └── Application.java  # 启动类
│   │   └── resources/
│   │       └── application.yml   # 配置文件
│   └── pom.xml             # Maven配置
├── database/               # 数据库
│   └── init.sql           # 初始化SQL脚本
└── docs/                  # 文档
    └── README.md          # 项目说明
```

## 快速开始

### 环境要求

#### 前端
- 微信开发者工具
- Node.js 14+（可选，用于开发）（暂时没有用微信云开发功能）

#### 后端
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置

1. 创建数据库并导入初始数据：
```bash
mysql -u root -p < database/init.sql
```

2. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_bookshare?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 安装依赖：
```bash
mvn clean install
```

3. 启动项目：
```bash
mvn spring-boot:run
```

或者直接运行：
```bash
java -jar target/bookshare-1.0.0.jar
```

服务将在 http://localhost:8080/api 启动

### 前端启动

1. 使用微信开发者工具打开 `miniprogram` 目录

2. 修改 `app.js` 中的 `baseUrl` 为你的后端地址：
```javascript
globalData: {
  baseUrl: 'http://localhost:8080/api'  // 修改为实际后端地址
}
```

3. 配置微信小程序 AppID（测试环境可使用测试号）

4. 编译运行

## API接口文档

### 用户相关
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/profile` - 更新用户信息
- `POST /user/certify` - 学生认证

### 书籍相关
- `GET /book/list` - 获取书籍列表
- `GET /book/detail/:id` - 获取书籍详情
- `GET /book/search` - 搜索书籍
- `GET /book/isbn/:isbn` - 根据ISBN获取书籍
- `POST /book/recognize` - 图像识别

### 商品相关
- `POST /goods/publish` - 发布商品
- `GET /goods/list` - 获取商品列表
- `GET /goods/detail/:id` - 获取商品详情
- `GET /goods/my` - 获取我的商品
- `PUT /goods/:id/status` - 更新商品状态
- `DELETE /goods/:id` - 删除商品

### 订单相关
- `POST /order/create` - 创建订单
- `GET /order/list` - 获取订单列表
- `GET /order/detail/:id` - 获取订单详情
- `PUT /order/:id/confirm` - 确认订单
- `PUT /order/:id/cancel` - 取消订单

### 消息相关
- `GET /message/list` - 获取消息列表
- `GET /message/conversation/:userId` - 获取对话
- `POST /message/send` - 发送消息
- `PUT /message/read` - 标记已读

### 文件相关
- `POST /file/upload` - 上传文件

## 核心功能实现

### 1. 扫描ISBN码识别
```javascript
// 前端调用
wx.scanCode({
  onlyFromCamera: true,
  scanType: ['barCode'],
  success: (res) => {
    const isbn = res.result;
    // 调用后端API获取书籍信息
    api.book.getByISBN(isbn);
  }
});
```

### 2. 图像识别（OCR）
```javascript
// 拍照上传后调用OCR识别
api.file.upload(imagePath).then(imageUrl => {
  return api.book.recognizeImage(imageUrl);
}).then(bookInfo => {
  // 自动填充书籍信息
  this.fillBookInfo(bookInfo);
});
```

### 3. 智能价格建议
```javascript
// 根据原价计算建议价格
const suggestedPrice = originalPrice * 0.6;
```

### 4. 多维度筛选
- 校区筛选
- 专业筛选
- 排序方式（最新发布、价格低到高、价格高到低）

## 数据库设计

### 主要表结构

- `user` - 用户表
- `book` - 书籍信息表（标准库）
- `goods` - 商品表（用户发布）
- `order` - 订单表
- `message` - 消息表
- `review` - 评价表
- `collect` - 收藏表

详细字段请查看 `database/init.sql`

## 配置说明

### 微信小程序配置

在 `backend/src/main/resources/application.yml` 中配置微信小程序信息：

```yaml
wechat:
  appid: your_appid
  secret: your_secret
```

### JWT配置

```yaml
jwt:
  secret: campus_bookshare_secret_key
  expiration: 604800  # 7天，单位：秒
```

### 文件上传配置

```yaml
file:
  upload-path: /data/uploads/
  base-url: http://localhost:8080/api/files/
```

## 功能扩展建议

### 短期优化
1. 完善搜索功能（全文搜索、关键词高亮）
2. 添加消息推送
3. 实现实时聊天（WebSocket）
4. 添加书籍推荐算法

### 长期规划
1. 接入第三方支付
2. 引入信用评分系统
3. 添加社区论坛功能
4. 开发管理后台
5. 数据统计分析

## 常见问题

### Q: 如何测试微信登录？
A: 在微信开发者工具中可以使用测试号，或者暂时mock登录接口。

### Q: 图像识别如何实现？
A: 可以接入百度OCR、腾讯OCR等第三方服务，或使用开源OCR库。

## 开发团队

- 项目经理 & 后端开发
- 前端开发
- 前端开发 & 测试
- 产品与UI设计 & 后端开发

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提Issue或联系开发团队。

---

