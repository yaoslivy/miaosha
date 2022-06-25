package com.ysl.miaosha.controller;


import com.ysl.miaosha.controller.viewobject.UserVO;
import com.ysl.miaosha.service.UserService;
import com.ysl.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("user")
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/get")
    @ResponseBody //使用在单独的方法上的，需要哪个方法返回json数据格式，就在哪个方法上使用
    public UserVO getUser(@RequestParam(name="id") Integer id) {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //将核心领域模型用户对象转化为可供UI使用的viewobject
        return convertFromModel(userModel);
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
