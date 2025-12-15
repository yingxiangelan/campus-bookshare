package com.campus.bookshare.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类
 * 对应数据库里的 goods 表
 */
@TableName("goods")
public class Goods {

    // 主键 ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 发布者的用户 ID
    private Long userId;

    // 书名 (对应数据库 book_name)
    @TableField("book_name")
    private String bookName;

    // 作者 (对应数据库 author)
    private String author;

    // 价格
    private BigDecimal price;

    // 校区
    private String campus;

    // 成色 (对应数据库 condition，因为是保留字所以加反引号)
    @TableField("`condition`")
    private String condition;

    // 状态 (0在售，1已出)
    private Integer status;

    // 【关键】前端代码里叫 coverUrl，但你数据库里叫 images
    // 这里我们用注解把它们“连”起来
    @TableField("images")
    private String coverUrl;

    // 描述
    private String description;

    // 发布时间
    private Date createTime;

    // ============ Getter & Setter 方法 (必须有) ============
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}