# 修复日志

Write By CoderJsq

## 修复日期
2025

## 修复内容

### 1. 修复 Java 版本编译错误
**问题：** `java: 错误: 不支持发行版本 5`

**原因：** Maven 编译器插件未明确配置 Java 版本，导致使用默认的 Java 5 版本。

**修复方案：**
- 在 `backend/pom.xml` 的 `<build><plugins>` 部分添加了 `maven-compiler-plugin` 配置
- 明确指定 Java 版本为 1.8（source 和 target）
- 设置编码为 UTF-8

**修改文件：** `backend/pom.xml`

**修改内容：**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```

---

### 2. 修复 MyBatis Spring 注解包缺失错误
**问题：** `java: 程序包org.mybatis.spring.annotation不存在`

**原因：** `mybatis-plus-boot-starter` 虽然包含了 MyBatis Plus 的功能，但 `@MapperScan` 注解来自 `mybatis-spring` 包，需要显式添加该依赖。

**修复方案：**
- 在 `backend/pom.xml` 的 `<dependencies>` 部分添加了 `mybatis-spring` 依赖
- 使用版本 2.0.7（与 Spring Boot 2.7.18 兼容）

**修改文件：** `backend/pom.xml`

**修改内容：**
```xml
<!-- MyBatis Spring -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>2.0.7</version>
</dependency>
```

---

### 3. 实现后端数据库集成功能
**任务：** 实现将发布的书籍内容导入数据库的功能

**实现内容：**

#### 3.1 创建实体类（Entity）
**文件：**
- `backend/src/main/java/com/campus/bookshare/entity/Goods.java` - 商品实体类
- `backend/src/main/java/com/campus/bookshare/entity/Book.java` - 书籍实体类
- `backend/src/main/java/com/campus/bookshare/entity/User.java` - 用户实体类

**说明：**

- 使用 MyBatis Plus 注解（@TableName, @TableId, @TableLogic）
- 实体类字段与数据库表结构对应
- 支持逻辑删除功能

#### 3.2 创建 Mapper 接口和 XML
**文件：**
- `backend/src/main/java/com/campus/bookshare/mapper/GoodsMapper.java` - 商品Mapper接口
- `backend/src/main/java/com/campus/bookshare/mapper/BookMapper.java` - 书籍Mapper接口
- `backend/src/main/resources/mapper/GoodsMapper.xml` - 商品Mapper XML配置
- `backend/src/main/resources/mapper/BookMapper.xml` - 书籍Mapper XML配置

**功能：**
- 商品列表查询（支持校区、专业筛选）
- 根据用户ID查询商品
- 增加浏览次数
- 根据ISBN查询书籍
- 根据书名搜索书籍

#### 3.3 创建 Service 层
**文件：**
- `backend/src/main/java/com/campus/bookshare/service/GoodsService.java` - 商品服务接口
- `backend/src/main/java/com/campus/bookshare/service/impl/GoodsServiceImpl.java` - 商品服务实现
- `backend/src/main/java/com/campus/bookshare/service/BookService.java` - 书籍服务接口
- `backend/src/main/java/com/campus/bookshare/service/impl/BookServiceImpl.java` - 书籍服务实现

**核心功能：**
- **发布商品（publishGoods）**：将小程序提交的书籍信息保存到数据库
  - 自动关联或创建书籍记录（book表）
  - 保存商品信息到goods表
  - 处理图片、价格等数据格式转换
- **获取商品列表**：支持分页、筛选
- **获取商品详情**：包含浏览次数统计
- **更新商品状态**：在售/已售/已下架
- **删除商品**：逻辑删除
- **书籍管理**：根据ISBN获取或创建书籍记录

#### 3.4 更新 Controller 层
**文件：**
- `backend/src/main/java/com/campus/bookshare/controller/GoodsController.java` - 商品控制器

**修改内容：**
- 将模拟数据替换为真实的数据库操作
- 实现发布商品接口，真正保存到数据库
- 实现商品列表查询，从数据库读取
- 实现商品详情查询，从数据库读取
- 实现我的商品查询
- 实现商品状态更新和删除功能

#### 3.5 创建数据导入功能
**文件：**
- `backend/src/main/java/com/campus/bookshare/controller/DataImportController.java` - 数据导入控制器

**功能：**
- **批量导入书籍** (`/data/import/books`)：批量导入书籍信息到标准书籍库
- **批量导入商品** (`/data/import/goods`)：批量导入二手书商品
- **获取示例数据** (`/data/sample/books`, `/data/sample/goods`)：提供示例数据用于测试

**使用方式：**
1. 通过API接口批量导入测试数据
2. 小程序发布时自动导入到数据库
3. 可以手动调用导入接口添加数据

#### 3.6 更新配置文件
**文件：**
- `backend/src/main/resources/application.yml`

**修改内容：**
- 添加 MyBatis Mapper XML 文件路径配置：`mapper-locations: classpath:mapper/*.xml`

---

## 数据库集成说明

### 数据流程
1. **小程序发布书籍**：
   - 用户在小程序填写书籍信息并提交
   - 前端调用 `/api/goods/publish` 接口
   - 后端接收数据，保存到 `goods` 表
   - 如果提供了ISBN，自动关联或创建 `book` 表记录

2. **数据导入方式**：
   - **方式一**：通过小程序发布功能，用户上传后自动导入
   - **方式二**：通过数据导入接口批量导入测试数据
     - POST `/api/data/import/books` - 导入书籍
     - POST `/api/data/import/goods` - 导入商品

3. **数据查询**：
   - 商品列表：GET `/api/goods/list`
   - 商品详情：GET `/api/goods/detail/{id}`
   - 我的商品：GET `/api/goods/my`

### 数据库表结构
- **book表**：标准书籍库，存储书籍基本信息（ISBN、书名、作者等）
- **goods表**：用户发布的二手书商品，关联book表，包含价格、新旧程度、校区等信息

---

### 4. 完善小程序发布功能
**任务：** 确保小程序发布功能能正确发送数据到后端

**修改内容：**
- 更新 `miniprogram/pages/publish/publish.js`
- 在发布商品时，自动获取用户信息并传递 `userId` 到后端
- 确保发布流程完整：上传图片 → 获取用户信息 → 发布商品

**修改文件：** `miniprogram/pages/publish/publish.js`

**关键改动：**
- 在 `submitPublish` 方法中，先获取用户信息
- 将 `userId` 添加到发布数据中
- 确保用户已登录才能发布

---

### 5. 创建Vue3管理系统
**任务：** 创建基于Vue3的管理系统，用于管理商品数据和批量导入

**创建的文件：**

#### 5.1 项目配置文件
- `admin/package.json` - 项目依赖配置
- `admin/vite.config.js` - Vite构建配置
- `admin/index.html` - HTML入口文件

**依赖：**
- Vue 3.3.4
- Vue Router 4.2.5
- Pinia 2.1.7
- Element Plus 2.4.4
- Axios 1.6.2
- XLSX 0.18.5（用于Excel处理）

#### 5.2 核心文件
- `admin/src/main.js` - 应用入口
- `admin/src/App.vue` - 根组件
- `admin/src/router/index.js` - 路由配置
- `admin/src/utils/request.js` - HTTP请求封装

#### 5.3 API接口
- `admin/src/api/goods.js` - 商品相关API接口

#### 5.4 页面组件
- `admin/src/views/GoodsManagement.vue` - 商品管理页面
  - 商品列表展示（支持分页、搜索、筛选）
  - 发布商品功能
  - 查看商品详情
  - 修改商品状态
  - 删除商品

- `admin/src/views/ImportData.vue` - 数据导入页面
  - Excel文件上传导入
  - 手动JSON数据导入
  - 数据格式说明
  - 导入结果展示

**功能特性：**
- 响应式布局，使用Element Plus组件库
- 支持校区、专业筛选
- 分页显示商品列表
- 商品状态管理（在售/已售/已下架）
- 图片上传功能
- Excel批量导入
- JSON手动导入

---

### 6. 实现Excel批量导入功能
**任务：** 后端支持Excel文件解析和批量导入

#### 6.1 添加依赖
**文件：** `backend/pom.xml`

**添加的依赖：**
```xml
<!-- Apache POI for Excel -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

#### 6.2 创建Excel工具类
**文件：** `backend/src/main/java/com/campus/bookshare/util/ExcelUtil.java`

**功能：**
- `readExcel()` - 读取Excel文件并转换为Map列表
- `getCellValue()` - 获取单元格值（支持字符串、数字、日期等）
- `convertToGoodsData()` - 将Excel数据转换为商品数据格式
- 支持字段映射（Excel列名 → 数据库字段名）
- 支持 .xlsx 和 .xls 格式

**字段映射：**
- 书名 → bookName
- 作者 → author
- 出版社 → publisher
- ISBN → isbn
- 原价 → originalPrice
- 售价 → price
- 新旧程度 → condition
- 校区 → campus
- 专业 → major
- 适用课程 → courseName
- 商品描述 → description

#### 6.3 更新数据导入控制器
**文件：** `backend/src/main/java/com/campus/bookshare/controller/DataImportController.java`

**新增接口：**
- `POST /api/data/import/excel` - Excel文件上传导入

**功能：**
- 接收Excel文件（.xlsx或.xls格式）
- 解析Excel文件内容
- 转换为商品数据格式
- 批量导入到数据库
- 返回导入结果（成功数量、失败数量）

**使用方式：**
1. 前端上传Excel文件
2. 后端解析Excel文件
3. 转换为商品数据格式
4. 批量保存到数据库
5. 返回导入结果

### 管理系统说明

```bash
node 18
```



