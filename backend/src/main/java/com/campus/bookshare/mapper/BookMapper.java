package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 书籍Mapper
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
    
    /**
     * 根据ISBN查询书籍
     */
    Book selectByISBN(@Param("isbn") String isbn);
    
    /**
     * 根据书名搜索书籍
     */
    List<Book> searchByBookName(@Param("keyword") String keyword);
}

