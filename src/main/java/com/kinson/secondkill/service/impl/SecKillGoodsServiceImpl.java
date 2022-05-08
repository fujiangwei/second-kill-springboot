package com.kinson.secondkill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinson.secondkill.domain.SeckillGoodsEntity;
import com.kinson.secondkill.mapper.ISecKillGoodsMapper;
import com.kinson.secondkill.service.ISecKillGoodsService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品表接口服务实现类
 *
 * @author
 * @date
 */
@Service
@Primary
public class SecKillGoodsServiceImpl extends ServiceImpl<ISecKillGoodsMapper, SeckillGoodsEntity> implements ISecKillGoodsService {

}
