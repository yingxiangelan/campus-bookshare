package com.campus.bookshare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.bookshare.entity.Book;
import com.campus.bookshare.entity.Goods;
import com.campus.bookshare.mapper.GoodsMapper;
import com.campus.bookshare.service.BookService;
import com.campus.bookshare.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    
    @Autowired
    private BookService bookService;
    
    @Override
    @Transactional
    public Goods publishGoods(Long userId, Map<String, Object> data) {
        Goods goods = new Goods();
        goods.setUserId(userId);
        
        // 设置基本信息
        goods.setBookName((String) data.get("bookName"));
        goods.setAuthor((String) data.get("author"));
        goods.setPublisher((String) data.get("publisher"));
        goods.setIsbn((String) data.get("isbn"));
        goods.setCondition((String) data.get("condition"));
        goods.setCampus((String) data.get("campus"));
        goods.setMajor((String) data.get("major"));
        goods.setCourseName((String) data.get("courseName"));
        goods.setDescription((String) data.get("description"));
        
        // 处理价格
        if (data.get("price") != null) {
            if (data.get("price") instanceof Number) {
                goods.setPrice(BigDecimal.valueOf(((Number) data.get("price")).doubleValue()));
            } else {
                goods.setPrice(new BigDecimal(data.get("price").toString()));
            }
        }
        
        if (data.get("originalPrice") != null) {
            if (data.get("originalPrice") instanceof Number) {
                goods.setOriginalPrice(BigDecimal.valueOf(((Number) data.get("originalPrice")).doubleValue()));
            } else {
                goods.setOriginalPrice(new BigDecimal(data.get("originalPrice").toString()));
            }
        }
        
        // 处理图片（JSON数组）
        if (data.get("images") != null) {
            if (data.get("images") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageList = (List<String>) data.get("images");
                goods.setImages(String.join(",", imageList)); // 简单处理，实际可以用JSON
            } else {
                goods.setImages(data.get("images").toString());
            }
        }
        
        // 处理书籍关联
        String isbn = goods.getIsbn();
        if (isbn != null && !isbn.isEmpty()) {
            // 尝试获取或创建书籍记录
            Book book = bookService.getOrCreateBook(
                    isbn,
                    goods.getBookName(),
                    goods.getAuthor(),
                    goods.getPublisher(),
                    goods.getOriginalPrice() != null ? goods.getOriginalPrice().doubleValue() : null
            );
            if (book != null) {
                goods.setBookId(book.getId());
            }
        }
        
        // 设置默认值
        goods.setStatus(0); // 在售
        goods.setViewCount(0);
        goods.setCollectCount(0);
        goods.setIsDeleted(0);
        
        // 保存到数据库
        baseMapper.insert(goods);
        
        return goods;
    }
    
    @Override
    public List<Goods> getGoodsList(String campus, String major, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        return baseMapper.selectGoodsList(campus, major, 0, offset, pageSize);
    }
    
    @Override
    public Map<String, Object> getGoodsDetail(Long id) {
        Goods goods = baseMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            return null;
        }
        
        // 增加浏览次数
        incrementViewCount(id);
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", goods.getId());
        result.put("bookName", goods.getBookName());
        result.put("author", goods.getAuthor());
        result.put("publisher", goods.getPublisher());
        result.put("isbn", goods.getIsbn());
        result.put("originalPrice", goods.getOriginalPrice());
        result.put("price", goods.getPrice());
        result.put("condition", goods.getCondition());
        result.put("campus", goods.getCampus());
        result.put("major", goods.getMajor());
        result.put("courseName", goods.getCourseName());
        result.put("description", goods.getDescription());
        
        // 解析图片
        List<String> images = new ArrayList<>();
        if (goods.getImages() != null && !goods.getImages().isEmpty()) {
            String[] imageArray = goods.getImages().split(",");
            for (String img : imageArray) {
                images.add(img.trim());
            }
        }
        result.put("images", images);
        
        result.put("status", goods.getStatus());
        result.put("viewCount", goods.getViewCount());
        result.put("collectCount", goods.getCollectCount());
        result.put("sellerId", goods.getUserId());
        result.put("createTime", goods.getCreateTime());
        
        return result;
    }
    
    @Override
    public List<Goods> getMyGoods(Long userId) {
        return baseMapper.selectGoodsByUserId(userId);
    }
    
    @Override
    public boolean updateGoodsStatus(Long id, Integer status) {
        Goods goods = baseMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            return false;
        }
        goods.setStatus(status);
        return baseMapper.updateById(goods) > 0;
    }
    
    @Override
    public boolean deleteGoods(Long id) {
        Goods goods = baseMapper.selectById(id);
        if (goods == null) {
            return false;
        }
        goods.setIsDeleted(1);
        return baseMapper.updateById(goods) > 0;
    }
    
    @Override
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
    }
}

