package com.kinson.secondkill.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinson.secondkill.annotaions.AccessLimit;
import com.kinson.secondkill.config.UserContext;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.service.IUserService;
import com.kinson.secondkill.utils.CookieUtil;
import com.kinson.secondkill.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Describe 访问拦截器
 * @date
 */
@Slf4j(topic = "AccessInterceptor")
@Component
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 获取用户
            UserEntity user = getUser(request, response);
            // 设置用户上下文数据
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            // 访问次数
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    log.warn("用户信息为空");
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                // 访问路径
                key += ":" + user.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer count = (Integer) valueOperations.get(key);
            if (count == null) {
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                valueOperations.increment(key);
            } else {
                log.warn("用户{}访问{}过于频繁", user.getId(), key);
                render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }

        return true;
    }

    /**
     * 渲染错误信息
     * @param response
     * @param respBeanEnum
     * @throws IOException
     */
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        RespBean bean = RespBean.error(respBeanEnum);
        out.write(new ObjectMapper().writeValueAsString(bean));
        out.flush();
        out.close();
    }

    /**
     * 获取用户
     * @param request
     * @param response
     * @return
     */
    private UserEntity getUser(HttpServletRequest request, HttpServletResponse response) {
        // Cookie获取userTicket
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(ticket)) {
            log.warn("userTicket为空");
            return null;
        }

        return userService.getByUserTicket(ticket, request, response);
    }

}
