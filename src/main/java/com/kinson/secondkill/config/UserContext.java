package com.kinson.secondkill.config;

import com.kinson.secondkill.domain.UserEntity;

/**
 * @author
 * @Describe 用户上下文
 * @date
 */
public class UserContext {
    private static ThreadLocal<UserEntity> userHolder = new ThreadLocal<>();

    public static void setUser(UserEntity user) {
        userHolder.set(user);
    }

    public static UserEntity getUser() {
        return userHolder.get();
    }
}

