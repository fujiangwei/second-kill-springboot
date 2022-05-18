package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.OrderDetailVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description: ${description}
 * @author: kinson
 * @date: 2022/5/12 23:15
 **/
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    @RequestMapping("/orderDetail")
    public String toOrderDetail(Long orderId) {
        return "orderDetail";
    }

    /**
     * 订单详情
     *
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(UserEntity user, Long orderId) {
        if (null == user) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
