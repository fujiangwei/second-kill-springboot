package com.kinson.secondkill.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author
 * @Describe 用户表映射实体类
 * @date
 */
@Data
@TableName("t_user")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID,手机号码
     **/
    private Long id;

    private String nickname;

    /**
     * MD5(MD5(pass明文+固定salt)+salt)
     **/
    private String password;

    private String salt;

    /**
     * 头像
     **/
    private String head;

    /**
     * 注册时间
     **/
    private Date registerDate;

    /**
     * 最后一次登录事件
     **/
    private Date lastLoginDate;

    /**
     * 登录次数
     **/
    private Integer loginCount;

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", password=******" +
                ", salt='" + salt + '\'' +
                ", head='" + head + '\'' +
                ", registerDate=" + registerDate +
                ", lastLoginDate=" + lastLoginDate +
                ", loginCount=" + loginCount +
                '}';
    }
}
