package com.campus.bookshare.service;

import java.util.Map;

/**
 * OCR图像识别服务接口
 * 
 * 用于识别书籍封面图片，提取书名、作者、ISBN等信息
 * 支持多种OCR服务：百度OCR、腾讯OCR、阿里OCR等
 */
public interface OCRService {
    
    /**
     * 识别书籍封面图片
     * 
     * @param imageUrl 图片URL
     * @return 识别结果Map，包含：bookName, author, isbn, publisher等
     *         如果识别失败返回null
     */
    Map<String, Object> recognizeBookCover(String imageUrl);
    
    /**
     * 通用OCR文字识别
     * 
     * @param imageUrl 图片URL
     * @return 识别出的所有文字（按行分割）
     */
    String[] recognizeText(String imageUrl);
}
