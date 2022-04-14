package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.vo.LoginVo;
import com.kinson.secondkill.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @Description: 登录控制器
 * @author:
 * @date:
 **/
@Slf4j
@RequestMapping("/login")
@Controller
public class LoginController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/toLogin", method = RequestMethod.GET)
    public String toLogin() {
        return "login";
    }

    @RequestMapping(value = "/doLogin", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        log.info("入参{}", loginVo);
        return userService.doLogin(loginVo, request, response);
    }
}