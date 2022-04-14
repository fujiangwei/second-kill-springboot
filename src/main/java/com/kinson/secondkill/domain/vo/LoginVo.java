package com.kinson.secondkill.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author
 * @Describe 登录入参
 * @date
 */
@Data
public class LoginVo {
    @NotNull
    private String mobile;

    @NotNull
    private String password;

    @Override
    public String toString() {
        return "LoginVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
