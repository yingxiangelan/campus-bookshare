package com.campus.bookshare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.bookshare.entity.Book;
import com.campus.bookshare.mapper.BookMapper;
import com.campus.bookshare.service.BookService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 书籍服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    
    @Override
    public Book getOrCreateBook(String isbn, String bookName, String author, String publisher, Double price) {
        // 先查询是否存在
        Book book = baseMapper.selectByISBN(isbn);
        
        if (book == null) {
            // 不存在则创建
            book = new Book();
            book.setIsbn(isbn);
            book.setBookName(bookName);
            book.setAuthor(author);
            book.setPublisher(publisher);
            if (price != null) {
                book.setPrice(BigDecimal.valueOf(price));
            }
            baseMapper.insert(book);
        } else {
            // 存在则更新信息（如果提供了新信息）
            boolean needUpdate = false;
            if (bookName != null && !bookName.isEmpty() && !bookName.equals(book.getBookName())) {
                book.setBookName(bookName);
                needUpdate = true;
            }
            if (author != null && !author.isEmpty() && !author.equals(book.getAuthor())) {
                book.setAuthor(author);
                needUpdate = true;
            }
            if (publisher != null && !publisher.isEmpty() && !publisher.equals(book.getPublisher())) {
                book.setPublisher(publisher);
                needUpdate = true;
            }
            if (price != null && !price.equals(book.getPrice())) {
                book.setPrice(BigDecimal.valueOf(price));
                needUpdate = true;
            }
            if (needUpdate) {
                baseMapper.updateById(book);
            }
        }
        
        return book;
    }
    
    @Override
    public Book getByISBN(String isbn) {
        return baseMapper.selectByISBN(isbn);
    }

    @Override
    public List<Book> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return list();
        }

        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("book_name", keyword)
                .or()
                .like("author", keyword)
                .or()
                .like("publisher", keyword)
                .orderByDesc("create_time")
                .last("LIMIT 20");

        return baseMapper.selectList(queryWrapper);
    }
}

