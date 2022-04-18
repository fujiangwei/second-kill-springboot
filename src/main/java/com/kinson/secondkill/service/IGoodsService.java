package com.kinson.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinson.secondkill.domain.GoodsEntity;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.GoodsVo;
import com.kinson.secondkill.domain.vo.LoginVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author
 * @Describe 商品接口
 * @date
 */
public interface IGoodsService extends IService<GoodsEntity> {

    /**
     * 返回商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
