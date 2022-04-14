package com.kinson.secondkill.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author
 * @Describe 手机号码校验工具类
 * @date
 */
public class ValidatorUtil {

    /**
     * 校验正则
     */
    private static final Pattern MOBILE_PATTEN = Pattern.compile("[1]([3-9])[0-9]{9}$");


    /**
     * 手机号码校验
     * @param mobile 手机号
     * @return boolean
     */
    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        Matcher matcher = MOBILE_PATTEN.matcher(mobile);
        return matcher.matches();
    }
}
