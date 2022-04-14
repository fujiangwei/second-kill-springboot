package com.kinson.secondkill.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.URLEncoder;

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
     * @param salt
     * @return String
     **/
    public static String formPassToDBPass(String formPass, String salt) {
        String str = salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 输入密码二次加密为数据库存储密码
     *
     * @param inputPass
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, salt);
        return dbPass;
    }

    public static void main(String[] args) throws Exception{
        String inputPass = URLEncoder.encode("111111", "utf-8");
        String inputPassToFormPass = inputPassToFormPass(inputPass);
        System.out.println(inputPassToFormPass);
        System.out.println(inputPassToDBPass(inputPassToFormPass, PWD_SALT));
        System.out.println(formPassToDBPass(inputPass, PWD_SALT));
    }
}
