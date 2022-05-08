package com.kinson.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;

/**
 * 秒杀订单表接口
 *
 * @author
 * @date
 */
public interface ISecKillOrderService extends IService<SeckillOrderEntity> {

    /**
     * 获取秒杀结果
     *
     * @param tUser
     * @param goodsId
     * @return orderId 成功 ；-1 秒杀失败 ；0 排队中
     **/
    Long getResult(UserEntity tUser, Long goodsId);
}
