package com.ysl.miaosha.service.impl;

import com.ysl.miaosha.dao.PromoDOMapper;
import com.ysl.miaosha.dataobject.PromoDO;
import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.error.EmBusinessError;
import com.ysl.miaosha.service.ItemService;
import com.ysl.miaosha.service.PromoService;
import com.ysl.miaosha.service.UserService;
import com.ysl.miaosha.service.model.ItemModel;
import com.ysl.miaosha.service.model.PromoModel;
import com.ysl.miaosha.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        ////获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        // dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return  null;
        }
        // 判断当前时间是否秒杀活动即将开始，或正在进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0) {
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //将缓存同步到redis中
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());

        //将大闸的限制数字设置到redis内
        redisTemplate.opsForValue().set("promo_door_count_"+promoId, itemModel.getStock().intValue()*5);
    }

    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException {
        // 判断是否库存已经售罄，若标识存在，则直接返回下单失败
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
//            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
            return  null;
        }

        ////获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        // dataobject -> model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return  null;
        }
        // 判断当前时间是否秒杀活动即将开始，或正在进行
        if (promoModel.getStartDate().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行
        if (promoModel.getStatus() != 2) {
            return  null;
        }
        //判断item信息和用户是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return null;
        }
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel == null){
            return  null;
        }
        //获取秒杀大闸的count数量
        Long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if (result < 0)
            return null;

        //生成token并且存入redis内，并给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-", "");
        //给下单接口做验证使用
        redisTemplate.opsForValue().set("promo_token_" + promoId + "_userid_" + userId + "_itemid_"+itemId, token);
        redisTemplate.expire("promo_token_" + promoId + "_userid_" + userId + "_itemid_"+itemId, 5, TimeUnit.MINUTES);
        return token;
    }

    //dataobject->model
    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return  null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }

}
