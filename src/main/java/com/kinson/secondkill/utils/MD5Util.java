package com.kinson.secondkill.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author
 * @Describe MD5工具类
 * @date
 */
public class MD5Util {

    /**
     * 密码加密盐值
     */
    private static final String PWD_SALT = "1a2b3c4d";

    /**
     * MD5加密
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    /**
     * 第一次加密
     *
     * @param inputPass
     * @return String
     **/
    public static String inputPassToFormPass(String inputPass) {
        String str = PWD_SALT.charAt(0) + PWD_SALT.charAt(2) + inputPass + PWD_SALT.charAt(5) + PWD_SALT.charAt(4);
        return md5(str);
    }

    /**
     * 第二次加密
     *
     * @param formPass
     * @param PWD_SALT
     * @return String
     **/
    public static String formPassToDBPass(String formPass, String PWD_SALT) {
        String str = PWD_SALT.charAt(0) + PWD_SALT.charAt(2) + formPass + PWD_SALT.charAt(5) + PWD_SALT.charAt(4);
        return md5(str);
    }

    /**
     * 输入密码二次加密为数据库存储密码
     *
     * @param inputPass
     * @param PWD_SALT
     * @return
     */
    public static String inputPassToDBPass(String inputPass, String PWD_SALT) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, PWD_SALT);
        return dbPass;
    }
}
