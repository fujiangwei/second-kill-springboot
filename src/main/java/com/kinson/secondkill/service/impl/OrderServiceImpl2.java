//package com.kinson.secondkill.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.kinson.secondkill.domain.OrderEntity;
//import com.kinson.secondkill.domain.SeckillGoodsEntity;
//import com.kinson.secondkill.domain.SeckillOrderEntity;
//import com.kinson.secondkill.domain.UserEntity;
//import com.kinson.secondkill.domain.vo.GoodsVo;
//import com.kinson.secondkill.domain.vo.OrderDetailVo;
//import com.kinson.secondkill.enums.RespBeanEnum;
//import com.kinson.secondkill.exception.GlobalException;
//import com.kinson.secondkill.mapper.IOrderMapper;
//import com.kinson.secondkill.service.IGoodsService;
//import com.kinson.secondkill.service.IOrderService;
//import com.kinson.secondkill.service.ISecKillGoodsService;
//import com.kinson.secondkill.service.ISecKillOrderService;
//import com.kinson.secondkill.utils.JsonUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//
///**
// * @author
// * @Describe 订单服务接口实现
// * @date
// */
//@Service
//@Slf4j(topic = "OrderServiceImpl2")
//// 当有多个相同类型的bean时,使用@Primary来赋予bean更高的优先级
//@Primary
//public class OrderServiceImpl2 extends ServiceImpl<IOrderMapper, OrderEntity> implements IOrderService {
//
//    @Autowired
//    private ISecKillGoodsService secKillGoodsService;
//
//    @Autowired
//    private IOrderMapper orderMapper;
//
//    @Autowired
//    private ISecKillOrderService secKillOrderService;
//
//    @Autowired
//    private IGoodsService goodsService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Override
//    @Transactional
//    public OrderEntity secKill(UserEntity user, GoodsVo goods) {
//
//        // 秒杀商品表减库存
//        SeckillGoodsEntity secKillGoods = secKillGoodsService.getOne(new QueryWrapper<SeckillGoodsEntity>()
//                .eq("goods_id", goods.getId()));
//        Integer stockCurCount = secKillGoods.getStockCount();
//        int stockRemainCount = stockCurCount - 1;
//        log.info("用户{}抢购商品{}的当前库存为{},线程为[{}:{}]", user.getId(), goods.getId(), secKillGoods.getStockCount(),
//                Thread.currentThread().getId(), Thread.currentThread().getName());
//        secKillGoods.setStockCount(stockRemainCount);
//        // secKillGoodsService.updateById(secKillGoods);
//        // 减库存时判断库存是否足够(库存超卖)
//        boolean secKillGoodsResult = secKillGoodsService.update(new UpdateWrapper<SeckillGoodsEntity>()
//                .set("stock_count", stockRemainCount)
//                .eq("id", secKillGoods.getId())
//                // 乐观 只修前面查询的库粗数量
//                .eq("stock_count", stockCurCount)
//                .gt("stock_count", 0));
//        log.info("用户{}抢购商品{}更新库存数量为{}的结果为{},线程为[{}:{}]", user.getId(), goods.getId(), stockRemainCount,
//                secKillGoodsResult, Thread.currentThread().getId(), Thread.currentThread().getName());
//        if (!secKillGoodsResult) {
//            log.info("用户{}抢购商品{}库存不足", user.getId(), goods.getId());
//            return null;
//        }
//
//        // 生成订单
//        OrderEntity order = new OrderEntity();
//        order.setUserId(user.getId());
//        order.setGoodsId(goods.getId());
//        order.setDeliveryAddrId(0L);
//        order.setGoodsName(goods.getGoodsName());
//        order.setGoodsCount(1);
//        order.setGoodsPrice(secKillGoods.getSeckillPrice());
//        order.setOrderChannel(1);
//        order.setStatus(0);
//        order.setCreateDate(new Date());
//        orderMapper.insert(order);
//        log.info("用户{}抢购商品{}成功，生成订单为{},线程为[{}:{}]", user.getId(), goods.getId(), order.getId(),
//                Thread.currentThread().getId(), Thread.currentThread().getName());
//
//        // 生成秒杀订单
//        SeckillOrderEntity secKillOrder = new SeckillOrderEntity();
//        secKillOrder.setOrderId(order.getId());
//        secKillOrder.setUserId(user.getId());
//        secKillOrder.setGoodsId(goods.getId());
//        secKillOrderService.save(secKillOrder);
//        log.info("用户{}抢购商品{}成功，生成秒杀订单为{},线程为[{}:{}]", user.getId(), goods.getId(), secKillOrder.getId(),
//                Thread.currentThread().getId(), Thread.currentThread().getName());
//        // 将秒杀订单信息存入Redis，方便判断是否重复抢购时进行查询
//        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(),
//                JsonUtil.object2JsonStr(secKillOrder));
//
//        return order;
//    }
//
//    /**
//     * 订单详情
//     *
//     * @param orderId
//     * @return
//     */
//    @Override
//    public OrderDetailVo detail(Long orderId) {
//        if (null == orderId) {
//            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
//        }
//        OrderEntity order = orderMapper.selectById(orderId);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
//        OrderDetailVo detail = new OrderDetailVo();
//        detail.setGoodsVo(goodsVo);
//        detail.setOrder(order);
//
//        return detail;
//    }
//}
