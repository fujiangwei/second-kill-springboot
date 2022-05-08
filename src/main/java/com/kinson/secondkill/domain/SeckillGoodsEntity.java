package com.kinson.secondkill.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品表
 *
 * @author
 * @date
 */
@Data
@TableName("t_seckill_goods")
public class SeckillGoodsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀商品ID
     **/
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     **/
    private Long goodsId;

    /**
     * 秒杀家
     **/
    private BigDecimal seckillPrice;

    /**
     * 库存数量
     **/
    private Integer stockCount;

    /**
     * 秒杀开始时间
     **/
    private LocalDateTime startDate;

    /**
     * 秒杀结束时间
     **/
    private LocalDateTime endDate;
}
