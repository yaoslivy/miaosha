package com.ysl.miaosha.controller;


import com.ysl.miaosha.controller.viewobject.UserVO;
import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.error.EmBusinessError;
import com.ysl.miaosha.response.CommonReturnType;
import com.ysl.miaosha.service.UserService;
import com.ysl.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController{
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    //用户注册接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telphone)||
            StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登陆服务，用来校验用户登陆是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));

        //将登陆凭证加入到用户登陆成功的session内

        //修改成若用户登陆验证成功后将对应的登陆信息和登陆凭证一起存入redis中
        //生成登陆凭证token， UUID
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-", "");
        //建立了token和用户登陆态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);

//        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
//        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

//        return CommonReturnType.create(null);

        //下发token
        return CommonReturnType.create(uuidToken);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone) {
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt =  random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //将OTP验证码通过短信通道发送给用户,省略
        System.out.println("telphone = " + telphone + " & otpCode = "+otpCode);

        return CommonReturnType.create(otpCode);
    }

    //根据用户id查询数据库接口
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
