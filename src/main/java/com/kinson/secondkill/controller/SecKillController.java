package com.kinson.secondkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kinson.secondkill.domain.OrderEntity;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.service.ISecKillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description: 秒杀控制器
 * @author:
 * @date:
 **/
@Controller
@Slf4j(topic = "SecKillController")
@RequestMapping("/secKill")
public class SecKillController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISecKillOrderService secKillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/doSecKill")
    public String doSecKill(Model model, UserEntity user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if (goods.getStockCount() < 1) {
            log.warn("用户{}抢购商品{}库存不足", user.getId(), goodsId);
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        // 判断是否重复抢购(解决同一用户同时秒杀多件商品,可以通过数据库建立唯一索引避免)
        SeckillOrderEntity secKillOrder = secKillOrderService.getOne(new QueryWrapper<SeckillOrderEntity>()
                .eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (secKillOrder != null) {
            log.warn("用户{}已抢购商品{},同一用户只能秒杀一件商品", user.getId(), goodsId);
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        // 秒杀
        OrderEntity order = orderService.secKill(user, goods);
        if (null == order) {
            log.warn("用户{}抢购商品{}失败", user.getId(), goodsId);
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(UserEntity user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if (goods.getStockCount() < 1) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 判断是否重复抢购(解决同一用户同时秒杀多件商,可以通过数据库建立唯一索引避免)
        /*SeckillOrderEntity secKillOrder = secKillOrderService.getOne(new QueryWrapper<SeckillOrderEntity>()
                .eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (secKillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }*/
        // 从redis缓存获取(成功秒杀生成订单时会写入redis)
        String secKillOrderJson = (String) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (StringUtils.isNotEmpty(secKillOrderJson)) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 秒杀
        OrderEntity order = orderService.secKill(user, goods);
        if (null == order) {
            return RespBean.error(RespBeanEnum.ERROR);
        }

        return RespBean.success(order);
    }
}
