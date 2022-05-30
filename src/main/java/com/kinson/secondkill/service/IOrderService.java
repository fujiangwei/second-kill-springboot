package com.kinson.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinson.secondkill.domain.OrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.domain.vo.OrderDetailVo;

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

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);

    /**
     * 验证秒杀地址
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    Boolean checkPath(UserEntity user, Long goodsId, String path);


    /**
     * 生成秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(UserEntity user, Long goodsId);

    /**
     * 校验验证码
     *
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    Boolean checkCaptcha(UserEntity user, Long goodsId, String captcha);


}
