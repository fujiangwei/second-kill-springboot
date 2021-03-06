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
 * η§ζδΌε
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
     * εε­ζ θ?°,εε°Redisθ?Ώι?
     */
    private Map<Long, Boolean> EMPTY_STOCK_MAP = new HashMap<>();

    /**
     * η§ζ
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSecKill2", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill2(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("η¨ζ·δΏ‘ζ―δΈΊη©ΊοΌεΎη§ζεε{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // ε€ζ­ζ―ε¦ιε€ζ’θ΄­
        String secKillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (StringUtils.isNotEmpty(secKillOrderJson)) {
            log.warn("η¨ζ·{}ε·²η§ζεε{},εδΈη¨ζ·εͺθ½η§ζδΈδ»Ά", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // εε­ζ θ?°,εε°Redisθ?Ώι?
        if (EMPTY_STOCK_MAP.get(goodsId)) {
            log.warn("η¨ζ·{}η§ζεε{}εΊε­δΈθΆ³", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ι’εεΊε­
        Long stock = valueOperations.decrement("secKillGoods:" + goodsId);
        if (stock < 0) {
            log.warn("η¨ζ·{}η§ζεε{}εΊε­δΈθΆ³,ζ΄ζ°εε­ζ θ―", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            valueOperations.increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // θ―·ζ±ε₯ιοΌη«ε³θΏεζιδΈ­
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * δ½Ώη¨luaθζ¬δΌεredisι’εεΊε­
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("η¨ζ·δΏ‘ζ―δΈΊη©ΊοΌεΎη§ζεε{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        // ε€ζ­ζ―ε¦ιε€ζ’θ΄­
        String secKillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(secKillOrderJson)) {
            log.warn("η¨ζ·{}ε·²η§ζεε{},εδΈη¨ζ·εͺθ½η§ζδΈδ»Ά", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // εε­ζ θ?°,εε°Redisθ?Ώι?
        if (EMPTY_STOCK_MAP.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ι’εεΊε­οΌδ½Ώη¨luaθζ¬ε?η°οΌ
        Long stock = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            log.warn("η¨ζ·{}η§ζεε{}εΊε­δΈθΆ³,ζ΄ζ°εε­ζ θ―", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // θ―·ζ±ε₯ιοΌη«ε³θΏεζιδΈ­
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * θ·εη§ζη»ζ
     *
     * @param user
     * @param goodsId
     * @return orderId:ζεοΌ-1οΌη§ζε€±θ΄₯οΌ0οΌζιδΈ­
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("η¨ζ·δΏ‘ζ―δΈΊη©ΊοΌεΎη§ζεε{}", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = secKillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    @RequestMapping(value = "/reSet", method = RequestMethod.GET)
    @ResponseBody
    public RespBean reSetStock() {

        // ε ι€ζζηηΌε­θ?’ε
        this.prefixMatchDel("order:");

        // ζ₯θ―’ζζηεεζ°ζ?
        List<GoodsVo> goodsVos = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(goodsVos)) {
            log.warn("εΎιη½?ηεεζ°ζ?δΈΊη©Ί");
            return RespBean.success(goodsVos.size());
        }

        // θ?Ύη½?εεηΌε­εΊε­
        goodsVos.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EMPTY_STOCK_MAP.put(goodsVo.getId(), false);
        });

        return RespBean.success(goodsVos.size());
    }

    /**
     * εηΌε ι€
     *
     * @param key
     */
    public void prefixMatchDel(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * θ·εη§ζε°ε
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
     * θ·εη§ζε°ε2
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
        // ιεΆθ?Ώι?ζ¬‘ζ°οΌ5η§εθ?Ώι?5ζ¬‘
        String uri = request.getRequestURI();
        // ζΉδΎΏζ΅θ―captcha = "0";
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
            log.warn("η¨ζ·{}η§ζεε{}ηιͺθ―η {}ζ ‘ιͺιθ――", user.getId(), goodsId, captcha);
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * θ·εη§ζε°ε
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
            log.warn("η§ζεε{}ηη¨ζ·δΏ‘ζ―δΈΊη©Ί", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        // ζΉδΎΏζ΅θ―captcha = "0";
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            log.warn("η¨ζ·{}η§ζεε{}ηιͺθ―η {}ζ ‘ιͺιθ――", user.getId(), goodsId, captcha);
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * η§ζ
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, UserEntity user, Long goodsId) {
        if (user == null) {
            log.warn("η§ζεε{}ηη¨ζ·δΏ‘ζ―δΈΊη©Ί", goodsId);
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            log.warn("η¨ζ·{}η§ζεε{}ηη§ζθ·―εΎιθ――", user.getId(), goodsId, path);
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // ε€ζ­ζ―ε¦ιε€ζ’θ΄­
        String secKillOrderJson = (String) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(secKillOrderJson)) {
            log.warn("η¨ζ·{}ε·²η§ζεε{},εδΈη¨ζ·εͺθ½η§ζδΈδ»Ά", user.getId(), goodsId);
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // εε­ζ θ?°,εε°Redisθ?Ώι?
        if (EMPTY_STOCK_MAP.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ι’εεΊε­
        /*Long stock = valueOperations.decrement("secKillGoods:" + goodsId);
        if (stock < 0) {
            EMPTY_STOCK_MAP.put(goodsId, true);
            valueOperations.increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }*/
        // δ½Ώη¨luaθζ¬ε?η°εΊε­εε°
        Long stock = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock <= 0) {
            log.warn("η¨ζ·{}ε·²η§ζεε{}εΊε­δΈθΆ³", user.getId(), goodsId);
            EMPTY_STOCK_MAP.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // θ―·ζ±ε₯ιοΌη«ε³θΏεζιδΈ­
        SecKillMessage message = new SecKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     * ιͺθ―η 
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
        // θ?Ύη½?θ―·ζ±ε€΄δΈΊθΎεΊεΎηη±»ε
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // ηζιͺθ―η οΌε°η»ζζΎε₯redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);

        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("ιͺθ―η ηζε€±θ΄₯", e.getMessage());
        }
    }

    /**
     * η³»η»εε§εοΌζεεεΊε­ζ°ιε θ½½ε°Redis
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
