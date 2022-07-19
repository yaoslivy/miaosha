package com.ysl.miaosha.controller;

import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.error.EmBusinessError;
import com.ysl.miaosha.mq.MqProducer;
import com.ysl.miaosha.response.CommonReturnType;
import com.ysl.miaosha.service.ItemService;
import com.ysl.miaosha.service.OrderService;
import com.ysl.miaosha.service.model.OrderModel;
import com.ysl.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ItemService itemService;

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId) throws BusinessException {
        //获取用户登陆信息
//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(isLogin == null || !isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
//        }
//        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

//        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId,promoId, amount);
        // 判断是否库存已经售罄，若标识存在，则直接返回下单失败
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //在下单之前先初始化库存流水
        String stockLogId = itemService.initStockLog(itemId, amount);

        //再去完成对应的下单事务型消息机制–
        boolean res = mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId);
        if (!res) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        }

        return CommonReturnType.create(null);
    }
}
