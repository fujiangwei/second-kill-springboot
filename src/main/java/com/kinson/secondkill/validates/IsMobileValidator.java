package com.kinson.secondkill.validates;

import com.kinson.secondkill.annotaions.IsMobile;
import com.kinson.secondkill.utils.ValidatorUtil;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author
 * @Describe 手机号码校验规则
 * @date
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String mobile, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return ValidatorUtil.isMobile(mobile);
        } else {
            if (StringUtils.isEmpty(mobile)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(mobile);
            }
        }
    }
}
