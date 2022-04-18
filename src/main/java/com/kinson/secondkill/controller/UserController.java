package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public RespBean info(UserEntity userReq) {
        UserEntity user = userService.getById(userReq.getId());
        return RespBean.success(user);
    }
}
