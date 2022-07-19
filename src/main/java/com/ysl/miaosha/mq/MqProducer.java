package com.ysl.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.ysl.miaosha.dao.StockLogDOMapper;
import com.ysl.miaosha.dataobject.StockLogDO;
import com.ysl.miaosha.error.BusinessException;
import com.ysl.miaosha.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}") //注入properties中的内容
    private String nameAddr;

    @Value("${mq.topicname}") //注入properties中的内容
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @PostConstruct //会在Bean完成初始化后被调用
    public void init() throws MQClientException {
        //完成mq producer的初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object arg) {
                //真正要做的事，创建订单
                Integer userId = (Integer)((Map)arg).get("userId");
                Integer itemId = (Integer)((Map)arg).get("itemId");
                Integer promoId = (Integer)((Map)arg).get("promoId");
                Integer amount = (Integer)((Map)arg).get("amount");
                String stockLogId = (String)((Map)arg).get("stockLogId");
                try {
                    orderService.createOrder(userId,itemId,promoId,amount, stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    //设置对应的stockLog为回滚状态
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

                    return LocalTransactionState.ROLLBACK_MESSAGE;//有异常就回滚信息
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override //若mq发现消息一直未ROLLBACK或COMMIT,就会执行该方法
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                //根据是否扣减库存成功，来判断COMMIT，ROLLBACK还是UNKNOW
                String jsonString = new String(msg.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String) map.get("stockLogId");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO == null) {
                    return LocalTransactionState.UNKNOW;
                }
                if (stockLogDO.getStatus().intValue() == 2) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (stockLogDO.getStatus().intValue() == 1) {
                    return LocalTransactionState.UNKNOW;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }
    //事务型同步库存扣减消息
    public boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId,  Integer amount, String stockLogId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        argsMap.put("stockLogId", stockLogId);

        Message message = new Message(topicName, "increase",
                JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
        TransactionSendResult sendResult = null;
        try { //发送事务型消息，但是状态不是可被消费状态，而是，会执行executeLocalTransaction方法
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return  false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
            return  false;
        } else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            return true;
        } else {
            return false;
        }
    }

    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
        try {
            producer.send(message);
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
