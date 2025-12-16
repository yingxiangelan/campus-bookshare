package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.entity.Goods;
import com.campus.bookshare.service.BookService;
import com.campus.bookshare.service.GoodsService;
import com.campus.bookshare.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * 数据导入Controller
 * 用于批量导入测试数据或书籍信息
 */
@RestController
@RequestMapping("/data")
@CrossOrigin
public class DataImportController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private GoodsService goodsService;

    /**
     * 批量导入书籍信息到标准书籍库
     */
    @PostMapping("/import/books")
    public Result<?> importBooks(@RequestBody List<Map<String, Object>> books) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> bookData : books) {
                try {
                    String isbn = (String) bookData.get("isbn");
                    String bookName = (String) bookData.get("bookName");
                    String author = (String) bookData.get("author");
                    String publisher = (String) bookData.get("publisher");
                    Double price = bookData.get("price") != null ? 
                        Double.valueOf(bookData.get("price").toString()) : null;
                    
                    if (isbn == null || bookName == null) {
                        failCount++;
                        continue;
                    }
                    
                    bookService.getOrCreateBook(isbn, bookName, author, publisher, price);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", books.size());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入商品（二手书）
     */
    @PostMapping("/import/goods")
    public Result<?> importGoods(@RequestBody List<Map<String, Object>> goodsList) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> goodsData : goodsList) {
                try {
                    Long userId = goodsData.get("userId") != null ? 
                        Long.valueOf(goodsData.get("userId").toString()) : 1L;
                    
                    Goods goods = goodsService.publishGoods(userId, goodsData);
                    if (goods != null) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", goodsList.size());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取示例数据（用于测试）
     */
    @GetMapping("/sample/books")
    public Result<?> getSampleBooks() {
        List<Map<String, Object>> sampleBooks = new ArrayList<>();
        
        // 计算机类书籍
        Map<String, Object> book1 = new HashMap<>();
        book1.put("isbn", "9787111544937");
        book1.put("bookName", "深入理解计算机系统");
        book1.put("author", "Randal E.Bryant");
        book1.put("publisher", "机械工业出版社");
        book1.put("price", 139.00);
        sampleBooks.add(book1);
        
        Map<String, Object> book2 = new HashMap<>();
        book2.put("isbn", "9787115428028");
        book2.put("bookName", "Python编程：从入门到实践");
        book2.put("author", "Eric Matthes");
        book2.put("publisher", "人民邮电出版社");
        book2.put("price", 89.00);
        sampleBooks.add(book2);
        
        Map<String, Object> book3 = new HashMap<>();
        book3.put("isbn", "9787302511991");
        book3.put("bookName", "数据结构与算法分析");
        book3.put("author", "Mark Allen Weiss");
        book3.put("publisher", "清华大学出版社");
        book3.put("price", 59.00);
        sampleBooks.add(book3);
        
        Map<String, Object> book4 = new HashMap<>();
        book4.put("isbn", "9787111407010");
        book4.put("bookName", "算法导论");
        book4.put("author", "Thomas H.Cormen");
        book4.put("publisher", "机械工业出版社");
        book4.put("price", 128.00);
        sampleBooks.add(book4);
        
        Map<String, Object> book5 = new HashMap<>();
        book5.put("isbn", "9787111213826");
        book5.put("bookName", "Java核心技术");
        book5.put("author", "Cay S.Horstmann");
        book5.put("publisher", "机械工业出版社");
        book5.put("price", 139.00);
        sampleBooks.add(book5);
        
        return Result.success(sampleBooks);
    }

    /**
     * 获取示例商品数据（用于测试）
     */
    @GetMapping("/sample/goods")
    public Result<?> getSampleGoods() {
        List<Map<String, Object>> sampleGoods = new ArrayList<>();
        
        Map<String, Object> goods1 = new HashMap<>();
        goods1.put("userId", 1L);
        goods1.put("bookName", "深入理解计算机系统");
        goods1.put("author", "Randal E.Bryant");
        goods1.put("publisher", "机械工业出版社");
        goods1.put("isbn", "9787111544937");
        goods1.put("originalPrice", 139.00);
        goods1.put("price", 80.00);
        goods1.put("condition", "9成新");
        goods1.put("campus", "东校区");
        goods1.put("major", "计算机科学与技术");
        goods1.put("courseName", "计算机组成原理");
        goods1.put("description", "书籍保存完好，无笔记，适合计算机专业学生");
        goods1.put("images", Arrays.asList("https://dummyimage.com/600x800"));
        sampleGoods.add(goods1);
        
        Map<String, Object> goods2 = new HashMap<>();
        goods2.put("userId", 2L);
        goods2.put("bookName", "Python编程：从入门到实践");
        goods2.put("author", "Eric Matthes");
        goods2.put("publisher", "人民邮电出版社");
        goods2.put("isbn", "9787115428028");
        goods2.put("originalPrice", 89.00);
        goods2.put("price", 50.00);
        goods2.put("condition", "8成新");
        goods2.put("campus", "西校区");
        goods2.put("major", "软件工程");
        goods2.put("courseName", "Python程序设计");
        goods2.put("description", "有少量笔记，不影响阅读");
        goods2.put("images", Arrays.asList("https://dummyimage.com/600x800"));
        sampleGoods.add(goods2);
        
        return Result.success(sampleGoods);
    }

    /**
     * Excel批量导入商品
     */
    @PostMapping("/import/excel")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return Result.error("只支持Excel文件（.xlsx或.xls格式）");
            }
            
            // 读取Excel文件
            List<Map<String, Object>> excelData = ExcelUtil.readExcel(file.getInputStream(), fileName);
            
            if (excelData.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }
            
            // 转换为商品数据格式
            List<Map<String, Object>> goodsList = ExcelUtil.convertToGoodsData(excelData, 1L);
            
            // 批量导入
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> goodsData : goodsList) {
                try {
                    Goods goods = goodsService.publishGoods(1L, goodsData);
                    if (goods != null) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", goodsList.size());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载Excel模板文件
     */
    @GetMapping("/template/excel")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            // 设置文件名
            String fileName = URLEncoder.encode("商品导入模板.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
            
            // 生成Excel模板并写入响应流
            OutputStream outputStream = response.getOutputStream();
            ExcelUtil.generateTemplate(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.getWriter().write("模板下载失败: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

