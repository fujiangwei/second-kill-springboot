package com.kinson.secondkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kinson.secondkill.domain.OrderEntity;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.service.ISecKillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description: 秒杀控制器
 * @author:
 * @date:
 **/
@Controller
@RequestMapping("/secKill")
public class SecKillController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISecKillOrderService secKillOrderService;

    @Autowired
    private IOrderService orderService;

    @RequestMapping("/doSecKill")
    public String doSecKill(Model model, UserEntity user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        // 判断是否重复抢购
        SeckillOrderEntity secKillOrder = secKillOrderService.getOne(new QueryWrapper<SeckillOrderEntity>()
                .eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (secKillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        // 秒杀
        OrderEntity order = orderService.secKill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }

}