package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.mapper.ISecKillOrderMapper;
import com.kinson.secondkill.service.ISecKillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 秒杀订单表接口服务实现类
 *
 * @author
 * @date
 */
@Service
@Primary
public class SecKillOrderServiceImpl extends ServiceImpl<ISecKillOrderMapper, SeckillOrderEntity> implements ISecKillOrderService {

    @Autowired
    private ISecKillOrderMapper secKillOrderMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Long getResult(UserEntity user, Long goodsId) {
        // 查询秒杀订单信息
        SeckillOrderEntity secKillOrder = secKillOrderMapper.selectOne(new QueryWrapper<SeckillOrderEntity>()
                .eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (null != secKillOrder) {
            return secKillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }
    }
}
