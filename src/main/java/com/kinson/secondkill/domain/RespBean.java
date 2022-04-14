package com.kinson.secondkill.domain;

import com.kinson.secondkill.enums.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author
 * @Describe 公共返回对象枚举
 * @date
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {
    /**
     * 返回码
     */
    private long code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private Object object;

    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }

    public static RespBean success(Object object) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), object);
    }

    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    public static RespBean error(RespBeanEnum respBeanEnum, Object object) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), object);
    }
}
