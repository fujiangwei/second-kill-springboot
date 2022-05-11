package com.kinson.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinson.secondkill.domain.RespBean;
import com.kinson.secondkill.domain.UserEntity;
import com.kinson.secondkill.domain.vo.LoginVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author
 * @Describe 用户接口
 * @date
 */
public interface IUserService extends IService<UserEntity> {
    /**
     * 登录方法
     *
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     *	更新密码
     *	@param userTicket
     *	@param id
     *	@param password
     *	@return
     */
    RespBean updatePassword(String userTicket,Long id,String password);

    /**
     * 根据cookie获取用户
     *
     * @param userTicket
     * @param request
     * @param response
     * @return
     */
    UserEntity getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    /**
     * 更新密码
     *
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     *
     * @param userTicket
     * @param request
     * @param response
     * @return
     */
    UserEntity getByUserTicket(String userTicket, HttpServletRequest request, HttpServletResponse response);

}
