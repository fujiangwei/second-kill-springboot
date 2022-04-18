package com.kinson.secondkill.domain.vo;

import com.kinson.secondkill.domain.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author
 * @Describe 商品详情出参
 * @date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailVo {
    private UserEntity user;

    private GoodsVo goodsVo;

    private int secKillStatus;

    private int remainSeconds;
}
