package com.kinson.secondkill.mq;

import com.kinson.secondkill.domain.SecKillMessage;
import com.kinson.secondkill.domain.SeckillOrderEntity;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author
 * @Describe MQ消息接收处理类
 * @date
 */
@Component
@Slf4j(topic = "MQReceiver")
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    /**
     * 消息接收处理订单秒杀
     *
     * @param message
     */
    @RabbitListener(queues = "secKillQueue")
    public void receive(String message) {
        log.info("接收消息：" + message);
        SecKillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SecKillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        UserEntity user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            log.warn("用户{}抢购商品{}库存{}不足", user.getId(), goodsId, goodsVo.getStockCount());
            return;
        }
        // 判断是否重复抢购
        SeckillOrderEntity secKillOrder = (SeckillOrderEntity) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (secKillOrder != null) {
            log.warn("用户{}已抢购商品{},不能重复抢购", user.getId(), goodsId);
            return;
        }
        // 下单操作
        orderService.secKill(user, goodsVo);
    }
}
