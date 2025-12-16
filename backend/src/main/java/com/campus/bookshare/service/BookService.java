package com.campus.bookshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.bookshare.entity.Book;
import java.util.List;

/**
 * 书籍服务接口
 */
public interface BookService extends IService<Book> {
    
    /**
     * 根据ISBN获取或创建书籍
     * 如果书籍不存在，则创建新记录
     */
    Book getOrCreateBook(String isbn, String bookName, String author, String publisher, Double price);
    
    /**
     * 根据ISBN查询书籍
     */
    Book getByISBN(String isbn);

    /**
     * 根据关键词搜索书籍（新增）
     */
    List<Book> searchByKeyword(String keyword);
}

