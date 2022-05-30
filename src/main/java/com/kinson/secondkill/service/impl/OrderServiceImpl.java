package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.kinson.secondkill.utils.JsonUtil;
import com.kinson.secondkill.utils.MD5Util;
import com.kinson.secondkill.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public OrderEntity secKill(UserEntity user, GoodsVo goods) {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 秒杀商品表减库存
        SeckillGoodsEntity secKillGoods = secKillGoodsService.getOne(new QueryWrapper<SeckillGoodsEntity>()
                .eq("goods_id", goods.getId()));
        if (secKillGoods.getStockCount() < 1) {
            log.info("用户{}抢购商品{}库存不足,同步设置缓存isStockEmpty为0", user.getId(), goods.getId());
            // 判断是否还有库存
            valueOperations.set("isStockEmpty:" + goods.getId(), "0");
            return null;
        }
        // 库存减一
        boolean secKillGoodsResult = secKillGoodsService.update(
                new UpdateWrapper<SeckillGoodsEntity>()
                        .setSql("stock_count = stock_count- 1")
                        .eq("goods_id", goods.getId())
                        .gt("stock_count", 0));
        if (!secKillGoodsResult) {
            log.info("用户{}抢购商品{}库存不足", user.getId(), goods.getId());
            return null;
        }

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
        valueOperations.set("order:" + user.getId() + ":" + goods.getId(),
                JsonUtil.object2JsonStr(secKillOrder));

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

    /**
     * 验证请求地址
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(UserEntity user, Long goodsId, String path) {
        if (user == null || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("secKillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    /**
     * 生成秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(UserEntity user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("secKillPath:" + user.getId() + ":" +
                goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验验证码
     *
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public Boolean checkCaptcha(UserEntity user, Long goodsId, String captcha) {
        if (StringUtils.isEmpty(captcha) || null == user || goodsId < 0) {
            return false;
        }

        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return redisCaptcha.equals(captcha);
    }
}
