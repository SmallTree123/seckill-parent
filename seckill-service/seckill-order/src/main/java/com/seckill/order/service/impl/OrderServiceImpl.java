package com.seckill.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seckill.goods.feign.SkuFeign;
import com.seckill.goods.pojo.Sku;
import com.seckill.message.feign.MessageFeign;
import com.seckill.order.config.RedissonDistributedLocker;
import com.seckill.order.dao.OrderMapper;
import com.seckill.order.pojo.Order;
import com.seckill.order.service.OrderService;
import com.seckill.util.IdWorker;
import com.seckill.util.Result;
import com.seckill.util.StatusCode;
import com.seckill.util.TimeUtil;
import io.seata.spring.annotation.GlobalTransactional;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/****
 * @Author:www.itheima.com
 * @Description:Order业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;

    @Autowired
    private MessageFeign messageFeign;


    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /****
     * 热点商品下单
     * @param orderMap
     * @return
     */
    @Override
    public void hotAdd(Map<String, String> orderMap) throws IOException {
        //消息封装对象
        Map<String,Object> messageMap = new HashMap<String,Object>();

        String username = orderMap.get("username");
        String id = orderMap.get("id");
        int totalNum = Integer.parseInt(orderMap.get("num"));

        //Redis中对应的key
        String key="SKU_"+id;
        String lockkey="LOCKSKU_"+id;
        String userKey="USER"+username+"ID"+id;
        String queueKey = "SKU_"+id+":queue";
        String numKey = "SKU_"+id+":num";

        //如果key在redis缓存，则表示商品信息在Redis中进行操作
        boolean bo = redissonDistributedLocker.tryLock(lockkey, 10, 10, TimeUnit.MINUTES);
        if(bo){
            if(redisTemplate.hasKey(key+":id")){
                //获取商品数量
                Object o = redisTemplate.opsForValue().get(numKey);
                Integer num = Integer.parseInt(o.toString());
                if(num<=0){
                    //商品售罄通知
                    messageMap.put("code",20001);
                    messageMap.put("message","商品已售罄");
                    messageFeign.send(username,JSON.toJSONString(messageMap));
                    return;
                }
                Result<Sku> skuResult =skuFeign.findById(id);
                Sku sku = skuResult.getData();

                //1.创建Order
                Order order = new Order();
                order.setTotalNum(totalNum);
                order.setCreateTime(new Date());
                order.setUpdateTime(order.getCreateTime());
                order.setId("No"+idWorker.nextId());
                order.setOrderStatus("0");
                order.setPayStatus("0");
                order.setConsignStatus("0");
                order.setSkuId(id);
                order.setName(sku.getName());
                order.setPrice(sku.getSeckillPrice()*order.getTotalNum());
                orderMapper.insert(order);

                //2.Redis中对应的num递减
                skuFeign.commonDcount(id,totalNum);
                num-=totalNum;
                //2.清理用户排队信息
//                redisTemplate.opsForValue().set(queueKey,0);
//                redisTemplate.opsForValue().set(numKey,num);
//                redisTemplate.delete(numKey);


                //3.记录用户购买过该商品,24小时后过期
//                redisTemplate.opsForValue().set(userKey,"",1,TimeUnit.DAYS);

                //抢单成功通知
                messageMap.put("code",200);
                messageMap.put("message","抢单成功！");
                messageFeign.send(username,JSON.toJSONString(messageMap));
            }

            //释放锁
            redissonDistributedLocker.unLock(lockkey);
//            return;
        }

        //抢单失败通知
        messageMap.put("code",20001);
        messageMap.put("message","抢单失败！");
        messageFeign.send(username,JSON.toJSONString(messageMap));
    }

    /***
     * 添加订单
     * @param order
     * @return
     */
    @Override
