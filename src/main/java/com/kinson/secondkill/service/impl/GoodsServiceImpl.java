package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinson.secondkill.domain.GoodsEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.mapper.IGoodsMapper;
import com.kinson.secondkill.service.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author
 * @Describe 商品服务接口实现
 * @date
 */
@Service
@Slf4j(topic = "GoodsServiceImpl")
// 当有多个相同类型的bean时,使用@Primary来赋予bean更高的优先级
@Primary
public class GoodsServiceImpl extends ServiceImpl<IGoodsMapper, GoodsEntity> implements IGoodsService {

    @Autowired
    private IGoodsMapper goodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
