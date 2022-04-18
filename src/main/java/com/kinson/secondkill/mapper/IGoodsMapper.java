package com.kinson.secondkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinson.secondkill.domain.GoodsEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;

import java.util.List;

/**
 * @author
 * @Describe 商品表Mapper
 * @date
 */
public interface IGoodsMapper extends BaseMapper<GoodsEntity> {
    /**
     * 返回商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 根据商品序号查询商品信息
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
