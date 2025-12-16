package com.campus.bookshare.service;

import java.util.Map;

/**
 * ISBN查询服务接口
 * 
 * 用于通过ISBN号查询书籍详细信息
 * 支持多种数据源：豆瓣、Google Books、OpenLibrary等
 */
public interface ISBNService {
    
    /**
     * 根据ISBN查询书籍信息
     * 
     * @param isbn ISBN号（支持ISBN-10和ISBN-13）
     * @return 书籍信息Map，包含：bookName, author, publisher, price, coverUrl等
     *         如果未找到返回null
     */
    Map<String, Object> queryByISBN(String isbn);
}