//    @GlobalTransactional
    public int add(Order order) {
        ReentrantLock reentrantLock = new ReentrantLock();
        try {
            String userKey="USER"+order.getUsername()+"ID"+order.getSkuId();
            //同一用户24小时只能不能重复消费
//        if (null != redisTemplate.opsForValue().get(userKey)){
//            return StatusCode.ORDER_UNION;
//        }

            reentrantLock.lock();
            String numKey = "SKU_"+order.getSkuId()+":num";
            Long decrement = redisTemplate.opsForValue().decrement(numKey, order.getTotalNum().longValue());
            if(decrement <= 0){
                reentrantLock.unlock();
                return StatusCode.DECOUNT_NUM;
            }

            //1.递减库存
            Result<Sku> dcount = skuFeign.dcount(order.getSkuId(), order.getTotalNum());
            //2.递减成功->下单->记录当前用户抢单的时间点，24小时内不能抢购该商品
            if(dcount.getCode()== StatusCode.DECOUNT_OK){
    //            int q=10/0;
                Sku sku = dcount.getData();
                //下单
                //order.setId("No"+idWorker.nextId());
                order.setOrderStatus("0");
                order.setPayStatus("0");
                order.setConsignStatus("0");
                order.setSkuId(sku.getId());
                order.setName(sku.getName());
                order.setPrice(sku.getSeckillPrice()*order.getTotalNum());
                orderMapper.insert(order);
                //记录当前用户抢单的时间点，24小时内不能抢购该商品
                redisTemplate.opsForValue().set(userKey,"",1,TimeUnit.DAYS);
                return StatusCode.ORDER_OK;
            }else{
                //3.递减失败
                //405库存不足
                if(dcount.getCode()==StatusCode.DECOUNT_NUM){
                    return StatusCode.DECOUNT_NUM;
                }else if(dcount.getCode()==StatusCode.DECOUNT_HOT){
                    //205商品热点
                    String key = "SKU_"+order.getSkuId()+":queue:"+atomicInteger.getAndIncrement();
                    Long increment = redisTemplate.opsForValue().increment(key, 1);
                    if(increment==1){
                        //执行排队
                        Map<String,String> queueMap = new HashMap<String,String>();
                        queueMap.put("username",order.getUsername());
                        queueMap.put("id",order.getSkuId());
                        queueMap.put("num",order.getTotalNum().toString());
                        kafkaTemplate.send("neworder", JSON.toJSONString(queueMap));
                    }
                    return StatusCode.ORDER_QUEUE;
                }

                //0
                return dcount.getCode();
            }
        } finally {
            reentrantLock.unlock();
            //预减库存失败补偿机制
            String numKey = "SKU_"+order.getSkuId()+":num";
            Sku sku = skuFeign.findById(order.getSkuId()).getData();
            Object o = redisTemplate.opsForValue().get(numKey);
            int num = Integer.parseInt(o.toString());
            if (num != sku.getSeckillNum()){
                redisTemplate.opsForValue().set(numKey,sku.getSeckillNum());
            }
        }
    }

    /**
     * Order条件+分页查询
     *
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        QueryWrapper<Order> example = createExample(order);

        // 排序
        example.orderByDesc("createTime");

        //订单查询
        List<Order> orders = orderMapper.selectList(example);

        //查询每个订单的产品信息
        for (Order od : orders) {
            Result<Sku> skuResult = skuFeign.findById(od.getSkuId());
            if(skuResult.getData()!=null){
                od.setSku(skuResult.getData());
            }
        }

        //执行搜索
        return new PageInfo<Order>(orders);
    }

    @Override
    public Order findById(String id) {
        return orderMapper.selectById(id);
    }

    @Override
    public void pay(String id) {
        Order order = new Order();
        order.setId(id);
        order.setPayStatus("1");    //已支付
        orderMapper.updateById(order);
    }

    /**
     * Order构建查询对象
     *
     * @param order
     * @return
     */
    public QueryWrapper<Order> createExample(Order order) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getId())) {
                queryWrapper.eq("id", order.getId());
            }
            // 数量合计
            if (!StringUtils.isEmpty(order.getTotalNum())) {
                queryWrapper.eq("totalNum", order.getTotalNum());
            }
            // 支付类型，1、在线支付、0 货到付款
            if (!StringUtils.isEmpty(order.getPayType())) {
                queryWrapper.eq("payType", order.getPayType());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                //criteria.eq()("createTime", order.getCreateTime());
                queryWrapper.gt("createTime",order.getCreateTime());
                //当天时间
                queryWrapper.lt("createTime", TimeUtil.addDateHour(order.getCreateTime(),24));
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                queryWrapper.eq("updateTime", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPayTime())) {
                queryWrapper.eq("payTime", order.getPayTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                queryWrapper.eq("consignTime", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                queryWrapper.eq("endTime", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                queryWrapper.eq("closeTime", order.getCloseTime());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiverContact())) {
                queryWrapper.eq("receiverContact", order.getReceiverContact());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                queryWrapper.eq("receiverMobile", order.getReceiverMobile());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(order.getReceiverAddress())) {
                queryWrapper.eq("receiverAddress", order.getReceiverAddress());
            }
            // 交易流水号
            if (!StringUtils.isEmpty(order.getTransactionId())) {
                queryWrapper.eq("transactionId", order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if (!StringUtils.isEmpty(order.getOrderStatus())) {
                queryWrapper.eq("orderStatus", order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if (!StringUtils.isEmpty(order.getPayStatus())) {
                queryWrapper.eq("payStatus", order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if (!StringUtils.isEmpty(order.getConsignStatus())) {
                queryWrapper.eq("consignStatus", order.getConsignStatus());
            }
            // 是否删除
            if (!StringUtils.isEmpty(order.getIsDelete())) {
                queryWrapper.eq("isDelete", order.getIsDelete());
            }
            // 
            if (!StringUtils.isEmpty(order.getSkuId())) {
                queryWrapper.eq("skuId", order.getSkuId());
            }
            // 
            if (!StringUtils.isEmpty(order.getName())) {
                queryWrapper.like("name", "%" + order.getName() + "%");
            }
            // 
            if (!StringUtils.isEmpty(order.getPrice())) {
                queryWrapper.eq("price", order.getPrice());
            }
            //
            if (!StringUtils.isEmpty(order.getUsername())) {
                queryWrapper.eq("username", order.getUsername());
            }
        }
        return queryWrapper;
    }
}
