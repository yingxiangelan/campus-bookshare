package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.entity.Book;
import com.campus.bookshare.service.BookService;
import com.campus.bookshare.service.ISBNService;
import com.campus.bookshare.service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 书籍Controller - 完整版
 * 实现ISBN扫码查询和封面OCR识别功能
 */
@RestController
@RequestMapping("/book")
@CrossOrigin
public class BookController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private ISBNService isbnService;
    
    @Autowired
    private OCRService ocrService;

    /**
     * 【核心功能】根据ISBN查询书籍信息
     * 
     * 查询优先级：
     * 1. 本地数据库 book 表
     * 2. 第三方ISBN API（豆瓣、Google Books等）
     * 
     * 前端调用: GET /api/book/isbn/{isbn}
     */
    @GetMapping("/isbn/{isbn}")
    public Result<?> getByISBN(@PathVariable String isbn) {
        try {
            // 1. 验证ISBN格式
            if (!isValidISBN(isbn)) {
                return Result.error("ISBN格式无效");
            }
            
            // 2. 优先从本地数据库查询
            Book localBook = bookService.getByISBN(isbn);
            if (localBook != null) {
                return Result.success(convertBookToMap(localBook));
            }
            
            // 3. 本地没有，调用第三方API查询
            Map<String, Object> apiResult = isbnService.queryByISBN(isbn);
            if (apiResult != null && !apiResult.isEmpty()) {
                // 4. 将查询结果保存到本地数据库（缓存）
                saveBookToLocal(apiResult);
                return Result.success(apiResult);
            }
            
            // 5. 都没找到
            return Result.error("未找到该书籍信息，请手动填写");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 【核心功能】OCR识别书籍封面
     * 
     * 通过分析封面图片，提取书名、作者、ISBN等信息
     * 
     * 前端调用: POST /api/book/recognize
     */
    @PostMapping("/recognize")
    public Result<?> recognizeImage(@RequestBody Map<String, String> data) {
        try {
            String imageUrl = data.get("imageUrl");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return Result.error("图片URL不能为空");
            }
            
            // 1. 调用OCR服务识别图片中的文字
            Map<String, Object> ocrResult = ocrService.recognizeBookCover(imageUrl);
            
            if (ocrResult == null || ocrResult.isEmpty()) {
                return Result.error("识别失败，请手动填写");
            }
            
            // 2. 如果识别出了ISBN，尝试获取更详细的信息
            String recognizedISBN = (String) ocrResult.get("isbn");
            if (recognizedISBN != null && isValidISBN(recognizedISBN)) {
                // 用ISBN查询详细信息
                Book book = bookService.getByISBN(recognizedISBN);
                if (book != null) {
                    // 合并OCR结果和数据库信息
                    ocrResult.put("bookName", book.getBookName());
                    ocrResult.put("author", book.getAuthor());
                    ocrResult.put("publisher", book.getPublisher());
                    ocrResult.put("price", book.getPrice());
                }
            }
            
            return Result.success(ocrResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 搜索书籍
     */
    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 本地搜索
            List<Book> books = bookService.searchByKeyword(keyword.trim());
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Book book : books) {
                result.add(convertBookToMap(book));
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取书籍列表
     */
    @GetMapping("/list")
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<Book> books = bookService.list();
            return Result.success(books);
        } catch (Exception e) {
            return Result.error("获取列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取书籍详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        try {
            Book book = bookService.getById(id);
            if (book == null) {
                return Result.error("书籍不存在");
            }
            return Result.success(convertBookToMap(book));
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证ISBN格式
     * 支持ISBN-10和ISBN-13
     */
    private boolean isValidISBN(String isbn) {
        if (isbn == null) return false;
        
        // 去除连字符和空格
        isbn = isbn.replaceAll("[-\\s]", "");
        
        // ISBN-10: 10位数字，最后一位可能是X
        // ISBN-13: 13位数字，以978或979开头
        return isbn.matches("^(97[89])?\\d{9}[\\dX]$");
    }

    /**
     * 将Book实体转换为Map
     */
    private Map<String, Object> convertBookToMap(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", book.getId());
        map.put("isbn", book.getIsbn());
        map.put("bookName", book.getBookName());
        map.put("author", book.getAuthor());
        map.put("publisher", book.getPublisher());
        map.put("price", book.getPrice());
        map.put("coverUrl", book.getCoverUrl());
        map.put("publishDate", book.getPublishDate());
        map.put("description", book.getDescription());
        return map;
    }

    /**
     * 将API查询结果保存到本地数据库
     */
    private void saveBookToLocal(Map<String, Object> bookData) {
        try {
            String isbn = (String) bookData.get("isbn");
            String bookName = (String) bookData.get("bookName");
            String author = (String) bookData.get("author");
            String publisher = (String) bookData.get("publisher");
            Double price = null;
            
            Object priceObj = bookData.get("price");
            if (priceObj instanceof Number) {
                price = ((Number) priceObj).doubleValue();
            }
            
            // 使用现有的getOrCreateBook方法保存
            bookService.getOrCreateBook(isbn, bookName, author, publisher, price);
        } catch (Exception e) {
            // 保存失败不影响主流程
            e.printStackTrace();
        }
    }
}
