package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.LoginVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.exception.GlobalException;
import com.kinson.secondkill.mapper.IUserMapper;
import com.kinson.secondkill.service.IUserService;
import com.kinson.secondkill.utils.CookieUtil;
import com.kinson.secondkill.utils.MD5Util;
import com.kinson.secondkill.utils.UUIDUtil;
import com.kinson.secondkill.utils.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author
 * @Describe 功能描述
 * @date
 */
@Service
// 当有多个相同类型的bean时,使用@Primary来赋予bean更高的优先级
@Primary
public class UserServiceImpl extends ServiceImpl<IUserMapper, UserEntity> implements IUserService {

    @Autowired
    private IUserMapper userMapper;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 参数校验
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        if (!ValidatorUtil.isMobile(mobile)) {
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }

        UserEntity user = userMapper.selectById(mobile);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断密码是否正确
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 生成Cookie
        String userTicket = UUIDUtil.uuid();
        // 将用户信息存入redis
        // redisTemplate.opsForValue().set("user:" + userTicket, user);

        // 会话设置用户信息
        // request.getSession().setAttribute(userTicket, user);
        CookieUtil.setCookie(request, response, "userTicket", userTicket);
        return RespBean.success(userTicket);
    }

    @Override
    public UserEntity getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }
}
