package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinson.secondkill.domain.OrderEntity;
import com.kinson.secondkill.domain.SeckillGoodsEntity;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.domain.vo.OrderDetailVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.exception.GlobalException;
import com.kinson.secondkill.mapper.IOrderMapper;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.service.ISecKillGoodsService;
import com.kinson.secondkill.service.ISecKillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author
 * @Describe 订单服务接口实现
 * @date
 */
@Service
@Slf4j(topic = "orderServiceImpl")
// 当有多个相同类型的bean时,使用@Primary来赋予bean更高的优先级
@Primary
public class OrderServiceImpl extends ServiceImpl<IOrderMapper, OrderEntity> implements IOrderService {

    @Autowired
    private ISecKillGoodsService secKillGoodsService;

    @Autowired
    private IOrderMapper orderMapper;

    @Autowired
    private ISecKillOrderService secKillOrderService;

    @Autowired
    private IGoodsService goodsService;

    @Override
    @Transactional
    public OrderEntity secKill(UserEntity user, GoodsVo goods) {

        // 秒杀商品表减库存
        SeckillGoodsEntity secKillGoods = secKillGoodsService.getOne(new QueryWrapper<SeckillGoodsEntity>()
                .eq("goods_id", goods.getId()));
        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        secKillGoodsService.updateById(secKillGoods);

        // 生成订单
        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(secKillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        // 生成秒杀订单
        SeckillOrderEntity secKillOrder = new SeckillOrderEntity();
        secKillOrder.setOrderId(order.getId());
        secKillOrder.setUserId(user.getId());
        secKillOrder.setGoodsId(goods.getId());
        secKillOrderService.save(secKillOrder);

        return order;
    }

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (null == orderId) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        OrderEntity order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setGoodsVo(goodsVo);
        detail.setOrder(order);

        return detail;
    }
}
