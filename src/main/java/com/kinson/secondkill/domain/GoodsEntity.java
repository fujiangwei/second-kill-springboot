package com.kinson.secondkill.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 * @Describe 商品表映射实体类
 * @date
 */
@Data
@TableName("t_goods")
public class GoodsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商品名称 **/
    private String goodsName;

    /** 商品标题 **/
    private String goodsTitle;

    /** 商品图片 **/
    private String goodsImg;

    /** 商品详情 **/
    private String goodsDetail;

    /** 商品价格 **/
    private BigDecimal goodsPrice;

    /** 商品库存，-1表示没有限制 **/
    private Integer goodsStock;

}
