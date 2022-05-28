package com.kinson.secondkill.controller;

import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.SecKillMessage;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.mq.MQSender;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.service.ISecKillGoodsService;
import com.kinson.secondkill.service.ISecKillOrderService;
import com.kinson.secondkill.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 秒杀优化
 *
 * @author
 * @date
 */
@Slf4j(topic = "SeKillController")
@Controller
@RequestMapping("/secKill")
public class SeKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISecKillOrderService secKillOrderService;
    @Autowired
    private ISecKillGoodsService secKillGoodsService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;

    /**
     * 内存标记,减少Redis访问
     */
    private Map<Long, Boolean> EMPTY_STOCK_MAP = new HashMap<>();

    /**
     * 秒杀
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSecKill2", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill2(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("用户信息为空，待秒杀商品{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断是否重复抢购
        String secKillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (StringUtils.isNotEmpty(secKillOrderJson)) {
            log.warn("用户{}已秒杀商品{},同一用户只能秒杀一件", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 内存标记,减少Redis访问
        if (EMPTY_STOCK_MAP.get(goodsId)) {
            log.warn("用户{}秒杀商品{}库存不足", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 预减库存
        Long stock = valueOperations.decrement("secKillGoods:" + goodsId);
        if (stock < 0) {
            log.warn("用户{}秒杀商品{}库存不足,更新内存标识", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            valueOperations.increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 请求入队，立即返回排队中
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * 使用lua脚本优化redis预减库存
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("用户信息为空，待秒杀商品{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断是否重复抢购
        String secKillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(secKillOrderJson)) {
            log.warn("用户{}已秒杀商品{},同一用户只能秒杀一件", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 内存标记,减少Redis访问
        if (EMPTY_STOCK_MAP.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 预减库存（使用lua脚本实现）
        Long stock = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            log.warn("用户{}秒杀商品{}库存不足,更新内存标识", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 请求入队，立即返回排队中
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * 获取秒杀结果
     *
     * @param user
     * @param goodsId
     * @return orderId:成功，-1：秒杀失败，0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("用户信息为空，待秒杀商品{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = secKillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    @RequestMapping(value = "/reSet", method = RequestMethod.GET)
    @ResponseBody
    public RespBean reSetStock() {

        // 删除所有的缓存订单
        this.prefixMatchDel("order:");

        // 查询所有的商品数据
        List<GoodsVo> goodsVos = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(goodsVos)) {
            log.warn("待重置的商品数据为空");
            return RespBean.success(goodsVos.size());
        }

        // 设置商品缓存库存
        goodsVos.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EMPTY_STOCK_MAP.put(goodsVo.getId(), false);
        });

        return RespBean.success(goodsVos.size());
    }

    /**
     * 前缀删除
     *
     * @param key
     */
    public void prefixMatchDel(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 系统初始化，把商品库存数量加载到Redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(),
                    goodsVo.getStockCount());
            EMPTY_STOCK_MAP.put(goodsVo.getId(), false);
        });
    }
}