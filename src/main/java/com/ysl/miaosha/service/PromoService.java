package com.ysl.miaosha.service;

import com.ysl.miaosha.service.model.PromoModel;

public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);
    //活动发布
    void publishPromo(Integer promoId);
}
