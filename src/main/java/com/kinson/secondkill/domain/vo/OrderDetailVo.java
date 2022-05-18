package com.kinson.secondkill.domain.vo;

import com.kinson.secondkill.domain.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author
 * @Describe 订单详情出参
 * @date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {
    private OrderEntity order;
    private GoodsVo goodsVo;
}
