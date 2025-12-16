# 数据库集成说明文档

## 概述

本文档说明如何将发布的书籍内容导入到数据库中。系统支持两种方式：
1. **小程序发布**：用户通过小程序发布书籍，自动保存到数据库
2. **批量导入**：通过API接口批量导入测试数据或已有数据

## 数据库表结构

### book 表（标准书籍库）
存储书籍的基本信息，作为标准书籍库。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| isbn | VARCHAR(20) | ISBN码，唯一 |
| book_name | VARCHAR(200) | 书名 |
| author | VARCHAR(100) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| price | DECIMAL(10,2) | 定价 |
| cover_url | VARCHAR(500) | 封面URL |

### goods 表（用户发布的二手书）
存储用户发布的二手书商品信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 卖家用户ID |
| book_id | BIGINT | 关联的书籍ID（可选） |
| book_name | VARCHAR(200) | 书名 |
| author | VARCHAR(100) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| isbn | VARCHAR(20) | ISBN码 |
| price | DECIMAL(10,2) | 售价 |
| original_price | DECIMAL(10,2) | 原价 |
| condition | VARCHAR(20) | 新旧程度 |
| campus | VARCHAR(50) | 校区 |
| major | VARCHAR(100) | 专业 |
| description | TEXT | 商品描述 |
| images | TEXT | 商品图片（JSON数组） |
| status | TINYINT | 状态：0-在售 1-已售 2-已下架 |

## 数据导入方式

### 方式一：小程序发布（推荐）

**流程：**
1. 用户在小程序填写书籍信息
2. 上传书籍图片
3. 提交发布
4. 前端调用 `/api/goods/publish` 接口
5. 后端自动保存到数据库

**接口：** `POST /api/goods/publish`

**请求体示例：**
```json
{
  "bookName": "深入理解计算机系统",
  "author": "Randal E.Bryant",
  "publisher": "机械工业出版社",
  "isbn": "9787111544937",
  "originalPrice": 139.00,
  "price": 80.00,
  "condition": "9成新",
  "campus": "东校区",
  "major": "计算机科学与技术",
  "courseName": "计算机组成原理",
  "description": "书籍保存完好，无笔记",
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ]
}
```

**功能说明：**
- 自动保存商品信息到 `goods` 表
- 如果提供了ISBN，自动关联或创建 `book` 表记录
- 返回发布成功信息

### 方式二：批量导入API

#### 1. 批量导入书籍到标准书籍库

**接口：** `POST /api/data/import/books`

**请求体示例：**
```json
[
  {
    "isbn": "9787111544937",
    "bookName": "深入理解计算机系统",
    "author": "Randal E.Bryant",
    "publisher": "机械工业出版社",
    "price": 139.00
  },
  {
    "isbn": "9787115428028",
    "bookName": "Python编程：从入门到实践",
    "author": "Eric Matthes",
    "publisher": "人民邮电出版社",
    "price": 89.00
  }
]
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "successCount": 2,
    "failCount": 0,
    "total": 2
  }
}
```

#### 2. 批量导入商品（二手书）

**接口：** `POST /api/data/import/goods`

**请求体示例：**
```json
[
  {
    "userId": 1,
    "bookName": "深入理解计算机系统",
    "author": "Randal E.Bryant",
    "publisher": "机械工业出版社",
    "isbn": "9787111544937",
    "originalPrice": 139.00,
    "price": 80.00,
    "condition": "9成新",
    "campus": "东校区",
    "major": "计算机科学与技术",
    "courseName": "计算机组成原理",
    "description": "书籍保存完好",
    "images": ["https://example.com/image.jpg"]
  }
]
```

#### 3. 获取示例数据

**获取示例书籍数据：** `GET /api/data/sample/books`

**获取示例商品数据：** `GET /api/data/sample/goods`

这些接口返回示例数据，可以用于测试或作为导入数据的模板。

## 数据查询接口

### 1. 获取商品列表
**接口：** `GET /api/goods/list`

**参数：**
- `page`：页码（默认1）
- `pageSize`：每页数量（默认10）
- `campus`：校区（可选）
- `major`：专业（可选）

### 2. 获取商品详情
**接口：** `GET /api/goods/detail/{id}`

自动增加浏览次数。

### 3. 获取我的商品
**接口：** `GET /api/goods/my?userId={userId}`

## 使用示例

### 使用 curl 导入数据

```bash
# 1. 获取示例书籍数据
curl http://localhost:8080/api/data/sample/books

# 2. 导入书籍数据
curl -X POST http://localhost:8080/api/data/import/books \
  -H "Content-Type: application/json" \
  -d '[
    {
      "isbn": "9787111544937",
      "bookName": "深入理解计算机系统",
      "author": "Randal E.Bryant",
      "publisher": "机械工业出版社",
      "price": 139.00
    }
  ]'

# 3. 导入商品数据
curl -X POST http://localhost:8080/api/data/import/goods \
  -H "Content-Type: application/json" \
  -d '[
    {
      "userId": 1,
      "bookName": "深入理解计算机系统",
      "author": "Randal E.Bryant",
      "publisher": "机械工业出版社",
      "isbn": "9787111544937",
      "originalPrice": 139.00,
      "price": 80.00,
      "condition": "9成新",
      "campus": "东校区",
      "major": "计算机科学与技术",
      "description": "书籍保存完好",
      "images": ["https://example.com/image.jpg"]
    }
  ]'
```

### 使用 Postman 或类似工具

1. 设置请求方法为 POST
2. 设置 URL：`http://localhost:8080/api/data/import/books`
3. 设置 Headers：`Content-Type: application/json`
4. 在 Body 中选择 raw，格式选择 JSON
5. 粘贴示例数据
6. 发送请求

## 注意事项

1. **用户ID**：导入商品时需要提供有效的 `userId`，确保用户表中存在该用户
2. **ISBN唯一性**：书籍表的ISBN是唯一的，重复导入会更新已有记录
3. **图片格式**：图片字段可以是URL字符串数组，存储时会转换为逗号分隔的字符串
4. **数据验证**：导入前建议验证数据的完整性和格式
5. **事务处理**：批量导入时，如果某条数据失败，不会影响其他数据的导入

## 技术实现

### 核心类说明

- **GoodsService**：商品服务，处理商品相关的业务逻辑
- **BookService**：书籍服务，处理书籍相关的业务逻辑
- **GoodsMapper**：商品数据访问层
- **BookMapper**：书籍数据访问层

### 关键功能

1. **自动关联书籍**：发布商品时，如果提供了ISBN，系统会自动查找或创建对应的书籍记录
2. **逻辑删除**：删除商品时使用逻辑删除，不会真正删除数据
3. **浏览次数统计**：查看商品详情时自动增加浏览次数

## 后续优化建议

1. 添加数据验证和错误处理
2. 支持Excel文件导入
3. 添加导入历史记录
4. 支持数据导出功能
5. 添加数据统计和分析功能

