package com.ysl.miaosha.service;

import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
    //用户注册
    void register(UserModel userModel) throws BusinessException;
    //登陆检验
//    telphone:用户注册手机
//    password:用户加密后的密码
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}
