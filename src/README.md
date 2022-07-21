

# 启动redis

# 启动mysql

# 启动rocketmq
- nohup sh /Volumes/Files/ysl/opt/rocketmq-4.9.3/bin/mqnamesrv 1>/dev/null 2>&1 &
- tail -f ~/logs/rocketmqlogs/namesrv.log

- nohup sh /Volumes/Files/ysl/opt/rocketmq-4.9.3/bin/mqbroker -n localhost:9876 1>/dev/null 2>&1 &
- tail -f ~/logs/rocketmqlogs/broker.log 


- sh /Volumes/Files/ysl/opt/rocketmq-4.9.3/bin/mqshutdown broker
- sh /Volumes/Files/ysl/opt/rocketmq-4.9.3/bin/mqshutdown namesrv

# 主要测试接口和注意事项
1. 商品详情页：http://192.168.1.113/item/get?id=6
2. 秒杀接口：http://192.168.1.113/order/generatetoken?token=6598aff0f09048218750fff462a6d5ba
3. 在测试秒杀接口是需要注意先将秒杀商品信息(秒杀商品表中id)入redis中，http://43.142.116.110:9000/item/publishpromo?id=2