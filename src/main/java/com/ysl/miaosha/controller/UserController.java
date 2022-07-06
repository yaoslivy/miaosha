package com.ysl.miaosha.controller;


import com.ysl.miaosha.controller.viewobject.UserVO;
import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.error.EmBusinessError;
import com.ysl.miaosha.response.CommonReturnType;
import com.ysl.miaosha.service.UserService;
import com.ysl.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController{
    @Autowired
    UserService userService;

    @RequestMapping("/get")
    @ResponseBody //使用在单独的方法上的，需要哪个方法返回json数据格式，就在哪个方法上使用
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对应用户信息不存在
        if(userModel == null){
//             userModel.setEncrptPassword("123");
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    //将model对象转换为OV对象返回
    public UserVO convertFromModel(UserModel userModel) {
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }




}
