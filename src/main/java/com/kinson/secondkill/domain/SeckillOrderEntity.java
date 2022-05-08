package com.kinson.secondkill.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀订单表
 *
 * @author
 * @date
 */
@Data
@TableName("t_seckill_order")
public class SeckillOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀订单ID
     **/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     **/
    private Long userId;

    /**
     * 订单ID
     **/
    private Long orderId;

    /**
     * 商品ID
     **/
    private Long goodsId;
}
