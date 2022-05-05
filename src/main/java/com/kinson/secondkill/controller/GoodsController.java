package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

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

    @Autowired
    private IUserService userService;

    /**
     * 跳转到商品列表（会话存放用户信息）
     * @param session
     * @param model
     * @param userTicket
     * @return
     */
    @RequestMapping("/toList2")
    public String toLogin2(HttpSession session, Model model, @CookieValue("userTicket") String userTicket) {
        if (StringUtils.isEmpty(userTicket)) {
            return "login";
        }
        // 会话中获取用户信息
        UserEntity user = (UserEntity) session.getAttribute(userTicket);
        if (null == user) {
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    /**
     * 跳转到商品列表（redis存储用户信息）
     * @param request
     * @param response
     * @param model
     * @param userTicket
     * @return
     */
    @RequestMapping("/toList3")
    public String toLogin3(HttpServletRequest request, HttpServletResponse response,
                          Model model, @CookieValue("userTicket") String userTicket) {
        if (StringUtils.isEmpty(userTicket)) {
            return "login";
        }
        // redis获取用户信息
        UserEntity user = userService.getByUserTicket(userTicket, request, response);
        if (null == user) {
            log.warn("获取用户信息为空");
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    /**
     * 跳转到用户列表（通过mvc配置解析自定义用户参数类信息处理用户信息，用户信息存redis）
     * @param model
     * @param user 通过mvc配置解析自定义用户参数类信息处理获取到的用户信息
     * @return
     */
    @RequestMapping("/toList")
    public String toLogin(Model model, UserEntity user) {
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, UserEntity user, @PathVariable Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        Date startDate = goods.getStartDate();
        Date endDate = goods.getEndDate();
        Date nowDate = new Date();
        // 秒杀状态
        int secKillStatus = 0;
        // 剩余开始时间
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            // 秒杀还未开始
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            // 秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goodsDetail";
    }


}
