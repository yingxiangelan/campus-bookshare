# 项目启动指南

## 快速启动步骤

### 第一步：准备环境

1. **安装JDK 1.8+**
```bash
java -version  # 检查Java版本
```

2. **安装MySQL 8.0+**
```bash
mysql --version  # 检查MySQL版本
```

3. **安装Maven 3.6+**
```bash
mvn -version  # 检查Maven版本
```

4. **安装微信开发者工具**
   - 下载地址：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html

### 第二步：数据库配置

1. **启动MySQL服务**
```bash
# Windows
net start mysql

# Linux/Mac
sudo systemctl start mysql
# 或
sudo service mysql start
```

2. **创建数据库并导入数据**
```bash
# 登录MySQL
mysql -u root -p

# 导入SQL脚本
mysql -u root -p < database/init.sql
```

或者在MySQL命令行中执行：
```sql
source /path/to/campus-bookshare/database/init.sql;
```

3. **验证数据导入**
```sql
USE campus_bookshare;
SHOW TABLES;
SELECT * FROM user;
SELECT * FROM goods;
```

### 第三步：后端配置与启动

1. **修改配置文件**

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_bookshare?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的MySQL用户名
    password: your_password # 修改为你的MySQL密码
```

2. **编译项目**
```bash
cd backend
mvn clean compile
```

3. **启动后端服务**

方式一：使用Maven
```bash
mvn spring-boot:run
```

方式二：打包后运行
```bash
mvn clean package
java -jar target/bookshare-1.0.0.jar
```

4. **验证后端启动成功**

访问：http://localhost:8080/api

看到类似以下信息表示启动成功：
```
=== 校园二手书交易平台启动成功 ===
```

### 第四步：前端配置与启动

1. **修改API地址**

编辑 `miniprogram/app.js`，修改baseUrl：

```javascript
globalData: {
  baseUrl: 'http://localhost:8080/api',  // 本地开发地址
  // 或使用实际服务器地址
  // baseUrl: 'https://your-domain.com/api',
}
```

2. **打开微信开发者工具**
   - 选择"导入项目"
   - 选择 `campus-bookshare/miniprogram` 目录
   - 填写AppID（测试可选择"测试号"）

3. **准备图片资源**

由于项目需要图标资源，有两种方式：

**方式一（推荐）：使用占位图**
暂时使用在线占位图或本地占位图进行开发测试。

**方式二：准备实际图标**
参考 `miniprogram/images/README.md` 准备所需图标。

4. **编译运行**
   - 点击"编译"按钮
   - 在模拟器中查看效果

## 测试账号

数据库中已预置测试账号：

```
测试用户1：
- openid: test_openid_1
- 昵称: 张同学
- 学号: 20210001
- 已认证

测试用户2：
- openid: test_openid_2
- 昵称: 李同学
- 学号: 20210002
- 已认证
```

测试商品已预置3本书籍。

## 常见问题解决

### 1. 数据库连接失败

**错误信息：**
```
Communications link failure
```

**解决方案：**
- 检查MySQL服务是否启动
- 检查用户名密码是否正确
- 检查数据库名称是否正确
- 检查MySQL端口（默认3306）

### 2. Maven依赖下载失败

**解决方案：**
```bash
# 清理Maven缓存
mvn clean

# 强制更新依赖
mvn clean install -U
```

或配置阿里云Maven镜像，在 `~/.m2/settings.xml` 添加：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

### 3. 端口被占用

**错误信息：**
```
Port 8080 is already in use
```

**解决方案：**

方式一：修改端口
在 `application.yml` 中修改：
```yaml
server:
  port: 8081  # 修改为其他端口
```

方式二：停止占用端口的程序
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID [进程ID] /F

# Linux/Mac
lsof -i :8080
kill -9 [进程ID]
```

### 4. 微信小程序访问后端失败

**错误信息：**
```
request:fail
```

**解决方案：**
1. 检查后端服务是否启动
2. 在微信开发者工具中：
   - 点击"详情"
   - 勾选"不校验合法域名..."
3. 检查 `app.js` 中的 `baseUrl` 是否正确

### 5. 图片资源404

**解决方案：**
暂时注释掉或使用占位图：
```html
<!-- 原代码 -->
<image src="../../images/home.png"></image>

<!-- 临时方案1：使用占位图 -->
<image src="https://via.placeholder.com/44x44"></image>

<!-- 临时方案2：使用view组件 -->
<view class="icon-placeholder"></view>
```

## 开发建议

### 1. 开发顺序
1. 先启动后端，确保API可用
2. 使用Postman测试API接口
3. 再启动小程序前端
4. 逐步完善功能

### 2. 调试技巧
- 使用微信开发者工具的Console查看错误
- 使用Network面板查看API请求
- 使用Chrome DevTools调试
- 查看后端控制台日志

### 3. 数据Mock
如果后端未完成，可以在前端mock数据：
```javascript
// utils/mock.js
export const mockBookList = [
  {
    id: 1,
    bookName: '深入理解计算机系统',
    price: 80.00,
    // ...
  }
];
```

## 下一步

1. 完善业务逻辑
2. 添加单元测试
3. 优化用户体验
4. 准备上线部署

## 技术支持

如遇到其他问题：
1. 查看项目README.md
2. 查看代码注释
3. 搜索相关技术文档
4. 在Issues中提问

