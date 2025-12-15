package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 消息Controller
 */
@RestController
@RequestMapping("/message")
@CrossOrigin
class MessageController {

    @GetMapping("/list")
    public Result<?> getList() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> msg1 = new HashMap<>();
        msg1.put("id", 1L);
        msg1.put("userId", 2L);
        msg1.put("userName", "李同学");
        msg1.put("avatar", "https://dummyimage.com/150");
        msg1.put("lastMessage", "这本书还在吗？");
        msg1.put("lastMessageTime", System.currentTimeMillis() - 3600000);
        msg1.put("time", "1小时前");
        msg1.put("unreadCount", 2);
        list.add(msg1);
        
        return Result.success(list);
    }

    @GetMapping("/conversation/{userId}")
    public Result<?> getConversation(@PathVariable Long userId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        // 返回对话消息列表
        return Result.success(messages);
    }

    @PostMapping("/send")
    public Result<?> send(@RequestBody Map<String, Object> data) {
        return Result.success("发送成功");
    }

    @PutMapping("/read")
    public Result<?> markRead(@RequestBody Map<String, Object> data) {
        return Result.success("标记成功");
    }
}

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
@CrossOrigin
class OrderController {

    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Object> data) {
        return Result.success("订单创建成功");
    }

    @GetMapping("/list")
    public Result<?> getList(@RequestParam(required = false) String type) {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("orderNo", "ORDER" + System.currentTimeMillis());
        return Result.success(order);
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id) {
        return Result.success("确认成功");
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        return Result.success("取消成功");
    }
}

/**
 * 书籍Controller
 */
@RestController
@RequestMapping("/book")
@CrossOrigin
class BookController {

    @GetMapping("/list")
    public Result<?> getList() {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(new HashMap<>());
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword) {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/isbn/{isbn}")
    public Result<?> getByISBN(@PathVariable String isbn) {
        Map<String, Object> book = new HashMap<>();
        book.put("isbn", isbn);
        book.put("bookName", "示例书籍");
        book.put("author", "作者");
        book.put("publisher", "出版社");
        book.put("price", 99.00);
        return Result.success(book);
    }

    @PostMapping("/recognize")
    public Result<?> recognizeImage(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("bookName", "识别的书名");
        result.put("author", "识别的作者");
        result.put("isbn", "识别的ISBN");
        return Result.success(result);
    }
}

/**
 * 文件Controller
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
class FileController {

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 实际项目中这里会：
            // 1. 保存文件到本地或OSS
            // 2. 返回文件访问URL
            
            String fileName = file.getOriginalFilename();
            String fileUrl = "https://dummyimage.com/600x800";
            
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("上传失败");
        }
    }
}

/**
 * 评价Controller
 */
@RestController
@RequestMapping("/review")
@CrossOrigin
class ReviewController {

    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Object> data) {
        return Result.success("评价成功");
    }

    @GetMapping("/list/{userId}")
    public Result<?> getList(@PathVariable Long userId) {
        return Result.success(new ArrayList<>());
    }
}
