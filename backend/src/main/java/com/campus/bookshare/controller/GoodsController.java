package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 商品Controller
 */
@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "time") String sortType) {
        
        // 模拟返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", getMockGoodsList());
        data.put("hasMore", true);
        data.put("total", 20);
        
        return Result.success(data);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("bookName", "深入理解计算机系统");
        detail.put("author", "Randal E.Bryant");
        detail.put("publisher", "机械工业出版社");
        detail.put("isbn", "9787111544937");
        detail.put("originalPrice", 139.00);
        detail.put("price", 80.00);
        detail.put("condition", "9成新");
        detail.put("campus", "东校区");
        detail.put("major", "计算机科学与技术");
        detail.put("courseName", "计算机组成原理");
        detail.put("description", "书籍保存完好，无笔记，适合计算机专业学生");
        detail.put("images", Arrays.asList(
            "https://dummyimage.com/600x800",
            "https://dummyimage.com/600x800"
        ));
        detail.put("sellerAvatar", "https://dummyimage.com/150");
        detail.put("sellerName", "张同学");
        detail.put("sellerCertified", true);
        detail.put("sellerRate", 98);
        detail.put("publishTime", "2天前");
        detail.put("viewCount", 125);
        detail.put("isCollected", false);
        detail.put("sellerId", 1L);
        detail.put("createTime", System.currentTimeMillis());
        
        return Result.success(detail);
    }

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<?> publish(@RequestBody Map<String, Object> data) {
        // 实际项目中这里会保存到数据库
        return Result.success("发布成功");
    }

    /**
     * 获取我的商品
     */
    @GetMapping("/my")
    public Result<?> getMyGoods() {
        List<Map<String, Object>> list = getMockGoodsList();
        return Result.success(list);
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return Result.success("更新成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return Result.success("删除成功");
    }

    /**
     * 模拟商品列表数据
     */
    private List<Map<String, Object>> getMockGoodsList() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1L);
        item1.put("bookName", "深入理解计算机系统");
        item1.put("author", "Randal E.Bryant");
        item1.put("price", 80.00);
        item1.put("originalPrice", 139.00);
        item1.put("condition", "9成新");
        item1.put("campus", "东校区");
        item1.put("major", "计算机科学与技术");
        item1.put("coverUrl", "https://dummyimage.com/300x400");
        item1.put("status", 0);
        item1.put("statusText", "在售");
        list.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2L);
        item2.put("bookName", "Python编程：从入门到实践");
        item2.put("author", "Eric Matthes");
        item2.put("price", 50.00);
        item2.put("originalPrice", 89.00);
        item2.put("condition", "8成新");
        item2.put("campus", "西校区");
        item2.put("major", "软件工程");
        item2.put("coverUrl", "https://dummyimage.com/300x400");
        item2.put("status", 0);
        item2.put("statusText", "在售");
        list.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("id", 3L);
        item3.put("bookName", "数据结构与算法分析");
        item3.put("author", "Mark Allen Weiss");
        item3.put("price", 35.00);
        item3.put("originalPrice", 59.00);
        item3.put("condition", "9成新");
        item3.put("campus", "东校区");
        item3.put("major", "计算机科学与技术");
        item3.put("coverUrl", "https://dummyimage.com/300x400");
        item3.put("status", 0);
        item3.put("statusText", "在售");
        list.add(item3);
        
        return list;
    }
}
