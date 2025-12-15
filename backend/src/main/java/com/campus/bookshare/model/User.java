package com.campus.bookshare.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 用户实体类
 * 对应数据库表结构 desc user
 */
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 【重要修正】数据库叫 openid，必须加注解
    @TableField("openid")
    private String openId;

    // 数据库叫 nick_name，MyBatis会自动转换，不需要注解
    private String nickName;

    // 【重要修正】数据库叫 avatar，必须加注解
    @TableField("avatar")
    private String avatarUrl;

    // 下面是截图里其他的字段，顺便补全，方便以后用
    private String phone;
    private String email;
    private String studentId;
    private String school;
    private String major;
    private String campus;
    private Integer certified; // tinyint 对应 Integer
    private Integer status;
    private Date createTime;

    // ============ Getter 和 Setter 方法 ============
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public Integer getCertified() { return certified; }
    public void setCertified(Integer certified) { this.certified = certified; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}