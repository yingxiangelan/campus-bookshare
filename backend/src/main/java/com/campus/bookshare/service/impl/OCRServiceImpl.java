package com.campus.bookshare.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.bookshare.service.OCRService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR识别服务实现类
 * 
 * 支持多种OCR服务提供商：
 * 1. 百度OCR（推荐，识别准确率高）
 * 2. 腾讯OCR
 * 3. 本地模拟（用于测试）
 * 
 * 使用方法：
 * 1. 在application.yml中配置API Key
 * 2. 调用recognizeBookCover方法识别书籍封面
 */
@Service
public class OCRServiceImpl implements OCRService {

    // 百度OCR配置
    @Value("${ocr.baidu.apikey:}")
    private String baiduApiKey;
    
    @Value("${ocr.baidu.secretkey:}")
    private String baiduSecretKey;
    
    // 腾讯OCR配置
    @Value("${ocr.tencent.secretid:}")
    private String tencentSecretId;
    
    @Value("${ocr.tencent.secretkey:}")
    private String tencentSecretKey;
    
    // 百度Access Token缓存
    private String baiduAccessToken;
    private long baiduTokenExpireTime;

    @PostConstruct
    public void init() {
        // 启动时尝试获取百度Access Token
        if (baiduApiKey != null && !baiduApiKey.isEmpty()) {
            refreshBaiduAccessToken();
        }
    }

