package com.kinson.secondkill.controller;

import com.kinson.secondkill.service.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * @Description: 商品控制器
 * @author:
 * @date:
 **/
@Slf4j(topic = "GoodsController")
@Controller
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping("/toList")
    public String toLogin(HttpSession session, Model model/*, @CookieValue("userTicket") String userTicket*/) {
        /*if (StringUtils.isEmpty(userTicket)) {
            return "login";
        }

        User user = (User) session.getAttribute(userTicket);
        if (null == user) {
            return "login";
        }*/
        // model.addAttribute("user", user);
        return "goodsList";
    }

}