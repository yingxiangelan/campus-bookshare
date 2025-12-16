package com.campus.bookshare.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.bookshare.service.ISBNService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ISBN查询服务实现类
 * 
 * 支持多种数据源：
 * 1. 豆瓣图书API（需要申请API Key）
 * 2. Google Books API（免费，但国内可能需要代理）
 * 3. OpenLibrary API（免费开放）
 * 4. 本地ISBN数据库（作为兜底方案）
 */
@Service
public class ISBNServiceImpl implements ISBNService {

    // 可在application.yml中配置API Key
    @Value("${isbn.douban.apikey:}")
    private String doubanApiKey;
    
    @Value("${isbn.google.apikey:}")
    private String googleApiKey;

    @Override
    public Map<String, Object> queryByISBN(String isbn) {
        // 清理ISBN格式
        isbn = cleanISBN(isbn);
        
        Map<String, Object> result = null;
        
        // 1. 优先尝试OpenLibrary（免费开放，无需API Key）
        result = queryFromOpenLibrary(isbn);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        
        // 2. 尝试Google Books API
        result = queryFromGoogleBooks(isbn);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        
        // 3. 尝试豆瓣API（如果配置了API Key）
        if (doubanApiKey != null && !doubanApiKey.isEmpty()) {
            result = queryFromDouban(isbn);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        
        // 4. 尝试本地模拟数据库（用于测试）
        result = queryFromLocalDatabase(isbn);
        
        return result;
    }

    /**
     * 从OpenLibrary查询（推荐，免费开放）
     * API文档：https://openlibrary.org/dev/docs/api/books
     */
    private Map<String, Object> queryFromOpenLibrary(String isbn) {
        try {
            String url = String.format(
                "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data",
                isbn
            );
            
            String response = HttpUtil.get(url, 5000);
            if (response == null || response.isEmpty() || "{}".equals(response)) {
                return null;
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            JSONObject bookData = json.getJSONObject("ISBN:" + isbn);
            
            if (bookData == null) {
                return null;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("isbn", isbn);
            result.put("bookName", bookData.getStr("title", ""));
            
            // 作者信息
            JSONArray authors = bookData.getJSONArray("authors");
            if (authors != null && !authors.isEmpty()) {
                StringBuilder authorStr = new StringBuilder();
                for (int i = 0; i < authors.size(); i++) {
                    if (i > 0) authorStr.append(", ");
                    authorStr.append(authors.getJSONObject(i).getStr("name", ""));
                }
                result.put("author", authorStr.toString());
            }
            
            // 出版社
            JSONArray publishers = bookData.getJSONArray("publishers");
            if (publishers != null && !publishers.isEmpty()) {
                result.put("publisher", publishers.getJSONObject(0).getStr("name", ""));
            }
            
            // 封面图片
            JSONObject cover = bookData.getJSONObject("cover");
            if (cover != null) {
                result.put("coverUrl", cover.getStr("medium", cover.getStr("small", "")));
            }
            
            // 出版日期
            result.put("publishDate", bookData.getStr("publish_date", ""));
            
            // OpenLibrary没有价格信息，设为null
            result.put("price", null);
            
            result.put("source", "OpenLibrary");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("OpenLibrary查询失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从Google Books查询
     * API文档：https://developers.google.com/books/docs/v1/using
     */
    private Map<String, Object> queryFromGoogleBooks(String isbn) {
        try {
            String url = String.format(
                "https://www.googleapis.com/books/v1/volumes?q=isbn:%s",
                isbn
            );
            
            // 如果有API Key，添加到URL
            if (googleApiKey != null && !googleApiKey.isEmpty()) {
                url += "&key=" + googleApiKey;
            }
            
            String response = HttpUtil.get(url, 5000);
            if (response == null || response.isEmpty()) {
                return null;
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            Integer totalItems = json.getInt("totalItems");
            
            if (totalItems == null || totalItems == 0) {
                return null;
            }
            
            JSONArray items = json.getJSONArray("items");
            if (items == null || items.isEmpty()) {
                return null;
            }
            
            JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");
            if (volumeInfo == null) {
                return null;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("isbn", isbn);
            result.put("bookName", volumeInfo.getStr("title", ""));
            
            // 作者
            JSONArray authors = volumeInfo.getJSONArray("authors");
            if (authors != null && !authors.isEmpty()) {
                result.put("author", String.join(", ", authors.toList(String.class)));
            }
            
            // 出版社
            result.put("publisher", volumeInfo.getStr("publisher", ""));
            
            // 出版日期
            result.put("publishDate", volumeInfo.getStr("publishedDate", ""));
            
            // 封面图片
            JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
            if (imageLinks != null) {
                result.put("coverUrl", imageLinks.getStr("thumbnail", ""));
            }
            
            // 简介
            result.put("description", volumeInfo.getStr("description", ""));
            
            // Google Books没有价格信息
            result.put("price", null);
            
            result.put("source", "GoogleBooks");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Google Books查询失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从豆瓣查询（需要API Key）
     * 注意：豆瓣API已限制，需要申请正式权限
     */
    private Map<String, Object> queryFromDouban(String isbn) {
        try {
            // 豆瓣API v2已不再公开，这里使用旧版API作为示例
            // 实际使用需要申请正式的API权限
            String url = String.format(
                "https://api.douban.com/v2/book/isbn/%s?apikey=%s",
                isbn, doubanApiKey
            );
            
            String response = HttpUtil.get(url, 5000);
            if (response == null || response.isEmpty()) {
                return null;
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            
            // 检查是否有错误
            if (json.containsKey("code")) {
                return null;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("isbn", isbn);
            result.put("bookName", json.getStr("title", ""));
            
            // 作者
            JSONArray authors = json.getJSONArray("author");
            if (authors != null && !authors.isEmpty()) {
                result.put("author", String.join(", ", authors.toList(String.class)));
            }
            
            // 出版社
            result.put("publisher", json.getStr("publisher", ""));
            
            // 出版日期
            result.put("publishDate", json.getStr("pubdate", ""));
            
            // 价格（豆瓣有价格信息）
            String priceStr = json.getStr("price", "");
            if (!priceStr.isEmpty()) {
                try {
                    // 去除价格中的非数字字符，如"CNY 99.00"
                    String numStr = priceStr.replaceAll("[^\\d.]", "");
                    if (!numStr.isEmpty()) {
                        result.put("price", Double.parseDouble(numStr));
                    }
                } catch (NumberFormatException e) {
                    result.put("price", null);
                }
            }
            
            // 封面图片
            JSONObject images = json.getJSONObject("images");
            if (images != null) {
                result.put("coverUrl", images.getStr("large", images.getStr("medium", "")));
            }
            
            // 简介
            result.put("description", json.getStr("summary", ""));
            
            result.put("source", "Douban");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("豆瓣API查询失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从本地模拟数据库查询（用于测试）
     * 实际项目中可以替换为真实的本地ISBN数据库
     */
    private Map<String, Object> queryFromLocalDatabase(String isbn) {
        // 预设一些常用教材的ISBN信息，用于测试
        Map<String, Map<String, Object>> localDB = new HashMap<>();
        
        // 计算机类书籍
        localDB.put("9787111544937", createBookInfo(
            "9787111544937", "深入理解计算机系统", "Randal E.Bryant / David O'Hallaron",
            "机械工业出版社", 139.00, "https://img3.doubanio.com/view/subject/l/public/s29195878.jpg"
        ));
        
        localDB.put("9787115428028", createBookInfo(
            "9787115428028", "Python编程：从入门到实践", "Eric Matthes",
            "人民邮电出版社", 89.00, "https://img1.doubanio.com/view/subject/l/public/s29056553.jpg"
        ));
        
        localDB.put("9787302511991", createBookInfo(
            "9787302511991", "数据结构与算法分析", "Mark Allen Weiss",
            "清华大学出版社", 59.00, null
        ));
        
        localDB.put("9787111407010", createBookInfo(
            "9787111407010", "算法导论", "Thomas H.Cormen",
            "机械工业出版社", 128.00, "https://img9.doubanio.com/view/subject/l/public/s25648004.jpg"
        ));
        
        localDB.put("9787111213826", createBookInfo(
            "9787111213826", "Java核心技术 卷I", "Cay S.Horstmann",
            "机械工业出版社", 139.00, null
        ));
        
        localDB.put("9787115546081", createBookInfo(
            "9787115546081", "机器学习", "周志华",
            "清华大学出版社", 108.00, "https://img2.doubanio.com/view/subject/l/public/s28735609.jpg"
        ));
        
        localDB.put("9787115472953", createBookInfo(
            "9787115472953", "深度学习", "Ian Goodfellow",
            "人民邮电出版社", 168.00, "https://img3.doubanio.com/view/subject/l/public/s29135Mo.jpg"
        ));
        
        // 数学类书籍
        localDB.put("9787302517597", createBookInfo(
            "9787302517597", "线性代数", "同济大学数学系",
            "高等教育出版社", 39.00, null
        ));
        
        localDB.put("9787040396638", createBookInfo(
            "9787040396638", "高等数学 上册", "同济大学数学系",
            "高等教育出版社", 38.80, null
        ));
        
        Map<String, Object> result = localDB.get(isbn);
        if (result != null) {
            result.put("source", "LocalDatabase");
        }
        
        return result;
    }

    /**
     * 创建书籍信息Map
     */
    private Map<String, Object> createBookInfo(String isbn, String bookName, String author,
                                                String publisher, Double price, String coverUrl) {
        Map<String, Object> book = new HashMap<>();
        book.put("isbn", isbn);
        book.put("bookName", bookName);
        book.put("author", author);
        book.put("publisher", publisher);
        book.put("price", price);
        book.put("coverUrl", coverUrl);
        return book;
    }

    /**
     * 清理ISBN格式
     * 去除连字符、空格等
     */
    private String cleanISBN(String isbn) {
        if (isbn == null) return "";
        return isbn.replaceAll("[-\\s]", "");
    }
}