    @Override
    public Map<String, Object> recognizeBookCover(String imageUrl) {
        try {
            // 1. 先尝试使用百度OCR
            if (baiduApiKey != null && !baiduApiKey.isEmpty()) {
                Map<String, Object> result = recognizeWithBaiduOCR(imageUrl);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
            
            // 2. 如果百度OCR不可用，使用本地模拟
            return recognizeWithLocalSimulation(imageUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String[] recognizeText(String imageUrl) {
        try {
            if (baiduApiKey != null && !baiduApiKey.isEmpty()) {
                return recognizeTextWithBaiduOCR(imageUrl);
            }
            return new String[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * 使用百度OCR识别书籍封面
     * 
     * 百度OCR API文档：https://ai.baidu.com/ai-doc/OCR/dk3iqnq51
     */
    private Map<String, Object> recognizeWithBaiduOCR(String imageUrl) {
        try {
            // 1. 确保Access Token有效
            if (baiduAccessToken == null || System.currentTimeMillis() > baiduTokenExpireTime) {
                refreshBaiduAccessToken();
            }
            
            if (baiduAccessToken == null) {
                return null;
            }
            
            // 2. 下载图片并转为Base64
            String imageBase64 = downloadImageAsBase64(imageUrl);
            if (imageBase64 == null) {
                return null;
            }
            
            // 3. 调用百度通用文字识别（高精度版）
            String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + baiduAccessToken;
            
            String response = HttpRequest.post(ocrUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form("image", imageBase64)
                .form("detect_direction", "true")
                .form("paragraph", "true")
                .execute()
                .body();
            
            if (response == null || response.isEmpty()) {
                return null;
            }
            
            JSONObject json = JSONUtil.parseObj(response);
            
            // 检查错误
            if (json.containsKey("error_code")) {
                System.err.println("百度OCR错误: " + json.getStr("error_msg"));
                return null;
            }
            
            // 4. 解析识别结果
            JSONArray wordsResult = json.getJSONArray("words_result");
            if (wordsResult == null || wordsResult.isEmpty()) {
                return null;
            }
            
            // 提取所有文字
            List<String> allText = new ArrayList<>();
            for (int i = 0; i < wordsResult.size(); i++) {
                String words = wordsResult.getJSONObject(i).getStr("words", "");
                if (!words.isEmpty()) {
                    allText.add(words);
                }
            }
            
            // 5. 智能分析提取书籍信息
            return analyzeBookInfo(allText);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用百度OCR识别文字
     */
    private String[] recognizeTextWithBaiduOCR(String imageUrl) {
        try {
            if (baiduAccessToken == null || System.currentTimeMillis() > baiduTokenExpireTime) {
                refreshBaiduAccessToken();
            }
            
            if (baiduAccessToken == null) {
                return new String[0];
            }
            
            String imageBase64 = downloadImageAsBase64(imageUrl);
            if (imageBase64 == null) {
                return new String[0];
            }
            
            String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + baiduAccessToken;
            
            String response = HttpRequest.post(ocrUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form("image", imageBase64)
                .execute()
                .body();
            
            JSONObject json = JSONUtil.parseObj(response);
            JSONArray wordsResult = json.getJSONArray("words_result");
            
            if (wordsResult == null) {
                return new String[0];
            }
            
            String[] result = new String[wordsResult.size()];
            for (int i = 0; i < wordsResult.size(); i++) {
                result[i] = wordsResult.getJSONObject(i).getStr("words", "");
            }
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    /**
     * 刷新百度Access Token
     */
    private void refreshBaiduAccessToken() {
        try {
            String tokenUrl = String.format(
                "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                baiduApiKey, baiduSecretKey
            );
            
            String response = HttpUtil.get(tokenUrl, 5000);
            JSONObject json = JSONUtil.parseObj(response);
            
            if (json.containsKey("access_token")) {
                baiduAccessToken = json.getStr("access_token");
                // Token有效期30天，这里设置为29天后过期
                baiduTokenExpireTime = System.currentTimeMillis() + 29L * 24 * 60 * 60 * 1000;
                System.out.println("百度OCR Access Token 获取成功");
            } else {
                System.err.println("获取百度Access Token失败: " + response);
            }
            
        } catch (Exception e) {
            System.err.println("获取百度Access Token异常: " + e.getMessage());
        }
    }

    /**
     * 下载图片并转为Base64
     */
    private String downloadImageAsBase64(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            try (InputStream is = url.openStream()) {
                byte[] bytes = is.readAllBytes();
                return Base64.encode(bytes);
            }
        } catch (Exception e) {
            System.err.println("下载图片失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 智能分析OCR结果，提取书籍信息
     * 
     * 分析策略：
     * 1. 书名：通常是最大字体的文字，位于封面上半部分
     * 2. 作者：通常包含"著"、"编"、"译"等关键词
     * 3. ISBN：13位或10位数字，可能以978开头
     * 4. 出版社：通常包含"出版社"、"出版"等关键词
     */
    private Map<String, Object> analyzeBookInfo(List<String> textLines) {
        Map<String, Object> result = new HashMap<>();
        
        String bookName = null;
        String author = null;
        String isbn = null;
        String publisher = null;
        
        // ISBN正则表达式
        Pattern isbnPattern = Pattern.compile("(97[89])?\\d{9}[\\dX]");
        
        // 作者关键词
        String[] authorKeywords = {"著", "编著", "编", "译", "主编", "作者"};
        
        // 出版社关键词
        String[] publisherKeywords = {"出版社", "出版", "Press", "Publishing"};
        
        for (int i = 0; i < textLines.size(); i++) {
            String line = textLines.get(i).trim();
            
            if (line.isEmpty()) continue;
            
            // 1. 提取ISBN
            if (isbn == null) {
                Matcher isbnMatcher = isbnPattern.matcher(line.replaceAll("[-\\s]", ""));
                if (isbnMatcher.find()) {
                    isbn = isbnMatcher.group();
                    continue;
                }
            }
            
            // 2. 提取作者
            if (author == null) {
                for (String keyword : authorKeywords) {
                    if (line.contains(keyword)) {
                        // 去除关键词，剩下的就是作者名
                        author = line.replaceAll("[著编译主作者]", "").trim();
                        if (author.isEmpty()) {
                            author = null;
                        }
                        break;
                    }
                }
                if (author != null) continue;
            }
            
            // 3. 提取出版社
            if (publisher == null) {
                for (String keyword : publisherKeywords) {
                    if (line.contains(keyword)) {
                        publisher = line;
                        break;
                    }
                }
                if (publisher != null) continue;
            }
            
            // 4. 提取书名（排除作者和出版社后的第一行较长文字）
            if (bookName == null && line.length() > 2 && !isNumeric(line)) {
                // 排除一些常见的非书名文字
                if (!line.contains("版") && !line.contains("ISBN") && 
                    !line.contains("定价") && !line.contains("￥")) {
                    bookName = line;
                }
            }
        }
        
        // 如果前面没有识别出书名，取第一行非空文字
        if (bookName == null && !textLines.isEmpty()) {
            for (String line : textLines) {
                if (line.length() > 2 && !isNumeric(line)) {
                    bookName = line;
                    break;
                }
            }
        }
        
        result.put("bookName", bookName);
        result.put("author", author);
        result.put("isbn", isbn);
        result.put("publisher", publisher);
        result.put("confidence", calculateConfidence(bookName, author, isbn, publisher));
        
        return result;
    }

    /**
     * 计算识别置信度
     */
    private double calculateConfidence(String bookName, String author, String isbn, String publisher) {
        int score = 0;
        int total = 4;
        
        if (bookName != null && !bookName.isEmpty()) score++;
        if (author != null && !author.isEmpty()) score++;
        if (isbn != null && !isbn.isEmpty()) score += 2;  // ISBN权重更高
        if (publisher != null && !publisher.isEmpty()) score++;
        
        return (double) score / (total + 1);
    }

    /**
     * 判断字符串是否全是数字
     */
    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    /**
     * 本地模拟识别（用于测试，无需OCR API）
     * 
     * 根据图片URL中的关键词返回模拟数据
     */
    private Map<String, Object> recognizeWithLocalSimulation(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟识别结果
        result.put("bookName", "识别的书籍名称");
        result.put("author", "识别的作者");
        result.put("isbn", null);  // 封面通常没有ISBN
        result.put("publisher", "识别的出版社");
        result.put("confidence", 0.7);
        result.put("source", "LocalSimulation");
        result.put("message", "当前使用模拟数据，请配置OCR API以获得真实识别结果");
        
        return result;
    }
}
