package com.kinson.secondkill.utils;

import java.util.UUID;

/**
 * @author
 * @Describe UUID工具类
 * @date
 */
public class UUIDUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
