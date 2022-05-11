package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转到商品列表（会话存放用户信息）
     *
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
     *
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
     *
     * @param model
     * @param user  通过mvc配置解析自定义用户参数类信息处理获取到的用户信息
     * @return
     */
    @RequestMapping("/toList4")
    public String toLogin4(Model model, UserEntity user) {
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    /**
     * 商品列表页面优化（缓存）
     *
     * @param model
     * @param user  通过mvc配置解析自定义用户参数类信息处理获取到的用户信息
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toLogin(HttpServletRequest request, HttpServletResponse response,
                          Model model, UserEntity user) {
        ValueOperations vos = redisTemplate.opsForValue();
        // Redis中获取页面，如果不为空，直接返回页面
        String goodsListHtmlCache = (String) vos.get("goodsList");
        if (StringUtils.isNotEmpty(goodsListHtmlCache)) {
            return goodsListHtmlCache;
        }

        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        // 如果为空，手动渲染，存入Redis并返回
        WebContext context = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        goodsListHtmlCache = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (StringUtils.isNotEmpty(goodsListHtmlCache)) {
            vos.set("goodsList", goodsListHtmlCache, 60, TimeUnit.SECONDS);
            return goodsListHtmlCache;
        }
        return "goodsList";
    }

    /**
     * 跳转到详情
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/toDetail2/{goodsId}")
    public String toDetail2(Model model, UserEntity user, @PathVariable Long goodsId) {
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

    /**
     * 跳转到详情()
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(HttpServletRequest request, HttpServletResponse response,
                           Model model, UserEntity user, @PathVariable Long goodsId) {
        ValueOperations vos = redisTemplate.opsForValue();
        String goodsDetailHtmlCache = (String) vos.get("goodsDetail:" + goodsId);
        if (StringUtils.isNotEmpty(goodsDetailHtmlCache)) {
            return goodsDetailHtmlCache;
        }

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
        //如果为空，手动渲染，存入Redis并返回
        WebContext context = new WebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap());
        model.addAttribute("remainSeconds", remainSeconds);

        goodsDetailHtmlCache = thymeleafViewResolver.getTemplateEngine().process("goodsDetail:", context);
        if (StringUtils.isNotEmpty(goodsDetailHtmlCache)) {
            vos.set("goodsDetail" + goodsId, goodsDetailHtmlCache, 60, TimeUnit.SECONDS);
            return goodsDetailHtmlCache;
        }

        return "goodsDetail";
    }


}
