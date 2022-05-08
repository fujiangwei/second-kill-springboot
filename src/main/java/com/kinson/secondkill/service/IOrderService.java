package com.kinson.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinson.secondkill.domain.OrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;

/**
 * @Description: 订单接口
 * @author:
 * @date:
 **/
public interface IOrderService extends IService<OrderEntity> {

    /**
     * 秒杀
     *
     * @param user
     * @param goods
     * @return OrderEntity
     */
    OrderEntity secKill(UserEntity user, GoodsVo goods);

}
