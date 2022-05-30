package com.kinson.secondkill.controller;

import com.kinson.secondkill.annotaions.AccessLimit;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.SecKillMessage;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.enums.RespBeanEnum;
import com.kinson.secondkill.exception.GlobalException;
import com.kinson.secondkill.mq.MQSender;
import com.kinson.secondkill.service.IGoodsService;
import com.kinson.secondkill.service.IOrderService;
import com.kinson.secondkill.service.ISecKillGoodsService;
import com.kinson.secondkill.service.ISecKillOrderService;
import com.kinson.secondkill.utils.JsonUtil;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
     *
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
     * 获取秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    /*@RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(UserEntity user, Long goodsId, String captcha) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);

        return RespBean.success(str);
    }*/

    /**
     * 获取秒杀地址2
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path2", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath2(UserEntity user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 限制访问次数，5秒内访问5次
        String uri = request.getRequestURI();
        // 方便测试captcha = "0";
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null) {
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            log.warn("用户{}秒杀商品{}的验证码{}校验错误", user.getId(), goodsId, captcha);
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * 获取秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(UserEntity user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            log.warn("秒杀商品{}的用户信息为空", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        // 方便测试captcha = "0";
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            log.warn("用户{}秒杀商品{}的验证码{}校验错误", user.getId(), goodsId, captcha);
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * 秒杀
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("秒杀商品{}的用户信息为空", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            log.warn("用户{}秒杀商品{}的秒杀路径错误", user.getId(), goodsId, path);
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
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
        // 预减库存
        /*Long stock = valueOperations.decrement("secKillGoods:" + goodsId);
        if (stock < 0) {
            EMPTY_STOCK_MAP.put(goodsId, true);
            valueOperations.increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }*/
        // 使用lua脚本实现库存减少
        Long stock = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock <= 0) {
            log.warn("用户{}已秒杀商品{}库存不足", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 请求入队，立即返回排队中
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * 验证码
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(UserEntity user, Long goodsId, HttpServletResponse response) {
        if (null == user || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 生成验证码，将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);

        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
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
