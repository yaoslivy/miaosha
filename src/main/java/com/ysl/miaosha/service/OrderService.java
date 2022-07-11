package com.ysl.miaosha.service;

import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId,Integer itemId,Integer amount) throws BusinessException;
}
