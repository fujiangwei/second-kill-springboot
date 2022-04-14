package com.kinson.secondkill.exception;

import com.kinson.secondkill.enums.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @Describe 全局异常
 * @date
 */
@Setter
@Getter
@AllArgsConstructor
public class GlobalException extends RuntimeException {
    private RespBeanEnum respBeanEnum;
}
