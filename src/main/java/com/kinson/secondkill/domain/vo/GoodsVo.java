package com.kinson.secondkill.domain.vo;

import com.kinson.secondkill.domain.GoodsEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @Describe 商品出参
 * @date
 */
@Data
public class GoodsVo extends GoodsEntity {
    /**
     * 秒杀价格
     **/
    private BigDecimal seckillPrice;

    /**
     * 剩余数量
     **/
    private Integer stockCount;

    /**
     * 开始时间
     **/
    private Date startDate;

    /**
     * 结束时间
     **/
    private Date endDate;
}
