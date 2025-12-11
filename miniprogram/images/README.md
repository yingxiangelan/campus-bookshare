# 图片资源说明

本项目需要以下图片资源，请放置在 `miniprogram/images/` 目录下：

## TabBar 图标（44x44px）

### 首页
- `home.png` - 首页未选中图标
- `home-active.png` - 首页选中图标

### 发布
- `publish.png` - 发布未选中图标
- `publish-active.png` - 发布选中图标

### 消息
- `message.png` - 消息未选中图标
- `message-active.png` - 消息选中图标

### 我的
- `profile.png` - 我的未选中图标
- `profile-active.png` - 我的选中图标

## 功能图标（根据实际使用尺寸）

### 搜索和导航
- `search.png` - 搜索图标
- `arrow-down.png` - 下拉箭头
- `arrow-right.png` - 右箭头
- `close.png` - 关闭图标

### 扫描和上传
- `scan.png` - 扫描图标
- `camera.png` - 相机图标
- `add.png` - 添加图标

### 功能入口
- `star.png` - 收藏未选中
- `star-active.png` - 收藏选中
- `chat.png` - 聊天图标
- `order.png` - 订单图标
- `collect.png` - 收藏图标
- `cert.png` - 认证图标
- `about.png` - 关于图标
- `logout.png` - 退出图标

### 空状态
- `empty.png` - 空状态图标

## 获取图标资源

### 方法1：使用在线图标库
推荐网站：
- [Iconfont](https://www.iconfont.cn/) - 阿里巴巴图标库
- [Iconpark](https://iconpark.oceanengine.com/) - 字节跳动图标库
- [Feather Icons](https://feathericons.com/) - 简洁线性图标

### 方法2：使用Figma/Sketch设计
可以自己设计或使用UI设计稿中的图标。

### 方法3：临时替代方案
在开发阶段，可以使用纯色方块作为临时图标：

```javascript
// 在代码中使用base64图片或使用view组件代替
<view class="icon-placeholder"></view>
```

```css
.icon-placeholder {
  width: 44rpx;
  height: 44rpx;
  background: #4A90E2;
  border-radius: 50%;
}
```

## 图片规格建议

### TabBar图标
- 尺寸：81x81px（@3x）
- 格式：PNG
- 背景：透明

### 普通图标
- 小图标：40-60rpx
- 中图标：60-88rpx
- 大图标：100-120rpx

### 书籍封面
- 建议比例：3:4
- 列表缩略图：200x267px
- 详情大图：600x800px

## 图片优化

1. 使用TinyPNG等工具压缩图片
2. TabBar图标使用PNG-8格式
3. 普通图标可使用SVG（需转换为base64）
4. 书籍封面使用JPG格式，质量80-90%

## 注意事项

1. 所有图片都应压缩后使用
2. 图标建议使用统一风格
3. 保持视觉一致性
4. 注意版权问题，使用免费或授权图标

## 快速开始

如果您想快速开始测试项目，可以：

1. 创建一个临时的images目录
2. 使用占位符图片网站生成图片：
   - https://via.placeholder.com/100x100
3. 或暂时注释掉图片相关代码
