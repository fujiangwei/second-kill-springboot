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
import com.kinson.secondkill.utils.JsonUtil;
import com.kinson.secondkill.utils.MD5Util;
import com.kinson.secondkill.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Describe 用户服务接口实现
 * @date
 */
@Service
@Slf4j(topic = "UserServiceImpl")
// 当有多个相同类型的bean时,使用@Primary来赋予bean更高的优先级
@Primary
public class UserServiceImpl extends ServiceImpl<IUserMapper, UserEntity> implements IUserService {

    @Autowired
    private IUserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 参数校验 由入参@Valid注解进行控制,校验不通过会在全局异常中的BindException中进行处理
        /*if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        if (!ValidatorUtil.isMobile(mobile)) {
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }*/

        UserEntity user = userMapper.selectById(mobile);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断密码是否正确
        String formPassToDBPass = MD5Util.formPassToDBPass(password, user.getSalt());
        // log.info("formPassToDBPass = {}", formPassToDBPass);
        if (!formPassToDBPass.equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 生成Cookie
        String userTicket = UUIDUtil.uuid();
        // 将用户信息存入redis，有效时长为30分钟
        redisTemplate.opsForValue().set("user:" + userTicket, JsonUtil.object2JsonStr(user), 1800, TimeUnit.SECONDS);

        // 会话设置用户信息，放入redis替换
        // request.getSession().setAttribute(userTicket, user);
        CookieUtil.setCookie(request, response, "userTicket", userTicket);
        return RespBean.success(userTicket);
    }

    @Override
    public RespBean updatePassword(String userTicket, Long id, String password) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if (1 == result) {
            // 删除Redis
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }

        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }

    @Override
    public UserEntity getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public UserEntity getByUserTicket(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            log.warn("userTicket为空");
            return null;
        }

        // 从redis中获取用户信息
        String userJson = (String) redisTemplate.opsForValue().get("user:" + userTicket);
        if (StringUtils.isEmpty(userJson)) {
            log.warn("userJson为空");
            return null;
        }

        UserEntity user = JsonUtil.jsonStr2Object(userJson, UserEntity.class);
        if (null != user) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
            return user;
        }

        return null;
    }
}
