package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.service.IUserService;
import com.kinson.secondkill.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author
 * @Describe 用户控制器
 * @date
 */
@Slf4j(topic = "UserController")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 根据用户id获取用户信息
     *
     * @param userReq
     * @return
     */
    @RequestMapping(value = "/info2", method = RequestMethod.GET)
    @ResponseBody
    public RespBean info2(UserEntity userReq) {
        UserEntity user = userService.getById(userReq.getId());
        return RespBean.success(user);
    }

    /**
     * 通过mvc配置解析自定义用户参数类信息处理用户信息
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public RespBean info(UserEntity user) {
        return RespBean.success(user);
    }

    /**
     * 创建用户
     *
     * @param start
     * @param count
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createUser", method = RequestMethod.GET)
    @ResponseBody
    public RespBean createUser(Long start, int count) throws Exception {
        UserUtil.createUser(start, count);
        return RespBean.success(count);
    }

    /**
     * 创建用户ticket
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createUserTicket", method = RequestMethod.GET)
    @ResponseBody
    public RespBean createUserTicket(Integer count) throws Exception {
        List<UserEntity> users = userService.list();
        if (null != count) {
            users = users.subList(0, count);
        }
        UserUtil.createUserTicket(users);
        return RespBean.success(users.size());
    }
}
